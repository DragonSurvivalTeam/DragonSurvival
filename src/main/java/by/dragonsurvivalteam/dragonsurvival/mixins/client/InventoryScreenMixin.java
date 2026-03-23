package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
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
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractRecipeBookScreen<InventoryMenu> implements RecipeUpdateListener {
    @Unique private static float dragon_survival$storedXAngle = 0;
    @Unique private static float dragon_survival$storedYAngle = 0;

    public InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component component) {
        super(menu, null, inventory, component);
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

        MovementData movement = MovementData.getData(entityToRender);
        double bodyYaw = movement.bodyYaw;
        double headYaw = movement.headYaw;
        double headPitch = movement.headPitch;
        Vec3 deltaMovement = movement.deltaMovement;
        Vec3 deltaMovementLastFrame = movement.deltaMovementLastFrame;

        movement.bodyYaw = entityToRender.yBodyRot;
        movement.headYaw = -Math.toDegrees(dragon_survival$storedXAngle);
        movement.headPitch = -Math.toDegrees(dragon_survival$storedYAngle);
        movement.deltaMovement = Vec3.ZERO;
        movement.deltaMovementLastFrame = Vec3.ZERO;

        try {
            if (dragon != null) {
                dragon.isInInventory = true;

                Player player = dragon.getPlayer();
                if (player != null) {
                    player.getExistingData(DSDataAttachments.HUNTER).ifPresent(HunterData::disableTransparency);
                }
            } else {
                entity.getExistingData(DSDataAttachments.HUNTER).ifPresent(HunterData::disableTransparency);
            }

            return original.call(entity);
        } finally {
            if (dragon != null) {
                Player player = dragon.getPlayer();

                if (player != null) {
                    player.getExistingData(DSDataAttachments.HUNTER).ifPresent(HunterData::enableTransparency);
                }

                dragon.isInInventory = false;
            } else {
                entity.getExistingData(DSDataAttachments.HUNTER).ifPresent(HunterData::enableTransparency);
            }

            dragon_survival$storedXAngle = 0;
            dragon_survival$storedYAngle = 0;

            movement.bodyYaw = bodyYaw;
            movement.headYaw = headYaw;
            movement.headPitch = headPitch;
            movement.deltaMovement = deltaMovement;
            movement.deltaMovementLastFrame = deltaMovementLastFrame;
        }
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
        if (DragonStateProvider.isDragon((LivingEntity)args.get(9))) {
            dragon_survival$storedXAngle = args.get(7);
            dragon_survival$storedYAngle = args.get(8);
            args.set(7, 0.0F);
            args.set(8, 0.0F);
        }
    }
}
