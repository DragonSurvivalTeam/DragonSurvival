package by.dragonsurvivalteam.dragonsurvival.mixins.sodium;

import by.dragonsurvivalteam.dragonsurvival.client.render.VisionHandler;
import by.dragonsurvivalteam.dragonsurvival.client.util.ClientFluidTypeExtensionsWrapper;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(value = FluidType.class, remap = false)
public abstract class ClientForgeModMixin {
    @WrapOperation(method = "initClient", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fluids/FluidType;initializeClient(Ljava/util/function/Consumer;)V"))
    private void dragonSurvival$wrapVanillaFluidExtensions(final FluidType instance, final Consumer<IClientFluidTypeExtensions> consumer, final Operation<Void> original) {
        VisionHandler.VisionType visionType = switch (instance.getDescriptionId()) {
            case "block.minecraft.water" -> VisionHandler.VisionType.WATER;
            case "block.minecraft.lava" -> VisionHandler.VisionType.LAVA;
            default -> null;
        };

        if (visionType == null) {
            original.call(instance, consumer);
            return;
        }

        original.call(instance, (Consumer<IClientFluidTypeExtensions>) extensions -> consumer.accept(new ClientFluidTypeExtensionsWrapper(extensions, visionType)));
    }
}
