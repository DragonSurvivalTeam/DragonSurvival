package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class BaseBlockBlockEntity extends BlockEntity {
    public BaseBlockBlockEntity(BlockEntityType<?> pType, BlockPos pWorldPosition, BlockState pBlockState) {
        super(pType, pWorldPosition, pBlockState);
    }

    @Nullable @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider pRegistries) {
        CompoundTag parentTag = super.getUpdateTag(pRegistries);
        saveAdditional(parentTag, pRegistries);
        return parentTag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.@NotNull Provider pRegistries) {
        super.handleUpdateTag(tag, pRegistries);
        loadAdditional(tag, pRegistries);
    }

    public int getX() {
        return getBlockPos().getX();
    }

    public int getY() {
        return getBlockPos().getY();
    }

    public int getZ() {
        return getBlockPos().getZ();
    }
}