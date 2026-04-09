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
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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

    public DragonSoulBlockEntity(final BlockPos position, final BlockState state) {
        super(DSBlockEntities.DRAGON_SOUL.get(), position, state);
    }

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, DragonSoulBlockEntity dragonSoulBlockEntity) {
        // TODO :: anything needed here?
    }

    public @Nullable DragonStateHandler getHandler() {
        if ((handler == null || !handler.isDragon()) && components().has(DSDataComponents.DRAGON_SOUL.get())) {
            // This is only the case when placed, and at that point the client still has the data component
            //noinspection DataFlowIssue -> level and components are expected to be present
            ValueInput valueInput = TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), components().get(DSDataComponents.DRAGON_SOUL.get()).dragonData());
            initializeHandler(valueInput);
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

    public boolean canInteract(final Player player) {
        if (!locked || player.isCreative()) {
            return true;
        }

        return playerUUID == null || playerUUID.equals(player.getUUID());
    }

    private void initializeHandler(final ValueInput valueInput) {
        handler = new DragonStateHandler();
        handler.deserialize(valueInput);
    }

    @Override // Responsible for synchronizing the data to the client that joins the world
    public @NotNull CompoundTag getUpdateTag(@NotNull final HolderLookup.Provider provider) {
        TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
        saveAdditional(valueOutput);

        // We need to do this because components are not synchronized / retained client-side by default
        // 'BlockEntity.ComponentHelper' is private, but this is the "official" key to serialize components (see 'BlockEntity#loadWithComponents')
        valueOutput.store("components", DataComponentMap.CODEC, components());
        return valueOutput.buildResult();
    }

    @Override
    protected void saveAdditional(@NotNull final ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putString(ANIMATION, animation);
        valueOutput.putBoolean(LOCKED, locked);

        if (playerUUID != null) {
            valueOutput.store(PLAYER_UUID, UUIDUtil.CODEC, playerUUID);
        }
    }

    @Override
    public void loadAdditional(@NotNull final ValueInput valueInput) {
        super.loadAdditional(valueInput);
        animation = valueInput.getString(ANIMATION).orElseThrow();
        locked = valueInput.getBooleanOr(LOCKED, false);

        valueInput.read(PLAYER_UUID, UUIDUtil.CODEC).ifPresentOrElse(UUID -> playerUUID = UUID, () -> playerUUID = null);
        initializeHandler(valueInput);
    }

    private static final String ANIMATION = "animation";
    private static final String PLAYER_UUID = "player_uuid";
    private static final String LOCKED = "locked";
}