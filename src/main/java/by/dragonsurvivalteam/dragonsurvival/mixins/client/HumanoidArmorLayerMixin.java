package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.HunterHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.EquipmentLayerRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EquipmentLayerRenderer.class)
public abstract class HumanoidArmorLayerMixin {
    @Shadow @Final private EquipmentAssetManager equipmentAssets;

    @Inject(
        method = "renderLayers(Lnet/minecraft/client/resources/model/EquipmentClientInfo$LayerType;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lnet/minecraft/world/item/ItemStack;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/resources/Identifier;II)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private <S> void dragonSurvival$renderTransparentArmor(
        final EquipmentClientInfo.LayerType layerType,
        final ResourceKey<EquipmentAsset> equipmentAssetId,
        Model<? super S> model,
        final S state,
        final ItemStack itemStack,
        final PoseStack poseStack,
        final SubmitNodeCollector submitNodeCollector,
        final int lightCoords,
        final @Nullable Identifier playerTextureOverride,
        final int outlineColor,
        final int order,
        final CallbackInfo callback
    ) {
        Player player = dragonSurvival$getHunterPlayer(state);
        if (player == null || !dragonSurvival$hasHunterTransparency(player)) {
            return;
        }

        IClientItemExtensions extensions = IClientItemExtensions.of(itemStack);
        model = extensions.getGenericArmorModel(itemStack, layerType, model);

        List<EquipmentClientInfo.Layer> layers = this.equipmentAssets.get(equipmentAssetId).getLayers(layerType);
        if (layers.isEmpty()) {
            callback.cancel();
            return;
        }

        int dyeColor = extensions.getDefaultDyeColor(itemStack);
        boolean renderFoil = itemStack.hasFoil();
        int nextOrder = order;

        for (int idx = 0; idx < layers.size(); idx++) {
            EquipmentClientInfo.Layer layer = layers.get(idx);
            int color = extensions.getArmorLayerTintColor(itemStack, layer, idx, dyeColor);

            if (color == 0) {
                continue;
            }

            Identifier layerTexture = layer.usePlayerTexture() && playerTextureOverride != null
                ? playerTextureOverride
                : layer.getTextureLocation(layerType);
            layerTexture = net.neoforged.neoforge.client.ClientHooks.getArmorTexture(itemStack, layerType, layer, layerTexture);

            int tintedColor = HunterHandler.modifyAlpha(player, color);
            RenderType renderType = RenderTypes.armorTranslucent(layerTexture);
            submitNodeCollector.order(nextOrder++)
                .submitModel(model, state, poseStack, renderType, lightCoords, OverlayTexture.NO_OVERLAY, tintedColor, null, outlineColor, null);

            if (renderFoil) {
                submitNodeCollector.order(nextOrder++)
                    .submitModel(
                        model,
                        state,
                        poseStack,
                        RenderTypes.armorEntityGlint(),
                        lightCoords,
                        OverlayTexture.NO_OVERLAY,
                        tintedColor,
                        null,
                        outlineColor,
                        null
                    );
            }

            renderFoil = false;
        }

        // Armor trims use a sprite-atlas path that changed under the hood; skipping the vanilla path here
        // keeps the armor itself in sync with hunter translucency instead of rendering fully opaque.
        callback.cancel();
    }

    private static @Nullable Player dragonSurvival$getHunterPlayer(final Object state) {
        if (!(state instanceof AvatarRenderState avatarRenderState)) {
            return null;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }

        return minecraft.level.getEntity(avatarRenderState.id) instanceof Player player ? player : null;
    }

    private static boolean dragonSurvival$hasHunterTransparency(final Player player) {
        float alpha = HunterHandler.calculateAlphaAsFloat(player);
        return alpha != HunterHandler.UNMODIFIED && alpha < 1.0F;
    }
}
