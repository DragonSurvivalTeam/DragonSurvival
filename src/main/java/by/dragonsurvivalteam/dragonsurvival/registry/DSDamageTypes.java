package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DeathMessageType;
import net.minecraft.world.level.Level;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DSDamageTypes {
    // We don't need to use a DeferredRegister for DamageTypes, as they are fully data driven.
    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Anti-Dragon")
    public static final ResourceKey<DamageType> ANTI_DRAGON = key("anti_dragon");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Fire Breath")
    public static final ResourceKey<DamageType> FIRE_BREATH = key("fire_breath");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Burn")
    public static final ResourceKey<DamageType> BURN = key("burn");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Crushed")
    public static final ResourceKey<DamageType> CRUSHED = key("crushed");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Dehydration")
    public static final ResourceKey<DamageType> DEHYDRATION = key("dehydration");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Ball Lightning")
    public static final ResourceKey<DamageType> BALL_LIGHTNING = key("ball_lightning");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Dragon Breath")
    public static final ResourceKey<DamageType> DRAGON_BREATH = key("dragon_breath");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Poison Breath")
    public static final ResourceKey<DamageType> POISON_BREATH = key("poison_breath");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Drain")
    public static final ResourceKey<DamageType> DRAIN = key("drain");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Rain Burn")
    public static final ResourceKey<DamageType> RAIN_BURN = key("rain_burn");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Lightning Breath")
    public static final ResourceKey<DamageType> LIGHTNING_BREATH = key("lightning_breath");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Water Burn")
    public static final ResourceKey<DamageType> WATER_BURN = key("water_burn");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Spike")
    public static final ResourceKey<DamageType> SPIKE = key("spike");

    @Translation(type = Translation.Type.DAMAGE_TYPE, comments = "Electric")
    public static final ResourceKey<DamageType> ELECTRIC = key("electric");

    // Used to prevent flinching when taking damage from a size decrease (see GameRendererMixin)
    public static final ResourceKey<DamageType> NO_FLINCH = key("no_flinch");

    public static Holder<DamageType> get(final Level level, final ResourceKey<DamageType> damageType) {
        return level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(damageType);
    }

    public static void registerDamageTypes(final BootstrapContext<DamageType> context) {
        register(context, DSDamageTypes.ANTI_DRAGON);
        register(context, DSDamageTypes.FIRE_BREATH);
        register(context, DSDamageTypes.BURN);
        register(context, DSDamageTypes.CRUSHED);
        register(context, DSDamageTypes.DEHYDRATION);
        register(context, DSDamageTypes.BALL_LIGHTNING);
        register(context, DSDamageTypes.DRAGON_BREATH);
        register(context, DSDamageTypes.POISON_BREATH);
        register(context, DSDamageTypes.DRAIN);
        register(context, DSDamageTypes.RAIN_BURN);
        register(context, DSDamageTypes.LIGHTNING_BREATH);
        register(context, DSDamageTypes.WATER_BURN);
        register(context, DSDamageTypes.SPIKE);
        register(context, DSDamageTypes.ELECTRIC);
        register(context, DSDamageTypes.NO_FLINCH);
    }

    private static void register(final BootstrapContext<DamageType> context, final ResourceKey<DamageType> damageType) {
        context.register(damageType, type(damageType.location().getPath()));
    }

    private static DamageType type(final String messageId) {
        return new DamageType(
                // Translation key for the death message
                DragonSurvival.MODID + "." + messageId,
                // Determines when the game difficulty should scale the damage
                DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                // Amount of exhaustion caused by receiving this kind of damage
                0.1f,
                // Currently only affects sound effects
                DamageEffects.HURT,
                // How the death message is built (e.g. intentional game design links to a bed-used-in-nether bug report)
                DeathMessageType.DEFAULT
        );
    }

    private static ResourceKey<DamageType> key(final String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(MODID, name));
    }
}