package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record ExperienceLevelUpgrade(int maxLevel, LevelBasedValue levelRequirement) implements UpgradeType<InputData> {
    @Translation(comments = "§6■ Next level will be unlocked at experience level %s")
    private static final String EXPERIENCE_LEVEL_UPGRADE = Translation.Type.GUI.wrap("ability_upgrade.experience_level_upgrade");

    public static final MapCodec<ExperienceLevelUpgrade> CODEC = RecordCodecBuilder.mapCodec(instance -> UpgradeType.codecStart(instance)
            .and(LevelBasedValue.CODEC.fieldOf("level_requirement").forGetter(ExperienceLevelUpgrade::levelRequirement)).apply(instance, ExperienceLevelUpgrade::new)
    );

    @Override
    public boolean apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final InputData data) {
        if (data.type() != InputData.Type.EXPERIENCE_LEVELS) {
            return false;
        }

        int newLevel = 0;

        for (int level = DragonAbilityInstance.MIN_LEVEL_FOR_CALCULATIONS; level <= maxLevel(); level++) {
            if (data.input() < levelRequirement.calculate(level)) {
                break;
            }

            newLevel++;
        }

        if (newLevel != ability.level()) {
            ability.setLevel(newLevel);
            return true;
        }

        return false;
    }

    @Override
    public MutableComponent getDescription(final int abilityLevel) {
        return Component.translatable(EXPERIENCE_LEVEL_UPGRADE, (int) levelRequirement.calculate(abilityLevel));
    }

    @Override
    public MapCodec<? extends UpgradeType<?>> codec() {
        return CODEC;
    }
}
