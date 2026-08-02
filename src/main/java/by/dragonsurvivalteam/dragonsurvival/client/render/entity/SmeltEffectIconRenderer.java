package by.dragonsurvivalteam.dragonsurvival.client.render.entity;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderStateBackup;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AttachmentManager;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.joml.Matrix4f;

public class SmeltEffectIconRenderer {
    private static final ResourceLocation ICON_EMPTY = DragonSurvival.res("textures/icons/smelting_effect_empty.png");
    private static final ResourceLocation ICON_FULL = DragonSurvival.res("textures/icons/smelting_effect_full.png");

    private static final int WIDTH = 16;
    private static final int HEIGHT = 8;

    public static void renderIcon(final Entity entity, final PoseStack pose, final double distance) {
        if (distance > 16 * 16) {
            return;
        }

        if (!(entity instanceof ItemEntity item)) {
            return;
        }

        if (entity.isInvisible()) {
            return;
        }

        AttachmentManager.getExistingData(item, DSDataAttachments.ITEM).ifPresent(data -> {
            double progress = data.smeltingProgress;

            if (progress == 0 || progress >= data.smeltingTime) {
                return;
            }

            float percent = (float) (progress / data.smeltingTime);

            RenderSystem.enablePolygonOffset();
            renderIconAboveEntity(entity, ICON_EMPTY, pose, 1);
            // Push this icon slightly above the other, to avoid flickering
            // Depth function cannot be uses since it will cause the icons to render through blocks
            RenderSystem.polygonOffset(-1, -1);
            renderIconAboveEntity(entity, ICON_FULL, pose, percent);
            RenderSystem.polygonOffset(0, 0);
            RenderSystem.disablePolygonOffset();

            // TODO :: add an option to render the item it will smelt into? example how the texture needs to be set up
//            ResourceLocation resource = item.getItem().getItem().builtInRegistryHolder().getKey().location().withPrefix("textures/item/").withSuffix(".png");
        });
    }

    public static void renderIconAboveEntity(final Entity entity, final ResourceLocation icon, final PoseStack pose, final float widthPercentage) {
        RenderStateBackup state = RenderStateBackup.capture();

        float scale = 0.025f;
        float x = -WIDTH / 2f;
        float y = -HEIGHT / 2f - 4;

        pose.pushPose();
        pose.translate(0, entity.getBbHeight() + 0.5, 0);
        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        pose.scale(scale, -scale, scale);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        Matrix4f matrix = pose.last().pose();

        Minecraft.getInstance().getTextureManager().getTexture(icon).setFilter(false, false);
        RenderSystem.setShaderTexture(0, icon);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.vertex(matrix, x, y + HEIGHT, 0).uv(0, 1).color(1, 1, 1, 1f).endVertex();
        // Need to use the percentage for the uv here, otherwise the left side of the icon will have some rendering issues
        buffer.vertex(matrix, x + WIDTH * widthPercentage, y + HEIGHT, 0).uv(widthPercentage, 1).color(1, 1, 1, 1f).endVertex();
        buffer.vertex(matrix, x + WIDTH * widthPercentage, y, 0).uv(widthPercentage, 0).color(1, 1, 1, 1f).endVertex();
        buffer.vertex(matrix, x, y, 0).uv(0, 0).color(1, 1, 1, 1f).endVertex();

        BufferBuilder.RenderedBuffer data = buffer.endOrDiscardIfEmpty();

        if (data != null) {
            BufferUploader.drawWithShader(data);
        }

        pose.popPose();
        state.restore();
    }
}
