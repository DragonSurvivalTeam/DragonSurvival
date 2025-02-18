package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncEnderDragonMark;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.PrimordialAnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Mixture of vanilla implementation from {@link RespawnAnchorBlock} and {@link TheEndGatewayBlockEntity} */
public class PrimordialAnchorBlock extends Block implements EntityBlock {
    @Translation(key = "primordial_anchor_gives_flight_grant_state", type = Translation.Type.CONFIGURATION, comments = "If enabled, the primordial anchor will give the flight grant state.")
    @ConfigOption(side = ConfigSide.SERVER, category = "primordial_anchor", key = "primordial_anchor_gives_flight_grant_state")
    public static Boolean anchorGivesFlightGrantState = true;

    @Translation(key = "primordial_anchor_gives_spin_grant_state", type = Translation.Type.CONFIGURATION, comments = "If enabled, the primordial anchor will give the spin grant state.")
    @ConfigOption(side = ConfigSide.SERVER, category = "primordial_anchor", key = "primordial_anchor_gives_spin_grant_state")
    public static Boolean anchorGivesSpinGrantState = false;

    @Translation(comments = "The ender dragon has blessed you with the ability to fly.")
    private static final String PRIMORDIAL_ANCHOR_GRANTED_FLIGHT = Translation.Type.GUI.wrap("primordial_anchor.spin_grant_gained");

    @Translation(comments = "The ender dragon has blessed you with the ability to spin through the air.")
    private static final String PRIMORDIAL_ANCHOR_GRANTED_SPIN = Translation.Type.GUI.wrap("primordial_anchor.flight_grant_gained");

    @Translation(comments = "The ender dragon has blessed you with the ability to fly and spin through the air.")
    private static final String PRIMORDIAL_ANCHOR_GRANTED_FLIGHT_SPIN = Translation.Type.GUI.wrap("primordial_anchor.flight_spin_grant_gained");

    public static final BooleanProperty CHARGED = BooleanProperty.create("charged");
    public static final BooleanProperty BLOODY = BooleanProperty.create("bloody");

