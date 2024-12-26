package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.List;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record DragonEmoteSet(List<DragonEmote> emotes) {
    public static final ResourceKey<Registry<DragonEmoteSet>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_emote_set"));

    public static final Codec<DragonEmoteSet> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DragonEmote.CODEC.listOf().fieldOf("emotes").forGetter(DragonEmoteSet::emotes)
    ).apply(instance, DragonEmoteSet::new));

    public static final Codec<Holder<DragonEmoteSet>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonEmoteSet>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }
}
