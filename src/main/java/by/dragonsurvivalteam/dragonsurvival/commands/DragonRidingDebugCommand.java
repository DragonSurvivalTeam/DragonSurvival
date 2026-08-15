package by.dragonsurvivalteam.dragonsurvival.commands;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonBodyArgument;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonSpeciesArgument;
import by.dragonsurvivalteam.dragonsurvival.commands.arguments.DragonStageArgument;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.mixins.EntityAccessor;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber
public class DragonRidingDebugCommand {
    private static final String COMMAND = "dragon-riding-debug";
    private static final int REMOVAL_DELAY_TICKS = 30;

    private static final Map<UUID, FakePlayer> ACTIVE_RIDERS = new HashMap<>();
    private static final Map<UUID, PendingRemoval> PENDING_REMOVALS = new HashMap<>();

    public static void register(final RegisterCommandsEvent event) {
        LiteralCommandNode<CommandSourceStack> command = Commands.literal(COMMAND)
                .requires(source -> source.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .build();
        LiteralCommandNode<CommandSourceStack> start = Commands.literal("start")
                .executes(context -> start(context.getSource(), null, null, null))
                .build();
        LiteralCommandNode<CommandSourceStack> stop = Commands.literal("stop")
                .executes(context -> stop(context.getSource()))
                .build();

        ArgumentCommandNode<CommandSourceStack, Holder<DragonSpecies>> species = Commands.argument(DragonSpeciesArgument.ID, new DragonSpeciesArgument(event.getBuildContext()))
                .executes(context -> start(context.getSource(), DragonSpeciesArgument.get(context), null, null))
                .build();
        ArgumentCommandNode<CommandSourceStack, Holder<DragonBody>> body = Commands.argument(DragonBodyArgument.ID, new DragonBodyArgument(event.getBuildContext()))
                .executes(context -> start(context.getSource(), DragonSpeciesArgument.get(context), DragonBodyArgument.get(context), null))
                .build();
        ArgumentCommandNode<CommandSourceStack, Holder<DragonStage>> stage = Commands.argument(DragonStageArgument.ID, new DragonStageArgument(event.getBuildContext()))
                .executes(context -> start(context.getSource(), DragonSpeciesArgument.get(context), DragonBodyArgument.get(context), DragonStageArgument.get(context)))
                .build();

        event.getDispatcher().getRoot().addChild(command);
        command.addChild(start);
        command.addChild(stop);
        start.addChild(species);
        species.addChild(body);
        body.addChild(stage);
    }

    private static int start(final CommandSourceStack source, @Nullable final Holder<DragonSpecies> species, @Nullable final Holder<DragonBody> body, @Nullable final Holder<DragonStage> stage) throws CommandSyntaxException {
        ServerPlayer owner = source.getPlayerOrException();
        removeExisting(owner.getUUID());

        if (owner.isVehicle()) {
            source.sendFailure(Component.literal("Cannot start the riding debug while another entity is riding you."));
            return 0;
        }

        ServerLevel level = owner.serverLevel();
        GameProfile profile = new GameProfile(createProfileId(owner.getUUID()), createProfileName(owner));
        FakePlayer rider = new FakePlayer(level, profile);
        rider.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());
        configureDragon(rider, species, body, stage);

        forceMount(rider, owner);
        broadcastPlayerInfo(level.getServer(), rider);

        if (!level.addFreshEntity(rider)) {
            forceDismount(rider);
            removePlayerInfo(level.getServer(), rider);
            source.sendFailure(Component.literal("Failed to add the riding debug player to the level."));
            return 0;
        }

        ACTIVE_RIDERS.put(owner.getUUID(), rider);
        level.getChunkSource().broadcastAndSend(owner, new ClientboundSetPassengersPacket(owner));

        DragonStateHandler handler = DragonStateProvider.getData(rider);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(rider, new SyncComplete(rider.getId(), handler.serializeNBT(rider.level().registryAccess())));

        String form = handler.isDragon() ? "dragon" : "human";
        source.sendSuccess(() -> Component.literal("Spawned a " + form + " riding debug player."), false);
        return 1;
    }

    private static int stop(final CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer owner = source.getPlayerOrException();
        UUID ownerId = owner.getUUID();
        FakePlayer rider = ACTIVE_RIDERS.remove(ownerId);

        if (rider == null) {
            if (PENDING_REMOVALS.containsKey(ownerId)) {
                source.sendFailure(Component.literal("The riding debug player is already waiting to be removed."));
            } else {
                source.sendFailure(Component.literal("You do not have an active riding debug player."));
            }
            return 0;
        }

        forceDismount(rider);
        PENDING_REMOVALS.put(ownerId, new PendingRemoval(rider, owner.getServer().getTickCount() + REMOVAL_DELAY_TICKS));
        source.sendSuccess(() -> Component.literal("Dismounted the riding debug player; it will be removed in 1.5 seconds."), false);
        return 1;
    }

