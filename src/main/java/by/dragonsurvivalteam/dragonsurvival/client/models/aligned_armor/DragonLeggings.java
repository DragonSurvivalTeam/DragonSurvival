package by.dragonsurvivalteam.dragonsurvival.client.models.aligned_armor;
// Made with Blockbench 4.10.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class DragonLeggings<T extends Entity> extends EntityModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("dragonsurvival", "dragon_leggings"), "main");
    public final ModelPart left_leg;
    public final ModelPart right_leg;

    public DragonLeggings(ModelPart root) {
        this.left_leg = root.getChild("left_leg");
        this.right_leg = root.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)).mirror(false), PartPose.offset(5.0F, 2.0F, 0.0F));

        PartDefinition leg_l1 = left_leg.addOrReplaceChild("leg_l1", CubeListBuilder.create().texOffs(0, 53).addBox(-0.5F, -0.5F, -1.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.75F)), PartPose.offsetAndRotation(1.6F, -1.7F, 0.6F, 0.6333F, 0.2489F, 0.1789F));

        PartDefinition leg_l2 = left_leg.addOrReplaceChild("leg_l2", CubeListBuilder.create().texOffs(14, 53).addBox(-0.5F, -0.5F, -1.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.75F)), PartPose.offsetAndRotation(1.6F, 2.3F, 0.6F, 0.1855F, 0.3435F, 0.0631F));

        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(40, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

        PartDefinition leg_r1 = right_arm.addOrReplaceChild("leg_r1", CubeListBuilder.create().texOffs(14, 53).mirror().addBox(-1.5F, -0.5F, -1.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.75F)).mirror(false), PartPose.offsetAndRotation(-1.6F, 2.3F, 0.6F, 0.1855F, -0.3435F, -0.0631F));

        PartDefinition leg_r2 = right_arm.addOrReplaceChild("leg_r2", CubeListBuilder.create().texOffs(0, 53).mirror().addBox(-1.5F, -0.5F, -1.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.75F)).mirror(false), PartPose.offsetAndRotation(-1.6F, -1.7F, 0.6F, 0.6333F, -0.2489F, -0.1789F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        left_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        right_leg.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}