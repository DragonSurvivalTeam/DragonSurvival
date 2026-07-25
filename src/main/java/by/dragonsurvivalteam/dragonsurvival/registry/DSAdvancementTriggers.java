package by.dragonsurvivalteam.dragonsurvival.registry;

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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.function.Supplier;

public class DSAdvancementTriggers {
    public static final TriggerRegistry REGISTRY = new TriggerRegistry();

    public static final Supplier<BeDragonTrigger> BE_DRAGON = register(new BeDragonTrigger());
    public static final Supplier<SleepOnTreasureTrigger> SLEEP_ON_TREASURE = register(new SleepOnTreasureTrigger());
    public static final Supplier<MineBlockUnderLavaTrigger> MINE_BLOCK_UNDER_LAVA = register(new MineBlockUnderLavaTrigger());
    /** In order to only trigger when the item has been fully used, not just started to being used */
    public static final Supplier<UseDragonSoulTrigger> USE_DRAGON_SOUL = register(new UseDragonSoulTrigger());
    /** {@link CriteriaTriggers#USING_ITEM} is only triggered when {@link LivingEntity#startUsingItem(InteractionHand)} is called in {@link Item#use(Level, Player, InteractionHand)} */
    public static final Supplier<StopNaturalGrowthTrigger> STOP_NATURAL_GROWTH = register(new StopNaturalGrowthTrigger());
    public static final Supplier<UpgradeAbilityTrigger> UPGRADE_ABILITY = register(new UpgradeAbilityTrigger());
    public static final Supplier<ConvertItemFromAbility> CONVERT_ITEM_FROM_ABILITY = register(new ConvertItemFromAbility());
    public static final Supplier<StealFromVillagerTrigger> STEAL_FROM_VILLAGER = register(new StealFromVillagerTrigger());

    private static <T extends CriterionTrigger<?>> Supplier<T> register(final T trigger) {
        CriteriaTriggers.register(trigger);
        return () -> trigger;
    }

    public static final class TriggerRegistry {
        private TriggerRegistry() {}

        public void register(final IEventBus bus) {
            // Criteria triggers use a static registry in 1.20.1 and are registered above.
        }
    }
}
