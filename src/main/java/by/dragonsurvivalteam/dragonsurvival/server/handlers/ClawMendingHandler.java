package by.dragonsurvivalteam.dragonsurvival.server.handlers;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber
public class ClawMendingHandler {
    @SubscribeEvent
    public void playerXPPickUp(PlayerXpEvent.PickupXp evt) {
        Player player = evt.getEntity();
        if (player instanceof ServerPlayer p) {
            handleClawMending(p, evt);
        }
    }

    private static void handleClawMending(ServerPlayer player, PlayerXpEvent.PickupXp evt) {
        ExperienceOrb orb = evt.getOrb();
        player.takeXpDelay = 2;
        player.take(orb, 1);
        orb.value = repairClawItems(player, orb.value);

        if (orb.value <= 0) {
            evt.isCanceled();
            orb.discard();
        }
    }

    private static int repairClawItems(ServerPlayer player, int value) {
        Holder<Enchantment> mendingHolder = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING);
        NonNullList<ItemStack> clawItems = ClawInventoryData.getData(player).getContainer().getItems();
        List<ItemStack> mendable = new ArrayList<>();
        for(ItemStack stack : clawItems) {
            if (!stack.isEmpty() && stack.getEnchantmentLevel(mendingHolder) > 0 && stack.isDamaged()) {
                mendable.add(stack);
            }
        }

        Optional<ItemStack> optional = Util.getRandomSafe(mendable, player.getRandom());
        if (optional.isPresent()) {
            ItemStack itemstack = optional.get();
            int i = EnchantmentHelper.modifyDurabilityToRepairFromXp(player.serverLevel(), itemstack, (int)((float)value * itemstack.getXpRepairRatio()));
            int j = Math.min(i, itemstack.getDamageValue());
            itemstack.setDamageValue(itemstack.getDamageValue() - j);
            if (j > 0) {
                int k = value - j * value / i;
                if (k > 0) {
                    return repairClawItems(player, k);
                }
            }

            return 0;
        } else {
            return value;
        }
    }
}
