package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
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
            "■ Inhabitants of all biomes, and the most common type of dragon. They are the most balanced type of dragon, having no particular strengths or weaknesses.",
            "§7■ You may change your body type at any time, but you will lose your growth progress."
    })
    @Translation(type = Translation.Type.BODY, comments = "Center")
    public static ResourceKey<DragonBody> center = key("center");

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Eastern Type§r",
            "■ Adapted to life in caves, they lack large wings, reducing the effectiveness of their levitation magic, but they are still excellent swimmers. They have a larger supply of mana, and natural armor.",
            "§7■ You may change your body type at any time, but you will lose your growth progress."
    })
    @Translation(type = Translation.Type.BODY, comments = "East")
    public static ResourceKey<DragonBody> east = key("east");

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Northern Type§r",
            "■ Perfect travelers, conquering water, lava and air. They are slower on the ground and weaker physically, but are magically adept and excel at swimming. Their flat bodies allow them to go places other dragons cannot.",
            "§7■ You may change your body type at any time, but you will lose your growth progress. Each type has their own strengths and weaknesses, but the change is mostly cosmetic."
    })
    @Translation(type = Translation.Type.BODY, comments = "North")
    public static ResourceKey<DragonBody> north = key("north");

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Southern Type§r",
            "■ They are adapted to life on the plains, capable of running swiftly, and leaping high into the air. The special structure of their paws gives them many advantages on the ground, and they are physically strong, but they will struggle at flight and swimming.",
            "§7■ You may change your body type at any time, but you will lose your growth progress. Each type has their own strengths and weaknesses, but the change is mostly cosmetic."
    })
    @Translation(type = Translation.Type.BODY, comments = "South")
    public static ResourceKey<DragonBody> south = key("south");

    @Translation(type = Translation.Type.BODY_DESCRIPTION, comments = {
            "§6■ Western Type§r",
            "■ Conquerors of mountain and sky, they are unrivalled in their element, but are rather clumsy on the ground.",
            "§7■ You may change your body type at any time, but you will lose your growth progress."
    })
    @Translation(type = Translation.Type.BODY, comments = "West")
    public static ResourceKey<DragonBody> west = key("west");

    public static void registerBodies(final BootstrapContext<DragonBody> context) {
        context.register(center, new DragonBody(List.of(
                Modifier.constant(DSAttributes.FLIGHT_SPEED, 0.2f, AttributeModifier.Operation.ADD_VALUE)
        ), 1, false, false, true));

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
        ), 1, false, true, true));

        context.register(north, new DragonBody(List.of(
                Modifier.constant(Attributes.ATTACK_DAMAGE, -0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                Modifier.constant(Attributes.ATTACK_KNOCKBACK, -0.5f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.MOVEMENT_SPEED, -0.3f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                Modifier.constant(NeoForgeMod.SWIM_SPEED, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.MANA, 2, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.FLIGHT_STAMINA_COST, -0.1f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        ), 0.55, true, true, true));

        context.register(south, new DragonBody(List.of(
                Modifier.constant(Attributes.ATTACK_DAMAGE, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.GRAVITY, 0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                Modifier.constant(Attributes.JUMP_STRENGTH, 0.2f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.MOVEMENT_SPEED, 0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                Modifier.constant(NeoForgeMod.SWIM_SPEED, -0.2f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.FLIGHT_SPEED, -0.2f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.FLIGHT_STAMINA_COST, -0.5f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        ), 1, false, true, true));

        context.register(west, new DragonBody(List.of(
                Modifier.constant(Attributes.ATTACK_KNOCKBACK, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(Attributes.MOVEMENT_SPEED, -0.3f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                Modifier.constant(Attributes.STEP_HEIGHT, 1, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(NeoForgeMod.SWIM_SPEED, -0.3f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.FLIGHT_SPEED, 0.2f, AttributeModifier.Operation.ADD_VALUE),
                Modifier.constant(DSAttributes.FLIGHT_STAMINA_COST, 1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
        ), 1, false, false, true));
    }

    public static ResourceKey<DragonBody> key(final ResourceLocation location) {
        return ResourceKey.create(DragonBody.REGISTRY, location);
    }

    private static ResourceKey<DragonBody> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
