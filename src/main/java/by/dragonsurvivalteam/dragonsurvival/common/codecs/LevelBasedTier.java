package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Tiers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record LevelBasedTier(List<Entry> entries) {
    public static final Codec<LevelBasedTier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().xmap(list -> {
                List<Entry> sorted = new ArrayList<>(list);
                Collections.sort(sorted);
                return sorted.reversed();
            }, Function.identity()).fieldOf("tiers").forGetter(LevelBasedTier::entries)
    ).apply(instance, LevelBasedTier::new));

    public @Nullable Tiers get(final int level) {
        for (Entry entry : entries) {
            if (level >= entry.fromLevel()) {
                return entry.tier();
            }
        }

        return null;
    }

    public record Entry(Tiers tier, int fromLevel) implements Comparable<Entry> {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MiscCodecs.enumCodec(Tiers.class).fieldOf("tier").forGetter(Entry::tier),
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
