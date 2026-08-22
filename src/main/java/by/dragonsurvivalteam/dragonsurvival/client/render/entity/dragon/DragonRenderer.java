package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.EntityScale;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import by.dragonsurvivalteam.dragonsurvival.compat.do_a_barrel_roll.DoABarrelRollCompat;
import by.dragonsurvivalteam.dragonsurvival.compat.sophisticatedBackpacks.DragonBackpackRenderLayer;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClimbableData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.DragonRidingHandler;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.core.object.Color;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DragonRenderer extends GeoEntityRenderer<DragonEntity> {
    public static final Map<Integer, Map<String, Vec3>> BONE_POSITIONS = new HashMap<>();
    private static final Map<Integer, Map<String, Vec3>> BONE_OFFSETS = new HashMap<>();
    private static final Map<Integer, Map<String, Quaternionf>> BONE_ROTATIONS = new HashMap<>();
    private static final Map<Integer, Map<String, Long>> BONE_UPDATE_TICKS = new HashMap<>();

    public static final String BREATH_SOURCE = "BreathSource";
    private static final List<String> BONES = List.of(BREATH_SOURCE, DragonRidingHandler.MOUNTING_BONE);

    private static final Color RENDER_COLOR = Color.ofRGB(255, 255, 255);
    private static final Color TRANSPARENT_RENDER_COLOR = Color.ofRGBA(1, 1, 1, HunterHandler.MIN_ALPHA);

    // The bone-only traversal never emits vertices, but GeckoLib still requires a buffer.
    private static final VertexConsumer BONE_CALCULATION_BUFFER = new BufferBuilder(256);
    private static final MultiBufferSource BONE_CALCULATION_BUFFERS = renderType -> BONE_CALCULATION_BUFFER;

    public ResourceLocation glowTexture;

    private boolean resetNeckVisibility;
    private boolean calculatingBonesOnly;
    private final Set<String> updatedTrackedBones = new HashSet<>();

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

        if (ModID.SOPHISTICATED_BACKPACKS.isLoaded()) {
            getRenderLayers().add(new DragonBackpackRenderLayer(this));
        }

    }

    public static @Nullable Vec3 getMountingBonePositionCorrection(final Player rider, float partialTick) {
        if (!(rider.getVehicle() instanceof Player mount) || !DragonStateProvider.isDragon(mount)) {
            return null;
        }

        DragonStateHandler handler = DragonStateProvider.getData(mount);
        if (handler.body().value().noDragonModelRendering()) {
            return null;
        }

        Vec3 mountingOffset = DragonRenderer.getBoneOffsetOrNull(mount, DragonRidingHandler.MOUNTING_BONE);
        if (mountingOffset == null) {
            return null;
        }

        Vec3 pivot = DragonRidingHandler.getVehicleAttachmentPoint(rider, mount);
        Vec3 targetRiderPosition = mount.getPosition(partialTick).add(mountingOffset).subtract(pivot);
        return targetRiderPosition.subtract(rider.getPosition(partialTick));
    }

    public static @Nullable Vec3 getBonePositionOrNull(final Player player, final String name) {
        DragonEntity dragon = getDragonWithFreshBoneData(player, name);

        if (dragon == null) {
            return null;
        }

        Map<String, Vec3> positions = BONE_POSITIONS.get(dragon.getId());

        if (positions == null) {
            return null;
        }

        return positions.get(name);
    }

    public static @Nullable Vec3 getBoneOffsetOrNull(final Player player, final String name) {
        DragonEntity dragon = getDragonWithFreshBoneData(player, name);

        if (dragon == null) {
            return null;
        }

        Map<String, Vec3> offsets = BONE_OFFSETS.get(dragon.getId());

        if (offsets == null) {
            return null;
        }

        return offsets.get(name);
    }

    public static boolean isBonePositionFresh(final Player player, final String name) {
        DragonEntity dragon = ClientDragonRenderer.getDragon(player);

        return dragon != null && hasFreshBoneData(dragon, name);
    }

    private static @Nullable DragonEntity getDragonWithFreshBoneData(final Player player, final String name) {
        DragonEntity dragon = ClientDragonRenderer.getDragon(player);

        if (dragon == null || !hasFreshBoneData(dragon, name)) {
            ClientDragonRenderer.updateDragonBoneData(player);
            dragon = ClientDragonRenderer.getDragon(player);
        }

        return dragon;
    }

    private static boolean hasFreshBoneData(final DragonEntity dragon, final String name) {
        Map<String, Long> updateTicks = BONE_UPDATE_TICKS.get(dragon.getId());
        Long updateTick = updateTicks == null ? null : updateTicks.get(name);

        if (updateTick == null) {
            return false;
        }

        long age = dragon.level().getGameTime() - updateTick;
        return age == 0;
    }

    public static @Nullable Quaternionf getBoneRotationOrNull(final Player player, final String name) {
        DragonEntity dragon = getDragonWithFreshBoneData(player, name);

        if (dragon == null) {
            return null;
        }

        Map<String, Quaternionf> rotations = BONE_ROTATIONS.get(dragon.getId());

        if (rotations == null) {
            return null;
        }

        Quaternionf rotation = rotations.get(name);
        return rotation == null ? null : new Quaternionf(rotation);
    }

    public static void removeBoneData(int dragonId) {
        BONE_POSITIONS.remove(dragonId);
        BONE_OFFSETS.remove(dragonId);
        BONE_ROTATIONS.remove(dragonId);
        BONE_UPDATE_TICKS.remove(dragonId);
    }

    public static void clearBoneData() {
        BONE_POSITIONS.clear();
        BONE_OFFSETS.clear();
        BONE_ROTATIONS.clear();
        BONE_UPDATE_TICKS.clear();
    }

    public void calculateBoneTransforms(final DragonEntity dragon, float partialTick) {
        BakedGeoModel model = getGeoModel().getBakedModel(getGeoModel().getModelResource(dragon, this));
        PoseStack poseStack = new PoseStack();
        RenderType renderType = getRenderType(dragon, getTextureLocation(dragon), BONE_CALCULATION_BUFFERS, partialTick);
        DragonEntity previousAnimatable = animatable;
        boolean previousCalculationState = calculatingBonesOnly;
        Set<String> previouslyUpdatedTrackedBones = Set.copyOf(updatedTrackedBones);
        boolean previousNeckState = resetNeckVisibility;

        calculatingBonesOnly = true;
        animatable = dragon;

        try {
            preRender(poseStack, dragon, model, BONE_CALCULATION_BUFFERS, BONE_CALCULATION_BUFFER, false, partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    RENDER_COLOR.getRedFloat(), RENDER_COLOR.getGreenFloat(), RENDER_COLOR.getBlueFloat(), RENDER_COLOR.getAlphaFloat());
            actuallyRender(poseStack, dragon, model, renderType, BONE_CALCULATION_BUFFERS, BONE_CALCULATION_BUFFER, false, partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    RENDER_COLOR.getRedFloat(), RENDER_COLOR.getGreenFloat(), RENDER_COLOR.getBlueFloat(), RENDER_COLOR.getAlphaFloat());
            postRender(poseStack, dragon, model, BONE_CALCULATION_BUFFERS, BONE_CALCULATION_BUFFER, false, partialTick, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
                    RENDER_COLOR.getRedFloat(), RENDER_COLOR.getGreenFloat(), RENDER_COLOR.getBlueFloat(), RENDER_COLOR.getAlphaFloat());
        } finally {
            animatable = previousAnimatable;
            calculatingBonesOnly = previousCalculationState;
            updatedTrackedBones.clear();
            updatedTrackedBones.addAll(previouslyUpdatedTrackedBones);
            resetNeckVisibility = previousNeckState;
        }
    }

    @Override
    public void preRender(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel model, final MultiBufferSource bufferSource, final VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (!isReRender) {
            updatedTrackedBones.clear();
        }

        Minecraft.getInstance().getProfiler().push("player_dragon");
        Player player = animatable.getPlayer();

        BONES.forEach(name -> model.getBone(name).ifPresent(bone -> bone.setTrackingMatrices(true)));

        resetNeckVisibility = model.getBone("Neck").map(bone -> {
            if (bone.isHidden()) {
                return false;
            }

            if (animatable.isInInventory || calculatingBonesOnly || Compat.displayNeck()) {
                return false;
            }

            if (RenderingUtils.isFirstPerson(player)) {
                bone.setHidden(true);
            } else {
                return false;
            }

            return true;
        }).orElse(false);

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void postRender(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel model, final MultiBufferSource bufferSource, final VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.postRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        if (resetNeckVisibility) {
            model.getBone("Neck").ifPresent(bone -> bone.setHidden(false));
            resetNeckVisibility = false;
        }

        if (!animatable.isInInventory) {
            // Need to store the positions per entity ourselves since the model and its bones are singletons.
            Map<String, Vec3> positions = BONE_POSITIONS.computeIfAbsent(animatable.getId(), key -> new HashMap<>());
            Map<String, Vec3> offsets = BONE_OFFSETS.computeIfAbsent(animatable.getId(), key -> new HashMap<>());
            Map<String, Quaternionf> rotations = BONE_ROTATIONS.computeIfAbsent(animatable.getId(), key -> new HashMap<>());
            Map<String, Long> updateTicks = BONE_UPDATE_TICKS.computeIfAbsent(animatable.getId(), key -> new HashMap<>());

            BONES.forEach(name -> model.getBone(name).ifPresentOrElse(bone -> {
                if (!updatedTrackedBones.contains(name)) {
                    return;
                }

                Vector3d worldPosition = bone.getWorldPosition();
                Vec3 position = new Vec3(worldPosition.x(), worldPosition.y(), worldPosition.z()).subtract(getModelOffset(animatable, 1));
                positions.put(bone.getName(), position);
                offsets.put(bone.getName(), position.subtract(animatable.position()));
                updateTicks.put(bone.getName(), animatable.level().getGameTime());

                Quaternionf rotation = calculateBoneTilt(bone.getWorldSpaceMatrix());
                if (rotation == null) {
                    rotations.remove(name);
                } else {
                    rotations.put(bone.getName(), rotation);
                }
            }, () -> {
                positions.remove(name);
                offsets.remove(name);
                rotations.remove(name);
                updateTicks.put(name, animatable.level().getGameTime());
            }));
        }

        Minecraft.getInstance().getProfiler().pop();
    }

    @Override
    public void actuallyRender(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel model, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        Player player = animatable.getPlayer();

        //noinspection DataFlowIssue -> player is present
        if (player == null || player.isSpectator() || !calculatingBonesOnly && player.isInvisibleTo(Minecraft.getInstance().player)) {
            return;
        }

        poseStack.pushPose();
        setupRender(animatable, player, poseStack, partialTick);
        float scale = EntityScale.get(animatable);
        poseStack.scale(scale, scale, scale);

        DragonStateHandler handler = DragonStateProvider.getData(player);
        boolean hasWings = !handler.body().value().canHideWings() || handler.getCurrentStageCustomization().wings;

        for (String boneName : handler.body().value().bonesToHideForToggle()) {
            model.getBone(boneName).ifPresent(bone -> bone.setHidden(!hasWings));
        }

        super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);

        // If a body refresh was requested, all the animations will have been reset once we are post-render
        handler.refreshBody = false;

        poseStack.popPose();
    }

    @Override
    public void renderRecursively(final PoseStack poseStack, final DragonEntity animatable, final GeoBone bone, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        if (!calculatingBonesOnly) {
            super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
            if (bone.isTrackingMatrices()) {
                updatedTrackedBones.add(bone.getName());
            }
            return;
        }

        poseStack.pushPose();
        RenderUtils.translateMatrixToBone(poseStack, bone);
        RenderUtils.translateToPivotPoint(poseStack, bone);
        RenderUtils.rotateMatrixAroundBone(poseStack, bone);
        RenderUtils.scaleMatrixForBone(poseStack, bone);

        if (bone.isTrackingMatrices()) {
            Matrix4f poseState = new Matrix4f(poseStack.last().pose());
            Matrix4f localMatrix = RenderUtils.invertAndMultiplyMatrices(poseState, entityRenderTranslations);

            bone.setModelSpaceMatrix(RenderUtils.invertAndMultiplyMatrices(poseState, modelRenderTranslations));
            bone.setLocalSpaceMatrix(RenderUtils.translateMatrix(localMatrix, getRenderOffset(animatable, 1).toVector3f()));
            bone.setWorldSpaceMatrix(RenderUtils.translateMatrix(new Matrix4f(localMatrix), animatable.position().toVector3f()));
            updatedTrackedBones.add(bone.getName());
        }

        RenderUtils.translateAwayFromPivotPoint(poseStack, bone);
        renderChildBones(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        poseStack.popPose();
    }

    private void setupRender(final DragonEntity dragon, final Player player, final PoseStack pose, final float partialTick) {
        MovementData movement = MovementData.getData(player);
        boolean doABarrelRollActive = DoABarrelRollCompat.isActive(player);

        // This is normally used in 'EntityRenderDispatcher#render', but that isn't triggered for 'DragonEntity'
        Vec3 offset = getRenderOffset(dragon, partialTick);
        pose.translate(-offset.x(), -offset.y(), -offset.z());

        float bodyYaw = doABarrelRollActive ? player.getViewYRot(partialTick) : (float) movement.bodyYaw;
        pose.mulPose(Axis.YN.rotationDegrees(bodyYaw));

        if (ServerFlightHandler.isGliding(player)) {
            // Responsible for the pitch (rotating entity downward / upward)
            float pitch = doABarrelRollActive ? -player.getViewXRot(partialTick) : dragon.prevXRot;
            pose.mulPose(Axis.XN.rotationDegrees(pitch));
            // Responsible for the roll (rotating entity to the side)
            float roll = doABarrelRollActive ? DoABarrelRollCompat.getRollRadians(player, partialTick) : dragon.prevZRot;
            pose.mulPose(Axis.ZP.rotation(roll));
        }

        ClimbableData climbData = player.getExistingData(DSDataAttachments.CLIMBABLE_DATA).orElse(null);

        if (climbData != null && climbData.isCeilingClimbing()) {
            pose.mulPose(Axis.XP.rotationDegrees(-90));
            // Need to invert the facing direction for movement since the model is inverted
            pose.mulPose(Axis.ZP.rotationDegrees(-180));
        }
    }

    static @Nullable Quaternionf calculateBoneTilt(final Matrix4fc boneTransform) {
        Vector3f up = boneTransform.transformDirection(new Vector3f(0, 1, 0));
        float lengthSquared = up.lengthSquared();

        if (!Float.isFinite(lengthSquared) || lengthSquared < 1.0E-8F) {
            return null;
        }

        up.div((float) Math.sqrt(lengthSquared));
        Quaternionf rotation = new Quaternionf().rotationTo(0, 1, 0, up.x(), up.y(), up.z());

        if (!Float.isFinite(rotation.x()) || !Float.isFinite(rotation.y()) || !Float.isFinite(rotation.z()) || !Float.isFinite(rotation.w())) {
            return null;
        }

        return rotation.normalize();
    }

    private Vec3 getModelOffset(final DragonEntity dragon, float partialTicks) {
        Player player = dragon.getPlayer();

        if (player == null) {
            return Vec3.ZERO;
        }

        float angle = -(float) MovementData.getData(player).bodyYaw * ((float) Math.PI / 180);
        float x = Mth.sin(angle);
        float z = Mth.cos(angle);

        DragonStateHandler handler = DragonStateProvider.getData(player);
        float scale = (float) handler.getVisualScale(player, partialTicks) * (float) handler.body().value().scalingProportions().scaleMultiplier();

        return new Vec3(x * scale, 0, z * scale);
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
        return getModelOffset(dragon, partialTicks);
    }
}
