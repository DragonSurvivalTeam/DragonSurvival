package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record LevelBasedBoolean(boolean fallback, List<Entry> entries) {
    public static final Codec<LevelBasedBoolean> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("fallback", false).forGetter(LevelBasedBoolean::fallback),
            Entry.CODEC.listOf().xmap(list -> {
                List<Entry> sorted = new ArrayList<>(list);
                sorted.sort(Collections.reverseOrder());
                return sorted;
            }, Function.identity()).fieldOf("entries").forGetter(LevelBasedBoolean::entries)
    ).apply(instance, LevelBasedBoolean::new));

    public static LevelBasedBoolean constant(boolean value) {
        return new LevelBasedBoolean(value, List.of());
    }

    public static LevelBasedBoolean atLevel(final boolean value, final int level) {
        return new LevelBasedBoolean(false, List.of(new Entry(value, level)));
    }

    public boolean calculate(final int level) {
        for (Entry entry : entries) {
            if (level >= entry.fromLevel()) {
                return entry.value();
            }
        }

        return fallback;
    }

    public record Entry(boolean value, int fromLevel) implements Comparable<Entry> {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("value").forGetter(Entry::value),
                ExtraCodecs.intRange(DragonAbilityInstance.MIN_LEVEL, DragonAbilityInstance.MAX_LEVEL).fieldOf("from_level").forGetter(Entry::fromLevel)
        ).apply(instance, Entry::new));

        @Override
        public int compareTo(@NotNull final Entry other) {
            if (fromLevel < other.fromLevel()) {
                return -1;
            } else if (fromLevel > other.fromLevel()) {
                return 1;
            }

            return 0;
        }
    }
}
