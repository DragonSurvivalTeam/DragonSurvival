package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.ExperienceUtils;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record ExperienceUpgrade(int maxLevel, LevelBasedValue experienceCost) implements UpgradeType<ExperienceUpgrade.Type> {
    public static final MapCodec<ExperienceUpgrade> CODEC = RecordCodecBuilder.mapCodec(instance -> UpgradeType.codecStart(instance)
            .and(LevelBasedValue.CODEC.fieldOf("experience_cost").forGetter(ExperienceUpgrade::experienceCost)).apply(instance, ExperienceUpgrade::new)
    );

    @Override
    public boolean apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final ExperienceUpgrade.Type type) {
        if (type == Type.UPGRADE && !canUpgrade(dragon, ability)) {
            return false;
        } else if (type == Type.DOWNGRADE && !canDowngrade(dragon, ability)) {
            return false;
        }

        dragon.giveExperiencePoints(getExperience(dragon, ability, type));
        ability.setLevel(ability.level() + type.step());
        return true;
    }

    @Override
    public MutableComponent getDescription(final int abilityLevel) {
        int experiencePoints = (int) experienceCost.calculate(abilityLevel);
        return Component.translatable(LangKey.ABILITY_LEVEL_MANUAL_UPGRADE, experiencePoints, ExperienceUtils.getLevel(experiencePoints));
    }

    @Override
    public boolean canUpgrade(final ServerPlayer dragon, final DragonAbilityInstance ability) {
        if (!UpgradeType.super.canUpgrade(dragon, ability)) {
            return false;
        }

        return ExperienceUtils.getTotalExperience(dragon) >= Math.abs(getExperience(dragon, ability, Type.UPGRADE));
    }

    /** Returns the experience that will be either taken ({@link Type#UPGRADE}) (i.e. negative) or granted ({@link Type#DOWNGRADE}) */
    public int getExperience(final Player dragon, final DragonAbilityInstance ability, final Type type) {
        if (dragon.hasInfiniteMaterials()) {
            return 0;
        }

        int newLevel = ability.level() + type.step();
        // Going from 1 to 0 means we need to refund 0 to 1
        int experience = (int) experienceCost.calculate(type == Type.UPGRADE ? newLevel : ability.level());
        return type == Type.UPGRADE ? -experience : experience;
    }

    @Override
    public MapCodec<? extends UpgradeType<?>> codec() {
        return CODEC;
    }

    public enum Type {
        UPGRADE(1), DOWNGRADE(-1);

        private final int step;

        Type(int step) {
            this.step = step;
        }

        public int step() {
            return step;
        }
    }
}
