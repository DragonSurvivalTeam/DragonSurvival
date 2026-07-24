package by.dragonsurvivalteam.dragonsurvival.mixins.sodium;

import by.dragonsurvivalteam.dragonsurvival.client.render.VisionHandler;
import by.dragonsurvivalteam.dragonsurvival.client.util.ClientFluidTypeExtensionsWrapper;
import net.minecraftforge.client.ClientForgeMod;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ClientForgeMod.class)
@SuppressWarnings("UnstableApiUsage")
public abstract class ClientNeoForgeModMixin {
    @ModifyArg(method = "onRegisterClientExtensions", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/extensions/common/RegisterClientExtensionsEvent;registerFluidType(Lnet/minecraftforge/client/extensions/common/IClientFluidTypeExtensions;[Lnet/minecraftforge/fluids/FluidType;)V", ordinal = 0))
    private static IClientFluidTypeExtensions dragonSurvival$modifyWater(final IClientFluidTypeExtensions original) {
        return new ClientFluidTypeExtensionsWrapper(original, VisionHandler.VisionType.WATER);
    }

    @ModifyArg(method = "onRegisterClientExtensions", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/extensions/common/RegisterClientExtensionsEvent;registerFluidType(Lnet/minecraftforge/client/extensions/common/IClientFluidTypeExtensions;[Lnet/minecraftforge/fluids/FluidType;)V", ordinal = 1))
    private static IClientFluidTypeExtensions dragonSurvival$modifyLava(final IClientFluidTypeExtensions original) {
        return new ClientFluidTypeExtensionsWrapper(original, VisionHandler.VisionType.LAVA);
    }
}
