package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmoteSet;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmoteSets;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.List;


public class DragonBodies {
    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Central Type§r",
            "■ Inhabitants of all biomes, and the most common type of dragon. They are the most balanced type of dragon, having no particular strengths or weaknesses."
    })
    @Translation(type = Translation.Type.BODY, comments = "Center")
    @Translation(type = Translation.Type.BODY_WINGS, comments = "Show wings")
    @Translation(type = Translation.Type.BODY_WINGS_DESCRIPTION, comments = "Show wings")
    public static final ResourceKey<DragonBody> center = key("center");

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Eastern Type§r",
            "■ Adapted to life in caves, they lack large wings, reducing the effectiveness of their levitation magic, but they are still excellent swimmers. They have a larger supply of mana, and natural armor."
    })
    @Translation(type = Translation.Type.BODY, comments = "East")
    @Translation(type = Translation.Type.BODY_WINGS, comments = "Show wings")
    @Translation(type = Translation.Type.BODY_WINGS_DESCRIPTION, comments = "Show wings")
    public static final ResourceKey<DragonBody> east = key("east");

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Northern Type§r",
            "■ Perfect travelers, conquering water, lava and air. They are slower on the ground and weaker physically, but are magically adept and excel at swimming. Their flat bodies allow them to go places other dragons cannot."
    })
    @Translation(type = Translation.Type.BODY, comments = "North")
    @Translation(type = Translation.Type.BODY_WINGS, comments = "Show wings")
    @Translation(type = Translation.Type.BODY_WINGS_DESCRIPTION, comments = "Show wings")
    public static final ResourceKey<DragonBody> north = key("north");

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Southern Type§r",
            "■ They are adapted to life on the plains, capable of running swiftly, and leaping high into the air. The special structure of their paws gives them many advantages on the ground, and they are physically strong, but they will struggle at flight and swimming."
    })
    @Translation(type = Translation.Type.BODY, comments = "South")
    @Translation(type = Translation.Type.BODY_WINGS, comments = "Show wings")
    @Translation(type = Translation.Type.BODY_WINGS_DESCRIPTION, comments = "Show wings")
    public static final ResourceKey<DragonBody> south = key("south");

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Western Type§r",
            "■ Conquerors of mountain and sky, they are unrivalled in their element, but are rather clumsy on the ground."
    })
    @Translation(type = Translation.Type.BODY, comments = "West")
    @Translation(type = Translation.Type.BODY_WINGS, comments = "Show wings")
    @Translation(type = Translation.Type.BODY_WINGS_DESCRIPTION, comments = "Show wings")
    public static final ResourceKey<DragonBody> west = key("west");

    public static void registerBodies(final BootstrapContext<DragonBody> context) {
        context.register(center, new DragonBody(List.of(
                Modifier.constant(DSAttributes.FLIGHT_SPEED, 0.2f, AttributeModifier.Operation.ADD_VALUE)
        ), 1, false, false, DragonBody.DEFAULT_MODEL, List.of("WingLeft", "WingRight", "SmallWingLeft", "SmallWingRight"), context.lookup(DragonEmoteSet.REGISTRY).getOrThrow(DragonEmoteSets.DEFAULT_EMOTES)));

        context.register(east, new DragonBody(List.of(
                Modifier.constant(Attributes.ARMOR, 2, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.ATTACK_DAMAGE, -1, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.ATTACK_KNOCKBACK, -1, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.GRAVITY, 0.1f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                Modifier.constant(Attributes.JUMP_STRENGTH, 0.1f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.MOVEMENT_SPEED, 0.1f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                Modifier.constant(NeoForgeMod.SWIM_SPEED, 1, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.MANA, 2, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.FLIGHT_SPEED, 0.2f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.FLIGHT_STAMINA_COST, -0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        ), 1, false, true, DragonBody.DEFAULT_MODEL, List.of("WingLeft", "WingRight", "SmallWingLeft", "SmallWingRight"), context.lookup(DragonEmoteSet.REGISTRY).getOrThrow(DragonEmoteSets.DEFAULT_EMOTES)));

        context.register(north, new DragonBody(List.of(
                Modifier.constant(Attributes.ATTACK_DAMAGE, -0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                Modifier.constant(Attributes.ATTACK_KNOCKBACK, -0.5f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.MOVEMENT_SPEED, -0.3f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                Modifier.constant(NeoForgeMod.SWIM_SPEED, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.MANA, 2, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.FLIGHT_STAMINA_COST, -0.1f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        ), 0.55, true, true, DragonBody.DEFAULT_MODEL, List.of("WingLeft", "WingRight", "SmallWingLeft", "SmallWingRight"), context.lookup(DragonEmoteSet.REGISTRY).getOrThrow(DragonEmoteSets.DEFAULT_EMOTES)));

        context.register(south, new DragonBody(List.of(
                Modifier.constant(Attributes.ATTACK_DAMAGE, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.GRAVITY, 0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                Modifier.constant(Attributes.JUMP_STRENGTH, 0.2f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.MOVEMENT_SPEED, 0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                Modifier.constant(NeoForgeMod.SWIM_SPEED, -0.2f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.FLIGHT_SPEED, -0.2f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.FLIGHT_STAMINA_COST, -0.5f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        ), 1, false, true, DragonBody.DEFAULT_MODEL, List.of("WingLeft", "WingRight", "SmallWingLeft", "SmallWingRight"), context.lookup(DragonEmoteSet.REGISTRY).getOrThrow(DragonEmoteSets.DEFAULT_EMOTES)));

        context.register(west, new DragonBody(List.of(
                Modifier.constant(Attributes.ATTACK_KNOCKBACK, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.KNOCKBACK_RESISTANCE, 0.15f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.MOVEMENT_SPEED, -0.15f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                Modifier.constant(NeoForgeMod.SWIM_SPEED, -0.2f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.FLIGHT_SPEED, 0.2f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.FLIGHT_STAMINA_COST, 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        ), 1, false, false, DragonBody.DEFAULT_MODEL, List.of("WingLeft", "WingRight", "SmallWingLeft", "SmallWingRight"), context.lookup(DragonEmoteSet.REGISTRY).getOrThrow(DragonEmoteSets.DEFAULT_EMOTES)));
    }

    public static ResourceKey<DragonBody> key(final ResourceLocation location) {
        return ResourceKey.create(DragonBody.REGISTRY, location);
    }

    private static ResourceKey<DragonBody> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
