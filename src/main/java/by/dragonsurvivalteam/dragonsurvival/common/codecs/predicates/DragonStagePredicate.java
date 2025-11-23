package by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;

import java.util.Optional;

public record DragonStagePredicate(Optional<HolderSet<DragonStage>> dragonStage, Optional<MinMaxBounds.Doubles> growthPercentage, Optional<MinMaxBounds.Doubles> growth) {
    public static final Codec<DragonStagePredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(DragonStage.REGISTRY).optionalFieldOf("dragon_stage").forGetter(DragonStagePredicate::dragonStage),
            MiscCodecs.percentageBounds().optionalFieldOf("growth_percentage").forGetter(DragonStagePredicate::growthPercentage),
            MinMaxBounds.Doubles.CODEC.optionalFieldOf("growth").forGetter(DragonStagePredicate::growth)
    ).apply(instance, DragonStagePredicate::new));

    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public boolean matches(final Holder<DragonStage> stage, double growth) {
        if (stage == null) {
            return false;
        }

        if (dragonStage().isPresent() && !dragonStage().get().contains(stage)) {
            return false;
        }

        if (growthPercentage().isPresent() && !growthPercentage().get().matches(stage.value().getProgress(growth))) {
            return false;
        }

        if (growth().isPresent() && !growth().get().matches(growth)) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
    public static class Builder {
        private Optional<HolderSet<DragonStage>> dragonStage = Optional.empty();
        private Optional<MinMaxBounds.Doubles> progress = Optional.empty();
        private Optional<MinMaxBounds.Doubles> growth = Optional.empty();

        public static DragonStagePredicate.Builder start() {
            return new DragonStagePredicate.Builder();
        }

        public DragonStagePredicate.Builder stage(final Holder<DragonStage> dragonStage) {
            this.dragonStage = Optional.of(HolderSet.direct(dragonStage));
            return this;
        }

        public DragonStagePredicate.Builder progress(final MinMaxBounds.Doubles progress) {
            this.progress = Optional.of(progress);
            return this;
        }

        public DragonStagePredicate.Builder progressAtLeast(double percentage) {
            this.progress = Optional.of((MinMaxBounds.Doubles.atLeast(percentage)));
            return this;
        }

        public DragonStagePredicate.Builder growth(final MinMaxBounds.Doubles growth) {
            this.growth = Optional.of(growth);
            return this;
        }

        public DragonStagePredicate.Builder growthBetween(double min, double max) {
            this.growth = Optional.of(MinMaxBounds.Doubles.between(min, max));
            return this;
        }

        public DragonStagePredicate.Builder growthAtLeast(double min) {
            this.growth = Optional.of(MinMaxBounds.Doubles.atLeast(min));
            return this;
        }

        public DragonStagePredicate.Builder growthAtMost(double max) {
            this.growth = Optional.of(MinMaxBounds.Doubles.atLeast(max));
            return this;
        }

        public DragonStagePredicate build() {
            return new DragonStagePredicate(dragonStage, progress, growth);
        }
    }
}