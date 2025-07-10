package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Technically only needed for cases where we need to know some context between two entities <br>
 * But checking this for the initial targeting can skip some unneeded processing <br>
 * It is also more clear in the ability description since this target mode is shown there
 */
public enum TargetingMode implements StringRepresentable {
    @Translation(comments = "all entities")
    ALL("all"),
    @Translation(comments = "allies")
    ALLIES("allies"),
    @Translation(comments = "allies and yourself")
    ALLIES_AND_SELF("allies_and_self"),
    @Translation(comments = "non allies")
    NON_ALLIES("non_allies"),
    @Translation(comments = "non enemies")
    NON_ENEMIES("non_enemies"),
    @Translation(comments = "neutral entities")
    NEUTRAL("neutral"),
    @Translation(comments = "enemies")
    ENEMIES("enemies"),
    @Translation(comments = "items")
    ITEMS("items"),
    @Translation(comments = "everyone except yourself")
    ALL_EXCEPT_SELF("all_except_self");

    @Translation(key = "player_targeting_handling", type = Translation.Type.CONFIGURATION, comments = {
            "Determines how players are handled for the initial targeting of abilities",
            "The flags can be combined, e.g. '3' combines the flags '1' and '2'",
            "0: No special handling (players are allies on the same team, otherwise they count as 'neutral')",
            "1: They are always considered as 'ally'",
            "2: They are always considered as 'enemy' (unless they're on the same team without friendly fire enabled)",
            "4: Enabled Friendly fire on a team no longer flags players as 'enemy'"
    })
    @ConfigOption(side = ConfigSide.SERVER, category = "abilities", key = "player_targeting_handling")
    public static int PLAYER_FLAG = 0;

    public static final Codec<TargetingMode> CODEC = StringRepresentable.fromEnum(TargetingMode::values);
    private final String name;

    TargetingMode(final String name) {
        this.name = name;
    }

    public boolean isEntityRelevant(final Player player, final Entity target) {
        if (this == TargetingMode.ALL) {
            return true;
        }

        if (player == target) {
            return this == TargetingMode.ALLIES_AND_SELF || this == TargetingMode.NON_ENEMIES;
        }

        if (target instanceof ItemEntity) {
            return this == TargetingMode.ITEMS;
        }

        if (isFriendly(player, target)) {
            return this == TargetingMode.ALLIES_AND_SELF || this == TargetingMode.ALLIES || this == TargetingMode.NON_ENEMIES || this == TargetingMode.ALL_EXCEPT_SELF;
        }

        if (isEnemy(player, target)) {
            return this == TargetingMode.ENEMIES || this == TargetingMode.NON_ALLIES || this == TargetingMode.ALL_EXCEPT_SELF;
        }

        return this == TargetingMode.NEUTRAL || this == TargetingMode.NON_ALLIES || this == TargetingMode.NON_ENEMIES || this == TargetingMode.ALL_EXCEPT_SELF;
    }

    private boolean isFriendly(final Player player, final Entity target) {
        if (target instanceof Player && (PLAYER_FLAG & ALWAYS_ALLY) != 0) {
            return true;
        }

        // The order of the check is important
        // since certain entities may have a more complex logic to check for allies
        return target.isAlliedTo(player);
    }

    private boolean isEnemy(final Player player, final Entity target) {
        if (target instanceof Enemy || target.getType().getCategory() == MobCategory.MONSTER) {
            return true;
        }

        if (target instanceof Mob mob && mob.getTarget() == player) {
            return true;
        }

        if (target instanceof Player otherPlayer && (PLAYER_FLAG & ALWAYS_ENEMY) != 0) {
            // Returns true if friendly fire is enabled for the team
            boolean canHarmPlayer = player.canHarmPlayer(otherPlayer);

            if (!canHarmPlayer) {
                return false;
            }

            // They're not allied or the flag for team safety is not present
            return !player.isAlliedTo(otherPlayer) || (PLAYER_FLAG & SAFE_IN_TEAM) == 0;
        }

        return false;
    }

    public Component translation() {
        return DSLanguageProvider.enumValue(this);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    private static final int ALWAYS_ALLY = 1;
    private static final int ALWAYS_ENEMY = 2;
    private static final int SAFE_IN_TEAM = 4;
}
