package by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.server.handlers.ServerFlightHandler;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record DragonPredicate(
        Optional<HolderSet<DragonSpecies>> dragonSpecies,
        Optional<DragonStagePredicate> dragonStage,
        Optional<HolderSet<DragonBody>> dragonBody,
        Optional<List<AbilityLevel>> abilityLevels,
        // TODO :: check for ability level
        Optional<Boolean> isGrowthStopped,
        Optional<Boolean> markedByEnderDragon,
        Optional<Boolean> flightWasGranted,
        Optional<Boolean> spinWasGranted,
        Optional<Boolean> isFlying
) implements EntitySubPredicate {
    public static final MapCodec<DragonPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(DragonSpecies.REGISTRY).optionalFieldOf("dragon_species").forGetter(DragonPredicate::dragonSpecies),
            DragonStagePredicate.CODEC.optionalFieldOf("stage_specific").forGetter(DragonPredicate::dragonStage),
            RegistryCodecs.homogeneousList(DragonBody.REGISTRY).optionalFieldOf("dragon_body").forGetter(DragonPredicate::dragonBody),
            AbilityLevel.CODEC.listOf().optionalFieldOf("ability_levels").forGetter(DragonPredicate::abilityLevels),
            Codec.BOOL.optionalFieldOf("is_growth_stopped").forGetter(DragonPredicate::isGrowthStopped),
            Codec.BOOL.optionalFieldOf("marked_by_ender_dragon").forGetter(DragonPredicate::markedByEnderDragon),
            Codec.BOOL.optionalFieldOf("flight_was_granted").forGetter(DragonPredicate::flightWasGranted),
            Codec.BOOL.optionalFieldOf("spin_was_granted").forGetter(DragonPredicate::spinWasGranted),
            Codec.BOOL.optionalFieldOf("is_flying").forGetter(DragonPredicate::isFlying)
    ).apply(instance, DragonPredicate::new));

    public record AbilityLevel(ResourceKey<DragonAbility> ability, MinMaxBounds.Ints level) {
        public static final Codec<AbilityLevel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(DragonAbility.REGISTRY).fieldOf("ability").forGetter(AbilityLevel::ability),
                MinMaxBounds.Ints.CODEC.fieldOf("level").forGetter(AbilityLevel::level)
        ).apply(instance, AbilityLevel::new));
    }

    @Override
    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public boolean matches(@NotNull final Entity entity, @NotNull final ServerLevel level, @Nullable final Vec3 position) {
        if (!(entity instanceof ServerPlayer player)) {
            return false;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return false;
        }

        if (dragonSpecies().isPresent() && !dragonSpecies().get().contains(handler.species())) {
            return false;
        }

        if (dragonStage().isPresent() && !dragonStage().get().matches(handler.stage(), handler.getGrowth())) {
            return false;
        }

        if (dragonBody().isPresent() && !dragonBody().get().contains(handler.body())) {
            return false;
        }

        if (abilityLevels().isPresent()) {
            MagicData magic = MagicData.getData(player);

            for (AbilityLevel abilityLevel : abilityLevels.get()) {
                DragonAbilityInstance instance = magic.getAbility(abilityLevel.ability());

                if (instance == null || !abilityLevel.level().matches(instance.level())) {
                    return false;
                }
            }
        }

        if (isGrowthStopped().isPresent() && isGrowthStopped().get() != handler.isGrowthStopped) {
            return false;
        }

        if (markedByEnderDragon().isPresent() && markedByEnderDragon().get() != handler.markedByEnderDragon) {
            return false;
        }

        if (flightWasGranted().isPresent() && flightWasGranted().get() != handler.flightWasGranted) {
            return false;
        }

        if (spinWasGranted().isPresent() && spinWasGranted().get() != handler.spinWasGranted) {
            return false;
        }

        if (isFlying().isPresent() && isFlying().get() != ServerFlightHandler.isFlying(player)) {
            return false;
        }

        return true;
    }

    @Override
    public @NotNull MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
    public static class Builder {
        private Optional<HolderSet<DragonSpecies>> dragonSpecies = Optional.empty();
        private Optional<DragonStagePredicate> dragonStage = Optional.empty();
        private Optional<HolderSet<DragonBody>> dragonBody = Optional.empty();
        private Optional<List<AbilityLevel>> abilityLevels = Optional.empty();
        private Optional<Boolean> isGrowthStopped = Optional.empty();
        private Optional<Boolean> markedByEnderDragon = Optional.empty();
        private Optional<Boolean> flightWasGranted = Optional.empty();
        private Optional<Boolean> spinWasGranted = Optional.empty();
        private Optional<Boolean> isFlying = Optional.empty();

        public static DragonPredicate.Builder dragon() {
            return new DragonPredicate.Builder();
        }

        public DragonPredicate.Builder species(final Holder<DragonSpecies> dragonSpecies) {
            this.dragonSpecies = Optional.of(HolderSet.direct(dragonSpecies));
            return this;
        }

        public DragonPredicate.Builder species(HolderSet<DragonSpecies> dragonSpecies) {
            this.dragonSpecies = Optional.of(dragonSpecies);
            return this;
        }

        public DragonPredicate.Builder stage(final DragonStagePredicate predicate) {
            this.dragonStage = Optional.of(predicate);
            return this;
        }

        public DragonPredicate.Builder stage(final Holder<DragonStage> dragonStage) {
            this.dragonStage = Optional.of(DragonStagePredicate.Builder.start().stage(dragonStage).build());
            return this;
        }

        public DragonPredicate.Builder stage(final Holder<DragonStage> dragonStage, final MinMaxBounds.Doubles progress) {
            this.dragonStage = Optional.of(DragonStagePredicate.Builder.start().stage(dragonStage).progress(progress).build());
            return this;
        }

        public DragonPredicate.Builder body(final Holder<DragonBody> dragonBody) {
            this.dragonBody = Optional.of(HolderSet.direct(dragonBody));
            return this;
        }

        public DragonPredicate.Builder abilityLevels(final AbilityLevel... abilityLevels) {
            this.abilityLevels = Optional.of(List.of(abilityLevels));
            return this;
        }

        public DragonPredicate.Builder growthStopped(final boolean isGrowthStopped) {
            this.isGrowthStopped = Optional.of(isGrowthStopped);
            return this;
        }

        public DragonPredicate.Builder markedByEnderDragon(final boolean markedByEnderDragon) {
            this.markedByEnderDragon = Optional.of(markedByEnderDragon);
            return this;
        }

        public DragonPredicate.Builder flightWasGranted(final boolean flightWasGranted) {
            this.flightWasGranted = Optional.of(flightWasGranted);
            return this;
        }

        public DragonPredicate.Builder spinWasGranted(final boolean spinWasGranted) {
            this.spinWasGranted = Optional.of(spinWasGranted);
            return this;
        }

        public DragonPredicate.Builder isFlying(final boolean isFlying) {
            this.isFlying = Optional.of(isFlying);
            return this;
        }

        public DragonPredicate build() {
            return new DragonPredicate(dragonSpecies, dragonStage, dragonBody, abilityLevels, isGrowthStopped, markedByEnderDragon, flightWasGranted, spinWasGranted, isFlying);
        }
    }
}
