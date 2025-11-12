package by.dragonsurvivalteam.dragonsurvival.server.tileentity;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DragonSoulData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class DragonSoulBlockEntity extends BlockEntity {
    @Translation(key = "soul_block_default_animation", type = Translation.Type.CONFIGURATION, comments = "Default animation for the soul block")
    @ConfigOption(side = ConfigSide.SERVER, category = {"items", "dragon_soul"}, key = "soul_block_default_animation")
    public static String DEFAULT_ANIMATION = "sit";

    /** These fields are only relevant on the client-side */
    public String animation = DEFAULT_ANIMATION;
    public int fakePlayerIndex = -1;
    public int tick;

    private DragonStateHandler handler;

    public DragonSoulBlockEntity(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.DRAGON_SOUL.get(), position, state);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, DragonSoulBlockEntity dragonSoulBlockEntity) {
        // FIXME :: anything needed here?
    }

    public DragonStateHandler getHandler() {
        if (handler == null && components().has(DSDataComponents.DRAGON_SOUL.get())) {
            // This is only the case when placed, and at that point the client still has the data component
            //noinspection DataFlowIssue -> level and components are expected to be present
            initializeHandler(level.registryAccess(), components().get(DSDataComponents.DRAGON_SOUL.get()).dragonData());
        }

        return handler;
    }

    public double getScale() {
        DragonSoulData data = components().get(DSDataComponents.DRAGON_SOUL.get());

        if (data == null) {
            // Can occur before the packet from the server with the data arrives
            // Otherwise the part below only gets called during player placement (in which case the component is present)
            return 1;
        }

        return data.scale();
    }

    @Override // Responsible for synchronizing the data to the client that joins the world
    public @NotNull CompoundTag getUpdateTag(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);

        // We need to do this because components are not synchronized / retained client-side by default
        // 'BlockEntity.ComponentHelper' is private, but this is the "official" key to serialize components (see 'BlockEntity#loadWithComponents')
        tag.put("components", DataComponentMap.CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), components()).getOrThrow());
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString(ANIMATION, animation);
    }

    @Override
    public void loadAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        animation = tag.getString(ANIMATION);
    }

    private void initializeHandler(final HolderLookup.Provider provider, final CompoundTag tag) {
        handler = new DragonStateHandler();
        handler.deserializeNBT(provider, tag);
    }

    private static final String ANIMATION = "animation";
}