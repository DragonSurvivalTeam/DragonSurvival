package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> implements RecipeUpdateListener {
    public InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component component) {
        super(menu, null, inventory, component);
    }

    @WrapOperation(
            method = "extractEntityInInventoryFollowsMouse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventoryFollowsAngle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"
            )
    )
    private static void dragon_survival$renderDragonInInventoryFollowsAngle(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, int size, float offsetY, float xAngle, float yAngle, LivingEntity entity, final Operation<Void> original)
    {
        LivingEntity entityToRender = entity;
        DragonEntity dragon = null;

        if (entity instanceof DragonEntity dragonEntity) {
            dragon = dragonEntity;
            entityToRender = dragon.getPlayer();
        } else if (entity instanceof Player player) {
            dragon = ClientDragonRenderer.getDragon(player);
        }

        if (!DragonStateProvider.isDragon(entityToRender) ||
                (entityToRender instanceof Player player && DragonStateProvider.getData(player).body().value().noDragonModelRendering())) {
            original.call(graphics, x0, y0, x1, y1, size, offsetY, xAngle, yAngle, entity);
            return;
        }

        LivingEntity renderEntity = dragon != null ? dragon : entity;
        float partialTick = DragonSurvival.PROXY.getPartialTick();
        LivingEntityRenderState renderState = (LivingEntityRenderState)DragonRenderer.createUIRenderState(
                renderEntity,
                partialTick,
                0.0F,
                -Math.toDegrees(xAngle),
                -Math.toDegrees(yAngle)
        );
        renderState.bodyRot = 180.0F;
        renderState.yRot = 0.0F;
        renderState.xRot = 0.0F;

        Player playerEntityToRender = (Player)entityToRender;
        DragonStateHandler handler = DragonStateProvider.getData(playerEntityToRender);
        float scale = (float) playerEntityToRender.getAttribute(Attributes.SCALE).getValue();
        EntityDimensions dimensions = DragonSizeHandler.calculateDimensions(handler, playerEntityToRender, handler.previousPose);
        renderState.boundingBoxWidth = dimensions.width() / scale;
        renderState.boundingBoxHeight = dimensions.height() / scale;
        renderState.scale = 1.0F;

        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        Vector3f translation = new Vector3f(0.0F, renderState.boundingBoxHeight / 2.0F + offsetY, 0.0F);
        graphics.entity(renderState, size, translation, rotation, rotation, x0, y0, x1, y1);
    }
}
