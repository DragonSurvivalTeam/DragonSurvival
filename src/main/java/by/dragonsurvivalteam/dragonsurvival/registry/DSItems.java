package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
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
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.util.BlockPosHelper;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public class DSItems {
    public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(BuiltInRegistries.ITEM, DragonSurvival.MODID);
    private static final Consumer<LivingEntity> REMOVE_EFFECTS_CURED_BY_MILK = entity -> entity.removeEffectsCuredBy(EffectCures.MILK);

    // --- Growth --- //
    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§7■ Deformed part of the Elder dragon.§r",
            "■§f The energy absorbing dragon magic had captured this bone. Makes the dragon smaller."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Star Bone")
    public static final Holder<Item> STAR_BONE = REGISTRY.register("star_bone", location -> new TooltipItem(new Properties(), location.getPath()));

    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§7■ Deformed part of the Elder dragon.§r",
            "■§f The energy absorbing dragon magic had captured this heart. It is able to change the dragon's growth many times."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Star Heart")
    public static final Holder<Item> STAR_HEART = REGISTRY.register("star_heart", () -> new Item(new Properties()));

    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§6■ Part of the Elder dragon.§r",
            "■§7 Dust left from an ancient creature, this is used in many dragon recipes. Can be found in ore and treasure."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Elder Dragon Dust")
    public static final Holder<Item> ELDER_DRAGON_DUST = REGISTRY.register("elder_dragon_dust", location -> new TooltipItem(new Item.Properties(), location.getPath()));

    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§6■ Part of the Elder dragon.§r",
            "■§7 The remains of small dragons. This is used in many dragon recipes. The dragons were created from the body of an elder creature that sacrificed itself to save the world."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Elder Dragon Bone")
    public static final Holder<Item> ELDER_DRAGON_BONE = REGISTRY.register("elder_dragon_bone", location -> new TooltipItem(new Item.Properties(), location.getPath()));

    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§6■ Part of the Elder dragon.§r",
            "■§7 A fragment from a small dragon's heart. Ideal for a newborn dragon's growth. Monsters with low health will rarely drop this when killed."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Dragon Heart Shard")
    public static final Holder<Item> DRAGON_HEART_SHARD = REGISTRY.register("heart_element", location -> new TooltipItem(new Item.Properties(), location.getPath()));

    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§6■ Part of the Elder dragon.§r",
            "■§7 A strengthened, but still unstable Heart. Ideal for a young dragon's growth. Some monsters will rarely drop this when killed."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Weak Dragon Heart")
    public static final Holder<Item> WEAK_DRAGON_HEART = REGISTRY.register("weak_dragon_heart", location -> new TooltipItem(new Item.Properties(), location.getPath()));

    @Translation(type = Translation.Type.DESCRIPTION, comments = {
            "§6■ Part of the Elder dragon.§r",
            "■§7 A heart that is radiant and full of energy. If you listen, you can hear an elder voice calling to you from the depths. Ideal for an adult dragon's growth. Monsters with a lot of health will rarely drop this when killed."
    })
    @Translation(type = Translation.Type.ITEM, comments = "Elder Dragon Heart")
    public static final Holder<Item> ELDER_DRAGON_HEART = REGISTRY.register("elder_dragon_heart", location -> new TooltipItem(new Item.Properties(), location.getPath()));

    // --- Food --- //

    @Translation(type = Translation.Type.ITEM, comments = "Charged Coal")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Removes all effects.")
    public static final Holder<Item> CHARGED_COAL = REGISTRY.register("charged_coal", location -> new ChargedCoalItem(new Properties(), location.getPath(), REMOVE_EFFECTS_CURED_BY_MILK));

    @Translation(type = Translation.Type.ITEM, comments = "Charged Soup")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A concoction of various cave dragon delicacies. Heats up the body enough to protect it from the damaging effects of water.")
    public static final Holder<Item> CHARGED_SOUP = REGISTRY.register("charged_soup", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), entity -> {
        entity.addEffect(new MobEffectInstance(DSEffects.FIRE, Functions.minutesToTicks(5)));
    }));

    @Translation(type = Translation.Type.ITEM, comments = "Charred Meat")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A cave dragon's 'medium rare'.")
    public static final Holder<Item> CHARRED_MEAT = REGISTRY.register("charred_meat", location -> new TooltipItem(new Properties(), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Charred Vegetable")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Crunchy!")
    public static final Holder<Item> CHARRED_VEGETABLE = REGISTRY.register("charred_vegetable", location -> new TooltipItem(new Properties(), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Charred Mushroom")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Spore-free!")
    public static final Holder<Item> CHARRED_MUSHROOM = REGISTRY.register("charred_mushroom", location -> new TooltipItem(new Properties(), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Charred Seafood")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Dry!")
    public static final Holder<Item> CHARRED_SEAFOOD = REGISTRY.register("charred_seafood", location -> new TooltipItem(new Properties(), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Hot Dragon Rod")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A blaze rod seasoned with dragon dust; a crispy snack that increases the dragon's body temperature. Protects cave dragons from water for a short time.")
    public static final Holder<Item> HOT_DRAGON_ROD = REGISTRY.register("hot_dragon_rod", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), entity -> entity.addEffect(new MobEffectInstance(DSEffects.FIRE, Functions.minutesToTicks(1)))));

    @Translation(type = Translation.Type.ITEM, comments = "Explosive Copper")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Cave dragon dessert. Dangerous and unstable.")
    public static final Holder<Item> EXPLOSIVE_COPPER = REGISTRY.register("explosive_copper", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), entity -> {
        entity.hurt(entity.damageSources().explosion(entity, entity), 1f);
        entity.level().addParticle(ParticleTypes.EXPLOSION, entity.getX(), entity.getEyeY(), entity.getZ(), 1.0D, 0.0D, 0.0D);
        entity.level().playSound(null, BlockPosHelper.get(entity.getEyePosition()), SoundEvents.FIREWORK_ROCKET_TWINKLE_FAR, SoundSource.PLAYERS, 1f, 1f);
    }));

    @Translation(type = Translation.Type.ITEM, comments = "Volatile Mineral Mix")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Removes Wither and Poison effects, gives Absorption and Regeneration. A high-pressure heated mixture of Fused Quartz and Explosive Copper.")
    public static final Holder<Item> QUARTZ_EXPLOSIVE_COPPER = REGISTRY.register("quartz_explosive_copper", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), entity -> {
        entity.removeEffect(MobEffects.POISON);
        entity.removeEffect(MobEffects.WITHER);
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)));
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1));
    }));

    @Translation(type = Translation.Type.ITEM, comments = "Fused Quartz")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Grants Regeneration. Quartz and Amethyst that has been combined with enough heat and force to greatly improve their flavor.")
    public static final Holder<Item> DOUBLE_QUARTZ = REGISTRY.register("double_quartz", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), entity -> entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5)))));

    @Translation(type = Translation.Type.ITEM, comments = "Sweet & Sour Rabbit")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Cleanses all active effects. Rabbit, marinated with a mixture of honey and spider eyes. Traditionally served during dragon holidays.")
    public static final Holder<Item> SWEET_SOUR_RABBIT = REGISTRY.register("sweet_sour_rabbit", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), REMOVE_EFFECTS_CURED_BY_MILK));

    @Translation(type = Translation.Type.ITEM, comments = "Luminous Tincture")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A tincture of various glowing ingredients that protects against the darkness when consumed. Dark environments sap a forest dragon's strength as their own plants begin to devour them.")
    public static final Holder<Item> LUMINOUS_OINTMENT = REGISTRY.register("luminous_ointment", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), entity -> {
        entity.addEffect(new MobEffectInstance(DSEffects.MAGIC, Functions.minutesToTicks(5)));
        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, Functions.minutesToTicks(5)));
    }));

    @Translation(type = Translation.Type.ITEM, comments = "Crystalline Chorus")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Removes Wither and Poison effects, and gives Absorption and Regeneration. Chorus Fruit stuffed with diamond dust. The diamonds are pulverized using the Ender Dragon's breath.")
    public static final Holder<Item> DIAMOND_CHORUS = REGISTRY.register("diamond_chorus", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), entity -> {
        entity.removeEffect(MobEffects.POISON);
        entity.removeEffect(MobEffects.WITHER);
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)));
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1));
    }));

    @Translation(type = Translation.Type.ITEM, comments = "Smelly Meat Porridge")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A meat and bone fertilizer. Contains plenty of nutrients to feed a forest dragon's plants.")
    public static final Holder<Item> SMELLY_MEAT_PORRIDGE = REGISTRY.register("smelly_meat_porridge", location -> new TooltipItem(new Properties(), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Meat & Wild Berries")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A classic combination of sweet berries and wild herbs. A favorite amongst forest dragons.")
    public static final Holder<Item> MEAT_WILD_BERRIES = REGISTRY.register("meat_wild_berries", location -> new TooltipItem(new Properties(), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Meat-Chorus Mix")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Grants Regeneration. The exotic Chorus Fruit has always been prized among forest dragons, and is commonly combined with raw meats.")
    public static final Holder<Item> MEAT_CHORUS_MIX = REGISTRY.register("meat_chorus_mix", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), entity -> entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5)))));

    @Translation(type = Translation.Type.ITEM, comments = "Seasoned Fish")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A popular seafood dish amongst dragons. It is prepared by removing the skin and coating it with ink. This gives it an unusual taste and hue. The kelp leaf is for decoration and freshness.")
    public static final Holder<Item> SEASONED_FISH = REGISTRY.register("seasoned_fish", location -> new TooltipItem(new Properties(), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Golden Pufferfish")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Grants Regeneration. Gilded pufferfish dusted with crushed coral. The gold neutralizes the natural poison of the pufferfish, making it safer for consumption.")
    public static final Holder<Item> GOLDEN_CORAL_PUFFERFISH = REGISTRY.register("golden_coral_pufferfish", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), entity -> entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(5)))));

    @Translation(type = Translation.Type.ITEM, comments = "Frozen Fish")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Cleanses all effects and replenishes water. Food and water in one convenient package!")
    public static final Holder<Item> FROZEN_RAW_FISH = REGISTRY.register("frozen_raw_fish", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), REMOVE_EFFECTS_CURED_BY_MILK));

    @Translation(type = Translation.Type.ITEM, comments = "Golden Turtle Egg")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Removes Wither and Poison effects, and gives Absorption and Regeneration. A turtle egg stuffed with dragon dust and gold. After a while, the dust reacts with the eggshell, changing its color and texture.")
    public static final Holder<Item> GOLDEN_TURTLE_EGG = REGISTRY.register("golden_turtle_egg", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), entity -> {
        entity.removeEffect(MobEffects.POISON);
        entity.removeEffect(MobEffects.WITHER);
        entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Functions.minutesToTicks(5)));
        entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, Functions.secondsToTicks(10), 1));
    }));

    private static final Consumer<LivingEntity> EAT_TREAT_EFFECT = entity -> {
        if (entity instanceof Player player) {
            ManaHandler.replenishMana(player, ManaHandler.getMaxMana(player));
            player.addEffect(new MobEffectInstance(DSEffects.SOURCE_OF_MAGIC, Functions.minutesToTicks(1)));
        }
    };

    @Translation(type = Translation.Type.ITEM, comments = "Sea Dragon Treat")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Grants infinite mana temporarily.")
    public static final Holder<Item> SEA_DRAGON_TREAT = REGISTRY.register("sea_dragon_treat", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), EAT_TREAT_EFFECT));

    @Translation(type = Translation.Type.ITEM, comments = "Cave Dragon Treat")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Grants infinite mana temporarily.")
    public static final Holder<Item> CAVE_DRAGON_TREAT = REGISTRY.register("cave_dragon_treat", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), EAT_TREAT_EFFECT));

    @Translation(type = Translation.Type.ITEM, comments = "Forest Dragon Treat")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 Grants infinite mana temporarily.")
    public static final Holder<Item> FOREST_DRAGON_TREAT = REGISTRY.register("forest_dragon_treat", location -> new CustomOnFinishEffectItem(new Properties(), location.getPath(), EAT_TREAT_EFFECT));

    // --- Armor --- //

    @Translation(type = Translation.Type.ITEM, comments = "Light Upgrade")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A rare item that allows you to upgrade your Netherite Armor to Light Armor. The armor will have built-in enchantments to help you live peacefully in this world.")
    public static final Holder<Item> LIGHT_UPGRADE = REGISTRY.register("light_upgrade", location -> new TooltipItem(new Properties().rarity(Rarity.RARE), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Light Helmet")
    public static final Holder<Item> LIGHT_DRAGON_HELMET = REGISTRY.register("light_dragon_helmet", () -> new LightDragonArmorItem(
            ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(100)).rarity(Rarity.EPIC).fireResistant())
    );

    @Translation(type = Translation.Type.ITEM, comments = "Light Chestplate")
    public static final Holder<Item> LIGHT_DRAGON_CHESTPLATE = REGISTRY.register("light_dragon_chestplate", () -> new LightDragonArmorItem(
            ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(100)).rarity(Rarity.EPIC).fireResistant())
    );

    @Translation(type = Translation.Type.ITEM, comments = "Light Leggings")
    public static final Holder<Item> LIGHT_DRAGON_LEGGINGS = REGISTRY.register("light_dragon_leggings", () -> new LightDragonArmorItem(
            ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(100)).rarity(Rarity.EPIC).fireResistant())
    );

    @Translation(type = Translation.Type.ITEM, comments = "Light Boots")
    public static final Holder<Item> LIGHT_DRAGON_BOOTS = REGISTRY.register("light_dragon_boots", () -> new LightDragonArmorItem(
            ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(100)).rarity(Rarity.EPIC).fireResistant())
    );

    @Translation(type = Translation.Type.ITEM, comments = "Dark Upgrade")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A rare item that allows you to upgrade your Netherite Armor to Dark Armor. The armor will have built-in enchantments to help you effectively shed blood in the name of evil.")
    public static final Holder<Item> DARK_UPGRADE = REGISTRY.register("dark_upgrade", location -> new TooltipItem(new Properties().rarity(Rarity.RARE), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Dark Helmet")
    public static final Holder<Item> DARK_DRAGON_HELMET = REGISTRY.register("dark_dragon_helmet", () -> new DarkDragonArmorItem(
            ArmorItem.Type.HELMET, new Item.Properties().durability(ArmorItem.Type.HELMET.getDurability(100)).rarity(Rarity.EPIC).fireResistant())
    );

    @Translation(type = Translation.Type.ITEM, comments = "Dark Chestplate")
    public static final Holder<Item> DARK_DRAGON_CHESTPLATE = REGISTRY.register("dark_dragon_chestplate", () -> new DarkDragonArmorItem(
            ArmorItem.Type.CHESTPLATE, new Item.Properties().durability(ArmorItem.Type.CHESTPLATE.getDurability(100)).rarity(Rarity.EPIC).fireResistant())
    );

    @Translation(type = Translation.Type.ITEM, comments = "Dark Leggings")
    public static final Holder<Item> DARK_DRAGON_LEGGINGS = REGISTRY.register("dark_dragon_leggings", () -> new DarkDragonArmorItem(
            ArmorItem.Type.LEGGINGS, new Item.Properties().durability(ArmorItem.Type.LEGGINGS.getDurability(100)).rarity(Rarity.EPIC).fireResistant())
    );

    @Translation(type = Translation.Type.ITEM, comments = "Dark Boots")
    public static final Holder<Item> DARK_DRAGON_BOOTS = REGISTRY.register("dark_dragon_boots", () -> new DarkDragonArmorItem(
            ArmorItem.Type.BOOTS, new Item.Properties().durability(ArmorItem.Type.BOOTS.getDurability(100)).rarity(Rarity.EPIC).fireResistant())
    );

    // --- Weapons --- //

    @Translation(type = Translation.Type.ITEM, comments = "Sword That Bonks Dragons")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 The sword of the dragon hunters. Slow, but strong. Can be found in the hunters treasury.")
    public static final Holder<Item> DRAGON_HUNTER_SWORD = REGISTRY.register("dragon_hunter_sword", location -> new DragonHunterWeapon(
            DSEquipment.DRAGON_HUNTER,
            new Item.Properties().rarity(Rarity.EPIC).fireResistant().attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4, -2.8F)),
            location.getPath(),
            List.of(Pair.of(DSEnchantments.DRAGONSBANE, 3))
    ));

    @Translation(type = Translation.Type.ITEM, comments = "Iron Partisan")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A long shafted weapon designed to take out enemies at a distance. Especially good against flying dragons.")
    public static final Holder<Item> PARTISAN = REGISTRY.register("hunter_partisan", location -> new DragonHunterWeapon(
            Tiers.IRON, new Item.Properties().component(
            DataComponents.ATTRIBUTE_MODIFIERS,
            ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 6, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -1.4f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(DragonSurvival.res("partisan_block_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(DragonSurvival.res("partisan_attack_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build()
    ), location.getPath(), List.of()));

    @Translation(type = Translation.Type.ITEM, comments = "Diamond Partisan")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A long shafted weapon designed to take out enemies at a distance. Especially good against flying dragons.")
    public static final Holder<Item> HUNTER_PARTISAN_DIAMOND = REGISTRY.register("hunter_partisan_diamond", location -> new DragonHunterWeapon(
            Tiers.DIAMOND, new Item.Properties().component(
            DataComponents.ATTRIBUTE_MODIFIERS,
            ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 7, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -1.4f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(DragonSurvival.res("partisan_block_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(DragonSurvival.res("partisan_attack_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build()
    ), location.getPath(), List.of(Pair.of(DSEnchantments.DRAGONSBANE, 3))));

    @Translation(type = Translation.Type.ITEM, comments = "Netherite Partisan")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 A long shafted weapon designed to take out enemies at a distance. Especially good against flying dragons.")
    public static final Holder<Item> HUNTER_PARTISAN_NETHERITE = REGISTRY.register("hunter_partisan_netherite", location -> new DragonHunterWeapon(
            Tiers.NETHERITE, new Item.Properties().component(
            DataComponents.ATTRIBUTE_MODIFIERS,
            ItemAttributeModifiers.builder()
                    .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 8, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -1.4f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(DragonSurvival.res("partisan_block_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(DragonSurvival.res("partisan_attack_reach"), 1f, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                    .build()
    ), location.getPath(), List.of(Pair.of(DSEnchantments.DRAGONSBANE, 3))));

    // --- Block items --- //

    public static final Holder<Item> CAVE_SOURCE_OF_MAGIC = REGISTRY.register("cave_source_of_magic", () -> new SourceOfMagicItem(
            DSBlocks.CAVE_SOURCE_OF_MAGIC.get(),
            new Item.Properties().rarity(Rarity.EPIC)
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
    ));

    public static final Holder<Item> FOREST_SOURCE_OF_MAGIC = REGISTRY.register("forest_source_of_magic", () -> new SourceOfMagicItem(
            DSBlocks.FOREST_SOURCE_OF_MAGIC.get(),
            new Item.Properties().rarity(Rarity.EPIC)
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
    ));

    public static final Holder<Item> SEA_SOURCE_OF_MAGIC = REGISTRY.register("sea_source_of_magic", () -> new SourceOfMagicItem(
            DSBlocks.SEA_SOURCE_OF_MAGIC.get(),
            new Item.Properties().rarity(Rarity.EPIC)
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
    ));

    // --- Misc --- //

    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 By combining the basic elements of dragons, you have the item to activate the beacon. Depending what species of dragon you are, the beacon will have different effects.")
    @Translation(type = Translation.Type.ITEM, comments = "Beacon Activator")
    public static final Holder<Item> BEACON_ACTIVATOR = REGISTRY.register("beacon_activator", location -> new TooltipItem(new Item.Properties(), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Dragon Soul")
    public static final Holder<Item> DRAGON_SOUL = REGISTRY.register("dragon_soul", () -> new DragonSoulItem(new Properties().rarity(Rarity.EPIC)));

    public static final String LIGHT_KEY_ID = "light_key";

    @Translation(type = Translation.Type.ITEM, comments = "Light Key")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 An enchanted key that unlocks the Light Vault. Purchased from the dragon rider villager.")
    public static final Holder<Item> LIGHT_KEY = REGISTRY.register(LIGHT_KEY_ID, () -> new LightKeyItem(
            new Item.Properties().rarity(Rarity.UNCOMMON).component(DSDataComponents.TARGET_POSITION, new Vector3f()),
            DragonSurvival.res("geo/" + LIGHT_KEY_ID + ".geo.json"),
            DragonSurvival.res("textures/item/" + LIGHT_KEY_ID + ".png"),
            DragonSurvival.res("light_treasure")));

    public static final String DARK_KEY_ID = "dark_key";

    @Translation(type = Translation.Type.ITEM, comments = "Dark Key")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 An enchanted key that unlocks the Dark Vault in Nether. Drops if you kill a dragon hunter knight.")
    public static final Holder<Item> DARK_KEY = REGISTRY.register(DARK_KEY_ID, () -> new DarkKeyItem(
            new Item.Properties().rarity(Rarity.UNCOMMON).component(DSDataComponents.TARGET_POSITION, new Vector3f()),
            DragonSurvival.res("geo/" + DARK_KEY_ID + ".geo.json"),
            DragonSurvival.res("textures/item/" + DARK_KEY_ID + ".png"),
            DragonSurvival.res("dark_treasure")));

    public static final String HUNTER_KEY_ID = "hunter_key";

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Key")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 An enchanted key that unlocks a treasure vault for dragon hunters. Can be purchased from the Hunter Leader in the Hunter Castle.")
    public static final Holder<Item> HUNTER_KEY = REGISTRY.register(HUNTER_KEY_ID, () -> new HunterKeyItem(
            new Item.Properties().rarity(Rarity.UNCOMMON).component(DSDataComponents.TARGET_POSITION, new Vector3f()),
            DragonSurvival.res("geo/" + HUNTER_KEY_ID + ".geo.json"),
            DragonSurvival.res("textures/item/" + HUNTER_KEY_ID + ".png"),
            DragonSurvival.res("hunter_treasure")));

    @Translation(type = Translation.Type.ITEM, comments = "Spearman Promotion")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■ §7Use on a Hunter Spearman to turn him into a Hunter Leader.")
    public static final Holder<Item> SPEARMAN_PROMOTION = REGISTRY.register("spearman_promotion", location -> new TooltipItem(new Properties().rarity(Rarity.COMMON), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Flight Grant")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 This item gives you the ability to fly. Consumed on use.")
    public static final Holder<Item> FLIGHT_GRANT_ITEM = REGISTRY.register("wing_grant", location -> new FlightGrantItem(new Properties(), location.getPath()));

    @Translation(type = Translation.Type.ITEM, comments = "Spin Grant")
    @Translation(type = Translation.Type.DESCRIPTION, comments = "■§7 This item gives you dragon the ability to spin while flying. Consumed on use.")
    public static final Holder<Item> SPIN_GRANT_ITEM = REGISTRY.register("spin_grant", location -> new SpinGrantItem(new Properties(), location.getPath()));

    // --- Spawn eggs --- //

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Hound Spawn Egg")
    public static final Holder<Item> HOUND_SPAWN_EGG = REGISTRY.register("hound_spawn_egg", () -> new DeferredSpawnEggItem(DSEntities.HUNTER_HOUND, 0xA66A2C, 0xD5AA72, new Properties().rarity(Rarity.COMMON)));

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Spearman Spawn Egg")
    public static final Holder<Item> SPEARMAN_SPAWN_EGG = REGISTRY.register("spearman_spawn_egg", () -> new DeferredSpawnEggItem(DSEntities.HUNTER_SPEARMAN, 0xE6E3E1, 0xD1C8B8, new Properties().rarity(Rarity.COMMON)));

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Knight Spawn Egg")
    public static final Holder<Item> KNIGHT_SPAWN_EGG = REGISTRY.register("knight_spawn_egg", () -> new DeferredSpawnEggItem(DSEntities.HUNTER_KNIGHT, 0x615B62, 0xCCBCAD, new Properties().rarity(Rarity.COMMON)));

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Ambusher Spawn Egg")
    public static final Holder<Item> AMBUSHER_SPAWN_EGG = REGISTRY.register("ambusher_spawn_egg", () -> new DeferredSpawnEggItem(DSEntities.HUNTER_AMBUSHER, 0x756C63, 0x423930, new Properties().rarity(Rarity.COMMON)));

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Griffin Spawn Egg")
    public static final Holder<Item> GRIFFIN_SPAWN_EGG = REGISTRY.register("griffin_spawn_egg", () -> new DeferredSpawnEggItem(DSEntities.HUNTER_GRIFFIN, 0xE9D5CC, 0x71260A, new Properties().rarity(Rarity.COMMON)));

    @Translation(type = Translation.Type.ITEM, comments = "Hunter Leader Spawn Egg")
    public static final Holder<Item> LEADER_SPAWN_EGG = REGISTRY.register("leader_spawn_egg", () -> new DeferredSpawnEggItem(DSEntities.HUNTER_LEADER, 0x202020, 0xb3814e, new Properties().rarity(Rarity.COMMON)));

    // --- Not shown in creative tab --- //

    public static final Holder<Item> BOLAS = REGISTRY.register("bolas", () -> new BolasArrowItem(new Item.Properties()));
    public static final Holder<Item> HUNTING_NET = REGISTRY.register("dragon_hunting_mesh", () -> new Item(new Item.Properties()));
    public static final Holder<Item> LIGHTNING_TEXTURE_ITEM = REGISTRY.register("lightning", () -> new Item(new Item.Properties()));
    public static final Holder<Item> FOREST_ICON = REGISTRY.register("forest_icon", () -> new Item(new Item.Properties()));
    public static final Holder<Item> CAVE_ICON = REGISTRY.register("cave_icon", () -> new Item(new Item.Properties()));
    public static final Holder<Item> SEA_ICON = REGISTRY.register("sea_icon", () -> new Item(new Item.Properties()));
    public static final Holder<Item> FOREST_FULL_ICON = REGISTRY.register("forest_full_icon", () -> new Item(new Item.Properties()));
    public static final Holder<Item> CAVE_FULL_ICON = REGISTRY.register("cave_full_icon", () -> new Item(new Item.Properties()));
    public static final Holder<Item> SEA_FULL_ICON = REGISTRY.register("sea_full_icon", () -> new Item(new Item.Properties()));

    public static final Holder<Item> ACTIVATED_DRAGON_BEACON = REGISTRY.register("activated_dragon_beacon", () -> new Item(new Item.Properties()));
    public static final Holder<Item> CAVE_BEACON = REGISTRY.register("cave_beacon", () -> new Item(new Item.Properties()));
    public static final Holder<Item> FOREST_BEACON = REGISTRY.register("forest_beacon", () -> new Item(new Item.Properties()));
    public static final Holder<Item> SEA_BEACON = REGISTRY.register("sea_beacon", () -> new Item(new Item.Properties()));

}