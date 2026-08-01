package by.dragonsurvivalteam.dragonsurvival.mixins.appleskin;

import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.HUDHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import squeek.appleskin.client.HUDOverlayHandler;

@Mixin(HUDOverlayHandler.class)
public abstract class HUDOverlayHandlerMixin {
    @ModifyArg(
            method = "drawSaturationOverlay(FFLnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            index = 0
    )
    private static ResourceLocation dragonSurvival$changeSaturationTexture(final ResourceLocation original) {
        ResourceLocation foodSprites = dragonSurvival$getDragonFoodSprites();
        return foodSprites == null ? original : foodSprites;
    }

    @ModifyArg(
            method = "drawSaturationOverlay(FFLnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            index = 3
    )
    private static int dragonSurvival$changeSaturationOffset(final int u) {
        if (dragonSurvival$getDragonFoodSprites() == null) {
            return u;
        }

        // AppleSkin has quarter and three-quarter icons, while dragon food sheets do not.
        return u == 0 ? 18 : u == 18 ? 27 : u;
    }

    @ModifyArg(
            method = "drawHungerOverlay(IILnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/GuiGraphics;IIFZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            index = 0
    )
    private static ResourceLocation dragonSurvival$changeHungerTexture(final ResourceLocation original) {
        ResourceLocation foodSprites = dragonSurvival$getDragonFoodSprites();
        return foodSprites == null ? original : foodSprites;
    }

    @ModifyArg(
            method = "drawHungerOverlay(IILnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/GuiGraphics;IIFZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            index = 3
    )
    private static int dragonSurvival$changeHungerOffset(final int u) {
        if (dragonSurvival$getDragonFoodSprites() == null) {
            return u;
        }

        return switch (u) {
            case 25 -> 108;
            case 52 -> 90;
            case 61 -> 99;
            case 88 -> 72;
            case 97 -> 81;
            case 133 -> 117;
            default -> u;
        };
    }

    @ModifyArg(
            method = "drawHungerOverlay(IILnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/GuiGraphics;IIFZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"),
            index = 4
    )
    private static int dragonSurvival$changeHungerVerticalOffset(final int v) {
        return dragonSurvival$getDragonFoodSprites() == null ? v : 0;
    }

    @Unique
    private static @Nullable ResourceLocation dragonSurvival$getDragonFoodSprites() {
        if (DragonFoodHandler.dragonFoodHandlingIsDisabled() || HUDHandler.vanillaFoodLevel) {
            return null;
        }

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return null;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        return handler.isDragon() ? handler.species().value().miscResources().foodSprites().orElse(null) : null;
    }
}
