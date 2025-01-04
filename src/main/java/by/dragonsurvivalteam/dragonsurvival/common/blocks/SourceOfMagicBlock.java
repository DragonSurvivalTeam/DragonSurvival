package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.*;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicBlockEntity;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicPlaceholder;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.SpawningUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import javax.annotation.Nullable;

// TODO :: add a generic one (same for doors, pressure plates) which is handled through some level storage
//  (said storage contains the relevant types and effects etc. per position aka place block)
public class SourceOfMagicBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock, EntityBlock {
    @Translation(comments = "You need a 3x3 area to place %s")
    private static final String OCCUPIED = Translation.Type.GUI.wrap("message.occupied");

    public static final BooleanProperty PRIMARY_BLOCK = BooleanProperty.create("primary");
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");

    private static final int REQUIRED_SOURCE_OF_MAGIC_TICKS = Functions.secondsToTicks(10);

    private static final VoxelShape SLAB = Shapes.box(0, 0, 0, 1, 0.5, 1);

    // Full height but half "frontal" width to the direction the block is facing
    private static final VoxelShape FULL_NORTH = Shapes.box(0, 0, 0.5, 1, 1, 1);
    private static final VoxelShape FULL_SOUTH = Shapes.box(0, 0, 0, 1, 1, 0.5);
    private static final VoxelShape FULL_EAST = Shapes.box(0, 0, 0, 0.5, 1, 1);
    private static final VoxelShape FULL_WEST = Shapes.box(0.5, 0, 0, 1, 1, 1);

    // Triangle shape
    private static final VoxelShape TOP_NORTH = Shapes.or(FULL_NORTH, Shapes.box(1, 0, 0.5, 1.5, 0.5, 1), Shapes.box(-0.5, 0, 0.5, 0, 0.5, 1));
    private static final VoxelShape TOP_SOUTH = Shapes.or(FULL_SOUTH, Shapes.box(1, 0, 0, 1.5, 0.5, 0.5), Shapes.box(-0.5, 0, 0, 0, 0.5, 0.5));
    private static final VoxelShape TOP_EAST = Shapes.or(FULL_EAST, Shapes.box(0, 0, 1, 0.5, 0.5, 1.5), Shapes.box(0, 0, -0.5, 0.5, 0.5, 0));
    private static final VoxelShape TOP_WEST = Shapes.or(FULL_WEST, Shapes.box(0.5, 0, 1, 1, 0.5, 1.5), Shapes.box(0.5, 0, -0.5, 1, 0.5, 0));

    // Stair shape
    private static final VoxelShape BACK_NORTH = Shapes.or(SLAB, FULL_NORTH);
    private static final VoxelShape BACK_SOUTH = Shapes.or(SLAB, FULL_SOUTH);
    private static final VoxelShape BACK_EAST = Shapes.or(SLAB, FULL_EAST);
    private static final VoxelShape BACK_WEST = Shapes.or(SLAB, FULL_WEST);

    private enum Type implements StringRepresentable {
        GROUND, BACK, BACK_MIDDLE, TOP;

        @Override
        public @NotNull String getSerializedName() {
            return toString().toLowerCase(Locale.ENGLISH);
        }
    }

    private static final EnumProperty<Type> TYPE = EnumProperty.create("type", Type.class);

    /** null -> all are valid */
    private final Function<DamageSources, DamageSource> damageSourceProvider;

