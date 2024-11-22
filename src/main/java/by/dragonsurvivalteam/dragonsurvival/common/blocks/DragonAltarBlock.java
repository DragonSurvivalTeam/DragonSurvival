package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DragonAltarBlock extends Block {
    @Translation(type = Translation.Type.MISC, comments = "The altar is on cooldown for: %s")
    private static final String ALTAR_COOLDOWN = Translation.Type.GUI.wrap("message.altar_cooldown");

    @Translation(type = Translation.Type.MISC, comments = "■§7 An altar that allows you to turn into a dragon.")
    private static final String ALTAR = Translation.Type.DESCRIPTION.wrap("dragon_altar");

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private final VoxelShape SHAPE = Shapes.block();

    public DragonAltarBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTootipComponents, @NotNull TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTootipComponents, pTooltipFlag);
        pTootipComponents.add(Component.translatable(ALTAR));
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull Player player, @NotNull BlockHitResult pHitResult) {
        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!pLevel.isClientSide()) {
            if (ServerConfig.altarUsageCooldown > 0 && handler.altarCooldown > 0) {
                Functions.Time time = Functions.Time.fromTicks(handler.altarCooldown);
                player.sendSystemMessage(Component.translatable(ALTAR_COOLDOWN, time.format()));
                return InteractionResult.FAIL;
            } else {
                PacketDistributor.sendToPlayer((ServerPlayer) player, OpenDragonAltar.INSTANCE);
                handler.altarCooldown = Functions.secondsToTicks(ServerConfig.altarUsageCooldown);
                handler.hasUsedAltar = true;
                handler.isInAltar = true;
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }
}