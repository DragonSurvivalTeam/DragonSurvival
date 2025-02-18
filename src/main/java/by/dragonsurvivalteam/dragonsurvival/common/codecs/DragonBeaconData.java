package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

import java.util.ArrayList;
import java.util.List;

public record DragonBeaconData(List<Effect> effects, PaymentData paymentData) {
    public static final Codec<DragonBeaconData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Effect.CODEC.listOf().fieldOf("effects").forGetter(DragonBeaconData::effects),
            PaymentData.CODEC.fieldOf("payment_data").forGetter(DragonBeaconData::paymentData)
    ).apply(instance, DragonBeaconData::new));

    public record PaymentData(int experienceCost, int durationMultiplier, int amplifierModification) {
        public static final Codec<PaymentData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.optionalFieldOf("experience_cost", 0).forGetter(PaymentData::experienceCost),
                Codec.INT.optionalFieldOf("duration_multiplier", 1).forGetter(PaymentData::durationMultiplier),
                Codec.INT.optionalFieldOf("amplifier_modification", 0).forGetter(PaymentData::amplifierModification)
        ).apply(instance, PaymentData::new));
    }

    public record Effect(Holder<MobEffect> effect, int duration, int amplifier) {
        public static final Codec<Effect> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(Effect::effect),
                Codec.INT.fieldOf("duration").forGetter(Effect::duration),
                Codec.INT.fieldOf("amplifier").forGetter(Effect::amplifier)
        ).apply(instance, Effect::new));
    }

    @SafeVarargs
    public static List<Effect> createEffects(final int duration, final int amplifier, final Holder<MobEffect>... effects) {
        List<Effect> instances = new ArrayList<>();

        for (Holder<MobEffect> effect : effects) {
            instances.add(new Effect(effect, duration, amplifier));
        }

        return instances;
    }
}
