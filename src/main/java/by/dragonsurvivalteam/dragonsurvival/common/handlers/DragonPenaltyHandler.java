package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.ItemUsedTrigger;
import by.dragonsurvivalteam.dragonsurvival.server.containers.slots.ClawToolSlot;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.PotionUtils;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.inventory.ArmorSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Optional;

@EventBusSubscriber
public class DragonPenaltyHandler {
    @SubscribeEvent
    public static void applyPenalties(final PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(serverPlayer);
        PenaltySupply data = PenaltySupply.getData(serverPlayer);

        if (!handler.isDragon()) {
            data.clear();
            return;
        }

        for (Holder<DragonPenalty> penalty : handler.species().value().penalties()) {
            if (penalty.value().trigger().hasCustomTrigger()) {
                continue;
            }

            penalty.value().apply(serverPlayer);
        }

        // Remove any penalties the player no longer has
        for (String id : data.getSupplyTypes()) {
            if (handler.species().value().penalties().stream().noneMatch(penalty -> penalty.value().trigger().id().equals(id))) {
                data.remove(id);
            }
        }
    }

    @SubscribeEvent
    public static void applyItemConsumedPenalties(final LivingEntityUseItemEvent.Finish event){
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(serverPlayer);

        if (!handler.isDragon()) {
            return;
        }

        for (Holder<DragonPenalty> penalty : handler.species().value().penalties()) {
            //noinspection DeconstructionCanBeUsed -> spotless is too stupid to handle this
            if (penalty.value().trigger() instanceof ItemUsedTrigger trigger && trigger.condition().test(Condition.createContext(serverPlayer.serverLevel(), event.getItem()))) {
                penalty.value().apply(serverPlayer);
            }
        }
    }

    @SubscribeEvent // TODO :: remove this logic entirely? have some sort of config or penalty?
    public static void hitByWaterPotion(final ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof ThrownPotion potion) {
            if (potion.getItem().getItem() != Items.SPLASH_POTION) {
                return;
            }

            Optional<Potion> potionData = PotionUtils.getPotion(potion.getItem());
            // If we have no data here, just default to doing nothing (some mods do strange things with potion items that have no Potion data)
            if (potionData.isEmpty() || !potionData.get().getEffects().isEmpty()) {
                return;
            }

            Vec3 pos = event.getRayTraceResult().getLocation();
            List<Player> entities = potion.level().getEntities(EntityType.PLAYER, new AABB(pos.x - 5, pos.y - 1, pos.z - 5, pos.x + 5, pos.y + 1, pos.z + 5), entity -> entity.position().distanceTo(pos) <= 4);

            for (Player player : entities) {
                if (player.hasEffect(DSEffects.FIRE)) {
                    continue;
                }

                DragonStateProvider.getOptional(player).ifPresent(dragonStateHandler -> {
                    if(dragonStateHandler.isDragon()){
                        if(dragonStateHandler.species() == null || !DragonUtils.isType(dragonStateHandler, BuiltInDragonSpecies.CAVE)){
                            return;
                        }
                        // FIXME
                        player.hurt(new DamageSource(DSDamageTypes.get(player.level(), DSDamageTypes.WATER_BURN)), 0/*CaveDragonConfig.caveSplashDamage.floatValue()*/);
                    }
                });
            }
        }
    }

    @SubscribeEvent // Prevent the player from equipping blacklisted armor (or from mixing light and dark dragon armor)
    public static void preventEquipment(final ItemStackedOnOtherEvent event) {
        ItemStack stack = event.getStackedOnItem(); // FIXME :: this is probably a neoforge bug, this should be carried item -> might be changed in the future
        Player player = event.getPlayer();

        if (stack.isEmpty()) {
            return;
        }

        // Will have to see what type of slots modded inventories may use
        if (!(event.getSlot() instanceof ArmorSlot) && !(event.getSlot() instanceof ClawToolSlot)) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (data.isDragon() && data.species().value().isItemBlacklisted(stack.getItem())) {
            event.setCanceled(true);
            return;
        }

        boolean isLightArmor = stack.is(DSItemTags.LIGHT_ARMOR);

        if (isLightArmor && player.hasEffect(DSEffects.HUNTER_OMEN)) {
            event.setCanceled(true);
            return;
        }

        boolean isDarkArmor = stack.is(DSItemTags.DARK_ARMOR);

        for (ItemStack armor : player.getArmorSlots()) {
            if (armor.isEmpty()) {
                continue;
            }

            boolean isActionInvalid = false;

            if (isDarkArmor && armor.is(DSItemTags.LIGHT_ARMOR)) {
                isActionInvalid = true;
            } else if (isLightArmor && (armor.is(DSItemTags.DARK_ARMOR))) {
                isActionInvalid = true;
            }

            if (isActionInvalid) {
                event.setCanceled(true);
                return;
            }
        }
    }
}