package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.LiquidBlockRendererMixin;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Handles water, lava, and block vision effects */
@EventBusSubscriber(Dist.CLIENT)
public class VisionHandler {
    private static boolean hadLavaVision;
    private static boolean hadWaterVision;

    @SubscribeEvent
    public static void onRenderFog(final ViewportEvent.RenderFog event) {
        if (hasLavaVision() && event.getCamera().getFluidInCamera() == FogType.LAVA) {
            event.setNearPlaneDistance(0);
            event.setFarPlaneDistance(event.getRenderer().getRenderDistance() * 0.5f);
            event.setCanceled(true);
        }
    }

    /** The alpha change in {@link LiquidBlockRendererMixin} requires the drawn blocks to be uncached and be re-rendered */
    @SubscribeEvent
    public static void markChangedIfVisionStateChanged(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        boolean hasLavaVision = hasLavaVision();
        boolean hasWaterVision = hasWaterVision();

        boolean shouldUpdate = !hadLavaVision && hasLavaVision || hadLavaVision && !hasLavaVision;
        shouldUpdate = shouldUpdate || (!hadWaterVision && hasWaterVision || hadWaterVision && !hasWaterVision);

        hadLavaVision = hasLavaVision;
        hadWaterVision = hasWaterVision;

        if (shouldUpdate) {
            event.getLevelRenderer().allChanged();
        }
    }

    public static boolean hasLavaVision() {
        Player player = DragonSurvival.PROXY.getLocalPlayer();

        if (player != null) {
            return player.hasEffect(DSEffects.LAVA_VISION);
        }

        return false;
    }

    public static boolean hasWaterVision() {
        Player player = DragonSurvival.PROXY.getLocalPlayer();

        if (player != null) {
            return player.hasEffect(DSEffects.WATER_VISION);
        }

        return false;
    }

    public static boolean hasVision(final VisionType type) {
        return switch (type) {
            case WATER -> hasWaterVision();
            case LAVA -> hasLavaVision();
        };
    }

    public enum VisionType {
        WATER,
        LAVA
    }
}
