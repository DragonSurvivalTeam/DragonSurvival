package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Climbable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.WorldGenLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
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
     * Client-only: positions the server has confirmed as climbable </br>
     * Used to actually check (on the client-side) whether climbing is allowed
     */
    private @Nullable @Unmodifiable Collection<BlockPos> approvedClimbPositions;

    /**
     * Last set of (unfiltered in regard to climbable) positions collected by the client and sent to the server </br>
     * On the server-side they may be updated through the 'LevelMixin' (causing a refresh to be sent to the client)
     */
    public @Nullable @Unmodifiable Collection<BlockPos> trackedClimbPositions;

    public boolean isApprovedClimbPosition(final BlockPos position) {
        return approvedClimbPositions != null && approvedClimbPositions.contains(position);
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

    public boolean canClimb(final WorldGenLevel level, final BlockPos position) {
        if (storage == null) {
            return false;
        }

        for (final Climbable.Instance instance : storage.values()) {
            if (instance.canClimb(level, position)) {
                return true;
            }
        }

        return false;
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

    public boolean canStickToWalls(final WorldGenLevel level, final LivingEntity entity) {
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
        event.getEntity().getExistingData(DSDataAttachments.CLIMBABLE_DATA).ifPresent(data -> {
            if (!data.isApprovedClimbPosition(data.climbPosition)) {
                data.climbPosition = null;
                data.isCeilingClimbing = false;
            }

            if (event.getEntity().level().isClientSide()) {
                return;
            }

            data.tick(event.getEntity());

            if (data.isEmpty()) {
                event.getEntity().removeData(DSDataAttachments.CLIMBABLE_DATA);
            }
        });
    }

    @Override
    protected Tag save(@NotNull final HolderLookup.Provider provider, final Climbable.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected Climbable.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        return Climbable.Instance.load(provider, tag);
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.CLIMBABLE_DATA.get();
    }
}
