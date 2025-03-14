package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.HitByProjectileTrigger;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.ItemUsedTrigger;
import by.dragonsurvivalteam.dragonsurvival.server.containers.slots.ClawToolSlot;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber
public class DragonPenaltyHandler {
    @SubscribeEvent
    public static void applyPenalties(final PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (serverPlayer.isSpectator() || serverPlayer.isCreative()) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(serverPlayer);

        if (!handler.isDragon()) {
            PenaltySupply.clear(serverPlayer);
            return;
        }

        for (Holder<DragonPenalty> penalty : handler.species().value().penalties()) {
            if (penalty.value().trigger().hasCustomTrigger()) {
                continue;
            }

            penalty.value().apply(serverPlayer, penalty);
        }
    }

    @SubscribeEvent
    public static void applyItemConsumedPenalties(final LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(serverPlayer);

        if (!handler.isDragon()) {
            return;
        }

        for (Holder<DragonPenalty> penalty : handler.species().value().penalties()) {
            if (penalty.value().trigger() instanceof ItemUsedTrigger trigger && trigger.test(event.getItem())) {
                penalty.value().apply(serverPlayer, penalty);
            }
        }
    }

    @SubscribeEvent
    public static void applyHitByProjectilePenalties(final ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hitResult && hitResult.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(serverPlayer);

        if (!handler.isDragon()) {
            return;
        }

        for (Holder<DragonPenalty> penalty : handler.species().value().penalties()) {
            //noinspection DeconstructionCanBeUsed -> spotless is too stupid to handle this
            if (penalty.value().trigger() instanceof HitByProjectileTrigger trigger && event.getProjectile().getType().is(trigger.projectiles())) {
                penalty.value().apply(serverPlayer, penalty);
            }
        }
    }

    @SubscribeEvent // Prevent the player from equipping blacklisted armor (or from mixing light and dark dragon armor)
    public static void preventEquipment(final ItemStackedOnOtherEvent event) {
        ItemStack stack = event.getStackedOnItem(); // FIXME :: this is probably a neoforge bug, this should be carried item -> might be changed in the future
        Player player = event.getPlayer();

        if (stack.isEmpty()) {
            return;
        }

        // Will have to see what type of slots modded inventories may use
        if (!(event.getSlot() instanceof ArmorSlot) && !(event.getSlot() instanceof ClawToolSlot)) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (data.isDragon() && data.species().value().isItemBlacklisted(stack.getItem())) {
            event.setCanceled(true);
        }
    }
}