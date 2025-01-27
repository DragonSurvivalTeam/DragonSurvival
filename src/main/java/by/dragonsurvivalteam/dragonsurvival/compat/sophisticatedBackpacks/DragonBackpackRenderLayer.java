package by.dragonsurvivalteam.dragonsurvival.compat.sophisticatedBackpacks;

import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.BackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.backpack.wrapper.IBackpackWrapper;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.BackpackModelManager;
import net.p3pp3rf1y.sophisticatedbackpacks.client.render.IBackpackModel;
import net.p3pp3rf1y.sophisticatedbackpacks.util.PlayerInventoryProvider;
import org.joml.*;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.lang.Math;


public class DragonBackpackRenderLayer extends GeoRenderLayer<DragonEntity> {

    @Translation(key = "backpack_offset_x", type = Translation.Type.CONFIGURATION, comments = "Backpack X offset")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"compat", "sophisticated backpacks"}, key = "backpack_offset_x")
    @ConfigRange(min = -1000, max = 1000)
    public static double model_offset_x = 0;

    @Translation(key = "backpack_offset_y", type = Translation.Type.CONFIGURATION, comments = "Backpack Y offset")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"compat", "sophisticated backpacks"}, key = "backpack_offset_y")
    @ConfigRange(min = -1000, max = 1000)
    public static double model_offset_y = 0.65;

    @Translation(key = "backpack_offset_z", type = Translation.Type.CONFIGURATION, comments = "Backpack Z offset")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"compat", "sophisticated backpacks"}, key = "backpack_offset_z")
    @ConfigRange(min = -1000, max = 1000)
    public static double model_offset_z = -0.3;


    @Translation(key = "backpack_rot_x", type = Translation.Type.CONFIGURATION, comments = "Backpack X rotation")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"compat", "sophisticated backpacks"}, key = "backpack_rot_x")
    @ConfigRange(min = -180, max = 180)
    public static double model_rot_x = 90;

    @Translation(key = "backpack_rot_y", type = Translation.Type.CONFIGURATION, comments = "Backpack Y rotation")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"compat", "sophisticated backpacks"}, key = "backpack_rot_y")
    @ConfigRange(min = -180, max = 180)
    public static double model_rot_y = 0;

    @Translation(key = "backpack_rot_z", type = Translation.Type.CONFIGURATION, comments = "Backpack Z rotation")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"compat", "sophisticated backpacks"}, key = "backpack_rot_z")
    @ConfigRange(min = -180, max = 180)
    public static double model_rot_z = 0;
    

    @Translation(key = "backpack_scale", type = Translation.Type.CONFIGURATION, comments = "Backpack scale")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"compat", "sophisticated backpacks"}, key = "backpack_scale")
    @ConfigRange(min = 0.01, max = 10000)
    public static double model_scale = 0.9;


    public DragonBackpackRenderLayer(GeoEntityRenderer<DragonEntity> renderer) {

        super(renderer);
    }

    @Override
    public void renderForBone(PoseStack poseStack, DragonEntity animatable, GeoBone bone, RenderType renderType,
                              MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {


        if(!bone.getName().equalsIgnoreCase("torso")) return;

        Player player = animatable.getPlayer();

        if(player == null) return;


        PlayerInventoryProvider.get().getBackpackFromRendered(player).ifPresent(backpackRenderInfo -> {

            poseStack.pushPose();

            ItemStack backpack = backpackRenderInfo.getBackpack();
            IBackpackWrapper wrapper = BackpackWrapper.fromStack(backpack);

            int clothColor = wrapper.getMainColor();
            int borderColor = wrapper.getAccentColor();
            IBackpackModel model = BackpackModelManager.getBackpackModel(backpack.getItem());

            transformModel(poseStack);

            model.render(null, player, poseStack, bufferSource, packedLight, clothColor, borderColor, backpack.getItem(), wrapper.getRenderInfo());

            poseStack.popPose();

        });


    }

    private void transformModel(PoseStack poseStack) {

        Quaternionf rotation = new Quaternionf();

        rotation.rotateX((float) Math.toRadians(model_rot_x));
        rotation.rotateY((float) Math.toRadians(model_rot_y));
        rotation.rotateZ((float) Math.toRadians(model_rot_z));
        poseStack.translate(model_offset_x, model_offset_y, model_offset_z);
        float s = (float) model_scale;
        poseStack.scale(s, s, s);
        
        poseStack.rotateAround(rotation, 0, 1, 0);

    }
}
