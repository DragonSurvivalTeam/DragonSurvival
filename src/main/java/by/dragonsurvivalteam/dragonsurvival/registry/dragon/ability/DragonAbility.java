package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.Activation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.PassiveActivation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.UpgradeType;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber
public record DragonAbility(
        Activation activation,
        Optional<UpgradeType<?>> upgrade,
        Optional<LootItemCondition> usageBlocked,
        List<ActionContainer> actions,
        boolean canBeManuallyDisabled,
        LevelBasedResource icon
) {
    @Translation(comments = "§6■ Trigger:§r %s")
    private static final String ACTIVATION_TRIGGER = Translation.Type.GUI.wrap("ability.activation_trigger");

    public static final ResourceKey<Registry<DragonAbility>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_ability"));

    public static final Codec<DragonAbility> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Activation.CODEC.fieldOf("activation").forGetter(DragonAbility::activation),
            UpgradeType.CODEC.optionalFieldOf("upgrade").forGetter(DragonAbility::upgrade),
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("usage_blocked").forGetter(DragonAbility::usageBlocked),
            ActionContainer.CODEC.listOf().optionalFieldOf("actions", List.of()).forGetter(DragonAbility::actions),
            Codec.BOOL.optionalFieldOf("can_be_manually_disabled", true).forGetter(DragonAbility::canBeManuallyDisabled),
            LevelBasedResource.CODEC.fieldOf("icon").forGetter(DragonAbility::icon)
    ).apply(instance, instance.stable(DragonAbility::new)));

    public static final Codec<Holder<DragonAbility>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonAbility>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    public int getMaxLevel() {
        return upgrade.map(UpgradeType::maxLevel).orElse(1);
    }

    /**
     * Handle validation that cannot be done through the codec </br>
     * Since they require more context
     */
    public static void validate(final RegistryAccess access) {
        StringBuilder errorMessage = new StringBuilder("The following abilities are incorrectly defined:");
        AtomicBoolean isDefinitionValid = new AtomicBoolean(true);

        ResourceHelper.keys(access, REGISTRY).forEach(key -> {
            //noinspection OptionalGetWithoutIsPresent -> ignore
            Holder.Reference<DragonAbility> ability = ResourceHelper.get(access, key).get();
            boolean isPassive = ability.value().activation().type() == Activation.Type.PASSIVE;

            ability.value().actions.forEach(action -> {
                if (isPassive && action.triggerPoint() != ActionContainer.TriggerPoint.DEFAULT) {
                    // Passive abilities have no cast / charge time and no channeling
                    errorMessage.append("\n").append(key).append(" has a non-default action trigger point set on a passive ability");
                    isDefinitionValid.set(false);
                }

                if (ability.value().activation().type() != Activation.Type.CHANNELED && action.triggerPoint() == ActionContainer.TriggerPoint.CHANNEL_COMPLETION) {
                    errorMessage.append("\n").append(key).append(" has a channel completion action trigger point set on a non-channeling ability");
                    isDefinitionValid.set(false);
                }
            });
        });

        if (!isDefinitionValid.get()) {
            throw new IllegalStateException(errorMessage.toString());
        }
    }

    public List<Component> getInfo(final Player dragon, final DragonAbilityInstance instance) {
        List<Component> info = new ArrayList<>();

        if (activation instanceof PassiveActivation passive) {
            info.add(Component.translatable(ACTIVATION_TRIGGER, DSLanguageProvider.enumValue(passive.trigger().type())));
        }

        int castTime = activation.getCastTime(instance.level());

        if (castTime > 0) {
            info.add(Component.translatable(LangKey.ABILITY_CAST_TIME, DSColors.dynamicValue(Functions.ticksToSeconds(castTime))));
        }

        int cooldown = instance.ability().value().activation.getCooldown(instance.level());

        if (cooldown > 0) {
            info.add(Component.translatable(LangKey.ABILITY_COOLDOWN, DSColors.dynamicValue(Functions.ticksToSeconds(cooldown))));
        }

        float initialManaCost = instance.ability().value().activation().getInitialManaCost(instance.level());

        if (initialManaCost > 0) {
            info.add(Component.translatable(LangKey.ABILITY_INITIAL_MANA_COST, DSColors.dynamicValue(initialManaCost)));
        }

        instance.ability().value().activation().continuousManaCost().ifPresent(cost -> info.add(Component.translatable(LangKey.ABILITY_CONTINUOUS_MANA_COST, DSColors.dynamicValue(cost.manaCost().calculate(instance.level())), DSLanguageProvider.enumValue(cost.manaCostType()))));

        for (ActionContainer action : actions) {
            List<MutableComponent> descriptions = action.effect().getAllEffectDescriptions(dragon, instance);

            for (MutableComponent description : descriptions) {
                if (!info.isEmpty()) {
                    info.add(Component.empty());
                }

                info.add(description);
            }
        }

        return info;
    }

    public void tickDefaultActions(final ServerPlayer player, final DragonAbilityInstance instance, final int currentTick) {
        actions.forEach(action -> {
            if (action.triggerPoint() == ActionContainer.TriggerPoint.DEFAULT) {
                action.tick(player, instance, currentTick);
            }
        });
    }

    public void tickChargingActions(final ServerPlayer player, final DragonAbilityInstance instance, final int currentTick) {
        actions.forEach(action -> {
            if (action.triggerPoint() == ActionContainer.TriggerPoint.CHARGING) {
                action.tick(player, instance, currentTick);
            }
        });
    }

    public void tickChannelingEndActions(final ServerPlayer player, final DragonAbilityInstance instance, final int currentTick) {
        actions.forEach(action -> {
            if (action.triggerPoint() == ActionContainer.TriggerPoint.CHARGING) {
                action.tick(player, instance, currentTick);
            }
        });
    }

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }
}
