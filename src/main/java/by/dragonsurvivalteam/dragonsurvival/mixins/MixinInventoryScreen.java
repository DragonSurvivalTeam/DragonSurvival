package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin( InventoryScreen.class )
public abstract class MixinInventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener{
	public MixinInventoryScreen(InventoryMenu p_98701_, Inventory p_98702_, Component p_98703_){
		super(p_98701_, p_98702_, p_98703_);
	}

	@Unique private static float dragon_Survival$storedXAngle = 0;
	@Unique private static float dragon_Survival$storedYAngle = 0;

	// This is to angle the dragon entity (including its head) to correctly follow the angle specified when rendering.
	@Redirect(method = "renderEntityInInventory", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;runAsFancy(Ljava/lang/Runnable;)V"))
	private static void dragonScreenEntityRender(final Runnable runnable, @Local(argsOnly = true) LivingEntity entity){

		LivingEntity newEntity;
		if (entity instanceof DragonEntity de) {
			newEntity = de.getPlayer();
		} else {
            newEntity = entity;
        }

        DragonStateProvider.getCap(newEntity).ifPresentOrElse(cap -> {
			if (cap.isDragon()) {
				double bodyYaw = cap.getMovementData().bodyYaw;
				double headYaw = cap.getMovementData().headYaw;
				double headPitch = cap.getMovementData().headPitch;
				Vec3 deltaMovement = cap.getMovementData().deltaMovement;
				Vec3 deltaMovementLastFrame = cap.getMovementData().deltaMovementLastFrame;

				cap.getMovementData().bodyYaw = newEntity.yBodyRot;
				cap.getMovementData().headYaw = -Math.toDegrees(dragon_Survival$storedXAngle);
				cap.getMovementData().headPitch = -Math.toDegrees(dragon_Survival$storedYAngle);
				cap.getMovementData().deltaMovement = Vec3.ZERO;
				cap.getMovementData().deltaMovementLastFrame = Vec3.ZERO;

				ClientDragonRenderer.isOverridingMovementData = true;
				RenderSystem.runAsFancy(runnable);
				ClientDragonRenderer.isOverridingMovementData = false;

				dragon_Survival$storedXAngle = 0;
				dragon_Survival$storedYAngle = 0;

				cap.getMovementData().bodyYaw = bodyYaw;
				cap.getMovementData().headYaw = headYaw;
				cap.getMovementData().headPitch = headPitch;
				cap.getMovementData().deltaMovement = deltaMovement;
				cap.getMovementData().deltaMovementLastFrame = deltaMovementLastFrame;
			} else {
				RenderSystem.runAsFancy(runnable);
			}
		}, () -> RenderSystem.runAsFancy(runnable));
	}

	// We want to disable the scissor being used when rendering a dragon entity in the inventory. There are issues with effect tooltip text being clipped otherwise.
	@WrapWithCondition(method ="renderEntityInInventoryFollowsAngle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;enableScissor(IIII)V"))
	private static boolean skipEnableScissorCallForDragons(GuiGraphics instance, int pMinX, int pMinY, int pMaxX, int pMaxY, @Local(argsOnly = true) LivingEntity entity){
		return !DragonStateProvider.isDragon(entity);
	}

	// Make sure to also catch the disableScissor call to prevent a stack underflow of the scissor stack
	@WrapWithCondition(method ="renderEntityInInventoryFollowsAngle", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;disableScissor()V"))
	private static boolean skipDisableScissorCallForDragons(GuiGraphics instance, @Local(argsOnly = true) LivingEntity entity){
		return !DragonStateProvider.isDragon(entity);
	}

	// If we are a dragon, we don't want to angle the entire entity when rendering it with a follows mouse command (like vanilla does).
	// Instead, we angle just the dragon's head to follow the given angle. So we modify the angles to be zero if we are a dragon and capture them to use them later.
	@ModifyArgs(method = "renderEntityInInventoryFollowsMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventoryFollowsAngle(Lnet/minecraft/client/gui/GuiGraphics;IIIIIFFFLnet/minecraft/world/entity/LivingEntity;)V"))
	private static void cancelEntityAnglingForDragons(Args args){
		if(DragonStateProvider.isDragon(args.get(9))) {
			dragon_Survival$storedXAngle = args.get(7);
			dragon_Survival$storedYAngle = args.get(8);
			args.set(7, 0.f);
			args.set(8, 0.f);
		}
	}

	@ModifyArgs(method = "renderEntityInInventory", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
	private static void dragonScreenEntityRescaler(Args args, @Local(argsOnly = true) LivingEntity entity) {
		DragonStateProvider.getCap(entity)
			.ifPresent(cap -> {
				if (cap.isDragon()) {
					double size = cap.getSize();
					if(size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE)
					{
						// Scale the matrix back to the MAX_GROWTH_SIZE to prevent the entity from clipping in the inventory panel
						float scale = (float)(ServerConfig.DEFAULT_MAX_GROWTH_SIZE / size);
						args.setAll(scale * (float)args.get(0), scale * (float)args.get(1), scale * (float)args.get(2));
					}
				}
			});
	}
}
