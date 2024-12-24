package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncTreasureRestStatus;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.network.PacketDistributor;
import software.bernie.geckolib.util.Color;

/** See {@link by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonTreasureHandler} for common logic */
@EventBusSubscriber(Dist.CLIENT)
public class DragonTreasureHandler {
    private static int sleepTimer = 0;

    @SubscribeEvent
    public static void stopRestingIfActive(ClientTickEvent.Post event) {
        Player player = DragonSurvival.PROXY.getLocalPlayer();

        if (player == null) {
            return;
        }

        TreasureRestData data = TreasureRestData.getData(player);
        if (data.isResting) {
            Vec3 velocity = player.getDeltaMovement();

            // Stop the process of resting if the player is moving too much or starts to mine sth.
            if (Math.abs(velocity.horizontalDistance()) > 0.05 || MovementData.getData(player).dig) {
                data.isResting = false;
                PacketDistributor.sendToServer(new SyncTreasureRestStatus.Data(player.getId(), false));
            }
        }
    }

    @SubscribeEvent
    public static void renderSleepScreen(RenderGuiLayerEvent.Post event) {
        Player player = Minecraft.getInstance().player;

        if (player == null || player.isSpectator() || event.getName() != VanillaGuiLayers.AIR_LEVEL) {
            return;
        }

        TreasureRestData data = TreasureRestData.getData(player);
        Window window = Minecraft.getInstance().getWindow();

        if (data.isResting && sleepTimer < 100 && Functions.getSunPosition(player) < 0.25) {
            sleepTimer++;
        } else if (sleepTimer > 0) {
            sleepTimer--;
        }

        if (sleepTimer > 0) {
            Color darkening = Color.ofRGBA(0.05f, 0.05f, 0.05f, Mth.lerp(Math.min(sleepTimer, 100) / 100f, 0, 0.5F));
            event.getGuiGraphics().fill(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(), darkening.getColor());
        }
    }
}
