package by.dragonsurvivalteam.dragonsurvival.client.skins;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;

import static by.dragonsurvivalteam.dragonsurvival.config.ClientConfig.skinTimeoutInSeconds;

public class DragonSkins{
	protected static boolean initialized = false;
	public static NetSkinLoader skinLoader = new GithubSkinLoaderAPI();
	private static final ArrayList<String> hasFailedFetch = new ArrayList<>();
	public static HashMap<DragonLevel, HashMap<String, SkinObject>> SKIN_USERS = new HashMap<>();
	public static HashMap<String, ResourceLocation> playerSkinCache = new HashMap<>();
	public static HashMap<String, ResourceLocation> playerGlowCache = new HashMap<>();
	public static long lastAttempt = 0;

	public static ResourceLocation getPlayerSkin(String playerName, DragonLevel level){
		String skinKey = playerName + "_" + level.name;
		if(playerSkinCache.containsKey(skinKey) && playerSkinCache.get(skinKey) != null){
			return playerSkinCache.get(skinKey);
		}

		if(!hasFailedFetch.contains(skinKey) && !playerSkinCache.containsKey(skinKey)){
			ResourceLocation texture = fetchSkinFile(playerName, level);

			if(texture != null){
				playerSkinCache.put(skinKey, texture);
				return texture;
			}
		}

		return null;
	}

	public static ResourceLocation getPlayerGlow(String playerName, DragonLevel level){
		String skinKey = playerName + "_" + level.name;
		if(playerGlowCache.containsKey(skinKey)){
			return playerGlowCache.get(skinKey);
		}else{
			ResourceLocation texture = fetchSkinFile(playerName, level, "glow");
			playerGlowCache.put(skinKey, texture);
			return texture;
		}
	}


	public static ResourceLocation getPlayerSkin(Player player, AbstractDragonType type, DragonLevel dragonStage){
		ResourceLocation texture = null;
		String playerKey = player.getGameProfile().getName() + "_" + dragonStage.name;

		boolean renderStage = renderStage(player, dragonStage);

		if((ClientDragonRender.renderOtherPlayerSkins || player == Minecraft.getInstance().player) && renderStage){
			if(playerSkinCache.containsKey(playerKey) && playerSkinCache.get(playerKey) != null){
				return playerSkinCache.get(playerKey);
			}

			if(!hasFailedFetch.contains(playerKey) && !playerSkinCache.containsKey(playerKey)){
				texture = fetchSkinFile(player, dragonStage);

				if(texture != null){
					playerSkinCache.put(playerKey, texture);
				}
			}
		}

		return texture;
	}

	public static ResourceLocation fetchSkinFile(final String playerName, final DragonLevel level, final String... extra) {
		String playerKey = playerName + "_" + level.name;
		String[] text = ArrayUtils.addAll(new String[]{playerKey}, extra);

		String resourceName = StringUtils.join(text, "_");
		ResourceLocation resourceLocation = new ResourceLocation(DragonSurvivalMod.MODID, resourceName.toLowerCase(Locale.ROOT));

		try (SimpleTexture simpleTexture = new SimpleTexture(resourceLocation)) {
			if (Minecraft.getInstance().getTextureManager().getTexture(resourceLocation, simpleTexture) != simpleTexture) {
				return resourceLocation;
			}
		}

		HashMap<String, SkinObject> playerSkinMap = SKIN_USERS.getOrDefault(level, null);

		Player p = Minecraft.getInstance().player;
		if (playerSkinMap == null && p != null && lastAttempt + (skinTimeoutInSeconds * 20L) < p.level.getGameTime()) {
			DragonSurvivalMod.LOGGER.warn("Customs skins are not yet fetched, re-fetching...");
			init();

			playerSkinMap = SKIN_USERS.getOrDefault(level, null);

			if (playerSkinMap == null) {
				DragonSurvivalMod.LOGGER.error("Custom skins could not be fetched");
				lastAttempt = p.level.getGameTime();
			}
		}

		String skinName = StringUtils.join(ArrayUtils.addAll(new String[]{playerName}, extra), "_");
		SkinObject skin = null;

		if (playerSkinMap != null) {
			skin = playerSkinMap.getOrDefault(skinName, null);
		}

		// Only use the API to get the names (for the random button)
		if (skinLoader instanceof GithubSkinLoader gitHubOld) {
			try (InputStream imageStream = gitHubOld.querySkinImage(skinName, level)) {
				return readSkin(imageStream, resourceLocation);
			} catch (IOException exception) {
				boolean isNormalSkin = extra == null || extra.length == 0;
				handleSkinFetchError(playerKey, isNormalSkin);
				return null;
			}
		}

		if (skin == null) {
			return null;
		}

		try (InputStream imageStream = skinLoader.querySkinImage(skin)) {
			return readSkin(imageStream, resourceLocation);
		} catch (IOException exception) {
			boolean isNormalSkin = extra == null || extra.length == 0;
			handleSkinFetchError(playerKey, isNormalSkin);
			return null;
		}
	}

