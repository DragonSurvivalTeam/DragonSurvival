package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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

public class SmallDragonDoor extends Block implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);

    private final @Nullable TagKey<DragonSpecies> types;
    private final boolean allowHumans;
    private final boolean requiresPower;

    public SmallDragonDoor(final Properties properties) {
        this(properties, null, true, false);
    }

    public SmallDragonDoor(final Properties properties, final TagKey<DragonSpecies> types) {
        this(properties, types, false, false);
    }

    public SmallDragonDoor(final Properties properties, boolean requiresPower) {
        this(properties, null, true, requiresPower);
    }

    public SmallDragonDoor(final Properties properties, @Nullable final TagKey<DragonSpecies> types, boolean allowHumans, boolean requiresPower) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(OPEN, false).setValue(HINGE, DoorHingeSide.LEFT).setValue(POWERED, false));

        this.types = types;
        this.allowHumans = allowHumans;
        this.requiresPower = requiresPower;
    }

    @Override
    @Nullable public BlockState getStateForPlacement(final BlockPlaceContext context) {
        BlockPos clickedPosition = context.getClickedPos();

        if (clickedPosition.getY() < 255) {
            Level level = context.getLevel();
            boolean hasPower = level.hasNeighborSignal(clickedPosition) || level.hasNeighborSignal(clickedPosition.above());
            return defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(HINGE, getHinge(context)).setValue(POWERED, hasPower).setValue(OPEN, hasPower).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
        } else {
            return null;
        }
    }

    private DoorHingeSide getHinge(final BlockPlaceContext context) {
        //TODO Logic handling aligning doors
        BlockGetter iblockreader = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        Direction north = context.getHorizontalDirection();
        Direction directionCounterClockWiseHorizontal = north.getCounterClockWise();
        BlockPos blockpos2 = blockpos.relative(directionCounterClockWiseHorizontal);
        BlockState blockstate = iblockreader.getBlockState(blockpos2);
        Direction direction2 = north.getClockWise();
        BlockPos blockpos4 = blockpos.relative(direction2);
        BlockState blockstate2 = iblockreader.getBlockState(blockpos4);
        int i = (blockstate.isCollisionShapeFullBlock(iblockreader, blockpos2) ? -1 : 0) + (blockstate2.isCollisionShapeFullBlock(iblockreader, blockpos4) ? 1 : 0);
        boolean flag = blockstate.is(this);
        boolean flag1 = blockstate2.is(this);
        if ((!flag || flag1) && i <= 0) {
            if ((!flag1 || flag) && i >= 0) {
                int j = north.getStepX();
                int k = north.getStepZ();
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
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, OPEN, HINGE, POWERED, WATERLOGGED);
    }

    @Override
    public boolean isPathfindable(@NotNull final BlockState state, final PathComputationType type) {
        return switch (type) {
            case LAND, AIR -> state.getValue(OPEN);
            default -> false;
        };
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull final BlockState state, @NotNull final Direction facing, @NotNull final BlockState facingState, @NotNull final LevelAccessor level, @NotNull final BlockPos position, @NotNull final BlockPos facingPosition) {
        if (facing == Direction.DOWN && !state.canSurvive(level, position)) {
            return Blocks.AIR.defaultBlockState();
        }

        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(position, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, facing, facingState, level, position, facingPosition);
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

        boolean hasPower = level.hasNeighborSignal(position) || level.hasNeighborSignal(position.relative(Direction.UP));

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

    private SoundEvent getSound(final BlockState state, boolean isOpening) {
        if (state.is(DSBlockTags.WOODEN_DRAGON_DOORS)) {
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
            BlockState newState = state.cycle(OPEN);
            level.setBlock(position, newState, /* Block.UPDATE_CLIENTS + Block.UPDATE_IMMEDIATE */ 10);
            playSound(player, level, position, newState, newState.getValue(OPEN));
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
        return mirror == Mirror.NONE ? state : state.rotate(mirror.getRotation(state.getValue(FACING))).cycle(HINGE);
    }

    @Override
    public long getSeed(@NotNull final BlockState state, final BlockPos position) {
        return Mth.getSeed(position.getX(), position.below(0).getY(), position.getZ());
    }

    @Override
    public boolean canSurvive(@NotNull final BlockState state, final LevelReader level, final BlockPos position) {
        BlockPos below = position.below();
        BlockState stateBelow = level.getBlockState(below);
        return stateBelow.isFaceSturdy(level, below, Direction.UP);
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
}