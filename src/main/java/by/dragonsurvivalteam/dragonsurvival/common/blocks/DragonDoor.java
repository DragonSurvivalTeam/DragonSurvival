package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class DragonDoor extends Block implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);

    private final @Nullable TagKey<DragonSpecies> types;
    private final boolean allowHumans;
    private final boolean requiresPower;

    public enum Part implements StringRepresentable {
        BOTTOM, MIDDLE, TOP;

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }

    public DragonDoor(final Properties properties) {
        this(properties, null, true, false);
    }

    public DragonDoor(final Properties properties, final TagKey<DragonSpecies> types) {
        this(properties, types, false, false);
    }

    public DragonDoor(final Properties properties, boolean requiresPower) {
        this(properties, null, true, requiresPower);
    }

    public DragonDoor(final Properties properties, @Nullable final TagKey<DragonSpecies> types, boolean allowHumans, boolean requiresPower) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(OPEN, false).setValue(HINGE, DoorHingeSide.LEFT).setValue(POWERED, false).setValue(PART, Part.BOTTOM).setValue(WATERLOGGED, false));

        this.types = types;
        this.allowHumans = allowHumans;
        this.requiresPower = requiresPower;
    }

    @Override
    public boolean isPathfindable(@NotNull final BlockState state, final PathComputationType type) {
        return switch (type) {
            case LAND, AIR -> state.getValue(OPEN);
            default -> false;
        };
    }

    @Override
    public @NotNull BlockState updateShape(final BlockState state, @NotNull final Direction facing, @NotNull final BlockState facingState, @NotNull final LevelAccessor level, @NotNull final BlockPos position, @NotNull final BlockPos facingPosition) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(position, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (facing.getAxis() == Direction.Axis.Y && state.is(facingState.getBlock())) {
            // Update the other door parts to match the changed door part
            return state.setValue(FACING, facingState.getValue(FACING))
                    .setValue(OPEN, facingState.getValue(OPEN))
                    .setValue(HINGE, facingState.getValue(HINGE))
                    .setValue(POWERED, facingState.getValue(POWERED));
        }

        return canSurvive(state, level, position) ? super.updateShape(state, facing, facingState, level, position, facingPosition) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void neighborChanged(@NotNull final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, @NotNull final Block neighborBlock, @NotNull final BlockPos neighborPosition, boolean isMoving) {
        if (neighborBlock == this) {
            return;
        }

        boolean isValidType = types == null;

        if (!isValidType && neighborBlock instanceof DragonPressurePlates plate) {
            isValidType = types == plate.getTypes();
        }

        if (!isValidType) {
            return;
        }

        boolean hasPower = level.hasNeighborSignal(position) || level.hasNeighborSignal(position.relative(state.getValue(PART) == Part.BOTTOM ? Direction.UP : Direction.DOWN));

        if (hasPower != state.getValue(POWERED)) {
            if (hasPower != state.getValue(OPEN)) {
                playSound(null, level, position, state, hasPower);
            }

            level.setBlock(position, state.setValue(POWERED, hasPower).setValue(OPEN, hasPower), Block.UPDATE_CLIENTS);
        }
    }

    private void playSound(@Nullable final Entity entity, final Level level, final BlockPos blockPosition, final BlockState blockState, boolean isOpening) {
        level.playSound(entity, blockPosition, getSound(blockState, isOpening), SoundSource.BLOCKS, 1, level.getRandom().nextFloat() * 0.1f + 0.9f);
    }

    private SoundEvent getSound(final BlockState blockState, boolean isOpening) {
        if (blockState.is(DSBlockTags.WOODEN_DRAGON_DOORS)) {
            return isOpening ? SoundEvents.WOODEN_DOOR_OPEN : SoundEvents.WOODEN_DOOR_CLOSE;
        }

        return isOpening ? SoundEvents.IRON_DOOR_OPEN : SoundEvents.IRON_DOOR_CLOSE;
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, @NotNull final Player player, @NotNull final BlockHitResult hitResult) {
        DragonStateHandler data = DragonStateProvider.getData(player);
        boolean canOpen;

        if (!data.isDragon()) {
            canOpen = allowHumans;
        } else {
            canOpen = types == null || data.species().is(types);
        }

        if (!requiresPower && canOpen) {
            BlockState newState = state.cycle(OPEN).setValue(WATERLOGGED, level.getFluidState(position).getType() == Fluids.WATER);

            level.setBlock(position, newState, 10);
            playSound(player, level, position, newState, newState.getValue(OPEN));

            if (newState.getValue(PART) == Part.TOP) {
                level.setBlock(position.below(2), newState.setValue(PART, Part.BOTTOM).setValue(WATERLOGGED, level.getFluidState(position.below(2)).getType() == Fluids.WATER), /* Block.UPDATE_CLIENTS + Block.UPDATE_IMMEDIATE */ 10);
                level.setBlock(position.below(), newState.setValue(PART, Part.MIDDLE).setValue(WATERLOGGED, level.getFluidState(position.below()).getType() == Fluids.WATER), /* Block.UPDATE_CLIENTS + Block.UPDATE_IMMEDIATE */ 10);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public @NotNull FluidState getFluidState(final BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public @NotNull BlockState rotate(final BlockState state, final Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(@NotNull final BlockState state, @NotNull final Mirror mirror) {
        //noinspection deprecation -> ignore
        return mirror == Mirror.NONE ? state : state.rotate(mirror.getRotation(state.getValue(FACING))).cycle(HINGE);
    }

    @Override
    public long getSeed(final BlockState state, final BlockPos position) {
        //noinspection deprecation -> ignore
        return Mth.getSeed(position.getX(), position.below(state.getValue(PART) == Part.BOTTOM ? 0 : 1).getY(), position.getZ());
    }

    @Override
    public boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos position) {
        BlockPos below = position.below();
        BlockState stateBelow = level.getBlockState(below);

        if (state.getValue(PART) == Part.BOTTOM) {
            return stateBelow.isFaceSturdy(level, below, Direction.UP);
        } else {
            return stateBelow.getBlock() == this;
        }
    }

    @Override
    public @NotNull VoxelShape getShape(final BlockState state, @NotNull final BlockGetter level, @NotNull final BlockPos position, @NotNull final CollisionContext context) {
        Direction facing = state.getValue(FACING);
        boolean isClosed = !state.getValue(OPEN);
        boolean isRightHinge = state.getValue(HINGE) == DoorHingeSide.RIGHT;

        return switch (facing) {
            case SOUTH -> isClosed ? SOUTH_AABB : isRightHinge ? EAST_AABB : WEST_AABB;
            case WEST -> isClosed ? WEST_AABB : isRightHinge ? SOUTH_AABB : NORTH_AABB;
            case NORTH -> isClosed ? NORTH_AABB : isRightHinge ? WEST_AABB : EAST_AABB;
            default -> isClosed ? EAST_AABB : isRightHinge ? NORTH_AABB : SOUTH_AABB;
        };
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final BlockPlaceContext context) {
        BlockPos clickedPosition = context.getClickedPos();

        if (clickedPosition.getY() < context.getLevel().getMaxBuildHeight() && context.getLevel().getBlockState(clickedPosition.above()).canBeReplaced(context) && context.getLevel().getBlockState(clickedPosition.above(2)).canBeReplaced(context)) {
            Level level = context.getLevel();
            boolean hasPower = level.hasNeighborSignal(clickedPosition) || level.hasNeighborSignal(clickedPosition.above());
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(HINGE, getHinge(context)).setValue(POWERED, hasPower).setValue(OPEN, hasPower).setValue(PART, Part.BOTTOM).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER && context.getLevel().getBlockState(clickedPosition).getBlock() == DSBlocks.SEA_DRAGON_DOOR.get());
        } else {
            return null;
        }
    }

    private DoorHingeSide getHinge(final BlockPlaceContext context) {
        //TODO Logic handling aligning doors
        BlockGetter iblockreader = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Direction direction = context.getHorizontalDirection();
        BlockPos blockpos1 = blockpos.above();
        Direction direction1 = direction.getCounterClockWise();
        BlockPos blockpos2 = blockpos.relative(direction1);
        BlockState blockstate = iblockreader.getBlockState(blockpos2);
        BlockPos blockpos3 = blockpos1.relative(direction1);
        BlockState blockstate1 = iblockreader.getBlockState(blockpos3);
        Direction direction2 = direction.getClockWise();
        BlockPos blockpos4 = blockpos.relative(direction2);
        BlockState blockstate2 = iblockreader.getBlockState(blockpos4);
        BlockPos blockpos5 = blockpos1.relative(direction2);
        BlockState blockstate3 = iblockreader.getBlockState(blockpos5);
        int i = (blockstate.isCollisionShapeFullBlock(iblockreader, blockpos2) ? -1 : 0) + (blockstate1.isCollisionShapeFullBlock(iblockreader, blockpos3) ? -1 : 0) + (blockstate2.isCollisionShapeFullBlock(iblockreader, blockpos4) ? 1 : 0) + (blockstate3.isCollisionShapeFullBlock(iblockreader, blockpos5) ? 1 : 0);
        boolean flag = blockstate.is(this) && blockstate.getValue(PART) == Part.BOTTOM;
        boolean flag1 = blockstate2.is(this) && blockstate2.getValue(PART) == Part.BOTTOM;
        if ((!flag || flag1) && i <= 0) {
            if ((!flag1 || flag) && i >= 0) {
                int j = direction.getStepX();
                int k = direction.getStepZ();
                Vec3 vec3d = context.getClickLocation();
                double d0 = vec3d.x - (double) blockpos.getX();
                double d1 = vec3d.z - (double) blockpos.getZ();
                return (j >= 0 || !(d1 < 0.5D)) && (j <= 0 || !(d1 > 0.5D)) && (k >= 0 || !(d0 > 0.5D)) && (k <= 0 || !(d0 < 0.5D)) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
            } else {
                return DoorHingeSide.LEFT;
            }
        } else {
            return DoorHingeSide.RIGHT;
        }
    }

    @Override
    public void setPlacedBy(final Level level, final BlockPos position, final BlockState state, final LivingEntity placer, @NotNull final ItemStack stack) {
        level.setBlock(position.above(), state.setValue(PART, Part.MIDDLE).setValue(WATERLOGGED, level.getFluidState(position.above()).getType() == Fluids.WATER), Block.UPDATE_ALL);
        level.setBlock(position.above(2), state.setValue(PART, Part.TOP).setValue(WATERLOGGED, level.getFluidState(position.above(2)).getType() == Fluids.WATER), Block.UPDATE_ALL);
    }

    @Override
    public @NotNull BlockState playerWillDestroy(final Level level, @NotNull final BlockPos position, @NotNull final BlockState state, @NotNull final Player player) {
        if (!level.isClientSide()) {
            Part part = state.getValue(PART);

            if (part != Part.MIDDLE && !player.isCreative()) {
                BlockPos middlePos = part == Part.BOTTOM ? position.above() : position.below();
                BlockState middleState = level.getBlockState(middlePos);

                if (middleState.getBlock() == state.getBlock()) {
                    level.setBlock(middlePos, Blocks.AIR.defaultBlockState(), /* Block.UPDATE_NEIGHBORS + Block.UPDATE_CLIENTS + Block.UPDATE_SUPPRESS_DROPS */ 35);
                    level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, middlePos, Block.getId(middleState));
                }
            } else if (part != Part.BOTTOM && player.isCreative()) {
                BlockPos bottomPos = part == Part.MIDDLE ? position.below() : position.below(2);
                BlockState bottomState = level.getBlockState(bottomPos);

                if (bottomState.getBlock() == state.getBlock()) {
                    level.setBlock(bottomPos, Blocks.AIR.defaultBlockState(), /* Block.UPDATE_NEIGHBORS + Block.UPDATE_CLIENTS + Block.UPDATE_SUPPRESS_DROPS */ 35);
                    level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, bottomPos, Block.getId(bottomState));
                }
            }
        }

        return super.playerWillDestroy(level, position, state, player);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PART, FACING, OPEN, HINGE, POWERED, WATERLOGGED);
    }
}