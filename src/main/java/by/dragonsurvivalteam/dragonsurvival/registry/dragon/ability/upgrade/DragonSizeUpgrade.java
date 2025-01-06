package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.LevelBasedValue;

public record DragonSizeUpgrade(int maxLevel, LevelBasedValue sizeRequirement) implements UpgradeType<InputData> {
    @Translation(comments = "Next level will be unlocked at size %s")
    private static final String DRAGON_SIZE_UPGRADE = Translation.Type.GUI.wrap("ability_upgrade.dragon_size_upgrade");

    public static final MapCodec<DragonSizeUpgrade> CODEC = RecordCodecBuilder.mapCodec(instance -> UpgradeType.codecStart(instance)
            .and(LevelBasedValue.CODEC.fieldOf("size_requirement").forGetter(DragonSizeUpgrade::sizeRequirement)).apply(instance, DragonSizeUpgrade::new)
    );

    @Override
    public boolean apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final InputData data) {
        if (data.type() != InputData.Type.SIZE) {
            return false;
        }

        int newLevel = 0;

        for (int level = DragonAbilityInstance.MIN_LEVEL_FOR_CALCULATIONS; level <= maxLevel(); level++) {
            if (data.input() < sizeRequirement.calculate(level)) {
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
        return Component.translatable(DRAGON_SIZE_UPGRADE, (int) sizeRequirement.calculate(abilityLevel));
    }

    @Override
    public MapCodec<? extends UpgradeType<?>> codec() {
        return CODEC;
    }
}
