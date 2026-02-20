package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSProfessionTags;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@EventBusSubscriber
public class EntityStateHandler implements ValueIOSerializable {
    @Translation(comments = "You have to wait %s seconds until you can steal from this villager")
    public static final String PILLAGE_ON_COOLDOWN = Translation.Type.GUI.wrap("message.pillage_on_cooldown");

    @Translation(comments = "This villager had no items you could steal")
    public static final String PILLAGE_UNSUCCESSFUL = Translation.Type.GUI.wrap("message.pillage_unsuccessful");

    @Translation(comments = "You cannot steal from this villager")
    public static final String CANNOT_PILLAGE = Translation.Type.GUI.wrap("message.cannot_pillage");

    // To handle the burn effect damage
    public Vec3 lastPos;
    // Amount of times the last chain attack has chained
    public int chainCount;

    public int pillageCooldown;

    public static boolean cannotPillageProfession(final Villager villager) {
        Holder<VillagerProfession> profession = villager.getVillagerData().profession();
        Identifier key = profession.getKey().identifier();
        Holder.Reference<VillagerProfession> holder = BuiltInRegistries.VILLAGER_PROFESSION.getOrThrow(ResourceKey.create(BuiltInRegistries.VILLAGER_PROFESSION.key(), key));
        return BuiltInRegistries.VILLAGER_PROFESSION.get(DSProfessionTags.PILLAGE_BLACKLIST).orElseThrow().contains(holder);
    }

    public static boolean canPillage(final Entity target, final Player player) {
        if (!player.hasEffect(DSEffects.HUNTER_OMEN)) {
            return false;
        }

        if (target instanceof Villager villager && cannotPillageProfession(villager)) {
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
        pillageCooldown = ServerConfig.PILLAGE_COOLDOWN;
    }

    /** If no player is specified it will be sent to all tracking players */
    public void sync(final Entity holder, @Nullable final Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, holder.registryAccess());
            serialize(valueOutput);
            PacketDistributor.sendToPlayer(serverPlayer, new SyncData(holder.getId(), DSDataAttachments.ENTITY_HANDLER.getId(), valueOutput.buildResult()));
        } else if (player == null) {
            TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, holder.registryAccess());
            serialize(valueOutput);
            PacketDistributor.sendToPlayersTrackingEntity(holder, new SyncData(holder.getId(), DSDataAttachments.ENTITY_HANDLER.getId(), valueOutput.buildResult()));
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
    public void serialize(@NotNull final ValueOutput valueOutput) {
        valueOutput.putInt(CHAIN_COUNT, chainCount);
        valueOutput.putInt(PILLAGE_COOLDOWN_KEY, pillageCooldown);

        if (lastPos != null) {
            valueOutput.putDouble(LAST_POSITION_X, lastPos.x);
            valueOutput.putDouble(LAST_POSITION_Y, lastPos.y);
            valueOutput.putDouble(LAST_POSITION_Z, lastPos.z);
        }
    }

    @Override
    public void deserialize(final ValueInput valueInput) {
        chainCount = valueInput.getInt(CHAIN_COUNT).orElseThrow();
        pillageCooldown = valueInput.getInt(PILLAGE_COOLDOWN_KEY).orElseThrow();

        Vec3 readLastPos = new Vec3(valueInput.getDoubleOr(LAST_POSITION_X, 0.0), valueInput.getDoubleOr(LAST_POSITION_Y, 0.0), valueInput.getDoubleOr(LAST_POSITION_Z, 0.0));
        if (!readLastPos.equals(Vec3.ZERO)) {
            lastPos = readLastPos;
        }
    }

    private static final String LAST_POSITION_X = "last_position_x";
    private static final String LAST_POSITION_Y = "last_position_y";
    private static final String LAST_POSITION_Z = "last_position_z";
    private static final String CHAIN_COUNT = "chain_count";
    private static final String PILLAGE_COOLDOWN_KEY = "pillage_cooldown";
}
