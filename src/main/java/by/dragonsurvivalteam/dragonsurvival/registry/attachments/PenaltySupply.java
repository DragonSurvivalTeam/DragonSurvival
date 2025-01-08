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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber
public class PenaltySupply implements INBTSerializable<CompoundTag> {
    private final HashMap<ResourceLocation, Data> supplyData = new HashMap<>();

    public boolean hasSupply(final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return false;
        }

        return data.getSupply() > 0;
    }

    public void setSupply(final ResourceLocation supplyType, float supply) {
        Data data = supplyData.get(supplyType);

        if (data != null) {
            data.currentSupply = supply;
        }
    }

    public float getPercentage(final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return 0;
        }

        return (data.getSupply() / data.getMaximumSupply());
    }

    public int getMaxSupply(final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return -1;
        }

        return (int) data.getMaximumSupply();
    }

    public void reduce(final ServerPlayer player, final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return;
        }

        data.reduce();
        PacketDistributor.sendToPlayer(player, new SyncPenaltySupplyAmount(supplyType, data.getSupply()));
    }

    public void regenerate(final ServerPlayer player, final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return;
        }

        data.regenerate();
        PacketDistributor.sendToPlayer(player, new SyncPenaltySupplyAmount(supplyType, data.getSupply()));
    }

    public Optional<Holder<DragonPenalty>> getMatchingPenalty(final ResourceLocation supplyType, final DragonStateHandler handler) {
        if (handler.species() == null) {
            return Optional.empty();
        }

        for (Holder<DragonPenalty> penalty : handler.species().value().penalties()) {
            if (penalty.value().trigger() instanceof SupplyTrigger supplyTrigger && supplyTrigger.supplyType().equals(supplyType)) {
                // TODO :: return a list?
                return Optional.of(penalty);
            }
        }

        return Optional.empty();
    }

    public void initialize(final ResourceLocation supplyType, float maximumSupply, float reductionRate, float regenerationRate) {
        supplyData.put(supplyType, new Data(maximumSupply, maximumSupply, reductionRate, regenerationRate));
    }

    public void sync(final ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new SyncPenaltySupply(serializeNBT(player.registryAccess())));
    }

    public List<ResourceLocation> getSupplyTypes() {
        return List.copyOf(supplyData.keySet());
    }

    public void remove(final ResourceLocation supplyType) {
        supplyData.remove(supplyType);
    }

    public static void clear(final Player player) {
        player.getExistingData(DSDataAttachments.PENALTY_SUPPLY).ifPresent(data -> {
            data.supplyData.clear();

            if (player instanceof ServerPlayer serverPlayer) {
                data.sync(serverPlayer);
            }

            player.removeData(DSDataAttachments.PENALTY_SUPPLY);
        });
    }

    @SubscribeEvent
    public static void onRegenerationItemConsumed(final LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        player.getExistingData(DSDataAttachments.PENALTY_SUPPLY).ifPresent(data -> data.replenishSupplyFromItemStack(player, event.getItem()));
    }

    private void replenishSupplyFromItemStack(final ServerPlayer player, final ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        supplyData.forEach((supplyType, data) -> {
            // Get the matching penalty trigger
            Optional<Holder<DragonPenalty>> penalty = getMatchingPenalty(supplyType, handler);

            if (penalty.isEmpty()) {
                return;
            }

            // Check if the item stack is in the recovery items list
            if (penalty.get().value().trigger() instanceof SupplyTrigger supplyTrigger) {
                List<SupplyTrigger.RecoveryItems> recoveryList = supplyTrigger.recoveryItems();

                PotionUtils.getPotion(stack).ifPresentOrElse(potion -> {
                    for (SupplyTrigger.RecoveryItems recoveryItems : recoveryList) {
                        for (Holder<Potion> potions : recoveryItems.potions()) {
                            if (potions.value() == potion) {
                                regenerateManual(player, supplyType, recoveryItems.percentRestored());
                                break;
                            }
                        }
                    }
                }, () -> {
                    for (SupplyTrigger.RecoveryItems recoveryItems : recoveryList) {
                        if (recoveryItems.items().contains(stack.getItemHolder())) {
                            regenerateManual(player, supplyType, recoveryItems.percentRestored());
                            break;
                        }
                    }
                });
            }
        });
    }

    private void regenerateManual(final ServerPlayer player, final ResourceLocation supplyType, final float amount) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return;
        }

        data.regeneratePercentage(amount);
        PacketDistributor.sendToPlayer(player, new SyncPenaltySupplyAmount(supplyType, data.getSupply()));
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        supplyData.forEach((supplyType, value) -> tag.put(supplyType.toString(), value.serializeNBT()));
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        supplyData.clear();
        tag.getAllKeys().forEach(supplyType -> {
            ResourceLocation resource = ResourceLocation.tryParse(supplyType);

            if (resource != null) {
                supplyData.put(resource, Data.deserializeNBT(tag.getCompound(supplyType)));
            }
        });
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
