package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.emotes.Emote;
import by.dragonsurvivalteam.dragonsurvival.client.emotes.EmoteKeybinds;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.util.json.GsonFactory;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import java.io.*;
import java.util.*;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class DSEmoteKeybindings {
    public static EmoteKeybinds EMOTE_KEYBINDS = new EmoteKeybinds();
    private static final String EMOTE_BINDINGS = "emote_bindings.json";
    private static boolean hasInitialized;

    @SubscribeEvent
    public static void initializeEmoteKeybinds(final EntityJoinLevelEvent event) {
        load();
    }

    public static void load() {
        if (hasInitialized) {
            return;
        }

        File directory = new File(FMLPaths.GAMEDIR.get().toFile(), "dragon-survival");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File savedFile = new File(directory, EMOTE_BINDINGS + ".json");
        if (savedFile.exists()) {
            try {
                Gson gson = GsonFactory.getDefault();
                InputStream input = new FileInputStream(savedFile);

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                    EMOTE_KEYBINDS = gson.fromJson(reader, EmoteKeybinds.class);
                    if (EMOTE_KEYBINDS == null) {
                        throw new IOException("Emote keybinds file could not be read");
                    }
                } catch (IOException | JsonSyntaxException exception) {
                    DragonSurvival.LOGGER.warn("An error occurred while processing the [" + EMOTE_BINDINGS + "] file", exception);
                }
            } catch (FileNotFoundException exception) {
                DragonSurvival.LOGGER.error("Emote keybinds file could not be found", exception);
            }
        }

        hasInitialized = true;
    }

    public static void save() {
        if (!hasInitialized) {
            load();
        }

        try {
            File directory = new File(FMLPaths.GAMEDIR.get().toFile(), "dragon-survival");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            File savedFile = new File(directory, EMOTE_BINDINGS + ".json");
            Gson gson = GsonFactory.newBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(savedFile);
            gson.toJson(savedFile, writer);
            writer.close();
        } catch (IOException exception) {
            DragonSurvival.LOGGER.error("An error occurred while trying to save emote keybinds", exception);
        }
    }
}