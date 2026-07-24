package by.dragonsurvivalteam.dragonsurvival.common.items.armor;

import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
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

public class LightDragonArmorItem extends ArmorItem implements PermanentEnchantmentItem {
    public ItemEnchantments getDefaultEnchantments() {
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        switch (this.getType()) {
            case HELMET -> EnchantmentUtils.set(enchantments, DSEnchantments.COMBAT_RECOVERY, 1);
            case CHESTPLATE -> EnchantmentUtils.set(enchantments, DSEnchantments.AERODYNAMIC_MASTERY, 1);
            case LEGGINGS -> EnchantmentUtils.set(enchantments, DSEnchantments.UNBREAKABLE_SPIRIT, 1);
            case BOOTS -> EnchantmentUtils.set(enchantments, DSEnchantments.SACRED_SCALES, 1);
        }

        EnchantmentUtils.set(enchantments, DSEnchantments.CURSE_OF_KINDNESS, 1);
        return enchantments.toImmutable();
    }

    public LightDragonArmorItem(Type pType, Properties pProperties) {
        super(DSEquipment.LIGHT_DRAGON_ARMOR_MATERIAL, pType, pProperties);
    }

    @Override
    public boolean canEquip(@NotNull final ItemStack stack, @NotNull final EquipmentSlot armorType, @NotNull final LivingEntity entity) {
        if (!super.canEquip(stack, armorType, entity)) {
            return false;
        }

        if (entity.hasEffect(DSEffects.HUNTER_OMEN)) {
            return false;
        }

        for (ItemStack armor : entity.getArmorSlots()) {
            if (armor.isEmpty() || /* Allow swapping items */ entity.getEquipmentSlotForItem(armor) == armorType) {
                continue;
            }

            if (armor.is(DSItemTags.DARK_ARMOR)) {
                return false;
            }
        }

        return true;
    }
}
