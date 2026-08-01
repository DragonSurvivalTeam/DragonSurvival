package by.dragonsurvivalteam.dragonsurvival.mixins.bettercombat;

import by.dragonsurvivalteam.dragonsurvival.compat.bettercombat.BetterCombat;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerModel.class, priority = 2500)
public abstract class PlayerModelMixin<T extends LivingEntity> extends HumanoidModel<T> {
    public PlayerModelMixin(final ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim", at = @At("HEAD"))
    private void dragonSurvival$storePlayer(final T livingEntity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, final CallbackInfo callback) {
        if (livingEntity instanceof Player player) {
            BetterCombat.CURRENT_PLAYER = player;
        }
    }
}
