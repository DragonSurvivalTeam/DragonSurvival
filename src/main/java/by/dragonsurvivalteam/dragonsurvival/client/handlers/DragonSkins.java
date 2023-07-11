package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.GsonFactory;
import com.google.gson.Gson;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class DragonSkins{
	public static final String SKINS = "https://raw.githubusercontent.com/DragonSurvivalTeam/DragonSurvival/master/src/test/resources/";
	private static final String GITHUB_API = "https://api.github.com/repositories/280658566/contents/src/test/resources?ref=master";
	private static final ArrayList<String> hasFailedFetch = new ArrayList<>();
	public static HashMap<DragonLevel, ArrayList<String>> SKIN_USERS = new HashMap<>();
	public static HashMap<String, ResourceLocation> playerSkinCache = new HashMap<>();
	public static HashMap<String, ResourceLocation> playerGlowCache = new HashMap<>();

	public static ResourceLocation getPlayerSkin(String playerKey){
		if(playerSkinCache.containsKey(playerKey) && playerSkinCache.get(playerKey) != null){
			return playerSkinCache.get(playerKey);
		}

		if(!hasFailedFetch.contains(playerKey) && !playerSkinCache.containsKey(playerKey)){
			ResourceLocation texture = fetchSkinFile(playerKey);

			if(texture != null){
				playerSkinCache.put(playerKey, texture);
				return texture;
			}
		}

		return null;
	}

	public static ResourceLocation fetchSkinFile(String playerKey, String... extra){
		ResourceLocation resourceLocation = null;
		String[] text = ArrayUtils.addAll(new String[]{playerKey}, extra);
		String searchText = StringUtils.join(text, "_");

		try{
			InputStream inputStream = getStream(new URL(SKINS + searchText + ".png"));
			NativeImage customTexture = NativeImage.read(inputStream);
			resourceLocation = new ResourceLocation(DragonSurvivalMod.MODID, searchText.toLowerCase(Locale.ROOT));
			Minecraft.getInstance().getTextureManager().register(resourceLocation, new DynamicTexture(customTexture));
		}catch(IOException e){
			if(extra == null || extra.length == 0){ //Fetching glow layer failing must not affect normal skin fetches
				if(!hasFailedFetch.contains(playerKey)){
					DragonSurvivalMod.LOGGER.info("Custom skin for user {} doesn't exist", playerKey);
					hasFailedFetch.add(playerKey);
				}
			}

			return null;
		}

		return resourceLocation;
	}

	public static ResourceLocation getPlayerGlow(String playerKey){
		if(playerGlowCache.containsKey(playerKey)){
			return playerGlowCache.get(playerKey);
		}else{
			ResourceLocation texture = fetchSkinFile(playerKey, "glow");
			playerGlowCache.put(playerKey, texture);
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

	public static boolean renderStage(Player player, DragonLevel level){
		DragonStateHandler handler = DragonUtils.getHandler(player);

		return switch(level){
			case NEWBORN -> handler.getSkinData().renderNewborn;
			case YOUNG -> handler.getSkinData().renderYoung;
			case ADULT -> handler.getSkinData().renderAdult;
		};
	}

	public static ResourceLocation fetchSkinFile(Player playerEntity, DragonLevel dragonStage, String... extra){
		ResourceLocation resourceLocation = null;
		String name = playerEntity.getGameProfile().getName();
		String playerKey = playerEntity.getGameProfile().getName() + "_" + dragonStage.name;

		String[] text = ArrayUtils.addAll(new String[]{name, dragonStage.name}, extra);
		String searchText = StringUtils.join(text, "_");

		try{
			InputStream inputStream = getStream(new URL(SKINS + searchText + ".png"));
			NativeImage customTexture = NativeImage.read(inputStream);
			resourceLocation = new ResourceLocation(DragonSurvivalMod.MODID, searchText.toLowerCase(Locale.ROOT));
			Minecraft.getInstance().getTextureManager().register(resourceLocation, new DynamicTexture(customTexture));
		}catch(IOException e){
			if(extra == null || extra.length == 0){ //Fetching glow layer failing must not affect normal skin fetches
				if(!hasFailedFetch.contains(playerKey)){
					DragonSurvivalMod.LOGGER.info("Custom skin for user {} doesn't exist", name);
					hasFailedFetch.add(playerKey);
				}
			}

			return null;
		}

		return resourceLocation;
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

	public static void init(){
		try{
			Gson gson = GsonFactory.getDefault();
			URL url = new URL(GITHUB_API);
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			SkinObject[] je = gson.fromJson(reader, SkinObject[].class);

			for(SkinObject skin : je){
				String skinName = skin.name;

				if(skinName.contains("_")){
					String name = skinName.substring(0, skinName.lastIndexOf("_"));
					String level = skinName.substring(skinName.lastIndexOf("_") + 1, skinName.indexOf("."));
					DragonLevel size = level.equalsIgnoreCase("adult") ? DragonLevel.ADULT : level.equalsIgnoreCase("young") ? DragonLevel.YOUNG : level.equalsIgnoreCase("newborn") ? DragonLevel.NEWBORN : null;

					if(size != null){
						if(!SKIN_USERS.containsKey(size)){
							SKIN_USERS.put(size, new ArrayList<>());
						}

						if(!SKIN_USERS.get(size).contains(name)){
							SKIN_USERS.get(size).add(name);
						}
					}
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	private static InputStream getStream(URL url) throws IOException{
		URLConnection huc =  url.openConnection();
		huc.setConnectTimeout(15 * 1000);
		return huc.getInputStream();
	}

	private static class SkinObject{
		public String name;
		public int size;
		public String html_url;
	}
}