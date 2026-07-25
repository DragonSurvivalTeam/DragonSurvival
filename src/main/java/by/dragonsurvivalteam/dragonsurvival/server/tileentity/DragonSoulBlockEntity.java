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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class DragonSoulBlockEntity extends BlockEntity {
    @Translation(key = "soul_block_default_animation", type = Translation.Type.CONFIGURATION, comments = "Default animation for the soul block")
    @ConfigOption(side = ConfigSide.SERVER, category = {"items", "dragon_soul"}, key = "soul_block_default_animation")
    public static String DEFAULT_ANIMATION = "sit";

    public UUID playerUUID;
    public boolean locked;

    /** These fields are only relevant on the client-side */
    public String animation = DEFAULT_ANIMATION;
    public int fakePlayerIndex = -1;
    public int tick;
    public float packetTimeout;

    private DragonStateHandler handler;
    private DragonSoulData soulData;

    public DragonSoulBlockEntity(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.DRAGON_SOUL.get(), position, state);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, DragonSoulBlockEntity dragonSoulBlockEntity) {
        // FIXME :: anything needed here?
    }

    public @Nullable DragonStateHandler getHandler() {
        if ((handler == null || !handler.isDragon()) && soulData != null && level != null) {
            initializeHandler(level.registryAccess(), soulData.dragonData());
        }

        return handler;
    }

    public double getScale() {
        if (soulData == null) {
            // Can occur before the packet from the server with the data arrives
            return 1;
        }

        return soulData.scale();
    }

    @Override
    public @NotNull AABB getRenderBoundingBox() {
        return AABB.ofSize(getBlockPos().getCenter(), 6 * getScale(), 6 * getScale(), 6 * getScale());
    }

    public @Nullable DragonSoulData getSoulData() {
        return soulData;
    }

    public void setSoulData(final @Nullable DragonSoulData data) {
        soulData = data;
        handler = null;
    }

    public CompoundTag saveComponentData() {
        CompoundTag components = new CompoundTag();
        if (soulData != null) {
            components.put(DSDataComponents.DRAGON_SOUL.id().toString(), DSDataComponents.DRAGON_SOUL.encode(soulData));
        }
        return components;
    }

    public void loadComponentData(final CompoundTag components) {
        Tag encoded = components.get(DSDataComponents.DRAGON_SOUL.id().toString());
        setSoulData(encoded != null ? DSDataComponents.DRAGON_SOUL.decode(encoded) : null);
    }

    public boolean canInteract(final Player player) {
        if (!locked || player.isCreative()) {
            return true;
        }

        return playerUUID == null || playerUUID.equals(player.getUUID());
    }

    private void initializeHandler(final HolderLookup.Provider provider, final CompoundTag tag) {
        handler = new DragonStateHandler();
        handler.deserializeNBT(provider, tag);
    }

    @Override // Responsible for synchronizing the data to the client that joins the world
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    protected void saveAdditional(@NotNull final CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(ANIMATION, animation);
        tag.putBoolean(LOCKED, locked);
        tag.put(COMPONENTS, saveComponentData());

        if (playerUUID != null) {
            tag.putUUID(PLAYER_UUID, playerUUID);
        }
    }

    @Override
    public void load(@NotNull final CompoundTag tag) {
        super.load(tag);
        animation = tag.getString(ANIMATION);
        locked = tag.getBoolean(LOCKED);
        loadComponentData(tag.getCompound(COMPONENTS));

        if (tag.hasUUID(PLAYER_UUID)) {
            playerUUID = tag.getUUID(PLAYER_UUID);
        } else {
            playerUUID = null;
        }

        if (level != null && soulData != null) {
            initializeHandler(level.registryAccess(), soulData.dragonData());
        }
    }

    private static final String ANIMATION = "animation";
    private static final String PLAYER_UUID = "player_uuid";
    private static final String LOCKED = "locked";
    private static final String COMPONENTS = "components";
}
