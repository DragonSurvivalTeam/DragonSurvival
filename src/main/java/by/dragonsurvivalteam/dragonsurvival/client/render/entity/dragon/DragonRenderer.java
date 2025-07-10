package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.compat.ModCheck;
import by.dragonsurvivalteam.dragonsurvival.compat.sophisticatedBackpacks.DragonBackpackRenderLayer;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.Color;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DragonRenderer extends GeoEntityRenderer<DragonEntity> {
    public static final Map<Integer, Map<String, Vec3>> BONE_POSITIONS = new HashMap<>();
    private static final List<String> BONES = List.of("BreathSource");

    private static final Color RENDER_COLOR = Color.ofRGB(255, 255, 255);
    private static final Color TRANSPARENT_RENDER_COLOR = Color.ofRGBA(1, 1, 1, HunterHandler.MIN_ALPHA);

    public ResourceLocation glowTexture;
    public boolean isRenderingLayer;
    public boolean shouldRenderLayers = true;

    private boolean resetNeckVisibility;

    public DragonRenderer(final EntityRendererProvider.Context context, final GeoModel<DragonEntity> model) {
        super(context, model);

        getRenderLayers().add(new DragonGlowLayerRenderer(this));
        getRenderLayers().add(new DragonArmorRenderLayer(this));
        getRenderLayers().add(new DragonItemRenderLayer(this, (bone, animatable) -> {
            if (bone.getName().equals(ClientDragonRenderer.renderItemsInMouth ? "RightItem_jaw" : "RightItem")) {
                return animatable.getMainHandItem();
            } else if (bone.getName().equals(ClientDragonRenderer.renderItemsInMouth ? "LeftItem_jaw" : "LeftItem")) {
                return animatable.getOffhandItem();
            }
            return null;
        }, (bone, animatable) -> null));

        if (ModCheck.isModLoaded(ModCheck.SOPHISTICATED_BACKPACKS)) {
            getRenderLayers().add(new DragonBackpackRenderLayer(this));
        }
    }

    /**
     * Note: Position does not work in first person <br>
     * - GeckoLib cannot update the bone positions iff ClientDragonRenderer#renderInFirstPerson is not enabled <br>
     * - Even if it is enabled the position won't be correct - unsure as to why
     */
    public static Vec3 getBonePosition(final Player player, final String name) {
        DragonEntity dragon = ClientDragonRenderer.getDragon(player);

        if (dragon == null) {
            return Vec3.ZERO;
        }

        Map<String, Vec3> positions = BONE_POSITIONS.get(dragon.getId());

        if (positions == null) {
            return Vec3.ZERO;
        }

        return positions.getOrDefault(name, Vec3.ZERO);
    }

    @Override
    public void preRender(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel model, final MultiBufferSource bufferSource, final VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        Minecraft.getInstance().getProfiler().push("player_dragon");
        Player player = animatable.getPlayer();

        resetNeckVisibility = model.getBone("Neck").map(bone -> {
            if (bone.isHidden()) {
                return false;
            }

            if (animatable.isInInventory || Compat.displayNeck()) {
                return false;
            }

            if (RenderingUtils.isFirstPerson(player)) {
                bone.setHidden(true);
            } else {
                return false;
            }

            return true;
        }).orElse(false);

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
    }

    @Override
    public void postRender(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel model, final MultiBufferSource bufferSource, final VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        super.postRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);

        if (resetNeckVisibility) {
            model.getBone("Neck").ifPresent(bone -> bone.setHidden(false));
            resetNeckVisibility = false;
        }

        // Need to store the positions per entity ourselves
        // Since the model is a singleton, and it stores the bones
        BONES.forEach(name -> model.getBone(name).ifPresent(bone -> {
            Vector3d worldPosition = bone.getWorldPosition();
            Vec3 position = new Vec3(worldPosition.x(), worldPosition.y(), worldPosition.z()).subtract(ClientDragonRenderer.getModelOffset(animatable, 1));
            BONE_POSITIONS.computeIfAbsent(animatable.getId(), key -> new HashMap<>()).put(bone.getName(), position);
        }));

        Minecraft.getInstance().getProfiler().pop();
    }

    @Override
    public void actuallyRender(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel model, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int color) {
        Player player = animatable.getPlayer();

        //noinspection DataFlowIssue -> player is present
        if (player == null || player.isSpectator() || player.isInvisibleTo(Minecraft.getInstance().player)) {
            return;
        }

        poseStack.pushPose();
        setupRender(animatable, player, poseStack, partialTick);

        DragonStateHandler handler = DragonStateProvider.getData(player);
        boolean hasWings = !handler.body().value().canHideWings() || handler.getCurrentStageCustomization().wings;

        for (String boneName : handler.body().value().bonesToHideForToggle()) {
            model.getBone(boneName).ifPresent(bone -> bone.setHidden(!hasWings));
        }

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, color);
        poseStack.popPose();
    }

    private void setupRender(final DragonEntity dragon, final Player player, final PoseStack pose, final float partialTick) {
        MovementData movement = MovementData.getData(player);

        // This is normally used in EntityRenderDispatcher#render but that isn't triggered for 'DragonEntity'
        Vec3 offset = getRenderOffset(dragon, partialTick);
        pose.translate(-offset.x(), -offset.y(), -offset.z());

        pose.mulPose(Axis.YN.rotationDegrees((float) movement.bodyYaw));

        if (ServerFlightHandler.isGliding(player) || (player.isPassenger() && DragonStateProvider.isDragon(player.getVehicle()) && ServerFlightHandler.isGliding((Player) player.getVehicle()))) {
            // Responsible for the pitch (rotating entity downward / upward)
            pose.mulPose(Axis.XN.rotationDegrees(dragon.prevXRot));
            // Responsible for the roll (rotating entity to the side)
            pose.mulPose(Axis.ZP.rotation(dragon.prevZRot));
        }
    }

    @Override // Also used by the layers
    public Color getRenderColor(final DragonEntity animatable, float partialTick, int packedLight) {
        boolean isInvisible = animatable.isInvisible();
        Color color;

        //noinspection DataFlowIssue -> player is not null
        if (isInvisible && !animatable.isInvisibleTo(Minecraft.getInstance().player)) {
            color = TRANSPARENT_RENDER_COLOR;
        } else {
            color = RENDER_COLOR;
        }

        return HunterHandler.modifyAlpha(animatable.getPlayer(), color);
    }

    @Override
    public @NotNull Vec3 getRenderOffset(@NotNull final DragonEntity dragon, final float partialTicks) {
        return ClientDragonRenderer.getModelOffset(dragon, partialTicks);
    }
}