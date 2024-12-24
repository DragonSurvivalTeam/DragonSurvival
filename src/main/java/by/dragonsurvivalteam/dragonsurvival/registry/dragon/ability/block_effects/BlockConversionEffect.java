package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record BlockConversionEffect(List<BlockConversionData> blockConversions, LevelBasedValue probability) implements AbilityBlockEffect {
    public static final MapCodec<BlockConversionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockConversionData.CODEC.listOf().fieldOf("conversion_data").forGetter(BlockConversionEffect::blockConversions),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(BlockConversionEffect::probability)
    ).apply(instance, BlockConversionEffect::new));

    public record BlockConversionData(BlockPredicate fromPredicate, WeightedRandomList<BlockTo> blocksTo) {
        public static final Codec<BlockConversionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPredicate.CODEC.fieldOf("from_predicate").forGetter(BlockConversionData::fromPredicate),
                SimpleWeightedRandomList.codec(BlockTo.CODEC).fieldOf("blocks_to").forGetter(BlockConversionData::blocksTo)
        ).apply(instance, BlockConversionData::new));
    }

    public record BlockTo(BlockState state, int weight) implements WeightedEntry {
        public static final Codec<BlockTo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.fieldOf("state").forGetter(BlockTo::state),
                Codec.INT.fieldOf("weight").forGetter(BlockTo::weight)
        ).apply(instance, BlockTo::new));

        @Override
        public @NotNull Weight getWeight() {
            return Weight.of(weight);
        }
    }

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, final Direction direction) {
        if (dragon.getRandom().nextDouble() < probability.calculate(ability.level())) {
            return;
        }

        // This allows the state to be cached and not retrieved for every check
        BlockInWorld block = new BlockInWorld(dragon.serverLevel(), position, false);

        for (BlockConversionData data : blockConversions) {
            if (data.fromPredicate().matches(block)) {
                data.blocksTo().getRandom(dragon.getRandom()).ifPresent(conversion -> dragon.serverLevel().setBlock(position, conversion.state(), Block.UPDATE_ALL));
            }
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
