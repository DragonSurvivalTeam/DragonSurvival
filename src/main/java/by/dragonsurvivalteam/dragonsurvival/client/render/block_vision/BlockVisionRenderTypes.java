package by.dragonsurvivalteam.dragonsurvival.client.render.block_vision;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;

/**
 * This mostly exists due to the cutout render type, otherwise it will render full planes instead of the proper texture </br>
 * (Only happens when 'Iris' is not present - likely due to its heavy adjustments to the MC rendering pipeline) </br>
 * If we just use the render type as-is, it will not make use of our custom shader
 */
public final class BlockVisionRenderTypes {
    private static RenderType CUTOUT;
    private static RenderType TRANSLUCENT;

    private BlockVisionRenderTypes() {}

    public static RenderType blockVisionCutout() {
        if (CUTOUT == null) {
            //noinspection deprecation -> ignore
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(BlockVisionShaderSimple::getShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(true);

            CUTOUT = RenderType.create(
                    "dragonsurvival:block_vision_cutout",
                    DefaultVertexFormat.BLOCK,
                    VertexFormat.Mode.QUADS,
                    4194304, // Size of RenderType#SOLID
                    false,
                    true,
                    state
            );
        }

        return CUTOUT;
    }

    public static RenderType blockVisionTranslucent() {
        if (TRANSLUCENT == null) {
            //noinspection deprecation -> ignore
            RenderType.CompositeState state = RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(BlockVisionShaderSimple::getShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(true);

            TRANSLUCENT = RenderType.create(
                    "dragonsurvival:block_vision_translucent",
                    DefaultVertexFormat.BLOCK,
                    VertexFormat.Mode.QUADS,
                    786432, // Size of RenderType#CUTOUT
                    false,
                    true,
                    state
            );
        }

        return TRANSLUCENT;
    }
}
