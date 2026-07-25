package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedValue;

public record IgniteEffect(LevelBasedValue igniteTicks) implements AbilityEntityEffect {
    public static final MapCodec<IgniteEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("ignite_ticks").forGetter(IgniteEffect::igniteTicks)
    ).apply(instance, IgniteEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        int ticks = (int) igniteTicks().calculate(ability.level());
        target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), ticks));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
