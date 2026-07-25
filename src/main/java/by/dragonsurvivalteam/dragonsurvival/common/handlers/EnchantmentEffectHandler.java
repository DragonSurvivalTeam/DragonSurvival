package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.AttributeOperation;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDamageTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonSpeciesTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEffectTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

@EventBusSubscriber
public class EnchantmentEffectHandler {
    private static final String CHARGED_PROJECTILES = "ChargedProjectiles";

    @SubscribeEvent
    public static void fireCrossbow(final ArrowLooseEvent event) {
        ItemStack crossbow = event.getBow();

        if (!(crossbow.getItem() instanceof CrossbowItem)
                || EnchantmentUtils.getLevel(event.getLevel(), DSEnchantments.BOLAS, crossbow) < 1) {
            return;
        }

        CompoundTag tag = crossbow.getTag();

        if (tag == null || !tag.contains(CHARGED_PROJECTILES, Tag.TAG_LIST)) {
            return;
        }

        ListTag projectiles = tag.getList(CHARGED_PROJECTILES, Tag.TAG_COMPOUND);

        for (int i = 0; i < projectiles.size(); i++) {
            ItemStack projectile = ItemStack.of(projectiles.getCompound(i));

            if (projectile.getItem() instanceof ArrowItem) {
                projectiles.set(i, new ItemStack(DSItems.BOLAS.get()).save(new CompoundTag()));
            }
        }
    }

    @SubscribeEvent
    public static void handleDragonsbaneEnchantment(final LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (!victim.hasEffect(DSEffects.HUNTER_OMEN.get())) {
            return;
        }

        int enchantmentLevel = EnchantmentUtils.getLevel(attacker.level(), DSEnchantments.DRAGONSBANE, attacker.getMainHandItem());

        if (enchantmentLevel > 0) {
            DragonStateHandler victimData = DragonStateProvider.getData(victim);

            if (!victimData.isDragon()) {
                return;
            }

            victimData.setDesiredGrowth(victim, victimData.getGrowth() - getStolenTime(victimData) * enchantmentLevel);
            DragonStateHandler attackerData = DragonStateProvider.getData(attacker);

            if (attackerData.isDragon()) {
                // TODO :: why doesn't this scale with the enchantment level
                attackerData.setDesiredGrowth(attacker, attackerData.getGrowth() + getStolenTime(attackerData));
            }

            attacker.level().playLocalSound(attacker.blockPosition(), SoundEvents.AMBIENT_UNDERWATER_LOOP_ADDITIONS_ULTRA_RARE, SoundSource.PLAYERS, 2, 1, false);
        }
    }

