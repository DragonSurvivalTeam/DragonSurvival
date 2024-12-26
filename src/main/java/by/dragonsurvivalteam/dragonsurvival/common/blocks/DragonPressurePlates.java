package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import javax.annotation.Nullable;

public class DragonPressurePlates extends PressurePlateBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private final TagKey<DragonType> types;
    private final boolean allowHumans;

    public DragonPressurePlates(final Properties properties, final TagKey<DragonType> types, boolean allowHumans) {
        super(BlockSetType.WARPED, properties);
        registerDefaultState(stateDefinition.any().setValue(POWERED, false).setValue(WATERLOGGED, false));

        this.types = types;
        this.allowHumans = allowHumans;
    }

    public @Nullable TagKey<DragonType> getTypes() {
        return types;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull final BlockState state, @NotNull final BlockGetter level, @NotNull final BlockPos position, @NotNull final CollisionContext context) {
        return PRESSED_AABB;
    }

    @Override
    public @NotNull BlockState updateShape(final BlockState state, @NotNull final Direction facing, @NotNull final BlockState facingState, @NotNull final LevelAccessor level, @NotNull final BlockPos position, @NotNull final BlockPos facingPosition) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(position, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, facing, facingState, level, position, facingPosition);
    }

    @Override
    protected int getSignalStrength(final Level level, @NotNull final BlockPos position) {
        List<? extends Entity> entities = level.getEntities(null, TOUCH_AABB.move(position));

        if (entities.isEmpty()) {
            return 0;
        }

        for (Entity entity : entities) {
            if (!(entity instanceof Player player) || entity.isIgnoringBlockTriggers()) {
                continue;
            }

            DragonStateHandler data = DragonStateProvider.getData(player);

            if (!data.isDragon()) {
                return allowHumans ? 15 : 0;
            }

            return types != null && data.getType().is(types) ? 15 : 0;
        }

        return 0;
    }

    @Override
    protected void createBlockStateDefinition(@NotNull final Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(WATERLOGGED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NotNull final BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);

        if (state == null) {
            return null;
        }

        if (state.hasProperty(FACING)) {
            state = state.setValue(FACING, context.getHorizontalDirection());
        }

        return state.setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public @NotNull BlockState rotate(final BlockState state, @NotNull final Rotation rotation) {
        if (state.hasProperty(FACING)) {
            return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
        }

        return state;
    }

    @Override
    public @NotNull BlockState mirror(final BlockState state, @NotNull final Mirror mirror) {
        if (state.hasProperty(FACING)) {
            return state.rotate(mirror.getRotation(state.getValue(FACING)));
        }

        return state;
    }
}