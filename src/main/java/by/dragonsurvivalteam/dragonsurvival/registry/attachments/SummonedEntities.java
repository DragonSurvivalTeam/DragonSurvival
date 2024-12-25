package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects.SummonEntityEffect;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class SummonedEntities extends Storage<SummonEntityEffect.Instance> {
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
    public static void decrementSummonCount(final EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        if (!event.getEntity().isAlive()) {
            event.getEntity().getExistingData(DSDataAttachments.ENTITY_HANDLER).ifPresent(data -> {
                Entity owner = data.getSummonOwner(event.getLevel());

                if (owner != null) {
                    SummonedEntities summonData = owner.getData(DSDataAttachments.SUMMONED_ENTITIES);
                    summonData.removeSummon(owner, event.getEntity());

                    if (summonData.isEmpty()) {
                        owner.removeData(DSDataAttachments.SUMMONED_ENTITIES);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void avoidTargetingOwner(final LivingChangeTargetEvent event) {
        EntityStateHandler data = event.getEntity().getData(DSDataAttachments.ENTITY_HANDLER);
        Entity owner = data.getSummonOwner(event.getEntity().level());

        if (event.getNewAboutToBeSetTarget() == owner) {
            event.setNewAboutToBeSetTarget(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void avoidDamagingOwner(final LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            return;
        }

        EntityStateHandler data = event.getEntity().getData(DSDataAttachments.ENTITY_HANDLER);
        Entity owner = data.getSummonOwner(event.getEntity().level());

        if (event.getSource().getEntity() == owner) {
            event.setCanceled(true);
        }
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

    /**
     * Removes the entity from all summon instances it may be part of (should only be one) <br>
     * While also removing any instances which are left empty (i.e. with no remaining summons)
     */
    private void removeSummon(final Entity owner, final Entity summon) {
        List<SummonEntityEffect.Instance> emptyInstances = new ArrayList<>();

        all().forEach(instance -> {
            if (instance.removeSummon(summon)) {
                emptyInstances.add(instance);
            }
        });

        emptyInstances.forEach(instance -> remove(owner, instance));
    }

    @Override
    protected Tag save(@NotNull final HolderLookup.Provider provider, final SummonEntityEffect.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected SummonEntityEffect.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        return SummonEntityEffect.Instance.load(provider, tag);
    }
}
