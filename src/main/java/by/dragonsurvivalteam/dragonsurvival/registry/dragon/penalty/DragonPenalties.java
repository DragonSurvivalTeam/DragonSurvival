package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Fear;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.PotionData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates.EntityCheckPredicate;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.EntityCondition;
import by.dragonsurvivalteam.dragonsurvival.common.conditions.ItemCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.predicates.AnyOfCondition;
import net.neoforged.neoforge.common.Tags;

import java.util.List;
import java.util.Optional;

public class DragonPenalties {
    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ Cave dragons slowly take §cdamage§r while in snow or rain due to their fiery nature.\n",
            "■ The skill §2«Contrast Shower»§r §7could make your life easier.\n",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Cold Weakness")
    public static final ResourceKey<DragonPenalty> COLD_WEAKNESS = DragonPenalties.key("cold_weakness");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ Cave dragons quickly take §cdamage§r from water. Explosive water potions and snowballs also deal damage.\n",
            "■ The effect §2«Cave Fire»§r §7could make your life easier.\n",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Water Weakness")
    public static final ResourceKey<DragonPenalty> WATER_WEAKNESS = DragonPenalties.key("water_weakness");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ If sea dragon are outside of the water for too long, they will §ddehydrate§r§7 and suffer §ddamage§r§7.\n",
            "■ Being in rain, ice or snow or drinking water bottles rehydrates you. The skill §2«Hydration Capacity»§r §7could make your life easier.\n",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Thin Skin")
    public static final ResourceKey<DragonPenalty> THIN_SKIN = DragonPenalties.key("thin_skin");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = "■ Dragons are §dunable to wield§r§7 or equip certain items. Such as bows, shields, and tridents. They won't equip and will §cdrop§r from the hotbar, but you can still craft items with it.")
    @Translation(type = Translation.Type.PENALTY, comments = "Item Blacklist")
    public static final ResourceKey<DragonPenalty> ITEM_BLACKLIST = DragonPenalties.key("item_blacklist");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ The predatory plants in your body dislike §dDarkness§r. If the light level around you is lower than 4, you may receive the §c«Stress»§r effect, rapidly draining your food gauge.\n",
            "■ The skill §2«Light the Dark» §7and effect §2«Forest Magic» §7could make your life easier.",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Fear of Darkness")
    public static final ResourceKey<DragonPenalty> FEAR_OF_DARKNESS = DragonPenalties.key("fear_of_darkness");

    public static final ResourceKey<DragonPenalty> WATER_POTION_WEAKNESS = DragonPenalties.key("water_potion_weakness");
    public static final ResourceKey<DragonPenalty> WATER_SPLASH_POTION_WEAKNESS = DragonPenalties.key("water_splash_potion_weakness");
    public static final ResourceKey<DragonPenalty> SNOWBALL_WEAKNESS = DragonPenalties.key("snowball_weakness");

    @Translation(type = Translation.Type.PENALTY_DESCRIPTION, comments = {
            "■ Dragons are §dscary§r§7  creatures. Animals will try to §cavoid§r them.\n",
            "■ Build the §2«Beacon»§7 to become more attractive.",
    })
    @Translation(type = Translation.Type.PENALTY, comments = "Fear")
    public static final ResourceKey<DragonPenalty> FEAR = DragonPenalties.key("fear");

    public static void registerPenalties(final BootstrapContext<DragonPenalty> context) {
        context.register(COLD_WEAKNESS, new DragonPenalty(
                Optional.of(DragonSurvival.res("penalties/cave/cold_weakness")),
                Optional.of(AnyOfCondition.anyOf(
                        Condition.thisEntity(EntityCondition.isInRainOrSnow()),
                        Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.IS_WET)),
                        Condition.thisEntity(EntityCondition.isInBlock(DSBlockTags.IS_WET))
                ).and(Condition.thisEntity(EntityCondition.hasEffect(DSEffects.FIRE)).invert()).build()),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.RAIN_BURN), 1),
                new SupplyTrigger(DragonSurvival.res("rain_supply"), DSAttributes.PENALTY_RESISTANCE_TIME, Functions.secondsToTicks(2), 1, 0.013f, List.of(), false, Optional.of(ParticleTypes.SMOKE))
        ));

        context.register(WATER_WEAKNESS, new DragonPenalty(
                Optional.of(DragonSurvival.res("penalties/cave/water_weakness")),
                Optional.of(Condition.thisEntity(EntityCondition.isInFluid(context.lookup(Registries.FLUID).getOrThrow(FluidTags.WATER)))
                        .and(Condition.thisEntity(EntityCondition.hasEffect(DSEffects.FIRE)).invert()).build()),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.WATER_BURN), 1),
                new InstantTrigger(10)
        ));

        context.register(THIN_SKIN, new DragonPenalty(
                Optional.of(DragonSurvival.res("penalties/sea/thin_skin")),
                Optional.of(AnyOfCondition.anyOf(
                        Condition.thisEntity(EntityCondition.hasEffect(DSEffects.PEACE)),
                        Condition.thisEntity(EntityCondition.isInFluid(context.lookup(Registries.FLUID).getOrThrow(FluidTags.WATER))),
                        Condition.thisEntity(EntityCondition.isOnBlock(DSBlockTags.IS_WET)),
                        Condition.thisEntity(EntityCondition.isInBlock(DSBlockTags.IS_WET)),
                        Condition.thisEntity(EntityCondition.isInRainOrSnow())
                ).invert().build()),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.DEHYDRATION), 1),
                new SupplyTrigger(
                        DragonSurvival.res("water_supply"),
                        DSAttributes.PENALTY_RESISTANCE_TIME,
                        Functions.secondsToTicks(2),
                        1,
                        0.013f,
                        List.of(
                                new SupplyTrigger.RecoveryItem(
                                        List.of(
                                                ItemCondition.is(DSItems.FROZEN_RAW_FISH.value()),
                                                ItemCondition.hasPotion(Potions.WATER)
                                        ),
                                        0.5f
                                )
                        ),
                        true,
                        Optional.empty()
                )
        ));

        context.register(ITEM_BLACKLIST, new DragonPenalty(
                Optional.of(DragonSurvival.res("penalties/general/item_blacklist")),
                Optional.empty(),
                new ItemBlacklistPenalty(DEFAULT_COMMON_BLACKLIST),
                PenaltyTrigger.instant()
        ));

        context.register(FEAR_OF_DARKNESS, new DragonPenalty(
                Optional.of(DragonSurvival.res("penalties/forest/fear_of_darkness")),
                Optional.of(AnyOfCondition.anyOf(
                        Condition.thisEntity(EntityCondition.hasEffect(DSEffects.MAGIC)),
                        Condition.thisEntity(EntityCondition.hasEffect(MobEffects.GLOWING)),
                        Condition.thisEntity(EntityCondition.isItemEquipped(EquipmentSlot.MAINHAND, DSItemTags.LIGHT_SOURCE)),
                        Condition.thisEntity(EntityCondition.isItemEquipped(EquipmentSlot.OFFHAND, DSItemTags.LIGHT_SOURCE)),
                        Condition.thisEntity(EntityCondition.isInLight(3))
                ).invert().build()),
                new MobEffectPenalty(PotionData.create(DSEffects.STRESS).duration(10).showParticles().build()),
                new SupplyTrigger(DragonSurvival.res("stress_supply"), DSAttributes.PENALTY_RESISTANCE_TIME, Functions.secondsToTicks(2), 1, 0.013f, List.of(), false, Optional.empty())
        ));

        context.register(WATER_POTION_WEAKNESS, new DragonPenalty(
                Optional.empty(),
                Optional.empty(),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.WATER_BURN), 2),
                new ItemUsedTrigger(List.of(
                        ItemCondition.hasPotion(Potions.WATER),
                        ItemCondition.is(Items.MILK_BUCKET)
                ))
        ));

        context.register(WATER_SPLASH_POTION_WEAKNESS, new DragonPenalty(
                Optional.empty(),
                Optional.empty(),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.WATER_BURN), 2),
                HitByWaterPotionTrigger.INSTANCE
        ));

        context.register(SNOWBALL_WEAKNESS, new DragonPenalty(
                Optional.empty(),
                Optional.empty(),
                new DamagePenalty(context.lookup(Registries.DAMAGE_TYPE).getOrThrow(DSDamageTypes.WATER_BURN), 2),
                new HitByProjectileTrigger(HolderSet.direct(EntityType.SNOWBALL.builtInRegistryHolder()))
        ));

        context.register(FEAR, new DragonPenalty(
                Optional.of(DragonSurvival.res("penalties/general/fear")),
                Optional.of(Condition.thisEntity(EntityCondition.hasEffect(DSEffects.ANIMAL_PEACE)).invert().build()),
                new FearPenalty(List.of(new Fear(
                        DurationInstanceBase.create(DragonSurvival.res("animals")).infinite().removeAutomatically().hidden().build(),
                        Optional.of(
                                Condition.thisEntity(EntityCondition.isType(EntityCheckPredicate.Type.ANIMAL))
                                        .and(Condition.thisEntity(EntityCondition.isType(DSEntityTypeTags.ANIMAL_AVOID_BLACKLIST)).invert()).build()
                        ),
                        LevelBasedValue.constant(20),
                        LevelBasedValue.constant(1.3f),
                        LevelBasedValue.constant(1.5f)
                ))),
                PenaltyTrigger.instant()
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