    private static void configureDragon(final FakePlayer rider, @Nullable final Holder<DragonSpecies> species, @Nullable final Holder<DragonBody> body, @Nullable final Holder<DragonStage> stage) {
        if (species == null || species.value() == DragonSpeciesArgument.EMPTY) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(rider);
        handler.setSpecies(rider, species);
        handler.setBody(rider, body == null ? DragonBody.getRandom(rider.level().registryAccess(), species) : body);

        if (stage == null) {
            handler.setGrowth(rider, species.value().getStartingGrowth(rider.level().registryAccess()));
        } else {
            handler.setStage(rider, stage);
        }

        handler.isGrowing = true;
        SyncComplete.handleDragonSync(rider, false);
    }

    private static void forceMount(final FakePlayer rider, final ServerPlayer owner) {
        // NeoForge fake players reject startRiding by design, so the debug rider must attach directly.
        ((EntityAccessor) rider).dragonSurvival$setVehicle(owner);
        ((EntityAccessor) owner).dragonSurvival$addPassenger(rider);
    }

    private static void forceDismount(final FakePlayer rider) {
        Entity vehicle = rider.getVehicle();
        if (vehicle == null) {
            return;
        }

        ((EntityAccessor) rider).dragonSurvival$setVehicle(null);
        ((EntityAccessor) vehicle).dragonSurvival$removePassenger(rider);

        if (vehicle.level() instanceof ServerLevel level) {
            level.getChunkSource().broadcastAndSend(vehicle, new ClientboundSetPassengersPacket(vehicle));
        }
    }

    private static void removeExisting(final UUID ownerId) {
        FakePlayer active = ACTIVE_RIDERS.remove(ownerId);
        PendingRemoval pending = PENDING_REMOVALS.remove(ownerId);

        if (active != null) {
            removeNow(active);
        }
        if (pending != null && pending.rider() != active) {
            removeNow(pending.rider());
        }
    }

    private static void removeNow(final FakePlayer rider) {
        forceDismount(rider);
        if (!rider.isRemoved()) {
            rider.discard();
        }
        removePlayerInfo(rider.serverLevel().getServer(), rider);
    }

    private static UUID createProfileId(final UUID ownerId) {
        String key = DragonSurvival.MODID + ":riding_debug:" + ownerId;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    private static String createProfileName(final ServerPlayer owner) {
        String name = "DSRider_" + owner.getGameProfile().getName();
        return name.substring(0, Math.min(name.length(), 16));
    }

    private static void broadcastPlayerInfo(final MinecraftServer server, final FakePlayer rider) {
        server.getPlayerList().broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(rider)));
    }

    private static void removePlayerInfo(final MinecraftServer server, final FakePlayer rider) {
        server.getPlayerList().broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(rider.getUUID())));
    }

    @SubscribeEvent
    public static void removeExpiredRiders(final TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        int currentTick = event.getServer().getTickCount();
        Iterator<Map.Entry<UUID, PendingRemoval>> iterator = PENDING_REMOVALS.entrySet().iterator();

        while (iterator.hasNext()) {
            PendingRemoval pending = iterator.next().getValue();
            if (pending.rider().isRemoved() || currentTick >= pending.removeAtTick()) {
                iterator.remove();
                removeNow(pending.rider());
            }
        }

        Iterator<Map.Entry<UUID, FakePlayer>> activeIterator = ACTIVE_RIDERS.entrySet().iterator();
        while (activeIterator.hasNext()) {
            FakePlayer rider = activeIterator.next().getValue();
            if (rider.isRemoved()) {
                activeIterator.remove();
                removeNow(rider);
            }
        }
    }

    @SubscribeEvent
    public static void sendRiderInfoOnLogin(final PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player instanceof FakePlayer) {
            return;
        }

        List<ServerPlayer> riders = new ArrayList<>();
        ACTIVE_RIDERS.values().stream().filter(rider -> !rider.isRemoved()).forEach(riders::add);
        PENDING_REMOVALS.values().stream().map(PendingRemoval::rider).filter(rider -> !rider.isRemoved()).forEach(riders::add);

        if (!riders.isEmpty()) {
            player.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(riders));
        }
    }

    @SubscribeEvent
    public static void cleanupOnLogout(final PlayerEvent.PlayerLoggedOutEvent event) {
        cleanupOwner(event.getEntity());
    }

    @SubscribeEvent
    public static void cleanupOnRespawn(final PlayerEvent.PlayerRespawnEvent event) {
        cleanupOwner(event.getEntity());
    }

    @SubscribeEvent
    public static void cleanupOnDimensionChange(final PlayerEvent.PlayerChangedDimensionEvent event) {
        cleanupOwner(event.getEntity());
    }

    private static void cleanupOwner(final Entity entity) {
        if (entity instanceof ServerPlayer && !(entity instanceof FakePlayer)) {
            removeExisting(entity.getUUID());
        }
    }

    @SubscribeEvent
    public static void clearState(final ServerStoppedEvent event) {
        ACTIVE_RIDERS.clear();
        PENDING_REMOVALS.clear();
    }

    private record PendingRemoval(FakePlayer rider, int removeAtTick) {
    }
}
