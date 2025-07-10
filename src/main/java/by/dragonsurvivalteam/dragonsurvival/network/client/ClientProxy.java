package by.dragonsurvivalteam.dragonsurvival.network.client;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAltarScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.UnlockableBehavior;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonEditor;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncBreathParticles;
import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncParticleTrail;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

/** To avoid loading client classes on the server side */
public class ClientProxy {
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

        PacketDistributor.sendToServer(new SyncDragonClawRender(localPlayer.getId(), ClientDragonRenderer.renderDragonClaws));
        PacketDistributor.sendToServer(new SyncDragonSkinSettings(localPlayer.getId(), ClientDragonRenderer.renderCustomSkin));
        PacketDistributor.sendToServer(new SyncPlayerSkinPreset(localPlayer.getId(), data.speciesKey(), data.getCurrentSkinPreset().serializeNBT(localPlayer.registryAccess())));
    }

    public static void openDragonAltar() {
        PacketDistributor.sendToServer(new OpenDragonAltar(List.of()));
    }

    public static void openDragonAltar(final List<UnlockableBehavior.SpeciesEntry> entries) {
        Minecraft.getInstance().setScreen(new DragonAltarScreen(entries));
    }

    public static void openDragonEditor(ResourceKey<DragonSpecies> species, boolean fromAltar) {
        PacketDistributor.sendToServer(new OpenDragonEditor(species, List.of(), fromAltar));
    }

    public static void openDragonEditor(final List<UnlockableBehavior.BodyEntry> entries, ResourceKey<DragonSpecies> species, boolean fromAltar) {
        Optional<Holder.Reference<DragonSpecies>> speciesHolder = ResourceHelper.get(null, species);
        if (speciesHolder.isEmpty()) {
            Minecraft.getInstance().setScreen(null);
        } else {
            Minecraft.getInstance().setScreen(new DragonEditorScreen(speciesHolder.get(), entries, fromAltar));
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
            //noinspection DataFlowIssue -> level is present
            Minecraft.getInstance().level.addParticle(message.trailParticle(), step.x(), step.y(), step.z(), 0.0, 0.0, 0.0);
        }
    }

    public static void handleBreathParticles(final SyncBreathParticles packet, final Player receiver) {
        if (!(receiver.level().getEntity(packet.playerId()) instanceof Entity entity)) {
            return;
        }

        double speedMultiplier = 20;
        Vec3 position = null;

        if (entity instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.isDragon()) {
                speedMultiplier = handler.getGrowth();

                if (player != Minecraft.getInstance().player || Minecraft.getInstance().options.getCameraType() != CameraType.FIRST_PERSON) {
                    position = DragonRenderer.getBonePosition(player, "BreathSource");
                }
            }
        }

        float yaw = (float) Math.toRadians(-entity.getYRot());
        float pitch = (float) Math.toRadians(-entity.getXRot());
        float speed = (float) (packet.speedPerGrowth() * speedMultiplier);

        // Used to place the breath further in front of the dragon when moving fast
        // To avoid spawning the particles in the position of the entity's head
        double movement = 1 + entity.getDeltaMovement().horizontalDistanceSqr();
        Vec3 angle = entity.getLookAngle();

        if (position == null || position == Vec3.ZERO) {
            position = entity.getEyePosition().add(angle.scale(movement));
        } else {
            position = position.subtract(angle).add(angle.scale(movement));
        }

        for (int i = 0; i < packet.numParticles(); i++) {
            spawnParticle(packet.secondaryParticle(), entity, position, yaw, pitch, speed, packet.spread());
        }

        for (int i = 0; i < packet.numParticles() / 2; i++) {
            spawnParticle(packet.mainParticle(), entity, position, yaw, pitch, speed, packet.spread());
        }
    }

    private static void spawnParticle(final ParticleOptions particle, final Entity entity, final Vec3 position, final float yaw, final float pitch, final float speed, final float spread) {
        Vec3 velocity = calculateParticleVelocity((float) (yaw + spread * 2 * (entity.getRandom().nextDouble() * 2 - 1) * 2 * Math.PI), (float) (pitch + spread * (entity.getRandom().nextDouble() * 2 - 1) * 2.f * Math.PI), speed);
        velocity = velocity.add(entity.getDeltaMovement());
        entity.level().addParticle(particle, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
    }

    private static Vec3 calculateParticleVelocity(float yaw, float pitch, float speed) {
        float xVel = (float) (Math.sin(yaw) * Math.cos(pitch) * speed);
        float yVel = (float) Math.sin(pitch) * speed;
        float zVel = (float) (Math.cos(yaw) * Math.cos(pitch) * speed);
        return new Vec3(xVel, yVel, zVel);
    }
}
