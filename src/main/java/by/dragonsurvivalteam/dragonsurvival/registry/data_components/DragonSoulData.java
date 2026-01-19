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

    public static final String DRAGON = "dragon";
    public static final String ABILITIES = "soul";
}
