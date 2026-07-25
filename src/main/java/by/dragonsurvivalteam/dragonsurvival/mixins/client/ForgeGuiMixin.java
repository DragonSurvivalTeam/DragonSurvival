package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ForgeGui.class, remap = false)
public abstract class ForgeGuiMixin {
    @Redirect(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private void dragonSurvival$renderAirSprite(final GuiGraphics graphics, final ResourceLocation texture, int x, int y, int u, int v, int width, int height) {
        ResourceLocation replacement = dragonSurvival$getAirSprite(u == 25);

        if (replacement == null) {
            graphics.blit(texture, x, y, u, v, width, height);
        } else {
            RenderingUtils.blitGuiSprite(graphics, replacement, x, y, 0, width, height);
        }
    }

    @Unique
    private static @Nullable ResourceLocation dragonSurvival$getAirSprite(boolean burst) {
        Player player = Minecraft.getInstance().player;

        if (player == null) {
            return null;
        }

        FluidType currentFluidType = player.getEyeInFluidType();
        FluidType relevantFluid = ForgeMod.EMPTY_TYPE.getKey() == SwimData.key(currentFluidType)
                ? SwimData.getData(player).previousFluid
                : currentFluidType;

        if (relevantFluid == null) {
            return null;
        }

        ResourceLocation replacement = burst ? SwimData.getAirBurstSprite(relevantFluid) : SwimData.getAirSprite(relevantFluid);
        return replacement != null && RenderingUtils.hasGuiSprite(replacement) ? replacement : null;
    }
}
