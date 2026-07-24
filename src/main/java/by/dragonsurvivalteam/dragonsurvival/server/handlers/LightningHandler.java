package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

@EventBusSubscriber
public class LightningHandler implements INBTSerializable<CompoundTag> {
    public boolean ignoresItemsAndExperience;
    public boolean spawnsFire;
    public boolean canHurtSelf;

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("ignores_items_and_experience", ignoresItemsAndExperience);
        tag.putBoolean("spawns_fire", spawnsFire);
        tag.putBoolean("can_hurt_self", canHurtSelf);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        ignoresItemsAndExperience = nbt.getBoolean("ignores_items_and_experience");
        spawnsFire = nbt.getBoolean("spawns_fire");
        canHurtSelf = nbt.getBoolean("can_hurt_self");
    }

    public record Data(boolean ignoresItemsAndExperience, boolean spawnsFire, boolean canHurtSelf) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("ignores_items_and_experience").forGetter(Data::ignoresItemsAndExperience),
                Codec.BOOL.fieldOf("spawns_fire").forGetter(Data::spawnsFire),
                Codec.BOOL.fieldOf("can_hurt_self").forGetter(Data::canHurtSelf)
        ).apply(instance, Data::new));
    }

    public static LightningHandler fromData(Data data) {
        LightningHandler handler = new LightningHandler();
        handler.ignoresItemsAndExperience = data.ignoresItemsAndExperience;
        handler.spawnsFire = data.spawnsFire;
        handler.canHurtSelf = data.canHurtSelf;
        return handler;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void handleLightningBolt(final EntityStruckByLightningEvent event) {
        LightningBolt bolt = event.getLightning();
        Entity target = event.getEntity();
        LightningHandler handler = bolt.getData(DSDataAttachments.LIGHTNING_BOLT);

        if (handler.ignoresItemsAndExperience) {
            if (target instanceof ItemEntity || target instanceof ExperienceOrb) {
                event.setCanceled(true);
            }
        }

        if (!handler.canHurtSelf) {
            if (target == bolt.getCause()) {
                event.setCanceled(true);
            }
        }
    }
}
