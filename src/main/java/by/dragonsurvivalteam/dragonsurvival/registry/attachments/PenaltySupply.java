package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncPenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncPenaltySupplyAmount;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.SupplyTrigger;
import by.dragonsurvivalteam.dragonsurvival.util.PotionUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber
public class PenaltySupply implements INBTSerializable<CompoundTag> {
    private final HashMap<String, Data> supplyData = new HashMap<>();

    @SubscribeEvent
    public static void applyPenalties(final PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(serverPlayer);
        PenaltySupply data = getData(serverPlayer);

        if (!handler.isDragon()) {
            data.clear();
            return;
        }

        for (Holder<DragonPenalty> penalty : handler.getDragonType().value().penalties()) {
            penalty.value().apply(serverPlayer);
        }

        // Remove any penalties the player no longer has
        for (String id : data.getSupplyTypes()) {
            if (handler.getDragonType().value().penalties().stream().noneMatch(penalty -> penalty.value().trigger().id().equals(id))) {
                data.remove(id);
            }
        }
    }

    @SubscribeEvent
    public static void onRegenerationItemConsumed(LivingEntityUseItemEvent.Finish destroyItemEvent) {
        if (!(destroyItemEvent.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        getData(player).replenishSupplyFromItemStack(player, destroyItemEvent.getItem());
    }

    public static PenaltySupply getData(final Player player) {
        return player.getData(DSDataAttachments.PENALTY_SUPPLY);
    }

    public List<String> getSupplyTypes() {
        return List.copyOf(supplyData.keySet());
    }

    public void syncPenaltySupplyToPlayer(final ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncPenaltySupply(serializeNBT(player.registryAccess())));
    }

    public void clear() {
        supplyData.clear();
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        supplyData.forEach((key, value) -> tag.put(key, value.serializeNBT()));
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        supplyData.clear();
        tag.getAllKeys().forEach(key -> supplyData.put(key, Data.deserializeNBT(tag.getCompound(key))));
    }

    public boolean hasSupply(final String supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return false;
        }

        return data.getSupply() > 0;
    }

    public void setSupply(final String supplyType, float supply) {
        Data data = supplyData.get(supplyType);

        if (data != null) {
            data.currentSupply = supply;
        }
    }

    public float getPercentage(final String supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return 0;
        }

        return (data.getSupply() / data.getMaximumSupply());
    }

    public int getMaxSupply(final String supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return -1;
        }

        return (int) data.getMaximumSupply();
    }

    public void reduce(final ServerPlayer player, final String supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return;
        }

        data.reduce();
        PacketDistributor.sendToPlayer(player, new SyncPenaltySupplyAmount(supplyType, data.getSupply()));
    }

    public void regenerate(final ServerPlayer player, final String supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return;
        }

        data.regenerate();
        PacketDistributor.sendToPlayer(player, new SyncPenaltySupplyAmount(supplyType, data.getSupply()));
    }

    public Optional<Holder<DragonPenalty>> getMatchingPenalty(final String supplyType, final DragonStateHandler handler) {
        return handler.getDragonType().value().penalties().stream().filter(penalty -> penalty.value().trigger().id().equals(supplyType)).findFirst();
    }

    public void replenishSupplyFromItemStack(final ServerPlayer player, final ItemStack itemStack) {
        if(itemStack.isEmpty()) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        supplyData.forEach((supplyType, data) -> {
                    // Get the matching penalty trigger
                    Optional<Holder<DragonPenalty>> matchingPenalty = getMatchingPenalty(supplyType, handler);
                    if (matchingPenalty.isPresent()) {
                        // Check if the item stack is in the recovery items list
                        if (matchingPenalty.get().value().trigger() instanceof SupplyTrigger supplyTrigger) {
                            Optional<Potion> potion = PotionUtils.getPotion(itemStack);
                            List<SupplyTrigger.RecoveryItems> recoveryItemsList = supplyTrigger.recoveryItems();
                            if(potion.isPresent()) {
                                for(SupplyTrigger.RecoveryItems recoveryItems : recoveryItemsList) {
                                    for(Holder<Potion> potionHolder : recoveryItems.potions()) {
                                        if(potionHolder.value().equals(potion.get())) {
                                            regenerateManual(player, supplyType, recoveryItems.percentRestored());
                                            break;
                                        }
                                    }
                                }
                            } else {
                                for(SupplyTrigger.RecoveryItems recoveryItems : recoveryItemsList) {
                                    if(recoveryItems.items().contains(itemStack.getItemHolder())) {
                                        regenerateManual(player, supplyType, recoveryItems.percentRestored());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
        );
    }

    private void regenerateManual(final ServerPlayer player, final String supplyType, final float amount) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return;
        }

        data.regeneratePercentage(amount);
        PacketDistributor.sendToPlayer(player, new SyncPenaltySupplyAmount(supplyType, data.getSupply()));
    }

    public void initialize(final String supplyType, float maximumSupply, float reductionRate, float regenerationRate) {
        supplyData.put(supplyType, new Data(maximumSupply, maximumSupply, reductionRate, regenerationRate));
    }

    public void remove(final String supplyType) {
        supplyData.remove(supplyType);
    }

    private static class Data {
        private static final String MAXIMUM_SUPPLY = "maximum_supply";
        private static final String CURRENT_SUPPLY = "current_supply";
        private static final String REDUCTION_RATE_MULTIPLIER = "reduction_rate_multiplier";
        private static final String REGENERATION_RATE = "regeneration_rate";

        private final float maximumSupply;
        private float currentSupply;

        private final float reductionRateMultiplier;
        private final float regenerationRate;

        public Data(float maximumSupply, float currentSupply, float reductionRateMultiplier, float regenerationRate) {
            this.maximumSupply = maximumSupply;
            this.currentSupply = currentSupply;
            this.reductionRateMultiplier = reductionRateMultiplier;
            this.regenerationRate = regenerationRate;
        }

        public float getSupply() {
            return currentSupply;
        }

        public float getMaximumSupply() {
            return maximumSupply;
        }

        public void reduce() {
            currentSupply = Math.max(0, currentSupply - reductionRateMultiplier);
        }

        public void regenerate() {
            currentSupply = Math.min(maximumSupply, currentSupply + maximumSupply * regenerationRate);
        }

        public void regeneratePercentage(final float amount) {
            currentSupply = Math.min(maximumSupply, currentSupply + maximumSupply * amount);
        }

        public CompoundTag serializeNBT() {
            CompoundTag tag = new CompoundTag();
            tag.putFloat(MAXIMUM_SUPPLY, maximumSupply);
            tag.putFloat(CURRENT_SUPPLY, currentSupply);
            tag.putFloat(REDUCTION_RATE_MULTIPLIER, reductionRateMultiplier);
            tag.putFloat(REGENERATION_RATE, regenerationRate);

            return tag;
        }

        public static Data deserializeNBT(@NotNull final CompoundTag tag) {
            float maximumSupply = tag.getFloat(MAXIMUM_SUPPLY);
            float currentSupply = tag.getFloat(CURRENT_SUPPLY);
            float reductionRate = tag.getFloat(REDUCTION_RATE_MULTIPLIER);
            float regenerationRate = tag.getFloat(REGENERATION_RATE);

            return new Data(maximumSupply, currentSupply, reductionRate, regenerationRate);
        }
    }
}
