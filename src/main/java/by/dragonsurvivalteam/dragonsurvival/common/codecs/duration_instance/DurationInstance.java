package by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.StorageEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class DurationInstance<B extends DurationInstanceBase<?, ?>> implements ClientEffectProvider, StorageEntry {
    public static final int INFINITE_DURATION = -1;

    private final B baseData;
    private final CommonData commonData;

    private int currentDuration;

    public DurationInstance(final B baseData, final CommonData commonData, int currentDuration) {
        this.baseData = baseData;
        this.commonData = commonData;
        this.currentDuration = currentDuration;
    }

    public static <T extends DurationInstance<B>, B extends DurationInstanceBase<?, ?>> Products.P3<RecordCodecBuilder.Mu<T>, B, CommonData, Integer> codecStart(final RecordCodecBuilder.Instance<T> instance, final Supplier<Codec<B>> baseDataCodec) {
        return instance.group(
                baseDataCodec.get().fieldOf("base_data").forGetter(T::baseData),
                CommonData.CODEC.fieldOf("common_data").forGetter(T::commonData),
                Codec.INT.fieldOf("current_duration").forGetter(T::currentDuration)
        );
    }

    /**
     * This method will return 'true' when run on the server-side, and the instance is supposed to be removed </br>
     * (e.g. when the duration reaches 0 or the removal condition matches) </br> </br>
     * <p>
     * The client will always return 'false' since the server is handling the removal and informs the client through packets </br>
     * (Partly because data we need to check (abilities, i.e. magic data, of other players) is not synchronized between players
     */
    @Override
    public boolean tick(final Entity storageHolder) {
        if (currentDuration != INFINITE_DURATION) {
            currentDuration--;

            if (storageHolder.level().isClientSide()) {
                return false;
            } else if (currentDuration == 0) {
                return true;
            }
        }

        if (storageHolder.level().isClientSide()) {
            return false;
        }

        if (commonData.removeAutomatically() && commonData.source().isPresent()) {
            // TODO :: check across dimensions? usually would just cause the range to return true
            //  if the entities are in different dimensions
            // ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer()
            Player source = storageHolder.level().getPlayerByUUID(commonData.source().get());

            if (source == null) {
                return true;
            }

            DragonStateHandler handler = DragonStateProvider.getData(source);

            if (!handler.isDragon()) {
                return true;
            }

            if (commonData.ability().isPresent()) {
                MagicData magic = MagicData.getData(source);
                DragonAbilityInstance instance = magic.getAbility(commonData.ability().get());

                if (instance == null || !isAbilityActive(instance)) {
                    return true;
                }

                for (ActionContainer container : instance.value().actions()) {
                    Collection<ResourceLocation> ids = container.effect().target().map(
                            block -> List.of(),
                            AbilityTargeting.EntityTargeting::getEffectIDs
                    );

                    // Only doing a simple distance check since actually checking the proper AABB area (if present) might be too much here
                    if (ids.contains(baseData.id()) && storageHolder.distanceTo(source) > container.effect().getDistance(source, instance)) {
                        return true;
                    }
                }
            }

            if (commonData.penalty().isPresent() && source instanceof ServerPlayer serverPlayer) {
                for (Holder<DragonPenalty> penalty : handler.species().value().penalties()) {
                    if (penalty.getKey() == commonData.penalty().get() && penalty.value().condition().map(condition -> !condition.test(Condition.penaltyContext(serverPlayer))).orElse(false)) {
                        return true;
                    }
                }
            }
        }

        return storageHolder.level() instanceof ServerLevel serverLevel && earlyRemovalCondition().map(condition -> condition.test(Condition.entityContext(serverLevel, storageHolder))).orElse(false);
    }

    private boolean isAbilityActive(final DragonAbilityInstance instance) {
        if (instance.isPassive()) {
            return instance.isApplyingEffects();
        }

        // Otherwise active ability effects would always be instantly removed
        // And channeling ones would only stay while they are being channeled
        return instance.isEnabled();
    }

    public B baseData() {
        return baseData;
    }

    @Override
    public ResourceLocation id() {
        return baseData.id();
    }

    @Override
    public int getDuration() {
        return (int) baseData.duration().calculate(appliedAbilityLevel());
    }

    @Override
    public boolean isHidden() {
        return baseData.isHidden();
    }

    public CommonData commonData() {
        return commonData;
    }

    @Override
    public ClientData clientData() {
        return commonData.clientData();
    }

    public Optional<ResourceKey<DragonAbility>> ability() {
        return commonData.ability();
    }

    public Optional<UUID> source() {
        return commonData.source();
    }

    public int appliedAbilityLevel() {
        return commonData.appliedAbilityLevel();
    }

    @Override
    public int currentDuration() {
        return currentDuration;
    }

    public Optional<LootItemCondition> earlyRemovalCondition() {
        return baseData().earlyRemovalCondition();
    }
}
