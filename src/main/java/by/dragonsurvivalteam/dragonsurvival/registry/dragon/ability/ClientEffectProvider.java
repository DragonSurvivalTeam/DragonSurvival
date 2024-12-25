package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DamageModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ModifiersWithDuration;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClientEffectProvider {
    record ClientData(ResourceLocation texture, Component tooltip, Optional<UUID> effectSource) {
        public static final Codec<ClientData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter(ClientData::texture),
                ComponentSerialization.CODEC.optionalFieldOf("tooltip", Component.empty()).forGetter(ClientData::tooltip),
                UUIDUtil.CODEC.optionalFieldOf("effect_source").forGetter(ClientData::effectSource)
        ).apply(instance, ClientData::new));
    }

    /** See {@link net.minecraft.client.renderer.texture.MissingTextureAtlasSprite#MISSING_TEXTURE_LOCATION} */
    ResourceLocation MISSING_TEXTURE = ResourceLocation.withDefaultNamespace("missingno");
    ClientData NONE = new ClientData(MISSING_TEXTURE, Component.empty(), Optional.empty());

    static List<ClientEffectProvider> getProviders() {
        Player localPlayer = DragonSurvival.PROXY.getLocalPlayer();

        if (localPlayer == null) {
            return List.of();
        }

        List<ClientEffectProvider> providers = new ArrayList<>();
        providers.addAll(localPlayer.getExistingData(DSDataAttachments.MODIFIERS_WITH_DURATION).map(ModifiersWithDuration::all).orElse(List.of()));
        providers.addAll(localPlayer.getExistingData(DSDataAttachments.DAMAGE_MODIFICATIONS).map(DamageModifications::all).orElse(List.of()));
        providers.removeIf(ClientEffectProvider::isInvisible);

        return providers;
    }

    default boolean isInfiniteDuration() {
        return getDuration() == DurationInstance.INFINITE_DURATION;
    }

    default boolean isInvisible() {
        return false;
    }

    ClientData clientData();
    ResourceLocation id();

    int getDuration();
    int currentDuration();
}
