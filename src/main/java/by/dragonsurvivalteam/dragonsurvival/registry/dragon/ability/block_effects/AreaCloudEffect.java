package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.PotionData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
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

public record AreaCloudEffect(PotionData potion, LevelBasedValue duration, double probability, ParticleOptions particle) implements AbilityBlockEffect {
    public static final MapCodec<AreaCloudEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PotionData.CODEC.fieldOf("potion").forGetter(AreaCloudEffect::potion),
            LevelBasedValue.CODEC.fieldOf("duration").forGetter(AreaCloudEffect::duration),
            // TODO :: why not level based?
            Codec.DOUBLE.fieldOf("probability").forGetter(AreaCloudEffect::probability),
            ParticleTypes.CODEC.fieldOf("particle").forGetter(AreaCloudEffect::particle)
    ).apply(instance, AreaCloudEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, @Nullable final Direction direction) {
        if (dragon.level().random.nextDouble() < probability && dragon.level().getBlockState(position).isSolid()) {
            AreaEffectCloud cloud = new AreaEffectCloud(dragon.level(), position.above().getX(), position.above().getY(), position.above().getZ());
            cloud.setPotionContents(potion.toPotionContents(dragon, ability.level()));
            cloud.setDuration((int) duration.calculate(ability.level()));
            cloud.setParticle(particle);
            cloud.setOwner(dragon);
            cloud.setWaitTime(0);
            cloud.setRadius(1);

            dragon.level().addFreshEntity(cloud);
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
