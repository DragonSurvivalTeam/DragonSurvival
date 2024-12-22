package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.Function;

public record Upgrade(Either<ValueBasedUpgrade, ItemBasedUpgrade> upgrade) {
    public static final Codec<Upgrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(ValueBasedUpgrade.CODEC, ItemBasedUpgrade.CODEC).fieldOf("upgrade_data").forGetter(Upgrade::upgrade)
    ).apply(instance, Upgrade::new));

    public MutableComponent getDescription(int level) {
        return upgrade.map(upgrade -> upgrade.getDescription(level), upgrade -> upgrade.getDescription(level));
    }

    public boolean attemptUpgrade(final DragonAbilityInstance ability, final Object input) {
        UpgradeType<?> type = upgrade.map(Function.identity(), Function.identity());
        return type.attemptUpgrade(ability, input);
    }

    public float getExperienceCost(int abilityLevel) {
        if (abilityLevel < DragonAbilityInstance.MIN_LEVEL_FOR_CALCULATIONS) {
            return 0;
        }

        return upgrade.map(type -> type.getExperienceCost(abilityLevel), type -> type.getExperienceCost(abilityLevel));
    }

    public ValueBasedUpgrade.Type type() {
        return upgrade.left().map(ValueBasedUpgrade::type).orElse(null);
    }

    public int maximumLevel() {
        return upgrade.map(ValueBasedUpgrade::maximumLevel, upgrade -> upgrade.itemsPerLevel().size());
    }
}