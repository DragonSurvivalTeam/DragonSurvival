package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** Handles water, lava, and block vision effects */
@EventBusSubscriber(Dist.CLIENT)
public final class VisionHandler {
    private static boolean hadLavaVision;
    private static boolean hadWaterVision;

    private VisionHandler() {}

    public static boolean hasLavaVision() {
        Player player = DragonSurvival.PROXY.getLocalPlayer();
        return player != null && player.hasEffect(DSEffects.LAVA_VISION);
    }

    public static boolean hasWaterVision() {
        Player player = DragonSurvival.PROXY.getLocalPlayer();
        return player != null && player.hasEffect(DSEffects.WATER_VISION);
    }

    @SubscribeEvent
    public static void onRenderFog(final ViewportEvent.RenderFog event) {
        if (hasLavaVision() && event.getType() == FogType.LAVA) {
            event.setNearPlaneDistance(0.0F);
            event.setFarPlaneDistance(Minecraft.getInstance().options.getEffectiveRenderDistance() * 8.0F);
        }
    }

    /** Refresh chunk renders when fluid translucency changes with the active vision effect. */
    @SubscribeEvent
    public static void markChangedIfVisionStateChanged(final RenderLevelStageEvent.AfterTranslucentParticles event) {
        boolean hasLavaVision = hasLavaVision();
        boolean hasWaterVision = hasWaterVision();
        boolean shouldUpdate = hadLavaVision != hasLavaVision || hadWaterVision != hasWaterVision;

        hadLavaVision = hasLavaVision;
        hadWaterVision = hasWaterVision;

        if (shouldUpdate) {
            event.getLevelRenderer().allChanged();
        }
    }
}
