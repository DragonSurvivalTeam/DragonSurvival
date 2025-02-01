package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowSummonerGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.SummonerHurtByTargetGoal;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.SummonerHurtTargetGoal;
import by.dragonsurvivalteam.dragonsurvival.mixins.PrimedTntAccess;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSummonedEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
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
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class SummonEntityEffect extends DurationInstanceBase<SummonedEntities, SummonEntityEffect.Instance> implements AbilityEntityEffect, AbilityBlockEffect {
    @Translation(comments = "§6■ Can summon up to§r %s §6entities:§r")
    private static final String SUMMON = Translation.Type.GUI.wrap("summon_entity_effect.summon");

    @Translation(comments = "\n- %s (%s)")
    private static final String SUMMON_CHANCE = Translation.Type.GUI.wrap("summon_entity_effect.summon_chance");

    @Translation(comments = "Currently summoned: %s / %s")
    private static final String CURRENT_AMOUNT = Translation.Type.GUI.wrap("summon_entity_effect.current_amount");

    public static final MapCodec<SummonEntityEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
                    Codec.either(SimpleWeightedRandomList.wrappedCodec(BuiltInRegistries.ENTITY_TYPE.byNameCodec()), RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE)).fieldOf("entities").forGetter(SummonEntityEffect::entities),
                    LevelBasedValue.CODEC.fieldOf("max_summons").forGetter(SummonEntityEffect::maxSummons),
                    AttributeScale.CODEC.listOf().optionalFieldOf("attribute_scales", List.of()).forGetter(SummonEntityEffect::attributeScales),
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
                    ResourceLocation id = ModifierType.CUSTOM.randomId(attribute, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                    instance.addPermanentModifier(new AttributeModifier(id, scale, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            }
        }
    }

    private final Either<SimpleWeightedRandomList<EntityType<?>>, HolderSet<EntityType<?>>> entities;
    private final LevelBasedValue maxSummons;
    private final List<AttributeScale> attributeScales;
    private final boolean isAllied;

    public SummonEntityEffect(final DurationInstanceBase<?, ?> base, final Either<SimpleWeightedRandomList<EntityType<?>>, HolderSet<EntityType<?>>> entities, final LevelBasedValue maxSummons, final List<AttributeScale> attributeScales, final boolean isAllied) {
        super(base);
        this.entities = entities;
        this.maxSummons = maxSummons;
        this.attributeScales = attributeScales;
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
            int totalWeight = WeightedRandom.getTotalWeight(list.unwrap());

            list.unwrap().forEach(wrapper -> {
                Component entityName = DSColors.dynamicValue(wrapper.data().getDescription());
                double chance = (double) wrapper.getWeight().asInt() / totalWeight;
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

    public Either<SimpleWeightedRandomList<EntityType<?>>, HolderSet<EntityType<?>>> entities() {
        return entities;
    }

    public LevelBasedValue maxSummons() {
        return maxSummons;
    }

    public List<AttributeScale> attributeScales() {
        return attributeScales;
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
                summon(storageHolder, spawnPosition, summonData);
            }

            while (entityUUIDs.size() < maxSummons) {
                summon(storageHolder, spawnPosition, summonData);
            }

            positions = null;
            return true;
        }

        private void summon(final ServerPlayer storageHolder, final BlockPos spawnPosition, final SummonedEntities summonData) {
            EntityType<?> type = baseData().entities().map(
                    list -> list.getRandom(storageHolder.getRandom()).map(WeightedEntry.Wrapper::data).orElse(null),
                    set -> set.getRandomElement(storageHolder.getRandom()).map(Holder::value).orElse(null)
            );

            if (type == null) {
                return;
            }

            // Currently not checking 'BlockState#isValidSpawn' to allow spawning any type of entity
            BlockState state = storageHolder.level().getBlockState(spawnPosition);

            //noinspection deprecation -> ignore
            if (!state.blocksMotion() || !storageHolder.level().getBlockState(spawnPosition.above()).isAir()) {
                return;
            }

            Entity entity = type.spawn(storageHolder.serverLevel(), spawnPosition, MobSpawnType.TRIGGERED);

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
                access.dragonSurvival$setOwner(storageHolder);
            }

            setAllied(storageHolder, entity);

            SummonData summon = entity.getData(DSDataAttachments.SUMMON);
            summon.setOwnerUUID(storageHolder);
            summon.isAllied = baseData().isAllied();
            summon.attackBehaviour = summonData.attackBehaviour;
            summon.movementBehaviour = summonData.movementBehaviour;

            entity.moveTo(spawnPosition.getX(), spawnPosition.getY() + 1, spawnPosition.getZ(), entity.getYRot(), entity.getXRot());
            entityUUIDs.add(entity.getUUID());
        }

        private void setAllied(final ServerPlayer dragon, final Entity entity) {
            if (!baseData().isAllied()) {
                return;
            }

            if (entity instanceof TamableAnimal tamable) {
                tamable.setOwnerUUID(dragon.getUUID());
                tamable.setTame(true, true);
            }

            if (dragon.getTeam() != null) {
                dragon.level().getScoreboard().addPlayerToTeam(entity.getScoreboardName(), dragon.getTeam());
            }

            if (entity instanceof Mob mob) {
                mob.goalSelector.addGoal(1, new SummonerHurtByTargetGoal(mob));
                mob.goalSelector.addGoal(2, new SummonerHurtTargetGoal(mob));
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
                Entity summonedEntity = serverLevel.getEntity(uuid);

                if (summonedEntity != null) {
                    // Since the entry is already removed from the storage we don't need any behaviour based on the owner
                    summonedEntity.getData(DSDataAttachments.SUMMON).setOwnerUUID(null);
                    summonedEntity.discard();
                }
            });

            if (storageHolder instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncSummonedEntity(this, true));
            }
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable SummonEntityEffect.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }

        public CopyOnWriteArrayList<UUID> entityUUIDs() {
            return entityUUIDs;
        }
    }
}
