package by.dragonsurvivalteam.dragonsurvival.compat.sophisticatedBackpacks;

import by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon.DragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.compat.ModID;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.geckolib.cache.model.GeoBone;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.PerBoneRender;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackBlockModel;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.Optional;
import java.util.function.BiConsumer;

public class DragonBackpackRenderLayer<R extends LivingEntityRenderState & GeoRenderState> extends GeoRenderLayer<DragonEntity, Void, R> {
    private static final String BONE = "BackpackBone";
    private static final DragonBody.BackpackOffsets DEFAULT_OFFSETS = DragonBody.BackpackOffsets.of(Vec3.ZERO, Vec3.ZERO, new Vec3(1, 1, 1));

    public DragonBackpackRenderLayer(final DragonRenderer<R> renderer) {
        super(renderer);
    }

    @Override
    public void addPerBoneRender(final RenderPassInfo<R> renderPassInfo, final BiConsumer<GeoBone, PerBoneRender<R>> perBoneRenderer) {
        if (!renderPassInfo.willRender() || !ClientConfig.renderBackpack) {
            return;
        }

        DragonRenderer.DragonRenderData renderData = renderPassInfo.renderState().getGeckolibData(DragonRenderer.DRAGON_RENDER_DATA);

        if (renderData == null || renderData.player() == null || renderData.handler() == null || !renderData.handler().isDragon()) {
            return;
        }

        ItemStack backpack = getBackpack(renderData.player());

        if (backpack.isEmpty()) {
            return;
        }

        Optional<GeoBone> backpackBone = renderPassInfo.model().getBone(BONE);

        if (backpackBone.isEmpty()) {
            return;
        }

        ItemStackRenderState backpackRenderState = createBackpackRenderState(backpack);
        DragonStateHandler handler = renderData.handler();

        perBoneRenderer.accept(backpackBone.get(), (passInfo, bone, renderTasks) -> submitBackpack(
            passInfo.poseStack(),
            backpackRenderState,
            handler,
            passInfo.renderState(),
            renderTasks,
            passInfo.packedLight()
        ));
    }

    private static ItemStackRenderState createBackpackRenderState(final ItemStack backpack) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStackRenderState renderState = new ItemStackRenderState();
        minecraft.getItemModelResolver().updateForTopItem(renderState, backpack, BackpackBlockModel.WORN, minecraft.level, null, 0);
        return renderState;
    }

    private static void submitBackpack(
        final PoseStack poseStack,
        final ItemStackRenderState backpackRenderState,
        final DragonStateHandler handler,
        final GeoRenderState renderState,
        final SubmitNodeCollector renderTasks,
        final int packedLight
    ) {
        DragonBody.BackpackOffsets offsets = handler.body().value().backpackOffsets().orElse(DEFAULT_OFFSETS);

        poseStack.pushPose();
        transformModel(poseStack, offsets.posOffset(), offsets.rotOffset(), offsets.scale());
        backpackRenderState.submit(poseStack, renderTasks, packedLight, OverlayTexture.NO_OVERLAY, getOutlineColor(renderState));
        poseStack.popPose();
    }

    private static void transformModel(final PoseStack poseStack, final Vec3 posOffset, final Vec3 rotOffset, final Vec3 scale) {
        poseStack.mulPose(Axis.ZP.rotationDegrees((float)rotOffset.z + 180.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees((float)rotOffset.y));
        poseStack.mulPose(Axis.XP.rotationDegrees((float)rotOffset.x));

        // Sophisticated Backpacks models are centered for humanoid backs; the dragon bone marks the attachment point.
        poseStack.translate(posOffset.x, -posOffset.y + 0.5, -posOffset.z - 0.1);
        poseStack.scale((float)scale.x, (float)scale.y, (float)scale.z);
    }

    private static int getOutlineColor(final GeoRenderState renderState) {
        return renderState instanceof EntityRenderState entityRenderState ? entityRenderState.outlineColor : 0;
    }

    private static ItemStack getBackpack(final Player player) {
        if (ModID.CURIOS.isLoaded()) {
            ItemStack curioBackpack = getBackpackFromCurios(player);

            if (!curioBackpack.isEmpty()) {
                return curioBackpack;
            }
        }

        return getBackpackFromChestSlot(player);
    }

    private static ItemStack getBackpackFromCurios(final Player player) {
        return CuriosApi.getCuriosInventory(player)
            .stream()
            .flatMap(handler -> handler.findCurios(ClientConfig.backpackSlot).stream())
            .filter(slotResult -> slotResult.slotContext().visible())
            .map(SlotResult::stack)
            .filter(DragonBackpackRenderLayer::isBackpack)
            .findFirst()
            .orElse(ItemStack.EMPTY);
    }

    private static ItemStack getBackpackFromChestSlot(final Player player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
        return isBackpack(stack) ? stack : ItemStack.EMPTY;
    }

    private static boolean isBackpack(final @Nullable ItemStack stack) {
        return stack != null && stack.getItem() instanceof BackpackItem;
    }
}
