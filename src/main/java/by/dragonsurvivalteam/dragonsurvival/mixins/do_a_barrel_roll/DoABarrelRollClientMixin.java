package by.dragonsurvivalteam.dragonsurvival.mixins.do_a_barrel_roll;

import by.dragonsurvivalteam.dragonsurvival.compat.do_a_barrel_roll.DoABarrelRollCompat;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import nl.enjarai.doabarrelroll.DoABarrelRollClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Lets Do a Barrel Roll treat Dragon Survival gliding as Elytra flight. */
@Mixin(value = DoABarrelRollClient.class, remap = false)
public abstract class DoABarrelRollClientMixin {
    @ModifyExpressionValue(
        method = "isFallFlying",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;isFallFlying()Z",
            remap = false
        ),
        remap = false
    )
    private static boolean dragonSurvival$enableDragonGliding(final boolean original) {
        LocalPlayer player = Minecraft.getInstance().player;
        return original || DoABarrelRollCompat.shouldEnableDragonFlight(player);
    }
}
