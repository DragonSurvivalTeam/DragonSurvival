package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;

@Mixin( InventoryScreen.class )
public abstract class MixinInventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener{
	public MixinInventoryScreen(InventoryMenu p_98701_, Inventory p_98702_, Component p_98703_){
		super(p_98701_, p_98702_, p_98703_);
	}

	@Unique private static float dragon_survival$storedXAngle = 0;
	@Unique private static float dragon_survival$storedYAngle = 0;

	@Redirect( method = "renderEntityInInventoryRaw(IIIFFLnet/minecraft/world/entity/LivingEntity;)V", at = @At( value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;runAsFancy(Ljava/lang/Runnable;)V" ) )
	private static void dragonScreenEntityRender(Runnable runnable, @Local(argsOnly = true) LivingEntity entity){
		LivingEntity newEntity;

		if (entity instanceof DragonEntity de) {
			newEntity = de.getPlayer();
		} else {
			newEntity = entity;
		}

		if(DragonStateProvider.getCap(newEntity).isPresent() && DragonUtils.getHandler(newEntity).isDragon()){
			DragonStateProvider.getCap(newEntity).ifPresent(handler -> {
				if (handler.isDragon()) {
					double bodyYaw = handler.getMovementData().bodyYaw;
					double headYaw = handler.getMovementData().headYaw;
					double headPitch = handler.getMovementData().headPitch;
					Vec3 deltaMovement = handler.getMovementData().deltaMovement;
					Vec3 deltaMovementLastFrame = handler.getMovementData().deltaMovementLastFrame;

					handler.getMovementData().bodyYaw = newEntity.yBodyRot;
					handler.getMovementData().headYaw = -Math.toDegrees(dragon_survival$storedXAngle);
					handler.getMovementData().headPitch = -Math.toDegrees(dragon_survival$storedYAngle);
					handler.getMovementData().deltaMovement = Vec3.ZERO;
					handler.getMovementData().deltaMovementLastFrame = Vec3.ZERO;

					ClientDragonRender.isOverridingMovementData = true;
					RenderSystem.runAsFancy(runnable);
					ClientDragonRender.isOverridingMovementData = false;

					dragon_survival$storedXAngle = 0;
					dragon_survival$storedYAngle = 0;

					handler.getMovementData().bodyYaw = bodyYaw;
					handler.getMovementData().headYaw = headYaw;
					handler.getMovementData().headPitch = headPitch;
					handler.getMovementData().deltaMovement = deltaMovement;
					handler.getMovementData().deltaMovementLastFrame = deltaMovementLastFrame;
				} else {
					RenderSystem.runAsFancy(runnable);
				}
			});
		} else {
			RenderSystem.runAsFancy(runnable);
		}
	}

	@Unique
	private static float dragonSurvival$dragonScreenEntityRescaler(float pX){
		LocalPlayer player = Minecraft.getInstance().player;
		DragonStateHandler handler = DragonUtils.getHandler(player);

		if(handler.isDragon()){
			double size = handler.getSize();
			if(size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE){
				// Scale the matrix back to the DEFAULT_MAX_GROWTH_SIZE to prevent the entity from clipping in the inventory panel
				pX *= Mth.sqrt(((float)(ServerConfig.DEFAULT_MAX_GROWTH_SIZE / size)));
			}
		}

		return pX;
	}

	// If we are a dragon, we don't want to angle the entire entity when rendering it with a follows mouse command (like vanilla does).
	// Instead, we angle just the dragon's head to follow the given angle. So we modify the angles to be zero if we are a dragon and capture them to use them later.
	@ModifyArg(method = "renderEntityInInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventoryRaw(IIIFFLnet/minecraft/world/entity/LivingEntity;)V"), index = 3)
	private static float dragon_survival$cancelXEntityAnglingForDragons(float angleXComponent, @Local(argsOnly = true) LivingEntity entity) {
		DragonStateHandler handler = DragonStateProvider.getHandler(entity);
		if (handler != null && handler.isDragon()) {
			dragon_survival$storedXAngle = angleXComponent;
			return 0;
		}
		return angleXComponent;
	}

	@ModifyArg(method = "renderEntityInInventory", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventoryRaw(IIIFFLnet/minecraft/world/entity/LivingEntity;)V"), index = 4)
	private static float dragon_survival$cancelYEntityAnglingForDragons(float angleYComponent, @Local(argsOnly = true) LivingEntity entity){
		DragonStateHandler handler = DragonStateProvider.getHandler(entity);
		if (handler != null && handler.isDragon()) {
			dragon_survival$storedYAngle = angleYComponent;
			return 0;
		}
		return angleYComponent;
	}

	@ModifyArg(method = "renderEntityInInventoryRaw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"), index = 0)
	private static float dragonScreenEntityRescalerX(float pX) {
		return dragonSurvival$dragonScreenEntityRescaler(pX);
	}

	@ModifyArg(method = "renderEntityInInventoryRaw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"), index = 1)
	private static float dragonScreenEntityRescalerY(float pY) {
		return dragonSurvival$dragonScreenEntityRescaler(pY);
	}

	@ModifyArg(method = "renderEntityInInventoryRaw", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"), index = 2)
	private static float dragonScreenEntityRescalerZ(float pZ) {
		return dragonSurvival$dragonScreenEntityRescaler(pZ);
	}
}