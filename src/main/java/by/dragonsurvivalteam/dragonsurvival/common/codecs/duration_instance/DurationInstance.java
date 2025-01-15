package by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.StorageEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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
