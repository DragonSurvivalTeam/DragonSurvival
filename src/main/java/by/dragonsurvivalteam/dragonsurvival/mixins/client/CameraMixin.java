package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(Camera.class)
public abstract class CameraMixin {
    // We need to adjust the distance at which blocks are checked for collision to prevent the camera from thinking it is blocked
    @ModifyArgs(method = "getMaxZoom", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;add(DDD)Lnet/minecraft/world/phys/Vec3;"))
    private void dragonSurvival$adjustCameraPosition(Args args) {
        float scale = Math.min(1.0f, Minecraft.getInstance().player.getScale());
        args.set(0, (double)args.get(0) * scale);
        args.set(1, (double)args.get(1) * scale);
        args.set(2, (double)args.get(2) * scale);
    }
}
