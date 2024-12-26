package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.extensions.ILivingEntityExtension;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ILivingEntityExtension.class)
public interface ILivingEntityExtensionMixin {
    /** Allows proper sinking in lava when pressing shift e.g. */
    @ModifyReturnValue(method = "canSwimInFluidType", at = @At("RETURN"))
    private boolean dragonSurvival$enableSwimming(boolean canSwimIn, @Local(argsOnly = true) final FluidType fluid) {
        if (canSwimIn) {
            return true;
        }

        return self() instanceof Player player && SwimData.getData(player).canSwimIn(fluid);
    }

    @ModifyReturnValue(method = "canDrownInFluidType", at = @At("RETURN"))
    private boolean dragonSurvival$handleUnlimitedOxygen(boolean canDrownIn, @Local(argsOnly = true) final FluidType fluid) {
        if (self() instanceof Player player) {
            SwimData data = SwimData.getData(player);

            if (data.canSwimIn(fluid)) {
                return data.getMaxOxygen(fluid) != SwimData.UNLIMITED_OXYGEN;
            }
        }

        return canDrownIn;
    }

    @Shadow LivingEntity self();
}
