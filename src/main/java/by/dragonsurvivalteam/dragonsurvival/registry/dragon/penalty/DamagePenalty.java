package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

public record DamagePenalty(Holder<DamageType> damageType, float damage) implements PenaltyEffect {
    public static final MapCodec<DamagePenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageType.CODEC.fieldOf("type").forGetter(DamagePenalty::damageType),
            Codec.FLOAT.fieldOf("amount").forGetter(DamagePenalty::damage)
    ).apply(instance, DamagePenalty::new));

    @Override
    public void apply(final ServerPlayer player) {
        player.hurt(new DamageSource(damageType, player), damage);
    }

    @Override
    public MutableComponent getDescription() {
        //noinspection DataFlowIssue -> key is present
        MutableComponent translation = Component.translatable(Translation.Type.DAMAGE_TYPE.wrap(damageType.getKey().location())).withColor(DSColors.ORANGE);
        return Component.translatable(LangKey.ABILITY_DAMAGE, translation, DSColors.blue(String.format("%.1f", damage)));
    }

    @Override
    public MapCodec<? extends PenaltyEffect> codec() {
        return CODEC;
    }
}
