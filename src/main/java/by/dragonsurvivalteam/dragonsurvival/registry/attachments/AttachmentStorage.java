package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.compat.attachments.AttachmentType;

import java.util.Map;

public interface AttachmentStorage {
    Map<AttachmentType<?>, Object> dragonSurvival$getAttachments();
}
