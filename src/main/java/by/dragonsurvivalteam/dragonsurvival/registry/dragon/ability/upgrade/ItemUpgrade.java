package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public record ItemUpgrade(List<HolderSet<Item>> upgradeItems, HolderSet<Item> downgradeItems) implements UpgradeType<Item> {
    public static final MapCodec<ItemUpgrade> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).listOf().fieldOf("items_per_level").forGetter(ItemUpgrade::upgradeItems),
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("downgrade_items").forGetter(ItemUpgrade::downgradeItems)
    ).apply(instance, ItemUpgrade::new));

    @Override
    @SuppressWarnings("deprecation") // ignore
    public boolean apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Item input) {
        if (handleAutoUpgrade(ability, input)) {
            return true;
        }

        if (ability.level() > DragonAbilityInstance.MIN_LEVEL && downgradeItems.contains(input.builtInRegistryHolder())) {
            ability.setLevel(ability.level() - 1);
            return true;
        }

        if (ability.level() >= upgradeItems.size()) {
            return false;
        }

        if (upgradeItems.get(ability.level()).contains(input.builtInRegistryHolder())) {
            ability.setLevel(ability.level() + 1);
            return true;
        }

        return false;
    }

    private boolean handleAutoUpgrade(final DragonAbilityInstance ability, final Item input) {
        if (input != Items.AIR) {
            return false;
        }

        if (ability.level() >= upgradeItems.size()) {
            return false;
        }

        if (upgradeItems.get(ability.level()).size() == 0) {
            ability.setLevel(ability.level() + 1);
            return true;
        }

        return false;
    }

    @Override
    public MutableComponent getDescription(final int abilityLevel) {
        return Component.empty(); // TODO
    }

    @Override
    public int maxLevel() {
        return upgradeItems.size();
    }

    @Override
    public MapCodec<? extends UpgradeType<?>> codec() {
        return CODEC;
    }
}
