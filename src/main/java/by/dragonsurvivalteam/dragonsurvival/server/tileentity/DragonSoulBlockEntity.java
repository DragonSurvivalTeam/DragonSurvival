package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ServerData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class DragonSoulBlockEntity extends BlockEntity {
    private int fakePlayerIndex = -1;
    private DragonStateHandler handler;

    public DragonSoulBlockEntity(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.DRAGON_SOUL.get(), position, state);
    }

    /** Gets called client- and server-side, i.e. no need to sync*/
    public void initialize(final Tag nbt) {
        getPersistentData().put(SOUL_DATA, nbt);
        //noinspection DataFlowIssue -> level is present at this point
        fakePlayerIndex = ServerData.getNextID(level);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, DragonSoulBlockEntity dragonSoulBlockEntity) {
    }

    public int getFakePlayerIndex() {
        return fakePlayerIndex;
    }

    public DragonStateHandler getHandler() {
        if (handler == null) {
            handler = new DragonStateHandler();
            //noinspection DataFlowIssue -> level is present at this point
            handler.deserializeNBT(level.registryAccess(), getPersistentData().getCompound(SOUL_DATA));
        }

        return handler;
    }

    @Override // Responsible for synchronizing the data to the client that joins the world
    public @NotNull CompoundTag getUpdateTag(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        if (level != null) {
            ServerData.remove(level, fakePlayerIndex);
        }
    }

    @Override
    public void loadAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        fakePlayerIndex = tag.getInt(FAKE_PLAYER_INDEX);
        handler = new DragonStateHandler();
        handler.deserializeNBT(provider, tag.getCompound(SOUL_DATA)); // FIXME :: use proper codec so the soul data is properly stored and then set back to the item on block drop
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt(FAKE_PLAYER_INDEX, fakePlayerIndex);
        tag.put(SOUL_DATA, getHandler().serializeNBT(provider));
    }

    private static final String FAKE_PLAYER_INDEX = "fake_player_index";
    public static final String SOUL_DATA = "soul_data";
}