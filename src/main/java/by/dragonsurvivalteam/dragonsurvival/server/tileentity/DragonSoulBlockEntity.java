package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DragonSoulData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DragonSoulBlockEntity extends BlockEntity {
    private DragonStateHandler handler;
    /** Field only relevant on the client-side */
    public int fakePlayerIndex = -1;
    public double scale = -1;

    public DragonSoulBlockEntity(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.DRAGON_SOUL.get(), position, state);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, DragonSoulBlockEntity dragonSoulBlockEntity) {
    }

    public DragonStateHandler getHandler() {
        if (handler == null) {
            // This is only the case when placed, and at that point the client still has the data component
            handler = new DragonStateHandler();
            //noinspection DataFlowIssue -> level and component expected to be present
            handler.deserializeNBT(level.registryAccess(), components().get(DSDataComponents.DRAGON_SOUL.get()).dragonData());
        }

        return handler;
    }

    public double getScale() {
        if (scale == -1) {
            if (!components().has(DSDataComponents.DRAGON_SOUL.get())) {
                // Can occur before the packet from the server with the data arrives
                // Otherwise the part below only gets called during player placement (in which case the component is present)
                return 1;
            }

            //noinspection DataFlowIssue -> component expected to be present
            scale = components().get(DSDataComponents.DRAGON_SOUL.get()).scale();
        }

        return scale;
    }

    @Override // Responsible for synchronizing the data to the client that joins the world
    public @NotNull CompoundTag getUpdateTag(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);

        DragonSoulData soulData = Objects.requireNonNull(components().get(DSDataComponents.DRAGON_SOUL.get()));
        tag.put(SOUL_DATA, soulData.dragonData());
        tag.putDouble(SCALE, soulData.scale());

        return tag;
    }

    @Override
    public void loadAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains(SCALE)) {
            scale = tag.getDouble(SCALE);
        }

        if (tag.contains(SOUL_DATA)) {
            handler = new DragonStateHandler();
            handler.deserializeNBT(provider, tag.getCompound(SOUL_DATA));
        }
    }

    private static final String SOUL_DATA = "soul_data";
    private static final String SCALE = "scale";
}