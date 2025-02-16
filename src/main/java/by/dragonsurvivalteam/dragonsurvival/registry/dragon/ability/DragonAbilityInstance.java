package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.MagicHUD;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ManaCost;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ManaHandler;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncDisableAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Activation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
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
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DragonAbilityInstance {
    public static final int MIN_LEVEL = 0;
    /** Since {@link LevelBasedValue} does not expect a level of 0 we handle 0 as "ability is disabled" */
    public static final int MIN_LEVEL_FOR_CALCULATIONS = 1;
    public static final int MAX_LEVEL = 255;
    public static final int NO_COOLDOWN = 0;

    public static final Codec<DragonAbilityInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DragonAbility.CODEC.fieldOf("ability").forGetter(DragonAbilityInstance::ability),
            Codec.INT.fieldOf("level").forGetter(DragonAbilityInstance::level),
            Codec.BOOL.optionalFieldOf("is_manually_disabled", false).forGetter(ability -> ability.isManuallyDisabled)
    ).apply(instance, DragonAbilityInstance::new));

    private final Holder<DragonAbility> ability;
    private int level;
    /** Indicates that the player CTRL-clicked the ability in the ability screen */
    private boolean isManuallyDisabled;
    /** Indicates that the ability is blocked due to the 'usage_blocked' condition */
    private boolean isAutomaticallyDisabled;
    /** Indicates that the ability is able to apply its effects */
    private boolean isActive;

    private int currentTick;
    private int cooldown;

    public DragonAbilityInstance(final Holder<DragonAbility> ability, int level) {
        this(ability, level, false);
    }

    public DragonAbilityInstance(final Holder<DragonAbility> ability, int level, boolean isManuallyDisabled) {
        this.ability = ability;
        this.level = level;
        this.isManuallyDisabled = isManuallyDisabled;

        if (isEnabled() && isPassive()) {
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

        if (dragon instanceof ServerPlayer serverPlayer) {
            boolean isAutomaticallyDisabled = value().usageBlocked().map(condition -> condition.test(Condition.abilityContext(serverPlayer))).orElse(false);

            if (isAutomaticallyDisabled && !this.isAutomaticallyDisabled) {
                setDisabled(serverPlayer, true, false);
                PacketDistributor.sendToPlayer(serverPlayer, new SyncDisableAbility(ability.getKey(), true, false));
            } else if (!isAutomaticallyDisabled && this.isAutomaticallyDisabled) {
                setDisabled(serverPlayer, false, false);
                PacketDistributor.sendToPlayer(serverPlayer, new SyncDisableAbility(ability.getKey(), false, false));
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
        int castTime = value().activation().getCastTime(level);
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

        if (currentTick < castTime) {
            return;
        }

        if (currentTick == castTime) {
            ManaHandler.consumeMana(dragon, value().activation().getInitialManaCost(level));
        }

        if (currentTick > castTime) {
            float manaCost = getContinuousManaCost(ManaCost.ManaCostType.TICKING);

            if (ManaHandler.hasEnoughMana(dragon, manaCost)) {
                // TODO :: make this return a boolean and remove 'hasEnoughMana'?
                ManaHandler.consumeMana(dragon, manaCost);
            } else {
                stopCasting(dragon);
                return;
            }
        }

        if (dragon instanceof ServerPlayer serverPlayer) {
            ability.value().actions().forEach(action -> action.tick(serverPlayer, this, currentTick));
        }

        if (value().activation().type() == Activation.Type.SIMPLE) {
            stopCasting(dragon);
        }
    }

    private void stopCasting(final Player dragon) {
        value().activation().playEndSound(dragon);
        value().activation().playEndAnimation(dragon);

        MagicData magic = MagicData.getData(dragon);
        magic.stopCasting(dragon);
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

    public boolean hasEnoughMana(final Player dragon) {
        float manaCost = value().activation().getInitialManaCost(level);

        if (!ManaHandler.hasEnoughMana(dragon, manaCost)) {
            currentTick = 0;

            if (dragon.level().isClientSide()) {
                MagicData magic = MagicData.getData(dragon);
                magic.setErrorMessageSent(true);

                if (value().activation().notification().notEnoughMana().getContents() != PlainTextContents.EMPTY) {
                    MagicHUD.castingError(value().activation().notification().notEnoughMana());
                }
            }

            return false;
        }

        return true;
    }

    public boolean isApplyingEffects() {
        return isActive && canBeCast() && currentTick >= value().activation().getCastTime(level);
    }

    public boolean hasEndAnimation() {
        return value().activation().animations().isPresent() && value().activation().animations().get().end().isPresent();
    }

    public boolean canBeCast() {
        return isUsable() && cooldown == NO_COOLDOWN;
    }

    public ResourceLocation getIcon() {
        return ability.value().icon().get(level);
    }

    public void setActive(final Player player, boolean isActive) {
        this.isActive = isActive;

        if (player instanceof ServerPlayer serverPlayer && !isActive && isPassive()) {
            // Also makes sure to remove any affects that are applied by the ability
            ability.value().actions().forEach(action -> action.remove(serverPlayer, this));
        }

        if (!isActive) {
            currentTick = 0;
        }
    }

    public void release(final Player dragon) {
        currentTick = 0;

        if (dragon.hasInfiniteMaterials()) {
            cooldown = NO_COOLDOWN;
        } else {
            cooldown = ability.value().activation().getCooldown(level);
        }
    }

    public boolean isPassive() {
        return value().activation().type() == Activation.Type.PASSIVE;
    }

    public int getMaxLevel() {
        return value().getMaxLevel();
    }

    public int getCurrentCastTime() {
        return currentTick;
    }

    public boolean isUsable() {
        return isEnabled() && level > 0;
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

    /**
     * Depending on the 'isManual' value it will either modify: <br>
     * - {@link DragonAbilityInstance#isManuallyDisabled} <br>
     * - {@link DragonAbilityInstance#isAutomaticallyDisabled} <br>
     * <br>
     * Afterward its active status will be updated
     */
    public void setDisabled(final Player player, boolean isDisabled, boolean isManual) {
        boolean wasEnabled = isEnabled();

        if (isManual) {
            this.isManuallyDisabled = isDisabled;
        } else {
            this.isAutomaticallyDisabled = isDisabled;
        }

        if (wasEnabled == isEnabled()) {
            return;
        }

        if (!isEnabled()) {
            setActive(player, false);
        } if (isPassive()) {
            // Passive abilities need to be re-activated automatically
            setActive(player, true);
        }
    }

    public boolean isDisabled(boolean isManual) {
        if (isManual) {
            return isManuallyDisabled;
        } else {
            return isAutomaticallyDisabled;
        }
    }

    public boolean isEnabled() {
        return !isManuallyDisabled && !isAutomaticallyDisabled;
    }
}
