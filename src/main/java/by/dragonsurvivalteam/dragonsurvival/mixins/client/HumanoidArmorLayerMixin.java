package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.armortrim.ArmorTrim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Function;

/** Apply hunter stack alpha change to armor pieces (for human players) */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {
    @Unique
    private static final Function<ResourceLocation, RenderType> dragonSurvival$TRANSLUCENT_ARMOR_CUTOUT_NO_CULL = Util.memoize(texture -> dragonSurvival$createTranslucentArmorCutoutNoCull("translucent_armor_cutout_no_cull", texture, false));

    @Unique
    private static final Function<ResourceLocation, RenderType> dragonSurvival$TRANSLUCENT_ARMOR_DECAL_CUTOUT_NO_CULL = Util.memoize(texture -> dragonSurvival$createTranslucentArmorCutoutNoCull("translucent_armor_decal_cutout_no_cull", texture, true));

    /** Needed because there is no entity context at certain points - not an issue since the game is not multithreaded */
    @Unique private static int dragonSurvival$alpha = HunterHandler.UNMODIFIED;

    @ModifyArg(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/HumanoidArmorLayer;renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V"), index = 4)
    private int dragonSurvival$modifyAlpha(int color, @Local(argsOnly = true) final LivingEntity entity) {
        if (HunterData.hasTransparency(entity)) {
            dragonSurvival$alpha = HunterHandler.calculateAlpha(entity);
            return HunterHandler.applyAlpha(dragonSurvival$alpha, color);
        }

        dragonSurvival$alpha = HunterHandler.UNMODIFIED;
        return color;
    }

    @ModifyArg(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;ILnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private RenderType dragonSurvival$getTranslucentRenderType(final RenderType renderType, @Local(argsOnly = true) final ResourceLocation texture) {
        if (dragonSurvival$alpha != HunterHandler.UNMODIFIED && dragonSurvival$alpha != 1) {
            return dragonSurvival$TRANSLUCENT_ARMOR_CUTOUT_NO_CULL.apply(texture);
        }

        return renderType;
    }

    @ModifyArg(method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private RenderType dragonSurvival$getTranslucentRenderType(final RenderType renderType, @Local(argsOnly = true) final ArmorTrim trim) {
        if (dragonSurvival$alpha != HunterHandler.UNMODIFIED && dragonSurvival$alpha != 1) {
            boolean decal = trim.pattern().value().decal();

            if (decal) {
                return dragonSurvival$TRANSLUCENT_ARMOR_DECAL_CUTOUT_NO_CULL.apply(Sheets.ARMOR_TRIMS_SHEET);
            } else {
                return dragonSurvival$TRANSLUCENT_ARMOR_CUTOUT_NO_CULL.apply(Sheets.ARMOR_TRIMS_SHEET);
            }
        }

        return renderType;
    }

    @WrapOperation(method = "renderTrim(Lnet/minecraft/core/Holder;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V"))
    private void dragonSurvival$renderWithModifiedAlpha(final Model instance, final PoseStack poseStack, final VertexConsumer vertexConsumer, int packedLight, int packedOverlay, final Operation<Void> original) {
        if (dragonSurvival$alpha != HunterHandler.UNMODIFIED && dragonSurvival$alpha != 1) {
            instance.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, HunterHandler.applyAlpha(dragonSurvival$alpha, HunterHandler.UNMODIFIED));
        } else {
            original.call(instance, poseStack, vertexConsumer, packedLight, packedOverlay);
        }

        original.call(instance, poseStack, vertexConsumer, packedLight, packedOverlay);
    }

    @Unique
    private static RenderType.CompositeRenderType dragonSurvival$createTranslucentArmorCutoutNoCull(final String name, final ResourceLocation texture, boolean equalDepthTest) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ARMOR_CUTOUT_NO_CULL_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY) // Enable translucency
                .setOutputState(RenderStateShard.ITEM_ENTITY_TARGET) // Required to see other entities / water through the translucent parts
                .setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                .setDepthTestState(equalDepthTest ? RenderStateShard.EQUAL_DEPTH_TEST : RenderStateShard.LEQUAL_DEPTH_TEST)
                .createCompositeState(true);

        return new RenderType.CompositeRenderType(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 1536, true, false, state);
    }
}
