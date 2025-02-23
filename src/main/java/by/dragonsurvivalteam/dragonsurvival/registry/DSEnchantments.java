package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Optional;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DSEnchantments {
    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Bolas Arrows")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Causes crossbows to shoot bolas instead, trapping hit entities.")
    public static ResourceKey<Enchantment> BOLAS = register("bolas");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Dragonsbane")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Inflict increased damage to dragons. If you kill a dragon who has the Hunter's Omen effect, they will lose some growth progress. Damages dragons who hold it.")
    public static ResourceKey<Enchantment> DRAGONSBANE = register("dragonsbane");

    /*@Translation(type = Translation.Type.ENCHANTMENT, comments = "Dragonsboon")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Test enchantment - should not be available. Attacks heal dragons and apply a regeneration effect.")
    public static ResourceKey<Enchantment> DRAGONSBOON = register("dragonsboon");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Dragonsbonk")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Test enchantment - should not be available")
    public static ResourceKey<Enchantment> DRAGONSBONK = register("dragonsbonk");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Dragon Shrinker")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Test enchantment - should not be available. Causes the user to become smaller.")
    public static ResourceKey<Enchantment> SHRINK = register("shrink");*/

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Blood Siphon")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Dark Set. Has a chance to apply Blood Siphon to the enemy when you get hit, allowing you to recover a portion of the damage done.")
    public static ResourceKey<Enchantment> BLOOD_SIPHON = register("blood_siphon");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Murderer's Cunning")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Dark Set. You inflict increased damage to targets with full health.")
    public static ResourceKey<Enchantment> MURDERERS_CUNNING = register("murderers_cunning");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Overwhelming Might")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Dark Set. Debuffs you apply to targets are increased by 1 level.")
    public static ResourceKey<Enchantment> OVERWHELMING_MIGHT = register("overwhelming_might");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Draconic Superiority")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Dark Set. All damage you inflict is increased, and your melee damage is further increased.")
    public static ResourceKey<Enchantment> DRACONIC_SUPERIORITY = register("draconic_superiority");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Combat Recovery")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Light Set. When you take damage, has a chance to apply Regeneration to yourself.")
    public static ResourceKey<Enchantment> COMBAT_RECOVERY = register("combat_recovery");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Aerodynamic Mastery")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Light Set. Reduces flight stamina cost.")
    public static ResourceKey<Enchantment> AERODYNAMIC_MASTERY = register("aerodynamic_mastery");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Unbreakable Spirit")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Light Set. Reduces incoming debuffs by 1 level.")
    public static ResourceKey<Enchantment> UNBREAKABLE_SPIRIT = register("unbreakable_spirit");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Sacred Scales")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Light Set. Has a chance to reduce incoming damage.")
    public static ResourceKey<Enchantment> SACRED_SCALES = register("sacred_scales");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Outlaw's Mark")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Dark Set. Causes you to be a permanent target for dragon hunters.")
    public static ResourceKey<Enchantment> CURSE_OF_OUTLAW = register("curse_of_outlaw");

    @Translation(type = Translation.Type.ENCHANTMENT, comments = "Mark of Compassion")
    @Translation(type = Translation.Type.ENCHANTMENT_DESCRIPTION, comments = "Light Set. Villagers and dragon hunters do not take damage from you.")
    public static ResourceKey<Enchantment> CURSE_OF_KINDNESS = register("curse_of_kindness");

    private static ResourceKey<Enchantment> register(String key) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MODID, key));
    }

    // TODO :: currently only has enchantments which are needed for further data generation (e.g. for advancements)
    public static void registerEnchantments(final BootstrapContext<Enchantment> context) {
        context.register(BOLAS, new Enchantment(
                Component.translatable(Translation.Type.ENCHANTMENT.wrap("bolas")),
                new Enchantment.EnchantmentDefinition(
                        context.lookup(Registries.ITEM).getOrThrow(ItemTags.CROSSBOW_ENCHANTABLE),
                        Optional.empty(),
                        1,
                        1,
                        Enchantment.constantCost(10),
                        Enchantment.constantCost(25),
                        1,
                        List.of(EquipmentSlotGroup.MAINHAND, EquipmentSlotGroup.OFFHAND)
                ),
                HolderSet.empty(),
                DataComponentMap.EMPTY
        ));
    }
}
