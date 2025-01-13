package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.DisplayType;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.EnderDragonMarkHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DamageModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.EffectModifications;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.FlightData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HarvestBonuses;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ModifiersWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.OxygenBonuses;
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
    record ClientData(ResourceLocation texture, Component name, Component effectSource) {
        public static final Codec<ClientData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("texture").forGetter(ClientData::texture),
                ComponentSerialization.CODEC.fieldOf("name").forGetter(ClientData::name),
                ComponentSerialization.CODEC.optionalFieldOf("effect_source", Component.empty()).forGetter(ClientData::effectSource)
        ).apply(instance, ClientData::new));

        public static ClientData from(final ServerPlayer dragon, final DragonAbilityInstance ability) {
            return from(dragon, ability, Optional.empty());
        }

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
        public static ClientData from(final ServerPlayer dragon, final DragonAbilityInstance ability, final Optional<ResourceLocation> customIcon) {
            ResourceLocation icon = customIcon.orElse(ability.getIcon().withPrefix("textures/gui/sprites/").withSuffix(".png"));
            return new ClientData(icon, Component.translatable(Translation.Type.ABILITY.wrap(ability.location())), dragon.getName());
        }

        public static ClientData from(final Holder<DragonPenalty> penalty) {
            return from(penalty, Optional.empty());
        }

        @SuppressWarnings({"OptionalUsedAsFieldOrParameterType", "DataFlowIssue"}) // ignore
        public static ClientData from(final Holder<DragonPenalty> penalty, final Optional<ResourceLocation> customIcon) {
            ResourceLocation icon = customIcon.orElse(penalty.value().icon().orElse(UNKNOWN_ICON));
            return new ClientData(icon, Component.translatable(Translation.Type.PENALTY.wrap(penalty.getKey().location())), Component.empty());
        }
    }

    ResourceLocation UNKNOWN_ICON = DragonSurvival.res("textures/gui/ability_effect/generic_icons/unknown.png");

    /** See {@link net.minecraft.client.renderer.texture.MissingTextureAtlasSprite#MISSING_TEXTURE_LOCATION} */
    ResourceLocation MISSING_TEXTURE = ResourceLocation.withDefaultNamespace("missingno");
    ClientData NONE = new ClientData(MISSING_TEXTURE, Component.literal("N/A"), Component.empty());

    static List<ClientEffectProvider> getProviders(boolean isInventory) {
        if (!DisplayType.isVisible(isInventory)) {
            return List.of();
        }

        Player player = DragonSurvival.PROXY.getLocalPlayer();

        if (player == null) {
            return List.of();
        }

        List<ClientEffectProvider> providers = new ArrayList<>();
        providers.addAll(player.getExistingData(DSDataAttachments.MODIFIERS_WITH_DURATION).map(ModifiersWithDuration::all).orElse(List.of()));
        providers.addAll(player.getExistingData(DSDataAttachments.DAMAGE_MODIFICATIONS).map(DamageModifications::all).orElse(List.of()));
        providers.addAll(player.getExistingData(DSDataAttachments.HARVEST_BONUSES).map(HarvestBonuses::all).orElse(List.of()));
        providers.addAll(player.getExistingData(DSDataAttachments.EFFECT_MODIFICATIONS).map(EffectModifications::all).orElse(List.of()));
        providers.addAll(player.getExistingData(DSDataAttachments.OXYGEN_BONUSES).map(OxygenBonuses::all).orElse(List.of()));

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

        providers.removeIf(ClientEffectProvider::isHidden);
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
