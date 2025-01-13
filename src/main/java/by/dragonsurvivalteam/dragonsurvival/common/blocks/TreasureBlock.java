package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.particles.TreasureParticleOption;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncResting;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class TreasureBlock extends FallingBlock implements SimpleWaterloggedBlock {
    @Translation(comments = "■§7 Dragons can sleep on treasure to regenerate health and mana. Gathering more treasure increases the speed of the regeneration. Build your horde and show off your wealth!")
    private static final String TREASURE = Translation.Type.DESCRIPTION.wrap("treasure");

    public static final IntegerProperty LAYERS = BlockStateProperties.LAYERS;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected static final VoxelShape[] SHAPE_BY_LAYER = new VoxelShape[]{Shapes.empty(), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 12.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D)};

    private final int effectColor;

    public TreasureBlock(int effectColor, Properties properties) {
        super(properties);
        this.effectColor = effectColor;

        registerDefaultState(stateDefinition.any().setValue(LAYERS, 1).setValue(WATERLOGGED, false));
    }


    @Override
    public boolean isPathfindable(@NotNull BlockState state, PathComputationType pPathComputationType) {
        return switch (pPathComputationType) {
            case LAND -> state.getValue(LAYERS) < 5;
            case WATER, AIR -> false;
        };
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, final Player player, @NotNull final BlockHitResult hitResult) {
        if (player.getBlockStateOn().getBlock() == state.getBlock()) {
            TreasureRestData treasureData = TreasureRestData.getData(player);
            treasureData.setResting(true);

            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.resetStat(Stats.CUSTOM.get(Stats.TIME_SINCE_REST));
                serverPlayer.serverLevel().updateSleepingPlayerList();
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(serverPlayer, new SyncResting(serverPlayer.getId(), treasureData.isResting()));

                BlockPos respawnPosition = serverPlayer.getRespawnPosition();

                if (respawnPosition == null || serverPlayer.getRespawnDimension() != level.dimension() || !respawnPosition.equals(position) && respawnPosition.distSqr(position) > 40) {
                    serverPlayer.setRespawnPosition(level.dimension(), position, 0, false, true);
                }
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }

        return super.useWithoutItem(state, level, position, player, hitResult);
    }

    @Override
    public boolean useShapeForLightOcclusion(@NotNull BlockState blockState) {
        return true;
    }

    @Override
    public @NotNull Optional<ServerPlayer.RespawnPosAngle> getRespawnPosition(@NotNull BlockState state, @NotNull EntityType<?> type, @NotNull LevelReader levelReader, @NotNull BlockPos pos, float orientation) {
        if (levelReader instanceof Level) {
            Optional<Vec3> standUpPosition = RespawnAnchorBlock.findStandUpPosition(type, levelReader, pos);
            if (standUpPosition.isPresent()) {
                return Optional.of(new ServerPlayer.RespawnPosAngle(standUpPosition.get(), orientation));
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean isBed(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull LivingEntity sleeper) {
        return DragonStateProvider.isDragon(sleeper);
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public boolean canBeReplaced(BlockState blockState, BlockPlaceContext context) {
        int i = blockState.getValue(LAYERS);

        if (context.getItemInHand().getItem() == asItem() && i < 8) {
            if (context.replacingClickedOnBlock()) {
                return context.getClickedFace() == Direction.UP;
            }
        }

        return false;
    }

    @Override
    public @NotNull VoxelShape getBlockSupportShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos) {
        return SHAPE_BY_LAYER[blockState.getValue(LAYERS)];
    }

    @Override
    public float getShadeBrightness(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos) {
        return 1.0F;
    }

    @Override
    public @NotNull VoxelShape getShape(BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
        return SHAPE_BY_LAYER[blockState.getValue(LAYERS)];
    }

    @Override
    public @NotNull VoxelShape getCollisionShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
        return SHAPE_BY_LAYER[Math.max(blockState.getValue(LAYERS), 0)];
    }

    @Override
    public @NotNull VoxelShape getVisualShape(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean propagatesSkylightDown(@NotNull BlockState blockState, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos) {
        return true;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final BlockPlaceContext context) {
        BlockState clickedState = context.getLevel().getBlockState(context.getClickedPos());

        if (clickedState.is(this)) {
            int layers = clickedState.getValue(LAYERS);
            return clickedState.setValue(LAYERS, Math.min(8, layers + 1)).setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
        } else {
            BlockState state = super.getStateForPlacement(context);

            if (state == null) {
                return null;
            }

            return state.setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
        }
    }

    @Override
    public boolean isPossibleToRespawnInThis(@NotNull final BlockState ignored) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(LAYERS);
        builder.add(WATERLOGGED);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        pTooltipComponents.add(Component.translatable(TREASURE));
    }

    @Override
    public void onBrokenAfterFall(Level world, @NotNull BlockPos pos, @NotNull FallingBlockEntity entity) {
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() instanceof TreasureBlock) {
            if (state.getBlock() == entity.getBlockState().getBlock()) {
                int i = state.getValue(LAYERS);
                world.setBlockAndUpdate(pos, state.setValue(LAYERS, Math.min(8, i + entity.getBlockState().getValue(LAYERS))));
            }
        }
    }

    @Override
    protected @NotNull MapCodec<? extends FallingBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    public @NotNull BlockState updateShape(BlockState pState, @NotNull Direction pFacing, @NotNull BlockState pFacingState, @NotNull LevelAccessor pLevel, @NotNull BlockPos pCurrentPos, @NotNull BlockPos pFacingPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }
        pLevel.scheduleTick(pCurrentPos, this, getDelayAfterPlace());
        return super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos blockPos, @NotNull RandomSource randomSource) {
        boolean belowEmpty = isFree(level.getBlockState(blockPos.below())) && blockPos.getY() >= level.getMinBuildHeight();
        boolean lowerLayer = level.getBlockState(blockPos.below()).getBlock() == state.getBlock() && level.getBlockState(blockPos.below()).getValue(LAYERS) < 8;
        if (belowEmpty || lowerLayer) {
            FallingBlockEntity fallingblockentity = new FallingBlockEntity(level, (double) blockPos.getX() + 0.5D, blockPos.getY(), (double) blockPos.getZ() + 0.5D, level.getBlockState(blockPos)) {
                @Override
                public void tick() {
                    BlockState state = level().getBlockState(blockPosition().below());

                    if (state.getBlock() == getBlockState().getBlock()) {
                        int i = state.getValue(LAYERS);

                        // TODO: This code snaps the block in place if it enters the same block as a treasure layer, even if the layer is only 1 block high. Maybe check for collisions with the actual voxel shape somehow?
                        if (i > 0 && i < 8) {
                            int missingLayers = 8 - i;
                            int newLayers = getBlockState().getValue(LAYERS);
                            int leftOver = 0;

                            if (newLayers > missingLayers) {
                                leftOver = newLayers - missingLayers;
                                newLayers = missingLayers;
                            }

                            level().setBlockAndUpdate(blockPosition().below(), state.setValue(LAYERS, Math.min(8, i + newLayers)));

                            if (leftOver > 0) {
                                level().setBlock(blockPosition(), getBlockState().setValue(LAYERS, Math.min(8, leftOver)), Block.UPDATE_ALL);
                            } else {
                                level().setBlock(blockPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                            }

                            remove(RemovalReason.DISCARDED);
                            return;
                        }
                    }

                    super.tick();
                }
            };
            level.setBlock(blockPos, state.getFluidState().createLegacyBlock(), Block.UPDATE_ALL);
            level.addFreshEntity(fallingblockentity);
            falling(fallingblockentity);
        }
    }

    @Override
    public void animateTick(@NotNull BlockState block, Level level, BlockPos position, @NotNull RandomSource random) {
        double xOffset = random.nextDouble();
        double yOffset = block.getValue(LAYERS) * (1.0 / 8) + 0.1;
        double zOffset = random.nextDouble();

        if (level.isEmptyBlock(position.above())) {
            if (random.nextInt(100) < 35) {
                level.addParticle(new TreasureParticleOption(FastColor.ARGB32.red(effectColor) / 255F, FastColor.ARGB32.green(effectColor) / 255F, FastColor.ARGB32.blue(effectColor) / 255F, 1F), (double) position.getX() + xOffset, (double) position.getY() + yOffset, (double) position.getZ() + zOffset, 0, 0, 0);
            }
        }
    }
}