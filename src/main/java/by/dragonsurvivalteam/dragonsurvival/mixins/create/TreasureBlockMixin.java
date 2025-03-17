package by.dragonsurvivalteam.dragonsurvival.mixins.create;

import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import com.simibubi.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.simibubi.create.content.schematics.requirement.ItemRequirement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TreasureBlock.class)
public abstract class TreasureBlockMixin implements SpecialBlockItemRequirement {
    /** See {@link ItemRequirement#defaultOf(BlockState, BlockEntity)} for snow layer block */
    @Override
    public ItemRequirement getRequiredItems(final BlockState state, @Nullable final BlockEntity blockEntity) {
        return new ItemRequirement(ItemRequirement.ItemUseType.CONSUME, new ItemStack(state.getBlock().asItem(), state.getValue(TreasureBlock.LAYERS)));
    }
}
