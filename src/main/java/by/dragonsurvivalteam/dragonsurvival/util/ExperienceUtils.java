package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.world.entity.player.Player;

public class ExperienceUtils {
    /**
     * <a href="https://github.com/Shadows-of-Fire/Placebo/blob/1.21/src/main/java/dev/shadowsoffire/placebo/util/EnchantmentUtils.java#L60">Taken from here</a>
     * <br> <br>
     * Calculates the amount of experience the passed level is worth - <a href="https://minecraft.wiki/w/Experience#Leveling_up">Reference</a> <br>
     * It is intentionally different compared to {@link Player#getXpNeededForNextLevel()} <br>
     * (Since it calculates the experience required to reach the passed level from the previous level)
     *
     * @param level The target level
     * @return The amount of experience required to reach the given level when starting from the previous level
     */
    public static int getExperienceForLevel(int level) {
        if (level == 0) return 0;
        if (level > 30) return 112 + (level - 31) * 9;
        if (level > 15) return 37 + (level - 16) * 5;
        return 7 + (level - 1) * 2;
    }

    /** Calculates the experience level the experience is worth */
    public static int getLevel(int experience) {
        int level = 0;
        int currentExperience = 0;

        while (true) {
            currentExperience += getExperienceForLevel(level);

            if (currentExperience > experience) {
                break;
            }

            level++;
        }

        return level;
    }

    /** Calculate the total experience a level is worth given experience levels */
    public static int getTotalExperience(int targetLevel) {
        int experience = 0;

        for (int level = 1; level <= targetLevel; level++) {
            experience += getExperienceForLevel(level);
        }

        return experience;
    }

    /**
     * Calculate the total experience the player has <br>
     * {@link Player#totalExperience} is not used since it does not update when using commands to change the experience level
     */
    public static int getTotalExperience(final Player player) {
        int currentExperience = getTotalExperience(player.experienceLevel);
        return (int) (currentExperience + player.experienceProgress * getExperienceForLevel(player.experienceLevel + 1));
    }
}
