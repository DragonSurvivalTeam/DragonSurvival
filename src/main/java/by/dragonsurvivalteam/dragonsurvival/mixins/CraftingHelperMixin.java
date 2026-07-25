package by.dragonsurvivalteam.dragonsurvival.mixins;

import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CraftingHelper.class, remap = false)
public abstract class CraftingHelperMixin {
    @Inject(method = "getItemStack", at = @At("HEAD"))
    private static void dragonSurvival$copyResultId(
            final JsonObject json,
            final boolean readNbt,
            final CallbackInfoReturnable<ItemStack> callback
    ) {
        if (json.has("id") && !json.has("item")) {
            json.add("item", json.get("id"));
        }
    }
}
