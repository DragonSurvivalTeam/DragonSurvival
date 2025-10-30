package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AbilityBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.AbilityEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@EventBusSubscriber
public interface AbilityTargeting {
    String EFFECT_HEADER = "#HEADER#";
    NumberFormat FORMAT = Functions.getFormat(2);

    ResourceKey<Registry<MapCodec<? extends AbilityTargeting>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("ability_targeting"));
    Registry<MapCodec<? extends AbilityTargeting>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<AbilityTargeting> CODEC = REGISTRY.byNameCodec().dispatch("target_type", AbilityTargeting::codec, Function.identity());

    static Either<BlockTargeting, EntityTargeting> block(final List<AbilityBlockEffect> effects) {
        return block(null, effects);
    }

    static Either<BlockTargeting, EntityTargeting> block(final LootItemCondition targetConditions, final List<AbilityBlockEffect> effects) {
        return Either.left(new BlockTargeting(Optional.ofNullable(targetConditions), effects));
    }

    static Either<BlockTargeting, EntityTargeting> entity(final List<AbilityEntityEffect> effects, final TargetingMode targetingMode) {
        return entity(null, effects, targetingMode);
    }

    static Either<BlockTargeting, EntityTargeting> entity(final LootItemCondition targetConditions, final List<AbilityEntityEffect> effects, final TargetingMode targetingMode) {
        return Either.right(new EntityTargeting(Optional.ofNullable(targetConditions), effects, targetingMode));
    }

    record BlockTargeting(Optional<LootItemCondition> targetConditions, List<AbilityBlockEffect> effects) {
        public static final Codec<BlockTargeting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MiscCodecs.conditional(LootItemCondition.DIRECT_CODEC).optionalFieldOf("target_conditions").forGetter(BlockTargeting::targetConditions),
                ConditionalOps.decodeListWithElementConditions(AbilityBlockEffect.CODEC).fieldOf("block_effect").forGetter(BlockTargeting::effects)
        ).apply(instance, BlockTargeting::new));

        public boolean matches(final ServerPlayer dragon, final BlockPos position) {
            return targetConditions.map(condition -> condition.test(Condition.blockContext(dragon, position))).orElse(true);
        }
    }

    record EntityTargeting(Optional<LootItemCondition> targetConditions, List<AbilityEntityEffect> effects, TargetingMode targetingMode) {
        public static final Codec<EntityTargeting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MiscCodecs.conditional(LootItemCondition.DIRECT_CODEC).optionalFieldOf("target_conditions").forGetter(EntityTargeting::targetConditions),
                ConditionalOps.decodeListWithElementConditions(AbilityEntityEffect.CODEC).fieldOf("entity_effect").forGetter(EntityTargeting::effects),
                TargetingMode.CODEC.fieldOf("targeting_mode").forGetter(EntityTargeting::targetingMode)
        ).apply(instance, EntityTargeting::new));

        public boolean matches(final ServerPlayer dragon, final Entity entity, final Vec3 position) {
            return targetConditions.map(condition -> condition.test(Condition.abilityContext(dragon, entity, position))).orElse(true);
        }
    }

    static <T extends AbilityTargeting> Products.P1<RecordCodecBuilder.Mu<T>, Either<BlockTargeting, EntityTargeting>> codecStart(final RecordCodecBuilder.Instance<T> instance) {
        return instance.group(Codec.either(BlockTargeting.CODEC, EntityTargeting.CODEC).fieldOf("applied_effects").forGetter(AbilityTargeting::target));
    }

    @SubscribeEvent
    static void register(final NewRegistryEvent event) {
        event.register(REGISTRY);
    }

    @SubscribeEvent
    static void registerEntries(final RegisterEvent event) {
        if (event.getRegistry() == REGISTRY) {
            event.register(REGISTRY_KEY, DragonSurvival.res("area"), () -> AreaTarget.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("dragon_breath"), () -> DragonBreathTarget.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("looking_at"), () -> LookingAtTarget.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("self"), () -> SelfTarget.CODEC);
            event.register(REGISTRY_KEY, DragonSurvival.res("disc"), () -> DiscTarget.CODEC);
        }
    }

    default List<MutableComponent> getAllEffectDescriptions(final Player dragon, final DragonAbilityInstance ability) {
        if (!ability.isUsable()) {
            return List.of();
        }

        // FIXME :: need to adjust this so they can be grouped into the respective action container trigger points

        List<MutableComponent> descriptions = new ArrayList<>();
        MutableComponent targetDescription = getDescription(dragon, ability);

        target().ifLeft(blockTargeting -> blockTargeting.effects().forEach(effect -> {
            List<MutableComponent> abilityEffectDescriptions = effect.getDescription(dragon, ability);

            if (!effect.getDescription(dragon, ability).isEmpty()) {
                descriptions.addAll(abilityEffectDescriptions.stream().map(description -> format(description, targetDescription)).toList());
            }
        })).ifRight(entityTargeting -> entityTargeting.effects().forEach(effect -> {
            List<MutableComponent> abilityEffectDescriptions = effect.getDescription(dragon, ability);

            if (!effect.getDescription(dragon, ability).isEmpty()) {
                if (this instanceof SelfTarget) {
                    descriptions.addAll(effect.getDescription(dragon, ability).stream().map(this::format).toList());
                } else {
                    descriptions.addAll(abilityEffectDescriptions.stream().map(description -> format(description, targetDescription)).toList());
                }
            }

        }));

        return descriptions;
    }

    private MutableComponent format(final MutableComponent description) {
        return Component.literal(EFFECT_HEADER).append(Component.literal("\n")).append(description);
    }

    private MutableComponent format(final MutableComponent description, final MutableComponent targetDescription) {
        return format(description).append(Component.literal("\n\n")).append(targetDescription);
    }

    // FIXME :: may not be needed anymore
    default void remove(final ServerPlayer dragon, final DragonAbilityInstance ability) { /* Nothing to do */ }

    MutableComponent getDescription(final Player dragon, final DragonAbilityInstance ability);

    void apply(final ServerPlayer dragon, final DragonAbilityInstance ability);

    MapCodec<? extends AbilityTargeting> codec();

    Either<BlockTargeting, EntityTargeting> target();
}
