package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncPenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncPenaltySupplyAmount;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.SupplyTrigger;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    public static final int NO_ENTRY = -1;

    private final HashMap<ResourceLocation, Data> supplyData = new HashMap<>();

    public boolean hasSupply(final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return false;
        }

        return data.currentSupply() > 0;
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

        return (data.currentSupply() / data.maximumSupply());
    }

    public float getRawSupply(final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return 0;
        }

        return data.currentSupply();
    }

    public int getMaxSupply(final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return NO_ENTRY;
        }

        return (int) data.maximumSupply();
    }

    public int getCurrentTick(final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return 0;
        }

        return data.currentTick();
    }

    public void tick(final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return;
        }

        data.tick();
    }

    public void resetTick(final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return;
        }

        data.resetTick();
    }

    public void reduce(final ServerPlayer player, final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return;
        }

        if (data.reduce()) {
            PacketDistributor.sendToPlayer(player, new SyncPenaltySupplyAmount(supplyType, data.currentSupply()));
        }
    }

    public void regenerate(final ServerPlayer player, final ResourceLocation supplyType) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return;
        }

        if (data.regenerate()) {
            PacketDistributor.sendToPlayer(player, new SyncPenaltySupplyAmount(supplyType, data.currentSupply()));
        }
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

    public void initialize(final ResourceLocation supplyType, float maximumSupply, float reductionRate, float regenerationRate, float currentSupply, int currentTick) {
        supplyData.put(supplyType, new Data(maximumSupply, currentSupply, reductionRate, regenerationRate, currentTick));
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
            if (stack.isEmpty() || data.currentSupply == data.maximumSupply) {
                return;
            }

            Optional<Holder<DragonPenalty>> penalty = getMatchingPenalty(supplyType, handler);

            if (penalty.isEmpty()) {
                return;
            }

            if (penalty.get().value().trigger() instanceof SupplyTrigger trigger) {
                for (SupplyTrigger.RecoveryItem recovery : trigger.recoveryItems()) {
                    if (recovery.itemPredicates().stream().anyMatch(predicate -> predicate.test(stack))) {
                        regenerateManual(player, supplyType, recovery.percentRestored());
                        stack.consume(1, player);
                        break;
                    }
                }
            }
        });
    }

    private void regenerateManual(final ServerPlayer player, final ResourceLocation supplyType, final float amount) {
        Data data = supplyData.get(supplyType);

        if (data == null) {
            return;
        }

        data.regeneratePercentage(amount);
        PacketDistributor.sendToPlayer(player, new SyncPenaltySupplyAmount(supplyType, data.currentSupply()));
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        supplyData.forEach((supplyType, value) -> tag.put(supplyType.toString(), value.serializeNBT(provider)));
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        supplyData.clear();
        tag.getAllKeys().forEach(supplyType -> {
            ResourceLocation resource = ResourceLocation.tryParse(supplyType);

            if (resource != null) {
                supplyData.put(resource, Data.deserializeNBT(provider, tag.get(supplyType)));
            }
        });
    }

    private static class Data {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("maximum_supply").forGetter(Data::maximumSupply),
                Codec.FLOAT.optionalFieldOf("current_supply", Float.MAX_VALUE).forGetter(Data::currentSupply),
                Codec.FLOAT.fieldOf("reduction_rate").forGetter(Data::reductionRate),
                Codec.FLOAT.fieldOf("regeneration_rate").forGetter(Data::regenerationRate),
                Codec.INT.optionalFieldOf("current_tick", 0).forGetter(Data::currentTick)
        ).apply(instance, Data::new));

        private final float maximumSupply;
        private final float reductionRate;
        private final float regenerationRate;

        private float currentSupply;
        private int currentTick;

        public Data(float maximumSupply, float currentSupply, float reductionRate, float regenerationRate, int currentTick) {
            this.maximumSupply = maximumSupply;
            this.currentSupply = Math.min(maximumSupply, currentSupply);
            this.reductionRate = reductionRate;
            this.regenerationRate = regenerationRate;
            this.currentTick = currentTick;
        }

        public void tick() {
            currentTick++;
        }

        public void resetTick() {
            currentTick = 0;
        }

        public boolean reduce() {
            float oldSupply = currentSupply;
            currentSupply = Math.max(0, currentSupply - reductionRate);
            return oldSupply != currentSupply;
        }

        public boolean regenerate() {
            float oldSupply = currentSupply;
            currentSupply = Math.min(maximumSupply, currentSupply + maximumSupply * regenerationRate);
            return oldSupply != currentSupply;
        }

        public void regeneratePercentage(final float amount) {
            currentSupply = Math.min(maximumSupply, currentSupply + maximumSupply * amount);
        }

        public float maximumSupply() {
            return maximumSupply;
        }

        public float reductionRate() {
            return reductionRate;
        }

        public float regenerationRate() {
            return regenerationRate;
        }

        public float currentSupply() {
            return currentSupply;
        }

        public int currentTick() {
            return currentTick;
        }

        public Tag serializeNBT(final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this)
                    .resultOrPartial(DragonSurvival.LOGGER::error).orElseThrow();
        }

        public static Data deserializeNBT(final HolderLookup.Provider provider, final Tag tag) {
            return CODEC.decode(provider.createSerializationContext(NbtOps.INSTANCE), tag)
                    .resultOrPartial(DragonSurvival.LOGGER::error).orElseThrow().getFirst();
        }

        @Override
        public String toString() {
            return "Data{" +
                    "maximumSupply=" + maximumSupply +
                    ", reductionRate=" + reductionRate +
                    ", regenerationRate=" + regenerationRate +
                    ", currentSupply=" + currentSupply +
                    ", currentTick=" + currentTick +
                    '}';
        }
    }
}
