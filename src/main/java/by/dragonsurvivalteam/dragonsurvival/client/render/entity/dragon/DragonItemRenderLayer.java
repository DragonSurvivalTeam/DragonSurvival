package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.compat.bettercombat.BetterCombat;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import java.util.Optional;
import java.util.function.BiFunction;

public class DragonItemRenderLayer extends BlockAndItemGeoLayer<DragonEntity> {
    public DragonItemRenderLayer(GeoRenderer<DragonEntity> renderer, BiFunction<GeoBone, DragonEntity, ItemStack> stackForBone, BiFunction<GeoBone, DragonEntity, BlockState> blockForBone) {
        super(renderer, stackForBone, blockForBone);
    }

    @Override
    protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, DragonEntity animatable) {
        if (bone.getName().equals("RightItem")) {
            return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        }
        if (bone.getName().equals("LeftItem")) {
            return ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
        }

        return ItemDisplayContext.GROUND;
    }

    @Override
    protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, DragonEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
        if (BetterCombat.isAttacking(animatable.getPlayer())) {
            return;
        }

        if (ClientDragonRenderer.renderHeldItem && (animatable.getPlayer() != Minecraft.getInstance().player || !Minecraft.getInstance().options.getCameraType().isFirstPerson())) {
            poseStack.pushPose();

            if (bone.getName().equals("RightItem")) {
                Quaternionf rotation = new Quaternionf();
                rotation.rotateY((float) Math.toRadians(90));
                rotation.rotateX((float) Math.toRadians(60));
                poseStack.rotateAround(rotation, 0, 0, 0);
                poseStack.scale(0.75F, 0.75F, 0.75F);
            } else if (bone.getName().equals("LeftItem")) {
                Quaternionf rotation = new Quaternionf();
                rotation.rotateZ((float) Math.toRadians(90));
                rotation.rotateY((float) Math.toRadians(90));
                rotation.rotateX((float) Math.toRadians(-120d));
                poseStack.rotateAround(rotation, 0, 0, 0);
                poseStack.scale(0.75F, 0.75F, 0.75F);
            }

            // Isolate the outline buffer for item rendering to prevent the glow
            // pass from interfering with the dragon body visibility
            MultiBufferSource.BufferSource[] itemOutlineBufHolder = new MultiBufferSource.BufferSource[1];
            MultiBufferSource effectiveSource = bufferSource;

            if (bufferSource instanceof OutlineBufferSource outline) {
                MultiBufferSource.BufferSource normalBuf = outline.bufferSource;
                int color = FastColor.ARGB32.color(outline.teamA, outline.teamR, outline.teamG, outline.teamB);
                MultiBufferSource.BufferSource itemOutlineBuf = MultiBufferSource.immediate(new ByteBufferBuilder(1536));
                itemOutlineBufHolder[0] = itemOutlineBuf;

                effectiveSource = new MultiBufferSource() {
                    @Override
                    public VertexConsumer getBuffer(RenderType rt) {
                        if (rt.isOutline()) {
                            return itemOutlineBuf.getBuffer(rt);
                        }
                        VertexConsumer normal = normalBuf.getBuffer(rt);
                        Optional<RenderType> outlineVariant = rt.outline();
                        if (outlineVariant.isPresent()) {
                            VertexConsumer outConsumer = itemOutlineBuf.getBuffer(outlineVariant.get());
                            return VertexMultiConsumer.create(new VertexConsumer() {
                                public VertexConsumer addVertex(float x, float y, float z) {
                                    outConsumer.addVertex(x, y, z).setColor(color);
                                    return this;
                                }
                                public VertexConsumer setColor(int r, int g, int b, int a) { return this; }
                                public VertexConsumer setUv(float u, float v) { outConsumer.setUv(u, v); return this; }
                                public VertexConsumer setUv1(int u, int v) { return this; }
                                public VertexConsumer setUv2(int u, int v) { return this; }
                                public VertexConsumer setNormal(float x, float y, float z) { return this; }
                            }, normal);
                        }
                        return normal;
                    }
                };
            }

            super.renderStackForBone(poseStack, bone, stack, animatable, effectiveSource, partialTick, packedLight, packedOverlay);

            if (itemOutlineBufHolder[0] != null) {
                itemOutlineBufHolder[0].endBatch();
            }

            poseStack.popPose();
        }
    }
}
