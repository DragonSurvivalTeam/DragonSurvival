package by.dragonsurvivalteam.dragonsurvival.compat;

import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;

import java.util.HashMap;
import java.util.Map;

public class Compat {
    public static final String JEI = "jei";

    private static final Map<String, Boolean> MODS = new HashMap<>();

    public static boolean isModLoaded(final String mod) {
        return MODS.computeIfAbsent(mod, key -> {
            ModList modList = ModList.get();

            if (modList != null) {
                return modList.isLoaded(key);
            }

            return LoadingModList.get().getModFileById(key) != null;
        });
    }
}
