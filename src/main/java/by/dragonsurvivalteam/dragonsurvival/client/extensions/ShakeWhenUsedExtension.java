package by.dragonsurvivalteam.dragonsurvival.client.extensions;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ShakeWhenUsedExtension implements IClientItemExtensions {
    private double startTime = 0.0F;

    @Override
    public boolean applyForgeHandTransform(@NotNull PoseStack poseStack, @NotNull LocalPlayer player, @NotNull HumanoidArm arm, @NotNull ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        if (player.getUseItemRemainingTicks() > 0) {
            if (startTime == 0.0F) {
                startTime = Blaze3D.getTime();
            }

            Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().applyItemArmTransform(poseStack, arm, equipProcess);

            double time = (Blaze3D.getTime() - startTime) * 10.0;
            float shakeX = (float) Math.sin(time) * 0.1F;
            float shakeZ = (float) Math.cos(time) * 0.1F;
            poseStack.translate(shakeX, 0, shakeZ);
            return true;
        }

        startTime = 0.0F;

        return false;
    }
}
