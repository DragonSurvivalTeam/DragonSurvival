package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.objects.DragonMovementData;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin( InventoryScreen.class )
public abstract class MixinInventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener{
	public MixinInventoryScreen(InventoryMenu p_98701_, Inventory p_98702_, Component p_98703_){
		super(p_98701_, p_98702_, p_98703_);
	}

	@Unique private static float dragon_survival$storedXAngle = 0;
	@Unique private static float dragon_survival$storedYAngle = 0;

    // This is to angle the dragon entity (including its head) to correctly follow the angle specified when rendering.
    @WrapOperation(method = "renderEntityInInventory", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;runAsFancy(Ljava/lang/Runnable;)V"))
    private static void dragon_survival$dragonScreenEntityRender(final Runnable runnable, final Operation<Void> original, @Local(argsOnly = true) LivingEntity entity) {
        LivingEntity entityToRender = entity;
        DragonEntity dragon = null;

        if (entity instanceof DragonEntity) {
            dragon = (DragonEntity) entity;
            entityToRender = dragon.getPlayer();
        } else if (entity instanceof Player player) {
            dragon = ClientDragonRender.getDragon(player);
        }

        DragonStateHandler handler = DragonStateProvider.getHandler(entityToRender);
        if (handler != null && handler.isDragon()) {
            DragonMovementData movement = handler.getMovementData();
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

            if (dragon != null) {
                dragon.isInInventory = true;
            }

            RenderSystem.runAsFancy(runnable);

            if (dragon != null) {
                dragon.isInInventory = false;
            }

            dragon_survival$storedXAngle = 0;
            dragon_survival$storedYAngle = 0;

            movement.bodyYaw = bodyYaw;
            movement.headYaw = headYaw;
            movement.headPitch = headPitch;
            movement.deltaMovement = deltaMovement;
            movement.deltaMovementLastFrame = deltaMovementLastFrame;
        } else {
            original.call(runnable);
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

    // If we are a dragon, we don't want to angle the entire entity when rendering it with a follows mouse command (like vanilla does).
    // Instead, we angle just the dragon's head to follow the given angle. So we modify the angles to be zero if we are a dragon and capture them to use them later.
    // In 1.21, we use a ModifyArgs here, but the version of mixin we use on 1.20 doesn't support it. So use a Redirect instead.
    // Also, we need remap = false since forge does some remapping of their own of this function that we need to ignore.
    @Redirect(method = "renderEntityInInventoryFollowsMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/InventoryScreen;renderEntityInInventoryFollowsAngle(Lnet/minecraft/client/gui/GuiGraphics;IIIFFLnet/minecraft/world/entity/LivingEntity;)V", remap = false))
    private static void dragon_survival$redirectEntityAngling(GuiGraphics graphics, int x, int y, int scale, float xAngle, float yAngle, LivingEntity entity) {
        DragonStateHandler handler = DragonStateProvider.getHandler(entity);
        if (handler != null && handler.isDragon()) {
            dragon_survival$storedXAngle = xAngle;
            dragon_survival$storedYAngle = yAngle;

            InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, x, y, scale, 0.0F, 0.0F, entity);
            return;
        }

        InventoryScreen.renderEntityInInventoryFollowsAngle(graphics, x, y, scale, xAngle, yAngle, entity);
    }
}