package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.HolderSetCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

/**
 * Only applied during data generation <br>
 * The dragon ability codec will fail if it references a missing tag <br>
 * (This applies to most vanilla tags)
 */
@Mixin(HolderSetCodec.class)
public abstract class HolderSetCodecMixin<E> {
    @Shadow private ResourceKey<? extends Registry<E>> registryKey;
    @Shadow private Codec<Either<TagKey<E>, List<Holder<E>>>> registryAwareCodec;

    /** Allows data generation before vanilla tags have been loaded into its temporary registries. */
    @Inject(method = "decode", at = @At("HEAD"), cancellable = true)
    private <T> void dragonSurvival$decodeMissingTags(final DynamicOps<T> ops, final T input, final CallbackInfoReturnable<DataResult<Pair<HolderSet<E>, T>>> callback) {
        if (!(ops instanceof RegistryOps<T> registryOps)) {
            return;
        }

        Optional<HolderGetter<E>> getter = registryOps.getter(registryKey);

        if (getter.isEmpty()) {
            return;
        }

        DataResult<Pair<HolderSet<E>, T>> result = registryAwareCodec.decode(ops, input).map(pair -> pair.mapFirst(either -> either.map(
                tag -> getter.get().get(tag).<HolderSet<E>>map(named -> named).orElseGet(() -> dragonSurvival$missingTag(tag)),
                HolderSet::direct
        )));
        callback.setReturnValue(result);
    }

    private static <E> HolderSet<E> dragonSurvival$missingTag(final TagKey<E> tag) {
        DragonSurvival.LOGGER.error("Skipping missing data generation tag [{}]", tag.location());
        return HolderSet.direct();
    }
}
