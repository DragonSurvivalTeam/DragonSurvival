package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.BeDragonTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.ConvertItemFromAbility;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.MineBlockUnderLavaTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.SleepOnTreasureTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.StealFromVillagerTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.StopNaturalGrowthTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.UpgradeAbilityTrigger;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.UseDragonSoulTrigger;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DSAdvancementTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> REGISTRY = DeferredRegister.create(Registries.TRIGGER_TYPE, DragonSurvival.MODID);

    public static final Supplier<BeDragonTrigger> BE_DRAGON = REGISTRY.register("be_dragon", BeDragonTrigger::new);
    public static final Supplier<SleepOnTreasureTrigger> SLEEP_ON_TREASURE = REGISTRY.register("sleep_on_treasure", SleepOnTreasureTrigger::new);
    public static final Supplier<MineBlockUnderLavaTrigger> MINE_BLOCK_UNDER_LAVA = REGISTRY.register("mine_block_under_lava", MineBlockUnderLavaTrigger::new);
    /** In order to only trigger when the item has been fully used, not just started to being used */
    public static final Supplier<UseDragonSoulTrigger> USE_DRAGON_SOUL = REGISTRY.register("use_dragon_soul", UseDragonSoulTrigger::new);
    /** {@link CriteriaTriggers#USING_ITEM} is only triggered when {@link LivingEntity#startUsingItem(InteractionHand)} is called in {@link Item#use(Level, Player, InteractionHand)} */
    public static final Supplier<StopNaturalGrowthTrigger> STOP_NATURAL_GROWTH = REGISTRY.register("stop_natural_growth", StopNaturalGrowthTrigger::new);
    public static final Supplier<UpgradeAbilityTrigger> UPGRADE_ABILITY = REGISTRY.register("upgrade_ability", UpgradeAbilityTrigger::new);
    public static final Supplier<ConvertItemFromAbility> CONVERT_ITEM_FROM_ABILITY = REGISTRY.register("convert_item_from_ability", ConvertItemFromAbility::new);
    public static final Supplier<StealFromVillagerTrigger> STEAL_FROM_VILLAGER = REGISTRY.register("steal_from_villager", StealFromVillagerTrigger::new);
}
