package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record ParticleData(SpawnParticles particleData, LevelBasedValue particleCount) {
    public static final Codec<ParticleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SpawnParticles.CODEC.fieldOf("particle_data").forGetter(ParticleData::particleData),
            LevelBasedValue.CODEC.fieldOf("particle_count").forGetter(ParticleData::particleCount)
    ).apply(instance, ParticleData::new));

    public void spawn(final ServerLevel serverLevel, final Entity entity, final int level) {
        particleData.apply(serverLevel, entity, (int) particleCount.calculate(level));
    }

    public void spawn(final ServerLevel serverLevel, final BlockPos position, final int level) {
        particleData.apply(serverLevel, position, (int) particleCount.calculate(level));
    }
}
