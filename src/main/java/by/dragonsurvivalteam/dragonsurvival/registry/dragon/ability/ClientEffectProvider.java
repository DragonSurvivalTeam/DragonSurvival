package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.DisplayType;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.EnderDragonMarkHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.Storage;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ClientEffectProvider {
    record ClientData(ResourceLocation id, ResourceLocation texture, Component name, Component effectSource) {
        public static final Codec<ClientData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("id").forGetter(ClientData::id),
                ResourceLocation.CODEC.fieldOf("texture").forGetter(ClientData::texture),
                ComponentSerialization.CODEC.fieldOf("name").forGetter(ClientData::name),
                ComponentSerialization.CODEC.optionalFieldOf("effect_source", Component.empty()).forGetter(ClientData::effectSource)
        ).apply(instance, ClientData::new));

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
        public static ClientData from(final ResourceLocation id, final ServerPlayer dragon, final DragonAbilityInstance ability, final Optional<ResourceLocation> customIcon) {
            ResourceLocation icon = customIcon.orElse(ability.getIcon().withPrefix("textures/gui/sprites/").withSuffix(".png"));
            return new ClientData(id, icon, Component.translatable(Translation.Type.ABILITY.wrap(ability.location())), dragon.getName());
        }

        @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "DataFlowIssue"}) // ignore
        public static ClientData from(final ResourceLocation id, final Holder<DragonPenalty> penalty, final Optional<ResourceLocation> customIcon) {
            ResourceLocation icon = customIcon.orElse(penalty.value().icon().orElse(UNKNOWN_ICON));
            return new ClientData(id, icon, Component.translatable(Translation.Type.PENALTY.wrap(penalty.getKey().location())), Component.empty());
        }
    }

    ResourceLocation UNKNOWN_ICON = DragonSurvival.res("textures/gui/ability_effect/generic_icons/unknown.png");

    /** See {@link net.minecraft.client.renderer.texture.MissingTextureAtlasSprite#MISSING_TEXTURE_LOCATION} */
    ClientData NONE = new ClientData(DragonSurvival.res("none"), DragonSurvival.MISSING_TEXTURE, Component.literal("N/A"), Component.empty());

    static List<ClientEffectProvider> getProviders(boolean isInventory) {
        boolean isVisible = DisplayType.isVisible(isInventory);

        if (!isVisible && DisplayType.ALWAYS_VISIBLE.isEmpty()) {
            return List.of();
        }

        Player player = DragonSurvival.PROXY.getLocalPlayer();

        if (player == null) {
            return List.of();
        }

        List<ClientEffectProvider> providers = new ArrayList<>();

        for (Storage<? extends ClientEffectProvider> storage : DSDataAttachments.getStorages(player, ClientEffectProvider.class)) {
            providers.addAll(storage.all());
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (handler.isDragon()) {
            FlightData flightData = FlightData.getData(player);

            if (flightData.areWingsSpread) {
                providers.add(FlightData.FLIGHT_EFFECT);
            }

            if (handler.markedByEnderDragon) {
                providers.add(EnderDragonMarkHandler.MARK_EFFECT);
            }
        }

        providers.removeIf(provider -> provider.isHidden() || !isVisible && !DisplayType.ALWAYS_VISIBLE.contains(provider.clientData().id().toString()));
        return providers;
    }

    default boolean isInfiniteDuration() {
        return getDuration() == DurationInstance.INFINITE_DURATION;
    }

    default boolean isHidden() {
        return false;
    }

    Component getDescription();
    ClientData clientData();

    int getDuration();
    int currentDuration();
}
