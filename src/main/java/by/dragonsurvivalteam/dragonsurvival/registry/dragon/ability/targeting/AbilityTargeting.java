package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AbilityBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.AbilityEntityEffect;
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
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public interface AbilityTargeting {
    ResourceKey<Registry<MapCodec<? extends AbilityTargeting>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("targeting"));
    Registry<MapCodec<? extends AbilityTargeting>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<AbilityTargeting> CODEC = REGISTRY.byNameCodec().dispatch("target_type", AbilityTargeting::codec, Function.identity());

    enum EntityTargetingMode {
        @Translation(type = Translation.Type.ENUM, comments = "all entities")
        TARGET_ALL,
        @Translation(type = Translation.Type.ENUM, comments = "enemies")
        TARGET_ENEMIES,
        @Translation(type = Translation.Type.ENUM, comments = "allies")
        TARGET_ALLIES;

        public Component translation() {
            return DSLanguageProvider.enumValue(this);
        }
    }

    static Either<BlockTargeting, EntityTargeting> block(final List<AbilityBlockEffect> effects) {
        return block(null, effects);
    }

    static Either<BlockTargeting, EntityTargeting> block(final LootItemCondition targetConditions, final List<AbilityBlockEffect> effects) {
        return Either.left(new BlockTargeting(Optional.ofNullable(targetConditions), effects));
    }

    static Either<BlockTargeting, EntityTargeting> entity(final List<AbilityEntityEffect> effects, final EntityTargetingMode targetingMode) {
        return entity(null, effects, targetingMode);
    }

    static Either<BlockTargeting, EntityTargeting> entity(final LootItemCondition targetConditions, final List<AbilityEntityEffect> effects, final EntityTargetingMode targetingMode) {
        return Either.right(new EntityTargeting(Optional.ofNullable(targetConditions), effects, targetingMode));
    }

    record BlockTargeting(Optional<LootItemCondition> targetConditions, List<AbilityBlockEffect> effect) {
        public static final Codec<BlockTargeting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.DIRECT_CODEC.optionalFieldOf("target_conditions").forGetter(BlockTargeting::targetConditions),
                AbilityBlockEffect.CODEC.listOf().fieldOf("block_effect").forGetter(BlockTargeting::effect)
        ).apply(instance, BlockTargeting::new));

        public boolean matches(final ServerPlayer dragon, final BlockPos position) {
            return targetConditions.map(condition -> condition.test(Condition.blockContext(dragon, position))).orElse(true);
        }
    }

    record EntityTargeting(Optional<LootItemCondition> targetConditions, List<AbilityEntityEffect> effects, EntityTargetingMode targetingMode) {
        public static final Codec<EntityTargeting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                LootItemCondition.DIRECT_CODEC.optionalFieldOf("target_conditions").forGetter(EntityTargeting::targetConditions),
                AbilityEntityEffect.CODEC.listOf().fieldOf("entity_effect").forGetter(EntityTargeting::effects),
                Codec.STRING.xmap(EntityTargetingMode::valueOf, EntityTargetingMode::name).fieldOf("entity_targeting_mode").forGetter(EntityTargeting::targetingMode)
        ).apply(instance, EntityTargeting::new));

        public boolean matches(final ServerPlayer dragon, final Entity entity, final Vec3 position) {
            return targetConditions.map(condition -> condition.test(Condition.playerContext(dragon, entity, position))).orElse(true);
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
        }
    }

    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    default boolean isEntityRelevant(final ServerPlayer dragon, final EntityTargeting targeting, final Entity entity) {
        if (targeting.targetingMode == EntityTargetingMode.TARGET_ALL) {
            return true;
        }

        if (targeting.targetingMode == EntityTargetingMode.TARGET_ENEMIES && (dragon == entity || isFriendly(dragon, entity))) {
            return false;
        }

        if (targeting.targetingMode == EntityTargetingMode.TARGET_ALLIES && !(dragon == entity || isFriendly(dragon, entity))) {
            return false;
        }

        return true;
    }

    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    private boolean isFriendly(final ServerPlayer dragon, final Entity entity) {
        if (entity instanceof Player otherPlayer && !dragon.canHarmPlayer(otherPlayer)) {
            return true;
        }

        if (entity instanceof TamableAnimal tamable && tamable.getOwner() instanceof Player otherPlayer && (dragon == otherPlayer || !dragon.canHarmPlayer(otherPlayer))) {
            return true;
        }

        // TODO :: 'canHarmPlayer' returns true if friendly fire is enabled - do we want this behaviour?
        return false;
    }

    default List<MutableComponent> getAllEffectDescriptions(final Player dragon, final DragonAbilityInstance ability) {
        if (!ability.isUsable()) {
            return List.of();
        }

        List<MutableComponent> descriptions = new ArrayList<>();
        MutableComponent targetDescription = getDescription(dragon, ability);

        target().ifLeft(blockTargeting -> blockTargeting.effect().forEach(effect -> {
            List<MutableComponent> abilityEffectDescriptions = effect.getDescription(dragon, ability);

            if (!effect.getDescription(dragon, ability).isEmpty()) {
                descriptions.addAll(abilityEffectDescriptions.stream().map(description -> description.append(targetDescription)).toList());
            }
        })).ifRight(entityTargeting -> entityTargeting.effects().forEach(effect -> {
            List<MutableComponent> abilityEffectDescriptions = effect.getDescription(dragon, ability);

            if (!effect.getDescription(dragon, ability).isEmpty()) {
                if (this instanceof SelfTarget) {
                    descriptions.addAll(effect.getDescription(dragon, ability));
                } else {
                    descriptions.addAll(abilityEffectDescriptions.stream().map(description -> description.append(targetDescription)).toList());
                }
            }

        }));

        return descriptions;
    }

    default void remove(final ServerPlayer dragon, final DragonAbilityInstance ability) { /* Nothing to do */ }

    MutableComponent getDescription(final Player dragon, final DragonAbilityInstance ability);
    void apply(final ServerPlayer dragon, final DragonAbilityInstance ability);
    MapCodec<? extends AbilityTargeting> codec();
    Either<BlockTargeting, EntityTargeting> target();
}
