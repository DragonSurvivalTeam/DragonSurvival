package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.ItemCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Optional;

public class DragonPenalties {
    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ Cave dragons slowly take §cdamage§r while in snow or rain due to their fiery nature.\n",
            "■ The skill §2«Contrast Shower»§r §7could make your life easier.\n",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Snow and Rain Weakness")
    public static final ResourceKey<DragonPenalty> SNOW_AND_RAIN_WEAKNESS = DragonPenalties.key("snow_and_rain_weakness");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ Cave dragons quickly take §cdamage§r from water.\n",
            "■ The effect §2«Cave Fire»§r §7could make your life easier.\n",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Water Weakness")
    public static final ResourceKey<DragonPenalty> WATER_WEAKNESS = DragonPenalties.key("water_weakness");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ If sea dragon are outside of the water for too long, they will dehydrate and suffer damage. Being in rain, ice or snow or drinking water bottles rehydrates you.\n",
            "■ The skill §2«Hydration Capacity»§r §7could make your life easier.\n",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Thin Skin")
    public static final ResourceKey<DragonPenalty> THIN_SKIN = DragonPenalties.key("thin_skin");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ Dragons are unable to wield or equip certain items. Such as bows, shields, and tridents.\n",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Item Blacklist")
    public static final ResourceKey<DragonPenalty> ITEM_BLACKLIST = DragonPenalties.key("item_blacklist");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ The predatory plants in your body dislike §dDarkness§r. If the light level around you is lower than 4, you may receive the §c«Stress»§r effect, rapidly draining your food gauge.\n",
            "■ The skill §2«Light the Dark»§r§f and effect §2«Forest Magic»§r §7could make your life easier.",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Fear of Darkness")
    public static final ResourceKey<DragonPenalty> FEAR_OF_DARKNESS = DragonPenalties.key("fear_of_darkness");

    public static final ResourceKey<DragonPenalty> WATER_POTION_WEAKNESS = DragonPenalties.key("water_potion_weakness");
    public static final ResourceKey<DragonPenalty> WATER_SPLASH_POTION_WEAKNESS = DragonPenalties.key("water_splash_potion_weakness");
    public static final ResourceKey<DragonPenalty> SNOWBALL_WEAKNESS = DragonPenalties.key("snowball_weakness");

    public static void registerPenalties(final BootstrapContext<DragonPenalty> context) {
        context.register(SNOW_AND_RAIN_WEAKNESS, new DragonPenalty(
                Optional.of(DragonSurvival.res("abilities/cave/snow_and_rain_weakness")),
                // Enable when in rain or on (or within) said block tag (except when affected by the 'FIRE' effect)
                Optional.of(AnyOfCondition.anyOf(
                        Condition.thisEntity(EntityCondition.isInRain()),
                        Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.IS_WET)),
                        Condition.thisEntity(EntityCondition.isInBlock(DSBlockTags.IS_WET))
                ).and(Condition.thisEntity(EntityCondition.hasEffect(DSEffects.FIRE)).invert()).build()),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.RAIN_BURN), 1),
                new SupplyTrigger(DragonSurvival.res("rain_supply"), DSAttributes.PENALTY_RESISTANCE_TIME, Functions.secondsToTicks(2), 1, 0.013f, List.of(), false)
        ));

        context.register(WATER_WEAKNESS, new DragonPenalty(
                Optional.of(DragonSurvival.res("abilities/cave/water_weakness")),
                // Enable when water (except when affected by the 'FIRE' effect)
                Optional.of(Condition.thisEntity(EntityCondition.isInFluid(context.lookup(Registries.FLUID).getOrThrow(FluidTags.WATER)))
                        .and(Condition.thisEntity(EntityCondition.hasEffect(DSEffects.FIRE)).invert()).build()),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.WATER_BURN), 1),
                new InstantTrigger(10)
        ));

        context.register(THIN_SKIN, new DragonPenalty(
                Optional.of(DragonSurvival.res("abilities/sea/thin_skin")),
                // Enable when in water, in rain or on (or within) said block tag
                Optional.of(AnyOfCondition.anyOf(
                        Condition.thisEntity(EntityCondition.isInFluid(context.lookup(Registries.FLUID).getOrThrow(FluidTags.WATER))),
                        Condition.thisEntity(EntityCondition.isInRain()),
                        Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.IS_WET)),
                        Condition.thisEntity(EntityCondition.isInBlock(DSBlockTags.IS_WET))
                ).invert().build()),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.DEHYDRATION), 1),
                new SupplyTrigger(
                        DragonSurvival.res("water_supply"),
                        DSAttributes.PENALTY_RESISTANCE_TIME,
                        Functions.secondsToTicks(2),
                        1,
                        0.013f,
                        List.of(
                                new SupplyTrigger.RecoveryItems(
                                        HolderSet.direct(Items.MILK_BUCKET.builtInRegistryHolder(), DSItems.FROZEN_RAW_FISH),
                                        HolderSet.direct(Potions.WATER),
                                        0.5f
                                )
                        ),
                        true
                )
        ));

        context.register(ITEM_BLACKLIST, new DragonPenalty(
                Optional.of(DragonSurvival.res("abilities/cave/item_blacklist")),
                Optional.empty(),
                new ItemBlacklistPenalty(DEFAULT_COMMON_BLACKLIST),
                PenaltyTrigger.instant()
        ));

        context.register(FEAR_OF_DARKNESS, new DragonPenalty(
                Optional.of(DragonSurvival.res("abilities/forest/fear_of_darkness")),
                // Disable when within a light strength of at least 3 or when affected by the 'MAGIC' or 'GLOWING' effects
                Optional.of(AnyOfCondition.anyOf(
                        Condition.thisEntity(EntityCondition.isInLight(3)),
                        Condition.thisEntity(EntityCondition.hasEffect(DSEffects.MAGIC)),
                        Condition.thisEntity(EntityCondition.hasEffect(MobEffects.GLOWING))
                ).invert().build()),
                new MobEffectPenalty(HolderSet.direct(DSEffects.STRESS), 0, Functions.secondsToTicks(10)),
                new SupplyTrigger(DragonSurvival.res("stress_supply"), DSAttributes.PENALTY_RESISTANCE_TIME, Functions.secondsToTicks(2), 1, 0.013f, List.of(), false)
        ));

        context.register(WATER_POTION_WEAKNESS, new DragonPenalty(
                Optional.empty(),
                Optional.empty(),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.WATER_BURN), 2),
                new ItemUsedTrigger(HolderSet.empty(), HolderSet.direct(Potions.WATER))
        ));

        context.register(WATER_SPLASH_POTION_WEAKNESS, new DragonPenalty(
                Optional.empty(),
                Optional.empty(),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.WATER_BURN), 2),
                new HitByWaterPotionTrigger()
        ));

        context.register(SNOWBALL_WEAKNESS, new DragonPenalty(
                Optional.empty(),
                Optional.empty(),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.WATER_BURN), 2),
                new HitByProjectileTrigger(EntityType.SNOWBALL)
        ));
    }

    public static ResourceKey<DragonPenalty> key(final ResourceLocation location) {
        return ResourceKey.create(DragonPenalty.REGISTRY, location);
    }

    public static ResourceKey<DragonPenalty> key(final String path) {
        return key(DragonSurvival.res(path));
    }

    public static final List<String> DEFAULT_COMMON_BLACKLIST = List.of(
            "#" + Tags.Items.TOOLS_SHIELD.location(),
            "#" + Tags.Items.TOOLS_BOW.location(),
            "#" + Tags.Items.TOOLS_CROSSBOW.location(),
            "minecraft:trident",
            "born_in_chaos_v1:staffof_magic_arrows",
            "mowziesmobs:wrought_axe",
            "revised_phantoms:phantom_wings_chestplate",
            "quark:flamerang",
            "quark:pickarang",
            ".*:.*?elytra.*"
    );
}
