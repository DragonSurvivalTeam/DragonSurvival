package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin( InventoryScreen.class )
public abstract class MixinInventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener{
	public MixinInventoryScreen(InventoryMenu p_98701_, Inventory p_98702_, Component p_98703_){
		super(p_98701_, p_98702_, p_98703_);
	}

	// TODO :: The cause dragon editor dragons looking up when the editor is opened while the player is looking up?
	@Redirect(method = "renderEntityInInventory", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;runAsFancy(Ljava/lang/Runnable;)V"))
	private static void dragonScreenEntityRender(final Runnable runnable) {
		LocalPlayer player = Minecraft.getInstance().player;
		DragonStateHandler handler = DragonUtils.getHandler(player);

		if (handler.isDragon()) {
			double bodyYaw = handler.getMovementData().bodyYaw;
			double headYaw = handler.getMovementData().headYaw;
			double headPitch = handler.getMovementData().headPitch;

			double lastBodyYaw = handler.getMovementData().bodyYawLastTick;
			double lastHeadYaw = handler.getMovementData().headYawLastTick;
			double lastHeadPitch = handler.getMovementData().headPitchLastTick;

			handler.getMovementData().bodyYaw = player.yBodyRot;
			handler.getMovementData().headYaw = 0;
			handler.getMovementData().headPitch = 0;

			handler.getMovementData().bodyYawLastTick = player.yBodyRot;
			handler.getMovementData().headYawLastTick = player.yHeadRot;
			handler.getMovementData().headPitchLastTick = player.xRot;

			RenderSystem.runAsFancy(runnable);

			handler.getMovementData().bodyYaw = bodyYaw;
			handler.getMovementData().headYaw = headYaw;
			handler.getMovementData().headPitch = headPitch;

			handler.getMovementData().bodyYawLastTick = lastBodyYaw;
			handler.getMovementData().headYawLastTick = lastHeadYaw;
			handler.getMovementData().headPitchLastTick = lastHeadPitch;
		} else {
			RenderSystem.runAsFancy(runnable);
		}
	}

	@ModifyArg(method = "renderEntityInInventory", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPoseMatrix(Lorg/joml/Matrix4f;)V"), index = 0)
	private static Matrix4f dragonScreenEntityRescaler(Matrix4f pMatrix) {
		LocalPlayer player = Minecraft.getInstance().player;
		DragonStateHandler handler = DragonUtils.getHandler(player);

		if (handler.isDragon()) {
			double size = handler.getSize();
			if(size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE)
			{
				// Scale the matrix back to the MAX_GROWTH_SIZE to prevent the entity from clipping in the inventory panel
				float scale = (float)(ServerConfig.DEFAULT_MAX_GROWTH_SIZE / size);
				pMatrix.scale(scale, scale, scale);
			}
		}

		return pMatrix;
	}
}
