package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonBeaconData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDataMaps;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonBeaconBlockEntity;
import by.dragonsurvivalteam.dragonsurvival.util.ExperienceUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
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

public class DragonBeacon extends Block implements SimpleWaterloggedBlock, EntityBlock {
    @Translation(comments = "Not enough experience to gain the beacon effects (%s / %s)")
    private static final String NOT_ENOUGH_EXPERIENCE = Translation.Type.GUI.wrap("message.not_enough_experience");

    public DragonBeacon(final Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.LIT, false).setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public @NotNull BlockState updateShape(final BlockState state, @NotNull final Direction direction, @NotNull final BlockState neighborState, @NotNull final LevelAccessor level, @NotNull final BlockPos position, @NotNull final BlockPos neighborPosition) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(position, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, direction, neighborState, level, position, neighborPosition);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, @NotNull final Player player, @NotNull final BlockHitResult hitResult) {
        if (!(level.getBlockEntity(position) instanceof DragonBeaconBlockEntity beacon)) {
            return InteractionResult.FAIL;
        }

        int playerExperience = ExperienceUtils.getTotalExperience(player);

        if ((player.hasInfiniteMaterials() || playerExperience >= beacon.getExperienceCost())) {
            // The client does not retain the block entity data - not worth to sync it
            if (!player.level().isClientSide() && beacon.applyEffects(player, true)) {
                if (!player.hasInfiniteMaterials()) {
                    player.giveExperiencePoints(-beacon.getExperienceCost());
                }

                level.playSound(null, position, DSSounds.APPLY_EFFECT.get(), SoundSource.PLAYERS, 1, 1);
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        } else {
            player.displayClientMessage(Component.translatable(NOT_ENOUGH_EXPERIENCE, playerExperience, beacon.getExperienceCost()), true);
        }

        return InteractionResult.FAIL;
    }

    @Override
    public @NotNull ItemInteractionResult useItemOn(@NotNull final ItemStack stack, @NotNull final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, @NotNull final Player player, @NotNull final InteractionHand hand, @NotNull final BlockHitResult hitResult) {
        if (state.getValue(BlockStateProperties.LIT)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!stack.is(DSItemTags.ACTIVATES_DRAGON_BEACON)) {
            return ItemInteractionResult.FAIL;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return ItemInteractionResult.FAIL;
        }

        if (player.level().isClientSide()) {
            // We don't sync the beacon data to the client since it doesn't get retained anyway
            // Therefor we just think of it as a successful interaction at this point
            return ItemInteractionResult.sidedSuccess(true);
        }

        DragonBeaconData beaconData = handler.species().getData(DSDataMaps.DRAGON_BEACON_DATA);

        if (beaconData == null) {
            return ItemInteractionResult.FAIL;
        }

        if (!(level.getBlockEntity(position) instanceof DragonBeaconBlockEntity beacon)) {
            return ItemInteractionResult.FAIL;
        }

        beacon.setData(beaconData);
        stack.consume(1, player);

        level.setBlockAndUpdate(position, state.cycle(BlockStateProperties.LIT));
        level.playSound(null, position, DSSounds.ACTIVATE_BEACON.get(), SoundSource.BLOCKS, 1, 1);

        return ItemInteractionResult.sidedSuccess(level.isClientSide());
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
    public @NotNull FluidState getFluidState(final BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public @Nullable MenuProvider getMenuProvider(@NotNull final BlockState state, final Level level, @NotNull final BlockPos position) {
        BlockEntity blockentity = level.getBlockEntity(position);
        return blockentity instanceof MenuProvider menuProvider ? menuProvider : null;
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    protected void createBlockStateDefinition(@NotNull final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.LIT, BlockStateProperties.WATERLOGGED);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos position, @NotNull final BlockState state) {
        return DSBlockEntities.DRAGON_BEACON.get().create(position, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(final Level level, @NotNull final BlockState state, @NotNull final BlockEntityType<T> type) {
        return level.isClientSide ? null : BaseEntityBlock.createTickerHelper(type, DSBlockEntities.DRAGON_BEACON.get(), DragonBeaconBlockEntity::serverTick);
    }
}