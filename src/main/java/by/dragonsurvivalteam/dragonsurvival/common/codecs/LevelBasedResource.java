package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record LevelBasedResource(List<Entry> entries) {
    public static final Codec<LevelBasedResource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().xmap(list -> {
                List<Entry> sorted = new ArrayList<>(list);
                Collections.sort(sorted);
                return sorted.reversed();
            }, Function.identity()).fieldOf("texture_entries").forGetter(LevelBasedResource::entries)
    ).apply(instance, LevelBasedResource::new));

    public Identifier get(final int level) {
        for (Entry entry : entries) {
            if (level >= entry.fromLevel()) {
                return entry.location();
            }
        }

        // Fallback to returning the first entry (this is intended, as it happens for a single tick as the client is receiving projectile data from the server)
        return entries().getFirst().location();
    }

    public record Entry(Identifier location, int fromLevel) implements Comparable<Entry> {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("texture_resource").forGetter(Entry::location),
                ExtraCodecs.intRange(DragonAbilityInstance.MIN_LEVEL, DragonAbilityInstance.MAX_LEVEL).fieldOf("from_level").forGetter(Entry::fromLevel)
        ).apply(instance, Entry::new));

        @Override
        public int compareTo(@NotNull final LevelBasedResource.Entry other) {
            if (fromLevel < other.fromLevel()) {
                return -1;
            } else if (fromLevel > other.fromLevel()) {
                return 1;
            }

            return 0;
        }
    }
}
