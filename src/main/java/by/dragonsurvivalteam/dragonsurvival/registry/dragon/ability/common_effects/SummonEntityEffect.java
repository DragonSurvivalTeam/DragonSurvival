package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowSummonerGoal;
import by.dragonsurvivalteam.dragonsurvival.mixins.PrimedTntAccess;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AbilityBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.AbilityEntityEffect;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
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
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import javax.annotation.Nullable;

// TODO :: add optional mana cost for keeping the entity?
// TODO :: add option to add some goals to make sure entities can act as proper summons?
//  e.g. a target entity goal with switchable modes (on entity right click or sth.) between stuff like aggressive, stay in place, etc.
public record SummonEntityEffect(
        SimpleWeightedRandomList<Holder<EntityType<?>>> entities,
        ResourceLocation id,
        LevelBasedValue maxSummons,
        LevelBasedValue duration,
        List<AttributeScale> attributeScales,
        boolean shouldSetAllied
) implements AbilityBlockEffect, AbilityEntityEffect {
    public static final MapCodec<SummonEntityEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    SimpleWeightedRandomList.wrappedCodec(BuiltInRegistries.ENTITY_TYPE.holderByNameCodec()).fieldOf("entities").forGetter(SummonEntityEffect::entities),
                    ResourceLocation.CODEC.fieldOf("id").forGetter(SummonEntityEffect::id),
                    LevelBasedValue.CODEC.fieldOf("max_summons").forGetter(SummonEntityEffect::maxSummons),
                    LevelBasedValue.CODEC.optionalFieldOf("duration", LevelBasedValue.constant(DurationInstance.INFINITE_DURATION)).forGetter(SummonEntityEffect::duration),
                    AttributeScale.CODEC.listOf().optionalFieldOf("attribute_scales", List.of()).forGetter(SummonEntityEffect::attributeScales),
                    Codec.BOOL.optionalFieldOf("should_set_allied", true).forGetter(SummonEntityEffect::shouldSetAllied)
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

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        MutableComponent component = Component.translatable(LangKey.ABILITY_SUMMON, DSColors.blue(maxSummons.calculate(ability.level())));
        int totalWeight = WeightedRandom.getTotalWeight(entities.unwrap());

        entities.unwrap().forEach(wrapper -> {
            //noinspection DataFlowIssue -> key is present
            Component entityName = DSColors.blue(Component.translatable(wrapper.data().getKey().location().toLanguageKey("entity")));
            double chance = (double) wrapper.getWeight().asInt() / totalWeight;
            component.append(Component.translatable(LangKey.ABILITY_SUMMON_CHANCE, DSColors.blue(entityName), DSColors.blue(NumberFormat.getPercentInstance().format(chance))));
        });

        return List.of(component);
    }

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, final Direction direction) {
        spawn(dragon.serverLevel(), dragon, ability, position);
    }

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity entity) {
        spawn(dragon.serverLevel(), dragon, ability, entity.blockPosition());
    }

    private void spawn(final ServerLevel level, final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos spawnPosition) {
        int newDuration = (int) duration.calculate(ability.level());

        SummonedEntities data = dragon.getData(DSDataAttachments.SUMMONED_ENTITIES);
        Instance instance = data.get(id);

        if (instance != null) {
            if (instance.appliedAbilityLevel() != ability.level() || instance.currentDuration() != newDuration) {
                // When the effect is applied to an area the entities are summoned in one tick (= duration has not decreased)
                // Meaning this will only be reached if the ability is being cast again
                data.remove(dragon, instance);
                instance = null;
            } else if (instance.summonedAmount() == maxSummons.calculate(ability.level())) {
                // Keep the logic simple - players can kill their summons (they do not retaliate) if needed
                // Otherwise, if cast over a large area it would spawn and discard a lot of entities
                return;
            }
        }

        if (!Level.isInSpawnableBounds(spawnPosition)) {
            return;
        }

        EntityType<?> type = entities.getRandom(level.getRandom()).map(entry -> entry.data().value()).orElse(null);

        if (type == null) {
            return;
        }

        Entity entity = type.spawn(level, spawnPosition, MobSpawnType.TRIGGERED);

        if (entity == null) {
            return;
        }

        if (entity instanceof LivingEntity livingEntity) {
            int abilityLevel = ability.level();
            attributeScales.forEach(attributeScale -> attributeScale.apply(livingEntity, abilityLevel));
        }

        if (entity instanceof Projectile projectile) {
            projectile.setOwner(dragon);
        }

        if (entity instanceof LightningBolt bolt) {
            bolt.setCause(dragon);
        }

        if (entity instanceof PrimedTntAccess access) {
            access.dragonSurvival$setOwner(dragon);
        }

        setAllied(dragon, entity);
        entity.moveTo(spawnPosition.getX(), spawnPosition.getY() + 1, spawnPosition.getZ(), entity.getYRot(), entity.getXRot());

        if (instance == null) {
            instance = Instance.from(this, ability.level(), newDuration, entity.getUUID());
            data.add(dragon, instance);
        } else {
            instance.increment(entity.getUUID());
        }

        // TODO :: sync to client? would maybe be interesting outside inventory as well (indicator of current / max)
    }

    private void setAllied(final ServerPlayer dragon, final Entity entity) {
        if (!shouldSetAllied) {
            return;
        }

        EntityStateHandler data = entity.getData(DSDataAttachments.ENTITY_HANDLER);
        data.summonOwner = dragon.getUUID();
        // TODO :: might need to synchronize this to the client?

        if (entity instanceof TamableAnimal tamable) {
            tamable.setOwnerUUID(dragon.getUUID());
            tamable.setTame(true, true);
        }

        if (dragon.getTeam() != null) {
            dragon.level().getScoreboard().addPlayerToTeam(entity.getScoreboardName(), dragon.getTeam());
        }

        if (entity instanceof Mob mob) {
            try {
                mob.goalSelector.addGoal(3, new FollowSummonerGoal(mob, 1, 10, 2));
            } catch (IllegalArgumentException ignored) { /* Ignore due to custom path navigation */ }
        }
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
                .and(UUIDUtil.CODEC.listOf().xmap(/* Make list mutable */ ArrayList::new, Function.identity()).fieldOf("entity_uuid").forGetter(Instance::entityUUIDs))
                .and(Codec.INT.fieldOf("summoned_amount").forGetter(Instance::summonedAmount))
                .apply(instance, Instance::new));

        // Needs to specify 'ArrayList' due to the 'xmap'
        private final ArrayList<UUID> entityUUIDs;
        private int summonedAmount;

        public Instance(final SummonEntityEffect baseData, final ClientData clientData, int appliedAbilityLevel, int currentDuration, final ArrayList<UUID> entityUUIDs, int summonedAmount) {
            super(baseData, clientData, appliedAbilityLevel, currentDuration);
            this.entityUUIDs = entityUUIDs;
            this.summonedAmount = summonedAmount;
        }

        public static Instance from(final SummonEntityEffect baseData, int appliedAbilityLevel, int currentDuration, final UUID entityUUID) {
            ArrayList<UUID> entityUUIDs = new ArrayList<>();
            entityUUIDs.add(entityUUID);

            return new Instance(baseData, ClientEffectProvider.NONE, appliedAbilityLevel, currentDuration, entityUUIDs, 1);
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable SummonEntityEffect.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
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
                    summonedEntity.getData(DSDataAttachments.ENTITY_HANDLER).summonOwner = null;
                    summonedEntity.discard();
                }
            });
        }

        @Override
        public int getDuration() {
            return (int) baseData().duration().calculate(appliedAbilityLevel());
        }

        public void increment(final UUID uuid) {
            summonedAmount++;
            entityUUIDs.add(uuid);
        }

        public void discard(final ServerLevel level, int amount) {
            while (amount > 0 && !entityUUIDs.isEmpty()) {
                UUID removed = entityUUIDs.removeFirst();
                Entity entity = level.getEntity(removed);

                if (entity != null) {
                    // We already handled the removal ourselves
                    entity.getData(DSDataAttachments.ENTITY_HANDLER).summonOwner = null;
                    entity.discard();
                }

                amount--;
            }

            summonedAmount = entityUUIDs.size();
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

            int summonedAmount = entityUUIDs.size();

            if (summonedAmount == 0) {
                return true;
            }

            this.summonedAmount = summonedAmount;
            return false;
        }

        public void setTarget(final LivingEntity target) {
            if (target.level() instanceof ServerLevel serverLevel) {
                entityUUIDs.forEach(uuid -> {
                    if (serverLevel.getEntity(uuid) instanceof Mob mob && mob.getTarget() == null) {
                        mob.setTarget(target);
                    }
                });
            }
        }

        @Override
        public ResourceLocation id() {
            return baseData().id();
        }

        public ArrayList<UUID> entityUUIDs() {
            return entityUUIDs;
        }

        public int summonedAmount() {
            return summonedAmount;
        }
    }
}
