package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ManaHandling(double manaXpConversion, double manaPerLevel, double maxManaFromLevels) {
    public static final ManaHandling DEFAULT = new ManaHandling(0.1, 0.25, 9);

    public static final Codec<ManaHandling> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            MiscCodecs.doubleRange(0, Double.MAX_VALUE).optionalFieldOf("mana_xp_conversion", 0d).forGetter(ManaHandling::manaXpConversion),
            MiscCodecs.doubleRange(0, Double.MAX_VALUE).optionalFieldOf("mana_per_level", 0d).forGetter(ManaHandling::manaPerLevel),
            MiscCodecs.doubleRange(0, Double.MAX_VALUE).optionalFieldOf("max_mana_from_levels", 0d).forGetter(ManaHandling::maxManaFromLevels)
    ).apply(instance, ManaHandling::new));
}
