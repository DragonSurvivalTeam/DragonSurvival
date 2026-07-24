package by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.Bolas;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class BolasEntityRenderer extends EntityRenderer<Bolas> {

    // This class is purely for rendering the bolas projectile. The bolas rendered on top of the target when it is trapped is handled elsewhere: renderBolas & renderTrap inside of ClientEvents.java, and thirdPersonPreRender in ClientDragonRender.java

    private static final ResourceLocation BOLAS_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/item/dragon_hunting_mesh.png");

    public BolasEntityRenderer(Context p_174198_) {
        super(p_174198_);
    }

    @Override
    public void render(final Bolas bolas, float yaw, float partialTicks, @NotNull PoseStack stack, @NotNull MultiBufferSource bufferSource, int eventLight) {
        if (bolas.tickCount >= 2 || !(entityRenderDispatcher.camera.getEntity().distanceToSqr(bolas) < 12.25D)) {
            stack.pushPose();
            stack.scale(1.2F, 1.2F, 1.2F);
            stack.mulPose(entityRenderDispatcher.cameraOrientation());
            stack.mulPose(Axis.YP.rotationDegrees(180.0F));
            Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(DSItems.HUNTING_NET), ItemDisplayContext.GROUND, eventLight, OverlayTexture.NO_OVERLAY, stack, bufferSource, bolas.level(), 0);
            stack.popPose();
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Bolas bolas) {
        return BOLAS_TEXTURE;
    }
}