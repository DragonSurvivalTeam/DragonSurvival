package by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.StorageEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
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

    @Override
    public boolean tick(final Entity storageHolder) {
        // TODO :: check this differently for active abilities?
        //  since for them it would just always remove the the instance after the cast has been completed
        if (commonData.removeAutomatically() && commonData.source().isPresent()) {
            Player source = storageHolder.level().getPlayerByUUID(commonData.source().get());

            if (source == null) {
                return true;
            }

            DragonStateHandler handler = DragonStateProvider.getData(source);

            if (!handler.isDragon()) {
                return true;
            }

            if (commonData.ability().isPresent()) {
                // TODO :: let the server deal with removal of entries through this tick method?
                //  (avoids the need to sync magic data to the client - removal gets synchronized anyway)
                MagicData magic = MagicData.getData(source);
                DragonAbilityInstance ability = magic.getAbility(commonData.ability().get());

                if (ability == null || !ability.isApplyingEffects()) {
                    return true;
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

        if (storageHolder.level() instanceof ServerLevel serverLevel && earlyRemovalCondition().map(condition -> condition.test(Condition.entityContext(serverLevel, storageHolder))).orElse(false)) {
            return true;
        }

        if (currentDuration == INFINITE_DURATION) {
            return false;
        }

        currentDuration--;
        return currentDuration == 0;
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
