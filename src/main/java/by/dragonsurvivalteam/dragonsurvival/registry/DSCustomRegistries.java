package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Activation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger.ActivationTrigger;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AbilityBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.AbilityEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.UpgradeType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.PenaltyEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.PenaltyTrigger;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects.ProjectileBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects.ProjectileEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.targeting.ProjectileTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.projectile.world_effects.ProjectileWorldEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;

public final class DSCustomRegistries {
    private DSCustomRegistries() {}

    public static void register(final IEventBus bus) {
        bus.addListener(DSCustomRegistries::registerRegistries);
        bus.addListener(DSCustomRegistries::registerEntries);
    }

    private static void registerRegistries(final NewRegistryEvent event) {
        Activation.register(event);
        ActivationTrigger.register(event);
        AbilityBlockEffect.register(event);
        AbilityEntityEffect.register(event);
        AbilityTargeting.register(event);
        UpgradeType.register(event);
        PenaltyEffect.register(event);
        PenaltyTrigger.register(event);
        ProjectileBlockEffect.register(event);
        ProjectileEntityEffect.register(event);
        ProjectileTargeting.register(event);
        ProjectileWorldEffect.register(event);
    }

    private static void registerEntries(final RegisterEvent event) {
        Activation.registerEntries(event);
        ActivationTrigger.registerEntries(event);
        AbilityBlockEffect.registerEntries(event);
        AbilityEntityEffect.registerEntries(event);
        AbilityTargeting.registerEntries(event);
        UpgradeType.registerEntries(event);
        PenaltyEffect.registerEntries(event);
        PenaltyTrigger.registerEntries(event);
        ProjectileBlockEffect.registerEntries(event);
        ProjectileEntityEffect.registerEntries(event);
        ProjectileTargeting.registerEntries(event);
        ProjectileWorldEffect.registerEntries(event);
    }
}
