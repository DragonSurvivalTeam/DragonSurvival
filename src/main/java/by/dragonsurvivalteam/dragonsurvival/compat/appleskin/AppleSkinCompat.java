package by.dragonsurvivalteam.dragonsurvival.compat.appleskin;

import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import squeek.appleskin.client.HUDOverlayHandler;

public final class AppleSkinCompat {
    private static final HUDOverlayHandler HUD_OVERLAY_HANDLER = new HUDOverlayHandler();

    private AppleSkinCompat() {}

    public static void renderFoodOverlayAfterDragonBar(final RenderGuiOverlayEvent.Pre event) {
        RenderGuiOverlayEvent.Post postEvent = new RenderGuiOverlayEvent.Post(
                event.getWindow(),
                event.getGuiGraphics(),
                event.getPartialTick(),
                event.getOverlay()
        );
        HUD_OVERLAY_HANDLER.onRenderGuiOverlayPost(postEvent);
    }
}
