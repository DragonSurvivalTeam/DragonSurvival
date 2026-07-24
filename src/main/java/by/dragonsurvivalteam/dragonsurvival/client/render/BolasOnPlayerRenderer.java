package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import software.bernie.geckolib.event.GeoRenderEvent;

@EventBusSubscriber(Dist.CLIENT)
public class BolasOnPlayerRenderer {
    private static ItemStack BOLAS;

    @SubscribeEvent
    public static void renderTrap(final GeoRenderEvent.Entity.Post event) {
        if (event.getEntity() instanceof DragonEntity dragon) {
            Player player = dragon.getPlayer();

            if (player == null) {
                return;
            }

            renderBolas(player, event.getPoseStack(), event.getBufferSource(), event.getPackedLight());
        } else if (event.getEntity() instanceof LivingEntity entity) {
            renderBolas(entity, event.getPoseStack(), event.getBufferSource(), event.getPackedLight());
        }
    }

    @SubscribeEvent
    public static void renderTrap(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> event) {
        renderBolas(event.getEntity(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    public static void renderBolas(final LivingEntity entity, final PoseStack pose, final MultiBufferSource bufferSource, final int packedLight) {
        if (!entity.hasEffect(DSEffects.TRAPPED)) {
            return;
        }

        int overlayCoords = LivingEntityRenderer.getOverlayCoords(entity, 0);
        float scale = entity.getEyeHeight();

        if (entity instanceof Player player) {
            if (player.isSpectator()) {
                return;
            }

            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (handler.isDragon()) {
                scale = (float) DragonSizeHandler.calculateDragonEyeHeight(handler, player);
            }
        }

        renderBolas(packedLight, overlayCoords, bufferSource, pose, scale);
    }

    private static void renderBolas(int light, int overlayCoords, MultiBufferSource buffers, PoseStack matrixStack, float eyeHeight) {
        matrixStack.pushPose();
        matrixStack.translate(0, 0.9f + eyeHeight / 8.f, 0);
        matrixStack.scale(1.6f + eyeHeight / 8.f, 1.6f + eyeHeight / 8.f, 1.6f + eyeHeight / 8.f);

        if (BOLAS == null) {
            BOLAS = new ItemStack(DSItems.HUNTING_NET);
        }

        Minecraft.getInstance().getItemRenderer().renderStatic(BOLAS, ItemDisplayContext.NONE, light, overlayCoords, matrixStack, buffers, Minecraft.getInstance().level, 0);
        matrixStack.popPose();
    }
}
