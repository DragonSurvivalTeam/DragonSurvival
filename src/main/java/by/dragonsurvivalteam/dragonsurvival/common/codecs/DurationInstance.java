package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.StorageEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.Supplier;

public abstract class DurationInstance<B> implements ClientEffectProvider, StorageEntry {
    public static final int INFINITE_DURATION = -1;

    private final B baseData;
    private final ClientEffectProvider.ClientData clientData;
    private final int appliedAbilityLevel;

    private int currentDuration;

    public DurationInstance(final B baseData, final ClientEffectProvider.ClientData clientData, int appliedAbilityLevel, int currentDuration) {
        this.baseData = baseData;
        this.clientData = clientData;
        this.appliedAbilityLevel = appliedAbilityLevel;
        this.currentDuration = currentDuration;
    }

    public static <T extends DurationInstance<B>, B> Products.P4<RecordCodecBuilder.Mu<T>, B, ClientData, Integer, Integer> codecStart(final RecordCodecBuilder.Instance<T> instance, final Supplier<Codec<B>> baseDataCodec) {
        return instance.group(
                baseDataCodec.get().fieldOf("base_data").forGetter(T::baseData),
                ClientData.CODEC.fieldOf("client_data").forGetter(T::clientData),
                Codec.INT.fieldOf("applied_ability_level").forGetter(T::appliedAbilityLevel),
                Codec.INT.fieldOf("current_duration").forGetter(T::currentDuration)
        );
    }

    /** @return Whether the duration has reached its end or not */
    public boolean tick() {
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
    public ClientEffectProvider.ClientData clientData() {
        return clientData;
    }

    @Override
    public int currentDuration() {
        return currentDuration;
    }

    public int appliedAbilityLevel() {
        return appliedAbilityLevel;
    }

    @Override
    abstract public int getDuration();
}
