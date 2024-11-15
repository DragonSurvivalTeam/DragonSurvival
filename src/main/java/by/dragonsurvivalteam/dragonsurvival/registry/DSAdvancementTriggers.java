package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.criteria.*;
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
    public static final DeferredRegister<CriterionTrigger<?>> DS_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, DragonSurvival.MODID);

    public static final Supplier<BeDragonTrigger> BE_DRAGON = DS_TRIGGERS.register("be_dragon", BeDragonTrigger::new);
    public static final Supplier<SleepOnTreasureTrigger> SLEEP_ON_TREASURE = DS_TRIGGERS.register("sleep_on_treasure", SleepOnTreasureTrigger::new);
    public static final Supplier<MineBlockUnderLavaTrigger> MINE_BLOCK_UNDER_LAVA = DS_TRIGGERS.register("mine_block_under_lava", MineBlockUnderLavaTrigger::new);
    /** In order to only trigger when the item has been fully used, not just started to being used */
    public static final Supplier<UseDragonSoulTrigger> USE_DRAGON_SOUL = DS_TRIGGERS.register("use_dragon_soul", UseDragonSoulTrigger::new);
    /** {@link CriteriaTriggers#USING_ITEM} is only triggered when {@link LivingEntity#startUsingItem(InteractionHand)} is called in {@link Item#use(Level, Player, InteractionHand)} */
    public static final Supplier<UseStarHeartTrigger> USE_STAR_HEART = DS_TRIGGERS.register("use_star_heart", UseStarHeartTrigger::new);
    public static final Supplier<UpgradeAbilityTrigger> UPGRADE_ABILITY = DS_TRIGGERS.register("upgrade_ability", UpgradeAbilityTrigger::new);
    public static final Supplier<ConvertPotatoTrigger> CONVERT_POTATO = DS_TRIGGERS.register("convert_potato", ConvertPotatoTrigger::new);
}
