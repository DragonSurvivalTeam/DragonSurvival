package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonEditorObject.Texture;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset.SkinAgeGroup;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SkinCap;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Supplier;

public class DragonEditorHandler{
	public static ResourceLocation getSkinTexture(Player player, EnumSkinLayer layer, String key, AbstractDragonType type){
		if(Objects.equals(layer.name, "Extra") && layer != EnumSkinLayer.EXTRA){
			return getSkinTexture(player, EnumSkinLayer.EXTRA, key, type);
		}

		if(layer == EnumSkinLayer.BASE && (key.equalsIgnoreCase("Skin") || key.equalsIgnoreCase(SkinCap.defaultSkinValue))){
			DragonStateHandler handler = DragonUtils.getHandler(player);
			return getSkinTexture(player, layer, type.getTypeNameLowerCase() + "_base_" + handler.getLevel().ordinal(), type);
		}

		Texture[] texts = DragonEditorRegistry.CUSTOMIZATIONS.getOrDefault(type.getTypeNameUpperCase(), new HashMap<>()).getOrDefault(layer, new Texture[0]);

		for(Texture texture : texts){
			if(Objects.equals(texture.key, key)){
				return new ResourceLocation(texture.texture);
			}
		}

		return null;
	}

	public static Texture getSkin(Player player, EnumSkinLayer layer, String key, AbstractDragonType type){
		if(Objects.equals(layer.name, "Extra") && layer != EnumSkinLayer.EXTRA){
			return getSkin(player, EnumSkinLayer.EXTRA, key, type);
		}

		if(layer == EnumSkinLayer.BASE && (key.equalsIgnoreCase("Skin") || key.equalsIgnoreCase(SkinCap.defaultSkinValue))){
			return getSkin(player, layer, type.getTypeNameLowerCase() + "_base_" + DragonUtils.getDragonLevel(player).ordinal(), type);
		}

		Texture[] texts = DragonEditorRegistry.CUSTOMIZATIONS.getOrDefault(type.getTypeNameUpperCase(), new HashMap<>()).getOrDefault(layer, new Texture[0]);

		for(Texture texture : texts){
			if(Objects.equals(texture.key, key)){
				return texture;
			}
		}

		return null;
	}

	public static ArrayList<String> getKeys(AbstractDragonType type, EnumSkinLayer layers){
		if(Objects.equals(layers.name, "Extra") && layers != EnumSkinLayer.EXTRA){
			return getKeys(type, EnumSkinLayer.EXTRA);
		}

		ArrayList<String> list = new ArrayList<>();

		Texture[] texts = DragonEditorRegistry.CUSTOMIZATIONS.getOrDefault(type.getTypeNameUpperCase(), new HashMap<>()).getOrDefault(layers, new Texture[0]);
		for(Texture texture : texts){
			list.add(texture.key);
		}

		return list;
	}

	public static ArrayList<String> getKeys(Player player, EnumSkinLayer layers){
		return getKeys(DragonUtils.getDragonType(player), layers);
	}

	public static void generateSkinTextures(final DragonEntity dragon) {
		Player player = dragon.getPlayer();
		DragonStateHandler handler = DragonUtils.getHandler(player);

		if (!RenderSystem.isOnRenderThreadOrInit()) {
			RenderSystem.recordRenderCall(() -> genTextures(player, handler));
		} else {
			genTextures(player, handler);
		}
	}

