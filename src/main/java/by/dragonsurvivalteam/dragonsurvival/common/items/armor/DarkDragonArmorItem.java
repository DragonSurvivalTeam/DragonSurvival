package by.dragonsurvivalteam.dragonsurvival.common.items.armor;

import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class DarkDragonArmorItem extends Item implements PermanentEnchantmentItem {
    public ItemEnchantments getDefaultEnchantments() {
        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        Equippable equippable = this.components().get(DataComponents.EQUIPPABLE);
        if (equippable == null) return enchantments.toImmutable();

        switch (equippable.slot()) {
            case EquipmentSlot.HEAD -> EnchantmentUtils.set(enchantments, DSEnchantments.BLOOD_SIPHON, 1);
            case EquipmentSlot.CHEST -> EnchantmentUtils.set(enchantments, DSEnchantments.MURDERERS_CUNNING, 1);
            case EquipmentSlot.LEGS -> EnchantmentUtils.set(enchantments, DSEnchantments.OVERWHELMING_MIGHT, 1);
            case EquipmentSlot.FEET -> EnchantmentUtils.set(enchantments, DSEnchantments.DRACONIC_SUPERIORITY, 1);
        }

        EnchantmentUtils.set(enchantments, DSEnchantments.CURSE_OF_OUTLAW, 1);
        return enchantments.toImmutable();
    }

    public DarkDragonArmorItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquip(@NotNull final ItemStack stack, @NotNull final EquipmentSlot armorType, @NotNull final LivingEntity entity) {
        if (!super.canEquip(stack, armorType, entity)) {
            return false;
        }

        if (entity.hasEffect(DSEffects.ANIMAL_PEACE)) {
            return false;
        }

        List<ItemStack> armorItems = new ArrayList<>();
        armorItems.add(entity.getItemBySlot(EquipmentSlot.HEAD));
        armorItems.add(entity.getItemBySlot(EquipmentSlot.CHEST));
        armorItems.add(entity.getItemBySlot(EquipmentSlot.LEGS));
        armorItems.add(entity.getItemBySlot(EquipmentSlot.FEET));

        for (ItemStack armor : armorItems) {
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
