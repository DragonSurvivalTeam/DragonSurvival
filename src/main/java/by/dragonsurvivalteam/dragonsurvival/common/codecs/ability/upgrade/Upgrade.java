package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.List;
import java.util.Optional;
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

    public static Optional<Upgrade> value(final ValueBasedUpgrade.Type type, int maximumLevel, final LevelBasedValue requirementOrCost) {
        return Optional.of(new Upgrade(Either.left(new ValueBasedUpgrade(type, maximumLevel, requirementOrCost))));
    }

    public static Optional<Upgrade> item(final List<HolderSet<Item>> itemsPerLevel, final HolderSet<Item> downgradeItems) {
        return Optional.of(new Upgrade(Either.right(new ItemBasedUpgrade(itemsPerLevel, downgradeItems))));
    }
}