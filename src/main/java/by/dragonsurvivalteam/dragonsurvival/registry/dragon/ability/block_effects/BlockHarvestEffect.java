package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record BlockHarvestEffect(BlockPredicate validBlocks, LevelBasedValue probability, Optional<ItemStack> tool) implements AbilityBlockEffect {
    public static final MapCodec<BlockHarvestEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPredicate.CODEC.fieldOf("valid_blocks").forGetter(BlockHarvestEffect::validBlocks),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(BlockHarvestEffect::probability),
            ItemStack.CODEC.optionalFieldOf("tool").forGetter(BlockHarvestEffect::tool)
    ).apply(instance, BlockHarvestEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, @Nullable final Direction direction) {
        if (probability.calculate(ability.level()) < dragon.getRandom().nextDouble()) {
            return;
        }

        ServerLevel level = dragon.serverLevel();
        if (validBlocks.test(level, position)) {
            ItemStack blockTool = tool.orElse(ItemStack.EMPTY);
            BlockState blockState = level.getBlockState(position);
            BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(position) : null;
            Block.dropResources(blockState, level, position, blockEntity, dragon, blockTool);
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
