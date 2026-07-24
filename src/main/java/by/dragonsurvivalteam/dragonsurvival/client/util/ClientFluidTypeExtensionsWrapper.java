package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.client.render.VisionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.NotNull;

public class ClientFluidTypeExtensionsWrapper implements IClientFluidTypeExtensions {
    private final IClientFluidTypeExtensions original;
    private final VisionHandler.VisionType type;

    public ClientFluidTypeExtensionsWrapper(final IClientFluidTypeExtensions original, final VisionHandler.VisionType type) {
        this.original = original;
        this.type = type;
    }

    @Override
    public @NotNull ResourceLocation getStillTexture() {
        return original.getStillTexture();
    }

    @Override
    public @NotNull ResourceLocation getFlowingTexture() {
        return original.getFlowingTexture();
    }

    @Override
    public ResourceLocation getOverlayTexture() {
        return original.getOverlayTexture();
    }

    @Override
    public ResourceLocation getRenderOverlayTexture(@NotNull final Minecraft minecraft) {
        return original.getRenderOverlayTexture(minecraft);
    }

    @Override
    public int getTintColor() {
        int color = original.getTintColor();

        if (VisionHandler.hasVision(type)) {
            // 0x5A is 90 which is roughly the result of 255 * 0.35
            return (color /* Remove alpha */ & 0x00FFFFFF) /* Add custom alpha */ | 0x5A000000;
        }

        return color;
    }

    @Override
    public int getTintColor(@NotNull final FluidState state, @NotNull final BlockAndTintGetter getter, @NotNull final BlockPos position) {
        int color = original.getTintColor(state, getter, position);

        if (VisionHandler.hasVision(type)) {
            // 0x5A is 90 which is roughly the result of 255 * 0.35
            return (color /* Remove alpha */ & 0x00FFFFFF) /* Add custom alpha */ | 0x5A000000;
        }

        return color;
    }
}
