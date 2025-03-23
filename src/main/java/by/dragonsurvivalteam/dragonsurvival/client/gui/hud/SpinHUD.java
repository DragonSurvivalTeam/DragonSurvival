package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class SpinHUD {
    @ConfigRange
    @Translation(key = "spin_cooldown_x_offset", type = Translation.Type.CONFIGURATION, comments = "Offset to the x position of the spin cooldown indicator")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "spin"}, key = "spin_cooldown_x_offset")
    public static Integer spinCooldownXOffset = 0;

    @ConfigRange
    @Translation(key = "spin_cooldown_y_offset", type = Translation.Type.CONFIGURATION, comments = "Offset to the y position of the spin cooldown indicator")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "spin"}, key = "spin_cooldown_y_offset")
    public static Integer spinCooldownYOffset = 0;

    public static final ResourceLocation ID = DragonSurvival.res("spin_cooldown");

    private static final ResourceLocation SPIN_COOLDOWN = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/spin_cooldown.png");

    public static void render(@NotNull final GuiGraphics graphics, @NotNull final DeltaTracker tracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null || player.isSpectator()) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return;
        }

        if (!ServerFlightHandler.isFlying(player) && !ServerFlightHandler.canSwimSpin(player)) {
            return;
        }

        FlightData spin = FlightData.getData(player);

        if (spin.hasSpin && spin.cooldown > 0 && !Minecraft.getInstance().options.hideGui) {
            Window window = Minecraft.getInstance().getWindow();

            int cooldown = ServerFlightHandler.flightSpinCooldown * 20;
            float cooldownProgress = ((float) cooldown - (float) spin.cooldown) / (float) cooldown;

            int x = window.getGuiScaledWidth() / 2 - 66 / 2;
            int y = window.getGuiScaledHeight() - 96;

            x += spinCooldownXOffset;
            y += spinCooldownYOffset;

            int width = (int) (cooldownProgress * 62);
            graphics.blit(SPIN_COOLDOWN, x, y, 0, 0, 66, 21, 256, 256);
            graphics.blit(SPIN_COOLDOWN, x + 4, y + 1, 4, 21, width, 21, 256, 256);
        }
    }
}
