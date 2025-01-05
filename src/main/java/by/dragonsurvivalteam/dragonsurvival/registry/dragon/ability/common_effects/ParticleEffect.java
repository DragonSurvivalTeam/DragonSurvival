package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.SpawnParticles;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AbilityBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.AbilityEntityEffect;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ParticleEffect(SpawnParticles particleData, LevelBasedValue particleCount) implements AbilityBlockEffect, AbilityEntityEffect {
    public static final MapCodec<ParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            SpawnParticles.CODEC.fieldOf("particle_data").forGetter(ParticleEffect::particleData),
            LevelBasedValue.CODEC.fieldOf("particle_count").forGetter(ParticleEffect::particleCount)
    ).apply(instance, ParticleEffect::new));

    @Override
    public void apply(ServerPlayer dragon, DragonAbilityInstance ability, BlockPos position, @Nullable Direction direction) {
        particleData.apply((ServerLevel) dragon.level(), position, (int)particleCount.calculate(ability.level()));
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }

    @Override
    public void apply(ServerPlayer dragon, DragonAbilityInstance ability, Entity entity) {
        particleData.apply((ServerLevel) dragon.level(), entity, (int)particleCount.calculate(ability.level()));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        return List.of();
    }
}
