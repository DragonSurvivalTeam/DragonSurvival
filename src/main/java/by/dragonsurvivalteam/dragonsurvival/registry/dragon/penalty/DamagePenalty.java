package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;

public record DamagePenalty(Holder<DamageType> damageType, float damage) implements PenaltyEffect {
    public static final MapCodec<DamagePenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageType.CODEC.fieldOf("type").forGetter(DamagePenalty::damageType),
            Codec.FLOAT.fieldOf("amount").forGetter(DamagePenalty::damage)
    ).apply(instance, DamagePenalty::new));

    @Override
    public void apply(final Player player) {
        player.hurt(new DamageSource(damageType, player), damage);
    }

    @Override
    public MutableComponent getDescription() {
        //noinspection DataFlowIssue -> key is present
        return Component.translatable(LangKey.ABILITY_DAMAGE, Component.translatable(damageType.getKey().location().toLanguageKey()), DSColors.blue(String.format("%.1f", damage)));
    }

    @Override
    public MapCodec<? extends PenaltyEffect> codec() {
        return CODEC;
    }
}
