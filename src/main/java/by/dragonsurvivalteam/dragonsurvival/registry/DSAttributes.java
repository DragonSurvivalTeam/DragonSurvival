package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.TimeAttribute;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class DSAttributes {
    public static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(Registries.ATTRIBUTE, DragonSurvival.MODID);

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Flight Stamina")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Reduces the food exhaustion of flying")
    public static final Holder<Attribute> FLIGHT_STAMINA_COST = REGISTRY.register("flight_stamina", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("flight_stamina"), 1, 0, 5).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Lava Swim Speed")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "A multiplier to the lava swim speed")
    // TODO :: enable 'can swim in fluid' for lava when this value is above 0 (or some other threshold)?
    public static final Holder<Attribute> LAVA_SWIM_SPEED = REGISTRY.register("lava_swim_speed", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("lava_swim_speed"), 1, 0, 1024).setSyncable(true));

    // TODO :: use Attributes#FLYING_SPEED instead? Currently it seems to be only used for mobs
    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Flight Speed")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "A multiplier to the flight speed")
    public static final Holder<Attribute> FLIGHT_SPEED = REGISTRY.register("flight_speed", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("flight_speed"), 1, 0, 1024).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Mana")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Amount of mana for abilities")
    public static final Holder<Attribute> MANA = REGISTRY.register("mana", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("mana"), 1, 0, 1024).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Mana Regeneration")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Amount of mana regenerated per tick")
    public static final Holder<Attribute> MANA_REGENERATION = REGISTRY.register("mana_regeneration", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("mana_regeneration"), 0.004, 0, 1024).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Experience")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "A multiplier to the dropped experience")
    public static final Holder<Attribute> EXPERIENCE = REGISTRY.register("experience", () -> new PercentageAttribute(Translation.Type.ATTRIBUTE.wrap("experience"), 1, 0, 1024).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Breath Range")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Determines the range of the breath ability (the range acts in terms of blocks)")
    public static final Holder<Attribute> DRAGON_BREATH_RANGE = REGISTRY.register("dragon_breath_range", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("dragon_breath_range"), 3, 0, 1024).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Block Break Radius")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Determines the radius that you can break blocks when mining")
    public static final Holder<Attribute> BLOCK_BREAK_RADIUS = REGISTRY.register("block_break_radius", () -> new RangedAttribute(Translation.Type.ATTRIBUTE.wrap("block_break_radius"), 0, 0, 16).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Penalty Resistance Time")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "Increases the time before the penalty effect is applied")
    public static final Holder<Attribute> PENALTY_RESISTANCE_TIME = REGISTRY.register("penalty_resistance_time", () -> new TimeAttribute(Translation.Type.ATTRIBUTE.wrap("penalty_resistance_time"), Functions.secondsToTicks(10), 0, 16384).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Armor Ignore Chance")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "The chance to ignore armor when attacking")
    public static final Holder<Attribute> ARMOR_IGNORE_CHANCE = REGISTRY.register("armor_ignore_chance", () -> new PercentageAttribute(Translation.Type.ATTRIBUTE.wrap("armor_ignore_chance"), 0, 0, 1).setSyncable(true));

    @Translation(type = Translation.Type.ATTRIBUTE, comments = "Hunter Faction Damage")
    @Translation(type = Translation.Type.ATTRIBUTE_DESCRIPTION, comments = "A multiplier to the damage the hunter faction takes from your attacks")
    public static final Holder<Attribute> HUNTER_FACTION_DAMAGE = REGISTRY.register("hunter_faction_damage", () -> new PercentageAttribute(Translation.Type.ATTRIBUTE.wrap("hunter_faction_damage"), 1, 0, 10).setSyncable(true));

    @SubscribeEvent
    public static void attachAttributes(final EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, FLIGHT_STAMINA_COST);
        event.add(EntityType.PLAYER, FLIGHT_SPEED);
        event.add(EntityType.PLAYER, MANA);
        event.add(EntityType.PLAYER, MANA_REGENERATION);
        event.add(EntityType.PLAYER, EXPERIENCE);
        event.add(EntityType.PLAYER, DRAGON_BREATH_RANGE);
        event.add(EntityType.PLAYER, BLOCK_BREAK_RADIUS);
        event.add(EntityType.PLAYER, PENALTY_RESISTANCE_TIME);
        event.add(EntityType.PLAYER, ARMOR_IGNORE_CHANCE);
        event.add(EntityType.PLAYER, HUNTER_FACTION_DAMAGE);

        event.getTypes().forEach(type -> event.add(type, LAVA_SWIM_SPEED));
    }
}
