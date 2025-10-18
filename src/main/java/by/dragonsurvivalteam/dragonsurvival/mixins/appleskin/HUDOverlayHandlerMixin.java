package by.dragonsurvivalteam.dragonsurvival.mixins.appleskin;

import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.HUDHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import squeek.appleskin.client.HUDOverlayHandler;

import static squeek.appleskin.helpers.TextureHelper.*;

@Mixin(HUDOverlayHandler.class)
public class HUDOverlayHandlerMixin {
    @ModifyArgs(method="drawSaturationOverlay(FFLnet/minecraft/world/entity/player/Player;Lnet/minecraft/client/gui/GuiGraphics;IIFI)V", at= @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private static void dragonSurvival$changeSaturationIcons(Args args) {
        ResourceLocation foodSprites = dragonSurvival$getDragonFoodSprites();

        if (foodSprites != null) {
            args.set(0, foodSprites);
        }

        // Theirs: 0 = 1/4, 9 = 1/2, 18 = 3/4, 27 = 1
        // Replace 3/4 with 1 because we don't have a 3/4 sprite
        // 0 → 18
        // 9 → 9
        // 18 → 27
        // 27 → 27
        int u = args.get(3);
        if (u == 0)
            args.set(3, 18);
        else if (u == 18)
            args.set(3, 27);
    }

    @Redirect(method="drawHungerOverlay(IILnet/minecraft/world/entity/player/Player;Lnet/minecraft/client/gui/GuiGraphics;IIFZI)V", at=@At(value = "INVOKE", target="Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 1))
    private static void dragonSurvival$changeAppleSkinFoodIcons(GuiGraphics instance, ResourceLocation sprite, int x, int y, int width, int height, @Local(argsOnly=true) boolean useRottenTextures, @Local(ordinal = 1) ResourceLocation iconSprite) {
        ResourceLocation foodSprites = dragonSurvival$getDragonFoodSprites();

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

            instance.blit(foodSprites, x, y, uOffset, 0, width, height);
        } else {
            instance.blitSprite(sprite, x, y, width, height);
        }
    }

    @Unique private static @Nullable ResourceLocation dragonSurvival$getDragonFoodSprites() {
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
