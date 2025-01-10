package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(Dist.CLIENT)
public class HUDHandler {
    @Translation(key = "show_vanilla_food_bar", type = Translation.Type.CONFIGURATION, comments = "If enabled the vanilla food bar will be shown")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "hud"}, key = "show_vanilla_food_bar")
    public static Boolean vanillaFoodLevel = false;

    @Translation(key = "show_vanilla_experience_bar", type = Translation.Type.CONFIGURATION, comments = "If enabled the vanilla experience bar will be shown")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "hud"}, key = "show_vanilla_experience_bar")
    public static Boolean vanillaExperienceBar = false;

    @SubscribeEvent(receiveCanceled = true) // TODO :: should probably register experience + food as well and cancel those here depending on the config
    public static void onRenderOverlay(final RenderGuiLayerEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (event.isCanceled() || minecraft.options.hideGui) {
            return;
        }

        int screenWidth = event.getGuiGraphics().guiWidth();
        int screenHeight = event.getGuiGraphics().guiHeight();
        ResourceLocation id = event.getName();

        if (DragonFoodHandler.requireDragonFood && !vanillaFoodLevel && id == VanillaGuiLayers.FOOD_LEVEL) {
            boolean wasRendered = FoodBar.render(Minecraft.getInstance().gui, event.getGuiGraphics(), screenWidth, screenHeight);

            if (wasRendered) {
                event.setCanceled(true);
            }
        } else if (ServerConfig.consumeExperienceAsMana && !vanillaExperienceBar && id == VanillaGuiLayers.EXPERIENCE_BAR) {
            boolean wasRendered = MagicHUD.renderExperienceBar(event.getGuiGraphics(), screenWidth);

            if (wasRendered) {
                event.setCanceled(true);
            }
        } else if (id == VanillaGuiLayers.AIR_LEVEL) {
            //noinspection DataFlowIssue -> player is present
            SwimData data = SwimData.getData(minecraft.player);

            if (data.getMaxOxygen(minecraft.player.getEyeInFluidType()) == SwimData.UNLIMITED_OXYGEN) {
                event.setCanceled(true);
            }
        }
    }
}
