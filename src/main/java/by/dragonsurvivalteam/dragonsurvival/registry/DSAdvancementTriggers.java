package by.dragonsurvivalteam.dragonsurvival.registry;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import by.dragonsurvivalteam.dragonsurvival.common.criteria.*;
import java.util.function.Supplier;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("unused")
public class DSAdvancementTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> DS_TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, MODID);

    public static final Supplier<BeDragonTrigger> BE_DRAGON = DS_TRIGGERS.register(
            "be_dragon", BeDragonTrigger::new
    );
    public static final Supplier<SleepOnTreasureTrigger> SLEEP_ON_TREASURE = DS_TRIGGERS.register(
            "sleep_on_treasure", SleepOnTreasureTrigger::new
    );
    public static final Supplier<MineBlockUnderLavaTrigger> MINE_BLOCK_UNDER_LAVA = DS_TRIGGERS.register(
            "mine_block_under_lava", MineBlockUnderLavaTrigger::new
    );
    public static final Supplier<UseDragonSoulTrigger> USE_DRAGON_SOUL = DS_TRIGGERS.register(
            "use_dragon_soul", UseDragonSoulTrigger::new
    );
    public static final Supplier<UseStarHeartTrigger> USE_STAR_HEART = DS_TRIGGERS.register(
            "use_star_heart", UseStarHeartTrigger::new
    );
    public static final Supplier<UpgradeAbilityTrigger> UPGRADE_ABILITY = DS_TRIGGERS.register(
            "upgrade_ability", UpgradeAbilityTrigger::new
    );
}
