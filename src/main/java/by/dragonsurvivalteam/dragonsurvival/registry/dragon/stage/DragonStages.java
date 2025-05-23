package by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthItem;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
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

public class DragonStages {
    @Translation(type = Translation.Type.STAGE, comments = "Newborn")
    public static final ResourceKey<DragonStage> newborn = key("newborn");

    @Translation(type = Translation.Type.STAGE, comments = "Young")
    public static final ResourceKey<DragonStage> young = key("young");

    @Translation(type = Translation.Type.STAGE, comments = "Adult")
    public static final ResourceKey<DragonStage> adult = key("adult");

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
                        Modifier.constant(Attributes.ATTACK_DAMAGE, 1, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.JUMP_STRENGTH, 0.025f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.SAFE_FALL_DISTANCE, 0.25f, AttributeModifier.Operation.ADD_VALUE),
                        /* Per growth */
                        Modifier.precisePerWithBase(Attributes.SCALE, -0.75f, 0.013f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.MAX_HEALTH, -6, 0.4f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(DSAttributes.DRAGON_BREATH_RANGE, 1.5f, 0.05f, AttributeModifier.Operation.ADD_VALUE)
                ),
                List.of(
                        GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.value()),
                        GrowthItem.create(Functions.minutesToTicks(30), DSItems.WEAK_DRAGON_HEART.value()),
                        GrowthItem.create(Functions.minutesToTicks(10), DSItems.DRAGON_HEART_SHARD.value()),
                        GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.value()),
                        GrowthItem.create(0, DSItems.STAR_HEART.value())
                ),
                Optional.of(EntityCondition.defaultNaturalGrowthBlocker()),
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
                        Modifier.constant(Attributes.ATTACK_DAMAGE, 2, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.JUMP_STRENGTH, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.SAFE_FALL_DISTANCE, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        /* Per growth */
                        Modifier.precisePerWithBase(Attributes.SCALE, -0.55f, 1.f / 30.f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(Attributes.MAX_HEALTH, 2.f / 3.f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(DSAttributes.DRAGON_BREATH_RANGE, 3.25f, 0.05f, AttributeModifier.Operation.ADD_VALUE)
                ),
                List.of(
                        GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.value()),
                        GrowthItem.create(Functions.minutesToTicks(30), DSItems.WEAK_DRAGON_HEART.value()),
                        GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.value()),
                        GrowthItem.create(0, DSItems.STAR_HEART.value())
                ),
                Optional.of(EntityCondition.defaultNaturalGrowthBlocker()),
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
                        Modifier.constant(Attributes.STEP_HEIGHT, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.ATTACK_DAMAGE, 3, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.JUMP_STRENGTH, 0.1f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.SAFE_FALL_DISTANCE, 1, AttributeModifier.Operation.ADD_VALUE),
                        /* Per growth */
                        Modifier.precisePerWithBase(Attributes.SCALE, -0.05f, 0.025f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.MAX_HEALTH, 10.0f, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(DSAttributes.DRAGON_BREATH_RANGE, 5.5f, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(Attributes.ENTITY_INTERACTION_RANGE, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(Attributes.BLOCK_INTERACTION_RANGE, 0.05f, AttributeModifier.Operation.ADD_VALUE)
                ),
                List.of(
                        GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.value()),
                        GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.value()),
                        GrowthItem.create(0, DSItems.STAR_HEART.value())
                ),
                Optional.of(EntityCondition.defaultNaturalGrowthBlocker()),
                Optional.empty()
        );
    }
}
