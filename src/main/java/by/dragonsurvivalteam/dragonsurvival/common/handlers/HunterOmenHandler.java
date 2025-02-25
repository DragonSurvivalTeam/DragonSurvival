package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.DSMapDecorationTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSTrades;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.EffectsMaintainedThroughDeath;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber
public class HunterOmenHandler {
    @SubscribeEvent
    public static void applyHunterOmenOnMurderedEntities(final LivingDeathEvent deathEvent) {
        LivingEntity livingEntity = deathEvent.getEntity();
        Entity killer = deathEvent.getSource().getEntity();

        if (killer instanceof Player player && livingEntity.getType().is(DSEntityTypeTags.HUNTER_FACTION)) {
            applyHunterOmenFromKilling(player);
        }
    }

    public static boolean isNearbyPlayerWithHunterOmen(final double detectionRadius, final Level level, final Entity entity) {
        return !level.getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(detectionRadius), player -> !player.isCreative() && !player.isSpectator() && player.hasEffect(DSEffects.HUNTER_OMEN)).isEmpty();
    }

    public static List<ItemStack> generateVillagerLoot(final AbstractVillager genericVillager, final Level level, @Nullable final Player player, boolean wasKilled) {
        List<ItemStack> trades = new ArrayList<>();

        for (MerchantOffer offer : genericVillager.getOffers()) {
            if (offer.getResult().getItem() == Items.EMERALD) {
                continue;
            }

            trades.add(offer.getResult().copy());
        }

        int looting = player != null ? EnchantmentUtils.getLevel(player, Enchantments.LOOTING) : 0;
        int rolls = Math.min(looting + 1, trades.size());
        List<ItemStack> loot = new ArrayList<>();

        for (int i = 0; i < rolls; i++) {
            int roll = level.getRandom().nextInt(trades.size());
            loot.add(trades.remove(roll));
        }

        if (!wasKilled || !(genericVillager instanceof Villager villager)) {
            return loot;
        }

        // Have the cartographer drop a hunter's castle map if the player kills them (but not from stealing)
        if (level instanceof ServerLevel serverLevel && villager.getVillagerData().getProfession() == VillagerProfession.CARTOGRAPHER) {
            for (ItemStack stack : loot) {
                if (stack.getItem() != Items.MAP) {
                    continue;
                }

                MapDecorations decorations = stack.get(DataComponents.MAP_DECORATIONS);

                if (decorations == null) {
                    continue;
                }

                for (MapDecorations.Entry entry : decorations.decorations().values()) {
                    if (entry.type() == DSMapDecorationTypes.DRAGON_HUNTER) {
                        // Map is already part of the loot
                        return loot;
                    }
                }
            }

            BlockPos castlePosition = serverLevel.findNearestMapStructure(DSTrades.ON_DRAGON_HUNTERS_CASTLE_MAPS, villager.blockPosition(), 100, true);

            if (castlePosition == null) {
                return loot;
            }

            ItemStack map = MapItem.create(serverLevel, castlePosition.getX(), castlePosition.getZ(), (byte) 2, true, true);
            map.set(DataComponents.ITEM_NAME, Component.translatable(LangKey.ITEM_KINGDOM_EXPLORER_MAP));

            MapItem.renderBiomePreviewMap(serverLevel, map);
            MapItemSavedData.addTargetDecoration(map, castlePosition, "+", DSMapDecorationTypes.DRAGON_HUNTER);

            loot.add(map);
        }

        return loot;
    }

    @SubscribeEvent // Loot modifier cannot be used since villagers do not have a loot table
    public static void modifyDropsForVillagers(final LivingDeathEvent event) {
        if (!(event.getEntity() instanceof AbstractVillager genericVillager) || !(event.getSource().getEntity() instanceof Player player)) {
            return;
        }

        generateVillagerLoot(genericVillager, player.level(), player, true).forEach(genericVillager::spawnAtLocation);
        int experience;

        if (genericVillager instanceof Villager villager) {
            experience = (int) Math.pow(2, villager.getVillagerData().getLevel());
        } else {
            // This happens with the wandering trader in vanilla.
            experience = 4;
        }

        player.level().addFreshEntity(new ExperienceOrb(player.level(), genericVillager.getX(), genericVillager.getY() + 0.5, genericVillager.getZ(), experience));
    }

    @SubscribeEvent
    public static void preserveHunterOmenOnRespawn(final LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        EffectsMaintainedThroughDeath effects = EffectsMaintainedThroughDeath.getData(player);

        if (player.hasEffect(DSEffects.HUNTER_OMEN)) {
            effects.addEffect(player.getEffect(DSEffects.HUNTER_OMEN));
        }
    }

    private static void applyHunterOmenFromKilling(final Player player) {
        MobEffectInstance instance = player.getEffect(DSEffects.HUNTER_OMEN);
        int duration = instance != null ? instance.getDuration() : 0;

        // Double the duration unless it would add more than 30 minutes to the timer, but add a minimum of 1 minute
        player.addEffect(new MobEffectInstance(DSEffects.HUNTER_OMEN, Math.max(Functions.minutesToTicks(1), Math.min(duration * 2, Functions.minutesToTicks(30))), 0, false, false, true));
    }

    @SubscribeEvent
    public static void voidsHunterOmen(final MobEffectEvent.Added event) {
        if (event.getEffectInstance().getEffect() == MobEffects.HERO_OF_THE_VILLAGE) {
            event.getEntity().removeEffect(DSEffects.HUNTER_OMEN);
        }
    }

    @SubscribeEvent
    public static void ironGolemTargetsMarkedPlayers(final EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof IronGolem golem) {
            golem.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(golem, Player.class, 0, true, false, livingEntity -> livingEntity.hasEffect(DSEffects.HUNTER_OMEN)));
        }
    }

    @SubscribeEvent // Handles the damage modification and the
    public static void handleVillagerAttack(final LivingIncomingDamageEvent event) {
        Entity target = event.getEntity();
        Player attacker = event.getSource().getEntity() instanceof Player player ? player : null;

        if (attacker == null || !target.getType().is(DSEntityTypeTags.HUNTER_FACTION)) {
            return;
        }

        double multiplier = attacker.getAttributeValue(DSAttributes.HUNTER_FACTION_DAMAGE);
        double damage = event.getAmount() * multiplier;

        if (damage == 0) {
            event.setCanceled(true);
            return;
        }

        MobEffectInstance effect = attacker.getEffect(DSEffects.HUNTER_OMEN);
        int duration = 0;

        if (effect != null) {
            duration = effect.getDuration();
        }

        if (EnchantmentUtils.getLevel(attacker, DSEnchantments.CURSE_OF_KINDNESS) < 1) {
            attacker.addEffect(new MobEffectInstance(DSEffects.HUNTER_OMEN, duration + Functions.secondsToTicks(5), 0, false, false, true));
        }
    }
}