package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;

import java.util.List;

public class ItemBasedUpgrade extends UpgradeType<Item> {
    public static final Codec<ItemBasedUpgrade> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).listOf().fieldOf("items_per_level").forGetter(ItemBasedUpgrade::itemsPerLevel),
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("downgrade_items").forGetter(ItemBasedUpgrade::downgradeItems)
    ).apply(instance, ItemBasedUpgrade::new));

    private final List<HolderSet<Item>> itemsPerLevel;
    private final HolderSet<Item> downgradeItems;

    public ItemBasedUpgrade(final List<HolderSet<Item>> itemsPerLevel, final HolderSet<Item> downgradeItems) {
        this.itemsPerLevel = itemsPerLevel;
        this.downgradeItems = downgradeItems;
    }

    public MutableComponent getDescription(int abilityLevel) {
        // TODO :: implement
        return Component.empty();
    }

    @Override
    @SuppressWarnings("deprecation") // ignore
    public boolean upgrade(final DragonAbilityInstance ability, final Item input) {
        if (ability.level() > DragonAbilityInstance.MIN_LEVEL && downgradeItems.contains(input.builtInRegistryHolder())) {
            ability.setLevel(ability.level() - 1);
            return true;
        }

        if (ability.level() >= itemsPerLevel.size()) {
            return false;
        }

        if (itemsPerLevel.get(ability.level()).contains(input.builtInRegistryHolder())) {
            ability.setLevel(ability.level() + 1);
            return true;
        }

        return false;
    }

    public List<HolderSet<Item>> itemsPerLevel() {
        return itemsPerLevel;
    }

    public HolderSet<Item> downgradeItems() {
        return downgradeItems;
    }
}
