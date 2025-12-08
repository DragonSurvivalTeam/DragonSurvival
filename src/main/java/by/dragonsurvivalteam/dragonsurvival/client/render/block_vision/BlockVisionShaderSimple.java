package by.dragonsurvivalteam.dragonsurvival.client.render.block_vision;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.BlockVisionHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.GlStateBackup;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@EventBusSubscriber(value = Dist.CLIENT)
public class BlockVisionShaderSimple {
    private static ShaderInstance shader;
    private static GlStateBackup backup;

    public static void beginBatch() {
        backup = new GlStateBackup();
        RenderSystem.backupGlState(backup);
    }

    public static void render(final BlockVisionHandler.Data data, final PoseStack pose, final int colorARGB) {
        prepare();

        // Prepare per-vertex color (semi-transparent)
        int alpha = FastColor.ARGB32.alpha(colorARGB);
        int red = FastColor.ARGB32.red(colorARGB);
        int green = FastColor.ARGB32.green(colorARGB);
        int blue = FastColor.ARGB32.blue(colorARGB);

        BlockPos position = BlockPos.containing(data.x(), data.y(), data.z());
        Level level = Objects.requireNonNull(Minecraft.getInstance().level);

        pose.pushPose();
        // Apply the randomized offset some blocks can have to their position
        Vec3 offset = data.state().getOffset(level, position);
        pose.translate(offset.x, offset.y, offset.z);
        PoseStack.Pose lastPose = pose.last();
        pose.popPose();

        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(data.state());
        ModelData modelData = model.getModelData(level, position, data.state(), ModelData.EMPTY);

        RandomSource random = RandomSource.create();
        long seed = data.state().getSeed(position);
        random.setSeed(seed);

        for (RenderType type : model.getRenderTypes(data.state(), random, modelData)) {
            VertexConsumer buffer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(type);

            // Unculled faces
            putData(buffer, random, seed, model.getQuads(data.state(), null, random, modelData, type), lastPose, red, green, blue, alpha);

            // Culled faces
            for (Direction direction : Direction.values()) {
                putData(buffer, random, seed, model.getQuads(data.state(), direction, random, modelData, type), lastPose, red, green, blue, alpha);
            }
        }
    }

    private static void putData(final VertexConsumer buffer, final RandomSource rand, final long seed, final List<BakedQuad> model, final PoseStack.Pose lastPose, final int red, final int green, final int blue, final int alpha) {
        rand.setSeed(seed);

        for (BakedQuad quad : model) {
            buffer.putBulkData(lastPose, quad, red / 255f, green / 255f, blue / 255f, alpha / 255f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        }
    }

    public static void endBatch() {
        Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
        RenderSystem.restoreGlState(backup);
        shader.clear();
    }

    @SubscribeEvent
    public static void registerShaders(final RegisterShadersEvent event) throws IOException {
        // Register shader with BLOCK format to align with BufferBuilder data from putBulkData
        event.registerShader(new ShaderInstance(event.getResourceProvider(), DragonSurvival.res("block_vision_simple"), DefaultVertexFormat.BLOCK), instance -> shader = instance);
    }

    @SuppressWarnings("DataFlowIssue") // Shader variables are present
    private static void prepare() {
        RenderSystem.setShader(() -> shader);
        shader.getUniform("ProjMat").set(RenderSystem.getProjectionMatrix());
        // Use the current model-view matrix (camera transform) so world-space vertices are positioned correctly
        shader.getUniform("ModelViewMat").set(RenderSystem.getModelViewMatrix());
        shader.apply();

        RenderSystem.enableDepthTest();
        // Emulate vanilla cutout state: no blending and write depth
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        // Don't render both sides of transparent blocks (like plants)
        RenderSystem.enableCull();
        RenderSystem.enablePolygonOffset();
        // Prevents z-fighting issues
        RenderSystem.polygonOffset(-1, -1);
        //noinspection deprecation -> ignore
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
    }
}
