package by.dragonsurvivalteam.dragonsurvival.common.entity;

import net.minecraft.world.entity.MobCategory;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

/** Referenced by 'resources/META_INF/enumextensions.json' */
public class HunterFaction {
    public static final EnumProxy<MobCategory> DRAGONSURVIVAL_HUNTER_FACTION = new EnumProxy<>(
            MobCategory.class, "dragonsurvival:hunter_faction", 15, false, true, 128
    );
}
