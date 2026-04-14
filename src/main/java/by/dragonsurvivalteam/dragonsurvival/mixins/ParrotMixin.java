package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.world.entity.animal.parrot.Parrot")
public class ParrotMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$preventParrotInteractionForDragons(final Player player, final InteractionHand hand, final CallbackInfoReturnable<InteractionResult> callback) {
        if (DragonStateProvider.isDragon(player)) {
            callback.setReturnValue(InteractionResult.PASS);
        }
    }
}
