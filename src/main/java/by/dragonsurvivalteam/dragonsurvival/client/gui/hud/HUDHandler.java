package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import by.dragonsurvivalteam.dragonsurvival.compat.appleskin.AppleSkinCompat;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class HUDHandler {
    @Translation(key = "show_vanilla_food_bar", type = Translation.Type.CONFIGURATION, comments = "If enabled the vanilla food bar will be shown")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "hud"}, key = "show_vanilla_food_bar")
    public static Boolean vanillaFoodLevel = false;

    @Translation(key = "show_vanilla_experience_bar", type = Translation.Type.CONFIGURATION, comments = "If enabled the vanilla experience bar will be shown")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "hud"}, key = "show_vanilla_experience_bar")
    public static Boolean vanillaExperienceBar = false;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderOverlay(final RenderGuiOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.options.hideGui) {
            return;
        }

        int screenWidth = event.getGuiGraphics().guiWidth();
        int screenHeight = event.getGuiGraphics().guiHeight();
        ResourceLocation id = event.getOverlay().id();

        if (!DragonFoodHandler.dragonFoodHandlingIsDisabled() && !vanillaFoodLevel && id.equals(VanillaGuiOverlay.FOOD_LEVEL.id())) {
            boolean wasRendered = FoodBar.render(event.getGuiGraphics(), screenWidth, screenHeight);

            if (wasRendered) {
                if (ModID.APPLESKIN.isLoaded()) {
                    AppleSkinCompat.renderFoodOverlayAfterDragonBar(event);
                }

                event.setCanceled(true);
            }
        } else if (!vanillaExperienceBar && id.equals(VanillaGuiOverlay.EXPERIENCE_BAR.id())) {
            boolean wasRendered = MagicHUD.renderExperienceBar(event.getGuiGraphics(), screenWidth);

            if (wasRendered) {
                event.setCanceled(true);
            }
        } else if (id.equals(VanillaGuiOverlay.AIR_LEVEL.id())) {
            //noinspection DataFlowIssue -> player is present
            SwimData data = SwimData.getData(minecraft.player);

            if (data.getMaxOxygen(minecraft.player, minecraft.player.getEyeInFluidType()) == SwimData.UNLIMITED_OXYGEN) {
                event.setCanceled(true);
            }
        }
    }
}
