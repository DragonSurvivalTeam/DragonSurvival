package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade.InputData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    /** The 'set (...) levels' command does not trigger the neoforge event */
    @Inject(method = "setExperienceLevels", at = @At("TAIL"))
    private void dragonSurvival$triggerPassiveAbilityUpgrades(int level, final CallbackInfo callback) {
        ServerPlayer self = (ServerPlayer) (Object) this;
        MagicData.getData(self).handleAutoUpgrades(self, InputData.experienceLevels(level));
    }
}
