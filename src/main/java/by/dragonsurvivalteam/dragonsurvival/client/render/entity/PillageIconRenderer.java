package by.dragonsurvivalteam.dragonsurvival.client.render.entity;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.LeaderEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(Dist.CLIENT)
public class PillageIconRenderer {
    private static final Identifier ICON = DragonSurvival.res("textures/icons/pillage_icon.png");
    private static final int SIZE = 16;
    private static final int FULL_BRIGHT = 0x00F000F0;

    @SubscribeEvent
    public static void renderIcons(final RenderLevelStageEvent.AfterOpaqueFeatures event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;

        if (player == null || level == null || ServerConfig.MAX_RENDER_DISTANCE == 0) {
            return;
        }

        Vec3 cameraPosition = event.getLevelRenderState().cameraRenderState.pos;
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();

        for (Entity entity : level.entitiesForRendering()) {
            renderIcon(entity, event.getPoseStack(), cameraPosition, player, bufferSource);
        }

        bufferSource.endBatch(RenderTypes.text(ICON));
    }

    public static void renderIcon(final Entity entity, final PoseStack pose, final Vec3 cameraPosition, final LocalPlayer player, final MultiBufferSource bufferSource) {
        if (!(entity instanceof LivingEntity livingEntity) || entity.isInvisible()) {
            return;
        }

        double distance = entity.distanceToSqr(cameraPosition);

        if (distance > ServerConfig.MAX_RENDER_DISTANCE * ServerConfig.MAX_RENDER_DISTANCE || !shouldRenderIcon(livingEntity, player)) {
            return;
        }

        float scale = 0.025f * livingEntity.getScale();
        float x = -SIZE / 2f;
        float y = -SIZE / 2f - 4;

        pose.pushPose();
        pose.translate(entity.getX() - cameraPosition.x(), entity.getY() - cameraPosition.y() + entity.getBbHeight() + 0.5, entity.getZ() - cameraPosition.z());
        pose.mulPose(Minecraft.getInstance().gameRenderer.getMainCamera().rotation());
        pose.scale(scale, -scale, scale);

        VertexConsumer buffer = bufferSource.getBuffer(RenderTypes.text(ICON));
        PoseStack.Pose lastPose = pose.last();

        buffer.addVertex(lastPose, x, y + SIZE, 0).setColor(ARGB.white(255)).setUv(0, 1).setLight(FULL_BRIGHT);
        buffer.addVertex(lastPose, x + SIZE, y + SIZE, 0).setColor(ARGB.white(255)).setUv(1, 1).setLight(FULL_BRIGHT);
        buffer.addVertex(lastPose, x + SIZE, y, 0).setColor(ARGB.white(255)).setUv(1, 0).setLight(FULL_BRIGHT);
        buffer.addVertex(lastPose, x, y, 0).setColor(ARGB.white(255)).setUv(0, 0).setLight(FULL_BRIGHT);

        pose.popPose();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean shouldRenderIcon(final LivingEntity entity, final LocalPlayer player) {
        if (entity.isBaby()) {
            return false;
        }

        if (entity instanceof Villager villager) {
            if (!(villager instanceof LeaderEntity)
                && (villager.getVillagerData().profession().is(VillagerProfession.NONE) || villager.getVillagerData().profession().is(VillagerProfession.NITWIT))) {
                return false;
            }

            return EntityStateHandler.canPillage(villager, player);
        }

        return false;
    }
}
