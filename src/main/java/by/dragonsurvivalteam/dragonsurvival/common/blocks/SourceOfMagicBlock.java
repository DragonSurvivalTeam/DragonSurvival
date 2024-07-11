package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncMagicSourceStatus;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DSTileEntities;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicPlaceholder;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicTileEntity;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.SpawningUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class SourceOfMagicBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock, EntityBlock{
	public static final VoxelShape SHAPE = Shapes.box(0, 0, 0, 1, 0.25, 1);
	public static final VoxelShape OUTLINE = Shapes.box(0, 0, 0, 1, 0.5, 1);
	public static final VoxelShape FULL_OUTLINE = Shapes.box(0, 0, 0, 1, 0.99, 1);

	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final BooleanProperty PRIMARY_BLOCK = BooleanProperty.create("primary");
	public static final BooleanProperty FILLED = BooleanProperty.create("filled");
	static final BooleanProperty BACK_BLOCK = BooleanProperty.create("back");
	static final BooleanProperty TOP_BLOCK = BooleanProperty.create("top");

	public SourceOfMagicBlock(Properties properties){
		super(properties);
		registerDefaultState(getStateDefinition().any().setValue(WATERLOGGED, false).setValue(PRIMARY_BLOCK, true).setValue(BACK_BLOCK, false).setValue(TOP_BLOCK, false).setValue(FILLED, false));
	}

	private static void breakBlock(Level world, BlockPos pos){
		world.destroyBlock(pos, !(world.getBlockEntity(pos) instanceof SourceOfMagicPlaceholder));
		world.removeBlockEntity(pos);
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public void animateTick(BlockState state, Level level, BlockPos position, RandomSource random){
		if(state.getBlock() == DSBlocks.caveSourceOfMagic){
			if(level.getFluidState(position).is(FluidTags.WATER)){
				double d0 = position.getX();
				double d1 = position.getY();
				double d2 = position.getZ();
				level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d0 + 0.5D, d1, d2 + 0.5D, 0.0D, 0.04D, 0.0D);
				level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, d0 + (double)random.nextFloat(), d1 + (double)random.nextFloat(), d2 + (double)random.nextFloat(), 0.0D, 0.04D, 0.0D);
			}else{
				if(state.getValue(FILLED)){
					double d0 = position.getX();
					double d1 = position.getY();
					double d2 = position.getZ();
					level.addAlwaysVisibleParticle(ParticleTypes.LAVA, d0 + (double)random.nextFloat(), d1 + (double)random.nextFloat(), d2 + (double)random.nextFloat(), 0.0D, 0.04D, 0.0D);
				}
			}
		}
	}

	@Override
	public @Nullable BlockState getStateForPlacement(BlockPlaceContext context){
		BlockState superState = null;
		BlockPos blockPos = context.getClickedPos();
		Level world = context.getLevel();
		Player playerEntity = context.getPlayer();
		Direction direction = playerEntity.getDirection();

		if(SpawningUtils.isAirOrFluid(blockPos.relative(direction), world, context) && SpawningUtils.isAirOrFluid(blockPos.relative(direction.getCounterClockWise()), world, context) && SpawningUtils.isAirOrFluid(blockPos.relative(direction).relative(direction.getCounterClockWise()), world, context)){
			superState = super.getStateForPlacement(context).setValue(FACING, direction.getOpposite());
		}

		if(superState != null){
			if(SpawningUtils.isAirOrFluid(blockPos.relative(direction.getOpposite()), world, context) && SpawningUtils.isAirOrFluid(blockPos.relative(direction).relative(direction.getClockWise()), world, context) && SpawningUtils.isAirOrFluid(blockPos.relative(direction.getClockWise()), world, context) && SpawningUtils.isAirOrFluid(blockPos.relative(direction.getOpposite()).relative(direction.getCounterClockWise()), world, context) && SpawningUtils.isAirOrFluid(blockPos.relative(direction.getOpposite()).relative(direction.getClockWise()), world, context) && SpawningUtils.isAirOrFluid(blockPos.relative(direction).above(), world, context) && SpawningUtils.isAirOrFluid(blockPos.relative(direction).above().relative(direction.getClockWise()), world, context) && SpawningUtils.isAirOrFluid(blockPos.relative(direction).above().relative(direction.getCounterClockWise()), world, context)){
				return superState;
			}

			if(world.isClientSide()){
				playerEntity.sendSystemMessage(Component.translatable("ds.space.occupied"));
			}
		}

		return null;
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack){
		super.setPlacedBy(worldIn, pos, state, placer, stack);

		if(placer != null){
			Direction direction = placer.getDirection();
			setPlaceholder(worldIn, state, pos, pos.relative(direction.getOpposite()));
			setPlaceholder(worldIn, state.setValue(BACK_BLOCK, true), pos, pos.relative(direction));

			setPlaceholder(worldIn, state, pos, pos.relative(direction.getClockWise()));
			setPlaceholder(worldIn, state, pos, pos.relative(direction.getCounterClockWise()));

			setPlaceholder(worldIn, state.setValue(BACK_BLOCK, true), pos, pos.relative(direction).relative(direction.getClockWise()));
			setPlaceholder(worldIn, state.setValue(BACK_BLOCK, true), pos, pos.relative(direction).relative(direction.getCounterClockWise()));

			setPlaceholder(worldIn, state, pos, pos.relative(direction.getOpposite()).relative(direction.getCounterClockWise()));
			setPlaceholder(worldIn, state, pos, pos.relative(direction.getOpposite()).relative(direction.getClockWise()));

			setPlaceholder(worldIn, state.setValue(TOP_BLOCK, true), pos, pos.above().relative(direction));
			setPlaceholder(worldIn, state, pos, pos.above().relative(direction).relative(direction.getCounterClockWise()));
			setPlaceholder(worldIn, state, pos, pos.above().relative(direction).relative(direction.getClockWise()));
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder){
		super.createBlockStateDefinition(builder);
		builder.add(FACING, WATERLOGGED, PRIMARY_BLOCK, BACK_BLOCK, TOP_BLOCK, FILLED);
	}

	private static void setPlaceholder(Level world, BlockState state, BlockPos root, BlockPos newPos){
		world.setBlockAndUpdate(newPos, state.setValue(PRIMARY_BLOCK, false));
		SourceOfMagicPlaceholder placeHolder6 = (SourceOfMagicPlaceholder)world.getBlockEntity(newPos);
		placeHolder6.rootPos = root;
	}

	@Override
	public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState1, LevelAccessor world, BlockPos blockPos, BlockPos blockPos1){
		if(blockState.getValue(WATERLOGGED)){
			world.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
		}
		return super.updateShape(blockState, direction, blockState1, world, blockPos, blockPos1);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving){
		if(!(newState.getBlock() instanceof SourceOfMagicBlock)){
			if(state.getValue(PRIMARY_BLOCK)){
				BlockEntity tileentity = worldIn.getBlockEntity(pos);
				if(tileentity instanceof Container){
					Containers.dropContents(worldIn, pos, (Container)tileentity);
					worldIn.updateNeighbourForOutputSignal(pos, this);
				}

				super.onRemove(state, worldIn, pos, newState, isMoving);

				Direction direction = state.getValue(FACING).getOpposite();

				breakBlock(worldIn, pos);

				breakBlock(worldIn, pos.relative(direction.getOpposite()));
				breakBlock(worldIn, pos.relative(direction));

				breakBlock(worldIn, pos.relative(direction.getClockWise()));
				breakBlock(worldIn, pos.relative(direction.getCounterClockWise()));

				breakBlock(worldIn, pos.relative(direction).relative(direction.getClockWise()));
				breakBlock(worldIn, pos.relative(direction).relative(direction.getCounterClockWise()));

				breakBlock(worldIn, pos.relative(direction.getOpposite()).relative(direction.getCounterClockWise()));
				breakBlock(worldIn, pos.relative(direction.getOpposite()).relative(direction.getClockWise()));

				breakBlock(worldIn, pos.above().relative(direction));
				breakBlock(worldIn, pos.above().relative(direction).relative(direction.getCounterClockWise()));
				breakBlock(worldIn, pos.above().relative(direction).relative(direction.getClockWise()));
			}else{
				BlockEntity tile = worldIn.getBlockEntity(pos);
				if(tile instanceof SourceOfMagicPlaceholder placeholder){
					BlockPos rootPos = placeholder.rootPos;

					if(worldIn.getBlockEntity(rootPos) instanceof SourceOfMagicTileEntity){
						onRemove(worldIn.getBlockState(rootPos), worldIn, rootPos, Blocks.BUBBLE_COLUMN.defaultBlockState(), isMoving);
					}
				}
			}
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit){
		BlockEntity blockEntity = worldIn.getBlockEntity(pos);
		BlockPos pos1 = pos;

		if(blockEntity instanceof SourceOfMagicPlaceholder){
			pos1 = ((SourceOfMagicPlaceholder)blockEntity).rootPos;
		}

		if(!player.isCrouching()){
			if(player instanceof ServerPlayer){
				BlockPos finalPos = pos1;
				BlockEntity blockEntity1 = getBlockEntity(worldIn, pos1);
				NetworkHooks.openScreen((ServerPlayer)player, (MenuProvider)blockEntity1, packetBuffer -> packetBuffer.writeBlockPos(finalPos));
			}
		}else{
			if(DragonUtils.isDragon(player) && player.getMainHandItem().isEmpty()){
				if(player.getFeetBlockState().getBlock() == state.getBlock()){
					DragonStateHandler handler = DragonUtils.getHandler(player);

					if(!handler.getMagicData().onMagicSource){
						SourceOfMagicTileEntity source = getBlockEntity(worldIn, pos1);

						if(source != null && !source.isEmpty()){
							if(worldIn.isClientSide()){
								NetworkHandler.CHANNEL.sendToServer(new SyncMagicSourceStatus(player.getId(), true, 0));
							}
						}
					}
				}
			}
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean triggerEvent(BlockState pState, Level pLevel, BlockPos pPos, int pId, int pParam){
		super.triggerEvent(pState, pLevel, pPos, pId, pParam);
		BlockEntity blockentity = pLevel.getBlockEntity(pPos);
		return blockentity != null && blockentity.triggerEvent(pId, pParam);
	}

	@Override
	public RenderShape getRenderShape(BlockState state){
		return state.getValue(PRIMARY_BLOCK) ? RenderShape.MODEL : RenderShape.INVISIBLE;
	}

	@Override
	public FluidState getFluidState(BlockState blockState){
		return blockState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(blockState);
	}

	@Override
	public @Nullable MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos){
		BlockEntity blockentity = pLevel.getBlockEntity(pPos);
		return blockentity instanceof MenuProvider ? (MenuProvider)blockentity : null;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		return state.getValue(TOP_BLOCK) || state.getValue(BACK_BLOCK) ? FULL_OUTLINE : OUTLINE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		return state.getValue(TOP_BLOCK) ? FULL_OUTLINE : SHAPE;
	}

	@Override
	public void randomTick(BlockState p_225542_1_, ServerLevel world, BlockPos pos, RandomSource p_225542_4_){
		BlockPos blockpos = pos.above();
		if(world.getFluidState(pos).is(FluidTags.WATER)){
			world.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
			world.sendParticles(ParticleTypes.LARGE_SMOKE, (double)blockpos.getX() + 0.5D, (double)blockpos.getY() + 0.25D, (double)blockpos.getZ() + 0.5D, 8, 0.5D, 0.25D, 0.5D, 0.0D);
		}
	}

	@Override
	public void entityInside(BlockState pState, Level world, BlockPos pos, Entity entity){
		BlockEntity blockEntity = world.getBlockEntity(pos);
		BlockPos pos1 = pos;

		if(blockEntity instanceof SourceOfMagicPlaceholder){
			pos1 = ((SourceOfMagicPlaceholder)blockEntity).rootPos;
		}

		SourceOfMagicTileEntity source = getBlockEntity(world, pos1);

		if(source != null){
			if(shouldHarmPlayer(pState, entity)){
				if(entity instanceof ItemEntity itemE){
					ItemStack stack = itemE.getItem();
					ItemStack tileStack = source.getItem(0);
					if(SourceOfMagicTileEntity.consumables.containsKey(stack.getItem())){
						if(source.isEmpty()){
							source.setItem(0, stack);
							itemE.kill();
						}else if(ItemStack.isSameItem(tileStack, stack) && tileStack.getCount() < tileStack.getMaxStackSize()){
							int left = tileStack.getMaxStackSize() - tileStack.getCount();
							int toAdd = Math.min(stack.getCount(), left);
							itemE.getItem().shrink(toAdd);
							tileStack.setCount(tileStack.getCount() + toAdd);
						}
						return;
					}
				}

				if(ServerConfig.damageWrongSourceOfMagic){
					entity.hurt(pState.getBlock() == DSBlocks.caveSourceOfMagic ? entity.damageSources().hotFloor() : pState.getBlock() == DSBlocks.seaSourceOfMagic ? entity.damageSources().drown() : entity.damageSources().cactus(), 1F);
				}
			}
		}
		super.entityInside(pState, world, pos, entity);
	}

	public static boolean shouldHarmPlayer(BlockState state, Entity entity) {
		DragonStateHandler handler = DragonStateProvider.getHandler(entity);
		AbstractDragonType type = handler != null ? handler.getType() : null;
		Block block = state.getBlock();

		if (block == DSBlocks.caveSourceOfMagic && !DragonUtils.isDragonType(type, DragonTypes.CAVE)) {
			return true;
		}

		if (block == DSBlocks.seaSourceOfMagic && !DragonUtils.isDragonType(type, DragonTypes.SEA)) {
			return true;
		}

		return block == DSBlocks.forestSourceOfMagic && !DragonUtils.isDragonType(type, DragonTypes.FOREST);
	}
	
	public SourceOfMagicTileEntity getBlockEntity(Level world, BlockPos pos){
		BlockEntity entity = world.getBlockEntity(pos);
		return entity instanceof SourceOfMagicTileEntity ? (SourceOfMagicTileEntity)entity : null;
	}

	@Override
	public boolean placeLiquid(@NotNull final LevelAccessor level, @NotNull final BlockPos position, @NotNull final BlockState state, @NotNull final FluidState fluidState) {
		if (!state.getValue(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
			if (!level.isClientSide()) {
				level.setBlock(position, state.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE), Block.UPDATE_ALL);
				level.scheduleTick(position, fluidState.getType(), fluidState.getType().getTickDelay(level));
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public @NotNull ItemStack pickupBlock(@NotNull final LevelAccessor level, @NotNull final BlockPos position, @NotNull final BlockState state) {
		BlockEntity entity = level.getBlockEntity(position);
		BlockPos rootPosition = null;

		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			level.setBlock(position, state.setValue(BlockStateProperties.WATERLOGGED, false), Block.UPDATE_ALL);
			return Items.WATER_BUCKET.getDefaultInstance();
		}

		if (entity instanceof SourceOfMagicPlaceholder placeholder) {
			rootPosition = placeholder.rootPos;
		} else if (entity instanceof SourceOfMagicTileEntity source) {
			return updateAndTakeLiquid(level, position, state, source);
		}

		if (rootPosition != null && level.getBlockEntity(rootPosition) instanceof SourceOfMagicTileEntity source) {
			return updateAndTakeLiquid(level, rootPosition, level.getBlockState(rootPosition), source);
		}

		return ItemStack.EMPTY;
	}

	private ItemStack updateAndTakeLiquid(final LevelAccessor level, final BlockPos position, final BlockState state, final SourceOfMagicTileEntity source) {
		if (!state.getValue(SourceOfMagicBlock.FILLED)) {
			return ItemStack.EMPTY;
		}

		level.setBlock(position, state.setValue(SourceOfMagicBlock.FILLED, false), Block.UPDATE_ALL);
		Item item = source.getItem(0).getItem();
		Block block = state.getBlock();

		boolean decrementStack = false;

		if (item == DSItems.elderDragonDust) {
			decrementStack = true;
		} else if (item == DSItems.elderDragonBone) {
			decrementStack = level.getRandom().nextInt(3) == 0;
		} else if (item == DSItems.dragonHeartShard) {
			decrementStack = level.getRandom().nextInt(5) == 0;
		} else if (item == DSItems.weakDragonHeart) {
			decrementStack = level.getRandom().nextInt(15) == 0;
		} else if (item == DSItems.elderDragonHeart) {
			decrementStack = level.getRandom().nextInt(50) == 0;
		}

		if (decrementStack) {
			source.removeItem(0, 1);
		}

		if (block == DSBlocks.caveSourceOfMagic) {
			return Items.LAVA_BUCKET.getDefaultInstance();
		} else if (block == DSBlocks.seaSourceOfMagic || block == DSBlocks.forestSourceOfMagic) {
			return Items.WATER_BUCKET.getDefaultInstance();
		}

		return ItemStack.EMPTY;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		if (!pState.getValue(PRIMARY_BLOCK)) {
			return DSTileEntities.sourceOfMagicPlaceholder.create(pPos, pState);
		}
		return DSTileEntities.sourceOfMagicTileEntity.create(pPos, pState);
	}

	@Override
	@Nullable
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType){
		return pLevel.isClientSide() ? null : BaseEntityBlock.createTickerHelper(pBlockEntityType, DSTileEntities.sourceOfMagicTileEntity, SourceOfMagicTileEntity::serverTick);
	}
}