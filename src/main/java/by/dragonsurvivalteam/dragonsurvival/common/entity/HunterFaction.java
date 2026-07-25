package by.dragonsurvivalteam.dragonsurvival.common.entity;

import net.minecraft.world.entity.MobCategory;

public class HunterFaction {
    public static final MobCategory DRAGONSURVIVAL_HUNTER_FACTION = MobCategory.create(
            "DRAGONSURVIVAL_HUNTER_FACTION", "dragonsurvival:hunter_faction", 15, false, true, 128
    );

    public static void init() { /* Loads the extensible enum value. */ }
}
