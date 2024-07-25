package by.dragonsurvivalteam.dragonsurvival.mixins.embeddium;

import by.dragonsurvivalteam.dragonsurvival.client.VisionHandler;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.spongepowered.asm.mixin.Mixin;

/** Modify lava alpha */
@Mixin(targets = "net.minecraftforge.common.ForgeMod$3$1", remap = false)
public abstract class ForgeModLavaMixin implements IClientFluidTypeExtensions {
    @Override
    public int getTintColor() {
        if (VisionHandler.hasLavaVision()) {
            // 0x5A is 90 which is roughly the result of 255 * 0.35
            return 0x5AFFFFFF;
        }

        return -1;
    }
}