    public static MobEffectInstance modifyEffect(final Player affected, final MobEffectInstance instance, @Nullable final Entity applier) {
        if (instance == null || Objects.equals(affected, applier)) {
            return instance;
        }

        int amplifier = instance.getAmplifier();

        if (instance.getEffect().getCategory().equals(MobEffectCategory.HARMFUL)) {
            if (applier instanceof LivingEntity livingApplier && !isEffect(instance.getEffect(), DSEffectTags.OVERWHELMING_MIGHT_BLACKLIST)) {
                amplifier += EnchantmentUtils.getLevel(livingApplier, DSEnchantments.OVERWHELMING_MIGHT);
            }

            if (!isEffect(instance.getEffect(), DSEffectTags.UNBREAKABLE_SPIRIT_BLACKLIST)) {
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
    public static void livingHurt(final LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        RandomSource random = target.getRandom();

        if (event.getSource().getEntity() instanceof LivingEntity attacker) {
            applyDragonsbane(event, attacker, target);
            applyBloodSiphon(target, attacker, random);
            applyCombatRecovery(target, random);

            MobEffectInstance siphon = target.getEffect(DSEffects.BLOOD_SIPHON.get());

            if (siphon != null) {
                float percentage = 0.01f * (siphon.getAmplifier() + 1);
                attacker.heal(event.getAmount() * percentage);
            }

            int superiorityLevel = EnchantmentUtils.getLevel(attacker, DSEnchantments.DRACONIC_SUPERIORITY);

            if (superiorityLevel > 0) {
                event.setAmount(event.getAmount() * (1.2f + 0.08f * (superiorityLevel - 1)));

                if (event.getSource().is(DSDamageTypeTags.DRAGON_MAGIC)) {
                    event.setAmount(event.getAmount() * 1.2f + 0.08f * superiorityLevel);
                }
            }

            if (target.getHealth() == target.getMaxHealth()) {
                int cunningLevel = EnchantmentUtils.getLevel(attacker, DSEnchantments.MURDERERS_CUNNING);

                if (cunningLevel > 0) {
                    event.setAmount(event.getAmount() * 1.4f + 0.2f * cunningLevel);
                }
            }
        }

        applySacredScales(event, target, random);
    }

    @SubscribeEvent
    public static void itemAttributes(final ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        EquipmentSlot slot = event.getSlotType();
        int aerodynamicLevel = getStackLevel(stack, DSEnchantments.AERODYNAMIC_MASTERY);

        if (slot == EquipmentSlot.CHEST && aerodynamicLevel > 0) {
            event.addModifier(
                    DSAttributes.FLIGHT_STAMINA_COST.get(),
                    new AttributeModifier(
                            modifierId("aerodynamic_mastery", slot),
                            "dragonsurvival:enchantment.aerodynamic_mastery",
                            0.5f + 0.25f * (aerodynamicLevel - 1),
                            AttributeOperation.ADD_VALUE.legacy()
                    )
            );
        }

        int kindnessLevel = getStackLevel(stack, DSEnchantments.CURSE_OF_KINDNESS);

        if (kindnessLevel > 0) {
            event.addModifier(
                    DSAttributes.HUNTER_FACTION_DAMAGE.get(),
                    new AttributeModifier(
                            modifierId("curse_of_kindness", slot),
                            "dragonsurvival:enchantment.curse_of_kindness",
                            -0.25f * kindnessLevel,
                            AttributeOperation.ADD_VALUE.legacy()
                    )
            );
        }
    }

    @SubscribeEvent
    public static void livingTick(final LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();

        if (entity.tickCount % 20 == 0 && EnchantmentUtils.getLevel(entity, DSEnchantments.CURSE_OF_OUTLAW) > 0) {
            entity.addEffect(new MobEffectInstance(
                    DSEffects.HUNTER_OMEN.get(),
                    Functions.secondsToTicks(Mth.nextInt(entity.getRandom(), 5, 10)),
                    0,
                    false,
                    false,
                    true
            ));
        }

        if (entity.tickCount % 100 != 0 || !isTrueDragon(entity)) {
            return;
        }

        ItemStack weapon = entity.getMainHandItem();

        if (EnchantmentUtils.getLevel(entity.level(), DSEnchantments.DRAGONSBANE, weapon) < 1) {
            return;
        }

        entity.hurt(new DamageSource(DSDamageTypes.get(entity.level(), DSDamageTypes.ANTI_DRAGON)), 0.5f);
        weapon.hurtAndBreak(1, entity, living -> living.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, Functions.secondsToTicks(5), 1));
        entity.level().playSound(null, entity.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1, 1);
    }

    private static void applyDragonsbane(final LivingHurtEvent event, final LivingEntity attacker, final LivingEntity target) {
        int level = EnchantmentUtils.getLevel(attacker.level(), DSEnchantments.DRAGONSBANE, attacker.getMainHandItem());

        if (level < 1 || !isDragon(target)) {
            return;
        }

        event.setAmount(event.getAmount() + 2.5f * level);

        if (event.getSource().getDirectEntity() == attacker) {
            int duration = Functions.secondsToTicks(1.5f + 0.5f * (level - 1));
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1));
        }
    }

