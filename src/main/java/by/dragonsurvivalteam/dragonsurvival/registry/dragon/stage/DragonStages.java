package by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;
import java.util.Optional;

public class DragonStages {
    @Translation(type = Translation.Type.STAGE, comments = "Newborn")
    public static ResourceKey<DragonStage> newborn = key("newborn");

    @Translation(type = Translation.Type.STAGE, comments = "Young")
    public static ResourceKey<DragonStage> young = key("young");

    @Translation(type = Translation.Type.STAGE, comments = "Adult")
    public static ResourceKey<DragonStage> adult = key("adult");

    public static ResourceKey<DragonStage> key(final ResourceLocation location) {
        return ResourceKey.create(DragonStage.REGISTRY, location);
    }

    public static ResourceKey<DragonStage> key(final String path) {
        return key(DragonSurvival.res(path));
    }

    public static void registerStages(final BootstrapContext<DragonStage> context) {
        context.register(newborn, newborn());
        context.register(young, young());
        context.register(adult, adult());
    }

    public static DragonStage newborn() {
        return new DragonStage(
                true,
                new MiscCodecs.Bounds(10, 25),
                Functions.hoursToTicks(3),
                List.of(
                        /* Constant */
                        Modifier.constant(Attributes.SUBMERGED_MINING_SPEED, 1, AttributeModifier.Operation.ADD_VALUE, BuiltInDragonSpecies.SEA),
                        Modifier.constant(Attributes.MAX_HEALTH, -7, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.ATTACK_DAMAGE, 1, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.JUMP_STRENGTH, 0.025f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.SAFE_FALL_DISTANCE, 0.25f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DSAttributes.DRAGON_BREATH_RANGE, 1.5f, AttributeModifier.Operation.ADD_VALUE),
                        /* Per size */
                        Modifier.per(Attributes.MAX_HEALTH, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(DSAttributes.DRAGON_BREATH_RANGE, 0.05f, AttributeModifier.Operation.ADD_VALUE)
                ),
                List.of(
                        MiscCodecs.GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.value()),
                        MiscCodecs.GrowthItem.create(Functions.minutesToTicks(30), DSItems.WEAK_DRAGON_HEART.value()),
                        MiscCodecs.GrowthItem.create(Functions.minutesToTicks(10), DSItems.DRAGON_HEART_SHARD.value()),
                        MiscCodecs.GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.value())
                ),
                Optional.of(EntityCondition.defaultNaturalGrowthBlocker()),
                Optional.empty(),
                Optional.empty()
        );
    }

    public static DragonStage young() {
        return new DragonStage(
                true,
                new MiscCodecs.Bounds(25, 40),
                Functions.hoursToTicks(6),
                List.of(
                        /* Constant */
                        Modifier.constant(Attributes.SUBMERGED_MINING_SPEED, 2, AttributeModifier.Operation.ADD_VALUE, BuiltInDragonSpecies.SEA),
                        Modifier.constant(Attributes.STEP_HEIGHT, 0.25f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.ATTACK_DAMAGE, 2, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.JUMP_STRENGTH, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.SAFE_FALL_DISTANCE, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DSAttributes.DRAGON_BREATH_RANGE, 2.5f, AttributeModifier.Operation.ADD_VALUE),
                        /* Per size */
                        Modifier.per(Attributes.ENTITY_INTERACTION_RANGE, 0.01f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(Attributes.BLOCK_INTERACTION_RANGE, 0.01f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(Attributes.MAX_HEALTH, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(DSAttributes.DRAGON_BREATH_RANGE, 0.05f, AttributeModifier.Operation.ADD_VALUE)
                ),
                List.of(
                        MiscCodecs.GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.value()),
                        MiscCodecs.GrowthItem.create(Functions.minutesToTicks(30), DSItems.WEAK_DRAGON_HEART.value()),
                        MiscCodecs.GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.value())
                ),
                Optional.of(EntityCondition.defaultNaturalGrowthBlocker()),
                Optional.empty(),
                Optional.empty()
        );
    }

    public static DragonStage adult() {
        return new DragonStage(
                true,
                new MiscCodecs.Bounds(40, 60),
                Functions.hoursToTicks(24),
                List.of(
                        /* Constant */
                        Modifier.constant(Attributes.SUBMERGED_MINING_SPEED, 3, AttributeModifier.Operation.ADD_VALUE, BuiltInDragonSpecies.SEA),
                        Modifier.constant(Attributes.STEP_HEIGHT, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.ATTACK_DAMAGE, 3, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.JUMP_STRENGTH, 0.1f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.SAFE_FALL_DISTANCE, 1, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DSAttributes.DRAGON_BREATH_RANGE, 4, AttributeModifier.Operation.ADD_VALUE),
                        /* Per size */
                        Modifier.per(Attributes.ENTITY_INTERACTION_RANGE, 0.01f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(Attributes.BLOCK_INTERACTION_RANGE, 0.01f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(Attributes.MAX_HEALTH, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(DSAttributes.DRAGON_BREATH_RANGE, 0.05f, AttributeModifier.Operation.ADD_VALUE)
                ),
                List.of(
                        MiscCodecs.GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.value()),
                        MiscCodecs.GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.value())
                ),
                Optional.of(EntityCondition.defaultNaturalGrowthBlocker()),
                Optional.empty(),
                Optional.empty()
        );
    }
}
