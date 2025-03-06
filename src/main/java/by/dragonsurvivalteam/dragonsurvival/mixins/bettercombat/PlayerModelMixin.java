package by.dragonsurvivalteam.dragonsurvival.mixins.bettercombat;

import by.dragonsurvivalteam.dragonsurvival.compat.bettercombat.BetterCombat;
import com.bawnorton.mixinsquared.TargetHandler;
import dev.kosmx.playerAnim.impl.IPlayerModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Mixin(value = PlayerModel.class, priority = 2500, remap = false)
public abstract class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> implements IPlayerModel {
    public PlayerModelMixin(final ModelPart root) {
        super(root);
    }

    @TargetHandler(mixin = "dev.kosmx.playerAnim.mixin.PlayerModelMixin", name = "setEmote")
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "dev/kosmx/playerAnim/core/util/SetableSupplier.set(Ljava/lang/Object;)V"))
    private void dragonSurvival$storePlayer(final T livingEntity, float f, float g, float h, float i, float j, final CallbackInfo original, final CallbackInfo callback) {
        if (livingEntity instanceof Player player) {
            BetterCombat.CURRENT_PLAYER = player;
        }
    }
}