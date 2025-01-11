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

public class DragonHelmet<T extends Entity> extends EntityModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("dragonsurvival", "dragon_helmet"), "main");
    public final ModelPart head;

    public DragonHelmet(ModelPart root) {
        this.head = root.getChild("head");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.75F))
                .texOffs(32, 0).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition head_r1 = head.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(2, 34).mirror().addBox(-1.5F, -1.5F, 0.7F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.75F)).mirror(false), PartPose.offsetAndRotation(-3.6F, -6.7F, -1.1F, 0.2233F, -0.2129F, -0.0479F));
        PartDefinition head_r2 = head.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(5, 45).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.75F)), PartPose.offsetAndRotation(3.6F, -2.7F, 2.6F, 0.0F, 0.1309F, 0.0F));
        PartDefinition head_r3 = head.addOrReplaceChild("head_r3", CubeListBuilder.create().texOffs(5, 45).mirror().addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.75F)).mirror(false), PartPose.offsetAndRotation(-3.6F, -2.7F, 2.6F, 0.0F, -0.1745F, 0.0F));
        PartDefinition head_r4 = head.addOrReplaceChild("head_r4", CubeListBuilder.create().texOffs(2, 34).addBox(-1.5F, -1.5F, 0.7F, 3.0F, 3.0F, 8.0F, new CubeDeformation(0.75F)), PartPose.offsetAndRotation(3.6F, -6.7F, -1.1F, 0.2233F, 0.2129F, 0.0479F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        head.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}