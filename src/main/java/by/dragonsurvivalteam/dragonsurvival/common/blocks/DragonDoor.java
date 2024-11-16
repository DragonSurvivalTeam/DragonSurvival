package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Locale;
import java.util.Objects;


public class DragonDoor extends Block implements SimpleWaterloggedBlock{
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
	public static final EnumProperty<DoorHingeSide> HINGE = BlockStateProperties.DOOR_HINGE;
	public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);
	public static final EnumProperty<DragonDoorOpenRequirement> OPEN_REQ = EnumProperty.create("open_req", DragonDoorOpenRequirement.class);
	protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 3.0D);
	protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 13.0D, 16.0D, 16.0D, 16.0D);
	protected static final VoxelShape WEST_AABB = Block.box(13.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 3.0D, 16.0D, 16.0D);

	public enum Part implements StringRepresentable{
		BOTTOM,
		MIDDLE,
		TOP;


		@Override
		public String getSerializedName(){
			return name().toLowerCase(Locale.ENGLISH);
		}
	}

	public DragonDoor(Properties properties, DragonDoorOpenRequirement DragonDoorOpenRequirement){
		super(properties);
		registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(OPEN, false).setValue(HINGE, DoorHingeSide.LEFT).setValue(POWERED, false).setValue(PART, Part.BOTTOM).setValue(OPEN_REQ, DragonDoorOpenRequirement).setValue(WATERLOGGED, false));
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type){
		switch(type){
			case LAND:
			case AIR:
				return state.getValue(OPEN);
			default:
				return false;
		}
	}

	@Override
	public @NotNull BlockState updateShape(BlockState stateIn, @NotNull Direction facing, @NotNull BlockState facingState, @NotNull LevelAccessor level, @NotNull BlockPos currentPos, @NotNull BlockPos facingPos) {
        if (stateIn.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        if (facing.getAxis() == Direction.Axis.Y && stateIn.is(facingState.getBlock())) {
            // Update the other door parts to match the changed door part
            return stateIn.setValue(FACING, facingState.getValue(FACING))
                    .setValue(OPEN, facingState.getValue(OPEN))
                    .setValue(HINGE, facingState.getValue(HINGE))
                    .setValue(POWERED, facingState.getValue(POWERED));
        }

        return canSurvive(stateIn, level, currentPos) ? super.updateShape(stateIn, facing, facingState, level, currentPos, facingPos) : Blocks.AIR.defaultBlockState();
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving){
		boolean validPower = state.getValue(OPEN_REQ) == DragonDoorOpenRequirement.NONE || state.getValue(OPEN_REQ) == DragonDoorOpenRequirement.POWER;
		boolean validType = state.getValue(OPEN_REQ) == DragonDoorOpenRequirement.SEA && blockIn == DSBlocks.seaPressurePlate || state.getValue(OPEN_REQ) == DragonDoorOpenRequirement.FOREST && blockIn == DSBlocks.forestPressurePlate || state.getValue(OPEN_REQ) == DragonDoorOpenRequirement.CAVE && blockIn == DSBlocks.cavePressurePlate;
		if(validPower || validType){
			boolean flag = worldIn.hasNeighborSignal(pos) || worldIn.hasNeighborSignal(pos.relative(state.getValue(PART) == Part.BOTTOM ? Direction.UP : Direction.DOWN));
			if(blockIn != this && flag != state.getValue(POWERED)){
				if(flag != state.getValue(OPEN)){
					playSound(worldIn, pos, flag);
				}

				worldIn.setBlock(pos, state.setValue(POWERED, flag).setValue(OPEN, flag), Block.UPDATE_CLIENTS);
			}
		}
	}

	private void playSound(Level worldIn, BlockPos pos, boolean isOpening){
		worldIn.levelEvent(null, isOpening ? getOpenSound() : getCloseSound(), pos, 0);
	}

	private int getCloseSound(){
		return material == Material.METAL ? LevelEvent.SOUND_CLOSE_IRON_DOOR : LevelEvent.SOUND_CLOSE_WOODEN_DOOR;
	}

	private int getOpenSound(){
		return material == Material.METAL ? LevelEvent.SOUND_OPEN_IRON_DOOR : LevelEvent.SOUND_OPEN_WOODEN_DOOR;
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit){
		DragonStateHandler dragonStateHandler = DragonUtils.getHandler(player);
		if(state.getValue(OPEN_REQ) == DragonDoorOpenRequirement.NONE || dragonStateHandler.isDragon() && state.getValue(OPEN_REQ) == DragonDoorOpenRequirement.CAVE && Objects.equals(dragonStateHandler.getType(), DragonTypes.CAVE) || state.getValue(
				OPEN_REQ) == DragonDoorOpenRequirement.FOREST && Objects.equals(dragonStateHandler.getType(), DragonTypes.FOREST) || state.getValue(OPEN_REQ) == DragonDoorOpenRequirement.SEA && Objects.equals(dragonStateHandler.getType(), DragonTypes.SEA)){
			state = state.cycle(OPEN).setValue(WATERLOGGED, worldIn.getFluidState(pos).getType() == Fluids.WATER);
			worldIn.setBlock(pos, state, /* Block.UPDATE_CLIENTS + Block.UPDATE_IMMEDIATE */ 10);
			worldIn.levelEvent(player, state.getValue(OPEN) ? getOpenSound() : getCloseSound(), pos, 0);
			if(state.getValue(PART) == Part.TOP){
				worldIn.setBlock(pos.below(2), state.setValue(PART, Part.BOTTOM).setValue(WATERLOGGED, worldIn.getFluidState(pos.below(2)).getType() == Fluids.WATER), 10);
				worldIn.setBlock(pos.below(), state.setValue(PART, Part.MIDDLE).setValue(WATERLOGGED, worldIn.getFluidState(pos.below()).getType() == Fluids.WATER), /* Block.UPDATE_CLIENTS + Block.UPDATE_IMMEDIATE */ 10);
			}
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

	@Override
	public PushReaction getPistonPushReaction(BlockState state){
		return state.getValue(OPEN_REQ) == DragonDoorOpenRequirement.NONE ? PushReaction.DESTROY : PushReaction.IGNORE;
	}

	@Override
	public FluidState getFluidState(BlockState state){
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot){
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn){
		return mirrorIn == Mirror.NONE ? state : state.rotate(mirrorIn.getRotation(state.getValue(FACING))).cycle(HINGE);
	}

	@Override
	public long getSeed(BlockState state, BlockPos pos){
		return Mth.getSeed(pos.getX(), pos.below(state.getValue(PART) == Part.BOTTOM ? 0 : 1).getY(), pos.getZ());
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos){
		BlockPos blockpos = pos.below();
		BlockState blockstate = worldIn.getBlockState(blockpos);
		if(state.getValue(PART) == Part.BOTTOM){
			return blockstate.isFaceSturdy(worldIn, blockpos, Direction.UP);
		}else{
			return blockstate.getBlock() == this;
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		Direction direction = state.getValue(FACING);
		boolean flag = !state.getValue(OPEN);
		boolean flag1 = state.getValue(HINGE) == DoorHingeSide.RIGHT;
		switch(direction){
			case EAST:
			default:
				return flag ? EAST_AABB : flag1 ? NORTH_AABB : SOUTH_AABB;
			case SOUTH:
				return flag ? SOUTH_AABB : flag1 ? EAST_AABB : WEST_AABB;
			case WEST:
				return flag ? WEST_AABB : flag1 ? SOUTH_AABB : NORTH_AABB;
			case NORTH:
				return flag ? NORTH_AABB : flag1 ? WEST_AABB : EAST_AABB;
		}
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context){
		BlockPos blockpos = context.getClickedPos();
		if(blockpos.getY() < 255 && context.getLevel().getBlockState(blockpos.above()).canBeReplaced(context) && context.getLevel().getBlockState(blockpos.above(2)).canBeReplaced(context)){
			Level world = context.getLevel();
			boolean flag = world.hasNeighborSignal(blockpos) || world.hasNeighborSignal(blockpos.above());
			return defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(HINGE, getHinge(context)).setValue(POWERED, flag).setValue(OPEN, flag).setValue(PART, Part.BOTTOM).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER && context.getLevel().getBlockState(blockpos).getBlock() == DSBlocks.seaDoor);
		}else{
			return null;
		}
	}

	private DoorHingeSide getHinge(BlockPlaceContext blockItemUseContext){

		//TODO Logic handling aligning doors
		BlockGetter iblockreader = blockItemUseContext.getLevel();
		BlockPos blockpos = blockItemUseContext.getClickedPos();
		Direction direction = blockItemUseContext.getHorizontalDirection();
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
		if((!flag || flag1) && i <= 0){
			if((!flag1 || flag) && i >= 0){
				int j = direction.getStepX();
				int k = direction.getStepZ();
				Vec3 vec3d = blockItemUseContext.getClickLocation();
				double d0 = vec3d.x - (double)blockpos.getX();
				double d1 = vec3d.z - (double)blockpos.getZ();
				return (j >= 0 || !(d1 < 0.5D)) && (j <= 0 || !(d1 > 0.5D)) && (k >= 0 || !(d0 > 0.5D)) && (k <= 0 || !(d0 < 0.5D)) ? DoorHingeSide.LEFT : DoorHingeSide.RIGHT;
			}else{
				return DoorHingeSide.LEFT;
			}
		}else{
			return DoorHingeSide.RIGHT;
		}
	}

	@Override
	public void playerDestroy(Level worldIn, Player player, BlockPos pos, BlockState state,
		@Nullable
			BlockEntity te, ItemStack stack){
		super.playerDestroy(worldIn, player, pos, Blocks.AIR.defaultBlockState(), te, stack);
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
		worldIn.setBlock(pos.above(), state.setValue(PART, Part.MIDDLE).setValue(WATERLOGGED, worldIn.getFluidState(pos.above()).getType() == Fluids.WATER), Block.UPDATE_ALL);
		worldIn.setBlock(pos.above(2), state.setValue(PART, Part.TOP).setValue(WATERLOGGED, worldIn.getFluidState(pos.above(2)).getType() == Fluids.WATER), Block.UPDATE_ALL);
	}

	@Override
	public void playerWillDestroy(Level worldIn, BlockPos pos, BlockState state, Player player){
		if(!worldIn.isClientSide){
			Part part = state.getValue(PART);
			if(part != Part.MIDDLE && !player.isCreative()){
				BlockPos middlePos = part == Part.BOTTOM ? pos.above() : pos.below();
				BlockState middleState = worldIn.getBlockState(middlePos);
				if(middleState.getBlock() == state.getBlock()){
					worldIn.setBlock(middlePos, Blocks.AIR.defaultBlockState(), /* Block.UPDATE_NEIGHBORS + Block.UPDATE_CLIENTS + Block.UPDATE_SUPPRESS_DROPS */ 35);
					worldIn.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, middlePos, Block.getId(middleState));
				}
			}else if(part != Part.BOTTOM && player.isCreative()){
				BlockPos bottomPos = part == Part.MIDDLE ? pos.below() : pos.below(2);
				BlockState bottomState = worldIn.getBlockState(bottomPos);
				if(bottomState.getBlock() == state.getBlock()){
					worldIn.setBlock(bottomPos, Blocks.AIR.defaultBlockState(), /* Block.UPDATE_NEIGHBORS + Block.UPDATE_CLIENTS + Block.UPDATE_SUPPRESS_DROPS */ 35);
					worldIn.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, bottomPos, Block.getId(bottomState));
				}
			}
		}
		super.playerWillDestroy(worldIn, pos, state, player);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(PART, FACING, OPEN, HINGE, POWERED, OPEN_REQ, WATERLOGGED);
	}

	public enum DragonDoorOpenRequirement implements StringRepresentable{
		NONE,
		POWER,
		CAVE,
		FOREST,
		SEA,
		LOCKED;

		@Override
		public String getSerializedName(){
			return name().toLowerCase(Locale.ENGLISH);
		}
	}
}