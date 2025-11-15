package by.dragonsurvivalteam.dragonsurvival.registry.data_components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

// TODO :: store uuid of the player to display custom skins? but what if they want to see the skin stored inside the soul (for other players)?
public record DragonSoulData(CompoundTag dragonData, CompoundTag abilityData, double scale) {
    public static final DragonSoulData EMPTY = new DragonSoulData(new CompoundTag(), new CompoundTag(), 1);

    public static final Codec<DragonSoulData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CompoundTag.CODEC.fieldOf("dragon_handler").forGetter(DragonSoulData::dragonData),
            CompoundTag.CODEC.fieldOf("magic_data").forGetter(DragonSoulData::abilityData),
            Codec.DOUBLE.fieldOf("scale").forGetter(DragonSoulData::scale)
    ).apply(instance, DragonSoulData::new));

    // FIXME 1.22 :: remove, this was only a fallback for a breaking change
    public static @Nullable DragonSoulData parseLegacy(final CompoundTag tag) {
        CompoundTag handlerData = tag.getCompound(DRAGON);

        if (handlerData.isEmpty()) {
            return null;
        }

        return new DragonSoulData(handlerData, tag.getCompound(ABILITIES), 1);
    }

    public static final String DRAGON = "dragon";
    public static final String ABILITIES = "soul";
}
