package by.dragonsurvivalteam.dragonsurvival.registry.data_components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public record DragonSoulData(CompoundTag dragonData, CompoundTag abilityData) {
    public static final DragonSoulData EMPTY = new DragonSoulData(new CompoundTag(), new CompoundTag());

    public static final Codec<DragonSoulData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CompoundTag.CODEC.fieldOf("dragon_handler").forGetter(DragonSoulData::dragonData),
            CompoundTag.CODEC.fieldOf("magic_data").forGetter(DragonSoulData::abilityData)
    ).apply(instance, DragonSoulData::new));

    // FIXME 1.22 :: remove, this was only a fallback for a breaking change
    public static @Nullable DragonSoulData parse(final CompoundTag tag) {
        CompoundTag handlerData = tag.getCompound(DRAGON);

        if (handlerData.isEmpty()) {
            return null;
        }

        return new DragonSoulData(handlerData, tag.getCompound(ABILITIES));
    }

    public static final String DRAGON = "dragon";
    public static final String ABILITIES = "soul";
}
