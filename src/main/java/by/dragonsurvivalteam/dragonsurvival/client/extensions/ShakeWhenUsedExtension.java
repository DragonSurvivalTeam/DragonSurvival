package by.dragonsurvivalteam.dragonsurvival.client.extensions;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

// FIXME :: This doesn't work currently, I think forge's old hook is broken (it seems to only hook into crossbow animation)
public class ShakeWhenUsedExtension implements IClientItemExtensions {
    private static final double SHAKE_SPEED = 10.0;
    private static final float SHAKE_DISTANCE = 0.1F;

    @Override
    public boolean applyForgeHandTransform(@NotNull PoseStack poseStack, @NotNull LocalPlayer player, @NotNull HumanoidArm arm, @NotNull ItemStack itemInHand, float partialTick, float equipProcess, float swingProcess) {
        InteractionHand hand = player.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        return applyUseShakeTransform(poseStack, player, hand, arm, itemInHand, equipProcess);
    }

    public static boolean applyUseShakeTransform(@NotNull final PoseStack poseStack, @NotNull final LocalPlayer player, @NotNull final InteractionHand hand, @NotNull final HumanoidArm arm, @NotNull final ItemStack itemInHand, final float equipProcess) {
        if (!player.isUsingItem() || player.getUseItemRemainingTicks() <= 0 || player.getUsedItemHand() != hand || !ItemStack.isSameItemSameComponents(player.getUseItem(), itemInHand)) {
            return false;
        }

        Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer().applyItemArmTransform(poseStack, arm, equipProcess);

        double time = Blaze3D.getTime() * SHAKE_SPEED;
        float shakeX = (float)Math.sin(time) * SHAKE_DISTANCE;
        float shakeZ = (float)Math.cos(time) * SHAKE_DISTANCE;
        poseStack.translate(shakeX, 0.0F, shakeZ);

        return true;
    }
}
