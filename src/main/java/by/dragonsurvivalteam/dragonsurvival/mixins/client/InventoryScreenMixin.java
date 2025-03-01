package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HunterData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MovementData;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener {
    @Unique private static float dragon_survival$storedXAngle = 0;
    @Unique private static float dragon_survival$storedYAngle = 0;

    public InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    // This is to angle the dragon entity (including its head) to correctly follow the angle specified when rendering.
    @Redirect(method = "renderEntityInInventory", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;runAsFancy(Ljava/lang/Runnable;)V"))
    private static void dragon_survival$dragonScreenEntityRender(final Runnable runnable, @Local(argsOnly = true) LivingEntity entity) {
        LivingEntity newEntity;

        if (entity instanceof DragonEntity dragon) {
            dragon.isOverridingMovementData = true;
            newEntity = dragon.getPlayer();
        } else {
            newEntity = entity;
        }

        if (DragonStateProvider.isDragon(newEntity)) {
            MovementData movement = MovementData.getData(newEntity);
            double bodyYaw = movement.bodyYaw;
            double headYaw = movement.headYaw;
            double headPitch = movement.headPitch;
            Vec3 deltaMovement = movement.deltaMovement;
            Vec3 deltaMovementLastFrame = movement.deltaMovementLastFrame;

            movement.bodyYaw = newEntity.yBodyRot;
            movement.headYaw = -Math.toDegrees(dragon_survival$storedXAngle);
            movement.headPitch = -Math.toDegrees(dragon_survival$storedYAngle);
            movement.deltaMovement = Vec3.ZERO;
            movement.deltaMovementLastFrame = Vec3.ZERO;

            RenderSystem.runAsFancy(runnable);

            dragon_survival$storedXAngle = 0;
            dragon_survival$storedYAngle = 0;

            movement.bodyYaw = bodyYaw;
            movement.headYaw = headYaw;
            movement.headPitch = headPitch;
            movement.deltaMovement = deltaMovement;
            movement.deltaMovementLastFrame = deltaMovementLastFrame;
        } else {
            RenderSystem.runAsFancy(runnable);
        }

        if (entity instanceof DragonEntity dragon) {
            dragon.isOverridingMovementData = false;
        }
    }

    // If we are a dragon, we don't want to angle the entire entity when rendering it with a follows mouse command (like vanilla does).
    // Instead, we angle just the dragon's head to follow the given angle. So we modify the angles to eb zero if we are a dragon and capture them to use them later.
    @ModifyArgs(method = "renderEntityInInventoryFollowsMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventoryFollowsAngle(Lnet/minecraft/client/gui/GuiGraphics;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"))
    private static void dragon_survival$cancelEntityAnglingForDragons(Args args) {
        if (DragonStateProvider.isDragon(args.get(9))) {
            dragon_survival$storedXAngle = args.get(7);
            dragon_survival$storedYAngle = args.get(8);
            args.set(7, 0.f);
            args.set(8, 0.f);
        }
    }

    @Inject(method = "renderEntityInInventory", at = @At("HEAD"))
    private static void dragonSurvival$setFlag(final GuiGraphics graphics, float x, float y, float scale, final Vector3f translate, final Quaternionf pose, final Quaternionf cameraOrientation, final LivingEntity entity, final CallbackInfo callback) {
        if (entity instanceof DragonEntity dragon) {
            Player player = dragon.getPlayer();

            if (player != null) {
                player.getExistingData(DSDataAttachments.HUNTER).ifPresent(HunterData::disableTransparency);
            }
        } else {
            entity.getExistingData(DSDataAttachments.HUNTER).ifPresent(HunterData::disableTransparency);
        }
    }

    @Inject(method = "renderEntityInInventory", at = @At("RETURN"))
    private static void dragonSurvival$clearFlag(final GuiGraphics graphics, float x, float y, float scale, final Vector3f translate, final Quaternionf pose, final Quaternionf cameraOrientation, final LivingEntity entity, final CallbackInfo callback) {
        if (entity instanceof DragonEntity dragon) {
            Player player = dragon.getPlayer();

            if (player != null) {
                player.getExistingData(DSDataAttachments.HUNTER).ifPresent(HunterData::enableTransparency);
            }
        } else {
            entity.getExistingData(DSDataAttachments.HUNTER).ifPresent(HunterData::enableTransparency);
        }
    }
}
