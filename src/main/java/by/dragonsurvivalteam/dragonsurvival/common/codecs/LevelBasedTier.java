package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public record LevelBasedTier(List<Entry> entries) {
    private static final List<HarvestTier> LEGACY_TIERS = List.of(HarvestTier.WOOD, HarvestTier.STONE, HarvestTier.IRON, HarvestTier.DIAMOND, HarvestTier.NETHERITE);

    public static final Codec<LevelBasedTier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().fieldOf("tiers").forGetter(LevelBasedTier::entries)
    ).apply(instance, LevelBasedTier::new));

    public LevelBasedTier {
        List<Entry> sorted = new ArrayList<>(entries);
        Collections.sort(sorted);

        List<Entry> resolved = new ArrayList<>(sorted.size());

        for (int index = 0; index < sorted.size(); index++) {
            Entry entry = sorted.get(index);
            HarvestTier fallbackTier = defaultTier(index);
            HarvestTier tier = entry.tier().orElse(fallbackTier);
            resolved.add(new Entry(tier, entry.fromLevel()));
        }

        Collections.reverse(resolved);
        entries = List.copyOf(resolved);
    }

    private static HarvestTier defaultTier(final int index) {
        return LEGACY_TIERS.get(Math.min(index, LEGACY_TIERS.size() - 1));
    }

    public @Nullable HarvestTier get(final int level) {
        for (Entry entry : entries) {
            if (level >= entry.fromLevel()) {
                return entry.tier().orElse(null);
            }
        }

        return null;
    }

    public enum HarvestTier implements StringRepresentable {
        WOOD(2.0F, "Wood"),
        STONE(4.0F, "Stone"),
        IRON(6.0F, "Iron"),
        DIAMOND(8.0F, "Diamond"),
        GOLD(12.0F, "Gold"),
        NETHERITE(9.0F, "Netherite");

        public static final Codec<HarvestTier> CODEC = StringRepresentable.fromEnum(HarvestTier::values);

        private final float speed;
        private final String englishName;
        private final String serializedName;

        HarvestTier(final float speed, final String englishName) {
            this.speed = speed;
            this.englishName = englishName;
            serializedName = name().toLowerCase(Locale.ENGLISH);
        }

        public float getSpeed() {
            return speed;
        }

        public String translationKey() {
            return "enum.tiers." + serializedName;
        }

        public String englishName() {
            return englishName;
        }

        public Component getDisplayName() {
            return Component.translatable(translationKey());
        }

        @Override
        public @NotNull String getSerializedName() {
            return serializedName;
        }
    }

    public record Entry(Optional<HarvestTier> tier, int fromLevel) implements Comparable<Entry> {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                HarvestTier.CODEC.optionalFieldOf("tier").forGetter(Entry::tier),
                ExtraCodecs.intRange(DragonAbilityInstance.MIN_LEVEL, DragonAbilityInstance.MAX_LEVEL).fieldOf("from_level").forGetter(Entry::fromLevel)
        ).apply(instance, Entry::new));

        public Entry(final HarvestTier tier, final int fromLevel) {
            this(Optional.of(tier), fromLevel);
        }

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
