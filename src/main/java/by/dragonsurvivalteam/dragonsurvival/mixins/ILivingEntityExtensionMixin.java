package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class ILivingEntityExtensionMixin {
    /** Allows proper sinking in lava when pressing shift e.g. */
    public boolean canSwimInFluidType(final FluidType fluid) {
        LivingEntity self = (LivingEntity) (Object) this;
        boolean canSwimIn = fluid == ForgeMod.WATER_TYPE.get() ? !self.isSensitiveToWater() : fluid.canSwim(self);

        if (canSwimIn) {
            return true;
        }

        return self instanceof Player player && SwimData.getData(player).canSwimIn(fluid);
    }

    public boolean canDrownInFluidType(final FluidType fluid) {
        LivingEntity self = (LivingEntity) (Object) this;

        if (self instanceof Player player) {
            // TODO :: early return needed if already false? since this may change that to true
            //         if not (in case we allow players to apply negative bonus, also don't early-return for "can swim in"?
            return SwimData.getData(player).getMaxOxygen(player, fluid) != SwimData.UNLIMITED_OXYGEN;
        }

        return fluid == ForgeMod.WATER_TYPE.get() ? !self.canBreatheUnderwater() : fluid.canDrownIn(self);
    }
}
