package by.dragonsurvivalteam.dragonsurvival.compat;

import by.dragonsurvivalteam.dragonsurvival.compat.create.CardboardBoxHelper;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.LoadingModList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Compat {
    public static final String JEI = "jei";
    public static final String IRIS = "iris";
    public static final String COSMETIC_ARMOR_REWORKED = "cosmeticarmorreworked";
    public static final String SOPHISTICATED_BACKPACKS = "sophisticatedbackpacks";
    public static final String CURIOS = "curios";
    // TODO :: Custom "chain-riding" animation state?
    public static final String CREATE = "create";

    private static final Map<String, List<String>> ALIAS = Map.of(
            IRIS, List.of("oculus"),
            "sodium", List.of("embeddium")
    );

    private static final Map<String, Boolean> MODS = new HashMap<>();

    public static boolean isModLoaded(final String mod) {
        return MODS.computeIfAbsent(mod, key -> {
            if (check(key)) {
                return true;
            }

            for (String alias : ALIAS.getOrDefault(key, List.of())) {
                if (check(alias)) {
                    return true;
                }
            }

            return false;
        });
    }

    private static boolean check(final String modid) {
        ModList modList = ModList.get();

        if (modList != null && modList.isLoaded(modid)) {
            return true;
        }

        return LoadingModList.get().getModFileById(modid) != null;
    }

    public static boolean entityHasBeenModelSwapped(final Entity entity) {
        return CardboardBoxHelper.testForStealth(entity);
    }
}
