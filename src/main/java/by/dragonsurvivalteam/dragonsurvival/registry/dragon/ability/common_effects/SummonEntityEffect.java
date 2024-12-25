package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
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
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

// TODO :: add optional mana cost for keeping the entity?
// TODO :: add option to add some goals to make sure entities can act as proper summons?
//  e.g. a target entity goal with switchable modes (on entity right click or sth.) between stuff like aggressive, stay in place, etc.
//  add max. count (would need resource location id as well then)
public record SummonEntityEffect(HolderSet<EntityType<?>> entities, LevelBasedValue duration, List<AttributeScale> attributeScales, boolean shouldSetAllied) implements AbilityBlockEffect, AbilityEntityEffect {
    public static final MapCodec<SummonEntityEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).fieldOf("entities").forGetter(SummonEntityEffect::entities),
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
        if (!Level.isInSpawnableBounds(spawnPosition)) {
            return;
        }

        Optional<Holder<EntityType<?>>> optional = entities.getRandomElement(level.getRandom());

        if (optional.isEmpty()) {
            return;
        }

        Entity entity = optional.get().value().spawn(level, spawnPosition, MobSpawnType.TRIGGERED);

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

        setAllied(dragon, entity);

        // TODO :: not needed? or maybe add offset to y? not sure if blockpos means it spawns inside a block or not
        entity.moveTo(spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ(), entity.getYRot(), entity.getXRot());

        SummonedEntities data = dragon.getData(DSDataAttachments.SUMMONED_ENTITIES);
        data.add(dragon, new Instance(this, ClientEffectProvider.NONE, ability.level(), (int) duration.calculate(ability.level()), entity.getUUID()));

        // TODO :: sync to client? needed? 1 effect icon per entity seems like it'd be too much
    }

    private void setAllied(final ServerPlayer dragon, final Entity entity) {
        if (!shouldSetAllied) {
            return;
        }

        // TODO :: add custom owner / ally field through data attachment or sth. like that?
        //  and use it for 'isAllied' checks?
        //  and modify goals as well?

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
                .and(UUIDUtil.CODEC.fieldOf("entity_uuid").forGetter(Instance::entityUUID)).apply(instance, Instance::new));

        private final UUID entityUUID;

        public Instance(final SummonEntityEffect baseData, final ClientData clientData, int appliedAbilityLevel, int currentDuration, final UUID entityUUID) {
            super(baseData, clientData, appliedAbilityLevel, currentDuration);
            this.entityUUID = entityUUID;
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable SummonEntityEffect.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }

        @Override
        public void onRemovalFromStorage(final Entity entity) {
            if (entity.level() instanceof ServerLevel serverLevel) {
                Entity summonedEntity = serverLevel.getEntity(entityUUID);

                if (summonedEntity != null) {
                    summonedEntity.discard();
                }
            }
        }

        @Override
        public int getDuration() {
            return (int) baseData().duration().calculate(appliedAbilityLevel());
        }

        @Override
        public ResourceLocation id() {
            // TODO :: might work?
            return DragonSurvival.res(entityUUID.toString());
        }

        public UUID entityUUID() {
            return entityUUID;
        }
    }
}
