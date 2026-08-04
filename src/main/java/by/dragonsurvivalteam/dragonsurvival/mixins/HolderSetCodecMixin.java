package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.resources.HolderSetCodec;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Only applied during data generation <br>
 * The dragon ability codec will fail if it references a missing tag <br>
 * (This applies to most vanilla tags)
 */
@Mixin(HolderSetCodec.class)
public class HolderSetCodecMixin {
    // FIXME :: 1.21.1 backport issue? -> lookupTag does not exist
//    @ModifyReturnValue(method = "lookupTag", at = @At("RETURN"))
//    private static <E> DataResult<HolderSet<E>> dragonSurvival$skipError(final DataResult<HolderSet<E>> original) {
//        if (original.error().isPresent()) {
//            DragonSurvival.LOGGER.error("Skipping data generation error [{}]", original.error().get().message());
//            return DataResult.success(HolderSet.direct());
//        }
//
//        return original;
//    }
}
