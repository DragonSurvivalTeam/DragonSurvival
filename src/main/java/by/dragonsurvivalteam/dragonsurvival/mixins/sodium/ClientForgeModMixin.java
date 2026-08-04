package by.dragonsurvivalteam.dragonsurvival.mixins.sodium;

import net.minecraftforge.client.ClientForgeMod;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientForgeMod.class)
public abstract class ClientForgeModMixin {
    // FIXME 1.21.1 backport issue? -> methods do not exist anymore but it's probably not even needed in 1.20.1?
//    @ModifyArg(method = "onRegisterClientExtensions", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/extensions/common/RegisterClientExtensionsEvent;registerFluidType(Lnet/minecraftforge/client/extensions/common/IClientFluidTypeExtensions;[Lnet/minecraftforge/fluids/FluidType;)V", ordinal = 0))
//    private static IClientFluidTypeExtensions dragonSurvival$modifyWater(final IClientFluidTypeExtensions original) {
//        return new ClientFluidTypeExtensionsWrapper(original, VisionHandler.VisionType.WATER);
//    }
//
//    @ModifyArg(method = "onRegisterClientExtensions", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/extensions/common/RegisterClientExtensionsEvent;registerFluidType(Lnet/minecraftforge/client/extensions/common/IClientFluidTypeExtensions;[Lnet/minecraftforge/fluids/FluidType;)V", ordinal = 1))
//    private static IClientFluidTypeExtensions dragonSurvival$modifyLava(final IClientFluidTypeExtensions original) {
//        return new ClientFluidTypeExtensionsWrapper(original, VisionHandler.VisionType.LAVA);
//    }
}
