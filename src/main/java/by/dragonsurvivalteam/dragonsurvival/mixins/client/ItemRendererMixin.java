package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Render the held item with the modified alpha from the hunter stacks */
@Mixin(ItemRenderer.class)
@Debug(export = true) // TODO :: remove
public abstract class ItemRendererMixin { // FIXME :: doesn't work with sodium since they replace item rendering
    @Inject(method = "renderStatic(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/level/Level;III)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/ItemRenderer;render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V", shift = At.Shift.BEFORE))
    private void dragonSurvival$storeAlpha(final LivingEntity entity, final ItemStack stack, final ItemDisplayContext context, boolean leftHand, final PoseStack poseStack, final MultiBufferSource bufferSource, final Level level, int combinedLight, int combinedOverlay, int seed, final CallbackInfo callback) {
        if (dragonSurvival$isThirdPerson(context) || HunterHandler.TRANSLUCENT_ITEMS_IN_FIRST_PERSON && dragonSurvival$isFirstPerson(context)) {
            LivingEntity relevantEntity = dragonSurvival$getRelevantEntity(entity);

            if (relevantEntity != null) {
                HunterHandler.itemTranslucency = HunterHandler.calculateAlphaAsFloat(relevantEntity);
                return;
            }
        }

        HunterHandler.itemTranslucency = HunterHandler.UNMODIFIED;
    }

    @ModifyArg(method = "renderQuadList", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;putBulkData(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lnet/minecraft/client/renderer/block/model/BakedQuad;FFFFIIZ)V"), index = 5)
    private float dragonSurvival$modifyAlpha(float alpha) {
        if (HunterHandler.itemTranslucency != HunterHandler.UNMODIFIED) {
            return HunterHandler.itemTranslucency;
        }

        return alpha;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void dragonSurvival$clearItemTranslucency(final CallbackInfo callback) {
        HunterHandler.itemTranslucency = HunterHandler.UNMODIFIED;
    }

    @Unique private static boolean dragonSurvival$isThirdPerson(final ItemDisplayContext context) {
        return context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
    }

    @Unique private static boolean dragonSurvival$isFirstPerson(final ItemDisplayContext context) {
        return context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
    }

    @Unique private static @Nullable LivingEntity dragonSurvival$getRelevantEntity(final LivingEntity entity) {
        if (entity instanceof DragonEntity dragon) {
            Player player = dragon.getPlayer();

            if (player != null && player.getData(DSDataAttachments.HUNTER).hasHunterStacks()) {
                return player;
            }
        }

        return entity.getData(DSDataAttachments.HUNTER).hasHunterStacks() ? entity : null;
    }
}
