package by.dragonsurvivalteam.dragonsurvival.compat.sophisticatedBackpacks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.compat.Compat;
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
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import org.joml.*;
import org.joml.Math;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

import java.util.List;
import java.util.Optional;


public class DragonBackpackRenderLayer extends GeoRenderLayer<DragonEntity> {

    @Translation(key = "render_backpack", type = Translation.Type.CONFIGURATION, comments = "enable / disable backpack rendering")
    @ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "render_backpack")
    public static Boolean renderBackpack = true;

    private boolean isCurioLoaded = false;

    public DragonBackpackRenderLayer(GeoEntityRenderer<DragonEntity> renderer) {

        super(renderer);

        isCurioLoaded = Compat.isModLoaded("curios");
    }

    @Override
    public void renderForBone(PoseStack poseStack, DragonEntity animatable, GeoBone bone, RenderType renderType,
                              MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        if(!renderBackpack) return;

        if(!bone.getName().equalsIgnoreCase("BackpackBone")) return;

        Player player = animatable.getPlayer();

        if(player == null) return;

        DragonStateHandler handler = DragonStateProvider.getData(player);

        Optional<PlayerInventoryProvider.RenderInfo> backpackRenderInfo = getRenderInfo(player);

        if(backpackRenderInfo.isEmpty()) return;

        poseStack.pushPose();

        ItemStack backpack = backpackRenderInfo.get().getBackpack();
        IBackpackWrapper wrapper = BackpackWrapper.fromStack(backpack);

        int clothColor = wrapper.getMainColor();
        int borderColor = wrapper.getAccentColor();
        IBackpackModel model = BackpackModelManager.getBackpackModel(backpack.getItem());

        Vec3 pos_offset = new Vec3(bone.getPivotX(), bone.getPivotY(), bone.getPivotZ());

        Vec3 rot_offset = Vec3.ZERO;

        Vec3 scale = new Vec3(1, 1, 1);

        if(handler.body().value().backpackOffsets().isPresent()) {
            DragonBody.BackpackOffsets backpackOffsets = handler.body().value().backpackOffsets().get();

            scale = backpackOffsets.scale();
            pos_offset = pos_offset.add(backpackOffsets.pos_offset());
            rot_offset = backpackOffsets.rot_offset();

        }

        transformModel(poseStack, pos_offset, rot_offset, scale);

        model.render(null, player, poseStack, bufferSource, packedLight, clothColor, borderColor, backpack.getItem(), wrapper.getRenderInfo());
        poseStack.popPose();


    }

    private Optional<PlayerInventoryProvider.RenderInfo> getRenderInfo(Player player) {

        Optional<ItemStack> backpackStack = Optional.empty();
        boolean isArmorSlot = false;
        boolean isVisible = true;

        if(isCurioLoaded) {
            backpackStack = getBackpackFromCurio(player);
        }

        if(backpackStack.isEmpty()) {
            backpackStack = getBackpackFromChestSlot(player);
            isArmorSlot = true;
        }

        if(backpackStack.isPresent()) {

            return Optional.of(new PlayerInventoryProvider.RenderInfo(backpackStack.get(), isArmorSlot));

        }

        return Optional.empty();

    }

    private void transformModel(PoseStack poseStack, Vec3 pos_offset, Vec3 rot_offset, Vec3 scale) {

        Vec3 rot = rot_offset.add(0, 0, 180);

        Quaternionf quat = new Quaternionf().rotationZYX((float) Math.toRadians(rot.x), (float) Math.toRadians(rot.y), (float) Math.toRadians(rot.z));

        poseStack.rotateAround(quat, 0, 1.1f, 0);

        pos_offset = pos_offset.scale(1 / 32f);

        // The backpack rendering is slightly offset to center the pivot in back middle
        poseStack.translate(pos_offset.x, -pos_offset.y + 0.5, -pos_offset.z - 0.1);

        poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);

    }


    private Optional<ItemStack> getBackpackFromCurio(Player player) {

        if(CuriosApi.getCuriosInventory(player).isPresent()) {

            List<SlotResult> curioBackSlots = CuriosApi.getCuriosInventory(player).get().findCurios("back");


            for(SlotResult backpackItem : curioBackSlots) {

                if(!backpackItem.slotContext().visible())
                    continue;

                ItemStack itemStack = backpackItem.stack();

                if(itemStack.getItem() instanceof BackpackItem) {
                    return Optional.of(itemStack);
                }

                return Optional.empty();
            }
        }
        return Optional.empty();

    }

    private Optional<ItemStack> getBackpackFromChestSlot(Player player) {


        ItemStack armorSlot = player.getInventory().armor.get(EquipmentSlot.CHEST.getIndex());

        if(armorSlot.getItem() instanceof BackpackItem) {
            return Optional.of(armorSlot);
        }

        return Optional.empty();

    }


}
