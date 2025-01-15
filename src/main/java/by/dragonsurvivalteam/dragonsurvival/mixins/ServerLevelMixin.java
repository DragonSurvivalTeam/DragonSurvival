package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonTreasureHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.TreasureRestData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
    @Shadow public abstract List<ServerPlayer> players();

    /** {@link PlayerWakeUpEvent} exists but for that we'd have to properly fake 'isSleeping' */
    @Inject(method = "wakeUpAllPlayers", at = @At(value = "HEAD"))
    private void dragonSurvival$applyEffectsAfterSleep(final CallbackInfo callback) {
        ServerLevel self = (ServerLevel) (Object) this;

        self.players().forEach(player -> {
            TreasureRestData data = TreasureRestData.getData(player);

            if (data.isResting()) {
                DragonTreasureHandler.EFFECTS_ON_SLEEP.forEach(effect -> effect.applyEffects(player, data.nearbyTreasure));
            }
        });
    }
}
