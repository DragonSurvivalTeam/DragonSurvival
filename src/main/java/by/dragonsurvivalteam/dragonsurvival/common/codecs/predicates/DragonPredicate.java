package by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.items.growth.StarHeartItem;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record DragonPredicate(
        Optional<HolderSet<DragonSpecies>> dragonSpecies,
        Optional<DragonStagePredicate> dragonStage,
        Optional<HolderSet<DragonBody>> dragonBody,
        Optional<StarHeartItem.State> starHeartState,
        Optional<Boolean> markedByEnderDragon
) implements EntitySubPredicate {
    public static final MapCodec<DragonPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(DragonSpecies.REGISTRY).optionalFieldOf("dragon_species").forGetter(DragonPredicate::dragonSpecies),
            DragonStagePredicate.CODEC.optionalFieldOf("stage_specific").forGetter(DragonPredicate::dragonStage),
            RegistryCodecs.homogeneousList(DragonBody.REGISTRY).optionalFieldOf("dragon_body").forGetter(DragonPredicate::dragonBody),
            StarHeartItem.State.CODEC.optionalFieldOf("star_heart_state").forGetter(DragonPredicate::starHeartState),
            Codec.BOOL.optionalFieldOf("marked_by_ender_dragon").forGetter(DragonPredicate::markedByEnderDragon)
    ).apply(instance, DragonPredicate::new));

    @Override
    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public boolean matches(@NotNull final Entity entity, @NotNull final ServerLevel level, @Nullable final Vec3 position) {
        if (!(entity instanceof ServerPlayer player)) {
            return false;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon()) {
            return false;
        }

        if (dragonSpecies().isPresent() && !dragonSpecies().get().contains(data.species())) {
            return false;
        }

        if (dragonStage().isPresent() && !dragonStage().get().matches(data.stage(), data.getSize())) {
            return false;
        }

        if (dragonBody().isPresent() && !dragonBody().get().contains(data.body())) {
            return false;
        }

        if (starHeartState().isPresent() && starHeartState().get() != data.starHeartState) {
            return false;
        }

        if(markedByEnderDragon().isPresent() && markedByEnderDragon().get() != data.markedByEnderDragon) {
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
        private Optional<StarHeartItem.State> starHeartState = Optional.empty();
        private Optional<Boolean> markedByEnderDragon = Optional.empty();

        public static DragonPredicate.Builder dragon() {
            return new DragonPredicate.Builder();
        }

        public DragonPredicate.Builder species(final Holder<DragonSpecies> dragonSpecies) {
            this.dragonSpecies = Optional.of(HolderSet.direct(dragonSpecies));
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

        public DragonPredicate.Builder stage(final Holder<DragonStage> dragonStage, final MinMaxBounds.Doubles growthPercentage) {
            this.dragonStage = Optional.of(DragonStagePredicate.Builder.start().stage(dragonStage).growth(growthPercentage).build());
            return this;
        }

        public DragonPredicate.Builder body(final Holder<DragonBody> dragonBody) {
            this.dragonBody = Optional.of(HolderSet.direct(dragonBody));
            return this;
        }

        public DragonPredicate.Builder starHeart(final StarHeartItem.State starHeartState) {
            this.starHeartState = Optional.of(starHeartState);
            return this;
        }

        public DragonPredicate.Builder markedByEnderDragon(final boolean markedByEnderDragon) {
            this.markedByEnderDragon = Optional.of(markedByEnderDragon);
            return this;
        }

        public DragonPredicate build() {
            return new DragonPredicate(dragonSpecies, dragonStage, dragonBody, starHeartState, markedByEnderDragon);
        }
    }
}
