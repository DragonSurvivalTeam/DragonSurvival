package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonSoulBlockEntity;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.DragonAnimations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO :: menu for armor stand behaviour? or just right click items onto it?
//  you can target retrieve normal armor stand items by viewing the areas - don't think (dynamic) dragon models will allow that?

// TODO :: in the future attempt to make the shape follow the stored dragon model (not the hitbox)
public class DragonSoulBlock extends Block implements SimpleWaterloggedBlock, EntityBlock {
    @Translation(comments = "The animation %s is not known.")
    public static final String INVALID_ANIMATION = Translation.Type.GUI.wrap("message.soul.invalid_animation");

    public DragonSoulBlock(final Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    // TODO :: add waterlog

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull final ItemStack stack, @NotNull final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, @NotNull final Player player, @NotNull final InteractionHand hand, @NotNull final BlockHitResult hitResult) {
        if (stack.getItem() instanceof NameTagItem && level.getBlockEntity(position) instanceof DragonSoulBlockEntity soul) {
            Component name = stack.get(DataComponents.CUSTOM_NAME);
            String animation = name != null ? name.getString() : "";

            if (DragonAnimations.doesExist(animation)) {
                soul.animation = animation;
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            } else {
                player.displayClientMessage(Component.translatable(INVALID_ANIMATION, DSColors.withColor(animation, DSColors.GOLD)), true);
                return ItemInteractionResult.FAIL;
            }
        }

        return super.useItemOn(stack, state, level, position, player, hand, hitResult);
    }

    @Override
    public @NotNull BlockState updateShape(final BlockState state, @NotNull final Direction direction, @NotNull final BlockState neighborState, @NotNull final LevelAccessor level, @NotNull final BlockPos position, @NotNull final BlockPos neighborPosition) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(position, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, position, neighborPosition);
    }

    @Override
    public boolean triggerEvent(@NotNull final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, int id, int param) {
        super.triggerEvent(state, level, position, id, param);
        BlockEntity blockentity = level.getBlockEntity(position);
        return blockentity != null && blockentity.triggerEvent(id, param);
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull final BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(@NotNull final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.HORIZONTAL_FACING, BlockStateProperties.WATERLOGGED);
    }

    @Override
    protected @NotNull BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING, rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    protected @NotNull BlockState mirror(final BlockState state, final Mirror mirror) {
        //noinspection deprecation -> ignore
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return this.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, context.getHorizontalDirection())
                .setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public @NotNull FluidState getFluidState(final BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos position, @NotNull final BlockState state) {
        return DSBlockEntities.DRAGON_SOUL.get().create(position, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(final Level level, @NotNull final BlockState state, @NotNull final BlockEntityType<T> type) {
        return level.isClientSide ? null : BaseEntityBlock.createTickerHelper(type, DSBlockEntities.DRAGON_SOUL.get(), DragonSoulBlockEntity::serverTick);
    }
}