	private static void genTextures(final Player player, final DragonStateHandler handler) {
		Set<DragonLevel> dragonLevels = handler.getSkinData().skinPreset.skinAges.keySet();

		for (DragonLevel dragonLevel : dragonLevels) {
			SkinAgeGroup skinAgeGroup = handler.getSkinData().skinPreset.skinAges.get(dragonLevel).get();

			NativeImage normal = new NativeImage(512, 512, true);
			NativeImage glow = new NativeImage(512, 512, true);

			for (EnumSkinLayer layer : EnumSkinLayer.values()) {
				LayerSettings settings = skinAgeGroup.layerSettings.get(layer).get();
				String selectedSkin = settings.selectedSkin;

				if (selectedSkin != null) {
					Texture skinTexture = getSkin(player, layer, selectedSkin, handler.getType());

					if (skinTexture != null) {
						float hueVal = settings.hue - skinTexture.average_hue;
						float satVal = settings.saturation;
						float brightVal = settings.brightness;

						try {
							ResourceLocation textureLocation = getSkinTexture(player, layer, selectedSkin, handler.getType());
							Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(textureLocation);

							if (resource.isEmpty()) {
								throw new IOException(String.format("Resource %s not found!", textureLocation.getPath()));
							}

							InputStream textureStream = resource.get().open();
							NativeImage tempColorPicker = NativeImage.read(textureStream);
							textureStream.close();

							for (int x = 0; x < tempColorPicker.getWidth(); x++) {
								for (int y = 0; y < tempColorPicker.getHeight(); y++) {
									Color color = getColor(settings, skinTexture, hueVal, satVal, brightVal, tempColorPicker, x, y);

									if (color == null) {
										continue;
									}

									if (color.getAlpha() != 0) {
										Supplier<NativeImage> g2 = settings.glowing ? () -> glow : () -> normal;
										g2.get().setPixelRGBA(x, y, color.getRGB());

										if (settings.glowing && layer == EnumSkinLayer.BASE) {
											normal.setPixelRGBA(x, y, color.getRGB());
										}
									}
								}
							}

							tempColorPicker.close();
						} catch (IOException e) {
							DragonSurvivalMod.LOGGER.error("An error occured while compiling the dragon skin texture", e);
						}
					}
				}
			}

			String uuid = player.getStringUUID();
			ResourceLocation dynamicNormalKey = new ResourceLocation(DragonSurvivalMod.MODID, "dynamic_normal_" + uuid + "_" + dragonLevel.name);
			ResourceLocation dynamicGlowKey = new ResourceLocation(DragonSurvivalMod.MODID, "dynamic_glow_" + uuid + "_" + dragonLevel.name);

			registerCompiledTexture(normal, dynamicNormalKey);
			registerCompiledTexture(glow, dynamicGlowKey);
		}

		handler.getSkinData().recompileSkin = false;
		handler.getSkinData().isCompiled = true;
	}

	@Nullable
	private static Color getColor(LayerSettings settings, Texture text, float hueVal, float satVal, float brightVal, NativeImage img, int x, int y){
		float[] hsb = new float[3];

		Color color = new Color(img.getPixelRGBA(x, y), true);
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);

		if(text.colorable){
			if(settings.glowing && hsb[0] == 0.5f && hsb[1] == 0.5f){
				return null;
			}

            hsb[0] = (float)(hsb[0] - hueVal);
			hsb[1] = (float)Mth.lerp(Math.abs(satVal - 0.5f) * 2 , hsb[1], satVal > 0.5f ? 1.0 : 0.0);
			hsb[2] = (float)Mth.lerp(Math.abs(brightVal - 0.5f) * 2, hsb[2], brightVal > 0.5f ? 1.0 : 0.0);
		}

		Color c = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), color.getAlpha());
	}

	private static void registerCompiledTexture(NativeImage image, ResourceLocation key){
		try (image) {
			// DEBUG :: Export the texture
//			if (key.toString().contains("dynamic_normal")) {
//				File file = new File(Minecraft.getInstance().gameDirectory, "texture");
//				file.mkdirs();
//				file = new File(file.getPath(), key.toString().replace(":", "_") + ".png");
//				image.writeToFile(file);
//			}
			if (Minecraft.getInstance().getTextureManager().getTexture(key, null) instanceof DynamicTexture texture) {
				texture.setPixels(image);
				texture.upload();
			} else {
				DynamicTexture layer = new DynamicTexture(image);
				Minecraft.getInstance().getTextureManager().register(key, layer);
			}
		} catch (Exception e) {
			DragonSurvivalMod.LOGGER.error(e);
		}
	}
}