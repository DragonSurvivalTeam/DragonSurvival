package by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record WeatherPredicate(Optional<Boolean> isRaining, Optional<Boolean> isThundering, Optional<Boolean> isSnowing, Optional<Boolean> isRainingOrSnowing) {
    public static final Codec<WeatherPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("is_raining").forGetter(WeatherPredicate::isRaining),
            Codec.BOOL.optionalFieldOf("is_thundering").forGetter(WeatherPredicate::isThundering),
            Codec.BOOL.optionalFieldOf("is_snowing").forGetter(WeatherPredicate::isSnowing),
            // Exists so we don't have to retrieve the biome twice (snow is basically just rain in cold biomes)
            Codec.BOOL.optionalFieldOf("is_raining_or_snowing").forGetter(WeatherPredicate::isRainingOrSnowing)
    ).apply(instance, WeatherPredicate::new));

    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public boolean matches(final ServerLevel level, final Vec3 position) {
        if (isRainingOrSnowing.isPresent() && isRainingOrSnowing.get() != isRainingOrSnowing(level, BlockPos.containing(position))) {
            return false;
        }

        if (isRaining.isPresent() && isRaining.get() != level.isRainingAt(BlockPos.containing(position))) {
            return false;
        }

        if (isThundering.isPresent() && isThundering.get() != level.isThundering()) {
            return false;
        }

        if (isSnowing.isPresent() && isSnowing.get() != isSnowing(level, BlockPos.containing(position))) {
            return false;
        }

        return true;
    }

    private boolean isSnowing(final ServerLevel level, final BlockPos position) {
        if (!level.isRaining()) {
            return false;
        }

        if (!level.canSeeSky(position)) {
            // This also checked for 'Level#isRainingAt'
            return false;
        }

        Biome biome = level.getBiome(position).value();
        return biome.getPrecipitationAt(position) == Biome.Precipitation.SNOW;
    }

    private boolean isRainingOrSnowing(final ServerLevel level, final BlockPos position) {
        if (!level.isRaining()) {
            return false;
        }

        if (!level.canSeeSky(position)) {
            // This also checked for 'Level#isRainingAt'
            return false;
        }

        Biome biome = level.getBiome(position).value();
        return biome.getPrecipitationAt(position) == Biome.Precipitation.RAIN || biome.getPrecipitationAt(position) == Biome.Precipitation.SNOW;
    }
}
