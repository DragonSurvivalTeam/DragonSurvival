package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.registries.DeferredRegister;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class DSEnchantments {
    public static final DeferredRegister<Enchantment> REGISTRY = DeferredRegister.create(Registries.ENCHANTMENT, DragonSurvival.MODID);

    private static final EquipmentSlot[] ALL_SLOTS = EquipmentSlot.values();
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    };
    private static final TagKey<Enchantment> ANTI_DRAGON = exclusiveSet("anti_dragon");
    private static final TagKey<Enchantment> DARK_DRAGON = exclusiveSet("dark_dragon");
    private static final TagKey<Enchantment> LIGHT_DRAGON = exclusiveSet("light_dragon");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Bolas Arrows")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Causes crossbows to shoot bolas instead, trapping hit entities.")
    public static final ResourceKey<Enchantment> BOLAS = register("bolas", () -> enchantment(
            Enchantment.Rarity.VERY_RARE, EnchantmentCategory.CROSSBOW, slots(EquipmentSlot.MAINHAND),
            1, 10, 0, 25, 0, null, false, null
    ));

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Dragonsbane")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Inflict increased damage to dragons. If you kill a dragon who has the Hunter's Omen effect, they will lose some growth progress. Damages dragons who hold it.")
    public static final ResourceKey<Enchantment> DRAGONSBANE = register("dragonsbane", () -> enchantment(
            Enchantment.Rarity.COMMON, EnchantmentCategory.WEAPON, slots(EquipmentSlot.MAINHAND),
            5, 1, 11, 21, 11, ANTI_DRAGON, false, null
    ));

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Blood Siphon")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Dark Set. Has a chance to apply Blood Siphon to the enemy when you get hit, allowing you to recover a portion of the damage done.")
    public static final ResourceKey<Enchantment> BLOOD_SIPHON = register("blood_siphon", () -> armorEnchantment(
            EnchantmentCategory.ARMOR_HEAD, slots(EquipmentSlot.HEAD), LIGHT_DRAGON
    ));

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Murderer's Cunning")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Dark Set. You inflict increased damage to targets with full health.")
    public static final ResourceKey<Enchantment> MURDERERS_CUNNING = register("murderers_cunning", () -> armorEnchantment(
            EnchantmentCategory.ARMOR_CHEST, slots(EquipmentSlot.CHEST), LIGHT_DRAGON
    ));

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Overwhelming Might")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Dark Set. Debuffs you apply to targets are increased by 1 level.")
    public static final ResourceKey<Enchantment> OVERWHELMING_MIGHT = register("overwhelming_might", () -> armorEnchantment(
            EnchantmentCategory.ARMOR_LEGS, slots(EquipmentSlot.LEGS), LIGHT_DRAGON
    ));

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Draconic Superiority")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Dark Set. All damage you inflict is increased, and your melee damage is further increased.")
    public static final ResourceKey<Enchantment> DRACONIC_SUPERIORITY = register("draconic_superiority", () -> armorEnchantment(
            EnchantmentCategory.ARMOR_FEET, slots(EquipmentSlot.FEET), LIGHT_DRAGON
    ));

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Combat Recovery")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Light Set. When you take damage, has a chance to apply Regeneration to yourself.")
    public static final ResourceKey<Enchantment> COMBAT_RECOVERY = register("combat_recovery", () -> enchantment(
            Enchantment.Rarity.UNCOMMON, EnchantmentCategory.ARMOR_HEAD, slots(EquipmentSlot.HEAD),
            1, 3, 6, 9, 6, DARK_DRAGON, false, null
    ));

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Aerodynamic Mastery")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Light Set. Reduces flight stamina cost.")
    public static final ResourceKey<Enchantment> AERODYNAMIC_MASTERY = register("aerodynamic_mastery", () -> enchantment(
            Enchantment.Rarity.COMMON, EnchantmentCategory.ARMOR_CHEST, slots(EquipmentSlot.CHEST),
            1, 1, 11, 21, 11, DARK_DRAGON, false,
            stack -> stack.is(Items.ELYTRA) || stack.getItem() instanceof ArmorItem armor && armor.getType() == ArmorItem.Type.CHESTPLATE
    ));

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Unbreakable Spirit")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Light Set. Reduces incoming debuffs by 1 level.")
    public static final ResourceKey<Enchantment> UNBREAKABLE_SPIRIT = register("unbreakable_spirit", () -> armorEnchantment(
            EnchantmentCategory.ARMOR_LEGS, slots(EquipmentSlot.LEGS), DARK_DRAGON
    ));

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Sacred Scales")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Light Set. Has a chance to reduce incoming damage.")
    public static final ResourceKey<Enchantment> SACRED_SCALES = register("sacred_scales", () -> armorEnchantment(
            EnchantmentCategory.ARMOR_FEET, slots(EquipmentSlot.FEET), DARK_DRAGON
    ));

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Outlaw's Mark")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Dark Set. Causes you to be a permanent target for dragon hunters.")
    public static final ResourceKey<Enchantment> CURSE_OF_OUTLAW = register("curse_of_outlaw", () -> enchantment(
            Enchantment.Rarity.COMMON, EnchantmentCategory.BREAKABLE, ARMOR_SLOTS,
            1, 1, 11, 21, 11, LIGHT_DRAGON, true,
            stack -> stack.getItem() instanceof ArmorItem
    ));

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Mark of Compassion")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Light Set. Villagers and dragon hunters do not take damage from you.")
    public static final ResourceKey<Enchantment> CURSE_OF_KINDNESS = register("curse_of_kindness", () -> enchantment(
            Enchantment.Rarity.COMMON, EnchantmentCategory.BREAKABLE, ALL_SLOTS,
            1, 1, 11, 21, 11, DARK_DRAGON, true, null
    ));

    private static Enchantment armorEnchantment(final EnchantmentCategory category, final EquipmentSlot[] slots,
                                                final TagKey<Enchantment> exclusiveSet) {
        return enchantment(
                Enchantment.Rarity.COMMON, category, slots,
                1, 1, 11, 21, 11, exclusiveSet, false, null
        );
    }

    private static Enchantment enchantment(final Enchantment.Rarity rarity, final EnchantmentCategory category,
                                           final EquipmentSlot[] slots, final int maxLevel,
                                           final int minCostBase, final int minCostPerLevel,
                                           final int maxCostBase, final int maxCostPerLevel,
                                           final TagKey<Enchantment> exclusiveSet, final boolean curse,
                                           final Predicate<ItemStack> applicability) {
        return new LegacyEnchantment(
                rarity, category, slots, maxLevel,
                minCostBase, minCostPerLevel, maxCostBase, maxCostPerLevel,
                exclusiveSet, curse, applicability
        );
    }

    private static ResourceKey<Enchantment> register(final String name, final Supplier<Enchantment> supplier) {
        REGISTRY.register(name, supplier);
        return ResourceKey.create(Registries.ENCHANTMENT, DragonSurvival.res(name));
    }

    private static EquipmentSlot[] slots(final EquipmentSlot... slots) {
        return slots;
    }

    private static TagKey<Enchantment> exclusiveSet(final String path) {
        return TagKey.create(Registries.ENCHANTMENT, DragonSurvival.res("exclusive_set/" + path));
    }

    private static class LegacyEnchantment extends Enchantment {
        private final int maxLevel;
        private final int minCostBase;
        private final int minCostPerLevel;
        private final int maxCostBase;
        private final int maxCostPerLevel;
        private final TagKey<Enchantment> exclusiveSet;
        private final boolean curse;
        private final Predicate<ItemStack> applicability;

        protected LegacyEnchantment(final Rarity rarity, final EnchantmentCategory category,
                                    final EquipmentSlot[] slots, final int maxLevel,
                                    final int minCostBase, final int minCostPerLevel,
                                    final int maxCostBase, final int maxCostPerLevel,
                                    final TagKey<Enchantment> exclusiveSet, final boolean curse,
                                    final Predicate<ItemStack> applicability) {
            super(rarity, category, slots);
            this.maxLevel = maxLevel;
            this.minCostBase = minCostBase;
            this.minCostPerLevel = minCostPerLevel;
            this.maxCostBase = maxCostBase;
            this.maxCostPerLevel = maxCostPerLevel;
            this.exclusiveSet = exclusiveSet;
            this.curse = curse;
            this.applicability = applicability;
        }

        @Override
        public int getMaxLevel() {
            return maxLevel;
        }

        @Override
        public int getMinCost(final int level) {
            return minCostBase + (level - 1) * minCostPerLevel;
        }

        @Override
        public int getMaxCost(final int level) {
            return maxCostBase + (level - 1) * maxCostPerLevel;
        }

        @Override
        public boolean canEnchant(final ItemStack stack) {
            return applicability == null ? super.canEnchant(stack) : applicability.test(stack);
        }

        @Override
        public boolean isCurse() {
            return curse;
        }

        @Override
        protected boolean checkCompatibility(final Enchantment other) {
            if (!super.checkCompatibility(other)) {
                return false;
            }

            if (exclusiveSet == null) {
                return true;
            }

            return BuiltInRegistries.ENCHANTMENT.getResourceKey(other)
                    .flatMap(BuiltInRegistries.ENCHANTMENT::getHolder)
                    .map(holder -> !exclusiveSet.contains(holder))
                    .orElse(true);
        }
    }
}
