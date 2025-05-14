package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
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

public record BlockBreakEffect(BlockPredicate validBlocks, LevelBasedValue probability, boolean dropLoot, Optional<ItemStack> tool) implements AbilityBlockEffect {
    public static final MapCodec<BlockBreakEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPredicate.CODEC.fieldOf("valid_blocks").forGetter(BlockBreakEffect::validBlocks),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(BlockBreakEffect::probability),
            Codec.BOOL.optionalFieldOf("drop_loot", false).forGetter(BlockBreakEffect::dropLoot),
            ItemStack.CODEC.optionalFieldOf("tool").forGetter(BlockBreakEffect::tool)
    ).apply(instance, BlockBreakEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, @Nullable final Direction direction) {
        if (probability.calculate(ability.level()) < dragon.getRandom().nextDouble()) {
            return;
        }

        ServerLevel level = dragon.serverLevel();
        if (validBlocks.test(level, position)) {
            if (tool.isPresent()) {
                BlockState blockState = level.getBlockState(position);
                BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(position) : null;
                Block.dropResources(blockState, level, position, blockEntity, dragon, tool.orElse(ItemStack.EMPTY));
            }
            level.destroyBlock(position, dropLoot);
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
