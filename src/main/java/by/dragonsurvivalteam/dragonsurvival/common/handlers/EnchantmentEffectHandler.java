package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class EnchantmentEffectHandler {
    @SubscribeEvent
    public static void fireCrossbow(ArrowLooseEvent event) {
        if (!(event.getBow().getItem() instanceof CrossbowItem)) {
            return;
        }

        if (EnchantmentUtils.getLevel(event.getLevel(), DSEnchantments.BOLAS, event.getBow()) > 0) {
            ChargedProjectiles charged = event.getBow().get(DataComponents.CHARGED_PROJECTILES);

            if (charged != null) {
                List<ItemStack> ammo = charged.getItems();
                List<ItemStack> projectiles = new ArrayList<>();

                for (ItemStack itemStack : ammo) {
                    projectiles.add(itemStack.getItem() instanceof ArrowItem ? new ItemStack(DSItems.BOLAS.value()) : itemStack);
                }

                event.getBow().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(projectiles));
            }
        }
    }

    @SubscribeEvent
    public static void handleDragonsbaneEnchantment(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (!victim.hasEffect(DSEffects.HUNTER_OMEN)) {
            return;
        }

        ItemStack weapon = event.getSource().getWeaponItem();

        if (weapon == null) {
            return;
        }

        int enchantmentLevel = EnchantmentUtils.getLevel(attacker.level(), DSEnchantments.DRAGONSBANE, weapon);

        if (enchantmentLevel > 0) {
            DragonStateHandler victimData = DragonStateProvider.getData(victim);

            if (!victimData.isDragon()) {
                return;
            }

            victimData.setDesiredSize(victim, victimData.getSize() - getStolenTime(victimData) * enchantmentLevel);
            DragonStateHandler attackerData = DragonStateProvider.getData(attacker);

            if (attackerData.isDragon()) {
                // TODO :: why doesn't this scale with the enchantment level
                attackerData.setDesiredSize(attacker, attackerData.getSize() + getStolenTime(attackerData));
            }

            attacker.level().playLocalSound(attacker.blockPosition(), SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, SoundSource.PLAYERS, 2, 1, false);
        }
    }

    private static double getStolenTime(DragonStateHandler handler) {
        int ticksToSteal = Functions.minutesToTicks(30); // TODO :: make this configurable in the enchantment
        DragonStage level = handler.getStage().value();
        return level.ticksToSize(ticksToSteal);
    }
}
