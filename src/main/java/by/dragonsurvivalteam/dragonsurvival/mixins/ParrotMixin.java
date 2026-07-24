package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Parrot.class)
public class ParrotMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    public void dragonSurvival$preventParrotInteractionForDragons(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (DragonStateProvider.isDragon(player)) {
            cir.setReturnValue(InteractionResult.PASS);
        }
    }
}
