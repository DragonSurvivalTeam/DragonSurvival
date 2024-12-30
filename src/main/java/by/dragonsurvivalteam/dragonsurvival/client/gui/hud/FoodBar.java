package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

public class FoodBar {
    private static final RandomSource RANDOM = RandomSource.create();

    public static boolean render(final Gui gui, final GuiGraphics graphics, int width, int height) {
        Player localPlayer = DragonSurvival.PROXY.getLocalPlayer();

        if (localPlayer == null || !Minecraft.getInstance().gameMode.canHurtPlayer()) {
            return false;
        }

        DragonStateHandler handler = DragonStateProvider.getData(localPlayer);

        if (!handler.isDragon()) {
            return false;
        }

        Minecraft.getInstance().getProfiler().push("food");
        RenderSystem.enableBlend();

        final int left = width / 2 + 91;
        final int top = height - gui.rightHeight;
        gui.rightHeight += 10;
        final FoodData food = localPlayer.getFoodData();

        ResourceLocation foodIcons = handler.species().value().miscResources().foodSprites();

        final boolean hunger = localPlayer.hasEffect(MobEffects.HUNGER);

        for (int i = 0; i < 10; i++) {
            int icon = i * 2 + 1; // there can be 10 icons (food level maximum is 20)
            int y = top;

            if (food.getSaturationLevel() <= 0 && localPlayer.tickCount % (food.getFoodLevel() * 3 + 1) == 0) {
                // Animate the food icons (moving up / down)
                y = top + RANDOM.nextInt(3) - 1;
            }

            graphics.blit(foodIcons, left - i * 8 - 9, y, hunger ? 117 : 0, 0, 9, 9);

            if (icon < food.getFoodLevel()) {
                graphics.blit(foodIcons, left - i * 8 - 9, y, hunger ? 72 : 36, 0, 9, 9);
            } else if (icon == food.getFoodLevel()) {
                graphics.blit(foodIcons, left - i * 8 - 9, y, hunger ? 81 : 45, 0, 9, 9);
            }
        }

        RenderSystem.disableBlend();
        Minecraft.getInstance().getProfiler().pop();

        return true;
    }
}
