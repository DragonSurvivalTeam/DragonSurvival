package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.Weight;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandomList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

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

    public record BlockTo(BlockState state, int weight, Optional<ItemStack> tool) implements WeightedEntry {
        public static final Codec<BlockTo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockState.CODEC.fieldOf("state").forGetter(BlockTo::state),
                Codec.INT.fieldOf("weight").forGetter(BlockTo::weight),
                ItemStack.CODEC.optionalFieldOf("tool").forGetter(BlockTo::tool)
        ).apply(instance, BlockTo::new));

        @Override
        public @NotNull Weight getWeight() {
            return Weight.of(weight);
        }
    }

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, final Direction direction) {
        if (dragon.getRandom().nextDouble() > probability.calculate(ability.level())) {
            return;
        }

        ServerLevel level = dragon.serverLevel();
        for (BlockConversionData data : blockConversions) {
            if (data.fromPredicate().test(level, position)) {
                data.blocksTo().getRandom(dragon.getRandom()).ifPresent(conversion -> handleConversion(conversion, level, position, dragon));
            }
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }

    private static void handleConversion(BlockConversionEffect.BlockTo conversion, ServerLevel level, BlockPos position, ServerPlayer dragon) {
        if (conversion.tool.isPresent()) {
            BlockState blockState = level.getBlockState(position);
            BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(position) : null;
            Block.dropResources(blockState, level, position, blockEntity, dragon, conversion.tool.orElse(ItemStack.EMPTY));
        }
        level.setBlock(position, conversion.state(), Block.UPDATE_ALL);
    }
}
