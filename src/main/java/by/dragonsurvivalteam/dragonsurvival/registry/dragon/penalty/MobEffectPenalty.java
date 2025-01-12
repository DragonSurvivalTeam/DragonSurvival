package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public record MobEffectPenalty(HolderSet<MobEffect> effects, int amplifier, int duration) implements PenaltyEffect {
    public static final MapCodec<MobEffectPenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(BuiltInRegistries.MOB_EFFECT.key()).fieldOf("effects").forGetter(MobEffectPenalty::effects),
            Codec.INT.fieldOf("amplifier").forGetter(MobEffectPenalty::amplifier),
            Codec.INT.fieldOf("duration").forGetter(MobEffectPenalty::duration)
    ).apply(instance, MobEffectPenalty::new));

    @Override
    public void apply(final ServerPlayer player, final Holder<DragonPenalty> penalty) {
        effects.forEach(effect -> player.addEffect(new MobEffectInstance(effect, duration, amplifier)));
    }

    @Override
    public MutableComponent getDescription() {
        MutableComponent name = Component.literal("§6■ ").append(Component.translatable(LangKey.ABILITY_APPLIES)).withColor(DSColors.GOLD);

        for (int i = 0; i < effects.size(); i++) {
            name.append(Component.translatable(effects.get(i).value().getDescriptionId()));

            if (i < effects.size() - 1) {
                name.append(Component.literal(", "));
            }
        }

        if (amplifier > 0) {
            name.append(Component.literal(" " + amplifier).withColor(DSColors.GOLD));
        }

        name.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, DSColors.dynamicValue(Functions.ticksToSeconds(duration))));

        return name;
    }

    @Override
    public MapCodec<? extends PenaltyEffect> codec() {
        return CODEC;
    }
}
