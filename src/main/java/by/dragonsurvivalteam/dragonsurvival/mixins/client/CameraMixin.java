package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.neoforged.neoforge.client.event.ViewportEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {
    /** {@link ViewportEvent.ComputeCameraAngles} is called too early, the {@link Camera#setPosition(double, double, double)} call would override our adjustment */
    @Inject(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setPosition(DDD)V", shift = At.Shift.AFTER))
    private void dragonSurvival$adjustCameraPosition(final BlockGetter level, final Entity entity, final boolean detached, final boolean thirdPersonReverse, final float partialTick, final CallbackInfo callback) {
        ClientDragonRenderer.adjustCamera((Camera) (Object) this);
    }
}
