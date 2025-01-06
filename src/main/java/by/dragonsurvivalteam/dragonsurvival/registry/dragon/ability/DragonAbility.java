package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedResource;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.ActionContainer;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Activation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.UpgradeType;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record DragonAbility(
        Activation activation,
        Optional<UpgradeType<?>> upgrade,
        Optional<LootItemCondition> usageBlocked,
        List<ActionContainer> actions,
        boolean canBeManuallyDisabled,
        LevelBasedResource icon
) {
    public static final ResourceKey<Registry<DragonAbility>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_abilities"));

    public static final Codec<DragonAbility> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Activation.codec().fieldOf("activation").forGetter(DragonAbility::activation),
            UpgradeType.CODEC.optionalFieldOf("upgrade").forGetter(DragonAbility::upgrade),
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("usage_blocked").forGetter(DragonAbility::usageBlocked),
            ActionContainer.CODEC.listOf().optionalFieldOf("actions", List.of()).forGetter(DragonAbility::actions),
            Codec.BOOL.optionalFieldOf("can_be_manually_disabled", true).forGetter(DragonAbility::canBeManuallyDisabled),
            LevelBasedResource.CODEC.fieldOf("icon").forGetter(DragonAbility::icon)
    ).apply(instance, instance.stable(DragonAbility::new)));

    public static final Codec<Holder<DragonAbility>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonAbility>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    public int getCooldown(int abilityLevel) {
        return activation.cooldown().map(cooldown -> cooldown.calculate(abilityLevel)).orElse(0f).intValue();
    }

    public int getChargeTime(int abilityLevel) {
        return activation.castTime().map(castTime -> castTime.calculate(abilityLevel)).orElse(0f).intValue();
    }

    public int getMaxLevel() {
        return upgrade.map(UpgradeType::maxLevel).orElse(1);
    }

    public static void validate(final RegistryAccess access) {
        StringBuilder validationError = new StringBuilder("The following stages are incorrectly defined:");
        AtomicBoolean areStagesValid = new AtomicBoolean(true);

        ResourceHelper.keys(access, REGISTRY).forEach(key -> {});

        if (!areStagesValid.get()) {
            throw new IllegalStateException(validationError.toString());
        }
    }

    public List<Component> getInfo(final Player dragon, final DragonAbilityInstance instance) {
        List<Component> info = new ArrayList<>();
        int castTime = instance.getCastTime();

        if (castTime > 0) {
            info.add(Component.translatable(LangKey.ABILITY_CAST_TIME, Functions.ticksToSeconds(castTime)));
        }

        int cooldown = instance.ability().value().getCooldown(instance.level());

        if (cooldown > 0) {
            info.add(Component.translatable(LangKey.ABILITY_COOLDOWN, Functions.ticksToSeconds(cooldown)));
        }

        instance.ability().value().activation().initialManaCost().ifPresent(cost -> info.add(Component.translatable(LangKey.ABILITY_INITIAL_MANA_COST, cost.calculate(instance.level()))));
        instance.ability().value().activation().continuousManaCost().ifPresent(cost -> info.add(Component.translatable(LangKey.ABILITY_CONTINUOUS_MANA_COST, cost.manaCost().calculate(instance.level()), DSLanguageProvider.enumValue(cost.manaCostType()))));

        if (!info.isEmpty()) {
            info.add(Component.literal("\n"));
        }

        for (ActionContainer action : actions) {
            info.addAll(action.effect().getAllEffectDescriptions(dragon, instance));
        }

        return info;
    }

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }
}
