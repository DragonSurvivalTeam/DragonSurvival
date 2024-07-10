package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Handles water and lava vision effects */
@EventBusSubscriber(Dist.CLIENT)
public class VisionHandler {
    private static boolean hadLavaVision;
    private static boolean hadWaterVision;

    @SubscribeEvent
    public static void onRenderFog(ViewportEvent.RenderFog event) {
        if (hasLavaVision() && event.getCamera().getFluidInCamera() == FogType.LAVA) {
            event.setNearPlaneDistance(0);
            event.setFarPlaneDistance(event.getRenderer().getRenderDistance() * 0.2f);
            event.setCanceled(true);
        }
    }

    /** The alpha change in {@link by.dragonsurvivalteam.dragonsurvival.mixins.client.LiquidBlockRendererMixin} requires the drawn blocks to be uncached and be re-rendered */
    @SubscribeEvent
    public static void onRenderWorldLastEvent(RenderLevelStageEvent event) {
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
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return false;
        }

        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            return player.hasEffect(DSEffects.LAVA_VISION);
        }

        return false;
    }

    public static boolean hasWaterVision() {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return false;
        }

        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            return player.hasEffect(DSEffects.WATER_VISION);
        }

        return false;
    }
}
