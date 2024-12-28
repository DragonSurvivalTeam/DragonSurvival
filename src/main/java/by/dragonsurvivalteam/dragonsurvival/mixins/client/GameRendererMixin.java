package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @ModifyReturnValue(method = "getNightVisionScale", at = @At(value = "RETURN"))
    private static float modifyNightVisionScale(float original) {
        return ClientConfig.stableNightVision ? 1f : original;
    }

    @ModifyExpressionValue(method = "bobHurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getLastDamageSource()Lnet/minecraft/world/damagesource/DamageSource;"))
    private DamageSource cancelBobHurtWhenNullDamageSource(DamageSource original) {
        // Cancel the bobbing effect when the player is hurt by giving a NO_FLINCH damage source if it is null
        if (original == null) {
            Holder<DamageType> noFlinch = Minecraft.getInstance().player.registryAccess().holderOrThrow(DSDamageTypes.NO_FLINCH);
            return new DamageSource(noFlinch);
        }

        return original;
    }
}