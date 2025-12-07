package by.dragonsurvivalteam.dragonsurvival.client.render.block_vision;

import by.dragonsurvivalteam.dragonsurvival.client.render.BlockVisionHandler;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.block_vision.BlockVision;
import by.dragonsurvivalteam.dragonsurvival.registry.DSParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;

public class BlockVisionParticle {
    // TODO :: make it configurable?
    private static final SpawnParticlesEffect.PositionSource PARTICLE_POSITION = SpawnParticlesEffect.inBoundingBox();

    public static void spawnParticle(final BlockVisionHandler.Data data, final Player player) {
        if (Minecraft.getInstance().isPaused()) {
            // Newly added particles will only render once the game is unpaused
            // Meaning if we don't skip here, all the added particles will be shown at once
            return;
        }

        if (data.particleRate() == BlockVision.NO_PARTICLE_RATE) {
            // It should not really be possible for this to occur with the particle display type
            return;
        }

        if (player.tickCount % data.particleRate() != 0) {
            return;
        }

        // Increase the bounding box to make the particles more visible for blocks in walls etc.
        // TODO :: Maybe there is a somewhat reasonable way to only show particles / focus particles on non-occluded faces?
        // TODO :: currently also spawns particles behind blocks (even though it doesn't have a x-ray "feature") - unsure about the performance impact
        double xPos = PARTICLE_POSITION.getCoordinate(data.x(), data.x() + 0.5, 2, player.getRandom());
        double yPos = PARTICLE_POSITION.getCoordinate(data.y(), data.y() + 0.5, 2, player.getRandom());
        double zPos = PARTICLE_POSITION.getCoordinate(data.z(), data.z() + 0.5, 2, player.getRandom());
        player.level().addParticle(DSParticles.GLOW.get(), xPos, yPos, zPos, BuiltInRegistries.BLOCK.getId(data.block()), 0, /* Color offset */ 0);
    }
}
