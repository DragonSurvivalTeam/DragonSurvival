package by.dragonsurvivalteam.dragonsurvival.common.items.armor;

import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import net.minecraft.core.Holder;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class DragonHunterWeapon extends SwordItem implements PermanentEnchantmentItem {
    public ItemEnchantments getDefaultEnchantments() {
        ItemEnchantments.Mutable e = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        Holder<Enchantment> dragonsbane = EnchantmentUtils.getHolder(DSEnchantments.DRAGONSBANE);
        if (dragonsbane != null) e.set(dragonsbane, 3);
        return e.toImmutable();
    }

    public DragonHunterWeapon(Tier pTier, Properties pProperties) {
        super(pTier, pProperties);
    }
}
