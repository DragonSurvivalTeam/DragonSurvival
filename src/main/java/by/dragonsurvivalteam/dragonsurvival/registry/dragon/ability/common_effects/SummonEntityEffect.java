package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.mixins.PrimedTntAccess;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SummonedEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AbilityBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.AbilityEntityEffect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

// TODO :: add optional mana cost for keeping the entity?
// TODO :: add option to add some goals to make sure entities can act as proper summons?
//  e.g. a target entity goal with switchable modes (on entity right click or sth.) between stuff like aggressive, stay in place, etc.
//  add max. count (would need resource location id as well then)
public record SummonEntityEffect(
        SimpleWeightedRandomList<Holder<EntityType<?>>> entities,
        ResourceLocation id,
        LevelBasedValue maxSummons,
        // TODO :: should probably be per entity?
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
        // TODO :: have information like x out of y (max) entities (of type z) summoned?
        return List.of();
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
        SummonedEntities data = dragon.getData(DSDataAttachments.SUMMONED_ENTITIES);
        Instance instance = data.get(id);

        if (instance != null) {
            if (instance.appliedAbilityLevel() != ability.level()) {
                // TODO :: either update here or add general method to ability instance
                //  which would update all effects when the level of said ability changes
                //  is probaly needed to update effects with infinite duration etc.
            }

            // TODO :: make summon over max. replace the first (oldest) summon?
            float maxSummons = this.maxSummons.calculate(ability.level());

            if (instance.summonedAmount() >= maxSummons) {
                // Technically could send a warning or error message, but it'd likely just end in spam
                instance.discard(level, (int) maxSummons - instance.summonedAmount);
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

        // TODO :: not needed? or maybe add offset to y? not sure if blockpos means it spawns inside a block or not
        entity.moveTo(spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ(), entity.getYRot(), entity.getXRot());

        if (instance == null) {
            data.add(dragon, Instance.from(this, ability.level(), (int) duration.calculate(ability.level()), entity.getUUID()));
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
        data.owner = dragon.getUUID();
        // TODO :: might need to synchronize this to the client?

        if (entity instanceof TamableAnimal tamable) {
            tamable.setOwnerUUID(dragon.getUUID());
            tamable.setTame(true, true);
        }

        if (dragon.getTeam() != null) {
            dragon.level().getScoreboard().addPlayerToTeam(entity.getScoreboardName(), dragon.getTeam());
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
                .and(UUIDUtil.CODEC.listOf().fieldOf("entity_uuid").forGetter(Instance::entityUUIDs))
                .and(Codec.INT.fieldOf("summoned_amount").forGetter(Instance::summonedAmount))
                .apply(instance, Instance::new));

        private final List<UUID> entityUUIDs;

        private int summonedAmount;

        public Instance(final SummonEntityEffect baseData, final ClientData clientData, int appliedAbilityLevel, int currentDuration, final List<UUID> entityUUIDs, int summonedAmount) {
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
        public void onRemovalFromStorage(final Entity entity) {
            if (entity.level() instanceof ServerLevel serverLevel && !entityUUIDs.isEmpty()) {
                entityUUIDs.forEach(uuid -> {
                    Entity summonedEntity = serverLevel.getEntity(uuid);

                    if (summonedEntity != null) {
                        summonedEntity.discard();
                    }
                });
            }
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
                    entity.discard();
                }

                amount--;
            }
        }

        @Override
        public ResourceLocation id() {
            return baseData().id();
        }

        public List<UUID> entityUUIDs() {
            return entityUUIDs;
        }

        public int summonedAmount() {
            return summonedAmount;
        }
    }
}
