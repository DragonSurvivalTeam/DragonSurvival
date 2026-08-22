package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.Climbable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.WorldGenLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber
public class ClimbableData extends Storage<Climbable.Instance> {
    /** Temporarily kept to check whether 'canStickToWall' is allowed */
    public @Nullable BlockPos climbPosition;

    public boolean canClimb(final WorldGenLevel level, final BlockPos position) {
        if (storage == null) {
            return false;
        }

        for (final Climbable.Instance modification : storage.values()) {
            if (modification.canClimb(level, position)) {
                return true;
            }
        }

        return false;
    }

    public boolean canStickToWall(final WorldGenLevel level) {
        if (storage == null || climbPosition == null) {
            return false;
        }

        for (final Climbable.Instance modification : storage.values()) {
            if (modification.canStickToWall(level, climbPosition)) {
                return true;
            }
        }

        return false;
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        event.getEntity().getExistingData(DSDataAttachments.CLIMBABLE_DATA).ifPresent(data -> {
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
