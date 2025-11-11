package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ServerData;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class DragonSoulBlockEntity extends BlockEntity {
    private int fakePlayerIndex = -1;
    private DragonStateHandler handler;
    private AABB renderBoundingBox;
    private VoxelShape shape;

    public DragonSoulBlockEntity(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.DRAGON_SOUL.get(), position, state);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, DragonSoulBlockEntity dragonSoulBlockEntity) {
    }

    public int getFakePlayerIndex() {
        return fakePlayerIndex;
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

    public AABB getRenderBoundingBox() {
        if (renderBoundingBox == null) {
            DragonStateHandler handler = getHandler();
            EntityDimensions dimensions = DragonSizeHandler.calculateDimensions(handler, null, handler.previousPose);
            this.renderBoundingBox = AABB.ofSize(worldPosition.getCenter(), dimensions.width(), dimensions.height(), dimensions.width());
        }

        return renderBoundingBox;
    }

    public VoxelShape getShape() {
        if (shape == null) {
            DragonStateHandler handler = getHandler();
            EntityDimensions dimensions = DragonSizeHandler.calculateDimensions(handler, null, handler.previousPose);
            // The position only needs to be set for the rendering box to properly cull the block entity
            shape = Shapes.create(dimensions.makeBoundingBox(Vec3.ZERO));
        }

        return shape;
    }

    @Override // Responsible for synchronizing the data to the client that joins the world
    public @NotNull CompoundTag getUpdateTag(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        //noinspection DataFlowIssue -> component expected to be present
        tag.put(SOUL_DATA, components().get(DSDataComponents.DRAGON_SOUL.get()).dragonData());
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

        if (tag.contains(SOUL_DATA)) {
            handler = new DragonStateHandler();
            handler.deserializeNBT(provider, tag.getCompound(SOUL_DATA));
        }
    }

    @Override
    public void saveAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt(FAKE_PLAYER_INDEX, fakePlayerIndex);
//        tag.put(SOUL_DATA, getHandler().serializeNBT(provider));
    }

    private static final String FAKE_PLAYER_INDEX = "fake_player_index";
    public static final String SOUL_DATA = "soul_data";
}