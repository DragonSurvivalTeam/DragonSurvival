package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static java.lang.Math.round;

public record BlockHarvestEffect(BlockPredicate validBlocks, LevelBasedValue probability, Optional<ItemStack> tool, Optional<Holder<Enchantment>> enchantment, LevelBasedValue enchant_level) implements AbilityBlockEffect {
    public static final MapCodec<BlockHarvestEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPredicate.CODEC.fieldOf("valid_blocks").forGetter(BlockHarvestEffect::validBlocks),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(BlockHarvestEffect::probability),
            ItemStack.CODEC.optionalFieldOf("tool").forGetter(BlockHarvestEffect::tool),
            Enchantment.CODEC.optionalFieldOf("enchantment").forGetter(BlockHarvestEffect::enchantment),
            LevelBasedValue.CODEC.fieldOf("enchant_level").forGetter(BlockHarvestEffect::enchant_level)
    ).apply(instance, BlockHarvestEffect::new));
    /** TODO: Move Enchantment + level above into a new codec?  Also make enchant_level optional */

    private static final ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE, 1);

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, @Nullable final Direction direction) {
        if (probability.calculate(ability.level()) < dragon.getRandom().nextDouble()) {
            return;
        }

        ServerLevel level = dragon.serverLevel();
        if (validBlocks.test(level, position)) {
            ItemStack blockTool = tool.orElse(pickaxe);
            initializeTool(blockTool, enchantment.orElse(null), round(enchant_level().calculate(ability.level())));
            BlockState blockState = level.getBlockState(position);
            BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(position) : null;
            Block.dropResources(blockState, level, position, blockEntity, dragon, blockTool);
        }
    }

    private static void initializeTool(ItemStack tool, @Nullable Holder<Enchantment> enchantment, Integer level) {
        if (enchantment != null && tool.getEnchantmentLevel(enchantment) != level) {
            tool.enchant(enchantment, level);
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
