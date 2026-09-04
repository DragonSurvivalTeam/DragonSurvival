package by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.Bolas;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ThrownItemRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class BolasEntityRenderer extends EntityRenderer<Bolas, BolasEntityRenderer.BolasRenderState> {
    private static final float NORMAL_PLAYER_EYE_HEIGHT = 1.62F;
    private static final float DEFAULT_SCALE = 1.2F;
    private final ItemModelResolver itemModelResolver;

    public BolasEntityRenderer(final EntityRendererProvider.Context context) {
        super(context);
        itemModelResolver = context.getItemModelResolver();
    }

    private static ItemStack createBolasStack() {
        ItemStack bolas = new ItemStack(DSItems.HUNTING_NET);
        bolas.set(DataComponents.ITEM_MODEL, DragonSurvival.res("dragon_hunting_mesh"));
        return bolas;
    }

    @Override
    protected int getBlockLightLevel(final Bolas entity, final BlockPos position) {
        return 15;
    }

    @Override
    public BolasRenderState createRenderState() {
        return new BolasRenderState();
    }

    @Override
    public void extractRenderState(final Bolas entity, final BolasRenderState state, final float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        itemModelResolver.updateForNonLiving(state.item, createBolasStack(), ItemDisplayContext.GROUND, entity);
        state.scale = getScale(entity.getOwner());
    }

    @Override
    public void submit(final BolasRenderState state, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector, final CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.scale(state.scale, state.scale, state.scale);
        poseStack.mulPose(camera.orientation);
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
        super.submit(state, poseStack, submitNodeCollector, camera);
    }

    private static float getScale(final Entity owner) {
        if (!(owner instanceof LivingEntity living)) {
            return DEFAULT_SCALE;
        }

        float eyeHeight = living.getEyeHeight();

        if (living instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.isDragon()) {
                eyeHeight = (float) DragonSizeHandler.calculateDragonEyeHeight(handler, player);
            }
        }

        return DEFAULT_SCALE * eyeHeight / NORMAL_PLAYER_EYE_HEIGHT;
    }

    public static class BolasRenderState extends ThrownItemRenderState {
        private float scale = DEFAULT_SCALE;
    }
}
