package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

/** Apply hunter stack alpha change to armor pieces (for human players) */
@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin {
    @Unique private static final Function<ResourceLocation, RenderType> dragonSurvival$TRANSLUCENT_ARMOR_CUTOUT_NO_CULL = Util.memoize(texture -> dragonSurvival$createTranslucentArmorCutoutNoCull("translucent_armor_cutout_no_cull", texture, false));

    /** Needed because there is no entity context at certain points - not an issue since the game is not multithreaded */
    @Unique private static int dragonSurvival$alpha = HunterHandler.UNMODIFIED;

    @Inject(method = "renderArmorPiece", at = @At("HEAD"))
    private void dragonSurvival$storeAlpha(final PoseStack poseStack, final MultiBufferSource bufferSource, final LivingEntity entity, final EquipmentSlot slot, int packedLight, final HumanoidModel<?> model, final CallbackInfo callback) {
        if (HunterData.hasTransparency(entity)) {
            dragonSurvival$alpha = HunterHandler.calculateAlpha(entity);
        } else {
            dragonSurvival$alpha = HunterHandler.UNMODIFIED;
        }
    }

    @ModifyArg(method = "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/resources/ResourceLocation;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private RenderType dragonSurvival$getTranslucentRenderType(final RenderType renderType, @Local(argsOnly = true) final ResourceLocation texture) {
        if (dragonSurvival$alpha != HunterHandler.UNMODIFIED && dragonSurvival$alpha != 1) {
            return dragonSurvival$TRANSLUCENT_ARMOR_CUTOUT_NO_CULL.apply(texture);
        }

        return renderType;
    }

    @ModifyArg(method = "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private RenderType dragonSurvival$getTranslucentTrimRenderType(final RenderType renderType) {
        if (dragonSurvival$alpha != HunterHandler.UNMODIFIED && dragonSurvival$alpha != 1) {
            return dragonSurvival$TRANSLUCENT_ARMOR_CUTOUT_NO_CULL.apply(Sheets.ARMOR_TRIMS_SHEET);
        }

        return renderType;
    }

    @ModifyArg(method = {
            "renderModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/resources/ResourceLocation;)V",
            "renderTrim(Lnet/minecraft/world/item/ArmorMaterial;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/item/armortrim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"), index = 7)
    private float dragonSurvival$modifyAlpha(final float alpha) {
        if (dragonSurvival$alpha != HunterHandler.UNMODIFIED && dragonSurvival$alpha != 1) {
            return dragonSurvival$alpha / 255.f;
        }

        return alpha;
    }

    @Unique private static RenderType.CompositeRenderType dragonSurvival$createTranslucentArmorCutoutNoCull(final String name, final ResourceLocation texture, boolean equalDepthTest) {
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
