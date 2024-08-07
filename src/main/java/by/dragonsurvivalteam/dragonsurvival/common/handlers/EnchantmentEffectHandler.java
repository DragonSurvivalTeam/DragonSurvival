package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
@EventBusSubscriber
public class EnchantmentEffectHandler {
    @SubscribeEvent
    public static void fireCrossbow(ArrowLooseEvent event) {
        Holder<Enchantment> bolas = EnchantmentUtils.getHolder(DSEnchantments.BOLAS);
        if (bolas != null && !event.isCanceled() && event.getBow().getEnchantmentLevel(bolas) > 0) {
            if (event.getBow().getItem() instanceof CrossbowItem) {
                ChargedProjectiles p = event.getBow().get(DataComponents.CHARGED_PROJECTILES);
                if (p != null) {
                    List<ItemStack> ammo = p.getItems();
                    List<ItemStack> projectiles = new ArrayList<>(List.of());
                    for (ItemStack itemStack : ammo) {
                        projectiles.add(itemStack.getItem() instanceof ArrowItem ? new ItemStack(DSItems.BOLAS.value()) : itemStack);
                    }
                    event.getBow().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(projectiles));
                }
            }
        }
    }

    @SubscribeEvent
    public static void dragonDies(LivingDeathEvent event) {
        if (DragonStateProvider.isDragon(event.getEntity()) && event.getEntity().hasEffect(DSEffects.HUNTER_OMEN)) {
            // If they are currently considered evil...
            if (event.getSource().getEntity() instanceof Player killer && event.getSource().getWeaponItem() != null) {
                int dragonsbaneLevel = EnchantmentUtils.getLevel(killer.level(), DSEnchantments.DRAGONSBANE, event.getSource().getWeaponItem());
                if (dragonsbaneLevel > 0 && event.getEntity() instanceof Player dyingPlayer) {
                    DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(event.getEntity());
                    handler.setSize(handler.getSize() - getStolenTime(handler) * dragonsbaneLevel, dyingPlayer);
                    if (DragonStateProvider.isDragon(killer)) {
                        DragonStateHandler killerHandler = DragonStateProvider.getOrGenerateHandler(killer);
                        killerHandler.setSize(killerHandler.getSize() + getStolenTime(killerHandler));
                    }
                    killer.level().playLocalSound(killer.blockPosition(), SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, SoundSource.PLAYERS, 2.0f, 1.0f, false);
                }
            }
        }
    }

    private static double getStolenTime(DragonStateHandler handler) {
        double ticksToSteal = 36000; // Steal 30 minutes per enchantment level
        return switch (handler.getLevel()) {
            case NEWBORN ->
                    (DragonLevel.YOUNG.size - DragonLevel.NEWBORN.size) / (DragonGrowthHandler.newbornToYoung * 20.0) * ticksToSteal * ServerConfig.newbornGrowthModifier;
            case YOUNG ->
                    (DragonLevel.ADULT.size - DragonLevel.YOUNG.size) / (DragonGrowthHandler.youngToAdult * 20.0) * ticksToSteal * ServerConfig.youngGrowthModifier;
            case ADULT -> {
                if (handler.getSize() > DragonLevel.ADULT.maxSize)
                    yield (60 - 40) / (DragonGrowthHandler.ancient * 20.0) * ticksToSteal * ServerConfig.maxGrowthModifier;
                yield (40 - DragonLevel.ADULT.size) / (DragonGrowthHandler.adultToAncient * 20.0) * ticksToSteal * ServerConfig.adultGrowthModifier;
            }
        };
    }
}
