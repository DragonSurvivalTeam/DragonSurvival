package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.common.capability.EntityStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDamageTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEffectTags;
import by.dragonsurvivalteam.dragonsurvival.util.AdditionalEffectData;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@EventBusSubscriber
public class EffectHandler {
    @SubscribeEvent
    public static void storeLastEntityPosForBurnEffect(EntityTickEvent.Post event){
        if(event.getEntity() instanceof LivingEntity entity) {
            EntityStateHandler data = entity.getData(DSDataAttachments.ENTITY_HANDLER);
            if (entity.tickCount % 20 == 0) {
                data.lastPos = entity.position();
            }
        }
    }

    @SubscribeEvent
    public static void playerStruckByLightning(EntityStruckByLightningEvent event) {
        // TODO: I believe we can already do this with our ability system now
        // Just give the sea dragon immunity to that damage type
        /*if (event.getEntity() instanceof Player player) {

            DragonStateProvider.getOptional(player).ifPresent(cap -> {
                if (!cap.isDragon()) {
                    return;
                }

                if(DragonUtils.isType(cap, DragonTypes.SEA)){
                    event.setCanceled(true);
                }
            });
        }*/
    }

    @SubscribeEvent
    public static void markLastAfflictedOnApplyEffect(final MobEffectEvent.Added event) {
        ((AdditionalEffectData) event.getEffectInstance()).dragonSurvival$setApplier(event.getEffectSource());
    }

    public static MobEffectInstance modifyEffect(final Player affected, final MobEffectInstance instance, @Nullable final Entity applier) {
        if (instance == null || Objects.equals(affected, applier)) {
            return instance;
        }

        int amplifier = instance.getAmplifier();

        if (instance.getEffect().value().getCategory().equals(MobEffectCategory.HARMFUL)) {
            if (applier instanceof LivingEntity livingApplier && !instance.getEffect().is(DSEffectTags.OVERWHELMING_MIGHT_BLACKLIST)) {
                amplifier += EnchantmentUtils.getLevel(livingApplier, DSEnchantments.OVERWHELMING_MIGHT);
            }

            if (!instance.getEffect().is(DSEffectTags.UNBREAKABLE_SPIRIT_BLACKLIST)) {
                amplifier -= EnchantmentUtils.getLevel(affected, DSEnchantments.UNBREAKABLE_SPIRIT);
            }

            amplifier = Mth.clamp(amplifier, 0, 255);

            if (amplifier != instance.getAmplifier()) {
                MobEffectInstance modifiedInstance = new MobEffectInstance(instance.getEffect(), instance.getDuration(), amplifier, instance.isAmbient(), instance.isVisible(), instance.showIcon());

                if (affected.hasEffect(instance.getEffect())) {
                    affected.removeEffect(instance.getEffect());
                }

                return modifiedInstance;
            }
        }

        return instance;
    }

    @SubscribeEvent
    public static void livingHurt(final LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity target) {
            if (event.getSource().getEntity() instanceof LivingEntity attacker) {
                if (target.hasEffect(DSEffects.BLOOD_SIPHON)) {
                    attacker.heal(event.getAmount() * 0.1f);
                }

                if (event.getEntity().level().registryAccess().registry(Registries.ENCHANTMENT).isPresent()) {
                    Registry<Enchantment> enchantments = event.getEntity().level().registryAccess().registry(Registries.ENCHANTMENT).get();
                    if (event.getSource().is(DSDamageTypeTags.DRAGON_MAGIC)) {
                        Optional<Holder.Reference<Enchantment>> draconicSuperiority = enchantments.getHolder(DSEnchantments.DRACONIC_SUPERIORITY);
                        if (draconicSuperiority.isPresent()) {
                            EnchantmentHelper.getEnchantmentLevel(draconicSuperiority.get(), attacker);
                            event.setAmount(event.getAmount() * 1.2f + (0.08f * EnchantmentHelper.getEnchantmentLevel(draconicSuperiority.get(), attacker)));
                        }
                    }
                    if (event.getEntity().getHealth() == event.getEntity().getMaxHealth()) {
                        Optional<Holder.Reference<Enchantment>> murderersCunning = enchantments.getHolder(DSEnchantments.MURDERERS_CUNNING);
                        murderersCunning.ifPresent(enchantmentReference -> event.setAmount(event.getAmount() * 1.4f + (0.2f * EnchantmentHelper.getEnchantmentLevel(enchantmentReference, attacker))));
                    }
                }

                AttributeInstance armorIgnoreChance = attacker.getAttribute(DSAttributes.ARMOR_IGNORE_CHANCE);
                if (armorIgnoreChance != null && armorIgnoreChance.getValue() > 0) {
                    if(armorIgnoreChance.getValue() < target.level().random.nextDouble()) {
                        event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reductionIn) -> 0);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void experienceDrop(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();

        if (player != null) {
            int droppedExperience = event.getDroppedExperience();
            event.setDroppedExperience((int) (droppedExperience * player.getAttributeValue(DSAttributes.EXPERIENCE)));
        }
    }

    public static void renderEffectParticle(final LivingEntity entity, final ParticleOptions particle) {
        double d0 = (double) entity.getRandom().nextFloat() * entity.getBbWidth();
        double d1 = (double) entity.getRandom().nextFloat() * entity.getBbHeight();
        double d2 = (double) entity.getRandom().nextFloat() * entity.getBbWidth();
        double x = entity.getX() + d0 - entity.getBbWidth() / 2;
        double y = entity.getY() + d1;
        double z = entity.getZ() + d2 - entity.getBbWidth() / 2;

        entity.level().addParticle(particle, x, y, z, 0, 0, 0);
    }
}