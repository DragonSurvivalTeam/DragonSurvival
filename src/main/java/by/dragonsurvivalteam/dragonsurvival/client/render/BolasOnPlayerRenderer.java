package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(Dist.CLIENT)
public final class BolasOnPlayerRenderer {
    private static final float NORMAL_PLAYER_EYE_HEIGHT = 1.62F;
    private static final float NORMAL_NET_SCALE = 1.6F + NORMAL_PLAYER_EYE_HEIGHT / 8.0F;
    private static final float NORMAL_NET_Y_OFFSET = 0.9F + NORMAL_PLAYER_EYE_HEIGHT / 8.0F;

    private BolasOnPlayerRenderer() {
    }

    private static ItemStack createBolasStack() {
        ItemStack bolas = new ItemStack(DSItems.HUNTING_NET);
        bolas.set(DataComponents.ITEM_MODEL, DragonSurvival.res("dragon_hunting_mesh"));
        return bolas;
    }

    @SubscribeEvent(receiveCanceled = true)
    public static void renderTrap(final RenderPlayerEvent.Pre<?> event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        Entity entity = minecraft.level.getEntity(event.getRenderState().id);

        if (!(entity instanceof Player player) || player.isSpectator() || !player.hasEffect(DSEffects.TRAPPED)) {
            return;
        }

        float eyeHeight = player.getEyeHeight();
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (handler.isDragon()) {
            eyeHeight = (float) DragonSizeHandler.calculateDragonEyeHeight(handler, player);
        }

        ItemStackRenderState renderState = new ItemStackRenderState();
        minecraft.getItemModelResolver().updateForLiving(renderState, createBolasStack(), ItemDisplayContext.GROUND, player);

        PoseStack poseStack = event.getPoseStack();
        float sizeRatio = eyeHeight / NORMAL_PLAYER_EYE_HEIGHT;
        poseStack.pushPose();
        poseStack.translate(0, NORMAL_NET_Y_OFFSET * sizeRatio, 0);
        poseStack.scale(NORMAL_NET_SCALE * sizeRatio, NORMAL_NET_SCALE * sizeRatio, NORMAL_NET_SCALE * sizeRatio);
        renderState.submit(poseStack, event.getSubmitNodeCollector(), event.getRenderState().lightCoords, OverlayTexture.NO_OVERLAY, event.getRenderState().outlineColor);
        poseStack.popPose();
    }
}
