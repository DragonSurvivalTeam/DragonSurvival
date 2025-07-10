package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDamageTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEffectTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ArrowLooseEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EventBusSubscriber
public class EnchantmentEffectHandler {
    @SubscribeEvent
    public static void fireCrossbow(ArrowLooseEvent event) {
        if (!(event.getBow().getItem() instanceof CrossbowItem)) {
            return;
        }

        if (EnchantmentUtils.getLevel(event.getLevel(), DSEnchantments.BOLAS, event.getBow()) > 0) {
            ChargedProjectiles charged = event.getBow().get(DataComponents.CHARGED_PROJECTILES);

            if (charged != null) {
                List<ItemStack> ammo = charged.getItems();
                List<ItemStack> projectiles = new ArrayList<>();

                for (ItemStack itemStack : ammo) {
                    projectiles.add(itemStack.getItem() instanceof ArrowItem ? new ItemStack(DSItems.BOLAS.value()) : itemStack);
                }

                event.getBow().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(projectiles));
            }
        }
    }

    @SubscribeEvent
    public static void handleDragonsbaneEnchantment(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getSource().getEntity() instanceof Player attacker)) {
            return;
        }

        if (!victim.hasEffect(DSEffects.HUNTER_OMEN)) {
            return;
        }

        ItemStack weapon = event.getSource().getWeaponItem();

        if (weapon == null) {
            return;
        }

        int enchantmentLevel = EnchantmentUtils.getLevel(attacker.level(), DSEnchantments.DRAGONSBANE, weapon);

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
        if (event.getEntity() instanceof LivingEntity target && event.getSource().getEntity() instanceof LivingEntity attacker) {
            MobEffectInstance siphon = target.getEffect(DSEffects.BLOOD_SIPHON);

            if (siphon != null) {
                float percentage = 0.01f * (siphon.getAmplifier() + 1);
                attacker.heal(event.getAmount() * percentage);
            }

            if (event.getSource().is(DSDamageTypeTags.DRAGON_MAGIC)) {
                int level = EnchantmentUtils.getLevel(attacker, DSEnchantments.DRACONIC_SUPERIORITY);

                if (level > 0) {
                    event.setAmount(event.getAmount() * 1.2f + (0.08f * level));
                }
            }

            if (event.getEntity().getHealth() == event.getEntity().getMaxHealth()) {
                int level = EnchantmentUtils.getLevel(attacker, DSEnchantments.MURDERERS_CUNNING);

                if (level > 0) {
                    event.setAmount(event.getAmount() * 1.4f + (0.2f * level));
                }
            }

            AttributeInstance armorIgnoreChance = attacker.getAttribute(DSAttributes.ARMOR_IGNORE_CHANCE);

            if (armorIgnoreChance != null && target.getRandom().nextDouble() < armorIgnoreChance.getValue()) {
                event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reductionIn) -> 0);
            }
        }
    }

    private static double getStolenTime(DragonStateHandler handler) {
        int ticksToSteal = Functions.minutesToTicks(30); // TODO :: make this configurable in the enchantment
        DragonStage level = handler.stage().value();
        return level.ticksToGrowth(ticksToSteal);
    }
}
