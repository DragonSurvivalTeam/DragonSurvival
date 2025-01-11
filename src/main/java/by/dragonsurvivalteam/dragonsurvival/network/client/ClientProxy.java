package by.dragonsurvivalteam.dragonsurvival.network.client;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAltarScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SpinStatus;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncDeltaMovement;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncWingsSpread;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicCap;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncVisualEffectAdded;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncVisualEffectRemoved;
import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncParticleTrail;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDragonMovement;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDragonPassengerID;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncGrowthState;
import by.dragonsurvivalteam.dragonsurvival.network.status.RefreshDragon;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncPlayerJump;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/** To avoid loading client classes on the server side */
public class ClientProxy {
    public static void handleSyncDragonClawRender(final SyncDragonClawRender.Data message) {
        Player player = Minecraft.getInstance().player;

        if (player != null) {
            Entity entity = player.level().getEntity(message.playerId());

            if (entity instanceof Player) {
                ClawInventoryData.getData(player).shouldRenderClaws = message.state();
            }
        }
    }

    public static void handleSyncDragonClawsMenu(final SyncDragonClawsMenu.Data message, HolderLookup.Provider provider) {
        Player player = Minecraft.getInstance().player;

        if (player != null) {
            Entity entity = player.level().getEntity(message.playerId());

            if (entity instanceof Player) {
                ClawInventoryData data = ClawInventoryData.getData(player);
                data.setMenuOpen(message.state());
                data.deserializeNBT(provider, message.clawInventory());
            }
        }
    }

    public static void sendClientData() {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer == null) {
            // Safety check
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(localPlayer);

        if (!data.isDragon()) {
            // Safety check
            return;
        }

        PacketDistributor.sendToServer(new SyncDragonClawRender.Data(localPlayer.getId(), ClientDragonRenderer.renderDragonClaws));
        PacketDistributor.sendToServer(new SyncDragonSkinSettings(localPlayer.getId(), ClientDragonRenderer.renderCustomSkin));
        PacketDistributor.sendToServer(new SyncPlayerSkinPreset(localPlayer.getId(), data.speciesKey(), data.getCurrentSkinPreset().serializeNBT(localPlayer.registryAccess())));
    }

    // For replying during the configuration stage
    public static void sendClientData(final IPayloadContext context) {
        Player sender = context.player();

        context.reply(new SyncDragonClawRender.Data(sender.getId(), ClientDragonRenderer.renderDragonClaws));
        context.reply(new SyncDragonSkinSettings(sender.getId(), ClientDragonRenderer.renderCustomSkin));
    }

