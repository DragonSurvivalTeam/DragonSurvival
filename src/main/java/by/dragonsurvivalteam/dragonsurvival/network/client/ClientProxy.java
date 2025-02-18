package by.dragonsurvivalteam.dragonsurvival.network.client;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAltarScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.UnlockableBehavior;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonEditor;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncParticleTrail;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
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
}
