package by.dragonsurvivalteam.dragonsurvival.common.items.armor;

import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEquipment;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.NotNull;

public class DarkDragonArmorItem extends ArmorItem implements PermanentEnchantmentItem {
    public ItemEnchantments getDefaultEnchantments() {
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        switch (this.getType()) {
            case HELMET -> EnchantmentUtils.set(enchantments, DSEnchantments.BLOOD_SIPHON, 1);
            case CHESTPLATE -> EnchantmentUtils.set(enchantments, DSEnchantments.MURDERERS_CUNNING, 1);
            case LEGGINGS -> EnchantmentUtils.set(enchantments, DSEnchantments.OVERWHELMING_MIGHT, 1);
            case BOOTS -> EnchantmentUtils.set(enchantments, DSEnchantments.DRACONIC_SUPERIORITY, 1);
        }

        EnchantmentUtils.set(enchantments, DSEnchantments.CURSE_OF_OUTLAW, 1);
        return enchantments.toImmutable();
    }

    public DarkDragonArmorItem(Type pType, Properties pProperties) {
        super(DSEquipment.DARK_DRAGON_ARMOR_MATERIAL, pType, pProperties);
    }

    @Override
    public boolean canEquip(@NotNull final ItemStack stack, @NotNull final EquipmentSlot armorType, @NotNull final LivingEntity entity) {
        if (!super.canEquip(stack, armorType, entity)) {
            return false;
        }

        for (ItemStack armor : entity.getArmorSlots()) {
            if (armor.isEmpty() || /* Allow swapping items */ entity.getEquipmentSlotForItem(armor) == armorType) {
                continue;
            }

            if (armor.is(DSItemTags.LIGHT_ARMOR)) {
                return false;
            }
        }

        return true;
    }
}
