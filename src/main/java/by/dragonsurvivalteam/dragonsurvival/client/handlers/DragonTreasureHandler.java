package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import software.bernie.geckolib.util.Color;

@EventBusSubscriber(Dist.CLIENT)
public class DragonTreasureHandler {
    private static int renderSleepTimer = 0;

    @SubscribeEvent
    public static void renderSleepScreen(final RenderGuiLayerEvent.Post event) {
        Player player = Minecraft.getInstance().player;

        if (player == null || player.isSpectator() || event.getName() != VanillaGuiLayers.AIR_LEVEL) {
            return;
        }

        TreasureRestData data = TreasureRestData.getData(player);
        Window window = Minecraft.getInstance().getWindow();

        if (data.isResting() && renderSleepTimer < 100 && Functions.getSunPosition(player) < 0.25) {
            renderSleepTimer++;
        } else if (renderSleepTimer > 0) {
            renderSleepTimer--;
        }

        if (renderSleepTimer > 0) {
            Color darkening = Color.ofRGBA(0.05f, 0.05f, 0.05f, Mth.lerp(Math.min(renderSleepTimer, 100) / 100f, 0, 0.5F));
            event.getGuiGraphics().fill(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(), darkening.getColor());
        }
    }
}