	private static ResourceLocation readSkin(final InputStream imageStream, final ResourceLocation location) throws IOException {
		if (imageStream == null) {
			throw new IOException("Skin was not successfully fetched for [" + location + "]");
		}

		NativeImage customTexture = NativeImage.read(imageStream);
		Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(customTexture));
		return location;
	}

	private static void handleSkinFetchError(final String playerKey, boolean isNormalSkin) {
		// A failed attempt for fetching a glow skin should not result in no longer attempting to fetch the normal skin
		if (isNormalSkin) {
			if (!hasFailedFetch.contains(playerKey)) {
				DragonSurvivalMod.LOGGER.info("Custom skin for user {} doesn't exist", playerKey);
				hasFailedFetch.add(playerKey);
			}
		}
	}

	public static ResourceLocation fetchSkinFile(Player playerEntity, DragonLevel dragonStage, String... extra){
		return fetchSkinFile(playerEntity.getGameProfile().getName(), dragonStage, extra);
	}

	public static boolean renderStage(Player player, DragonLevel level){
		DragonStateHandler handler = DragonUtils.getHandler(player);

		return switch(level){
			case NEWBORN -> handler.getSkinData().renderNewborn;
			case YOUNG -> handler.getSkinData().renderYoung;
			case ADULT -> handler.getSkinData().renderAdult;
		};
	}

	public static ResourceLocation getGlowTexture(Player player, AbstractDragonType type, DragonLevel dragonStage){
		ResourceLocation texture = null;
		String playerKey = player.getGameProfile().getName() + "_" + dragonStage.name;
		boolean renderStage = renderStage(player, dragonStage);


		if((ClientDragonRender.renderOtherPlayerSkins || player == Minecraft.getInstance().player) && playerSkinCache.containsKey(playerKey) && renderStage){
			if(playerGlowCache.containsKey(playerKey)){
				return playerGlowCache.get(playerKey);
			}else{
				texture = fetchSkinFile(player, dragonStage, "glow");
				playerGlowCache.put(playerKey, texture);

				if(texture == null){
					DragonSurvivalMod.LOGGER.info("Custom glow for user {} doesn't exist", player.getGameProfile().getName());
				}
			}
		}

		return texture;
	}

	public static void init() {
		init(false);
	}
	public static void init(boolean force) {
		if (initialized && !force)
			return;
		initialized = true;
		Collection<SkinObject> skins;
		invalidateSkins();
		String currentLanguage = Minecraft.getInstance().getLanguageManager().getSelected().getCode();
		NetSkinLoader first, second;
		// FIXME :: Add config to choose between new and old GitHub API
		if (currentLanguage.equals("zh_cn")) {
			first = new GitcodeSkinLoader();
			second = new GithubSkinLoader();
		} else{
			first = new GithubSkinLoader();
			second = new GitcodeSkinLoader();
		}
		if (!first.ping()) {
			if (!second.ping())
			{
				DragonSurvivalMod.LOGGER.warn("Unable to connect to skin database.");
				return;
			}
			first = second;
		}
		skinLoader = first;
        skins = skinLoader.querySkinList();
		if (skins != null)
			parseSkinObjects(skins);
	}

	public static void parseSkinObjects(Collection<SkinObject> skinObjects) {
		for(SkinObject skin : skinObjects){
			boolean isGlow = false;
			String skinName = skin.name;

			skinName = skin.name.substring(0, skinName.indexOf("."));
			if (skinName.endsWith("_glow")){
				isGlow = true;
				skinName = skin.name.substring(0, skinName.lastIndexOf("_"));
			}
			if(skinName.contains("_")){
				String name = skinName.substring(0, skinName.lastIndexOf("_"));
				if (isGlow)
					name += "_glow";

				String level = skinName.substring(skinName.lastIndexOf("_") + 1);
				DragonLevel size = level.equalsIgnoreCase("adult") ? DragonLevel.ADULT : level.equalsIgnoreCase("young") ? DragonLevel.YOUNG : level.equalsIgnoreCase("newborn") ? DragonLevel.NEWBORN : null;

				if(size != null){
					if(!SKIN_USERS.containsKey(size)){
						SKIN_USERS.put(size, new HashMap<>());
					}
					skin.short_name = name;
					skin.glow = isGlow;
					SKIN_USERS.get(size).putIfAbsent(name, skin);
				}
			}
		}
	}

	private static void invalidateSkins() {
		SKIN_USERS.clear();
		playerSkinCache.clear();
		playerGlowCache.clear();
		hasFailedFetch.clear();
	}
}