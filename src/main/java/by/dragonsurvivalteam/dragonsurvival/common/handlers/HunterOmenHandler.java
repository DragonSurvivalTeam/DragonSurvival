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
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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

@SuppressWarnings("unused")
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

    public static boolean isNearbyPlayerWithHunterOmen(double detectionRadius, Level level, Entity entity) {
        List<Player> players = level.getEntitiesOfClass(Player.class, entity.getBoundingBox().inflate(detectionRadius));

        for (Player player : players) {
            if (player.hasEffect(DSEffects.HUNTER_OMEN)) {
                return true;
            }
        }
        return false;
    }

    public static ObjectArrayList<ItemStack> getVillagerLoot(AbstractVillager villager, Level level, @Nullable Player player, boolean wasKilled) {
        List<ItemStack> nonEmeraldTrades = new ArrayList<>();
        villager.getOffers().stream().filter(merchantOffer -> merchantOffer.getResult().getItem() != Items.EMERALD).forEach(merchantOffer -> {
            // Be careful to copy the stack, since otherwise if we use this function when the villager is alive (when we steal from them)
            // we will modify the villager's trades and crash the game due to empty stacks.
            nonEmeraldTrades.add(merchantOffer.getResult().copy());
        });

        ObjectArrayList<ItemStack> loot = new ObjectArrayList<>();
        if (!nonEmeraldTrades.isEmpty()) {
            int lootingLevel = 0;
            if (player != null) {
                lootingLevel = EnchantmentUtils.getLevel(player, Enchantments.LOOTING);
            }
            int numRolls = Math.min(lootingLevel + 1, nonEmeraldTrades.size());
            for (int i = 0; i < numRolls; i++) {
                int roll = level.getRandom().nextInt(nonEmeraldTrades.size());
                loot.add(nonEmeraldTrades.get(roll));
                nonEmeraldTrades.remove(roll);
            }
        }

        // Have the cartographer drop a hunter's castle map if the player kills them (but not from stealing)
        if (wasKilled && villager instanceof Villager realVillager) {
            if (realVillager.getVillagerData().getProfession() == VillagerProfession.CARTOGRAPHER) {
                ServerLevel serverlevel = (ServerLevel) level;
                BlockPos blockpos = serverlevel.findNearestMapStructure(DSTrades.ON_DRAGON_HUNTERS_CASTLE_MAPS, realVillager.blockPosition(), 100, true);
                if (blockpos != null) {
                    ItemStack itemstack = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), (byte) 2, true, true);
                    MapItem.renderBiomePreviewMap(serverlevel, itemstack);
                    MapItemSavedData.addTargetDecoration(itemstack, blockpos, "+", DSMapDecorationTypes.DRAGON_HUNTER);
                    itemstack.set(DataComponents.ITEM_NAME, Component.translatable(LangKey.ITEM_KINGDOM_EXPLORER_MAP));
                    boolean lootHasMapOfSameType = loot.stream().anyMatch(stack -> {
                        if (stack.getItem() == itemstack.getItem()) {
                            MapDecorations mapDecorations = stack.get(DataComponents.MAP_DECORATIONS);
                            if (mapDecorations != null) {
                                return mapDecorations.decorations().values().stream().anyMatch(
                                        mapDecoration -> mapDecoration.type() == DSMapDecorationTypes.DRAGON_HUNTER
                                );
                            }
                        }
                        return false;
                    });

                    if (!lootHasMapOfSameType) {
                        loot.add(itemstack);
                    }
                }
            }
        }

        return loot;
    }

    // I want to use a loot modifier here, but it isn't possible since the villagers don't have an initial loot table to begin with.
    @SubscribeEvent
    public static void modifyDropsForVillagers(LivingDeathEvent deathEvent) {
        Entity entity = deathEvent.getEntity();
        Entity killer = deathEvent.getSource().getEntity();
        if (killer instanceof Player player) {
            if (entity instanceof AbstractVillager abstractVillager) {
                ObjectArrayList<ItemStack> loot = getVillagerLoot(abstractVillager, player.level(), player, true);
                for (ItemStack stack : loot) {
                    entity.spawnAtLocation(stack);
                }

                int experience;
                if (entity instanceof Villager villager) {
                    experience = (int) Math.pow(2.0, villager.getVillagerData().getLevel());
                } else {
                    // This happens with the wandering trader in vanilla.
                    experience = 4;
                }
                player.level().addFreshEntity(new ExperienceOrb(player.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(), experience));
            }
        }
    }

    @SubscribeEvent
    public static void preserveHunterOmenOnRespawn(LivingDeathEvent deathEvent) {
        LivingEntity livingEntity = deathEvent.getEntity();
        if (livingEntity instanceof Player player) {
            EffectsMaintainedThroughDeath effectsMaintainedThroughDeath = EffectsMaintainedThroughDeath.getData(player);
            if (player.hasEffect(DSEffects.HUNTER_OMEN)) {
                effectsMaintainedThroughDeath.addEffect(player.getEffect(DSEffects.HUNTER_OMEN));
            }
        }
    }

    private static void applyHunterOmenFromKilling(final Player player) {
        MobEffectInstance instance = player.getEffect(DSEffects.HUNTER_OMEN);
        int duration = instance != null ? instance.getDuration() : 0;

        // Double the duration unless it would add more than 30 minutes to the timer, but add a minimum of 1 minute
        player.addEffect(new MobEffectInstance(DSEffects.HUNTER_OMEN, Math.max(Functions.minutesToTicks(1), Math.min(duration * 2, Functions.minutesToTicks(30))), 0, false, false, true));
    }

    @SubscribeEvent
    public static void voidsHunterOmen(MobEffectEvent.Added potionAddedEvent) {
        MobEffectInstance effectInstance = potionAddedEvent.getEffectInstance();
        LivingEntity livingEntity = potionAddedEvent.getEntity();
        if (effectInstance != null && effectInstance.getEffect() == MobEffects.HERO_OF_THE_VILLAGE) {
            livingEntity.removeEffect(DSEffects.HUNTER_OMEN);
        }
    }

    @SubscribeEvent
    public static void ironGolemTargetsMarkedPlayers(EntityJoinLevelEvent joinWorldEvent) {
        Entity entity = joinWorldEvent.getEntity();
        if (entity instanceof IronGolem golemEntity) {
            golemEntity.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(golemEntity, Player.class, 0, true, false, livingEntity -> livingEntity.hasEffect(DSEffects.HUNTER_OMEN)));
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