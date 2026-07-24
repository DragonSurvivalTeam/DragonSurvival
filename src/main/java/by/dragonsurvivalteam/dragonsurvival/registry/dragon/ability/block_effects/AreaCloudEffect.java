package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.PotionData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record AreaCloudEffect(PotionData potion, LevelBasedValue duration, LevelBasedValue probability, Optional<LevelBasedValue> delay, Optional<LevelBasedValue> radius, ParticleOptions particle) implements AbilityBlockEffect {
    public static final MapCodec<AreaCloudEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PotionData.CODEC.fieldOf("potion").forGetter(AreaCloudEffect::potion),
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(AreaCloudEffect::duration),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(AreaCloudEffect::probability),
            LevelBasedValue.CODEC.optionalFieldOf("delay").forGetter(AreaCloudEffect::delay),
            LevelBasedValue.CODEC.optionalFieldOf("radius").forGetter(AreaCloudEffect::radius),
            ParticleTypes.CODEC.fieldOf("particle").forGetter(AreaCloudEffect::particle)
    ).apply(instance, AreaCloudEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, @Nullable final Direction direction) {
        if (dragon.level().random.nextDouble() < probability.calculate(ability.level()) && dragon.level().getBlockState(position).isSolid()) {
            AreaEffectCloud cloud = new AreaEffectCloud(dragon.level(), position.above().getX(), position.above().getY(), position.above().getZ());
            cloud.setPotionContents(potion.toPotionContents(dragon.getRandom(), ability.level()));
            cloud.setDuration((int) duration.calculate(ability.level()));
            cloud.setParticle(particle);
            cloud.setOwner(dragon);
            cloud.setWaitTime((int) delay.orElse(LevelBasedValue.constant(0)).calculate(ability.level()));
            cloud.setRadius((int) radius.orElse(LevelBasedValue.constant(1)).calculate(ability.level()));

            dragon.level().addFreshEntity(cloud);
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
