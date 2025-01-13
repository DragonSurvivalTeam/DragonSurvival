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

public class DragonBoots<T extends Entity> extends EntityModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("dragonsurvival", "dragon_shoe"), "main");
    public final ModelPart left_shoe;
    public final ModelPart right_shoe;

    public DragonBoots(ModelPart root) {
        this.left_shoe = root.getChild("left_shoe");
        this.right_shoe = root.getChild("right_shoe");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition left_shoe = partdefinition.addOrReplaceChild("left_shoe", CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)).mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));

        PartDefinition shoe_l1 = left_shoe.addOrReplaceChild("shoe_l1", CubeListBuilder.create().texOffs(14, 53).addBox(-0.5F, -0.5F, -1.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.75F)), PartPose.offsetAndRotation(0.7F, 9.6F, 2.6F, 0.166F, 0.126F, 0.0159F));

        PartDefinition shoe_l2 = left_shoe.addOrReplaceChild("shoe_l2", CubeListBuilder.create().texOffs(0, 53).addBox(-0.5F, -0.5F, -1.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.75F)), PartPose.offsetAndRotation(0.7F, 6.6F, 2.6F, 0.5813F, 0.1073F, 0.0206F));

        PartDefinition right_shoe = partdefinition.addOrReplaceChild("right_shoe", CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.75F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

        PartDefinition shoe_r1 = right_shoe.addOrReplaceChild("shoe_r1", CubeListBuilder.create().texOffs(14, 53).mirror().addBox(-1.5F, -0.5F, -1.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.75F)).mirror(false), PartPose.offsetAndRotation(-0.7F, 9.6F, 2.6F, 0.166F, -0.126F, -0.0159F));

        PartDefinition shoe_r2 = right_shoe.addOrReplaceChild("shoe_r2", CubeListBuilder.create().texOffs(0, 53).mirror().addBox(-1.5F, -0.5F, -1.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.75F)).mirror(false), PartPose.offsetAndRotation(-0.7F, 6.6F, 2.6F, 0.5813F, -0.1073F, -0.0206F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(@NotNull Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        left_shoe.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        right_shoe.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}