package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.goals.FollowSummonerGoal;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.SummonEntityEffect;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber
public class SummonedEntities extends Storage<SummonEntityEffect.Instance> {
    public static final String MOVEMENT_BEHAVIOUR = "movement_behaviour";
    public static final String ATTACK_BEHAVIOUR = "attack_behaviour";

    private MovementBehaviour movementBehaviour = MovementBehaviour.FOLLOW;
    private AttackBehaviour attackBehaviour = AttackBehaviour.DEFENSIVE;

    /** Returns the instance the entity is part of */
    public @Nullable SummonEntityEffect.Instance getInstance(final Entity entity) {
        for (SummonEntityEffect.Instance instance : all()) {
            if (instance.entityUUIDs().contains(entity.getUUID())) {
                return instance;
            }
        }

        return null;
    }

    public MovementBehaviour getMovementBehaviour() {
        return movementBehaviour;
    }

    public static boolean isAlly(final Entity entity, final Entity target) {
        return entity.getExistingData(DSDataAttachments.ENTITY_HANDLER).map(data -> {
            if (data.summonOwner == null) {
                return false;
            }

            if (target.getUUID().equals(data.summonOwner)) {
                return true;
            }

            Entity owner = data.getSummonOwner(entity.level());

            if (owner == null) {
                return false;
            }

            // The entity shares the same summon owner
            SummonedEntities summonData = owner.getData(DSDataAttachments.SUMMONED_ENTITIES);
            return summonData.getInstance(target) != null;
        }).orElse(false);
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        event.getEntity().getExistingData(DSDataAttachments.SUMMONED_ENTITIES).ifPresent(data -> {
            data.tick();

            if (data.isEmpty()) {
                event.getEntity().removeData(DSDataAttachments.SUMMONED_ENTITIES);
            }
        });
    }

    @SubscribeEvent
    public static void removeSummons(final LivingDeathEvent event) {
        event.getEntity().getExistingData(DSDataAttachments.SUMMONED_ENTITIES).ifPresent(data -> {
            data.all().forEach(instance -> instance.onRemovalFromStorage(event.getEntity()));
        });
    }

    @SubscribeEvent
    public static void attachFollowOwnerGoal(final EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        mob.getExistingData(DSDataAttachments.ENTITY_HANDLER).ifPresent(data -> {
            Entity owner = data.getSummonOwner(event.getLevel());

            if (owner != null) {
                SummonedEntities summonData = owner.getData(DSDataAttachments.SUMMONED_ENTITIES);
                SummonEntityEffect.Instance instance = summonData.getInstance(event.getEntity());

                if (instance == null) {
                    // The entity shouldn't exist
                    // It has a summoner owner but said summoner doesn't know this entity
                    mob.discard();
                    return;
                }

                if (instance.baseData().shouldSetAllied()) {
                    try {
                        mob.goalSelector.addGoal(3, new FollowSummonerGoal(mob, 1, 10, 2));
                    } catch (IllegalArgumentException ignored) { /* Ignore due to custom path navigation */ }
                }
            }
        });
    }

    @SubscribeEvent
    public static void decrementSummonCount(final EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        if (!event.getEntity().isAlive()) {
            event.getEntity().getExistingData(DSDataAttachments.ENTITY_HANDLER).ifPresent(data -> {
                Entity owner = data.getSummonOwner(event.getLevel());

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
    }

    /**
     * Prevents summoned entities from targeting their owner <br>
     * If an entity targets someone with summoned entities said entities will target the entity <br>
     * (If their behaviour is set to the appropriate type)
     */
    @SubscribeEvent
    public static void handleTargeting(final LivingChangeTargetEvent event) {
        if (event.getNewAboutToBeSetTarget() == null) {
            return;
        }

        EntityStateHandler data = event.getEntity().getData(DSDataAttachments.ENTITY_HANDLER);
        Entity owner = data.getSummonOwner(event.getEntity().level());

        if (owner == null) {
            event.getNewAboutToBeSetTarget().getExistingData(DSDataAttachments.SUMMONED_ENTITIES).ifPresent(summonData -> {
                if (summonData.attackBehaviour != AttackBehaviour.DEFAULT) {
                    summonData.setTarget(event.getEntity());
                }
            });
        } else if (event.getNewAboutToBeSetTarget() == owner) {
            // Technically 'isAlliedTo' should handle this
            // But at certain points the team is directly or other weird checks are done instead
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void avoidDamagingOwner(final LivingIncomingDamageEvent event) {
        Entity source = event.getSource().getEntity();

        if (source == null) {
            return;
        }

        EntityStateHandler data = source.getData(DSDataAttachments.ENTITY_HANDLER);
        Entity owner = data.getSummonOwner(event.getEntity().level());

        if (owner == null) {
            return;
        }

        if (event.getEntity() == owner) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void handleOwnerAttack(final AttackEntityEvent event) {
        if (!(event.getTarget() instanceof LivingEntity livingTarget) || livingTarget.isAlliedTo(event.getEntity())) {
            return;
        }

        event.getEntity().getExistingData(DSDataAttachments.SUMMONED_ENTITIES).ifPresent(data -> {
            if (data.attackBehaviour != AttackBehaviour.DEFAULT) {
                data.setTarget(livingTarget);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void skipLoot(final LivingDropsEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        event.getEntity().getExistingData(DSDataAttachments.ENTITY_HANDLER).ifPresent(data -> {
            if (data.getSummonOwner(event.getEntity().level()) != null) {
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void skipExperience(final LivingExperienceDropEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        event.getEntity().getExistingData(DSDataAttachments.ENTITY_HANDLER).ifPresent(data -> {
            if (data.getSummonOwner(event.getEntity().level()) != null) {
                event.setCanceled(true);
            }
        });
    }

    private void setTarget(final LivingEntity target) {
        all().forEach(instance -> instance.setTarget(target));
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

    // TODO :: changeable with keybinds?
    public enum MovementBehaviour {
        DEFAULT, FOLLOW
    }

    public enum AttackBehaviour {
        DEFAULT, DEFENSIVE, AGGRESSIVE
    }
}
