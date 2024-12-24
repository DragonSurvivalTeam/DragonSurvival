package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.neoforge.registries.DeferredRegister;

// We need this class for the effects that are caused by dragon breath (not actual potions)
public class DSPotions {
    public static final DeferredRegister<Potion> DS_POTIONS = DeferredRegister.create(BuiltInRegistries.POTION, DragonSurvival.MODID);

    /* Effect duration is normally divided by 4 */
    //public static final Holder<Potion> STORM_BREATH = DS_POTIONS.register("storm_breath", () -> new Potion(new MobEffectInstance(DSEffects.CHARGED, Functions.secondsToTicks(10) * 4)));

    /* Effect duration is normally divided by 4 */
   // public static final Holder<Potion> FOREST_BREATH = DS_POTIONS.register("forest_breath", () -> new Potion(new MobEffectInstance(DSEffects.DRAIN, Functions.secondsToTicks(10) * 4)));

    /* Effect duration is normally divided by 4 */
   // public static final Holder<Potion> CAVE_BREATH = DS_POTIONS.register("cave_breath", () -> new Potion(new MobEffectInstance(DSEffects.BURN, Functions.secondsToTicks(10) * 4)));
}
