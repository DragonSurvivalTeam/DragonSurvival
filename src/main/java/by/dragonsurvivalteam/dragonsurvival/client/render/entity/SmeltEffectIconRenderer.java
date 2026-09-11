package by.dragonsurvivalteam.dragonsurvival.client.render.entity;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ItemData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(Dist.CLIENT)
public class SmeltEffectIconRenderer {
    private static final Identifier ICON_EMPTY = DragonSurvival.res("textures/icons/smelting_effect_empty.png");
    private static final Identifier ICON_FULL = DragonSurvival.res("textures/icons/smelting_effect_full.png");

    private static final int WIDTH = 16;
    private static final int HEIGHT = 8;
    private static final int FULL_BRIGHT = 0x00F000F0;

    @SubscribeEvent
    public static void renderIcons(final RenderLevelStageEvent.AfterOpaqueFeatures event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;

        if (minecraft.player == null || level == null) {
            return;
        }

        Vec3 cameraPosition = event.getLevelRenderState().cameraRenderState.pos;
        float partialTick = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof ItemEntity item) || item.isInvisible() || item.distanceToSqr(cameraPosition) > 16 * 16) {
                continue;
            }

            ItemData data = item.getExistingData(DSDataAttachments.ITEM).orElse(null);

            if (data == null || data.smeltingTime <= 0 || data.smeltingProgress <= 0 || data.smeltingProgress >= data.smeltingTime) {
                continue;
            }

            float percent = (float) (data.smeltingProgress / data.smeltingTime);
            Vec3 position = item.getPosition(partialTick).subtract(cameraPosition);
            PoseStack pose = event.getPoseStack();

            pose.pushPose();
            pose.translate(position.x(), position.y() + item.getBbHeight() + 0.5, position.z());
            pose.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
            pose.scale(0.025f, -0.025f, 0.025f);

            // The fill texture is transparent around its edge, so retain the entire outline underneath.
            renderIcon(pose, bufferSource, ICON_EMPTY, 0, 1);
            // Move the fill toward the camera to avoid depth fighting with the background.
            pose.translate(0, 0, 0.01f);
            renderIcon(pose, bufferSource, ICON_FULL, 0, percent);

            pose.popPose();
        }

        bufferSource.endBatch(RenderTypes.text(ICON_EMPTY));
        bufferSource.endBatch(RenderTypes.text(ICON_FULL));
    }

    private static void renderIcon(final PoseStack pose, final MultiBufferSource bufferSource, final Identifier icon, final float start, final float end) {
        float x = -WIDTH / 2f;
        float y = -HEIGHT / 2f - 4;
        VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.text(icon));
        PoseStack.Pose lastPose = pose.last();

        buffer.addVertex(lastPose, x + WIDTH * start, y + HEIGHT, 0).setColor(ARGB.white(255)).setUv(start, 1).setLight(FULL_BRIGHT);
        buffer.addVertex(lastPose, x + WIDTH * end, y + HEIGHT, 0).setColor(ARGB.white(255)).setUv(end, 1).setLight(FULL_BRIGHT);
        buffer.addVertex(lastPose, x + WIDTH * end, y, 0).setColor(ARGB.white(255)).setUv(end, 0).setLight(FULL_BRIGHT);
        buffer.addVertex(lastPose, x + WIDTH * start, y, 0).setColor(ARGB.white(255)).setUv(start, 0).setLight(FULL_BRIGHT);
    }
}
