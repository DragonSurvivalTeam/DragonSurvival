package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

public class FoodBar {
    private static final RandomSource RANDOM = RandomSource.create();

    public static boolean render(final GuiGraphics graphics, int width, int height) {
        Player localPlayer = DragonSurvival.PROXY.getLocalPlayer();

        //noinspection DataFlowIssue -> instance is present
        if (localPlayer == null || !Minecraft.getInstance().gameMode.canHurtPlayer()) {
            return false;
        }

        DragonStateHandler handler = DragonStateProvider.getData(localPlayer);

        if (!handler.isDragon()) {
            return false;
        }

        if (DietEntryCache.isEmpty(handler.species())) {
            return false;
        }

        Identifier foodSprites = handler.species().value().miscResources().foodSprites().orElse(null);

        if (foodSprites == null) {
            return false;
        }

        // FIXME :: UI GRAPHICS
        Profiler.get().push("food");
        //RenderSystem.enableBlend();

        final int left = width / 2 + 91;
        final int top = height - Minecraft.getInstance().gui.rightHeight;
        Minecraft.getInstance().gui.rightHeight += 10;
        final FoodData food = localPlayer.getFoodData();

        final boolean hunger = localPlayer.hasEffect(MobEffects.HUNGER);

        for (int i = 0; i < 10; i++) {
            int icon = i * 2 + 1; // there can be 10 icons (food level maximum is 20)
            int y = top;

            if (food.getSaturationLevel() <= 0 && localPlayer.tickCount % (food.getFoodLevel() * 3 + 1) == 0) {
                // Animate the food icons (moving up / down)
                y = top + RANDOM.nextInt(3) - 1;
            }

            graphics.blit(foodSprites, left - i * 8 - 9, y, hunger ? 117 : 0, 0, 9, 9, 256, 256);

            if (icon < food.getFoodLevel()) {
                graphics.blit(foodSprites, left - i * 8 - 9, y, hunger ? 72 : 36, 0, 9, 9, 256, 256);
            } else if (icon == food.getFoodLevel()) {
                graphics.blit(foodSprites, left - i * 8 - 9, y, hunger ? 81 : 45, 0, 9, 9, 256, 256);
            }
        }

        // FIXME UI GRAPHICS
        //RenderSystem.disableBlend();
        Profiler.get().pop();

        return true;
    }
}
