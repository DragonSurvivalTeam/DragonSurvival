package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.VisionHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Adjust fluid model layer/tint for lava and water vision without touching FluidRenderer internals. */
@Mixin(FluidStateModelSet.class)
public abstract class ItemBlockRenderTypesMixin {
    private static final float VISION_ALPHA = 0.35F;

    @ModifyReturnValue(method = "get", at = @At("RETURN"))
    private FluidModel dragonSurvival$handleLavaVision(final FluidModel model, final FluidState fluidState) {
        boolean lavaVision = VisionHandler.hasLavaVision() && fluidState.is(FluidTags.LAVA);
        boolean waterVision = VisionHandler.hasWaterVision() && fluidState.is(FluidTags.WATER);

        if (!lavaVision && !waterVision) {
            return model;
        }

        ChunkSectionLayer layer = lavaVision ? ChunkSectionLayer.TRANSLUCENT : model.layer();
        FluidTintSource tintSource = withVisionAlpha(model.fluidTintSource());
        return new FluidModel(layer, model.stillMaterial(), model.flowingMaterial(), model.overlayMaterial(), tintSource, model.customRenderer());
    }

    private static @Nullable FluidTintSource withVisionAlpha(final @Nullable FluidTintSource original) {
        return new FluidTintSource() {
            @Override
            public int color(final FluidState fluidState) {
                return applyVisionAlpha(original != null ? original.color(fluidState) : -1);
            }

            @Override
            public int color(final BlockState blockState) {
                return applyVisionAlpha(original != null ? original.color(blockState) : -1);
            }

            @Override
            public int colorInWorld(final FluidState fluidState, final BlockState blockState, final BlockAndTintGetter level, final BlockPos pos) {
                return applyVisionAlpha(original != null ? original.colorInWorld(fluidState, blockState, level, pos) : -1);
            }

            @Override
            public int colorInWorld(final BlockState blockState, final BlockAndTintGetter level, final BlockPos pos) {
                return applyVisionAlpha(original != null ? original.colorInWorld(blockState, level, pos) : -1);
            }

            @Override
            public int colorAsTerrainParticle(final BlockState blockState, final BlockAndTintGetter level, final BlockPos pos) {
                return applyVisionAlpha(original != null ? original.colorAsTerrainParticle(blockState, level, pos) : -1);
            }
        };
    }

    private static int applyVisionAlpha(final int color) {
        return ARGB.color(ARGB.as8BitChannel(VISION_ALPHA), ARGB.transparent(color));
    }
}
