package by.dragonsurvivalteam.dragonsurvival.client.render.block_vision;

import by.dragonsurvivalteam.dragonsurvival.client.render.BlockVisionHandler;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.block_vision.BlockVision;
import by.dragonsurvivalteam.dragonsurvival.registry.DSParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.effects.SpawnParticlesEffect;

public final class BlockVisionParticle {
    private static final SpawnParticlesEffect.PositionSource PARTICLE_POSITION = SpawnParticlesEffect.inBoundingBox();

    private BlockVisionParticle() {}

    public static void spawnParticle(final BlockVisionHandler.Data data, final Player player) {
        if (Minecraft.getInstance().isPaused() || data.particleRate() == BlockVision.NO_VALUE || player.tickCount % data.particleRate() != 0) {
            return;
        }

        // Increase the sampled box to make particles visible even when the target block is buried.
        double xPos = PARTICLE_POSITION.getCoordinate(data.x(), data.x() + 0.5, 2, player.getRandom());
        double yPos = PARTICLE_POSITION.getCoordinate(data.y(), data.y() + 0.5, 2, player.getRandom());
        double zPos = PARTICLE_POSITION.getCoordinate(data.z(), data.z() + 0.5, 2, player.getRandom());
        player.level().addParticle(DSParticles.GLOW.get(), xPos, yPos, zPos, BuiltInRegistries.BLOCK.getId(data.state().getBlock()), 0, 0);
    }
}
