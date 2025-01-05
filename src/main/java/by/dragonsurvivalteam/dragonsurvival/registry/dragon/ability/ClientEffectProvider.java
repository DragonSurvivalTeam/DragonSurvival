package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.EnderDragonMarkHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DamageModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ModifiersWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public interface ClientEffectProvider {
    record ClientData(ResourceLocation texture, Component name, Component tooltip, Component effectSource) {
        public static final Codec<ClientData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter(ClientData::texture),
                ComponentSerialization.CODEC.fieldOf("name").forGetter(ClientData::name),
                ComponentSerialization.CODEC.optionalFieldOf("tooltip", Component.empty()).forGetter(ClientData::tooltip),
                ComponentSerialization.CODEC.optionalFieldOf("effect_source", Component.empty()).forGetter(ClientData::effectSource)
        ).apply(instance, ClientData::new));

        public static ClientData from(final ServerPlayer dragon, final DragonAbilityInstance ability, final ResourceLocation effect, final Component description) {
            return from(dragon, ability, effect, description, null);
        }

        public static ClientData from(final ServerPlayer dragon, final DragonAbilityInstance ability, final ResourceLocation effect, final Component description, @Nullable final ResourceLocation customIcon) {
            ResourceLocation icon = Objects.requireNonNullElseGet(customIcon, () -> ability.getIcon().withPrefix("textures/gui/sprites/").withSuffix(".png"));
            return new ClientData(icon, Component.translatable(Translation.Type.ABILITY_EFFECT.wrap(effect)), description, dragon.getName());
        }
    }

    /** See {@link net.minecraft.client.renderer.texture.MissingTextureAtlasSprite#MISSING_TEXTURE_LOCATION} */
    ResourceLocation MISSING_TEXTURE = ResourceLocation.withDefaultNamespace("missingno");
    ClientData NONE = new ClientData(MISSING_TEXTURE, Component.literal("N/A"), Component.empty(), Component.empty());

    static List<ClientEffectProvider> getProviders() {
        Player localPlayer = DragonSurvival.PROXY.getLocalPlayer();

        if (localPlayer == null) {
            return List.of();
        }

        List<ClientEffectProvider> providers = new ArrayList<>();
        providers.addAll(localPlayer.getExistingData(DSDataAttachments.MODIFIERS_WITH_DURATION).map(ModifiersWithDuration::all).orElse(List.of()));
        providers.addAll(localPlayer.getExistingData(DSDataAttachments.DAMAGE_MODIFICATIONS).map(DamageModifications::all).orElse(List.of()));

        DragonStateHandler handler = DragonStateProvider.getData(localPlayer);

        if (handler.isDragon()) {
            FlightData flightData = FlightData.getData(localPlayer);

            if (flightData.areWingsSpread) {
                providers.add(FlightData.FLIGHT_EFFECT);
            }

            if (handler.markedByEnderDragon) {
                providers.add(EnderDragonMarkHandler.MARK_EFFECT);
            }
        }

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

    int getDuration();
    int currentDuration();
}
