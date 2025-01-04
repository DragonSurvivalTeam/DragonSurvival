package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.minecraft.world.level.block.entity.vault.VaultSharedData;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VaultBlockEntity.Server.class)
public class VaultBlockEntityServerMixin {
    @ModifyReturnValue(method = "isValidToInsert", at = @At(value = "RETURN"))
    private static boolean dragonSurvival$isValidToInsert(boolean original, final VaultConfig config, final ItemStack stack) {
        if (stack.getComponents().get(DSDataComponents.TARGET_POSITION.get()) != null) {
            // Skip the components check if it is our vaults and keys
            return ItemStack.isSameItem(stack, config.keyItem()) && stack.getCount() >= config.keyItem().getCount();
        }
        return original;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private static void dragonSurvival$preventStateUpdatingDelay(ServerLevel pLevel, BlockPos pPos, BlockState pState, VaultConfig pConfig, VaultServerData pServerData, VaultSharedData pSharedData, CallbackInfo ci) {
        if (ServerConfig.forceStateUpdatingOnVaults) {
            pServerData.pauseStateUpdatingUntil(0);
        }
    }
}
