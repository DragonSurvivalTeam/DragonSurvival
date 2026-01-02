package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonAbilityHolder;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.IdentifierWrapper;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.SourceOfMagicData;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ManaHandler;
import by.dragonsurvivalteam.dragonsurvival.common.items.BolasArrowItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.ChargedCoalItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.DarkKeyItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.DragonSoulItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.FlightGrantItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.HunterKeyItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.LightKeyItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.SourceOfMagicItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.SpinGrantItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.TooltipItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.DarkDragonArmorItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.DragonHunterWeapon;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.LightDragonArmorItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.food.CustomOnFinishEffectItem;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonAbilityTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.util.BlockPosHelper;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class DSItems {
    public static final DeferredRegister.Items REGISTRY = DeferredRegister.Items.createItems(DragonSurvival.MODID);
    // At some point, the "milk curing" logic was changed to just remove all effects outright. So, change the logic here too.
    private static final Consumer<LivingEntity> REMOVE_ALL_EFFECTS = LivingEntity::removeAllEffects;

    // --- Growth --- //
    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§7■ Deformed part of the Elder dragon.§r",
            "■§f This bone contains magic that absorbs draconic energy. Makes the user smaller."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Star Bone")
    public static final Holder<Item> STAR_BONE = REGISTRY.registerItem(
        "star_bone",
        properties -> new TooltipItem(properties, "star_bone"),
        Properties::new
    );

    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§7■ Deformed part of the Elder dragon.§r",
            "■§f This heart contains magic that absorbs draconic energy. It is able to stop the natural growth cycle of the user."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Star Heart")
    public static final Holder<Item> STAR_HEART = REGISTRY.registerItem(
        "star_heart",
        Item::new,
        Properties::new
    );

    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§6■ Part of the Elder dragon.§r",
            "■§7 Dust left from an ancient creature. It is used in many dragon recipes. Can be found in ore and treasure."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Elder Dragon Dust")
    public static final Holder<Item> ELDER_DRAGON_DUST = REGISTRY.registerItem(
        "elder_dragon_dust",
        properties -> new TooltipItem(properties, "elder_dragon_dust"),
        Properties::new
    );

    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§6■ Part of the Elder dragon.§r",
            "■§7 The remains of small dragons. This is used in many dragon recipes. The dragons were created from the body of an elder creature that sacrificed itself to save the world."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Elder Dragon Bone")
    public static final Holder<Item> ELDER_DRAGON_BONE = REGISTRY.registerItem(
        "elder_dragon_bone",
        properties -> new TooltipItem(properties, "elder_dragon_bone"),
        Properties::new
    );

    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§6■ Part of the Elder dragon.§r",
            "■§7 A fragment from a small dragon's heart. Ideal for a newborn dragon's growth. Monsters with low health will rarely drop this when killed."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Dragon Heart Shard")
    public static final Holder<Item> DRAGON_HEART_SHARD = REGISTRY.registerItem(
        "heart_element",
        properties -> new TooltipItem(properties, "heart_element"),
        Properties::new
    );

    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§6■ Part of the Elder dragon.§r",
            "■§7 A strengthened, but still unstable Heart. Ideal for a young dragon's growth. Some monsters will rarely drop this when killed."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Weak Dragon Heart")
    public static final Holder<Item> WEAK_DRAGON_HEART = REGISTRY.registerItem(
        "weak_dragon_heart",
        properties -> new TooltipItem(properties, "weak_dragon_heart"),
        Properties::new
    );

    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§6■ Part of the Elder dragon.§r",
            "■§7 A heart that is radiant and full of energy. If you listen, you can hear an elder voice calling to you from the depths. Ideal for an adult dragon's growth. Monsters with a lot of health will rarely drop this when killed."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Elder Dragon Heart")
    public static final Holder<Item> ELDER_DRAGON_HEART = REGISTRY.registerItem(
        "elder_dragon_heart",
        properties -> new TooltipItem(properties, "elder_dragon_heart"),
        Properties::new
    );

    // --- Food --- //

    // TODO :: move custom applied effects to food properties and mark with 'retain_effects' in DietEntryProvider

    @Translation(type = Translation.Type.ITEM, comments = "Charged Coal")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Removes all effects.")
    public static final Holder<Item> CHARGED_COAL = REGISTRY.registerItem(
        "charged_coal",
        properties -> new ChargedCoalItem(properties, "charged_coal", REMOVE_ALL_EFFECTS),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Charged Soup")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A concoction of various cave dragon delicacies. Heats up the body enough to protect it from the damaging effects of water.")
    public static final Holder<Item> CHARGED_SOUP = REGISTRY.registerItem(
        "charged_soup",
        properties -> new CustomOnFinishEffectItem(properties, "charged_soup", entity -> {
            entity.addEffect(new MobEffectInstance(DSEffects.FIRE, Functions.minutesToTicks(5)));
        }),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Charred Meat")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A cave dragon's 'medium rare'.")
    public static final Holder<Item> CHARRED_MEAT = REGISTRY.registerItem(
        "charred_meat",
        properties -> new TooltipItem(properties, "charred_meat"),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Charred Vegetable")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Crunchy!")
    public static final Holder<Item> CHARRED_VEGETABLE = REGISTRY.registerItem(
        "charred_vegetable",
        properties -> new TooltipItem(properties, "charred_vegetable"),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Charred Mushroom")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Spore-free!")
    public static final Holder<Item> CHARRED_MUSHROOM = REGISTRY.registerItem(
        "charred_mushroom",
        properties -> new TooltipItem(properties, "charred_mushroom"),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Charred Seafood")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Dry!")
    public static final Holder<Item> CHARRED_SEAFOOD = REGISTRY.registerItem(
        "charred_seafood",
        properties -> new TooltipItem(properties, "charred_seafood"),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Hot Dragon Rod")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A blaze rod seasoned with dragon dust; a crispy snack that increases the dragon's body temperature. Protects cave dragons from water for a short time.")
    public static final Holder<Item> HOT_DRAGON_ROD = REGISTRY.registerItem(
        "hot_dragon_rod",
        properties -> new CustomOnFinishEffectItem(properties, "hot_dragon_rod", entity -> entity.addEffect(new MobEffectInstance(DSEffects.FIRE, Functions.minutesToTicks(1)))),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Explosive Copper")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Cave dragon dessert. Dangerous and unstable.")
    public static final Holder<Item> EXPLOSIVE_COPPER = REGISTRY.registerItem(
        "explosive_copper",
        properties -> new CustomOnFinishEffectItem(properties, "explosive_copper", entity -> {
            entity.hurt(entity.damageSources().explosion(entity, entity), 1f);
            entity.level().addParticle(ParticleTypes.EXPLOSION, entity.getX(), entity.getEyeY(), entity.getZ(), 1.0D, 0.0D, 0.0D);
            entity.level().playSound(null, BlockPosHelper.get(entity.getEyePosition()), SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR, SoundSource.PLAYERS, 1f, 1f);
        }),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Volatile Mineral Mix")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Removes Wither and Poison effects, gives Absorption and Regeneration. A high-pressure heated mixture of Fused Quartz and Explosive Copper.")
    public static final Holder<Item> QUARTZ_EXPLOSIVE_COPPER = REGISTRY.registerItem(
        "quartz_explosive_copper",
        properties -> new CustomOnFinishEffectItem(properties, "quartz_explosive_copper", entity -> {
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
            entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)));
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1));
        }),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Fused Quartz")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Grants Regeneration. Quartz and Amethyst that has been combined with enough heat and force to greatly improve their flavor.")
    public static final Holder<Item> DOUBLE_QUARTZ = REGISTRY.registerItem(
        "double_quartz",
        properties -> new CustomOnFinishEffectItem(properties, "double_quartz", entity ->
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5)))),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Sweet & Sour Rabbit")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Cleanses all active effects. Rabbit, marinated with a mixture of honey and spider eyes. Traditionally served during dragon holidays.")
    public static final Holder<Item> SWEET_SOUR_RABBIT = REGISTRY.registerItem(
        "sweet_sour_rabbit",
        properties -> new CustomOnFinishEffectItem(properties, "sweet_sour_rabbit", REMOVE_ALL_EFFECTS),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Luminous Tincture")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A tincture of various glowing ingredients that protects against the darkness when consumed. Dark environments sap a forest dragon's strength as their own plants begin to devour them.")
    public static final Holder<Item> LUMINOUS_OINTMENT = REGISTRY.registerItem(
        "luminous_ointment",
        properties -> new CustomOnFinishEffectItem(properties, "luminous_ointment", entity -> {
            entity.addEffect(new MobEffectInstance(DSEffects.MAGIC, Functions.minutesToTicks(5)));
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, Functions.minutesToTicks(5)));
        }),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Crystalline Chorus")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Removes Wither and Poison effects, and gives Absorption and Regeneration. Chorus Fruit stuffed with diamond dust. The diamonds are pulverized using the Ender Dragon's breath.")
    public static final Holder<Item> DIAMOND_CHORUS = REGISTRY.registerItem(
        "diamond_chorus",
        properties -> new CustomOnFinishEffectItem(properties, "diamond_chorus", entity -> {
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
            entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)));
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1));
        }),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Smelly Meat Porridge")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A meat and bone fertilizer. Contains plenty of nutrients to feed a forest dragon's plants.")
    public static final Holder<Item> SMELLY_MEAT_PORRIDGE = REGISTRY.registerItem(
        "smelly_meat_porridge",
        properties -> new TooltipItem(properties, "smelly_meat_porridge"),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Meat & Wild Berries")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A classic combination of sweet berries and wild herbs. A favorite amongst forest dragons.")
    public static final Holder<Item> MEAT_WILD_BERRIES = REGISTRY.registerItem(
        "meat_wild_berries",
        properties -> new TooltipItem(properties, "meat_wild_berries"),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Meat-Chorus Mix")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Grants Regeneration. The exotic Chorus Fruit has always been prized among forest dragons, and is commonly combined with raw meats.")
    public static final Holder<Item> MEAT_CHORUS_MIX = REGISTRY.registerItem(
        "meat_chorus_mix",
        properties -> new CustomOnFinishEffectItem(properties, "meat_chorus_mix", entity -> entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5)))),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Seasoned Fish")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A popular seafood dish amongst dragons. It is prepared by removing the skin and coating it with ink. This gives it an unusual taste and hue. The kelp leaf is for decoration and freshness.")
    public static final Holder<Item> SEASONED_FISH = REGISTRY.registerItem(
        "seasoned_fish",
        properties -> new TooltipItem(properties, "seasoned_fish"),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Golden Pufferfish")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Grants Regeneration. Gilded pufferfish dusted with crushed coral. The gold neutralizes the natural poison of the pufferfish, making it safer for consumption.")
    public static final Holder<Item> GOLDEN_CORAL_PUFFERFISH = REGISTRY.registerItem(
        "golden_coral_pufferfish",
        properties -> new CustomOnFinishEffectItem(properties, "golden_coral_pufferfish", entity -> entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5)))),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Frozen Fish")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Cleanses all effects and replenishes water. Food and water in one convenient package!")
    public static final Holder<Item> FROZEN_RAW_FISH = REGISTRY.registerItem(
        "frozen_raw_fish",
        properties -> new CustomOnFinishEffectItem(properties, "golden_coral_pufferfish", REMOVE_ALL_EFFECTS),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Golden Turtle Egg")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Removes Wither and Poison effects, and gives Absorption and Regeneration. A turtle egg stuffed with dragon dust and gold. After a while, the dust reacts with the eggshell, changing its color and texture.")
    public static final Holder<Item> GOLDEN_TURTLE_EGG = REGISTRY.registerItem(
        "golden_turtle_egg",
        properties -> new CustomOnFinishEffectItem(properties, "golden_turtle_egg", entity -> {
            entity.removeEffect(MobEffects.POISON);
            entity.removeEffect(MobEffects.WITHER);
            entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)));
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1));
        }),
        Properties::new
    );

    private static final Consumer<LivingEntity> EAT_TREAT_EFFECT = entity -> {
        if (entity instanceof Player player) {
            ManaHandler.replenishMana(player, ManaHandler.getMaxMana(player));
            player.addEffect(new MobEffectInstance(DSEffects.SOURCE_OF_MAGIC, Functions.minutesToTicks(1)));
        }
    };

    @Translation(type = Translation.Type.ITEM, comments = "Sea Dragon Treat")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Grants infinite mana temporarily.")
    public static final Holder<Item> SEA_DRAGON_TREAT = REGISTRY.registerItem(
        "sea_dragon_treat",
        properties -> new CustomOnFinishEffectItem(properties, "sea_dragon_treat", EAT_TREAT_EFFECT),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Cave Dragon Treat")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Grants infinite mana temporarily.")
    public static final Holder<Item> CAVE_DRAGON_TREAT = REGISTRY.registerItem(
        "cave_dragon_treat",
        properties -> new CustomOnFinishEffectItem(properties, "cave_dragon_treat", EAT_TREAT_EFFECT),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Forest Dragon Treat")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Grants infinite mana temporarily.")
    public static final Holder<Item> FOREST_DRAGON_TREAT = REGISTRY.registerItem(
        "forest_dragon_treat",
        properties -> new CustomOnFinishEffectItem(properties, "forest_dragon_treat", EAT_TREAT_EFFECT),
        Properties::new
    );

    // --- Armor --- //

    @Translation(type = Translation.Type.ITEM, comments = "Light Upgrade")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A rare item that allows you to upgrade your Netherite Armor to Light Armor. The armor will have built-in enchantments to help you live peacefully in this world.")
    public static final Holder<Item> LIGHT_UPGRADE = REGISTRY.registerItem(
        "light_upgrade",
        properties -> new TooltipItem(properties, "light_upgrade"),
        () -> new Properties().rarity(Rarity.RARE)
    );

    @Translation(type = Translation.Type.ITEM, comments = "Light Helmet")
    public static final Holder<Item> LIGHT_DRAGON_HELMET = REGISTRY.registerItem(
        "light_dragon_helmet",
        LightDragonArmorItem::new,
        () -> new Item.Properties().humanoidArmor(DSArmorMaterials.DRAGON_ARMOR_MATERIAL, ArmorType.HELMET).rarity(Rarity.EPIC).fireResistant()
    );

    @Translation(type = Translation.Type.ITEM, comments = "Light Chestplate")
    public static final Holder<Item> LIGHT_DRAGON_CHESTPLATE = REGISTRY.registerItem(
        "light_dragon_chestplate",
        LightDragonArmorItem::new,
        () -> new Item.Properties().humanoidArmor(DSArmorMaterials.DRAGON_ARMOR_MATERIAL, ArmorType.CHESTPLATE).rarity(Rarity.EPIC).fireResistant()
    );

    @Translation(type = Translation.Type.ITEM, comments = "Light Leggings")
    public static final Holder<Item> LIGHT_DRAGON_LEGGINGS = REGISTRY.registerItem(
        "light_dragon_leggings",
        LightDragonArmorItem::new,
        () -> new Item.Properties().humanoidArmor(DSArmorMaterials.DRAGON_ARMOR_MATERIAL, ArmorType.LEGGINGS).rarity(Rarity.EPIC).fireResistant()
    );

    @Translation(type = Translation.Type.ITEM, comments = "Light Boots")
    public static final Holder<Item> LIGHT_DRAGON_BOOTS = REGISTRY.registerItem(
        "light_dragon_boots",
            LightDragonArmorItem::new,
            () -> new Item.Properties().humanoidArmor(DSArmorMaterials.DRAGON_ARMOR_MATERIAL, ArmorType.BOOTS).rarity(Rarity.EPIC).fireResistant()
    );

    @Translation(type = Translation.Type.ITEM, comments = "Dark Upgrade")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A rare item that allows you to upgrade your Netherite Armor to Dark Armor. The armor will have built-in enchantments to help you effectively shed blood in the name of evil.")
    public static final Holder<Item> DARK_UPGRADE = REGISTRY.register("dark_upgrade", location -> new TooltipItem(new Properties().rarity(Rarity.RARE), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Dark Helmet")
    public static final Holder<Item> DARK_DRAGON_HELMET = REGISTRY.registerItem(
        "dark_dragon_helmet",
        DarkDragonArmorItem::new,
        () -> new Item.Properties().humanoidArmor(DSArmorMaterials.DRAGON_ARMOR_MATERIAL, ArmorType.HELMET).rarity(Rarity.EPIC).fireResistant()
    );

    @Translation(type = Translation.Type.ITEM, comments = "Dark Chestplate")
    public static final Holder<Item> DARK_DRAGON_CHESTPLATE = REGISTRY.registerItem(
        "dark_dragon_chestplate",
        DarkDragonArmorItem::new,
        () -> new Item.Properties().humanoidArmor(DSArmorMaterials.DRAGON_ARMOR_MATERIAL, ArmorType.CHESTPLATE).rarity(Rarity.EPIC).fireResistant()
    );

    @Translation(type = Translation.Type.ITEM, comments = "Dark Leggings")
    public static final Holder<Item> DARK_DRAGON_LEGGINGS = REGISTRY.registerItem(
        "dark_dragon_leggings",
        DarkDragonArmorItem::new,
        () -> new Item.Properties().humanoidArmor(DSArmorMaterials.DRAGON_ARMOR_MATERIAL, ArmorType.LEGGINGS).rarity(Rarity.EPIC).fireResistant()
    );

    @Translation(type = Translation.Type.ITEM, comments = "Dark Boots")
    public static final Holder<Item> DARK_DRAGON_BOOTS = REGISTRY.registerItem(
        "dark_dragon_boots",
        DarkDragonArmorItem::new,
        () -> new Item.Properties().humanoidArmor(DSArmorMaterials.DRAGON_ARMOR_MATERIAL, ArmorType.BOOTS).rarity(Rarity.EPIC).fireResistant()
    );

    // --- Weapons --- //

    @Translation(type = Translation.Type.ITEM, comments = "Sword That Bonks Dragons")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 The sword of the dragon hunters. Slow, but strong. Can be found in the hunters treasury.")
    public static final Holder<Item> DRAGON_HUNTER_SWORD = REGISTRY.registerItem(
        "dragon_hunter_sword",
        properties -> new DragonHunterWeapon(properties, "dragon_hunter_sword", List.of(Pair.of(DSEnchantments.DRAGONSBANE, 3))),
        () -> new Item.Properties().sword(ToolMaterial.NETHERITE, 4.0F, -2.8F).rarity(Rarity.EPIC).fireResistant()
    );

    @Translation(type = Translation.Type.ITEM, comments = "Iron Partisan")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A long shafted weapon designed to take out enemies at a distance. Especially good against flying dragons.")
    public static final Holder<Item> PARTISAN = REGISTRY.registerItem(
        "hunter_partisan",
        properties -> new DragonHunterWeapon(properties, "hunter_partisan", List.of()),
        () -> new Item.Properties()
            .sword(ToolMaterial.IRON, 6.0F, -2.6F)
            .component(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.builder()
                    .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(DragonSurvival.res("partisan_block_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(DragonSurvival.res("partisan_attack_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build())
    );

    @Translation(type = Translation.Type.ITEM, comments = "Diamond Partisan")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A long shafted weapon designed to take out enemies at a distance. Especially good against flying dragons.")
    public static final Holder<Item> HUNTER_PARTISAN_DIAMOND = REGISTRY.registerItem(
        "hunter_partisan_diamond",
        properties -> new DragonHunterWeapon(properties, "hunter_partisan_diamond", List.of(Pair.of(DSEnchantments.DRAGONSBANE, 3))),
        () -> new Item.Properties()
            .sword(ToolMaterial.DIAMOND, 7.0F, -2.6F)
            .component(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.builder()
                    .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(DragonSurvival.res("partisan_block_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(DragonSurvival.res("partisan_attack_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build())
    );

    @Translation(type = Translation.Type.ITEM, comments = "Netherite Partisan")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A long shafted weapon designed to take out enemies at a distance. Especially good against flying dragons.")
    public static final Holder<Item> HUNTER_PARTISAN_NETHERITE = REGISTRY.registerItem(
        "hunter_partisan_netherite",
        properties -> new DragonHunterWeapon(properties, "hunter_partisan_netherite", List.of(Pair.of(DSEnchantments.DRAGONSBANE, 3))),
        () -> new Item.Properties()
            .sword(ToolMaterial.NETHERITE, 8.0F, -2.6F)
            .component(DataComponents.ATTRIBUTE_MODIFIERS,
                ItemAttributeModifiers.builder()
                    .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(DragonSurvival.res("partisan_block_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(DragonSurvival.res("partisan_attack_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build())
    );

    // --- Block items --- //

    public static final Holder<Item> CAVE_SOURCE_OF_MAGIC = REGISTRY.registerItem(
        "cave_source_of_magic",
        properties -> new SourceOfMagicItem(DSBlocks.CAVE_SOURCE_OF_MAGIC.get(), properties),
        () -> new Item.Properties().rarity(Rarity.EPIC)
                    .component(DSDataComponents.SOURCE_OF_MAGIC, new SourceOfMagicData(
                            List.of(
                                    new SourceOfMagicData.Consumable(DSItems.ELDER_DRAGON_DUST.value(), Functions.secondsToTicks(10)),
                                    new SourceOfMagicData.Consumable(DSItems.ELDER_DRAGON_BONE.value(), Functions.secondsToTicks(30)),
                                    new SourceOfMagicData.Consumable(DSItems.DRAGON_HEART_SHARD.value(), Functions.secondsToTicks(60)),
                                    new SourceOfMagicData.Consumable(DSItems.WEAK_DRAGON_HEART.value(), Functions.secondsToTicks(120)),
                                    new SourceOfMagicData.Consumable(DSItems.ELDER_DRAGON_HEART.value(), Functions.secondsToTicks(300))
                            ),
                            List.of(BuiltInDragonSpecies.CAVE_DRAGON)
                    ))
    );

    public static final Holder<Item> FOREST_SOURCE_OF_MAGIC = REGISTRY.registerItem(
        "forest_source_of_magic",
        properties -> new SourceOfMagicItem(DSBlocks.FOREST_SOURCE_OF_MAGIC.get(), properties),
        () -> new Item.Properties().rarity(Rarity.EPIC)
            .component(DSDataComponents.SOURCE_OF_MAGIC, new SourceOfMagicData(
                List.of(
                    new SourceOfMagicData.Consumable(DSItems.ELDER_DRAGON_DUST.value(), Functions.secondsToTicks(10)),
                    new SourceOfMagicData.Consumable(DSItems.ELDER_DRAGON_BONE.value(), Functions.secondsToTicks(30)),
                    new SourceOfMagicData.Consumable(DSItems.DRAGON_HEART_SHARD.value(), Functions.secondsToTicks(60)),
                    new SourceOfMagicData.Consumable(DSItems.WEAK_DRAGON_HEART.value(), Functions.secondsToTicks(120)),
                    new SourceOfMagicData.Consumable(DSItems.ELDER_DRAGON_HEART.value(), Functions.secondsToTicks(300))
                ),
                List.of(BuiltInDragonSpecies.FOREST_DRAGON)
            ))
    );

    public static final Holder<Item> SEA_SOURCE_OF_MAGIC = REGISTRY.registerItem(
        "sea_source_of_magic",
        properties -> new SourceOfMagicItem(DSBlocks.SEA_SOURCE_OF_MAGIC.get(), properties),
        () -> new Item.Properties().rarity(Rarity.EPIC)
            .component(DSDataComponents.SOURCE_OF_MAGIC, new SourceOfMagicData(
                List.of(
                    new SourceOfMagicData.Consumable(DSItems.ELDER_DRAGON_DUST.value(), Functions.secondsToTicks(10)),
                    new SourceOfMagicData.Consumable(DSItems.ELDER_DRAGON_BONE.value(), Functions.secondsToTicks(30)),
                    new SourceOfMagicData.Consumable(DSItems.DRAGON_HEART_SHARD.value(), Functions.secondsToTicks(60)),
                    new SourceOfMagicData.Consumable(DSItems.WEAK_DRAGON_HEART.value(), Functions.secondsToTicks(120)),
                    new SourceOfMagicData.Consumable(DSItems.ELDER_DRAGON_HEART.value(), Functions.secondsToTicks(300))
                ),
                List.of(BuiltInDragonSpecies.SEA_DRAGON)
            ))
    );

    // --- Misc --- //

    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 By combining the basic elements of dragons, you have the item to activate the beacon. Depending what species of dragon you are, the beacon will have different effects.")
    @Translation(type = Translation.Type.ITEM, comments = "Beacon Activator")
    public static final Holder<Item> BEACON_ACTIVATOR = REGISTRY.registerItem(
        "beacon_activator",
        properties -> new TooltipItem(properties, "beacon_activator"),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Dragon Soul")
    public static final Holder<Item> DRAGON_SOUL = REGISTRY.registerItem(
        "dragon_soul",
        DragonSoulItem::new,
        () -> new Properties().stacksTo(1).rarity(Rarity.EPIC)
    );

    public static final String LIGHT_KEY_ID = "light_key";

    @Translation(type = Translation.Type.ITEM, comments = "Light Key")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 An enchanted key that unlocks the Light Vault. Purchased from the dragon rider villager.")
    public static final Holder<Item> LIGHT_KEY = REGISTRY.registerItem(
        LIGHT_KEY_ID,
        properties -> new LightKeyItem(
            properties,
            DragonSurvival.res("geo/" + LIGHT_KEY_ID + ".geo.json"),
            DragonSurvival.res("textures/item/" + LIGHT_KEY_ID + ".png"),
            DragonSurvival.res("light_treasure")),
        () -> new Item.Properties().rarity(Rarity.UNCOMMON).component(DSDataComponents.TARGET_POSITION, new Vector3f())
    );

    public static final String DARK_KEY_ID = "dark_key";

    @Translation(type = Translation.Type.ITEM, comments = "Dark Key")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 An enchanted key that unlocks the Dark Vault in Nether. Drops if you kill a dragon hunter knight.")
    public static final Holder<Item> DARK_KEY = REGISTRY.registerItem(
        DARK_KEY_ID,
        properties -> new DarkKeyItem(
            properties,
            DragonSurvival.res("geo/" + DARK_KEY_ID + ".geo.json"),
            DragonSurvival.res("textures/item/" + DARK_KEY_ID + ".png"),
            DragonSurvival.res("dark_treasure")),
        () -> new Item.Properties().rarity(Rarity.UNCOMMON).component(DSDataComponents.TARGET_POSITION, new Vector3f())
    );

    public static final String HUNTER_KEY_ID = "hunter_key";

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Key")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 An enchanted key that unlocks a treasure vault for dragon hunters. Can be purchased from the Hunter Leader in the Hunter Castle.")
    public static final Holder<Item> HUNTER_KEY = REGISTRY.registerItem(
        HUNTER_KEY_ID,
        properties -> new HunterKeyItem(
            properties,
            DragonSurvival.res("geo/" + HUNTER_KEY_ID + ".geo.json"),
            DragonSurvival.res("textures/item/" + HUNTER_KEY_ID + ".png"),
            DragonSurvival.res("hunter_treasure")),
        () -> new Item.Properties().rarity(Rarity.UNCOMMON).component(DSDataComponents.TARGET_POSITION, new Vector3f())
    );

    @Translation(type = Translation.Type.ITEM, comments = "Spearman Promotion")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■ §7Use on a Hunter Spearman to turn him into a Hunter Leader.")
    public static final Holder<Item> SPEARMAN_PROMOTION = REGISTRY.registerItem(
        "spearman_promotion",
        properties -> new TooltipItem(properties, "spearman_promotion"),
        () -> new Properties().rarity(Rarity.COMMON)
    );

    @Translation(type = Translation.Type.ITEM, comments = "Flight Grant")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 This item gives you the ability to fly. Consumed on use.")
    public static final Holder<Item> FLIGHT_GRANT_ITEM = REGISTRY.registerItem(
        "wing_grant",
        properties -> new FlightGrantItem(properties, "wing_grant"),
        Properties::new
    );

    @Translation(type = Translation.Type.ITEM, comments = "Spin Grant")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 This item gives you dragon the ability to spin while flying. Consumed on use.")
    public static final Holder<Item> SPIN_GRANT_ITEM = REGISTRY.registerItem(
        "spin_grant",
        properties -> new SpinGrantItem(properties, "spin_grant"),
        Properties::new
    );

    // --- Spawn eggs --- //

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Hound Spawn Egg")
    public static final Holder<Item> HOUND_SPAWN_EGG = REGISTRY.registerItem(
        "hound_spawn_egg",
        SpawnEggItem::new,
        () -> new Item.Properties().spawnEgg(DSEntities.HUNTER_HOUND.get()).rarity(Rarity.COMMON)
    );

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Spearman Spawn Egg")
    public static final Holder<Item> SPEARMAN_SPAWN_EGG = REGISTRY.registerItem(
        "spearman_spawn_egg",
        SpawnEggItem::new,
        () -> new Item.Properties().spawnEgg(DSEntities.HUNTER_SPEARMAN.get()).rarity(Rarity.COMMON)
    );

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Knight Spawn Egg")
    public static final Holder<Item> KNIGHT_SPAWN_EGG = REGISTRY.registerItem(
        "knight_spawn_egg",
        SpawnEggItem::new,
        () -> new Item.Properties().spawnEgg(DSEntities.HUNTER_KNIGHT.get()).rarity(Rarity.COMMON)
    );

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Ambusher Spawn Egg")
    public static final Holder<Item> AMBUSHER_SPAWN_EGG = REGISTRY.registerItem(
        "ambusher_spawn_egg",
        SpawnEggItem::new,
        () -> new Item.Properties().spawnEgg(DSEntities.HUNTER_AMBUSHER.get()).rarity(Rarity.COMMON)
    );

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Griffin Spawn Egg")
    public static final Holder<Item> GRIFFIN_SPAWN_EGG = REGISTRY.registerItem(
        "griffin_spawn_egg",
        SpawnEggItem::new,
        () -> new Item.Properties().spawnEgg(DSEntities.HUNTER_GRIFFIN.get()).rarity(Rarity.COMMON)
    );

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Leader Spawn Egg")
    public static final Holder<Item> LEADER_SPAWN_EGG = REGISTRY.registerItem(
        "leader_spawn_egg",
        SpawnEggItem::new,
        () -> new Item.Properties().spawnEgg(DSEntities.HUNTER_LEADER.get()).rarity(Rarity.COMMON)
    );

    // --- Test Item --- //
    @Translation(type = Translation.Type.ITEM, comments = "Magic Stick")
    public static final Holder<Item> MAGIC_STICK = REGISTRY.registerItem(
        "magic_stick",
        Item::new,
        () -> new Item.Properties().rarity(Rarity.EPIC)
                    .component(DSDataComponents.DRAGON_ABILITIES,
                            new DragonAbilityHolder(
                                    List.of(
                                            new DragonAbilityHolder.AbilityPair(
                                                    List.of(IdentifierWrapper.convert(DSDragonAbilityTags.TEST_ABILITIES)),
                                                    List.of(),
                                                    false
                                            ),
                                            new DragonAbilityHolder.AbilityPair(
                                                    List.of(IdentifierWrapper.convert(SeaDragonAbilities.ORE_GLOW)),
                                                    List.of(),
                                                    true
                                            ),
                                            new DragonAbilityHolder.AbilityPair(
                                                    List.of(IdentifierWrapper.convert(DSDragonAbilityTags.CAVE)),
                                                    List.of(IdentifierWrapper.convert(DSDragonAbilityTags.FOREST)),
                                                    true
                                            )
                                    ),
                                    Optional.empty(),
                                    List.of(IdentifierWrapper.convert(BuiltInDragonSpecies.FOREST_DRAGON))
                            )
    ));

    private static String getDescriptionKey(String path) {
        return "item.dragonsurvival." + path;
    }

    // --- Not shown in creative tab --- //

    public static final Holder<Item> BOLAS = REGISTRY.registerItem(
        "bolas",
        BolasArrowItem::new,
        Properties::new
    );

    public static final Holder<Item> HUNTING_NET = REGISTRY.registerItem(
        "dragon_hunting_mesh",
        Item::new,
        Properties::new
    );

    public static final Holder<Item> LIGHTNING_TEXTURE_ITEM = REGISTRY.registerItem(
        "lightning",
        Item::new,
        Properties::new
    );

    public static final Holder<Item> FOREST_ICON = REGISTRY.registerItem(
        "forest_icon",
        Item::new,
        Properties::new
    );

    public static final Holder<Item> CAVE_ICON = REGISTRY.registerItem(
        "cave_icon",
        Item::new,
        Properties::new
    );

    public static final Holder<Item> SEA_ICON = REGISTRY.registerItem(
        "sea_icon",
        Item::new,
        Properties::new
    );

    public static final Holder<Item> FOREST_FULL_ICON = REGISTRY.registerItem(
        "forest_full_icon",
        Item::new,
        Properties::new
    );

    public static final Holder<Item> CAVE_FULL_ICON = REGISTRY.registerItem(
        "cave_full_icon",
        Item::new,
        Properties::new
    );

    public static final Holder<Item> SEA_FULL_ICON = REGISTRY.registerItem(
        "sea_full_icon",
        Item::new,
        Properties::new
    );

    public static final Holder<Item> ACTIVATED_DRAGON_BEACON = REGISTRY.registerItem(
        "activated_dragon_beacon",
        Item::new,
        Properties::new
    );

    public static final Holder<Item> CAVE_BEACON = REGISTRY.registerItem(
        "cave_beacon",
        Item::new,
        Properties::new
    );

    public static final Holder<Item> FOREST_BEACON = REGISTRY.registerItem(
        "forest_beacon",
        Item::new,
        Properties::new
    );

    public static final Holder<Item> SEA_BEACON = REGISTRY.registerItem(
        "sea_beacon",
        Item::new,
        Properties::new
    );
}