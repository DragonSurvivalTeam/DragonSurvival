package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
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
    @Translation(comments = "neutral entities")
    NEUTRAL("neutral"),
    @Translation(comments = "enemies")
    ENEMIES("enemies");

    public static final Codec<TargetingMode> CODEC = StringRepresentable.fromEnum(TargetingMode::values);
    private final String name;

    TargetingMode(final String name) {
        this.name = name;
    }

    public boolean isEntityRelevant(final Player player, final Entity entity) {
        if (this == TargetingMode.ALL) {
            return true;
        }

        if (player == entity) {
            return this == TargetingMode.ALLIES_AND_SELF;
        }

        if (isFriendly(player, entity)) {
            return this == TargetingMode.ALLIES;
        }

        if (entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER) {
            return this == TargetingMode.ENEMIES || this == TargetingMode.NON_ALLIES;
        }

        return this == TargetingMode.NEUTRAL || this == TargetingMode.NON_ALLIES;
    }

    private boolean isFriendly(final Player player, final Entity entity) {
        if (player.isAlliedTo(entity)) {
            return true;
        }

        return entity instanceof Player otherPlayer && !player.canHarmPlayer(otherPlayer);
    }

    public Component translation() {
        return DSLanguageProvider.enumValue(this);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
