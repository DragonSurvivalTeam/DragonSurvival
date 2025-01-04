package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.List;

public record SourceOfMagicData(List<Consumable> consumables, List<ResourceKey<DragonSpecies>> applicableSpecies) {
    public static final Codec<SourceOfMagicData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Consumable.CODEC.listOf().fieldOf("consumables").forGetter(SourceOfMagicData::consumables),
            ResourceKey.codec(DragonSpecies.REGISTRY).listOf().fieldOf("applicable_species").forGetter(SourceOfMagicData::applicableSpecies)
    ).apply(instance, SourceOfMagicData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SourceOfMagicData> STREAM_CODEC = StreamCodec.composite(
            Consumable.STREAM_CODEC.apply(ByteBufCodecs.list()), SourceOfMagicData::consumables,
            ResourceKey.streamCodec(DragonSpecies.REGISTRY).apply(ByteBufCodecs.list()), SourceOfMagicData::applicableSpecies,
            SourceOfMagicData::new
    );

    public record Consumable(Item item, int duration) {
        public static final Codec<Consumable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Consumable::item),
                Codec.INT.fieldOf("duration").forGetter(Consumable::duration)
        ).apply(instance, Consumable::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Consumable> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.ITEM), Consumable::item,
                ByteBufCodecs.VAR_INT, Consumable::duration,
                Consumable::new
        );
    }
}
