package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.world.entity.player.Player;

public class ExperienceUtils {

    /** See {@link Player#getXpNeededForNextLevel()} */
    public static int getExperienceForLevelAfter(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else {
            return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
        }
    }

    /** Calculates the experience level the experience is worth */
    // Integral math from https://minecraft.wiki/w/Experience
    public static int getLevel(int experience) {
        int totalExperienceFor16 = 352;
        int totalExperienceFor31 = 1507;
        if (experience <= totalExperienceFor16) {
            return (int) (Math.sqrt(experience + 9) - 3);
        } else if (experience <= totalExperienceFor31) {
            return (int) (Math.sqrt((2.f / 5.f) * (experience - 7839.f / 40.f)) + 81.f / 10.f);
        } else {
            return (int) (Math.sqrt((2.f / 9.f) * (experience - 54215.f / 72.f)) + 325.f / 18.f);
        }
    }

    /** See {@link ExperienceUtils#getLevel(int)} + also adds the progress to the next level (0..1) */
    public static double getLevelAndProgress(int experience) {
        int wholeLevel = getLevel(experience);

        int requiredForNext = getLevel(wholeLevel + 1);
        int requiredExperience = requiredForNext - experience;
        double progress = (double) (experience - (requiredExperience - requiredForNext)) / requiredForNext;
        return wholeLevel + progress;
    }

    /** Calculate the total experience a level is worth given experience levels */
    // Integral math from https://minecraft.wiki/w/Experience
    public static int getTotalExperience(int targetLevel) {
        if (targetLevel <= 16) {
            return (targetLevel * targetLevel + (6 * targetLevel));
        } else if (targetLevel <= 31) {
            return (int) (2.5 * targetLevel * targetLevel - (40.5 * targetLevel) + 360);
        } else {
            return (int) (4.5 * targetLevel * targetLevel - (162.5 * targetLevel) + 2220);
        }
    }

    /**
     * Calculate the total experience the player has <br>
     * {@link Player#totalExperience} is not used since it does not update when using commands to change the experience level
     */
    public static int getTotalExperience(final Player player) {
        int currentExperience = getTotalExperience(player.experienceLevel);
        return (int) (currentExperience + player.experienceProgress * getExperienceForLevelAfter(player.experienceLevel));
    }
}
