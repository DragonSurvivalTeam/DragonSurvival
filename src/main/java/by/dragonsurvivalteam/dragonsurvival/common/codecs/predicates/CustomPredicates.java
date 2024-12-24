package by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** Useful general checks which are missing in the normal entity predicate */
public record CustomPredicates(
        Optional<HolderSet<FluidType>> eyeInFluid,
        Optional<WeatherPredicate> weatherPredicate
) implements EntitySubPredicate {
    public static final MapCodec<CustomPredicates> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(NeoForgeRegistries.FLUID_TYPES.key()).optionalFieldOf("eye_in_fluid").forGetter(CustomPredicates::eyeInFluid),
            WeatherPredicate.CODEC.optionalFieldOf("weather_predicate").forGetter(CustomPredicates::weatherPredicate)
    ).apply(instance, CustomPredicates::new));

    @Override
    public @NotNull MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    @Override
    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public boolean matches(@NotNull final Entity entity, @NotNull final ServerLevel level, @Nullable final Vec3 position) {
        if (!eyeInFluid.map(fluids -> fluids.stream().anyMatch(fluid -> entity.isEyeInFluidType(fluid.value()))).orElse(true)) {
            return false;
        }

        if (!weatherPredicate.map(predicate -> predicate.matches(level, entity.position())).orElse(true)) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
    public static class Builder {
        private Optional<HolderSet<FluidType>> eyeInFluid = Optional.empty();
        private Optional<Boolean> isRaining = Optional.empty();
        private Optional<Boolean> isThundering = Optional.empty();

        public static CustomPredicates.Builder start() {
            return new CustomPredicates.Builder();
        }

        public CustomPredicates.Builder eyeInFluid(final Holder<FluidType> fluid) {
            eyeInFluid = Optional.of(HolderSet.direct(fluid));
            return this;
        }

        public CustomPredicates.Builder eyeInFluid(final HolderSet<FluidType> fluids) {
            eyeInFluid = Optional.of(fluids);
            return this;
        }

        public CustomPredicates.Builder raining(boolean isRaining) {
            this.isRaining = Optional.of(isRaining);
            return this;
        }

        public CustomPredicates.Builder thundering(boolean isThundering) {
            this.isThundering = Optional.of(isThundering);
            return this;
        }

        public CustomPredicates build() {
            return new CustomPredicates(eyeInFluid, Optional.of(new WeatherPredicate(isRaining, isThundering)));
        }
    }
}
