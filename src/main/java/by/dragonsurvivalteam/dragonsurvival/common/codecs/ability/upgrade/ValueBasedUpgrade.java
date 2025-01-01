package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.ExperienceUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

public class ValueBasedUpgrade extends UpgradeType<ValueBasedUpgrade.InputData> {
    public static final Codec<ValueBasedUpgrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Type.CODEC.fieldOf("type").forGetter(ValueBasedUpgrade::type),
            ExtraCodecs.intRange(DragonAbilityInstance.MIN_LEVEL, DragonAbilityInstance.MAX_LEVEL).fieldOf("maximum_level").forGetter(ValueBasedUpgrade::maximumLevel),
            LevelBasedValue.CODEC.fieldOf("requirement_or_cost").forGetter(ValueBasedUpgrade::requirementOrCost)
    ).apply(instance, ValueBasedUpgrade::new));

    private final Type type;
    private final int maximumLevel;
    private final LevelBasedValue requirementOrCost;

    public ValueBasedUpgrade(final Type type, int maximumLevel, final LevelBasedValue requirementOrCost) {
        this.type = type;
        this.maximumLevel = maximumLevel;
        this.requirementOrCost = requirementOrCost;
    }

    public MutableComponent getDescription(int abilityLevel) {
        int requirement = (int) requirementOrCost.calculate(abilityLevel + 1);

        return switch (type) {
            case MANUAL -> Component.translatable(LangKey.ABILITY_LEVEL_MANUAL_UPGRADE, requirement, ExperienceUtils.getLevel(requirement));
            case PASSIVE_LEVEL -> Component.translatable(LangKey.ABILITY_LEVEL_AUTO_UPGRADE, requirement);
            case PASSIVE_GROWTH -> Component.translatable(LangKey.ABILITY_GROWTH_AUTO_UPGRADE, requirement);
        };
    }

    @Override // input is either experience points, levels or size
    public boolean upgrade(final DragonAbilityInstance ability, final InputData inputData) {
        if (inputData.type() != type) {
            return false;
        }

        int currentLevel = ability.level();

        switch (type) {
            case MANUAL -> handleManual(ability, inputData);
            case PASSIVE_LEVEL, PASSIVE_GROWTH -> handlePassive(ability, inputData);
        }

        return currentLevel != ability.level();
    }

    private void handleManual(final DragonAbilityInstance ability, final InputData inputData) {
        // TODO :: implement here? add a UPGRADE_ABILITY advancement trigger here as well
    }

    private void handlePassive(final DragonAbilityInstance ability, final InputData inputData) {
        for (int level = DragonAbilityInstance.MIN_LEVEL_FOR_CALCULATIONS; level <= maximumLevel; level++) {
            if (inputData.input() < requirementOrCost.calculate(level)) {
                ability.setLevel(level - 1);
                break;
            } else if (level > ability.level()) {
                ability.setLevel(level);
            }
        }
    }

    @Override
    public float getExperienceCost(int abilityLevel) {
        if (type == Type.MANUAL) {
            return requirementOrCost.calculate(abilityLevel);
        }

        return 0;
    }

    public Type type() {
        return type;
    }

    public int maximumLevel() {
        return maximumLevel;
    }

    public LevelBasedValue requirementOrCost() {
        return requirementOrCost;
    }

    public enum Type implements StringRepresentable {
        MANUAL("manual"),
        PASSIVE_LEVEL("passive_level"),
        PASSIVE_GROWTH("passive_growth");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);
        private final String name;

        Type(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    public record InputData(Type type, Integer input) {
        public static InputData manual(int experiencePoints) {
            return new InputData(Type.MANUAL, experiencePoints);
        }

        public static InputData passive(int experienceLevels) {
            return new InputData(Type.PASSIVE_LEVEL, experienceLevels);
        }

        public static InputData passiveGrowth(int size) {
            return new InputData(Type.PASSIVE_GROWTH, size);
        }
    }
}