    public SourceOfMagicBlock(final Properties properties, final Function<DamageSources, DamageSource> damageSourceProvider) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(BlockStateProperties.WATERLOGGED, false).setValue(PRIMARY_BLOCK, true).setValue(TYPE, Type.GROUND).setValue(FILLED, false));
        this.damageSourceProvider = damageSourceProvider;
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return MapCodec.unit(this);
    }

    private static void breakBlock(final Level level, final BlockPos position) {
        level.destroyBlock(position, !(level.getBlockEntity(position) instanceof SourceOfMagicPlaceholder));
        level.removeBlockEntity(position);
    }

    @Override
    public void animateTick(final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, @NotNull final RandomSource random) {
        if (state.getBlock() != DSBlocks.CAVE_SOURCE_OF_MAGIC.get()) {
            return;
        }

        double x = position.getX();
        double y = position.getY();
        double z = position.getZ();

        if (level.getFluidState(position).is(FluidTags.WATER)) {
            level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, x + 0.5, y, z + 0.5, 0, 0.04, 0);
            level.addAlwaysVisibleParticle(ParticleTypes.BUBBLE_COLUMN_UP, x + (double) random.nextFloat(), y + (double) random.nextFloat(), z + (double) random.nextFloat(), 0, 0.04, 0);
        } else if (state.getValue(FILLED)) {
            level.addAlwaysVisibleParticle(ParticleTypes.LAVA, x + (double) random.nextFloat(), y + (double) random.nextFloat(), z + (double) random.nextFloat(), 0.0D, 0.04D, 0.0D);
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final BlockPlaceContext context) {
        BlockPos clickedPosition = context.getClickedPos();
        Level level = context.getLevel();

        Player player = context.getPlayer();
        Direction direction = player != null ? player.getDirection() : Direction.getRandom(level.getRandom());

        AtomicBoolean isValid = new AtomicBoolean(true);

        // Need to check in the stream due to the usage of 'BlockPos$MutableBlockPos'
        BlockPos.betweenClosedStream(clickedPosition.getX() - 1, clickedPosition.getY(), clickedPosition.getZ() - 1, clickedPosition.getX() + 1, clickedPosition.getY(), clickedPosition.getZ() + 1).forEach(position -> {
            if (isValid.get() && !SpawningUtils.isAirOrFluid(position, level, context)) {
                if (player != null && level.isClientSide()) {
                    player.sendSystemMessage(Component.translatable(OCCUPIED, asItem().getDefaultInstance().getDisplayName()));
                }

                isValid.set(false);
            }
        });

        if (!isValid.get()) {
            return null;
        }

        // Check the backside which has a part which is two blocks high // TODO :: should this also have a message?
        if (/* behind of the clicked position + 1 height */ !SpawningUtils.isAirOrFluid(clickedPosition.relative(direction).above(), level, context)) {
            return null;
        }

        if (/* right corner behind of the clicked position + 1 height */ !SpawningUtils.isAirOrFluid(clickedPosition.relative(direction).above().relative(direction.getClockWise()), level, context)) {
            return null;
        }

        if (/* left corner behind of the clicked position + 1 height */ !SpawningUtils.isAirOrFluid(clickedPosition.relative(direction).above().relative(direction.getCounterClockWise()), level, context)) {
            return null;
        }

        BlockState state = super.getStateForPlacement(context);

        if (state != null) {
            state = state.setValue(FACING, direction.getOpposite());
        }

        return state;
    }

    @Override
    public void setPlacedBy(@NotNull final Level level, @NotNull final BlockPos position, @NotNull final BlockState state, @Nullable final LivingEntity placer, @NotNull final ItemStack stack) {
        super.setPlacedBy(level, position, state, placer, stack);

        if (placer != null) {
            Direction direction = placer.getDirection();
            setPlaceholder(level, state, position, position.relative(direction.getOpposite()));
            setPlaceholder(level, state.setValue(TYPE, Type.BACK), position, position.relative(direction));

            setPlaceholder(level, state, position, position.relative(direction.getClockWise()));
            setPlaceholder(level, state, position, position.relative(direction.getCounterClockWise()));

            setPlaceholder(level, state.setValue(TYPE, Type.BACK), position, position.relative(direction).relative(direction.getClockWise()));
            setPlaceholder(level, state.setValue(TYPE, Type.BACK), position, position.relative(direction).relative(direction.getCounterClockWise()));

            setPlaceholder(level, state, position, position.relative(direction.getOpposite()).relative(direction.getCounterClockWise()));
            setPlaceholder(level, state, position, position.relative(direction.getOpposite()).relative(direction.getClockWise()));

            setPlaceholder(level, state.setValue(TYPE, Type.TOP), position, position.above().relative(direction));
            setPlaceholder(level, state.setValue(TYPE, Type.BACK_MIDDLE), position, position.above().relative(direction).relative(direction.getCounterClockWise()));
            setPlaceholder(level, state.setValue(TYPE, Type.BACK_MIDDLE), position, position.above().relative(direction).relative(direction.getClockWise()));
        }
    }

    @Override
    protected void createBlockStateDefinition(@NotNull final Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.WATERLOGGED, FACING, PRIMARY_BLOCK, TYPE, FILLED);
    }

    private static void setPlaceholder(final Level level, final BlockState state, final BlockPos rootPosition, final BlockPos newPosition) {
        if (!(state.getBlock() instanceof SourceOfMagicBlock)) {
            level.setBlockAndUpdate(newPosition, state);
            return;
        }

        level.setBlockAndUpdate(newPosition, state.setValue(PRIMARY_BLOCK, false));

        if (level.getBlockEntity(newPosition) instanceof SourceOfMagicPlaceholder placeholder) {
            placeholder.rootPos = rootPosition;
        }
    }

    @Override
    public @NotNull BlockState updateShape(final BlockState state, @NotNull final Direction facing, @NotNull final BlockState neighborState, @NotNull final LevelAccessor level, @NotNull final BlockPos position, @NotNull final BlockPos neighborPosition) {
        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.scheduleTick(position, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, facing, neighborState, level, position, neighborPosition);
    }

    @Override
    public void onRemove(@NotNull final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, final BlockState newState, boolean isMoving) {
        if (newState.getBlock() instanceof SourceOfMagicBlock) {
            return;
        }

        if (state.getValue(PRIMARY_BLOCK)) {
            if (level.getBlockEntity(position) instanceof Container container) {
                Containers.dropContents(level, position, container);
                level.updateNeighbourForOutputSignal(position, this);
            }

            super.onRemove(state, level, position, newState, isMoving);

            Direction direction = state.getValue(FACING).getOpposite();

            breakBlock(level, position);

            breakBlock(level, position.relative(direction.getOpposite()));
            breakBlock(level, position.relative(direction));

            breakBlock(level, position.relative(direction.getClockWise()));
            breakBlock(level, position.relative(direction.getCounterClockWise()));

            breakBlock(level, position.relative(direction).relative(direction.getClockWise()));
            breakBlock(level, position.relative(direction).relative(direction.getCounterClockWise()));

            breakBlock(level, position.relative(direction.getOpposite()).relative(direction.getCounterClockWise()));
            breakBlock(level, position.relative(direction.getOpposite()).relative(direction.getClockWise()));

            breakBlock(level, position.above().relative(direction));
            breakBlock(level, position.above().relative(direction).relative(direction.getCounterClockWise()));
            breakBlock(level, position.above().relative(direction).relative(direction.getClockWise()));
        } else if (level.getBlockEntity(position) instanceof SourceOfMagicPlaceholder placeholder) {
            if (level.getBlockEntity(placeholder.rootPos) instanceof SourceOfMagicBlockEntity root) {
                onRemove(root.getBlockState(), level, placeholder.rootPos, Blocks.BUBBLE_COLUMN.defaultBlockState(), isMoving);
            }
        }
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull final BlockState state, final Level level, @NotNull final BlockPos position, @NotNull final Player player, @NotNull final BlockHitResult hitResult) {
        BlockPos rootPosition;

        if (level.getBlockEntity(position) instanceof SourceOfMagicPlaceholder placeholder) {
            rootPosition = placeholder.rootPos;
        } else {
            rootPosition = position;
        }

        if (!player.isCrouching() && getSource(level, rootPosition) instanceof MenuProvider provider) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(provider, buffer -> buffer.writeBlockPos(rootPosition));
            }

            return InteractionResult.sidedSuccess(player.level().isClientSide());
        }

        // TODO :: previously on the else branch the entity was marked to be on magic source (if the source was not empty)

        return InteractionResult.PASS;
    }

    @Override
    public boolean triggerEvent(@NotNull final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, int id, int param) {
        super.triggerEvent(state, level, position, id, param);
        BlockEntity blockentity = level.getBlockEntity(position);
        return blockentity != null && blockentity.triggerEvent(id, param);
    }

    @Override
    public @NotNull RenderShape getRenderShape(final BlockState state) {
        return state.getValue(PRIMARY_BLOCK) ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    public @NotNull FluidState getFluidState(final BlockState state) {
        return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    @Nullable public MenuProvider getMenuProvider(@NotNull final BlockState state, final Level level, @NotNull final BlockPos position) {
        BlockEntity blockentity = level.getBlockEntity(position);
        return blockentity instanceof MenuProvider ? (MenuProvider) blockentity : null;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull final BlockState state, @NotNull final BlockGetter level, @NotNull final BlockPos position, @NotNull final CollisionContext context) {
        return getShape(state);
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull final BlockState state, @NotNull final BlockGetter level, @NotNull final BlockPos position, @NotNull final CollisionContext context) {
        return getShape(state);
    }

    @Override
    public void randomTick(@NotNull final BlockState state, final ServerLevel world, final BlockPos position, @NotNull final RandomSource random) {
        BlockPos above = position.above();

        if (world.getFluidState(position).is(FluidTags.WATER)) {
            world.playSound(null, position, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);
            world.sendParticles(ParticleTypes.LARGE_SMOKE, (double) above.getX() + 0.5D, (double) above.getY() + 0.25D, (double) above.getZ() + 0.5D, 8, 0.5D, 0.25D, 0.5D, 0.0D);
        }
    }

    @Override
    public void entityInside(@NotNull final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, @NotNull final Entity entity) {
        super.entityInside(state, level, position, entity);

        BlockPos sourcePosition = position;

        if (level.getBlockEntity(position) instanceof SourceOfMagicPlaceholder placeholder) {
            sourcePosition = placeholder.rootPos;
        }

        SourceOfMagicBlockEntity source = getSource(level, sourcePosition);

        if (source == null) {
            return;
        }

        if (entity instanceof ItemEntity item) {
            ItemStack stack = item.getItem();
            ItemStack tileStack = source.getItem(0);

            if (source.getDuration(stack.getItem()) > 0) {
                if (source.isEmpty()) {
                    source.setItem(0, stack);
                    item.kill();
                } else if (ItemStack.isSameItem(tileStack, stack) && tileStack.getCount() < tileStack.getMaxStackSize()) {
                    int left = tileStack.getMaxStackSize() - tileStack.getCount();
                    int toAdd = Math.min(stack.getCount(), left);
                    item.getItem().shrink(toAdd);
                    tileStack.setCount(tileStack.getCount() + toAdd);
                }
            }
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (source.isApplicableFor(handler) && isMagic(state) && !source.isEmpty()) {
            if (handler.magicSource > REQUIRED_SOURCE_OF_MAGIC_TICKS) {
                handler.magicSource = 0;

                MobEffectInstance instance = player.getEffect(DSEffects.SOURCE_OF_MAGIC);
                int duration = source.getCurrentDuration();

                if (instance == null) {
                    player.addEffect(new MobEffectInstance(DSEffects.SOURCE_OF_MAGIC, duration, 0, true, false));
                } else {
                    // TODO :: should there be a max. duration?
                    player.addEffect(new MobEffectInstance(DSEffects.SOURCE_OF_MAGIC, instance.getDuration() + duration, 0, true, false));
                }

                player.playNotifySound(SoundEvents.BEACON_POWER_SELECT, SoundSource.NEUTRAL, 1, 1);
                source.removeItem(0, 1);
            } else {
                RandomSource random = player.getRandom();
                double x = -1 + random.nextDouble() * 2;
                double z = -1 + random.nextDouble() * 2;

                ParticleOptions particle = null;

                if (state.getBlock() == DSBlocks.CAVE_SOURCE_OF_MAGIC.get()) {
                    particle = DSParticles.CAVE_BEACON_PARTICLE.value();
                } else if (state.getBlock() == DSBlocks.FOREST_SOURCE_OF_MAGIC.get()) {
                    particle = DSParticles.FOREST_BEACON_PARTICLE.value();
                } else if (state.getBlock() == DSBlocks.SEA_SOURCE_OF_MAGIC.get()) {
                    particle = DSParticles.SEA_BEACON_PARTICLE.value();
                }

                if (particle != null) {
                    player.level().addParticle(particle, player.getX() + x, player.getY() + 0.5, player.getZ() + z, 0, 0, 0);
                }
            }
        } else if (!source.isApplicableFor(handler) && ServerConfig.damageWrongSourceOfMagic) {
            entity.hurt(damageSourceProvider.apply(entity.damageSources()), isMagic(state) ? 1f : 0.5f);
        }
    }

    public SourceOfMagicBlockEntity getSource(final Level level, final BlockPos position) {
        BlockEntity entity = level.getBlockEntity(position);
        return entity instanceof SourceOfMagicBlockEntity source ? source : null;
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

    @Override // Entrypoint for bucket interaction
    public @NotNull ItemStack pickupBlock(@Nullable final Player player, @NotNull final LevelAccessor level, @NotNull final BlockPos position, @NotNull final BlockState state) {
        BlockEntity entity = level.getBlockEntity(position);
        BlockPos rootPosition = null;

        if (state.getValue(BlockStateProperties.WATERLOGGED)) {
            level.setBlock(position, state.setValue(BlockStateProperties.WATERLOGGED, false), Block.UPDATE_ALL);
            return Items.WATER_BUCKET.getDefaultInstance();
        }

        if (entity instanceof SourceOfMagicPlaceholder placeholder) {
            rootPosition = placeholder.rootPos;
        } else if (entity instanceof SourceOfMagicBlockEntity source) {
            return updateAndTakeLiquid(level, position, state, source);
        }

        if (rootPosition != null && level.getBlockEntity(rootPosition) instanceof SourceOfMagicBlockEntity source) {
            return updateAndTakeLiquid(level, rootPosition, level.getBlockState(rootPosition), source);
        }

        return ItemStack.EMPTY;
    }

    /**
     * If the block entity is filled ({@link SourceOfMagicBlock#FILLED}) it will return the appropriate liquid <br>
     * If an item was present in the container (see {@link SourceOfMagicBlockEntity#consumables} then said stack may be decremented <br> <br>
     * This happens here because in {@link SourceOfMagicBlockEntity#serverTick(Level, BlockPos, BlockState, SourceOfMagicBlockEntity)} it will fill back up if an item is present
     */
    private ItemStack updateAndTakeLiquid(final LevelAccessor level, final BlockPos position, final BlockState state, final SourceOfMagicBlockEntity source) {
        if (!state.getValue(SourceOfMagicBlock.FILLED)) {
            return ItemStack.EMPTY;
        }

        level.setBlock(position, state.setValue(SourceOfMagicBlock.FILLED, false), Block.UPDATE_ALL);
        Item item = source.getItem(0).getItem();
        Block block = state.getBlock();

        boolean decrementStack = false;

        if (item == DSItems.ELDER_DRAGON_DUST.value()) {
            decrementStack = true;
        } else if (item == DSItems.ELDER_DRAGON_BONE.value()) {
            decrementStack = level.getRandom().nextInt(3) == 0;
        } else if (item == DSItems.DRAGON_HEART_SHARD.value()) {
            decrementStack = level.getRandom().nextInt(5) == 0;
        } else if (item == DSItems.WEAK_DRAGON_HEART.value()) {
            decrementStack = level.getRandom().nextInt(15) == 0;
        } else if (item == DSItems.ELDER_DRAGON_HEART.value()) {
            decrementStack = level.getRandom().nextInt(50) == 0;
        }

        if (decrementStack) {
            source.removeItem(0, 1);
        }

        // TODO :: add custom liquid (poison) for forest dragons? could possibly have various interactions
        if (block == DSBlocks.CAVE_SOURCE_OF_MAGIC.get()) {
            return Items.LAVA_BUCKET.getDefaultInstance();
        } else if (block == DSBlocks.SEA_SOURCE_OF_MAGIC.get() || block == DSBlocks.FOREST_SOURCE_OF_MAGIC.get()) {
            return Items.WATER_BUCKET.getDefaultInstance();
        }

        return ItemStack.EMPTY;
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos position, final BlockState state) {
        if (!state.getValue(PRIMARY_BLOCK)) {
            return DSTileEntities.SOURCE_OF_MAGIC_PLACEHOLDER.get().create(position, state);
        }

        return DSTileEntities.SOURCE_OF_MAGIC_TILE_ENTITY.get().create(position, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(final Level level, @NotNull final BlockState state, @NotNull final BlockEntityType<T> type) {
        return level.isClientSide ? null : BaseEntityBlock.createTickerHelper(type, DSTileEntities.SOURCE_OF_MAGIC_TILE_ENTITY.get(), SourceOfMagicBlockEntity::serverTick);
    }

    public boolean isMagic(final BlockState state) {
        return state.getValue(SourceOfMagicBlock.PRIMARY_BLOCK) && state.getValue(SourceOfMagicBlock.FILLED);
    }

    private VoxelShape getShape(final BlockState state) {
        Direction facing = state.getValue(FACING);
        Type type = state.getValue(TYPE);

        if (type == Type.TOP) {
            return switch (facing) {
                case NORTH -> TOP_NORTH;
                case SOUTH -> TOP_SOUTH;
                case EAST -> TOP_EAST;
                case WEST -> TOP_WEST;
                default -> Shapes.block();
            };
        }

        if (type == Type.BACK) {
            return switch (facing) {
                case NORTH -> BACK_NORTH;
                case SOUTH -> BACK_SOUTH;
                case EAST -> BACK_EAST;
                case WEST -> BACK_WEST;
                default -> Shapes.block();
            };
        }

        if (type == Type.BACK_MIDDLE) {
            // Collision is handled by the top shape
            // Otherwise we would need more specific block states to handle left / right from top
            return Shapes.empty();
        }

        return SLAB;
    }
}