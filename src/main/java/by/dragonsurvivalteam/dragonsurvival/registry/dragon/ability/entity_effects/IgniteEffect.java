package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record IgniteEffect(LevelBasedValue igniteTicks) implements AbilityEntityEffect {
    public static final MapCodec<IgniteEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("ignite_ticks").forGetter(IgniteEffect::igniteTicks)
    ).apply(instance, IgniteEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        target.igniteForTicks((int) igniteTicks().calculate(ability.level()));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
