package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade.ValueBasedUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {
    public ServerPlayerMixin(final Level level, final BlockPos position, float yRot, final GameProfile profile) {
        super(level, position, yRot, profile);
    }

    /** The 'set (...) levels' command does not trigger the neoforge event */
    @Inject(method = "setExperienceLevels", at = @At("TAIL"))
    private void dragonSurvival$triggerPassiveAbilityUpgrades(int level, final CallbackInfo callback) {
        MagicData.getData(this).handleValueUpgrades(this, ValueBasedUpgrade.InputData.passive(level));
    }
}
