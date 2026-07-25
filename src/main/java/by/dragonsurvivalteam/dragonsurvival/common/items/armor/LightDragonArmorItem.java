package by.dragonsurvivalteam.dragonsurvival.common.items.armor;

import by.dragonsurvivalteam.dragonsurvival.client.DragonSurvivalClient;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEquipment;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LightDragonArmorItem extends ArmorItem implements PermanentEnchantmentItem {
    public Map<Enchantment, Integer> getDefaultEnchantments() {
        Map<Enchantment, Integer> enchantments = new LinkedHashMap<>();

        switch (this.getType()) {
            case HELMET -> EnchantmentUtils.set(enchantments, DSEnchantments.COMBAT_RECOVERY, 1);
            case CHESTPLATE -> EnchantmentUtils.set(enchantments, DSEnchantments.AERODYNAMIC_MASTERY, 1);
            case LEGGINGS -> EnchantmentUtils.set(enchantments, DSEnchantments.UNBREAKABLE_SPIRIT, 1);
            case BOOTS -> EnchantmentUtils.set(enchantments, DSEnchantments.SACRED_SCALES, 1);
        }

        EnchantmentUtils.set(enchantments, DSEnchantments.CURSE_OF_KINDNESS, 1);
        return enchantments;
    }

    public LightDragonArmorItem(Type pType, Properties pProperties) {
        super(DSEquipment.LIGHT_DRAGON_ARMOR_MATERIAL, pType, pProperties);
    }

    @Override
    public void initializeClient(@NotNull final Consumer<IClientItemExtensions> consumer) {
        consumer.accept(DragonSurvivalClient.createArmorExtension(getType()));
    }

    @Override
    public boolean canEquip(@NotNull final ItemStack stack, @NotNull final EquipmentSlot armorType, @NotNull final Entity entity) {
        if (!super.canEquip(stack, armorType, entity)) {
            return false;
        }

        if (!(entity instanceof LivingEntity livingEntity)) {
            return true;
        }

        if (livingEntity.hasEffect(DSEffects.HUNTER_OMEN.get())) {
            return false;
        }

        for (ItemStack armor : livingEntity.getArmorSlots()) {
            if (armor.isEmpty() || /* Allow swapping items */ livingEntity.getEquipmentSlotForItem(armor) == armorType) {
                continue;
            }

            if (armor.is(DSItemTags.DARK_ARMOR)) {
                return false;
            }
        }

        return true;
    }
}
