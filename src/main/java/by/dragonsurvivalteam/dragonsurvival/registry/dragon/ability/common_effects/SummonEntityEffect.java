package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowSummonerGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.SummonerHurtByTargetGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.SummonerHurtTargetGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.SummonerTargetedGoal;
import by.dragonsurvivalteam.dragonsurvival.mixins.PrimedTntAccess;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSummonedEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AbilityBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.AbilityEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
public class SummonEntityEffect extends DurationInstanceBase<SummonedEntities, SummonEntityEffect.Instance> implements AbilityEntityEffect, AbilityBlockEffect {
    @Translation(comments = "§6■ Summon§r up to %s entities:")
    private static final String SUMMON = Translation.Type.GUI.wrap("summon_entity_effect.summon");

    @Translation(comments = "\n- %s (%s)")
    private static final String SUMMON_CHANCE = Translation.Type.GUI.wrap("summon_entity_effect.summon_chance");

    @Translation(comments = "Currently summoned: %s / %s")
    private static final String CURRENT_AMOUNT = Translation.Type.GUI.wrap("summon_entity_effect.current_amount");

    public static final MapCodec<SummonEntityEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
                    Codec.either(
                            WeightedList.codec(BuiltInRegistries.ENTITY_TYPE.byNameCodec()),
                            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE)
                    ).fieldOf("entities").forGetter(SummonEntityEffect::entities),
                    LevelBasedValue.CODEC.fieldOf("max_summons").forGetter(SummonEntityEffect::maxSummons),
                    AttributeScale.CODEC.listOf().optionalFieldOf("attribute_scales", List.of()).forGetter(SummonEntityEffect::attributeScales),
                    CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(SummonEntityEffect::nbt),
                    Codec.BOOL.optionalFieldOf("is_allied", true).forGetter(SummonEntityEffect::isAllied)
            ).apply(instance, SummonEntityEffect::new)
    );

    public record AttributeScale(HolderSet<Attribute> attributes, LevelBasedValue scale) {
        public static final Codec<AttributeScale> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.ATTRIBUTE).fieldOf("attributes").forGetter(AttributeScale::attributes),
                LevelBasedValue.CODEC.fieldOf("scale").forGetter(AttributeScale::scale)
        ).apply(instance, AttributeScale::new));

        public void apply(final LivingEntity entity, int abilityLevel) {
            float scale = this.scale.calculate(abilityLevel);

            for (Holder<Attribute> attribute : attributes) {
                AttributeInstance instance = entity.getAttribute(attribute);

                if (instance != null) {
                    Identifier id = ModifierType.CUSTOM.randomId(attribute, AttributeModifier.Operation.ADD_MULTIPLIED_BASE, entity.getRandom());
                    instance.addPermanentModifier(new AttributeModifier(id, scale, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }

                if (attribute == Attributes.MAX_HEALTH) {
                    entity.setHealth(entity.getMaxHealth());
                }
            }
        }
    }

    private final Either<WeightedList<EntityType<?>>, HolderSet<EntityType<?>>> entities;
    private final LevelBasedValue maxSummons;
    private final List<AttributeScale> attributeScales;
    private final Optional<CompoundTag> nbt;
    private final boolean isAllied;

    public SummonEntityEffect(
            final DurationInstanceBase<?, ?> base,
            final Either<WeightedList<EntityType<?>>, HolderSet<EntityType<?>>> entities,
            final LevelBasedValue maxSummons,
            final List<AttributeScale> attributeScales,
            final Optional<CompoundTag> nbt,
            final boolean isAllied
    ) {
        super(base);
        this.entities = entities;
        this.maxSummons = maxSummons;
        this.attributeScales = attributeScales;
        this.nbt = nbt;
        this.isAllied = isAllied;
    }

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, @Nullable final Direction direction) {
        collectPosition(dragon, ability, position);
    }

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        collectPosition(dragon, ability, target.blockPosition());
    }

    private void collectPosition(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos spawnPosition) {
        SummonedEntities summonData = dragon.getData(DSDataAttachments.SUMMONED_ENTITIES);
        Instance instance = summonData.get(id());

        if (instance != null && instance.hasEntities()) {
            summonData.remove(dragon, instance);
            instance = null;
        }

        if (!Level.isInSpawnableBounds(spawnPosition)) {
            return;
        }

        if (instance == null) {
            instance = createInstance(dragon, ability, (int) duration().calculate(ability.level()));
            summonData.add(dragon, instance);
        }

        instance.addPosition(spawnPosition.immutable());
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        MutableComponent component = Component.translatable(SUMMON, DSColors.dynamicValue(maxSummons.calculate(ability.level())));

        entities.ifLeft(list -> {
            int totalWeight = WeightedRandom.getTotalWeight(list.unwrap(), Weighted::weight);

            list.unwrap().forEach(wrapper -> {
                Component entityName = DSColors.dynamicValue(wrapper.value().getDescription());
                double chance = (double) wrapper.weight() / totalWeight;
                component.append(Component.translatable(SUMMON_CHANCE, DSColors.dynamicValue(entityName), DSColors.dynamicValue(NumberFormat.getPercentInstance().format(chance))));
            });

            if (!list.isEmpty()) {
                component.append(Component.literal("\n"));
            }
        }).ifRight(set -> {
            int totalWeight = set.size();

            set.forEach(type -> {
                Component entityName = DSColors.dynamicValue(type.value().getDescription());
                double chance = 1d / totalWeight;
                component.append(Component.translatable(SUMMON_CHANCE, DSColors.dynamicValue(entityName), DSColors.dynamicValue(NumberFormat.getPercentInstance().format(chance))));
            });

            if (set.size() > 0) {
                component.append(Component.literal("\n"));
            }
        });

        float duration = duration().calculate(ability.level());

        if (duration != DurationInstance.INFINITE_DURATION) {
            component.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, DSColors.dynamicValue(Functions.ticksToSeconds((int) duration))));
        }

        return List.of(component);
    }

    @Override
    public Instance createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, final int currentDuration) {
        return new Instance(this, CommonData.from(id(), dragon, ability, customIcon(), shouldRemoveAutomatically()), currentDuration, new CopyOnWriteArrayList<>());
    }

    @Override
    public AttachmentType<SummonedEntities> type() {
        return DSDataAttachments.SUMMONED_ENTITIES.value();
    }

    public Either<WeightedList<EntityType<?>>, HolderSet<EntityType<?>>> entities() {
        return entities;
    }

    public LevelBasedValue maxSummons() {
        return maxSummons;
    }

    public List<AttributeScale> attributeScales() {
        return attributeScales;
    }

    public Optional<CompoundTag> nbt() {
        return nbt;
    }

    public boolean isAllied() {
        return isAllied;
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }

    public static class Instance extends DurationInstance<SummonEntityEffect> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(instance, SummonEntityEffect.CODEC::codec)
                .and(UUIDUtil.CODEC.listOf().xmap(/* Make list mutable */ CopyOnWriteArrayList::new, Function.identity()).fieldOf("entity_uuid").forGetter(Instance::entityUUIDs))
                .apply(instance, Instance::new));

        // Needs to be concurrent since the encoding for the 'sync_summoned_entity' happens on the network thread
        // And since it goes through the list it can run into a ConcurrentModificationException
        private final CopyOnWriteArrayList<UUID> entityUUIDs;

        // Transient field only needed for the initial summoning
        private List<BlockPos> positions;

        public Instance(final SummonEntityEffect baseData, final CommonData commonData, int currentDuration, final CopyOnWriteArrayList<UUID> entityUUIDs) {
            super(baseData, commonData, currentDuration);
            this.entityUUIDs = entityUUIDs;
        }

        public void addPosition(final BlockPos position) {
            if (positions == null) {
                positions = new ArrayList<>();
            }

            positions.add(position);
        }

        public boolean hasEntities() {
            return !entityUUIDs.isEmpty();
        }

        /**
         * Removes the entity from the instance (if applicable)
         *
         * @return 'true' if the instance has no remaining summoned entities
         */
        public boolean removeSummon(final Entity summon) {
            boolean removed = entityUUIDs.remove(summon.getUUID());

            if (!removed) {
                return false;
            }

            return entityUUIDs.isEmpty();
        }

        public boolean initializeSummons(final ServerPlayer storageHolder) {
            if (positions == null || positions.isEmpty()) {
                return false;
            }

            int maxSummons = (int) baseData().maxSummons().calculate(appliedAbilityLevel());

            if (maxSummons == 0) {
                return false;
            }

            SummonedEntities summonData = storageHolder.getData(DSDataAttachments.SUMMONED_ENTITIES);
            BlockPos spawnPosition = null;

            while (!positions.isEmpty()) {
                if (entityUUIDs.size() >= maxSummons) {
                    break;
                }

                spawnPosition = positions.remove(storageHolder.getRandom().nextInt(positions.size()));
                // TODO :: store valid spawn positions so it can be used for the remaining attempts?
                summon(storageHolder, spawnPosition, summonData);
            }

            for (int attempt = 0; attempt < maxSummons - entityUUIDs.size(); attempt++) {
                summon(storageHolder, spawnPosition, summonData);
            }

            positions = null;
            return !entityUUIDs.isEmpty();
        }

        private void summon(final ServerPlayer storageHolder, final BlockPos spawnPosition, final SummonedEntities summonData) {
            EntityType<?> type = baseData().entities().map(
                    list -> list.getRandom(storageHolder.getRandom()).orElse(null),
                    set -> set.getRandomElement(storageHolder.getRandom()).map(Holder::value).orElse(null)
            );

            if (type == null) {
                return;
            }

            // Currently not checking 'BlockState#isValidSpawn' to allow spawning any type of entity
            BlockState state = storageHolder.level().getBlockState(spawnPosition);

            // No explicit check for 'FlyingAnimal' or 'FlyingMob' to give full control to the entity type tag
            // Also avoids having to create an entity twice (would've been needed to utilize 'instanceof' checks)
            //noinspection deprecation -> ignore
            if (!type.is(DSEntityTypeTags.CAN_FLY) && !state.blocksMotion()) {
                return;
            }

            for (int i = 1; i <= Math.max(1, type.getHeight()); i++) {
                if (!storageHolder.level().getBlockState(spawnPosition.above(i)).isAir()) {
                    return;
                }
            }

            Entity entity = type.spawn(storageHolder.level(), spawnPosition.above(), EntitySpawnReason.TRIGGERED);

            if (entity == null) {
                return;
            }

            if (entity instanceof LivingEntity livingEntity) {
                baseData().attributeScales().forEach(attributeScale -> attributeScale.apply(livingEntity, appliedAbilityLevel()));
            }

            if (entity instanceof Projectile projectile) {
                projectile.setOwner(storageHolder);
            }

            if (entity instanceof LightningBolt bolt) {
                bolt.setCause(storageHolder);
            }

            if (entity instanceof PrimedTntAccess access) {
                access.dragonSurvival$setOwner(EntityReference.of(storageHolder));
            }

            setAllied(storageHolder, entity);

            if (baseData().nbt().isPresent()) {
                TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, storageHolder.level().registryAccess());
                entity.saveWithoutId(valueOutput);
                valueOutput.store(baseData().nbt().get());

                ValueInput valueInput = TagValueInput.create(ProblemReporter.DISCARDING, storageHolder.level().registryAccess(), valueOutput.buildResult());
                entity.load(valueInput);
            }

            SummonData summon = entity.getData(DSDataAttachments.SUMMON);
            summon.setOwnerUUID(storageHolder);
            summon.isAllied = baseData().isAllied();
            summon.attackBehaviour = summonData.attackBehaviour;
            summon.movementBehaviour = summonData.movementBehaviour;

            entityUUIDs.add(entity.getUUID());
        }

        private void setAllied(final ServerPlayer dragon, final Entity entity) {
            if (!baseData().isAllied()) {
                return;
            }

            if (entity instanceof TamableAnimal tamable) {
                tamable.setOwner(dragon);
                tamable.setTame(true, true);
            }

            if (dragon.getTeam() != null) {
                dragon.level().getScoreboard().addPlayerToTeam(entity.getScoreboardName(), dragon.getTeam());
            }

            if (entity instanceof Mob mob) {
                mob.goalSelector.addGoal(1, new SummonerHurtByTargetGoal(mob));
                mob.goalSelector.addGoal(2, new SummonerHurtTargetGoal(mob));
                mob.goalSelector.addGoal(3, new SummonerTargetedGoal(mob));
                mob.goalSelector.addGoal(3, new FollowSummonerGoal(mob, 1, 10, 2));
            }
        }

        @Override
        public Component getDescription() {
            Component current = DSColors.dynamicValue(entityUUIDs.size());
            Component max = DSColors.dynamicValue((int) baseData().maxSummons().calculate(appliedAbilityLevel()));
            return Component.translatable(CURRENT_AMOUNT, current, max);
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncSummonedEntity(this, false));
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (!(storageHolder.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            entityUUIDs.forEach(uuid -> {
                // Go through all levels in case the summoner or the summons are in different dimensions
                for (ServerLevel level : serverLevel.getServer().getAllLevels()) {
                    Entity summonedEntity = level.getEntity(uuid);

                    if (summonedEntity != null) {
                        // Since the entry is already removed from the storage we don't need any behaviour based on the owner
                        summonedEntity.getData(DSDataAttachments.SUMMON).setOwnerUUID(null);
                        summonedEntity.discard();
                        break;
                    }
                }
            });

            if (storageHolder instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncSummonedEntity(this, true));
            }
        }

        public void save(@NotNull ValueOutput valueOutput, final String key) {
            valueOutput.store(key, CODEC, this);
        }

        public static @Nullable SummonEntityEffect.Instance load(@NotNull ValueInput valueInput, final String key) {
            return valueInput.read(key, CODEC).orElse(null);
        }

        public CopyOnWriteArrayList<UUID> entityUUIDs() {
            return entityUUIDs;
        }
    }
}
