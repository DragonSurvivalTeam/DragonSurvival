package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ClawToolHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Inventory.class)
public class InventoryMixin {

    @Shadow @Final public NonNullList<ItemStack> items;

    /**
     * Swaps the tool for the correct tool swap when querying destroy speed clientside. This is to account for things like the Efficiency enchantment correctly.
     */
    @ModifyReturnValue(method = "getDestroySpeed", at = @At(value = "RETURN"))
    private float dragonSurvival$toolSwapForDestroySpeed(float original, @Local(argsOnly = true) BlockState state) {
        Inventory inventory = (Inventory) (Object) this;

        Pair<ItemStack, Integer> data = ClawToolHandler.getDragonHarvestToolAndSlot(inventory.player, state);
        ItemStack dragonHarvestTool = data.getFirst();
        int toolSlot = data.getSecond();

        if (toolSlot != -1) {
            return dragonHarvestTool.getDestroySpeed(state);
        }

        return original;
    }
}
