package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.SummonEntityEffect;
import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@EventBusSubscriber
public class SummonedEntities extends Storage<SummonEntityEffect.Instance> {
    // TODO :: also handle entities spawned from summoned entities?
    //  persist entities ourselves to re-spawn them once the player re-joins?
    //      'EntityType#create' for loading / 'Entity#save' for saving
    //      meaning it's a list of [entity type | entity nbt]
    //      some sort of persistence flag required to avoid despawn?
    //      only apply to unlimited duration? store duration in entity and reduce on tick?
    //  would be useful if some sort of "personal companion" is intended as summon
    //  Unsure how dimension change is handled atm

    public static final String MOVEMENT_BEHAVIOUR = "movement_behaviour";
    public static final String ATTACK_BEHAVIOUR = "attack_behaviour";

    public MovementBehaviour movementBehaviour = MovementBehaviour.FOLLOW;
    public AttackBehaviour attackBehaviour = AttackBehaviour.DEFENSIVE;

    /** Returns the instance the entity is part of */
    public @Nullable SummonEntityEffect.Instance getInstance(final Entity entity) {
        for (SummonEntityEffect.Instance instance : all()) {
            if (instance.entityUUIDs().contains(entity.getUUID())) {
                return instance;
            }
        }

        return null;
    }

    public static boolean hasSummonRelationship(final Entity entity, final Entity target) {
        if (entity == null || target == null) {
            return false;
        }

        return entity.getExistingData(DSDataAttachments.SUMMON).map(data -> {
            if (!data.isAllied) {
                return false;
            }

            if (data.isOwner(target)) {
                return true;
            }

            SummonData targetData = target.getExistingData(DSDataAttachments.SUMMON).orElse(null);

            if (targetData == null || !targetData.isAllied) {
                return false;
            }

            LivingEntity owner = data.getOwner(entity.level());

            if (owner == null) {
                return false;
            }

            return targetData.isOwner(owner);
        }).orElse(false);
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            player.getExistingData(DSDataAttachments.SUMMONED_ENTITIES).ifPresent(data -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    boolean requiresSync = false;

                    for (SummonEntityEffect.Instance instance : data.all()) {
                        if (instance.initializeSummons(serverPlayer)) {
                            requiresSync = true;
                        }
                    }

                    if (requiresSync) {
                        data.sync(serverPlayer);
                    }
                }

                data.tick(player);

                if (data.isEmpty()) {
                    player.removeData(DSDataAttachments.SUMMONED_ENTITIES);
                }
            });
        }
    }

    @SubscribeEvent // Discard the summon if right-clicked with no item (while crouching)
    public static void discardSummon(final PlayerInteractEvent.EntityInteract event) {
        if (!(event.getHand() == InteractionHand.OFF_HAND && event.getItemStack().isEmpty() && event.getEntity().isCrouching())) {
            return;
        }

        event.getEntity().getExistingData(DSDataAttachments.SUMMONED_ENTITIES).ifPresent(data -> {
            SummonEntityEffect.Instance instance = data.getInstance(event.getTarget());

            if (instance == null) {
                return;
            }

            if (instance.removeSummon(event.getTarget())) {
                data.remove(event.getEntity(), instance);
            }

            event.getTarget().discard();
        });
    }

    @SubscribeEvent
    public static void removeSummons(final LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (event.getEntity() instanceof Player player) {
            player.getExistingData(DSDataAttachments.SUMMONED_ENTITIES).ifPresent(data -> data.clear(player));
            return;
        }

        event.getEntity().getExistingData(DSDataAttachments.SUMMON).ifPresent(data -> {
            Entity owner = data.getOwner(serverLevel);

            if (owner != null) {
                SummonedEntities summonData = owner.getData(DSDataAttachments.SUMMONED_ENTITIES);
                SummonEntityEffect.Instance instance = summonData.getInstance(event.getEntity());

                if (instance == null) {
                    // Faulty entity or summoner data already removed the entry
                    return;
                }

                if (instance.removeSummon(event.getEntity())) {
                    summonData.remove(owner, instance);
                }

                if (summonData.isEmpty()) {
                    owner.removeData(DSDataAttachments.SUMMONED_ENTITIES);
                }
            }
        });
    }

    @SubscribeEvent
    public static void removeSummons(final PlayerEvent.PlayerLoggedOutEvent event) {
        // Since we cannot tick the duration of the entities we need to remove them once the player leaves
        event.getEntity().getExistingData(DSDataAttachments.SUMMONED_ENTITIES).ifPresent(data -> data.clear(event.getEntity()));
    }

    @SubscribeEvent // Prevents summoned entities from targeting their owner or other allied summons
    public static void targetAttackedEnemy(final LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() == null) {
            return;
        }

        if (event.getEntity().getExistingData(DSDataAttachments.SUMMON).map(data -> data.attackBehaviour == AttackBehaviour.PASSIVE).orElse(false)) {
            event.setNewAboutToBeSetTarget(null);
            return;
        }

        if (hasSummonRelationship(event.getEntity(), event.getNewAboutToBeSetTarget())) {
            // Fallback - should technically already be handled through the 'EntityMixin'
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent
    public static void avoidDamagingAlly(final EntityInvulnerabilityCheckEvent event) {
        if (hasSummonRelationship(event.getEntity(), event.getSource().getEntity())) {
            event.setInvulnerable(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void skipLoot(final LivingDropsEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        event.getEntity().getExistingData(DSDataAttachments.SUMMON).ifPresent(data -> {
            if (data.getOwner(event.getEntity().level()) != null) {
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void skipExperience(final LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        event.getEntity().getExistingData(DSDataAttachments.SUMMON).ifPresent(data -> {
            if (data.getOwner(event.getEntity().level()) != null) {
                event.setCanceled(true);
            }
        });
    }

    @Translation(comments = "Movement Behaviour")
    public enum MovementBehaviour implements StringRepresentable {
        @Translation(comments = "Default")
        DEFAULT,
        @Translation(comments = "Follow")
        FOLLOW,
        @Translation(comments = "Stay")
        STAY;

        public static final Codec<MovementBehaviour> CODEC = StringRepresentable.fromValues(MovementBehaviour::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }

    @Translation(comments = "Attack Behaviour")
    public enum AttackBehaviour implements StringRepresentable {
        @Translation(comments = "Default")
        DEFAULT,
        @Translation(comments = "Passive")
        PASSIVE,
        @Translation(comments = "Defensive")
        DEFENSIVE;

        public static final Codec<AttackBehaviour> CODEC = StringRepresentable.fromValues(AttackBehaviour::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }

    @Override
    protected Tag save(@NotNull final HolderLookup.Provider provider, final SummonEntityEffect.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected SummonEntityEffect.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        return SummonEntityEffect.Instance.load(provider, tag);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = super.serializeNBT(provider);
        tag.putInt(MOVEMENT_BEHAVIOUR, movementBehaviour.ordinal());
        tag.putInt(ATTACK_BEHAVIOUR, attackBehaviour.ordinal());
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        super.deserializeNBT(provider, tag);
        movementBehaviour = MovementBehaviour.values()[tag.getInt(MOVEMENT_BEHAVIOUR)];
        attackBehaviour = AttackBehaviour.values()[tag.getInt(ATTACK_BEHAVIOUR)];
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.SUMMONED_ENTITIES.get();
    }
}
