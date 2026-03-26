package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.client.render.VisionHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.NotNull;

// FIXME
public class ClientFluidTypeExtensionsWrapper implements IClientFluidTypeExtensions {
//    private final IClientFluidTypeExtensions original;
//    private final VisionHandler.VisionType type;
//
//    public ClientFluidTypeExtensionsWrapper(final IClientFluidTypeExtensions original, final VisionHandler.VisionType type) {
//        this.original = original;
//        this.type = type;
//    }
//
//    @Override
//    public @NotNull Identifier getStillTexture() {
//        return original.getStillTexture();
//    }
//
//    @Override
//    public @NotNull Identifier getFlowingTexture() {
//        return original.getFlowingTexture();
//    }
//
//    @Override
//    public Identifier getOverlayTexture() {
//        return original.getOverlayTexture();
//    }
//
//    @Override
//    public Identifier getRenderOverlayTexture(@NotNull final Minecraft minecraft) {
//        return original.getRenderOverlayTexture(minecraft);
//    }
//
//    @Override
//    public int getTintColor() {
//        int color = original.getTintColor();
//
//        if (VisionHandler.hasVision(type)) {
//            // 0x5A is 90 which is roughly the result of 255 * 0.35
//            return (color /* Remove alpha */ & 0x00FFFFFF) /* Add custom alpha */ | 0x5A000000;
//        }
//
//        return color;
//    }
//
//    @Override
//    public int getTintColor(@NotNull final FluidState state, @NotNull final BlockAndTintGetter getter, @NotNull final BlockPos position) {
//        int color = original.getTintColor(state, getter, position);
//
//        if (VisionHandler.hasVision(type)) {
//            // 0x5A is 90 which is roughly the result of 255 * 0.35
//            return (color /* Remove alpha */ & 0x00FFFFFF) /* Add custom alpha */ | 0x5A000000;
//        }
//
//        return color;
//    }
}