    public static void handleSyncPlayerSkinPreset(final SyncPlayerSkinPreset message, HolderLookup.Provider provider) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getOptional(player).ifPresent(handler -> {
                    SkinPreset preset = new SkinPreset();
                    preset.deserializeNBT(provider, message.preset());
                    handler.setSkinPresetForType(message.dragonSpecies(), preset);

                    if (handler.isDragon()) {
                        handler.recompileCurrentSkin();
                    }
                });
            }
        }
    }

    public static void handleSyncDeltaMovement(final SyncDeltaMovement.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            // Local player already has the correct values of themselves
            if (entity instanceof Player player && player != localPlayer) {
                player.setDeltaMovement(message.speedX(), message.speedY(), message.speedZ());
            }
        }
    }

    public static void handleOpenDragonAltar() {
        Minecraft.getInstance().setScreen(new DragonAltarScreen());
    }

    public static void handleOpenDragonEditorPacket() {
        Minecraft.getInstance().setScreen(new DragonEditorScreen(Minecraft.getInstance().screen));
    }

    public static void handleSyncWingsSpread(final SyncWingsSpread.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                FlightData data = FlightData.getData(player);
                data.areWingsSpread = message.state();
            }
        }
    }

    public static void handleSyncSpinStatus(final SpinStatus packet) {
        Player localPlayer = Objects.requireNonNull(Minecraft.getInstance().player);

        if (localPlayer.level().getEntity(packet.playerId()) instanceof Player player) {
            FlightData spin = FlightData.getData(player);
            spin.hasSpin = packet.hasSpin();
            spin.swimSpinFluid = packet.swimSpinFluid().map(fluidTypeResourceKey -> player.registryAccess().holderOrThrow(fluidTypeResourceKey));
            ClientFlightHandler.lastSync = player.tickCount;
        }
    }

    public static void handleSyncMagicCap(final SyncMagicCap.Data message, HolderLookup.Provider provider) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                MagicData magicData = MagicData.getData(player);
                magicData.deserializeNBT(provider, message.nbt());
            }
        }
    }

    public static void handleSyncPotionAddedEffect(final SyncVisualEffectAdded.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.entityId());
            Optional<Holder.Reference<MobEffect>> mobEffect = BuiltInRegistries.MOB_EFFECT.getHolder(message.effectId());

            if (mobEffect.isPresent()) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addEffect(new MobEffectInstance(mobEffect.get(), message.duration(), message.amplifier()));
                }
            }
        }
    }

    public static void handleSyncPotionRemovedEffect(final SyncVisualEffectRemoved.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());
            Optional<Holder.Reference<MobEffect>> mobEffect = BuiltInRegistries.MOB_EFFECT.getHolder(message.effectId());

            if (mobEffect.isPresent()) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.removeEffect(mobEffect.get());
                }
            }
        }
    }

    public static void handleSyncDragonMovement(final SyncDragonMovement.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if(DragonStateProvider.isDragon(entity)) {
                MovementData data = MovementData.getData(entity);
                data.setFirstPerson(message.isFirstPerson());
                data.setBite(message.bite());
                data.setFreeLook(message.isFreeLook());
                data.setDesiredMoveVec(new Vec2(message.desiredMoveVecX(), message.desiredMoveVecY()));
            }
        }
    }

    public static void handleSyncPassengerID(final SyncDragonPassengerID.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getOptional(player).ifPresent(handler -> handler.setPassengerId(message.passengerId()));
            }
        }
    }

    public static void handleSyncGrowthState(final SyncGrowthState.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            DragonStateProvider.getOptional(localPlayer).ifPresent(handler -> handler.isGrowing = message.growing());
        }
    }

    public static void handlePlayerJumpSync(final SyncPlayerJump.Data message) {
        Entity entity = Minecraft.getInstance().level.getEntity(message.playerId());

        if (entity instanceof Player player) {
            DragonEntity.dragonsJumpingTicks.put(player.getId(), message.ticks());
        }
    }

    public static void handleRefreshDragons(final RefreshDragon.Data message) {
        Player localPlayer = Minecraft.getInstance().player;
        Entity entity = localPlayer.level().getEntity(message.playerId());

        if (entity instanceof Player player) {
            DragonEntity dragon = DSEntities.DRAGON.get().create(localPlayer.level());
            dragon.playerId = player.getId();
            ClientDragonRenderer.playerDragonHashMap.computeIfAbsent(player.getId(), integer -> new AtomicReference<>(dragon)).getAndSet(dragon);
        }
    }

    public static void handleSyncParticleTrail(SyncParticleTrail message) {
            // Creates a trail of particles between the entity and target(s)
            Vec3 source = new Vec3(message.source().x(), message.source().y(), message.source().z());
            Vec3 target = new Vec3(message.target().x(), message.target().y(), message.target().z());
            // Scale steps based off of the distance between the source and target
            int steps = Math.max(20, (int) Math.ceil(source.distanceTo(target) * 2.5));
            float stepSize = 1.f / steps;
            Vec3 distV = new Vec3(source.x - target.x, source.y - target.y, source.z - target.z);
            for (int i = 0; i < steps; i++) {
                // the current entity coordinate + ((the distance between it and the target) * (the fraction of the total))
                Vec3 step = target.add(distV.scale(stepSize * i));
                Minecraft.getInstance().level.addParticle(message.trailParticle(), step.x(), step.y(), step.z(), 0.0, 0.0, 0.0);
            }
    }
}
