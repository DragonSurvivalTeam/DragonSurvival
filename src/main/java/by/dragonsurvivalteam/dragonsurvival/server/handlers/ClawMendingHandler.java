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
        //ensure we are on server
        if (player instanceof ServerPlayer p) {
            handleClawMending(p, evt);
        }
    }

    private static void handleClawMending(ServerPlayer player, PlayerXpEvent.PickupXp evt) {
        ExperienceOrb orb = evt.getOrb();
        player.takeXpDelay = 2;
        player.take(orb, 1);
        
        //repair method returns the XP remaining in the orb after mending
        orb.value = repairClawItems(player, orb.value);

        //if orb has no XP left, discard it and mark the event as canceled
        if (orb.value <= 0) {
            evt.isCanceled();
            orb.discard();
        }
    }

    //based on a modified version of Minecraft 1.21.1's
    //ExperienceOrb.class repairPlayerItems() method
    private static int repairClawItems(ServerPlayer player, int value) {
        //make list of all Claw slot Items with Mending which are also damaged
        Holder<Enchantment> mendingHolder = player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.MENDING);
        NonNullList<ItemStack> clawItems = ClawInventoryData.getData(player).getContainer().getItems();
        List<ItemStack> mendable = new ArrayList<>();
        for(ItemStack stack : clawItems) {
            if (!stack.isEmpty() && stack.getEnchantmentLevel(mendingHolder) > 0 && stack.isDamaged()) {
                mendable.add(stack);
            }
        }

        //pick a random item to mend
        Optional<ItemStack> optional = Util.getRandomSafe(mendable, player.getRandom());
        
        //use the Vanilla Mending algorithm on the selected item
        if (optional.isPresent()) {
            ItemStack itemstack = optional.get();
            int canRepairAmt = EnchantmentHelper.modifyDurabilityToRepairFromXp(player.serverLevel(), itemstack, (int)((float)value * itemstack.getXpRepairRatio()));
            int actualRepairAmt = Math.min(canRepairAmt, itemstack.getDamageValue());
            itemstack.setDamageValue(itemstack.getDamageValue() - actualRepairAmt);
            if (actualRepairAmt > 0) {
                //subtract actualRepairAmt/canRepairAmt from value
                int k = value - value * actualRepairAmt / canRepairAmt;
                if (k > 0) {
                    //call recursively until we run out of either XP or Items to mend
                    return repairClawItems(player, k);
                }
            }
            //we ran out of XP
            return 0;
        } else {
            //if no more items to mend, return remaining XP value
            return value;
        }
    }
}
