package by.dragonsurvivalteam.dragonsurvival.client.skins;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks.AncientDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonSkins {
    protected static boolean initialized = false;
    public static NetSkinLoader skinLoader = new GithubSkinLoaderAPI();
    private static final ArrayList<String> hasFailedFetch = new ArrayList<>();
    public static HashMap<ResourceKey<DragonStage>, HashMap<String, SkinObject>> SKIN_USERS = new HashMap<>();
    public static HashMap<String, CompletableFuture<ResourceLocation>> playerSkinCache = new HashMap<>();
    public static HashMap<String, CompletableFuture<ResourceLocation>> playerGlowCache = new HashMap<>();
    private static double lastSkinFetchAttemptTime = 0;
    private static int numSkinFetchAttempts = 0;

    public static boolean playerSkinFailedToFetch(String playerName, ResourceKey<DragonStage> stage) {
        String playerKey = playerName + "_" + stage.location().getPath();
        return hasFailedFetch.contains(playerKey);
    }

    public static boolean playerSkinOrGlowFetchingInProgress(String playerName, ResourceKey<DragonStage> stage) {
        String playerKey = playerName + "_" + stage.location().getPath();
        return playerSkinCache.containsKey(playerKey) && !playerSkinCache.get(playerKey).isDone() || playerGlowCache.containsKey(playerKey) && !playerGlowCache.get(playerKey).isDone();
    }

    public static @Nullable ResourceLocation getPlayerSkin(String playerName, ResourceKey<DragonStage> dragonStage) {
        String skinKey = playerName + "_" + dragonStage.location().getPath();

        if (playerSkinCache.containsKey(skinKey) && playerSkinCache.get(skinKey) != null) {
            if(playerSkinCache.get(skinKey).isDone()) {
                return playerSkinCache.get(skinKey).join();
            }
        }

        if (!hasFailedFetch.contains(skinKey) && !playerSkinCache.containsKey(skinKey)) {
            playerSkinCache.put(skinKey, CompletableFuture.supplyAsync(() -> fetchSkinFile(playerName, dragonStage)));
        }

        return null;
    }

    public static @Nullable ResourceLocation getPlayerGlow(String playerName, ResourceKey<DragonStage> dragonStage) {
        String skinKey = playerName + "_" + dragonStage.location().getPath();

        if (playerGlowCache.containsKey(skinKey) && playerGlowCache.get(skinKey) != null) {
            if(playerGlowCache.get(skinKey).isDone()) {
                return playerGlowCache.get(skinKey).join();
            }
        } else {
            playerGlowCache.put(skinKey, CompletableFuture.supplyAsync(() -> fetchSkinFile(playerName, dragonStage, "glow")));
        }

        return null;
    }


    public static @Nullable ResourceLocation getPlayerSkin(Player player, ResourceKey<DragonStage> dragonStage) {
        String playerKey = player.getGameProfile().getName() + "_" + dragonStage.location().getPath();
        boolean renderCustomSkin = DragonStateProvider.getData(player).getSkinData().renderCustomSkin;

        if ((ClientDragonRenderer.renderOtherPlayerSkins || player == DragonSurvival.PROXY.getLocalPlayer()) && renderCustomSkin) {
            if (playerSkinCache.containsKey(playerKey) && playerSkinCache.get(playerKey) != null) {
                if(playerSkinCache.get(playerKey).isDone()) {
                    return playerSkinCache.get(playerKey).join();
                }
            }

            if (!hasFailedFetch.contains(playerKey) && !playerSkinCache.containsKey(playerKey)) {
                playerSkinCache.put(playerKey, CompletableFuture.supplyAsync(() -> fetchSkinFile(player, dragonStage)));
            }
        }

        return null;
    }

    public static @Nullable ResourceLocation fetchSkinFile(final String playerName, final ResourceKey<DragonStage> stage, final String... extra) {
        String playerKey = playerName + "_" + stage.location().getPath();
        String[] text = ArrayUtils.addAll(new String[]{playerKey}, extra);

        String resourceName = StringUtils.join(text, "_");
        ResourceLocation resourceLocation = ResourceLocation.fromNamespaceAndPath(MODID, resourceName.toLowerCase(Locale.ENGLISH));

        try (SimpleTexture simpleTexture = new SimpleTexture(resourceLocation)) {
            if (Minecraft.getInstance().getTextureManager().getTexture(resourceLocation, simpleTexture) != simpleTexture) {
                return resourceLocation;
            }
        }

        HashMap<String, SkinObject> playerSkinMap = SKIN_USERS.getOrDefault(stage, null);

        // Wait an increasing amount of time depending on the number of failed attempts
        if (playerSkinMap == null && lastSkinFetchAttemptTime + numSkinFetchAttempts < Blaze3D.getTime() && numSkinFetchAttempts < 10) {
            DragonSurvival.LOGGER.warn("Customs skins are not yet fetched, re-fetching...");
            init();

            numSkinFetchAttempts++;
            lastSkinFetchAttemptTime = Blaze3D.getTime();

            playerSkinMap = SKIN_USERS.getOrDefault(stage, null);

            if (playerSkinMap == null) {
                DragonSurvival.LOGGER.error("Custom skins could not be fetched");
            }
        }

        String skinName = StringUtils.join(ArrayUtils.addAll(new String[]{playerName}, extra), "_");
        SkinObject skin = null;

        if (playerSkinMap != null) {
            skin = playerSkinMap.getOrDefault(skinName, null);
        }

        // Only use the API to get the names (for the random button)
        if (skinLoader instanceof GithubSkinLoader gitHubOld) {
            try (InputStream imageStream = gitHubOld.querySkinImage(skinName, stage)) {
                return readSkin(imageStream, resourceLocation);
            } catch (IOException exception) {
                return fetchSkinResource(playerName, stage, extra, exception, playerKey);
            }
        }

        if (skin == null) {
            return null;
        }

        try (InputStream imageStream = skinLoader.querySkinImage(skin)) {
            return readSkin(imageStream, resourceLocation);
        } catch (IOException exception) {
            return fetchSkinResource(playerName, stage, extra, exception, playerKey);
        }
    }

    private static @Nullable ResourceLocation fetchSkinResource(final String playerName, final ResourceKey<DragonStage> stage, final String[] extra, final IOException exception, final String playerKey) {
        if (stage == AncientDatapack.ancient) {
            DragonSurvival.LOGGER.warn("Failed to get skin information for ancient stage: [{}]. Falling back to using adult stage.", exception.getMessage());
            return fetchSkinFile(playerName, DragonStages.adult, extra);
        }

        boolean isNormalSkin = extra == null || extra.length == 0;
        handleSkinFetchError(playerKey, isNormalSkin);
        return null;
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
                DragonSurvival.LOGGER.info("Custom skin for user {} doesn't exist", playerKey);
                hasFailedFetch.add(playerKey);
            }
        }
    }

    public static ResourceLocation fetchSkinFile(Player playerEntity, ResourceKey<DragonStage> dragonStage, String... extra) {
        return fetchSkinFile(playerEntity.getGameProfile().getName(), dragonStage, extra);
    }

    public static @Nullable ResourceLocation getGlowTexture(Player player, ResourceKey<DragonStage> dragonStage) {
        String playerKey = player.getGameProfile().getName() + "_" + dragonStage.location().getPath();
        boolean renderCustomSkin = DragonStateProvider.getData(player).getSkinData().renderCustomSkin;

        if ((ClientDragonRenderer.renderOtherPlayerSkins || player == DragonSurvival.PROXY.getLocalPlayer()) && playerSkinCache.containsKey(playerKey) && renderCustomSkin) {
            if (playerGlowCache.containsKey(playerKey)) {
                if(playerGlowCache.get(playerKey).isDone()) {
                    return playerGlowCache.get(playerKey).join();
                }
            } else {
                playerGlowCache.put(playerKey, CompletableFuture.supplyAsync(() -> fetchSkinFile(player, dragonStage, "glow")));
            }
        }

        return null;
    }

    public static void init() {
        init(false);
    }

    public static void init(boolean force) {
        if (initialized && !force) {
            return;
        }

        initialized = true;
        Collection<SkinObject> skins;
        invalidateSkins();
        String currentLanguage = Minecraft.getInstance().getLanguageManager().getSelected();
        NetSkinLoader first, second;

        if (currentLanguage.equals("zh_cn")) {
            first = new GitcodeSkinLoader();
            second = new GithubSkinLoader();
        } else {
            first = new GithubSkinLoader();
            second = new GitcodeSkinLoader();
        }

        if (!first.ping()) {
            if (!second.ping()) {
                DragonSurvival.LOGGER.warn("Unable to connect to skin database.");
                return;
            }

            first = second;
        }

        skinLoader = first;
        skins = skinLoader.querySkinList();

        if (skins != null) {
            parseSkinObjects(skins);
        }
    }

    private static void parseSkinObject(SkinObject skin) {
        boolean isGlow = false;
        String skinName = skin.name;

        skinName = skin.name.substring(0, skinName.indexOf("."));

        if (skinName.endsWith("_glow")) {
            isGlow = true;
            skinName = skin.name.substring(0, skinName.lastIndexOf("_"));
        }

        if (skinName.contains("_")) {
            String name = skinName.substring(0, skinName.lastIndexOf("_"));

            if (isGlow) {
                name += "_glow";
            }

            ResourceKey<DragonStage> dragonStage = parseResourceKeyFromName(skin.name);
            if(dragonStage == null) {
                return;
            }

            if (!SKIN_USERS.containsKey(dragonStage)) {
                SKIN_USERS.put(dragonStage, new HashMap<>());
            }

            skin.short_name = name;
            skin.glow = isGlow;
            SKIN_USERS.get(dragonStage).putIfAbsent(name, skin);
        }
    }

    public static ResourceKey<DragonStage> parseResourceKeyFromName(String name) {
        String skinName = name.substring(0, name.indexOf("."));

        if (skinName.endsWith("_glow")) {
            skinName = name.substring(0, skinName.lastIndexOf("_"));
        }

        if (skinName.contains("_")) {
            String level = skinName.substring(skinName.lastIndexOf("_") + 1);
            try {
                return ResourceKey.create(DragonStage.REGISTRY, DragonSurvival.res(level));
            } catch (ResourceLocationException exception) {
                DragonSurvival.LOGGER.warn("Could not parse dragon stage from the skin [{}] due to [{}]", name, exception.getMessage());
            }
        }

        return null;
    }

    public static void parseSkinObjects(Collection<SkinObject> skinObjects) {
        for (SkinObject skin : skinObjects) {
            parseSkinObject(skin);
        }
    }

    private static void invalidateSkins() {
        SKIN_USERS.clear();
        playerSkinCache.clear();
        playerGlowCache.clear();
        hasFailedFetch.clear();
    }

    public static boolean renderCustomSkin(final LocalPlayer player) {
        return DragonStateProvider.getData(player).getSkinData().renderCustomSkin;
    }
}