    public PrimordialAnchorBlock(final Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(CHARGED, false).setValue(BLOODY, false));
    }

    @Override
    public void animateTick(final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, @NotNull final RandomSource random) {
        if (!state.getValue(CHARGED)) {
            return;
        }

        if (random.nextInt(100) == 0) {
            level.playLocalSound(position, SoundEvents.RESPAWN_ANCHOR_AMBIENT, SoundSource.BLOCKS, 1, 1, false);
        }

        spawnParticles(position, level, ParticleTypes.REVERSE_PORTAL);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGED);
        builder.add(BLOODY);
    }

    @Override
    protected boolean hasAnalogOutputSignal(@NotNull final BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(final BlockState blockState, @NotNull final Level level, @NotNull final BlockPos position) {
        return blockState.getValue(CHARGED) ? 15 : 0;
    }

    @Override // TODO :: set state here when it is charged (to enable flight and / or wings)?
    protected @NotNull InteractionResult useWithoutItem(final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, @NotNull final Player player, @NotNull final BlockHitResult hitResult) {
        if (state.getValue(BLOODY)) {
            for (int i = 0; i < 10; i++) {
                spawnParticles(position, level, ParticleTypes.SOUL);
            }

            level.playSound(player, (double) position.getX() + 0.5, (double) position.getY() + 0.5, (double) position.getZ() + 0.5, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 4, 1);
            player.hurt(level.damageSources().magic(), 1);
            return InteractionResult.PASS;
        }

        if (!state.getValue(CHARGED) || level.dimension() != Level.END) {
            for (int i = 0; i < 10; i++) {
                spawnParticles(position, level, ParticleTypes.LARGE_SMOKE);
            }

            level.playSound(player, (double) position.getX() + 0.5, (double) position.getY() + 0.5, (double) position.getZ() + 0.5, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
            return InteractionResult.PASS;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            for (int i = 0; i < 10; i++) {
                spawnParticles(position, level, ParticleTypes.LARGE_SMOKE);
            }

            level.playSound(player, (double) position.getX() + 0.5, (double) position.getY() + 0.5, (double) position.getZ() + 0.5, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
            player.hurt(level.damageSources().magic(), 1);
            return InteractionResult.PASS;
        }

        expendCharge(player, level, position, state);

        if (level instanceof ServerLevel serverLevel) {
            BlockPos teleportPosition = findOrCreateValidTeleportPos(serverLevel, position).above(5);
            DimensionTransition transition = new DimensionTransition(serverLevel, teleportPosition.getCenter(), player.getDeltaMovement(), player.getYRot(), player.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND);
            player.changeDimension(transition);
            handler.markedByEnderDragon = false;
            boolean flightWasActuallyGranted = false;
            boolean spinWasActuallyGranted = false;
            if (anchorGivesFlightGrantState) {
                flightWasActuallyGranted = true;
                handler.flightWasGranted = true;
            }
            if (anchorGivesSpinGrantState) {
                spinWasActuallyGranted = true;
                handler.spinWasGranted = true;
            }

            if (flightWasActuallyGranted && spinWasActuallyGranted) {
                player.sendSystemMessage(Component.translatable(PRIMORDIAL_ANCHOR_GRANTED_FLIGHT_SPIN));
            } else if (flightWasActuallyGranted) {
                player.sendSystemMessage(Component.translatable(PRIMORDIAL_ANCHOR_GRANTED_FLIGHT));
            } else if (spinWasActuallyGranted) {
                player.sendSystemMessage(Component.translatable(PRIMORDIAL_ANCHOR_GRANTED_SPIN));
            }

            PacketDistributor.sendToPlayer((ServerPlayer) player, new SyncEnderDragonMark(false));
        }

        return InteractionResult.CONSUME;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull final ItemStack stack, @NotNull final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, @NotNull final Player player, @NotNull final InteractionHand hand, @NotNull final BlockHitResult hitResult) {
        if (state.getValue(CHARGED)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (stack.is(DSItemTags.PRIMORDIAL_ANCHOR_FUEL)) {
            charge(player, level, position, state);
            stack.consume(1, player);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected boolean isPathfindable(@NotNull final BlockState state, @NotNull final PathComputationType type) {
        return false;
    }

    /** See {@link TheEndGatewayBlockEntity#findTallestBlock(BlockGetter, BlockPos, int, boolean)} */
    @SuppressWarnings("SameParameterValue") // ignore
    private BlockPos findTallestBlock(final BlockGetter level, final BlockPos position, int radius, boolean allowBedrock) {
        BlockPos tallestPosition = null;

        for (int xOffset = -radius; xOffset <= radius; xOffset++) {
            for (int zOffset = -radius; zOffset <= radius; zOffset++) {
                if (xOffset != 0 || zOffset != 0 || allowBedrock) {
                    for (int y = level.getMaxBuildHeight() - 1; y > (tallestPosition == null ? level.getMinBuildHeight() : tallestPosition.getY()); y--) {
                        BlockPos positionToCheck = new BlockPos(position.getX() + xOffset, y, position.getZ() + zOffset);
                        BlockState state = level.getBlockState(positionToCheck);

                        if (state.isCollisionShapeFullBlock(level, positionToCheck) && (allowBedrock || !state.is(Blocks.BEDROCK))) {
                            tallestPosition = positionToCheck;
                            break;
                        }
                    }
                }
            }
        }

        return tallestPosition == null ? position : tallestPosition;
    }

    /** See {@link TheEndGatewayBlockEntity#findValidSpawnInChunk} */
    private @Nullable BlockPos findValidSpawnInChunk(final LevelChunk chunk) {
        ChunkPos chunkPosition = chunk.getPos();
        BlockPos position = new BlockPos(chunkPosition.getMinBlockX(), 30, chunkPosition.getMinBlockZ());
        //noinspection removal -> ignore
        int section = chunk.getHighestSectionPosition() + 16 - 1;
        BlockPos maxPosition = new BlockPos(chunkPosition.getMaxBlockX(), section, chunkPosition.getMaxBlockZ());

        BlockPos spawnPosition = null;
        double currentDistanceToCenter = 0;

        for (BlockPos positionToCheck : BlockPos.betweenClosed(position, maxPosition)) {
            BlockState state = chunk.getBlockState(positionToCheck);
            BlockPos above = positionToCheck.above();
            BlockPos twoAbove = positionToCheck.above(2);

            if (state.is(Blocks.END_STONE) && !chunk.getBlockState(above).isCollisionShapeFullBlock(chunk, above) && !chunk.getBlockState(twoAbove).isCollisionShapeFullBlock(chunk, twoAbove)) {
                double distance = positionToCheck.distToCenterSqr(Vec3.ZERO);

                if (spawnPosition == null || distance < currentDistanceToCenter) {
                    spawnPosition = positionToCheck;
                    currentDistanceToCenter = distance;
                }
            }
        }

        return spawnPosition;
    }

    private LevelChunk getChunk(final Level level, final Vec3 position) {
        return level.getChunk(Mth.floor(position.x / 16.0), Mth.floor(position.z / 16.0));
    }

    private boolean isChunkEmpty(final Level level, final Vec3 position) {
        return getChunk(level, position).getHighestFilledSectionIndex() == -1;
    }

    /** See {@link TheEndGatewayBlockEntity#findExitPortalXZPosTentative(ServerLevel, BlockPos)} */
    private Vec3 findExitPortalXZPosTentative(final Level level, final BlockPos startPosition) {
        Vec3 horizontal = new Vec3(startPosition.getX(), 0, startPosition.getZ()).normalize();
        Vec3 position = horizontal.scale(1024);

        for (int j = 16; !isChunkEmpty(level, position) && j-- > 0; position = position.add(horizontal.scale(-16.0))) {
            DragonSurvival.LOGGER.debug("Skipping backwards past nonempty chunk at {}", position);
        }

        for (int k = 16; isChunkEmpty(level, position) && k-- > 0; position = position.add(horizontal.scale(16.0))) {
            DragonSurvival.LOGGER.debug("Skipping forward past empty chunk at {}", position);
        }

        DragonSurvival.LOGGER.debug("Found chunk at {}", position);
        return position;
    }

    /** See {@link TheEndGatewayBlockEntity#findOrCreateValidTeleportPos(ServerLevel, BlockPos)} */
    private BlockPos findOrCreateValidTeleportPos(final ServerLevel level, final BlockPos position) {
        Vec3 exitPortalPosition = findExitPortalXZPosTentative(level, position);
        LevelChunk chunk = getChunk(level, exitPortalPosition);
        BlockPos spawnPosition = findValidSpawnInChunk(chunk);

        if (spawnPosition == null) {
            BlockPos islandPosition = BlockPos.containing(exitPortalPosition.x + 0.5, 75.0, exitPortalPosition.z + 0.5);
            DragonSurvival.LOGGER.debug("Failed to find a suitable block to teleport to, spawning an island on {}", islandPosition);

            level.registryAccess()
                    .registry(Registries.CONFIGURED_FEATURE)
                    .flatMap(configuredFeatureRegistry -> configuredFeatureRegistry.getHolder(EndFeatures.END_ISLAND))
                    .ifPresent(configuredFeatureHolder -> configuredFeatureHolder.value()
                            .place(level, level.getChunkSource().getGenerator(), RandomSource.create(islandPosition.asLong()), islandPosition)
                    );

            spawnPosition = islandPosition;
        } else {
            DragonSurvival.LOGGER.debug("Found suitable block to teleport to: {}", spawnPosition);
        }

        return findTallestBlock(level, spawnPosition, 16, true);
    }

    private void charge(@Nullable final Player player, final Level level, final BlockPos position, final BlockState state) {
        BlockState blockstate = state.setValue(CHARGED, true);
        level.setBlock(position, blockstate, 3);
        level.gameEvent(GameEvent.BLOCK_CHANGE, position, GameEvent.Context.of(player, blockstate));
        level.playSound(player, (double) position.getX() + 0.5, (double) position.getY() + 0.5, (double) position.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 1, 1);
    }

    private void expendCharge(@Nullable final Player player, final Level level, final BlockPos position, final BlockState state) {
        BlockState blockstate = state.setValue(CHARGED, false);
        level.setBlock(position, blockstate, 3);
        level.gameEvent(GameEvent.BLOCK_CHANGE, position, GameEvent.Context.of(player, blockstate));
        level.playSound(player, (double) position.getX() + 0.5, (double) position.getY() + 0.5, (double) position.getZ() + 0.5, SoundEvents.RESPAWN_ANCHOR_DEPLETE, SoundSource.BLOCKS, 1, 1);
    }

    private void spawnParticles(final BlockPos position, final Level level, final SimpleParticleType particle) {
        double x = (double) position.getX() + 0.5 + (0.5 - level.getRandom().nextDouble());
        double y = (double) position.getY() + 1.0;
        double z = (double) position.getZ() + 0.5 + (0.5 - level.getRandom().nextDouble());
        double speed = (double) level.getRandom().nextFloat() * 0.04;
        level.addParticle(particle, x, y, z, 0, speed, 0);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos position, @NotNull final BlockState state) {
        return DSBlockEntities.PRIMORDIAL_ANCHOR.get().create(position, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(final Level level, @NotNull final BlockState state, @NotNull final BlockEntityType<T> type) {
        return level.isClientSide ? null : BaseEntityBlock.createTickerHelper(type, DSBlockEntities.PRIMORDIAL_ANCHOR.get(), PrimordialAnchorBlockEntity::serverTick);
    }
}
