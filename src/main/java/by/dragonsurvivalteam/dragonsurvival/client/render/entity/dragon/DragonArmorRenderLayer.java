package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.awt.*;

public class DragonArmorRenderLayer extends GeoRenderLayer<DragonEntity> {
	private final GeoEntityRenderer<DragonEntity> renderer;

	public DragonArmorRenderLayer(final GeoEntityRenderer<DragonEntity> renderer) {
		super(renderer);
		this.renderer = renderer;
	}

	@Override
	public void render(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
		if (!ClientDragonRender.armorRenderLayer) {
			return;
		}

		Player player = animatable.getPlayer();

		if (player == null || player.isSpectator()) {
			return;
		}

		CoreGeoBone neck = ClientDragonRender.dragonArmorModel.getAnimationProcessor().getBone("Neck");

		if (neck != null) {
			neck.setHidden(false);
		}

		ResourceLocation helmetTexture = new ResourceLocation(DragonSurvivalMod.MODID, constructArmorTexture(player, EquipmentSlot.HEAD));
		ResourceLocation chestPlateTexture = new ResourceLocation(DragonSurvivalMod.MODID, constructArmorTexture(player, EquipmentSlot.CHEST));
		ResourceLocation legsTexture = new ResourceLocation(DragonSurvivalMod.MODID, constructArmorTexture(player, EquipmentSlot.LEGS));
		ResourceLocation bootsTexture = new ResourceLocation(DragonSurvivalMod.MODID, constructArmorTexture(player, EquipmentSlot.FEET));

		((DragonRenderer) renderer).isRenderLayers = true;

		renderArmorPiece(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, player.getItemBySlot(EquipmentSlot.HEAD), helmetTexture);
		renderArmorPiece(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, player.getItemBySlot(EquipmentSlot.CHEST), chestPlateTexture);
		renderArmorPiece(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, player.getItemBySlot(EquipmentSlot.LEGS), legsTexture);
		renderArmorPiece(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, player.getItemBySlot(EquipmentSlot.FEET), bootsTexture);

		((DragonRenderer) renderer).isRenderLayers = false;
	}

	private void renderArmorPiece(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final MultiBufferSource bufferSource, float partialTick, int packedLight, final ItemStack stack, final ResourceLocation texture) {
		if (animatable == null) {
			return;
		}

		if (stack == null || stack.isEmpty()) {
			return;
		}

		Color armorColor = new Color(1f, 1f, 1f);

		if (stack.getItem() instanceof DyeableArmorItem) {
			int colorCode = ((DyeableArmorItem) stack.getItem()).getColor(stack);
			armorColor = new Color(colorCode);
		}

		ClientDragonRender.dragonModel.setCurrentTexture(texture);
		ClientDragonRender.dragonArmor.copyPosition(animatable);
		RenderType type = renderer.getRenderType(animatable, texture, bufferSource, partialTick);
		VertexConsumer vertexConsumer = bufferSource.getBuffer(type);
		renderer.actuallyRender(poseStack, animatable, bakedModel, type, bufferSource, vertexConsumer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, armorColor.getRed() / 255F, armorColor.getGreen() / 255F, armorColor.getBlue() / 255F, 1F);
	}

	public static String constructArmorTexture(Player playerEntity, EquipmentSlot equipmentSlot){
		String texture = "textures/armor/";
		Item item = playerEntity.getItemBySlot(equipmentSlot).getItem();
		String texture2 = itemToResLoc(item);
		if (texture2 != null) {
			texture2 = texture + texture2;
			if (Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(DragonSurvivalMod.MODID, texture2)).isPresent()) {
				return texture2;
			}
		}
		if(item instanceof ArmorItem armorItem){
			ArmorMaterial armorMaterial = armorItem.getMaterial();
			if(armorMaterial instanceof ArmorMaterials){
				if(armorMaterial == ArmorMaterials.NETHERITE)
					texture += "netherite_";
				else if(armorMaterial == ArmorMaterials.DIAMOND)
					texture += "diamond_";
				else if(armorMaterial == ArmorMaterials.IRON)
					texture += "iron_";
				else if(armorMaterial == ArmorMaterials.LEATHER)
					texture += "leather_";
				else if(armorMaterial == ArmorMaterials.GOLD)
					texture += "gold_";
				else if(armorMaterial == ArmorMaterials.CHAIN)
					texture += "chainmail_";
				else if(armorMaterial == ArmorMaterials.TURTLE)
					texture += "turtle_";
				else
					return texture + "empty_armor.png";

				texture += "dragon_";
				switch(equipmentSlot){
					case HEAD -> texture += "helmet";
					case CHEST -> texture += "chestplate";
					case LEGS -> texture += "leggings";
					case FEET -> texture += "boots";
				}
				texture += ".png";
				return stripInvalidPathChars(texture);
			}
			int defense = armorItem.getDefense();
			switch(equipmentSlot){
				case FEET -> texture += Mth.clamp(defense, 1, 4) + "_dragon_boots";
				case CHEST -> texture += Mth.clamp(defense / 2, 1, 4) + "_dragon_chestplate";
				case HEAD -> texture += Mth.clamp(defense, 1, 4) + "_dragon_helmet";
				case LEGS -> texture += Mth.clamp((int)(defense / 1.5), 1, 4) + "_dragon_leggings";
			}
			texture += ".png";
			return stripInvalidPathChars(texture);
		}
		return texture + "empty_armor.png";
	}
	
	public static String itemToResLoc(Item item) {
		if (item == Items.AIR) return null;

		ResourceLocation registryName = ResourceHelper.getKey(item);
		if (registryName != null) {
			String[] reg = registryName.toString().split(":");
			String loc = reg[0] + "/" + reg[1] + ".png";
			return stripInvalidPathChars(loc);
		}
		return null;
	}
	public static String stripInvalidPathChars(String loc) {
		// filters certain characters (non [a-z0-9/._-]) to prevent crashes
		// this probably should never be relevant, but you can never be too safe
		loc = loc.chars()
			.filter(ch -> ResourceLocation.validPathChar((char) ch))
			.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
			.toString();
		return loc;
	}
}