package by.dragonsurvivalteam.dragonsurvival.common.capability;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.ATTACHMENT_TYPES;

import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import java.util.function.Supplier;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

public class EntityStateHandler implements INBTSerializable<CompoundTag> {
    public static final Supplier<AttachmentType<EntityStateHandler>> ENTITY_HANDLER = ATTACHMENT_TYPES.register(
            "entity_handler",
            () -> AttachmentType.serializable(EntityStateHandler::new).copyOnDeath().build()
    );

    // Last entity this entity recieved a debuff from
    public int lastAfflicted = -1;

    // Amount of times the last chain attack has chained
    public int chainCount;

    public Vec3 lastPos;

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("lastAfflicted", lastAfflicted);
        tag.putInt("chainCount", chainCount);

        if (lastPos != null) {
            tag.put("lastPos", Functions.newDoubleList(lastPos.x, lastPos.y, lastPos.z));
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        lastAfflicted = tag.getInt("lastAfflicted");
        chainCount = tag.getInt("chainCount");

        if (tag.contains("lastPos")) {
            ListTag listnbt = tag.getList("lastPos", ListTag.TAG_DOUBLE);
            lastPos = new Vec3(listnbt.getDouble(0), listnbt.getDouble(1), listnbt.getDouble(2));
        }
    }
}
