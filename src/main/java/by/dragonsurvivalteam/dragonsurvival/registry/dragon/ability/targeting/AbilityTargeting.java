package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AbilityBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.AbilityEntityEffect;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public interface AbilityTargeting {
    ResourceKey<Registry<MapCodec<? extends AbilityTargeting>>> REGISTRY_KEY = ResourceKey.createRegistryKey(DragonSurvival.res("targeting"));
    Registry<MapCodec<? extends AbilityTargeting>> REGISTRY = new RegistryBuilder<>(REGISTRY_KEY).create();

    Codec<AbilityTargeting> CODEC = REGISTRY.byNameCodec().dispatch("target_type", AbilityTargeting::codec, Function.identity());

    enum EntityTargetingMode {
        @Translation(type = Translation.Type.TARGET_MODE, comments = "all entities")
        TARGET_ALL,
        @Translation(type = Translation.Type.TARGET_MODE, comments = "enemies")
        TARGET_ENEMIES,
        @Translation(type = Translation.Type.TARGET_MODE, comments = "allies")
        TARGET_ALLIES;

        public Component translation() {
            return Component.translatable(Translation.Type.TARGET_MODE.wrap(toString().toLowerCase(Locale.ENGLISH)));
        }
    }

    static Either<BlockTargeting, EntityTargeting> block(final List<AbilityBlockEffect> effects) {
        return block(null, effects);
    }

    static Either<BlockTargeting, EntityTargeting> block(final List<BlockPredicate> targetConditions, final List<AbilityBlockEffect> effects) {
        return Either.left(new BlockTargeting(Optional.ofNullable(targetConditions), effects));
    }

    static Either<BlockTargeting, EntityTargeting> entity(final List<AbilityEntityEffect> effects, final EntityTargetingMode targetingMode) {
        return entity(null, effects, targetingMode);
    }

    static Either<BlockTargeting, EntityTargeting> entity(final List<EntityPredicate> targetConditions, final List<AbilityEntityEffect> effects, final EntityTargetingMode targetingMode) {
        return Either.right(new EntityTargeting(Optional.ofNullable(targetConditions), effects, targetingMode));
    }

    record BlockTargeting(Optional<List<BlockPredicate>> targetConditions, List<AbilityBlockEffect> effect) {
        public static final Codec<BlockTargeting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPredicate.CODEC.listOf().optionalFieldOf("target_conditions").forGetter(BlockTargeting::targetConditions),
                AbilityBlockEffect.CODEC.listOf().fieldOf("block_effect").forGetter(BlockTargeting::effect)
        ).apply(instance, BlockTargeting::new));

        public boolean matches(final ServerLevel level, final BlockPos position) {
            return targetConditions.map(conditions -> {
                for (BlockPredicate condition : conditions) {
                    if (condition.matches(level, position)) {
                        return true;
                    }
                }

                return false;
            }).orElse(true);
        }
    }

    record EntityTargeting(Optional<List<EntityPredicate>> targetConditions, List<AbilityEntityEffect> effects, EntityTargetingMode targetingMode) {
        public static final Codec<EntityTargeting> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.CODEC.listOf().optionalFieldOf("target_conditions").forGetter(EntityTargeting::targetConditions),
                AbilityEntityEffect.CODEC.listOf().fieldOf("entity_effect").forGetter(EntityTargeting::effects),
                Codec.STRING.xmap(EntityTargetingMode::valueOf, EntityTargetingMode::name).fieldOf("entity_targeting_mode").forGetter(EntityTargeting::targetingMode)
        ).apply(instance, EntityTargeting::new));

        public boolean matches(final ServerLevel level, final Vec3 position, final Entity entity) {
            return targetConditions.map(conditions -> {
                for (EntityPredicate condition : conditions) {
                    if (condition.matches(level, position, entity)) {
                        return true;
                    }
                }

                return false;
            }).orElse(true);
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

    default List<MutableComponent> getAllEffectDescriptions(final Player dragon, final DragonAbilityInstance abilityInstance) {
        // Don't try to generate a description for an ability that is disabled; it will be level 0, which causes LevelBasedValues to throw an exception when using a lookup table
        // TODO :: we could add a mixin to the lookup method to return 0 if it would otherwise cause an exception
        //  we wouldn't exactly modify anyone's behaviour since the alternative would be a crash
        if (!abilityInstance.isEnabled()) {
            return List.of();
        }

        List<MutableComponent> descriptions = new ArrayList<>();
        MutableComponent targetDescription = getDescription(dragon, abilityInstance);

        target().ifLeft(blockTargeting -> blockTargeting.effect().forEach(effect -> {
            List<MutableComponent> abilityEffectDescriptions = effect.getDescription(dragon, abilityInstance);

            if (!effect.getDescription(dragon, abilityInstance).isEmpty()) {
                descriptions.addAll(abilityEffectDescriptions.stream().map(description -> description.append(targetDescription)).toList());
            }
        })).ifRight(entityTargeting -> entityTargeting.effects().forEach(effect -> {
            List<MutableComponent> abilityEffectDescriptions = effect.getDescription(dragon, abilityInstance);

            if (!effect.getDescription(dragon, abilityInstance).isEmpty()) {
                if (!effect.shouldAppendSelfTargetingToDescription() && this instanceof SelfTarget) {
                    // Special case where we don't want to append the "self target" for certain effects
                    // TODO :: why is that the case? this looks kinda clunky atm
                    descriptions.addAll(effect.getDescription(dragon, abilityInstance));
                } else {
                    descriptions.addAll(abilityEffectDescriptions.stream().map(description -> description.append(targetDescription)).toList());
                }
            }

        }));

        return descriptions;
    }


    MutableComponent getDescription(final Player dragon, final DragonAbilityInstance ability);
    void apply(final ServerPlayer dragon, final DragonAbilityInstance ability);
    default void remove(final ServerPlayer dragon, final DragonAbilityInstance ability) {};
    MapCodec<? extends AbilityTargeting> codec();
    Either<BlockTargeting, EntityTargeting> target();
}
