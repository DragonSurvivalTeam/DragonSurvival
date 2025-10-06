package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;

public class DragonCuriosRenderLayer extends GeoRenderLayer<DragonEntity> {

    private final GeoEntityRenderer<DragonEntity> renderer;

    public DragonCuriosRenderLayer(GeoEntityRenderer<DragonEntity> entityRendererIn) {
        super(entityRendererIn);
        renderer = entityRendererIn;
    }

    @Override
    public void render(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        Player player = animatable.getPlayer();

        if (player == null || player.isSpectator()) {
            return;
        }

        CoreGeoBone neck = ClientDragonRender.dragonArmorModel.getAnimationProcessor().getBone("Neck");

        if (neck != null) {
            neck.setHidden(false);
        }

        ArrayList<ResourceLocation> curioTextures = getCurioTextures(player);

        if (!curioTextures.isEmpty()) {
            ((DragonRenderer) renderer).isRenderLayers = true;

            for (ResourceLocation texture : curioTextures) {
                renderCurioPiece(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, texture);
            }

            ((DragonRenderer) renderer).isRenderLayers = false;
        }
    }

    private void renderCurioPiece(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final MultiBufferSource bufferSource, float partialTick, int packedLight, ResourceLocation texture) {
        ClientDragonRender.dragonModel.setCurrentTexture(texture);
        ClientDragonRender.dragonArmor.copyPosition(animatable);
        RenderType type = renderer.getRenderType(animatable, texture, bufferSource, partialTick);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(type);

        renderer.actuallyRender(poseStack, animatable, bakedModel, type, bufferSource, vertexConsumer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
    }

    private ArrayList<ResourceLocation> getCurioTextures(Player player) {
        ArrayList<ResourceLocation> resources = new ArrayList<>();
        // mostly taken from https://github.com/TheIllusiveC4/Curios/blob/1.18.x/src/main/java/top/theillusivec4/curios/client/render/CuriosLayer.java
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> handler.getCurios().forEach(
                (id, stacksHandler) -> {
                    IDynamicStackHandler stackHandler = stacksHandler.getStacks();
                    IDynamicStackHandler cosmeticStacksHandler = stacksHandler.getCosmeticStacks();

                    for (int i = 0; i < stackHandler.getSlots(); i++) {
                        ItemStack stack = cosmeticStacksHandler.getStackInSlot(i);
                        if (stack.isEmpty()) {
                            stack = stackHandler.getStackInSlot(i);
                        }
                        NonNullList<Boolean> renderStates = stacksHandler.getRenders();
                        boolean renderable = renderStates.size() > i && renderStates.get(i);
                        if (renderable) {
                            String resId = DragonArmorRenderLayer.itemToResLoc(stack.getItem());
                            if (resId != null) {
                                resId = "textures/armor/" + resId;
                                if (Minecraft.getInstance().getResourceManager().getResource(
                                        new ResourceLocation(DragonSurvivalMod.MODID, resId)).isPresent()) {
                                    resources.add(new ResourceLocation(DragonSurvivalMod.MODID, resId));
                                }
                            }
                        }
                    }
                }
        ));
        return resources;
    }
}