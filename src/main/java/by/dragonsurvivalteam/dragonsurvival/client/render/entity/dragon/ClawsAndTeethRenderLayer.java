package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.Tiers;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class ClawsAndTeethRenderLayer extends GeoRenderLayer<DragonEntity> {
    private final GeoEntityRenderer<DragonEntity> renderer;

    public ClawsAndTeethRenderLayer(final GeoEntityRenderer<DragonEntity> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    private static String getMaterial(String texture, ItemStack clawItem) {
        if (clawItem.getItem() instanceof TieredItem item) {
            Tier tier = item.getTier();

            switch (tier) {
                case Tiers.NETHERITE -> texture += "netherite_";
                case Tiers.DIAMOND -> texture += "diamond_";
                case Tiers.IRON -> texture += "iron_";
                case Tiers.GOLD -> texture += "gold_";
                case Tiers.STONE -> texture += "stone_";
                case Tiers.WOOD -> texture += "wooden_";
                default -> texture += "modded_";
            }

            return texture;
        }

        return texture + "modded_";
    }

    @Override
    public void render(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!((DragonRenderer) renderer).shouldRenderLayers) {
            return;
        }

        Player player = animatable.getPlayer();

        if (player == null) {
            return;
        }

        if (!ClawInventoryData.getData(player).shouldRenderClaws) {
            return;
        }

        String clawTexture = constructClaws(player);

        if (clawTexture != null) {
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, clawTexture);

            ((DragonRenderer) renderer).isRenderLayers = true;
            renderToolLayer(poseStack, animatable, bakedModel, bufferSource, texture, partialTick, packedLight);
            ((DragonRenderer) renderer).isRenderLayers = false;
        }

        String teethTexture = constructTeethTexture(player);

        if (teethTexture != null) {
            ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(MODID, teethTexture);

            ((DragonRenderer) renderer).isRenderLayers = true;
            renderToolLayer(poseStack, animatable, bakedModel, bufferSource, texture, partialTick, packedLight);
            ((DragonRenderer) renderer).isRenderLayers = false;
        }
    }

    private void renderToolLayer(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final MultiBufferSource bufferSource, final ResourceLocation texture, float partialTick, int packedLight) {
        RenderType type = renderer.getRenderType(animatable, texture, bufferSource, partialTick);

        if (type != null) {
            VertexConsumer vertexConsumer = bufferSource.getBuffer(type);
            renderer.actuallyRender(poseStack, animatable, bakedModel, type, bufferSource, vertexConsumer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, renderer.getRenderColor(animatable, partialTick, packedLight).getColor());
        }
    }

    public String constructClaws(final Player player) {
        DragonStateHandler handler = DragonStateProvider.getData(player);
        String texturePath = "textures/armor/" + handler.body().value().model().getPath() + "/";
        if (handler.species() == null) {
            return null;
        }

        // FIXME :: where best to define this? misc resources to indicate a slot? or in the type itself?
        ItemStack clawItem = ItemStack.EMPTY;//ClawInventoryData.getData(player).getContainer().getItem(handler.getType().clawTextureSlot);

        if (!clawItem.isEmpty()) {
            texturePath = getMaterial(texturePath, clawItem);
        } else {
            return null;
        }

        return texturePath + "dragon_claws.png";
    }

    public String constructTeethTexture(final Player player) {
        String texturePath = "textures/armor/" + DragonStateProvider.getData(player).body().value().model().getPath() + "/";
        ItemStack swordItem = ClawInventoryData.getData(player).getContainer().getItem(0);

        if (!swordItem.isEmpty()) {
            texturePath = getMaterial(texturePath, swordItem);
        } else {
            return null;
        }

        return texturePath + "dragon_teeth.png";
    }
}