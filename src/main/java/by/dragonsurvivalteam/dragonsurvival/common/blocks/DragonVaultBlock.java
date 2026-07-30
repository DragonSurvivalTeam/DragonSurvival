package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.server.tileentity.DragonVaultBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DragonVaultBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty OMINOUS = BooleanProperty.create("ominous");
    public static final EnumProperty<DragonVaultState> STATE = EnumProperty.create("vault_state", DragonVaultState.class);

    private final ResourceLocation keyItem;
    private final ResourceLocation lootTable;

    public DragonVaultBlock(final BlockBehaviour.Properties properties, final ResourceLocation keyItem, final ResourceLocation lootTable) {
        super(properties);
        this.keyItem = keyItem;
        this.lootTable = lootTable;
        registerDefaultState(stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(STATE, DragonVaultState.INACTIVE)
                .setValue(OMINOUS, false));
    }

    public ResourceLocation keyItem() {
        return keyItem;
    }

    public ResourceLocation lootTable() {
        return lootTable;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull InteractionResult use(
            @NotNull final BlockState state,
            final Level level,
            @NotNull final BlockPos pos,
            @NotNull final Player player,
            @NotNull final InteractionHand hand,
            @NotNull final BlockHitResult hit
    ) {
        ItemStack heldItem = player.getItemInHand(hand);
        if (heldItem.isEmpty() || state.getValue(STATE) != DragonVaultState.ACTIVE) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof DragonVaultBlockEntity vault) {
            vault.tryInsertKey(player, heldItem);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull final BlockPos pos, @NotNull final BlockState state) {
        return new DragonVaultBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, STATE, OMINOUS);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            final Level level,
            @NotNull final BlockState state,
            @NotNull final BlockEntityType<T> type
    ) {
        return createTickerHelper(
                type,
                by.dragonsurvivalteam.dragonsurvival.registry.DSBlockEntities.DRAGON_VAULT.get(),
                level.isClientSide ? DragonVaultBlockEntity::clientTick : DragonVaultBlockEntity::serverTick
        );
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull BlockState rotate(@NotNull final BlockState state, final Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull BlockState mirror(@NotNull final BlockState state, final Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public @NotNull RenderShape getRenderShape(@NotNull final BlockState state) {
        return RenderShape.MODEL;
    }
}