    private static void applyBloodSiphon(final LivingEntity target, final LivingEntity attacker, final RandomSource random) {
        int level = EnchantmentUtils.getLevel(target, DSEnchantments.BLOOD_SIPHON);
        float chance = Mth.clamp(0.3f + 0.15f * (level - 1), 0, 1);

        if (level < 1 || random.nextFloat() >= chance) {
            return;
        }

        attacker.addEffect(new MobEffectInstance(
                DSEffects.BLOOD_SIPHON.get(),
                Functions.secondsToTicks(Mth.nextInt(random, 3, 10)),
                Mth.nextInt(random, 1, 2)
        ));
        damageEnchantedItem(target, DSEnchantments.BLOOD_SIPHON, 2);
    }

    private static void applyCombatRecovery(final LivingEntity target, final RandomSource random) {
        int level = EnchantmentUtils.getLevel(target, DSEnchantments.COMBAT_RECOVERY);
        float chance = Mth.clamp(0.2f + 0.1f * (level - 1), 0, 1);

        if (level < 1 || target.hasEffect(DSEffects.REGENERATION_DELAY.get()) || random.nextFloat() >= chance) {
            return;
        }

        target.addEffect(new MobEffectInstance(
                MobEffects.REGENERATION,
                Functions.secondsToTicks(Mth.nextInt(random, 5, 10)),
                Mth.nextInt(random, 0, 1)
        ));
        target.addEffect(new MobEffectInstance(
                DSEffects.REGENERATION_DELAY.get(),
                Functions.secondsToTicks(40),
                0
        ));
    }

    private static void applySacredScales(final LivingHurtEvent event, final LivingEntity target, final RandomSource random) {
        int level = EnchantmentUtils.getLevel(target, DSEnchantments.SACRED_SCALES);
        float chance = Mth.clamp(0.2f + 0.12f * (level - 1), 0, 0.8f);

        if (level > 0 && random.nextFloat() < chance) {
            event.setAmount(event.getAmount() * 0.2f);
        }
    }

    private static void damageEnchantedItem(final LivingEntity entity, final net.minecraft.resources.ResourceKey<Enchantment> enchantment,
                                            final int amount) {
        Enchantment value = EnchantmentUtils.get(enchantment);

        if (value == null) {
            return;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = entity.getItemBySlot(slot);

            if (!stack.isEmpty() && stack.getEnchantmentLevel(value) > 0) {
                stack.hurtAndBreak(amount, entity, living -> living.broadcastBreakEvent(slot));
                return;
            }
        }
    }

    private static int getStackLevel(final ItemStack stack, final net.minecraft.resources.ResourceKey<Enchantment> enchantment) {
        Enchantment value = EnchantmentUtils.get(enchantment);
        return value == null ? 0 : stack.getEnchantmentLevel(value);
    }

    private static UUID modifierId(final String path, final EquipmentSlot slot) {
        return UUID.nameUUIDFromBytes(("dragonsurvival:" + path + "/" + slot.getName()).getBytes(StandardCharsets.UTF_8));
    }

    private static boolean isDragon(final LivingEntity entity) {
        if (entity.getType().is(DSEntityTypeTags.DRAGONS)) {
            return true;
        }

        return isTrueDragon(entity);
    }

    private static boolean isTrueDragon(final LivingEntity entity) {
        if (!(entity instanceof Player player)) {
            return false;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);
        return data.isDragon() && data.species() != null && data.species().is(DSDragonSpeciesTags.TRUE_DRAGONS);
    }

    private static boolean isEffect(final MobEffect effect, final net.minecraft.tags.TagKey<MobEffect> tag) {
        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect).is(tag);
    }

    private static double getStolenTime(final DragonStateHandler handler) {
        int ticksToSteal = Functions.minutesToTicks(30); // TODO :: make this configurable in the enchantment
        DragonStage level = handler.stage().value();
        return level.ticksToGrowth(ticksToSteal);
    }
}
