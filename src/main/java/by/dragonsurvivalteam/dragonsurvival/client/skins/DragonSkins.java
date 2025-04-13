package by.dragonsurvivalteam.dragonsurvival.client.skins;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks.AncientDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
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
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonSkins {
    public static final HashMap<ResourceKey<DragonStage>, HashMap<String, SkinObject>> USER_SKINS = new HashMap<>();
    public static final HashMap<String, CompletableFuture<ResourceLocation>> SKIN_CACHE = new HashMap<>();
    public static final HashMap<String, CompletableFuture<ResourceLocation>> GLOW_CACHE = new HashMap<>();

    public static final List<Supplier<NetSkinLoader>> SKIN_LOADERS = List.of(GithubSkinLoader::new, GiteeSkinLoader::new);

    public static NetSkinLoader skinLoader = new GithubSkinLoaderAPI();

    private static final ArrayList<String> hasFailedFetch = new ArrayList<>();
    private static double lastSkinFetchAttemptTime;
    private static int numSkinFetchAttempts;
    private static boolean initialized;

    public static boolean playerSkinOrGlowFetchingInProgress(String playerName, ResourceKey<DragonStage> stage) {
        String playerKey = playerName + "_" + stage.location().getPath();
        return SKIN_CACHE.containsKey(playerKey) && !SKIN_CACHE.get(playerKey).isDone() || GLOW_CACHE.containsKey(playerKey) && !GLOW_CACHE.get(playerKey).isDone();
    }

    public static boolean fetchHasFailed(String playerName, ResourceKey<DragonStage> stage) {
        String playerKey = playerName + "_" + stage.location().getPath();
        return hasFailedFetch.contains(playerKey);
    }

    public static @Nullable ResourceLocation getPlayerSkin(String playerName, ResourceKey<DragonStage> dragonStage) {
        String skinKey = playerName + "_" + dragonStage.location().getPath();

        if (SKIN_CACHE.containsKey(skinKey) && SKIN_CACHE.get(skinKey) != null) {
            if (SKIN_CACHE.get(skinKey).isDone()) {
                return SKIN_CACHE.get(skinKey).join();
            }
        }

        if (!hasFailedFetch.contains(skinKey) && !SKIN_CACHE.containsKey(skinKey)) {
            SKIN_CACHE.put(skinKey, CompletableFuture.supplyAsync(() -> fetchSkinFile(playerName, dragonStage)));
        }

        return null;
    }

    public static @Nullable ResourceLocation getPlayerGlow(String playerName, ResourceKey<DragonStage> dragonStage) {
        String skinKey = playerName + "_" + dragonStage.location().getPath();

        if (GLOW_CACHE.containsKey(skinKey) && GLOW_CACHE.get(skinKey) != null) {
            if (GLOW_CACHE.get(skinKey).isDone()) {
                return GLOW_CACHE.get(skinKey).join();
            }
        } else {
            GLOW_CACHE.put(skinKey, CompletableFuture.supplyAsync(() -> fetchSkinFile(playerName, dragonStage, "glow")));
        }

        return null;
    }


    public static @Nullable ResourceLocation getPlayerSkin(Player player, ResourceKey<DragonStage> dragonStage) {
        String playerKey = player.getGameProfile().getName() + "_" + dragonStage.location().getPath();
        boolean renderCustomSkin = DragonStateProvider.getData(player).getSkinData().renderCustomSkin;

        if ((ClientDragonRenderer.renderOtherPlayerSkins || player == DragonSurvival.PROXY.getLocalPlayer()) && renderCustomSkin) {
            if (SKIN_CACHE.containsKey(playerKey) && SKIN_CACHE.get(playerKey) != null) {
                if (SKIN_CACHE.get(playerKey).isDone()) {
                    return SKIN_CACHE.get(playerKey).join();
                }
            }

            if (!hasFailedFetch.contains(playerKey) && !SKIN_CACHE.containsKey(playerKey)) {
                SKIN_CACHE.put(playerKey, CompletableFuture.supplyAsync(() -> fetchSkinFile(player, dragonStage)));
            }
        }

        return null;
    }

    public static @Nullable ResourceLocation fetchSkinFile(final String playerName, final ResourceKey<DragonStage> stage, final String... extra) {
        String playerKey = playerName + "_" + stage.location().getPath();
        String[] text = ArrayUtils.addAll(new String[]{playerKey}, extra);

        String resourceName = StringUtils.join(text, "_");
        ResourceLocation resource;

        try {
            resource = ResourceLocation.fromNamespaceAndPath(MODID, resourceName.toLowerCase(Locale.ENGLISH));
        } catch (ResourceLocationException exception) {
            DragonSurvival.LOGGER.error(exception);
            return null;
        }

        try (SimpleTexture simpleTexture = new SimpleTexture(resource)) {
            if (Minecraft.getInstance().getTextureManager().getTexture(resource, simpleTexture) != simpleTexture) {
                return resource;
            }
        }

        HashMap<String, SkinObject> playerSkinMap = USER_SKINS.getOrDefault(stage, null);

        // Wait an increasing amount of time depending on the number of failed attempts
        while (numSkinFetchAttempts < 10 && playerSkinMap == null) {
            if (lastSkinFetchAttemptTime + numSkinFetchAttempts < Blaze3D.getTime()) {
                DragonSurvival.LOGGER.warn("Customs skins are not yet fetched, re-fetching...");
                init();

                numSkinFetchAttempts++;
                lastSkinFetchAttemptTime = Blaze3D.getTime();

                playerSkinMap = USER_SKINS.getOrDefault(stage, null);

                if (playerSkinMap == null) {
                    DragonSurvival.LOGGER.error("Custom skins could not be fetched");
                }
            }
        }

        String skinName = StringUtils.join(ArrayUtils.addAll(new String[]{playerName}, extra), "_");
        SkinObject skin = null;

        if (playerSkinMap != null) {
            skin = playerSkinMap.getOrDefault(skinName, null);
        }

//        // Only use the API to get the names (for the random button)
//        if (skinLoader instanceof GithubSkinLoader gitHubOld) {
//            try (InputStream imageStream = gitHubOld.querySkinImage(skinName, stage)) {
//                return readSkin(imageStream, resource);
//            } catch (IOException exception) {
//                return fetchSkinResource(playerName, stage, extra, exception, playerKey);
//            }
//        }

        if (skin == null && stage == AncientDatapack.ancient) {
            DragonSurvival.LOGGER.warn("Failed to get skin information for ancient stage for {}. Falling back to using adult stage.", playerName);
            return fetchSkinFile(playerName, DragonStages.adult, extra);
        }

        try (InputStream imageStream = skinLoader.querySkinImage(skin)) {
            return readSkin(imageStream, resource);
        } catch (Exception exception) {
            return fetchSkinResource(extra, playerKey);
        }
    }

    private static @Nullable ResourceLocation fetchSkinResource(final String[] extra, final String playerKey) {
        boolean isNormalSkin = extra == null || extra.length == 0;
        handleSkinFetchError(playerKey, isNormalSkin);
        return null;
    }

    private static ResourceLocation readSkin(final InputStream imageStream, final ResourceLocation location) throws IOException {
        if (imageStream == null) {
            throw new IOException("Skin was not successfully fetched for [" + location + "]");
        }

        NativeImage customTexture = NativeImage.read(imageStream);
        // Avoid overwriting and closing the texture (closing the image as well, leading to a crash)
        // (Since this method is handled off-thread the image doesn't get immediately uploaded)
        RenderSystem.recordRenderCall(() -> Minecraft.getInstance().getTextureManager().register(location, new DynamicTexture(customTexture)));

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

        if ((ClientDragonRenderer.renderOtherPlayerSkins || player == DragonSurvival.PROXY.getLocalPlayer()) && SKIN_CACHE.containsKey(playerKey) && renderCustomSkin) {
            if (GLOW_CACHE.containsKey(playerKey)) {
                if (GLOW_CACHE.get(playerKey).isDone()) {
                    return GLOW_CACHE.get(playerKey).join();
                }
            } else {
                GLOW_CACHE.put(playerKey, CompletableFuture.supplyAsync(() -> fetchSkinFile(player, dragonStage, "glow")));
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
        for (Supplier<NetSkinLoader> loader : SKIN_LOADERS) {
            NetSkinLoader testLoader = loader.get();
            if (testLoader.ping()) {
                skinLoader = testLoader;
                break;
            }
        }
        if (skinLoader == null) {
            skinLoader = new GithubSkinLoader();
            DragonSurvival.LOGGER.warn("Unable to connect to skin database.");
            return;
        }
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
            if (dragonStage == null) {
                return;
            }

            if (!USER_SKINS.containsKey(dragonStage)) {
                USER_SKINS.put(dragonStage, new HashMap<>());
            }

            skin.short_name = name;
            skin.glow = isGlow;
            USER_SKINS.get(dragonStage).putIfAbsent(name, skin);
        }
    }

    public static ResourceKey<DragonStage> parseResourceKeyFromName(String name) {
        // FIXME :: has problems with '_Katya_Ket__newborn'
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
        USER_SKINS.clear();
        SKIN_CACHE.clear();
        GLOW_CACHE.clear();
        hasFailedFetch.clear();
    }

    public static boolean renderCustomSkin(final LocalPlayer player) {
        return DragonStateProvider.getData(player).getSkinData().renderCustomSkin;
    }
}