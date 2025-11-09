package by.dragonsurvivalteam.dragonsurvival.client.render.entity;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.neoforge.client.GlStateBackup;
import org.joml.Matrix4f;

public class PillageIconRenderer {
    private static final ResourceLocation ICON = DragonSurvival.res("textures/icons/pillage_icon.png");
    private static final int SIZE = 16;

    public static void renderIcon(final Entity entity, final PoseStack pose, final double distance) {
        if (ServerConfig.MAX_RENDER_DISTANCE == 0 || distance > ServerConfig.MAX_RENDER_DISTANCE * ServerConfig.MAX_RENDER_DISTANCE) {
            return;
        }

        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }

        if (entity.isInvisible()) {
            return;
        }

        if (!shouldRenderIcon(livingEntity)) {
            return;
        }

        GlStateBackup state = new GlStateBackup();
        RenderSystem.backupGlState(state);

        float scale = 0.025f * livingEntity.getScale();
        float x = -SIZE / 2f;
        float y = -SIZE / 2f - 4;

        pose.pushPose();
        pose.translate(0, entity.getBbHeight() + 0.5, 0);
        pose.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        pose.scale(scale, -scale, scale);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        Matrix4f matrix = pose.last().pose();

        Minecraft.getInstance().getTextureManager().getTexture(ICON).setFilter(false, false);
        RenderSystem.setShaderTexture(0, ICON);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);

        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buffer.addVertex(matrix, x, y + SIZE, 0).setUv(0, 1).setColor(1, 1, 1, 1f);
        buffer.addVertex(matrix, x + SIZE, y + SIZE, 0).setUv(1, 1).setColor(1, 1, 1, 1f);
        buffer.addVertex(matrix, x + SIZE, y, 0).setUv(1, 0).setColor(1, 1, 1, 1f);
        buffer.addVertex(matrix, x, y, 0).setUv(0, 0).setColor(1, 1, 1, 1f);

        MeshData data = buffer.build();

        if (data != null) {
            BufferUploader.drawWithShader(data);
        }

        pose.popPose();
        RenderSystem.restoreGlState(state);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // ignore
    private static boolean shouldRenderIcon(final LivingEntity entity) {
        if (entity.isBaby()) {
            return false;
        }

        if (entity instanceof Villager villager) {
            // Cannot check the offers on the client-side
            VillagerProfession profession = villager.getVillagerData().getProfession();

            if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
                return false;
            }

            //noinspection DataFlowIssue -> player is not null
            return EntityStateHandler.canPillage(villager, Minecraft.getInstance().player);
        }

        return false;
    }
}
