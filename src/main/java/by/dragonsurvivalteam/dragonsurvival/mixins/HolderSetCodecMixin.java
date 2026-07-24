package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.HolderSetCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Only applied during data generation <br>
 * The dragon ability codec will fail if it references a missing tag <br>
 * (This applies to most vanilla tags)
 */
@Mixin(HolderSetCodec.class)
public class HolderSetCodecMixin {
    @ModifyReturnValue(method = "lookupTag", at = @At("RETURN"))
    private static <E> DataResult<HolderSet<E>> dragonSurvival$skipError(final DataResult<HolderSet<E>> original) {
        if (original.error().isPresent()) {
            DragonSurvival.LOGGER.error("Skipping data generation error [{}]", original.error().get().message());
            return DataResult.success(HolderSet.empty());
        }

        return original;
    }
}
