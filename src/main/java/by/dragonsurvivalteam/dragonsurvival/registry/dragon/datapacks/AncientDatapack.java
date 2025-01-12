package by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthItem;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

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
                        Modifier.constant(Attributes.SUBMERGED_MINING_SPEED, 3, AttributeModifier.Operation.ADD_VALUE, BuiltInDragonSpecies.SEA),
                        Modifier.constant(Attributes.ATTACK_DAMAGE, 3, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.JUMP_STRENGTH, 0.1f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.SAFE_FALL_DISTANCE, 1, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(DSAttributes.DRAGON_BREATH_RANGE, 4, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.constant(Attributes.MOVEMENT_SPEED, 0.02f, AttributeModifier.Operation.ADD_VALUE),
                        /* Per size */
                        Modifier.per(Attributes.MAX_HEALTH, 0.5f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(DSAttributes.DRAGON_BREATH_RANGE, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(Attributes.ATTACK_DAMAGE, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(Attributes.JUMP_STRENGTH, 0.0015f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(Attributes.SAFE_FALL_DISTANCE, 0.015f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.per(DSAttributes.BLOCK_BREAK_RADIUS, 0.01f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.ENTITY_INTERACTION_RANGE, -2, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.BLOCK_INTERACTION_RANGE, -2, 0.05f, AttributeModifier.Operation.ADD_VALUE),
                        Modifier.perWithBase(Attributes.STEP_HEIGHT, -0.4f, 0.015f, AttributeModifier.Operation.ADD_VALUE)
                        ),
                List.of(
                        GrowthItem.create(Functions.hoursToTicks(1), DSItems.ELDER_DRAGON_HEART.value()),
                        GrowthItem.create(Functions.hoursToTicks(-1), DSItems.STAR_BONE.value())
                ),
                Optional.of(EntityCondition.defaultNaturalGrowthBlocker()),
                Optional.empty(),
                Optional.of(new MiscCodecs.DestructionData(120, 120, 0.05))
        );
    }
}
