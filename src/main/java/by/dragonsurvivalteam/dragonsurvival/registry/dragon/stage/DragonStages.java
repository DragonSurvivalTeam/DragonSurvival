package by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.AttributeOperation;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthItem;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

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

    public static void registerStages(final BootstapContext<DragonStage> context) {
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
                        Modifier.constant(Attributes.ATTACK_DAMAGE, 1, AttributeOperation.ADD_VALUE),
                        Modifier.constant(DSAttributes.JUMP_STRENGTH, 0.025f, AttributeOperation.ADD_VALUE),
                        Modifier.constant(DSAttributes.SAFE_FALL_DISTANCE, 0.25f, AttributeOperation.ADD_VALUE),
                        /* Per growth */
                Modifier.precisePerWithBase(DSAttributes.SCALE, -0.75f, 0.013f, AttributeOperation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.MAX_HEALTH, -6, 0.4f, AttributeOperation.ADD_VALUE),
                        Modifier.perWithBase(DSAttributes.DRAGON_BREATH_RANGE, 1.5f, 0.05f, AttributeOperation.ADD_VALUE)
                ),
                List.of(
                        GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.get()),
                        GrowthItem.create(Functions.minutesToTicks(30), DSItems.WEAK_DRAGON_HEART.get()),
                        GrowthItem.create(Functions.minutesToTicks(10), DSItems.DRAGON_HEART_SHARD.get()),
                        GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.get()),
                        GrowthItem.create(0, DSItems.STAR_HEART.get())
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
                        Modifier.constant(Attributes.ATTACK_DAMAGE, 2, AttributeOperation.ADD_VALUE),
                        Modifier.constant(DSAttributes.JUMP_STRENGTH, 0.05f, AttributeOperation.ADD_VALUE),
                        Modifier.constant(DSAttributes.SAFE_FALL_DISTANCE, 0.5f, AttributeOperation.ADD_VALUE),
                        /* Per growth */
                Modifier.precisePerWithBase(DSAttributes.SCALE, -0.55f, 1.f / 30.f, AttributeOperation.ADD_VALUE),
                        Modifier.per(Attributes.MAX_HEALTH, 2.f / 3.f, AttributeOperation.ADD_VALUE),
                        Modifier.perWithBase(DSAttributes.DRAGON_BREATH_RANGE, 3.25f, 0.05f, AttributeOperation.ADD_VALUE)
                ),
                List.of(
                        GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.get()),
                        GrowthItem.create(Functions.minutesToTicks(30), DSItems.WEAK_DRAGON_HEART.get()),
                        GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.get()),
                        GrowthItem.create(0, DSItems.STAR_HEART.get())
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
                        Modifier.constant(ForgeMod.STEP_HEIGHT_ADDITION, 0.5f, AttributeOperation.ADD_VALUE),
                        Modifier.constant(Attributes.ATTACK_DAMAGE, 3, AttributeOperation.ADD_VALUE),
                        Modifier.constant(DSAttributes.JUMP_STRENGTH, 0.1f, AttributeOperation.ADD_VALUE),
                        Modifier.constant(DSAttributes.SAFE_FALL_DISTANCE, 1, AttributeOperation.ADD_VALUE),
                        /* Per growth */
                Modifier.precisePerWithBase(DSAttributes.SCALE, -0.05f, 0.025f, AttributeOperation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.MAX_HEALTH, 10.0f, 0.5f, AttributeOperation.ADD_VALUE),
                        Modifier.perWithBase(DSAttributes.DRAGON_BREATH_RANGE, 5.5f, 0.05f, AttributeOperation.ADD_VALUE),
                        Modifier.per(ForgeMod.ENTITY_REACH, 0.05f, AttributeOperation.ADD_VALUE),
                Modifier.per(ForgeMod.BLOCK_REACH, 0.05f, AttributeOperation.ADD_VALUE)
                ),
                List.of(
                        GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.get()),
                        GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.get()),
                        GrowthItem.create(0, DSItems.STAR_HEART.get())
                ),
                Optional.of(EntityCondition.defaultNaturalGrowthBlocker()),
                Optional.empty()
        );
    }
}
