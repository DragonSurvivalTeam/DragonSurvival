package by.dragonsurvivalteam.dragonsurvival.client;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientGrowthHudHandler;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.magic.ClientMagicHUDHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderHudEvents {
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "hud"}, key = "vanillaFoodLevel", comment = "Re-enable the vanilla hud for the food level")
    public static Boolean vanillaFoodLevel = false;

    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "hud"}, key = "vanillaExperienceBar", comment = "Re-enable the vanilla hud for the experience bar")
    public static Boolean vanillaExperienceBar = false;

    public static ForgeGui getForgeGUI() {
        return (ForgeGui) Minecraft.getInstance().gui;
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void onRenderOverlay(final RenderGuiOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (event.isCanceled() || minecraft.options.hideGui){
            return;
        }

        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();
        ResourceLocation id = event.getOverlay().id();

        if (!DragonFoodHandler.disableDragonFoodHandling && DragonFoodHandler.requireDragonFood && !vanillaFoodLevel && id == VanillaGuiOverlay.FOOD_LEVEL.id()) {
            boolean wasRendered = DragonFoodHandler.renderFoodBar(getForgeGUI(), event.getGuiGraphics(), screenWidth, screenHeight);

            if (wasRendered) {
                event.setCanceled(true);
            }
        } else if (ServerConfig.consumeEXPAsMana && !vanillaExperienceBar && id == VanillaGuiOverlay.EXPERIENCE_BAR.id()) {
            boolean wasRendered = ClientMagicHUDHandler.renderExperienceBar(getForgeGUI(), event.getGuiGraphics(), screenWidth);

            if (wasRendered) {
                event.setCanceled(true);
            }
        } else if (id == VanillaGuiOverlay.AIR_LEVEL.id()) {
            DragonStateHandler handler = DragonStateProvider.getHandler(ClientProxy.getLocalPlayer());

            if (handler == null || !handler.isDragon()) {
                return;
            }

            // Render dragon specific hud elements (e.g. time in rain for cave dragons or time without water for sea dragons)
            ClientEvents.renderOverlay(handler, getForgeGUI(), event.getGuiGraphics());
            // Renders the abilities
            ClientMagicHUDHandler.renderAbilityHud(handler, event.getGuiGraphics(), screenWidth, screenHeight);
            // Renders the growth icon above the experience bar when an item is selected which grants growth
            ClientGrowthHudHandler.renderGrowth(handler, event.getGuiGraphics(), screenWidth, screenHeight);
        }
    }
}
