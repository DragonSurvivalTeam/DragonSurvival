package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.BlockVision;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;


@EventBusSubscriber
public class BlockVisionData extends Storage<BlockVision.Instance> {

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            player.getExistingData(DSDataAttachments.OXYGEN_BONUSES).ifPresent(storage -> {
                storage.tick(event.getEntity());

                if (storage.isEmpty()) {
                    player.removeData(DSDataAttachments.OXYGEN_BONUSES);
                }
            });
        }
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.BLOCK_VISION.get();
    }

    @Override
    protected Tag save(HolderLookup.@NotNull Provider provider, BlockVision.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected BlockVision.Instance load(HolderLookup.@NotNull Provider provider, CompoundTag tag) {
        return BlockVision.Instance.load(provider, tag);
    }
}
