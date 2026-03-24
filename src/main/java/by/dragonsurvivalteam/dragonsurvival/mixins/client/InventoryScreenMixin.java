package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> implements RecipeUpdateListener {
    @Unique private static float dragon_survival$storedXAngle = 0;
    @Unique private static float dragon_survival$storedYAngle = 0;

    public InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component component) {
        super(menu, null, inventory, component);
    }

    @Inject(
        method = "renderEntityInInventoryFollowsAngle",
        at = @At("HEAD")
    )
    private static void dragon_survival$pushInventoryRenderContext(final net.minecraft.client.gui.GuiGraphics guiGraphics, final int x1, final int y1, final int x2, final int y2, final int scale, final float offsetY, final float angleXComponent, final float angleYComponent, final LivingEntity entity, final CallbackInfo callbackInfo) {
        if (entity instanceof DragonEntity || DragonStateProvider.isDragon(entity)) {
            DragonRenderer.pushInventoryRenderOverrides(0.0F, -Math.toDegrees(dragon_survival$storedXAngle), -Math.toDegrees(dragon_survival$storedYAngle));
        }
    }

    @Inject(
        method = "renderEntityInInventoryFollowsAngle",
        at = @At("RETURN")
    )
    private static void dragon_survival$popInventoryRenderContext(final net.minecraft.client.gui.GuiGraphics guiGraphics, final int x1, final int y1, final int x2, final int y2, final int scale, final float offsetY, final float angleXComponent, final float angleYComponent, final LivingEntity entity, final CallbackInfo callbackInfo) {
        if (entity instanceof DragonEntity || DragonStateProvider.isDragon(entity)) {
            DragonRenderer.clearInventoryRenderOverrides();
        }
    }

    @WrapOperation(
        method = "renderEntityInInventoryFollowsAngle",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;extractRenderState(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;"
        )
    )
    private static EntityRenderState dragon_survival$dragonScreenEntityRender(final LivingEntity entity, final Operation<EntityRenderState> original) {
        LivingEntity entityToRender = entity;
        DragonEntity dragon = null;

        if (entity instanceof DragonEntity dragonEntity) {
            dragon = dragonEntity;
            entityToRender = dragon.getPlayer();
        } else if (entity instanceof Player player) {
            dragon = ClientDragonRenderer.getDragon(player);
        }

        if (!DragonStateProvider.isDragon(entityToRender)) {
            return original.call(entity);
        }

        LivingEntity renderEntity = dragon != null ? dragon : entity;
        return original.call(renderEntity);
    }

    // If we are a dragon, we don't want to angle the entire entity when rendering it with a follows mouse command.
    // Instead, we only angle the dragon's head, so capture the vanilla angle values and zero them out for the
    // entity render state itself.
    @ModifyArgs(
        method = "renderEntityInInventoryFollowsMouse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventoryFollowsAngle(Lnet/minecraft/client/gui/GuiGraphics;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"
        )
    )
    private static void dragon_survival$cancelEntityAnglingForDragons(Args args) {
        LivingEntity entity = (LivingEntity) args.get(9);

        if (entity instanceof DragonEntity || DragonStateProvider.isDragon(entity)) {
            dragon_survival$storedXAngle = args.get(7);
            dragon_survival$storedYAngle = args.get(8);
            args.set(7, 0.0F);
            args.set(8, 0.0F);
        }
    }

    @Inject(
        method = "renderEntityInInventoryFollowsMouse",
        at = @At("RETURN")
    )
    private static void dragon_survival$clearStoredInventoryAngles(final net.minecraft.client.gui.GuiGraphics guiGraphics, final int x1, final int y1, final int x2, final int y2, final int scale, final float offsetY, final float mouseX, final float mouseY, final LivingEntity entity, final CallbackInfo callbackInfo) {
        if (entity instanceof DragonEntity || DragonStateProvider.isDragon(entity)) {
            dragon_survival$storedXAngle = 0;
            dragon_survival$storedYAngle = 0;
        }
    }
}
