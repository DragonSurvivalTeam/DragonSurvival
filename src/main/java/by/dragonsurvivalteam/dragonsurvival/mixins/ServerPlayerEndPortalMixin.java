package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.structures.EndPlatformHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEndPortalMixin {
    @Inject(method = "createEndPlatform", at = @At("HEAD"), cancellable = true)
    private void dragonSurvival$createSpeciesEndPlatform(final ServerLevel level, final BlockPos position, final CallbackInfo callback) {
        if (EndPlatformHandler.placePlatform((ServerPlayer) (Object) this, level, position)) {
            callback.cancel();
        }
    }
}
