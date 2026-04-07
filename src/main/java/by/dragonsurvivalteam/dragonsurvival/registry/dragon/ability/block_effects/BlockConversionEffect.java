package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ParticleData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

import java.util.List;
import java.util.Optional;

public record BlockConversionEffect(List<BlockConversionData> blockConversions, LevelBasedValue probability) implements AbilityBlockEffect {
    public static final MapCodec<BlockConversionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockConversionData.CODEC.listOf().fieldOf("conversion_data").forGetter(BlockConversionEffect::blockConversions),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(BlockConversionEffect::probability)
    ).apply(instance, BlockConversionEffect::new));

    public record BlockConversionData(BlockPredicate fromPredicate, WeightedList<BlockTo> blocksTo) {
        public static final Codec<BlockConversionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPredicate.CODEC.fieldOf("from_predicate").forGetter(BlockConversionData::fromPredicate),
                WeightedList.codec(BlockTo.CODEC).fieldOf("blocks_to").forGetter(BlockConversionData::blocksTo)
        ).apply(instance, BlockConversionData::new));
    }

    public record BlockTo(BlockState state, Optional<ParticleData> particles) {
        public static final MapCodec<BlockTo> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                BlockState.CODEC.fieldOf("state").forGetter(BlockTo::state),
                ParticleData.CODEC.optionalFieldOf("particles").forGetter(BlockTo::particles)
        ).apply(instance, BlockTo::new));
    }

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, final Direction direction) {
        if (dragon.getRandom().nextDouble() > probability.calculate(ability.level())) {
            return;
        }

        for (BlockConversionData data : blockConversions) {
            if (data.fromPredicate().test(dragon.level(), position)) {
                data.blocksTo().getRandom(dragon.getRandom()).ifPresent(conversion -> {
                    dragon.level().setBlock(position, conversion.state(), Block.UPDATE_ALL);
                    conversion.particles().ifPresent(particles -> particles.spawn(dragon.level(), position, ability.level()));
                });
            }
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
