package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber
public class EntityStateHandler implements INBTSerializable<CompoundTag> {
    @Translation(key = "pillage_cooldown", type = Translation.Type.CONFIGURATION, comments = "Cooldown in ticks (20 ticks = 1 second) before the entity can be pillaged again")
    @ConfigOption(side = ConfigSide.SERVER, category = "misc", key = "pillage_cooldown")
    public static int PILLAGE_COOLDOWN = Functions.secondsToTicks(300);

    @Translation(comments = "You have to wait %s seconds until you can steal from this villager")
    public static final String PILLAGE_ON_COOLDOWN = Translation.Type.GUI.wrap("message.pillage_on_cooldown");

    @Translation(comments = "This villager had no items you could steal")
    public static final String PILLAGE_UNSUCCESSFUL = Translation.Type.GUI.wrap("message.pillage_unsuccessful");

    @Translation(comments = "This villager has nothing you can steal")
    public static final String CANNOT_PILLAGE = Translation.Type.GUI.wrap("message.cannot_pillage");

    // To handle the burn effect damage
    public Vec3 lastPos;
    // Amount of times the last chain attack has chained
    public int chainCount;

    public int pillageCooldown;

    public static boolean canPillage(final Entity target, final Player player) {
        if (!player.hasEffect(DSEffects.HUNTER_OMEN)) {
            return false;
        }

        EntityStateHandler handler = target.getExistingData(DSDataAttachments.ENTITY_HANDLER).orElse(null);

        if (handler == null) {
            // No data = no cooldown was set so far
            return true;
        }

        return handler.pillageCooldown == 0;
    }

    public void setPillageCooldown() {
        pillageCooldown = PILLAGE_COOLDOWN;
    }

    /** If no player is specified it will be sent to all tracking players */
    public void sync(final Entity holder, @Nullable final Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncData(holder.getId(), DSDataAttachments.ENTITY_HANDLER.getId(), serializeNBT(holder.registryAccess())));
        } else if (player == null) {
            PacketDistributor.sendToPlayersTrackingEntity(holder, new SyncData(holder.getId(), DSDataAttachments.ENTITY_HANDLER.getId(), serializeNBT(holder.registryAccess())));
        }
    }

    @SubscribeEvent
    public static void onTrackingStart(final PlayerEvent.StartTracking event) {
        EntityStateHandler handler = event.getTarget().getExistingData(DSDataAttachments.ENTITY_HANDLER).orElse(null);

        if (handler == null || handler.pillageCooldown == 0) {
            return;
        }

        handler.sync(event.getTarget(), event.getEntity());
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.putInt(CHAIN_COUNT, chainCount);
        tag.putInt(PILLAGE_COOLDOWN_KEY, pillageCooldown);

        if (lastPos != null) {
            tag.put(LAST_POSITION, Functions.newDoubleList(lastPos.x, lastPos.y, lastPos.z));
        }

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        chainCount = tag.getInt(CHAIN_COUNT);
        pillageCooldown = tag.getInt(PILLAGE_COOLDOWN_KEY);

        if (tag.contains(LAST_POSITION)) {
            ListTag list = tag.getList(LAST_POSITION, ListTag.TAG_DOUBLE);
            lastPos = new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2));
        }
    }

    private static final String LAST_POSITION = "last_position";
    private static final String CHAIN_COUNT = "chain_count";
    private static final String PILLAGE_COOLDOWN_KEY = "pillage_cooldown";
}
