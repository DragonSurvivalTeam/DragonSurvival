package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.AttributeOperation;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.effects.*;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class DSEffects {
    public static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(Registries.MOB_EFFECT, DragonSurvival.MODID);

    @Translation(type = Translation.Type.EFFECT, comments = "Stress")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Applied to forest dragons who remain too long in the dark. Instantly removes all saturation, and quickly depletes hunger.")
    public static RegistryObject<MobEffect> STRESS = REGISTRY.register("stress", () -> new StressEffect(0xf4a2e8));

    /** Some effects are handled in {@link by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonBonusHandler} and {@link by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler} */
    @Translation(type = Translation.Type.EFFECT, comments = "Trapped")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "This net prevents you from escaping into the sky.")
    public static RegistryObject<MobEffect> TRAPPED = REGISTRY.register("trapped",
            () -> new WingDisablingEffect(MobEffectCategory.HARMFUL, 0xdddddd, true)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, legacyId("trapped_slow_movement"), -0.75, AttributeOperation.ADD_MULTIPLIED_TOTAL.legacy())
                    .addAttributeModifier(DSAttributes.JUMP_STRENGTH.get(), legacyId("trapped_jump_strength"), -1, AttributeOperation.ADD_MULTIPLIED_TOTAL.legacy())
    );

    /** Some effects are handled in {@link by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler} */
    @Translation(type = Translation.Type.EFFECT, comments = "Broken Wings")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Your wings are broken and you can no longer fly. You have to wait for regeneration.") // TODO
    public static RegistryObject<MobEffect> BROKEN_WINGS = REGISTRY.register("broken_wings", () -> new WingDisablingEffect(MobEffectCategory.HARMFUL, 0x0, true));

    @Translation(type = Translation.Type.EFFECT, comments = "Magic Disabled")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "The knight has forbidden you to cast magic here.")
    public static RegistryObject<MobEffect> MAGIC_DISABLED = REGISTRY.register("magic_disabled", () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Hunter Omen")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "You've been too aggressive. Be careful. There is a bounty on your head.")
    public static RegistryObject<MobEffect> HUNTER_OMEN = REGISTRY.register("hunter_omen", () -> new ModifiableMobEffect(MobEffectCategory.NEUTRAL, 0x0, true));

    @Translation(type = Translation.Type.EFFECT, comments = "Sea Peace")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Protects from dehydration.")
    public static RegistryObject<MobEffect> PEACE = REGISTRY.register("sea_peace", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Forest Magic")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Protects against the darkness.")
    public static RegistryObject<MobEffect> MAGIC = REGISTRY.register("forest_magic", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Cave Fire")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Protects from the damaging effects of water.")
    public static RegistryObject<MobEffect> FIRE = REGISTRY.register("cave_fire", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Sturdy Skin")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Grants additional armor points.")
    public static RegistryObject<MobEffect> STURDY_SKIN = REGISTRY.register("sturdy_skin",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
                    .addAttributeModifier(Attributes.ARMOR, legacyId("sturdy_skin"), 3, AttributeOperation.ADD_VALUE.legacy())
    );

    @Translation(type = Translation.Type.EFFECT, comments = "Animal Peace")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Animals will not flee from dragons with this effect active.")
    public static RegistryObject<MobEffect> ANIMAL_PEACE = REGISTRY.register("animal_peace", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Source of Magic")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Gives the dragon infinite mana to use magic.")
    public static RegistryObject<MobEffect> SOURCE_OF_MAGIC = REGISTRY.register("source_of_magic", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
            .addAttributeModifier(DSAttributes.MANA_REGENERATION.get(), legacyId("source_of_magic"), 10, AttributeOperation.ADD_MULTIPLIED_BASE.legacy()));

    @Translation(type = Translation.Type.EFFECT, comments = "Water Vision")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Improves underwater visibility.")
    public static RegistryObject<MobEffect> WATER_VISION = REGISTRY.register("water_vision", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Lava Vision")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Improves visibility in lava.")
    public static RegistryObject<MobEffect> LAVA_VISION = REGISTRY.register("lava_vision", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Hunter")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Forest dragons with this effect are invisible while standing in any foliage - their first attack will deal extra damage and remove the effect.")
    public static RegistryObject<MobEffect> HUNTER = REGISTRY.register("hunter",
            () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false)
                    // Same value as vanilla speed effect
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, legacyId("hunter_speed_multiplier"), 0.2f, AttributeOperation.ADD_MULTIPLIED_TOTAL.legacy())
    );

    @Translation(type = Translation.Type.EFFECT, comments = "Burn")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "The target takes fire damage. Damage dealt depends on the speed of the target.")
    public static RegistryObject<MobEffect> BURN = REGISTRY.register("burn", () -> new BurnEffect(MobEffectCategory.HARMFUL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Charged")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Produces arcs of electricity, damaging nearby mobs.")
    public static RegistryObject<MobEffect> CHARGED = REGISTRY.register("charged", () -> new ChargedEffect(MobEffectCategory.HARMFUL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Drain")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Forest dragons produce this poisonous gas. Plants will grow when exposed to their breath, while most other things will have their life drained.")
    public static RegistryObject<MobEffect> DRAIN = REGISTRY.register("drain", () -> new DrainEffect(MobEffectCategory.HARMFUL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Blood Siphon")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Entities with this effect will restore life by 1% of the damage dealt (per amplifier) to the attacker")
    public static RegistryObject<MobEffect> BLOOD_SIPHON = REGISTRY.register("blood_siphon", () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Regeneration Delay")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "N/A") // TODO :: effect not implemented
    public static RegistryObject<MobEffect> REGENERATION_DELAY = REGISTRY.register("regeneration_delay", () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, true));

    @Translation(type = Translation.Type.EFFECT, comments = "Blast Dusted")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "You are about to explode if you don't find some water, quickly!")
    public static RegistryObject<MobEffect> BLAST_DUSTED = REGISTRY.register("blast_dusted", () -> new BlastDustedEffect(MobEffectCategory.HARMFUL, 0x0, true));

    @Translation(type = Translation.Type.EFFECT, comments = "Confounded")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "A deadly curse that impairs the senses, and causes you to take damage whenever you inflict harm upon others.")
    public static RegistryObject<MobEffect> CONFOUNDED = REGISTRY.register("confounded", () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, false));

    @Translation(type = Translation.Type.EFFECT, comments = "Exhausted Soul")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Your soul is exhausted and you have to rest until you can switch your form again.")
    public static RegistryObject<MobEffect> EXHAUSTED_SOUL = REGISTRY.register("exhausted_soul", () -> new ModifiableMobEffect(MobEffectCategory.HARMFUL, 0x0, true));

    @Translation(type = Translation.Type.EFFECT, comments = "Empowered Soul")
    @Translation(type = Translation.Type.EFFECT_DESCRIPTION, comments = "Your soul is empowered and you can freely switch your form whenever you want.")
    public static RegistryObject<MobEffect> EMPOWERED_SOUL = REGISTRY.register("empowered_soul", () -> new ModifiableMobEffect(MobEffectCategory.BENEFICIAL, 0x0, false));

    private static String legacyId(final String path) {
        ResourceLocation id = DragonSurvival.res(path);
        return UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8)).toString();
    }
}
