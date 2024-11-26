package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Active;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Passive;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Upgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.Targeting;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record DragonAbility(
        Either<Active, Passive> activation,
        Optional<Upgrade> upgrade,
        Optional<EntityPredicate> usageBlocked,
        List<Targeting> effects,
        ResourceLocation icon,
        Component description
) {
    public static final ResourceKey<Registry<DragonAbility>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_abilities"));

    public static final Codec<DragonAbility> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.either(Active.CODEC, Passive.CODEC).fieldOf("activation").forGetter(DragonAbility::activation),
            Upgrade.CODEC.optionalFieldOf("upgrade").forGetter(DragonAbility::upgrade),
            EntityPredicate.CODEC.optionalFieldOf("usage_blocked").forGetter(DragonAbility::usageBlocked), // TODO :: e.g. when the ability is not supposed to be used underwater
            Targeting.CODEC.listOf().optionalFieldOf("effects", List.of()).forGetter(DragonAbility::effects),
            ResourceLocation.CODEC.fieldOf("icon").forGetter(DragonAbility::icon),
            // TODO: How do we handle descriptions that are fed various values from the ability itself?
            ComponentSerialization.CODEC.fieldOf("description").forGetter(DragonAbility::description)
    ).apply(instance, instance.stable(DragonAbility::new)));

    public static final Codec<Holder<DragonAbility>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonAbility>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    public static final int MAX_ACTIVE = 4;
    public static final int MAX_PASSIVE = 8;

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }

    public static void update(@Nullable final HolderLookup.Provider provider) {
        validate(provider);
    }

    private static void validate(@Nullable final HolderLookup.Provider provider) {
        StringBuilder nextAbilityCheck = new StringBuilder("The following abilities are incorrectly defined:");
        AtomicBoolean areAbilitiesValid = new AtomicBoolean(true);

        ResourceHelper.keys(provider, REGISTRY).forEach(key -> {
            //noinspection OptionalGetWithoutIsPresent -> ignore
            Holder.Reference<DragonAbility> ability = ResourceHelper.get(provider, key, REGISTRY).get();

//            if(ability.value().slot.type != Type.ACTIVE && ability.value().effect.isPresent()) {
//                nextAbilityCheck.append("\n- Ability [").append(key.location()).append("] has an active effect but is not defined as active");
//                areAbilitiesValid.set(false);
//            }
//
//            if(ability.value().slot.type == Type.PASSIVE && ability.value().passiveModifiers.isEmpty()) {
//                nextAbilityCheck.append("\n- Ability [").append(key.location()).append("] has no passive modifiers but is defined as passive");
//                areAbilitiesValid.set(false);
//            }
//
//            if(ability.value().slot.type == Type.PASSIVE && ability.value().upgradeCost.isPresent()) {
//                nextAbilityCheck.append("\n- Ability [").append(key.location()).append("] has an upgrade cost but is defined as passive");
//                areAbilitiesValid.set(false);
//            }
//
//            if(ability.value().slot.type != Type.INNATE && ability.value().penalties.isEmpty()) {
//                nextAbilityCheck.append("\n- Ability [").append(key.location()).append("] has a penalty effect but is not defined as innate");
//                areAbilitiesValid.set(false);
//            }
        });

        if (!areAbilitiesValid.get()) {
            throw new IllegalStateException(nextAbilityCheck.toString());
        }
    }
}