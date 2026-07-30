package by.dragonsurvivalteam.dragonsurvival.client.render.blocks;

import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonVaultBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DragonVaultRenderer implements BlockEntityRenderer<DragonVaultBlockEntity> {
    private final ItemRenderer itemRenderer;

    public DragonVaultRenderer(final BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            final DragonVaultBlockEntity vault,
            final float partialTick,
            @NotNull final PoseStack poseStack,
            @NotNull final MultiBufferSource buffer,
            final int packedLight,
            final int packedOverlay
    ) {
        ItemStack displayItem = vault.getDisplayItem();
        if (displayItem.isEmpty() || vault.getLevel() == null) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.4F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(partialTick, vault.getPreviousSpin(), vault.getSpin())));
        itemRenderer.renderStatic(
                displayItem,
                ItemDisplayContext.GROUND,
                packedLight,
                packedOverlay,
                poseStack,
                buffer,
                vault.getLevel(),
                0
        );
        poseStack.popPose();
    }
}
