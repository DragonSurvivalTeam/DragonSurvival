package by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.Storage;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/** Useful general checks which are missing in the normal entity predicate */
public record CustomPredicates(
        Optional<HolderSet<FluidType>> eyeInFluid,
        Optional<WeatherPredicate> weatherPredicate,
        Optional<MinMaxBounds.Ints> sunLightLevel,
        Optional<ResourceLocation> hasAbilityEffect,
        Optional<NearbyEntityPredicate> isNearbyEntity
) implements EntitySubPredicate {
    public static final MapCodec<CustomPredicates> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(NeoForgeRegistries.FLUID_TYPES.key()).optionalFieldOf("eye_in_fluid").forGetter(CustomPredicates::eyeInFluid),
            WeatherPredicate.CODEC.optionalFieldOf("weather_predicate").forGetter(CustomPredicates::weatherPredicate),
            MinMaxBounds.Ints.CODEC.optionalFieldOf("sun_light_level").forGetter(CustomPredicates::sunLightLevel),
            ResourceLocation.CODEC.optionalFieldOf("has_ability_effect").forGetter(CustomPredicates::hasAbilityEffect),
            NearbyEntityPredicate.CODEC.optionalFieldOf("is_nearby_entity").forGetter(CustomPredicates::isNearbyEntity)
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

        if (!sunLightLevel.map(light -> light.matches(getSunLightLevel(entity))).orElse(true)) {
            return false;
        }

        if (hasAbilityEffect.isPresent()) {
            ResourceLocation abilityEffect = hasAbilityEffect.get();
            boolean isPresent = false;

            for (Storage<?> storage : DSDataAttachments.getStorages(entity)) {
                if (storage.all().stream().anyMatch(entry -> entry.id().equals(abilityEffect))) {
                    isPresent = true;
                    break;
                }
            }

            if (!isPresent) {
                return false;
            }
        }

        if (!isNearbyEntity.map(predicate -> predicate.matches(level, position)).orElse(true)) {
            return false;
        }

        return true;
    }

    public static int getSunLightLevel(final Entity entity) {
        int light = entity.level().getBrightness(LightLayer.SKY, entity.blockPosition()) - entity.level().getSkyDarken();
        // This reduces the light level of the level (which usually starts at 15) depending on the sun position
        return (int) Math.round(light * Functions.getSunPosition(entity));
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
    public static class Builder {
        private Optional<HolderSet<FluidType>> eyeInFluid = Optional.empty();
        private Optional<Boolean> isRaining = Optional.empty();
        private Optional<Boolean> isThundering = Optional.empty();
        private Optional<Boolean> isSnowing = Optional.empty();
        private Optional<Boolean> isRainingOrSnowing = Optional.empty();
        private Optional<MinMaxBounds.Ints> sunLightLevel = Optional.empty();
        private Optional<ResourceLocation> hasAbilityEffect = Optional.empty();
        private Optional<NearbyEntityPredicate> isNearbyEntity = Optional.empty();

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

        public CustomPredicates.Builder snowing(boolean isSnowing) {
            this.isSnowing = Optional.of(isSnowing);
            return this;
        }

        public CustomPredicates.Builder rainingOrSnowing(boolean isRainingOrSnowing) {
            this.isRainingOrSnowing = Optional.of(isRainingOrSnowing);
            return this;
        }

        public CustomPredicates.Builder sunLightLevel(int atLeast) {
            this.sunLightLevel = Optional.of(MinMaxBounds.Ints.atLeast(atLeast));
            return this;
        }

        public CustomPredicates.Builder hasAbilityEffect(final ResourceLocation abilityEffect) {
            this.hasAbilityEffect = Optional.of(abilityEffect);
            return this;
        }

        public CustomPredicates.Builder isNearbyEntity(final NearbyEntityPredicate isNearbyEntity) {
            this.isNearbyEntity = Optional.of(isNearbyEntity);
            return this;
        }

        public CustomPredicates build() {
            return new CustomPredicates(eyeInFluid, Optional.of(new WeatherPredicate(isRaining, isThundering, isSnowing, isRainingOrSnowing)), sunLightLevel, hasAbilityEffect, isNearbyEntity);
        }
    }
}
