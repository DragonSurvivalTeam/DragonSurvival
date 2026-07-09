package by.dragonsurvivalteam.dragonsurvival.mixins.appleskin;

import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.HUDHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import squeek.appleskin.client.HUDOverlayHandler;

import javax.annotation.Nullable;

import static squeek.appleskin.helpers.TextureHelper.*;

@Mixin(HUDOverlayHandler.class)
public class HUDOverlayHandlerMixin {
    @ModifyArgs(method="drawSaturationOverlay(FFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIFI)V", at= @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIIII)V"))
    private static void dragonSurvival$changeSaturationIcons(Args args) {
        Identifier foodSprites = dragonSurvival$getDragonFoodSprites();

        if (foodSprites != null) {
            args.set(1, foodSprites);
        }

        // Theirs: 0 = 1/4, 9 = 1/2, 18 = 3/4, 27 = 1
        // Replace 3/4 with 1 because we don't have a 3/4 sprite
        // 0 → 18
        // 9 → 9
        // 18 → 27
        // 27 → 27
        float u = args.get(4);
        if (u == 0)
            args.set(4, 18.f);
        else if (u == 18)
            args.set(4, 27.f);
    }

    @Redirect(method="drawHungerOverlay(IILnet/minecraft/world/entity/player/Player;Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIFZI)V", at=@At(value = "INVOKE", target="Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIIII)V", ordinal = 1))
    private static void dragonSurvival$changeAppleSkinFoodIcons(GuiGraphicsExtractor graphics, RenderPipeline renderPipeline, Identifier sprite, int x, int y, int width, int height, int color, @Local(argsOnly = true, name = "useRottenTextures") boolean useRottenTextures, @Local(name = "iconSprite") Identifier iconSprite) {
        Identifier foodSprites = dragonSurvival$getDragonFoodSprites();

        if (foodSprites != null) {
            int uOffset = 0;

            if (iconSprite.equals(FOOD_EMPTY_HUNGER_TEXTURE)) {
                uOffset = 117;
            } else if (iconSprite.equals(FOOD_EMPTY_TEXTURE)) {
                uOffset = 108;
            } else if (iconSprite.equals(FOOD_HALF_TEXTURE)) {
                uOffset = 99;
            } else if (iconSprite.equals(FOOD_FULL_TEXTURE)) {
                uOffset = 90;
            } else if (iconSprite.equals(FOOD_HALF_HUNGER_TEXTURE)) {
                uOffset = 81;
            } else if (iconSprite.equals(FOOD_FULL_HUNGER_TEXTURE)) {
                uOffset = 72;
            }

            // graphics.blit(foodSprites, left - i * 8 - 9, y, hunger ? 81 : 45, 0, 9, 9);
            // graphics.blit(RenderPipelines.GUI_TEXTURED, foodSprites, left - i * 8 - 9, y, hunger ? 72 : 36, 0, 9, 9, 256, 256);

            graphics.blit(RenderPipelines.GUI_TEXTURED, foodSprites, x, y, uOffset, 0, width, height, 256, 256);
        } else {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
        }
    }

    @Unique
    private static @Nullable Identifier dragonSurvival$getDragonFoodSprites() {
        if (DragonFoodHandler.dragonFoodHandlingIsDisabled() || HUDHandler.vanillaFoodLevel) {
            // Same check exists for 'HudHandler' which manages whether the vanilla food icons are to be shown or not
            return null;
        }

        LocalPlayer localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            DragonStateHandler handler = DragonStateProvider.getData(localPlayer);

            if (handler.isDragon()) {
                return handler.species().value().miscResources().foodSprites().orElse(null);
            }
        }

        return null;
    }
}
