package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.effects.BurnEffect;
import by.dragonsurvivalteam.dragonsurvival.common.effects.ChargedEffect;
import by.dragonsurvivalteam.dragonsurvival.common.effects.DrainEffect;
import by.dragonsurvivalteam.dragonsurvival.common.effects.ModifiableMobEffect;
import by.dragonsurvivalteam.dragonsurvival.common.effects.Stress;
import by.dragonsurvivalteam.dragonsurvival.common.effects.WingDisablingEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DSEffects { // TODO :: add descriptions for the missing N/A marked effects
    public static final DeferredRegister<MobEffect> DS_MOB_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, DragonSurvival.MODID);

    @Translation(type = Translation.Type.EFFECT, comments = "Stress")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Applied to forest dragons who remain too long in the dark. Instantly removes all saturation, and quickly depletes hunger.")
    public static Holder<MobEffect> STRESS = DS_MOB_EFFECTS.register("stress", () -> new Stress(0xf4a2e8));

    /** Some effects are handled in {@link by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonBonusHandler} and {@link by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler} */
    @Translation(type = Translation.Type.EFFECT, comments = "Trapped")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "This net prevents you from escaping into the sky.")
    public static Holder<MobEffect> TRAPPED = DS_MOB_EFFECTS.register("trapped",
            () -> new WingDisablingEffect(MobEffectCategory.HARMFUL, 0xdddddd, true)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, DragonSurvival.res("slow_movement"), -0.5, Operation.ADD_MULTIPLIED_TOTAL)
    );

    /** Some effects are handled in {@link by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler} */
    @Translation(type = Translation.Type.EFFECT, comments = "Broken Wings")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Your wings are broken and you can no longer fly. You have to wait for regeneration.") // TODO
    public static Holder<MobEffect> BROKEN_WINGS = DS_MOB_EFFECTS.register("broken_wings", () -> new WingDisablingEffect(MobEffectCategory.HARMFUL, 0x0, true));

    @Translation(type = Translation.Type.EFFECT, comments = "Magic Disabled")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "The knight has forbidden you to cast magic here.")
    public static Holder<MobEffect> MAGIC_DISABLED = DS_MOB_EFFECTS.register("magic_disabled", () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Hunter Omen")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "You've been too aggressive. Be careful. There is a bounty on your head.")
    public static Holder<MobEffect> HUNTER_OMEN = DS_MOB_EFFECTS.register("hunter_omen", () -> new ModifiableMobEffect(MobEffectCategory.NEUTRAL, 0x0, true));

    @Translation(type = Translation.Type.EFFECT, comments = "Sea Peace")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Protects from dehydration.")
    public static Holder<MobEffect> PEACE = DS_MOB_EFFECTS.register("sea_peace", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Forest Magic")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Protects against the darkness.")
    public static Holder<MobEffect> MAGIC = DS_MOB_EFFECTS.register("forest_magic", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Cave Fire")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Protects from the damaging effects of water.")
    public static Holder<MobEffect> FIRE = DS_MOB_EFFECTS.register("cave_fire", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Sturdy Skin")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Grants additional armor points.")
    public static Holder<MobEffect> STURDY_SKIN = DS_MOB_EFFECTS.register("sturdy_skin",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
                    .addAttributeModifier(Attributes.ARMOR, DragonSurvival.res("sturdy_skin"), 3, Operation.ADD_VALUE)
    );

    @Translation(type = Translation.Type.EFFECT, comments = "Animal Peace")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Animals will not flee from dragons with this effect active.")
    public static Holder<MobEffect> ANIMAL_PEACE = DS_MOB_EFFECTS.register("animal_peace", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Source of Magic")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Gives the dragon infinite mana to use magic.")
    public static Holder<MobEffect> SOURCE_OF_MAGIC = DS_MOB_EFFECTS.register("source_of_magic", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
            .addAttributeModifier(DSAttributes.MANA_REGENERATION, DragonSurvival.res("source_of_magic"), 10, Operation.ADD_MULTIPLIED_BASE));

    @Translation(type = Translation.Type.EFFECT, comments = "Water Vision")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Improves underwater visibility.")
    public static Holder<MobEffect> WATER_VISION = DS_MOB_EFFECTS.register("water_vision", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Lava Vision")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Improves visibility in lava.")
    public static Holder<MobEffect> LAVA_VISION = DS_MOB_EFFECTS.register("lava_vision", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Hunter")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Forest dragons with this effect are invisible while standing in any foliage - their first attack will deal extra damage and remove the effect.")
    public static Holder<MobEffect> HUNTER = DS_MOB_EFFECTS.register("hunter",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
                    // Same value as vanilla speed effect
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, DragonSurvival.res("hunter_speed_multiplier"), 0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
    );

    @Translation(type = Translation.Type.EFFECT, comments = "Burn")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "The target takes fire damage. Damage dealt depends on the speed of the target.")
    public static Holder<MobEffect> BURN = DS_MOB_EFFECTS.register("burn", () -> new BurnEffect(MobEffectCategory.HARMFUL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Charged")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Produces arcs of electricity, damaging nearby mobs.")
    public static Holder<MobEffect> CHARGED = DS_MOB_EFFECTS.register("charged", () -> new ChargedEffect(MobEffectCategory.HARMFUL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Drain")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Forest dragons produce this poisonous gas. Plants will grow when exposed to their breath, while most other things will have their life drained.")
    public static Holder<MobEffect> DRAIN = DS_MOB_EFFECTS.register("drain", () -> new DrainEffect(MobEffectCategory.HARMFUL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Blood Siphon")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "N/A") // TODO
    public static Holder<MobEffect> BLOOD_SIPHON = DS_MOB_EFFECTS.register("blood_siphon", () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Regeneration Delay")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "N/A") // TODO
    public static Holder<MobEffect> REGENERATION_DELAY = DS_MOB_EFFECTS.register("regeneration_delay", () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, true));
}