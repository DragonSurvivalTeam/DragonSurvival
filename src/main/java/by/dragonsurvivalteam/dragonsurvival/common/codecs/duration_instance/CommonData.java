package by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

public record CommonData(ClientEffectProvider.ClientData clientData, Optional<ResourceKey<DragonAbility>> ability, Optional<UUID> source, int appliedAbilityLevel, boolean removeAutomatically) {
    public static final Codec<CommonData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ClientEffectProvider.ClientData.CODEC.fieldOf("client_data").forGetter(CommonData::clientData),
            ResourceKey.codec(DragonAbility.REGISTRY).optionalFieldOf("ability").forGetter(CommonData::ability),
            UUIDUtil.CODEC.optionalFieldOf("source").forGetter(CommonData::source),
            Codec.INT.fieldOf("applied_ability_level").forGetter(CommonData::appliedAbilityLevel),
            Codec.BOOL.optionalFieldOf("remove_automatically", false).forGetter(CommonData::removeAutomatically)
    ).apply(instance, CommonData::new));

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
    public static CommonData from(final ServerPlayer dragon, final DragonAbilityInstance ability, final Optional<ResourceLocation> customIcon, boolean removeAutomatically) {
        ClientEffectProvider.ClientData clientData = ClientEffectProvider.ClientData.from(dragon, ability, customIcon);
        return new CommonData(clientData, Optional.of(ability.key()), Optional.of(dragon.getUUID()), ability.level(), removeAutomatically);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
    public static CommonData from(final Holder<DragonPenalty> penalty, final Optional<ResourceLocation> customIcon) {
        ClientEffectProvider.ClientData clientData = ClientEffectProvider.ClientData.from(penalty, customIcon);
        return new CommonData(clientData, Optional.empty(), Optional.empty(), 1, false);
    }
}