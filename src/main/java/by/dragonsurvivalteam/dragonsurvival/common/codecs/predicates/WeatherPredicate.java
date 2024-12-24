package by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public record WeatherPredicate(Optional<Boolean> isRaining, Optional<Boolean> isThundering) {
    public static final Codec<WeatherPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("is_raining").forGetter(WeatherPredicate::isRaining),
            Codec.BOOL.optionalFieldOf("is_thundering").forGetter(WeatherPredicate::isThundering)
    ).apply(instance, WeatherPredicate::new));

    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public boolean matches(final ServerLevel level, final Vec3 position) {
        if (isRaining.isPresent() && isRaining.get() != level.isRainingAt(BlockPos.containing(position))) {
            return false;
        }

        if (isThundering.isPresent() && isThundering.get() != level.isThundering()) {
            return false;
        }

        return true;
    }
}
