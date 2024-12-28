package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Optional;

public class DragonPenalties {
    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ Cave dragons take §cdamage§r snow and rain slowly due to their fiery nature.\n",
            "■ The skill «Contrast Shower» §7could make your life easier.\n",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Snow and Rain Weakness")
    public static final ResourceKey<DragonPenalty> SNOW_AND_RAIN_WEAKNESS = DragonPenalties.key("snow_and_rain_weakness");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ Cave dragons take §cdamage§r from water quickly due to their fiery nature.\n",
            "■ The skill effect «Cave Fire» §7could make your life easier.\n",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Water Weakness")
    public static final ResourceKey<DragonPenalty> WATER_WEAKNESS = DragonPenalties.key("water_weakness");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ Drying out under the harsh sun is a major concern for sea dragons. If they are outside of the water for too long, they will dehydrate and suffer damage.\n",
            "■ The skill «Hydration Capacity», rain, ice, snow and water bottles §7could make your life easier.§r\n",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Thin Skin")
    public static final ResourceKey<DragonPenalty> THIN_SKIN = DragonPenalties.key("thin_skin");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ Dragons are unable to wield or equip certain items.\n",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Item Blacklist")
    public static final ResourceKey<DragonPenalty> ITEM_BLACKLIST = DragonPenalties.key("item_blacklist");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ The predatory plants in your body dislike §dDarkness§r. If the light level around you is lower than 4, you may receive the §c«Stress»§r effect, rapidly draining your food gauge.\n",
            "■ The skill «Light the Dark» and effect «Forest Magic» §7could make your life easier.",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Fear of Darkness")
    public static final ResourceKey<DragonPenalty> FEAR_OF_DARKNESS = DragonPenalties.key("fear_of_darkness");

    public static void registerPenalties(final BootstrapContext<DragonPenalty> context) {
        context.register(SNOW_AND_RAIN_WEAKNESS, new DragonPenalty(
                DragonSurvival.res("abilities/cave/burn_1"),
                Optional.of(AnyOfCondition.anyOf(
                        Condition.thisEntity(EntityCondition.isInRain()),
                        Condition.thisEntity(EntityCondition.isOnBlock(Blocks.SNOW, Blocks.POWDER_SNOW, Blocks.SNOW_BLOCK))
                ).build()),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.RAIN_BURN), 1),
                new SupplyTrigger("rain_supply", DSAttributes.PENALTY_RESISTANCE_TIME, Functions.secondsToTicks(2), 1, 0.013f, List.of(), false)
        ));

        context.register(WATER_WEAKNESS, new DragonPenalty(
                DragonSurvival.res("abilities/cave/cave_claws_and_teeth_1"), // TODO
                Optional.of(Condition.thisEntity(EntityCondition.isInFluid(context.lookup(BuiltInRegistries.FLUID.key()).getOrThrow(FluidTags.WATER))).build()),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.WATER_BURN), 1),
                new InstantTrigger(10)
        ));

        context.register(THIN_SKIN, new DragonPenalty(
                DragonSurvival.res("abilities/cave/cave_magic_1"), // TODO
                Optional.of(AnyOfCondition.anyOf(
                        Condition.thisEntity(EntityCondition.isInFluid(context.lookup(BuiltInRegistries.FLUID.key()).getOrThrow(FluidTags.WATER))),
                        Condition.thisEntity(EntityCondition.isInRain()),
                        Condition.thisEntity(EntityCondition.isOnBlock(Blocks.SNOW, Blocks.POWDER_SNOW, Blocks.SNOW_BLOCK))
                ).invert().build()),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.DEHYDRATION), 1),
                new SupplyTrigger(
                        "water_supply",
                        DSAttributes.PENALTY_RESISTANCE_TIME,
                        Functions.secondsToTicks(2),
                        1,
                        0.013f,
                        List.of(
                                new SupplyTrigger.RecoveryItems(
                                        HolderSet.direct(Items.MILK_BUCKET.builtInRegistryHolder()),
                                        HolderSet.direct(Potions.WATER),
                                        0.5f
                                )
                        ),
                        true
                )
        ));

        context.register(ITEM_BLACKLIST, new DragonPenalty(
                DragonSurvival.res("abilities/cave/cave_athletics_1"), // TODO
                Optional.empty(),
                new ItemBlacklistPenalty(DEFAULT_COMMON_BLACKLIST),
                PenaltyTrigger.instant()
        ));

        context.register(FEAR_OF_DARKNESS, new DragonPenalty(
                DragonSurvival.res("abilities/cave/hot_blood_1"), // TODO
                Optional.of(AnyOfCondition.anyOf(
                        Condition.thisEntity(EntityCondition.isInLight(3)),
                        Condition.thisEntity(EntityCondition.hasEffect(DSEffects.MAGIC))
                ).invert().build()),
                new MobEffectPenalty(HolderSet.direct(DSEffects.STRESS), 0, Functions.secondsToTicks(10)),
                new SupplyTrigger("stress_supply", DSAttributes.PENALTY_RESISTANCE_TIME, Functions.secondsToTicks(2), 1, 0.013f, List.of(), false)
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
