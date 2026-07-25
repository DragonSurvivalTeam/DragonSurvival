package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.PercentageAttribute;
import by.dragonsurvivalteam.dragonsurvival.common.TimeAttribute;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(modid = DragonSurvival.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DSAttributes {
    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(Registries.ATTRIBUTE, DragonSurvival.MODID);

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Flight Stamina")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Reduces the food exhaustion of flying")
    public static final RegistryObject<Attribute> FLIGHT_STAMINA_COST = REGISTRY.register("flight_stamina", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("flight_stamina"), 1, 0, 5).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Lava Swim Speed")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "A multiplier to the lava swim speed")
    // TODO :: enable 'can swim in fluid' for lava when this value is above 0 (or some other threshold)?
    public static final RegistryObject<Attribute> LAVA_SWIM_SPEED = REGISTRY.register("lava_swim_speed", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("lava_swim_speed"), 1, 0, 1024).setSyncable(true));

    // TODO :: use Attributes#FLYING_SPEED instead? Currently it seems to be only used for mobs
    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Flight Speed")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "A multiplier to the flight speed")
    public static final RegistryObject<Attribute> FLIGHT_SPEED = REGISTRY.register("flight_speed", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("flight_speed"), 1, 0, 1024).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Mana")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Amount of mana for abilities")
    public static final RegistryObject<Attribute> MANA = REGISTRY.register("mana", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("mana"), 1, 0, 1024).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Mana Regeneration")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Amount of mana regenerated per tick")
    public static final RegistryObject<Attribute> MANA_REGENERATION = REGISTRY.register("mana_regeneration", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("mana_regeneration"), 0.004, 0, 1024).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Experience")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "A multiplier to the dropped experience")
    public static final RegistryObject<Attribute> EXPERIENCE = REGISTRY.register("experience", () -> new PercentageAttribute(Translation.Type.ATTRIBUTE.wrap("experience"), 1, 0, 1024).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Breath Range")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Determines the range of the breath ability (the range acts in terms of blocks)")
    public static final RegistryObject<Attribute> DRAGON_BREATH_RANGE = REGISTRY.register("dragon_breath_range", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("dragon_breath_range"), 3, 0, 1024).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Block Break Radius")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Determines the radius that you can break blocks when mining")
    public static final RegistryObject<Attribute> BLOCK_BREAK_RADIUS = REGISTRY.register("block_break_radius", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("block_break_radius"), 0, 0, 16).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Penalty Resistance Time")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Increases the time before the penalty effect is applied")
    public static final RegistryObject<Attribute> PENALTY_RESISTANCE_TIME = REGISTRY.register("penalty_resistance_time", () -> new TimeAttribute(Translation.Type.ATTRIBUTE.wrap("penalty_resistance_time"), Functions.secondsToTicks(10), 0, 16384).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Armor Ignore Chance")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "The chance to ignore armor when attacking")
    public static final RegistryObject<Attribute> ARMOR_IGNORE_CHANCE = REGISTRY.register("armor_ignore_chance", () -> new PercentageAttribute(Translation.Type.ATTRIBUTE.wrap("armor_ignore_chance"), 0, 0, 1).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Hunter Faction Damage")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "A multiplier to the damage the hunter faction takes from your attacks")
    public static final RegistryObject<Attribute> HUNTER_FACTION_DAMAGE = REGISTRY.register("hunter_faction_damage", () -> new PercentageAttribute(Translation.Type.ATTRIBUTE.wrap("hunter_faction_damage"), 1, 0, 10).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Dragon Ability Damage")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "A multiplier to the damage of dragon abilities")
    public static final RegistryObject<Attribute> DRAGON_ABILITY_DAMAGE = REGISTRY.register("dragon_ability_damage", () -> new PercentageAttribute(Translation.Type.ATTRIBUTE.wrap("dragon_ability_damage"), 1, 0, 10).setSyncable(true));

    /**
     * Backport of the vanilla 1.21 scale attribute. Dragon dimensions are applied by
     * {@code DragonSizeHandler}; this attribute retains the 1.21 data-driven growth model.
     */
    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Scale")
    public static final RegistryObject<Attribute> SCALE = REGISTRY.register("scale", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("scale"), 1, 0.0625, 16).setSyncable(true));

    /** Backport of the vanilla 1.21 safe fall distance attribute. */
    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Safe Fall Distance")
    public static final RegistryObject<Attribute> SAFE_FALL_DISTANCE = REGISTRY.register("safe_fall_distance", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("safe_fall_distance"), 3, -1024, 1024).setSyncable(true));

    /** Backport of the vanilla 1.21 jump strength attribute. */
    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Jump Strength")
    public static final RegistryObject<Attribute> JUMP_STRENGTH = REGISTRY.register("jump_strength", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("jump_strength"), 0.42, 0, 32).setSyncable(true));

    /** Backport of the vanilla 1.21 submerged mining speed attribute. */
    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Submerged Mining Speed")
    public static final RegistryObject<Attribute> SUBMERGED_MINING_SPEED = REGISTRY.register("submerged_mining_speed", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("submerged_mining_speed"), 0.2, 0, 20).setSyncable(true));

    @SubscribeEvent
    public static void attachAttributes(final EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, FLIGHT_STAMINA_COST.get());
        event.add(EntityType.PLAYER, FLIGHT_SPEED.get());
        event.add(EntityType.PLAYER, MANA.get());
        event.add(EntityType.PLAYER, MANA_REGENERATION.get());
        event.add(EntityType.PLAYER, EXPERIENCE.get());
        event.add(EntityType.PLAYER, DRAGON_BREATH_RANGE.get());
        event.add(EntityType.PLAYER, BLOCK_BREAK_RADIUS.get());
        event.add(EntityType.PLAYER, PENALTY_RESISTANCE_TIME.get());
        event.add(EntityType.PLAYER, ARMOR_IGNORE_CHANCE.get());
        event.add(EntityType.PLAYER, HUNTER_FACTION_DAMAGE.get());
        event.add(EntityType.PLAYER, DRAGON_ABILITY_DAMAGE.get());
        event.add(EntityType.PLAYER, SAFE_FALL_DISTANCE.get());
        event.add(EntityType.PLAYER, SUBMERGED_MINING_SPEED.get());

        event.getTypes().forEach(type -> {
            event.add(type, LAVA_SWIM_SPEED.get());
            event.add(type, SCALE.get());
            event.add(type, JUMP_STRENGTH.get());
        });
    }
}
