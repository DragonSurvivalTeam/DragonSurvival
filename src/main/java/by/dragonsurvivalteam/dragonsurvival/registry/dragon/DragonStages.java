package by.dragonsurvivalteam.dragonsurvival.registry.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;
import java.util.Optional;

import static by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType.DRAGON_STAGE;

public class DragonStages {
    @Translation(type = Translation.Type.STAGE, comments = "Newborn")
    public static ResourceKey<DragonStage> newborn = key("newborn");

    @Translation(type = Translation.Type.STAGE, comments = "Young")
    public static ResourceKey<DragonStage> young = key("young");

    @Translation(type = Translation.Type.STAGE, comments = "Adult")
    public static ResourceKey<DragonStage> adult = key("adult");

    public static void registerLevels(final BootstrapContext<DragonStage> context) {
        context.register(newborn, new DragonStage(
                new MiscCodecs.Bounds(10, 25),
                Functions.hoursToTicks(10),
                Optional.of(young),
                List.of(
                        /* Constant */
                        Modifier.constant(DRAGON_STAGE, Attributes.SUBMERGED_MINING_SPEED, 1, AttributeModifier.Operation.ADD_VALUE, DragonTypes.SEA.getTypeNameLowerCase()),
                        Modifier.constant(DRAGON_STAGE, Attributes.MAX_HEALTH, -7, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DRAGON_STAGE, Attributes.ATTACK_DAMAGE, 1, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DRAGON_STAGE, Attributes.JUMP_STRENGTH, 0.025f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DRAGON_STAGE, Attributes.SAFE_FALL_DISTANCE, 0.25f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DRAGON_STAGE, DSAttributes.DRAGON_BREATH_RANGE, 1.5f, AttributeModifier.Operation.ADD_VALUE),
                        /* Per size */
                        Modifier.perSize(DRAGON_STAGE, Attributes.MOVEMENT_SPEED, 0.0015f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perSize(DRAGON_STAGE, Attributes.MAX_HEALTH, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perSize(DRAGON_STAGE, DSAttributes.DRAGON_BREATH_RANGE, 0.05f, AttributeModifier.Operation.ADD_VALUE)
                ),
                0,
                1.5,
                List.of(
                        MiscCodecs.GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.value()),
                        MiscCodecs.GrowthItem.create(Functions.minutesToTicks(30), DSItems.WEAK_DRAGON_HEART.value()),
                        MiscCodecs.GrowthItem.create(Functions.minutesToTicks(10), DSItems.DRAGON_HEART_SHARD.value()),
                        MiscCodecs.GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.value())
                ),
                Optional.of(Condition.defaultNaturalGrowthBlocker()),
                Optional.empty()
        ));

        context.register(young, new DragonStage(
                new MiscCodecs.Bounds(25, 40),
                Functions.daysToTicks(3),
                Optional.of(adult),
                List.of(
                        /* Constant */
                        Modifier.constant(DRAGON_STAGE, Attributes.SUBMERGED_MINING_SPEED, 2, AttributeModifier.Operation.ADD_VALUE, DragonTypes.SEA.getTypeNameLowerCase()),
                        Modifier.constant(DRAGON_STAGE, Attributes.STEP_HEIGHT, 0.25f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DRAGON_STAGE, Attributes.ATTACK_DAMAGE, 2, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DRAGON_STAGE, Attributes.JUMP_STRENGTH, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DRAGON_STAGE, Attributes.SAFE_FALL_DISTANCE, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DRAGON_STAGE, DSAttributes.DRAGON_BREATH_RANGE, 2.5f, AttributeModifier.Operation.ADD_VALUE),
                        /* Per size */
                        Modifier.perSize(DRAGON_STAGE, Attributes.MOVEMENT_SPEED, 0.0015f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perSize(DRAGON_STAGE, Attributes.ENTITY_INTERACTION_RANGE, 0.01f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perSize(DRAGON_STAGE, Attributes.BLOCK_INTERACTION_RANGE, 0.01f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perSize(DRAGON_STAGE, Attributes.MAX_HEALTH, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perSize(DRAGON_STAGE, DSAttributes.DRAGON_BREATH_RANGE, 0.05f, AttributeModifier.Operation.ADD_VALUE)
                ),
                1,
                2,
                List.of(
                        MiscCodecs.GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.value()),
                        MiscCodecs.GrowthItem.create(Functions.minutesToTicks(30), DSItems.WEAK_DRAGON_HEART.value()),
                        MiscCodecs.GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.value())
                ),
                Optional.of(Condition.defaultNaturalGrowthBlocker()),
                Optional.empty()
        ));

        context.register(adult, new DragonStage(
                new MiscCodecs.Bounds(40, 60),
                Functions.daysToTicks(20),
                Optional.empty(),
                List.of(
                        /* Constant */
                        Modifier.constant(DRAGON_STAGE, Attributes.SUBMERGED_MINING_SPEED, 3, AttributeModifier.Operation.ADD_VALUE, DragonTypes.SEA.getTypeNameLowerCase()),
                        Modifier.constant(DRAGON_STAGE, Attributes.STEP_HEIGHT, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DRAGON_STAGE, Attributes.ATTACK_DAMAGE, 3, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DRAGON_STAGE, Attributes.JUMP_STRENGTH, 0.1f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DRAGON_STAGE, Attributes.SAFE_FALL_DISTANCE, 1, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DRAGON_STAGE, DSAttributes.DRAGON_BREATH_RANGE, 4, AttributeModifier.Operation.ADD_VALUE),
                        /* Per size */
                        Modifier.perSize(DRAGON_STAGE, Attributes.MOVEMENT_SPEED, 0.0015f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perSize(DRAGON_STAGE, Attributes.ENTITY_INTERACTION_RANGE, 0.01f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perSize(DRAGON_STAGE, Attributes.BLOCK_INTERACTION_RANGE, 0.01f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perSize(DRAGON_STAGE, Attributes.MAX_HEALTH, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perSize(DRAGON_STAGE, DSAttributes.DRAGON_BREATH_RANGE, 0.05f, AttributeModifier.Operation.ADD_VALUE)
                ),
                1,
                3,
                List.of(
                        MiscCodecs.GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.value()),
                        MiscCodecs.GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.value())
                ),
                Optional.of(Condition.defaultNaturalGrowthBlocker()),
                Optional.empty()
        ));
    }

    public static ResourceKey<DragonStage> key(final ResourceLocation location) {
        return ResourceKey.create(DragonStage.REGISTRY, location);
    }

    private static ResourceKey<DragonStage> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
