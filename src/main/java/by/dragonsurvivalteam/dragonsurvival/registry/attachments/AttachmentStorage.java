package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import net.minecraftforge.attachment.AttachmentType;

import java.util.Map;

public interface AttachmentStorage {
    Map<AttachmentType<?>, Object> dragonSurvival$getAttachments();
}
