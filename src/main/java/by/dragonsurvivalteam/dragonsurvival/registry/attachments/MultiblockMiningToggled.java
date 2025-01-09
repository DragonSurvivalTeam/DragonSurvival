package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class MultiblockMiningToggled implements INBTSerializable<CompoundTag> {
    public boolean enabled = false;

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("enabled", enabled);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, CompoundTag nbt) {
        enabled = nbt.getBoolean("enabled");
    }

    public AttachmentType<?> type() {
        return DSDataAttachments.MULTIBLOCK_MINING_TOGGLED.get();
    }

    public void sync(final ServerPlayer player) {
        player.getExistingData(type()).ifPresent(data -> PacketDistributor.sendToPlayer(player, new SyncData(NeoForgeRegistries.ATTACHMENT_TYPES.getKey(type()), serializeNBT(player.registryAccess()))));
    }
}
