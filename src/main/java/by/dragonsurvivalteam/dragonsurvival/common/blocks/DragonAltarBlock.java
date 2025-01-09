package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AltarData;
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
    @Translation(comments = "The altar is on cooldown for: %s")
    private static final String ALTAR_COOLDOWN = Translation.Type.GUI.wrap("message.altar_cooldown");

    @Translation(comments = "■§7 An altar that allows you to turn into a dragon and edit skin.")
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
    public @NotNull InteractionResult useWithoutItem(@NotNull final BlockState state, @NotNull final Level level, @NotNull final BlockPos position, @NotNull final Player player, @NotNull final BlockHitResult hitResult) {
        AltarData data = AltarData.getData(player);

        if (ServerConfig.altarUsageCooldown > 0 && data.altarCooldown > 0) {
            Functions.Time time = Functions.Time.fromTicks(data.altarCooldown);
            player.sendSystemMessage(Component.translatable(ALTAR_COOLDOWN, time.format()));
            return InteractionResult.FAIL;
        } else {
            data.altarCooldown = Functions.secondsToTicks(ServerConfig.altarUsageCooldown);
            data.hasUsedAltar = true;
            data.isInAltar = true;

            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, OpenDragonAltar.INSTANCE);
            }

            return InteractionResult.sidedSuccess(level.isClientSide());
        }
    }

    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        return SHAPE;
    }
}