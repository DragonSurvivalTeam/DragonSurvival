package by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthItem;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;

import java.util.List;
import java.util.Optional;

public class AncientDatapack {
    @Translation(type = Translation.Type.STAGE, comments = "Ancient")
    public static final ResourceKey<DragonStage> ancient = DragonStages.key("ancient");

    public static void register(final BootstrapContext<DragonStage> context) {
        context.register(ancient, ancient());
    }

    public static DragonStage ancient() {
        return new DragonStage(
                true,
                new MiscCodecs.Bounds(60, 300),
                Functions.daysToTicks(40),
                List.of(
                        /* Constant */
                        Modifier.constant(Attributes.MOVEMENT_SPEED, 0.02f, AttributeModifier.Operation.ADD_VALUE),
                        /* Per size */
                        Modifier.precisePerWithBase(Attributes.SCALE, 0.45f, 0.025f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.MAX_HEALTH, 20.0f, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(DSAttributes.DRAGON_BREATH_RANGE, 6.5f, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.ATTACK_DAMAGE, 3.f, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.JUMP_STRENGTH, 0.1f, 0.0015f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.SAFE_FALL_DISTANCE, 1, 0.015f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(DSAttributes.BLOCK_BREAK_RADIUS, 0.01f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.ENTITY_INTERACTION_RANGE, 1, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.BLOCK_INTERACTION_RANGE, 1, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.STEP_HEIGHT, 0.5f, 0.015f, AttributeModifier.Operation.ADD_VALUE)
                ),
                List.of(
                        GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.value()),
                        GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.value()),
                        GrowthItem.create(0, DSItems.STAR_HEART.value())
                ),
                Optional.of(EntityCondition.defaultNaturalGrowthBlocker()),
                Optional.of(new MiscCodecs.DestructionData(EntityPredicate.Builder.entity().build(), BlockPredicate.matchesTag(DSBlockTags.LARGE_DRAGON_DESTRUCTIBLE), 120, 120, 0.05))
        );
    }
}
