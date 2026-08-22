package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public record LevelBasedBlockPredicate(BlockPredicate fallback, List<Entry> entries) {
    public static final Codec<LevelBasedBlockPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPredicate.CODEC.optionalFieldOf("fallback", BlockPredicate.alwaysTrue()).forGetter(LevelBasedBlockPredicate::fallback),
            LevelBasedBlockPredicate.Entry.CODEC.listOf().xmap(list -> {
                List<LevelBasedBlockPredicate.Entry> sorted = new ArrayList<>(list);
                Collections.sort(sorted);
                Collections.reverse(sorted);
                return sorted;
            }, Function.identity()).fieldOf("entries").forGetter(LevelBasedBlockPredicate::entries)
    ).apply(instance, LevelBasedBlockPredicate::new));

    public static LevelBasedBlockPredicate constant(final BlockPredicate value) {
        return new LevelBasedBlockPredicate(value, List.of());
    }

    public static LevelBasedBlockPredicate atLevel(final BlockPredicate value, final int level) {
        return new LevelBasedBlockPredicate(BlockPredicate.alwaysTrue(), List.of(new Entry(value, level)));
    }

    public boolean matches(final int abilityLevel, final WorldGenLevel level, final BlockPos position) {
        return get(abilityLevel).test(level, position);
    }

    public BlockPredicate get(final int abilityLevel) {
        for (Entry entry : entries) {
            if (abilityLevel >= entry.fromLevel()) {
                return entry.value;
            }
        }

        return fallback;
    }

    public record Entry(BlockPredicate value, int fromLevel) implements Comparable<Entry> {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPredicate.CODEC.fieldOf("value").forGetter(Entry::value),
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
