package by.dragonsurvivalteam.dragonsurvival.compat.sophisticatedBackpacks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.compat.ModCheck;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.BackpackItem;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackModelManager;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.IBackpackModel;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Quaternionf;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;

public class DragonBackpackRenderLayer extends GeoRenderLayer<DragonEntity> {
    @Translation(key = "render_backpack", type = Translation.Type.CONFIGURATION, comments = "enable / disable backpack rendering")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_backpack")
    public static Boolean SHOULD_RENDER = true;

    @Translation(key = "backpack_slot", type = Translation.Type.CONFIGURATION, comments = "The curios slot which may contain the backpack (if Curios is installed)")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "backpack_slot")
    public static String CURIOS_SLOT = "back";

    private static final String BONE = "BackpackBone";

    private final boolean isCurioLoaded;

    public DragonBackpackRenderLayer(GeoEntityRenderer<DragonEntity> renderer) {
        super(renderer);

        isCurioLoaded = ModCheck.isModLoaded(ModCheck.CURIOS);
    }

    @Override
    public void renderForBone(PoseStack poseStack, DragonEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (!SHOULD_RENDER) {
            return;
        }

        if (!bone.getName().equals(BONE)) {
            return;
        }

        Player player = animatable.getPlayer();

        if (player == null) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return;
        }

        ItemStack backpack = getBackpack(player);

        if (backpack == null) {
            return;
        }

        IBackpackWrapper wrapper = BackpackWrapper.fromStack(backpack);

        int clothColor = wrapper.getMainColor();
        int borderColor = wrapper.getAccentColor();
        IBackpackModel model = BackpackModelManager.getBackpackModel(backpack.getItem());

        Vec3 posOffset = new Vec3(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());
        Vec3 rotOffset = Vec3.ZERO;
        Vec3 scale = new Vec3(1, 1, 1);

        if (handler.body().value().backpackOffsets().isPresent()) {
            DragonBody.BackpackOffsets backpackOffsets = handler.body().value().backpackOffsets().get();

            scale = backpackOffsets.scale();
            posOffset = posOffset.add(backpackOffsets.posOffset());
            rotOffset = backpackOffsets.rotOffset();
        }

        poseStack.pushPose();
        transformModel(poseStack, posOffset.scale(1 / 32f), rotOffset, scale);

        model.render(null, player, poseStack, bufferSource, packedLight, clothColor, borderColor, backpack.getItem(), wrapper.getRenderInfo());
        poseStack.popPose();
    }

    private void transformModel(final PoseStack poseStack, final Vec3 posOffset, final Vec3 rotOffset, final Vec3 scale) {
        Vec3 rot = rotOffset.add(0, 0, 180);
        Quaternionf quat = new Quaternionf().rotationZYX((float) Math.toRadians(rot.x), (float) Math.toRadians(rot.y), (float) Math.toRadians(rot.z));
        poseStack.rotateAround(quat, 0, 1.1f, 0);

        // The backpack rendering is slightly offset to center the pivot in back middle
        poseStack.translate(posOffset.x, -posOffset.y + 0.5, -posOffset.z - 0.1);
        poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);
    }

    private @Nullable ItemStack getBackpack(final Player player) {
        ItemStack backpack = null;

        if (isCurioLoaded) {
            backpack = getBackpackFromCurios(player);
        }

        if (backpack == null) {
            return getBackpackFromChestSlot(player);
        }

        return backpack;
    }

    private @Nullable ItemStack getBackpackFromCurios(final Player player) {
        if (CuriosApi.getCuriosInventory(player).isPresent()) {
            List<SlotResult> curioBackSlots = CuriosApi.getCuriosInventory(player).get().findCurios(CURIOS_SLOT);

            for (SlotResult slotItem : curioBackSlots) {
                if (!slotItem.slotContext().visible()) {
                    continue;
                }

                ItemStack stack = slotItem.stack();

                if (stack.getItem() instanceof BackpackItem) {
                    return stack;
                }

                return null;
            }
        }

        return null;
    }

    private ItemStack getBackpackFromChestSlot(final Player player) {
        ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);

        if (stack.getItem() instanceof BackpackItem) {
            return stack;
        }

        return null;
    }
}
