package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Climbable;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncClimbFlag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;

@EventBusSubscriber
public class ClimbableData extends Storage<Climbable.Instance> {
    // The core problem as to why this whole client / server setup is needed:
    // - The client calculates and stores the horizontal collision
    // - On the client-side the "blocking sliding down on ladders" part is handled
    // - Block predicates can only be evaluated on the server-side (i.e., is climbing allowed on that position)
    // Meaning the client needs to collect the relevant positions and the server has to approve them

    /** Temporarily kept to handle 'canStickToWalls' and ceiling climbing */
    public @Nullable BlockPos climbPosition;

    /** Retains whether the current {@link #climbPosition} was set from ceiling climbing */
    public boolean isCeilingClimbing;

    /**
     * Last set of (unfiltered in regard to climbable) positions collected by the client and sent to the server </br>
     * On the server-side they may be updated through the 'LevelMixin' (causing a refresh to be sent to the client)
     */
    public @Nullable @Unmodifiable Collection<BlockPos> trackedClimbPositions;

    /**
     * Client-only: positions the server has confirmed as climbable </br>
     * Used to actually check (on the client-side) whether climbing is allowed
     */
    private @Nullable @Unmodifiable Collection<BlockPos> approvedClimbPositions;

    /**
     * Purely for other players to know what the other client is doing </br>
     * On the server-side it is used to check whether a sync is required (i.e., type changed)
     */
    private SyncClimbFlag.ClimbingType climbingType = SyncClimbFlag.ClimbingType.NONE;

    public boolean isApprovedClimbPosition(final BlockPos position) {
        return approvedClimbPositions != null && approvedClimbPositions.contains(position);
    }

    public boolean canStillClimb(final LivingEntity entity) {
        if (climbPosition == null) {
            return false;
        }

        if (entity.level() instanceof WorldGenLevel level) {
            return canClimb(level, climbPosition, entity);
        }

        return isApprovedClimbPosition(climbPosition);
    }

    public void setApprovedClimbPositions(@Unmodifiable final Collection<BlockPos> positions) {
        if (positions.isEmpty()) {
            approvedClimbPositions = null;
        } else {
            approvedClimbPositions = positions;
        }
    }

    public void setTrackedClimbPositions(@Unmodifiable final Collection<BlockPos> positions) {
        if (positions.isEmpty()) {
            trackedClimbPositions = null;
        } else {
            trackedClimbPositions = positions;
        }
    }

    public boolean canClimb(final WorldGenLevel level, final BlockPos position, final LivingEntity entity) {
        if (storage == null) {
            return false;
        }

        boolean isCeiling = position.getY() > entity.getBlockY();

        for (final Climbable.Instance instance : storage.values()) {
            if (isCeiling && !instance.canClimbCeilings()) {
                continue;
            }

            if (instance.canClimb(level, position)) {
                return true;
            }
        }

        return false;
    }

    public SyncClimbFlag.ClimbingType getClimbingType() {
        return climbingType;
    }

    public void setClimbingType(final SyncClimbFlag.ClimbingType climbingType) {
        this.climbingType = climbingType;
        isCeilingClimbing = climbingType == SyncClimbFlag.ClimbingType.CEILING;
    }

    public boolean isCeilingClimbing() {
        return climbPosition != null && isCeilingClimbing;
    }

    public boolean canClimbCeilings() {
        if (storage == null) {
            return false;
        }

        for (final Climbable.Instance instance : storage.values()) {
            if (instance.canClimbCeilings()) {
                return true;
            }
        }

        return false;
    }

    public boolean canStickToWalls(final WorldGenLevel level) {
        if (storage == null || climbPosition == null) {
            return false;
        }

        boolean isCeilingCandidate = isCeilingClimbing;

        for (final Climbable.Instance instance : storage.values()) {
            if (isCeilingCandidate && !instance.canClimbCeilings()) {
                continue;
            }

            if (instance.canStickToWalls(level, climbPosition)) {
                return true;
            }
        }

        return false;
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof LivingEntity livingEntity)) {
            return;
        }

        livingEntity.getExistingData(DSDataAttachments.CLIMBABLE_DATA).ifPresent(data -> {
            // TODO :: remove? might already be properly handled with climbing handler, making this just unneeded queries
            if (!data.canStillClimb(livingEntity)) {
                boolean wasCeilingClimbing = data.isCeilingClimbing();
                data.climbPosition = null;
                data.isCeilingClimbing = false;

                if (wasCeilingClimbing) {
                    // The hit box depends on whether the entity is ceiling climbing
                    livingEntity.refreshDimensions();
                }

                if (event.getEntity() instanceof LivingEntity entity && !entity.level().isClientSide()) {
                    PacketDistributor.sendToPlayersTrackingEntity(entity, new SyncClimbFlag(entity.getId(), SyncClimbFlag.ClimbingType.NONE));
                }
            }

            if (livingEntity.level().isClientSide()) {
                return;
            }

            data.tick(livingEntity);

            if (data.isEmpty()) {
                livingEntity.removeData(DSDataAttachments.CLIMBABLE_DATA);
            }
        });
    }

    @Override
    protected void save(@NotNull final ValueOutput valueOutput, final Climbable.Instance entry, final String key) {
        entry.save(valueOutput, key);
    }

    @Override
    protected Climbable.Instance load(@NotNull final ValueInput valueInput, final String key) {
        return Climbable.Instance.load(valueInput, key);
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.CLIMBABLE_DATA.get();
    }
}
