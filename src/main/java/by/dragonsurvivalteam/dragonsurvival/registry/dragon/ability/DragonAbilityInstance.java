package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.MagicHUD;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ManaHandler;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncAbilityEnabled;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncStopCast;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import javax.annotation.Nullable;

public class DragonAbilityInstance {
    public static final int MIN_LEVEL = 0;
    /** Since {@link LevelBasedValue} does not expect a level of 0 we handle 0 as "ability is disabled" */
    public static final int MIN_LEVEL_FOR_CALCULATIONS = 1;
    public static final int MAX_LEVEL = 255;
    public static final int NO_COOLDOWN = 0;

    public static Codec<DragonAbilityInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DragonAbility.CODEC.fieldOf("ability").forGetter(DragonAbilityInstance::ability),
            Codec.INT.fieldOf("level").forGetter(DragonAbilityInstance::level),
            Codec.BOOL.optionalFieldOf("is_enabled", true).forGetter(DragonAbilityInstance::isEnabled)
    ).apply(instance, DragonAbilityInstance::new));

    private final Holder<DragonAbility> ability;
    private int level;
    private boolean isEnabled;
    private boolean manuallyDisabled;

    private int currentTick;
    private int cooldown;
    private boolean isActive;

    public DragonAbilityInstance(final Holder<DragonAbility> ability, int level) {
        this(ability, level, true);
    }

    public DragonAbilityInstance(final Holder<DragonAbility> ability, int level, boolean isEnabled) {
        this.ability = ability;
        this.level = level;
        this.isEnabled = isEnabled;

        if (isEnabled && value().activation().type() == Activation.Type.PASSIVE) {
            this.isActive = true;
        }
    }

    public Tag save(@NotNull final HolderLookup.Provider provider) {
        return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
    }

    public static @Nullable DragonAbilityInstance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
        return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
    }

    public void tick(final Player dragon) {
        if (dragon.hasInfiniteMaterials()) {
            cooldown = NO_COOLDOWN;
        } else {
            cooldown = Math.max(NO_COOLDOWN, cooldown - 1);
        }

        if(dragon instanceof ServerPlayer serverPlayer) {
            if (value().usageBlocked().map(condition -> condition.test(Condition.createContext(serverPlayer))).orElse(false)) {
                if(isEnabled()) {
                    setEnabled(false, false);
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncAbilityEnabled(ability.getKey(), false, false));
                }
            } else if(!manuallyDisabled) {
                if(!isEnabled()) {
                    setEnabled(true, false);
                    PacketDistributor.sendToPlayer(serverPlayer, new SyncAbilityEnabled(ability.getKey(), true, false));
                }
            }
        }

        if (isActive && canBeCast()) {
            tickActions(dragon);
        }
    }

    public void queueTickingSound(final SoundEvent soundEvent, final SoundSource soundSource, final Player dragon) {
        DragonSurvival.PROXY.queueTickingSound(location().withSuffix(dragon.getStringUUID()), soundEvent, soundSource, dragon);
    }

    public void stopSound(final Player dragon) {
        DragonSurvival.PROXY.stopTickingSound(location().withSuffix(dragon.getStringUUID()));
    }

    private void tickActions(final Player dragon) {
        int castTime = getCastTime();
        currentTick++;

        if (currentTick < castTime) {
            if (currentTick == 1) {
                value().activation().playChargingSound(dragon, this);
                value().activation().playStartAndChargingAnimation(dragon);
            }

            return;
        }

        if (currentTick == castTime) {
            value().activation().playStartAndLoopingSound(dragon, this);
            value().activation().playLoopingAnimation(dragon);
        }

        if (!(dragon instanceof ServerPlayer serverPlayer)) {
            // TODO :: should mana also be consumed client-side?
            //  maybe we can skip the sync that way?
            return;
        }

        if (value().usageBlocked().map(condition -> condition.test(Condition.createContext(serverPlayer))).orElse(false)) {
            stopCasting(serverPlayer);
            return;
        }

        if (currentTick < castTime) {
            return;
        }

        if (currentTick == castTime) {
            ManaHandler.consumeMana(serverPlayer, getInitialManaCost());
        }

        if (currentTick > castTime) {
            float manaCost = getContinuousManaCost(ManaCost.ManaCostType.TICKING);

            if (ManaHandler.hasEnoughMana(serverPlayer, manaCost)) {
                // TODO :: make this return a boolean and remove 'hasEnoughMana'?
                ManaHandler.consumeMana(serverPlayer, manaCost);
            } else {
                stopCasting(serverPlayer);
                return;
            }
        }

        ability.value().actions().forEach(action -> action.tick(serverPlayer, this, currentTick));

        if (value().activation().type() == Activation.Type.ACTIVE_SIMPLE) {
            stopCasting(serverPlayer);
        }
    }

    private void stopCasting(final ServerPlayer dragon) {
        value().activation().playEndSound(dragon);
        value().activation().playEndAnimation(dragon);
        MagicData magic = MagicData.getData(dragon);
        magic.stopCasting(dragon);
        // TODO: We can send back the reason we failed here to the client
        PacketDistributor.sendToPlayer(dragon, new SyncStopCast(dragon.getId(), false, true));
    }

    private float getInitialManaCost() {
        return value().activation().initialManaCost().map(cost -> cost.calculate(level)).orElse(0f);
    }

    public float getContinuousManaCost(final ManaCost.ManaCostType manaCostType) {
        Optional<ManaCost> optional = value().activation().continuousManaCost();

        if (optional.isEmpty()) {
            return 0;
        }

        ManaCost manaCost = optional.get();

        if (manaCost.manaCostType() != manaCostType) {
            return 0;
        }

        return manaCost.manaCost().calculate(level);
    }

    public boolean checkInitialManaCost(final Player dragon) {
        float manaCost = getInitialManaCost();

        if (!ManaHandler.hasEnoughMana(dragon, manaCost)) {
            releaseWithoutCooldown();

            if (dragon.level().isClientSide()) {
                MagicData magicData = MagicData.getData(dragon);
                magicData.setErrorMessageSent(true);
                MagicHUD.castingError(Component.translatable(MagicHUD.NO_MANA).withStyle(ChatFormatting.RED));
            }

            return false;
        }

        return true;
    }

    public boolean isApplyingEffects() {
        return isActive && canBeCast() && currentTick >= getCastTime();
    }

    public boolean hasEndAnimation() {
        return value().activation().animations().isPresent() && value().activation().animations().get().end().isPresent();
    }

    public boolean canBeCast() {
        return isUsable() && cooldown == NO_COOLDOWN;
    }

    public ResourceLocation getIcon() {
        if (!isEnabled) {
            return value().icon().get(0);
        }

        return ability.value().icon().get(level);
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setActive(boolean isActive, final ServerPlayer player) {
        setActive(isActive);

        if (!isActive && ability.value().activation().type() == Activation.Type.PASSIVE) {
            // Also makes sure to remove any affects that are applied by the ability
            ability.value().actions().forEach(action -> action.remove(player, this));
        }
    }

    // Used for when a client was denied from casting an ability by the server
    public void releaseWithoutCooldown() {
        currentTick = 0;
    }

    public void release(final Player dragon) {
        currentTick = 0;

        if (dragon.hasInfiniteMaterials()) {
            cooldown = NO_COOLDOWN;
        } else {
            cooldown = ability.value().getCooldown(level);
        }
    }

    public boolean isPassive() {
        return value().activation().type() == Activation.Type.PASSIVE;
    }

    public int getMaxLevel() {
        return value().getMaxLevel();
    }

    // TODO: These need to be synced in some way for MagicHUD?
    //  technically no since it just adds 1 per tick while the ability is active
    //  this can be done on both sides
    public int getCurrentCastTime() {
        return currentTick;
    }

    // TODO: These need to be synced in some way for MagicHUD?
    //  should not be needed, since the client has info about the level and would reach the same value as the server here
    public int getCastTime() {
        return value().activation().castTime().map(time -> time.calculate(level)).orElse(0f).intValue();
    }

    public boolean canMoveWhileCasting() {
        return value().activation().canMoveWhileCasting();
    }

    public boolean isUsable() {
        return isEnabled && level > 0;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public int getCooldown() {
        return cooldown;
    }

    public DragonAbility value() {
        return ability.value();
    }

    public Component getName() {
        return Component.translatable(Translation.Type.ABILITY.wrap(location()));
    }

    public ResourceKey<DragonAbility> key() {
        return ability.getKey();
    }

    public ResourceLocation location() {
        return key().location();
    }

    public String id() {
        return location().toString();
    }

    /** Returns the field in an unmodified way (also used by the codec) */
    public Holder<DragonAbility> ability() {
        return ability;
    }

    public void setLevel(int level) {
        this.level = Mth.clamp(level, MIN_LEVEL, getMaxLevel());
    }

    /** Returns the field in an unmodified way (also used by the codec) */
    public int level() {
        return level;
    }

    public void setEnabled(boolean isEnabled, boolean manuallyDisabled) {
        this.isEnabled = isEnabled;
        if(!isEnabled) {
            this.manuallyDisabled = manuallyDisabled;
        } else {
            this.manuallyDisabled = false;
        }

        if (!isEnabled) {
            setActive(false);
        }
    }

    /** If the ability is disabled automatically, we want to prevent the user from just clicking to re-enable it */
    public boolean isDisabledAutomatically() {
        return !isEnabled && !manuallyDisabled;
    }

    /** Returns the field in an unmodified way (also used by the codec) */
    public boolean isEnabled() {
        return isEnabled;
    }
}
