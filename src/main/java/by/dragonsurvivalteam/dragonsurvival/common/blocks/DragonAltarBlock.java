package by.dragonsurvivalteam.dragonsurvival.common.blocks;


import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;


public class DragonAltarBlock extends Block{
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private final VoxelShape SHAPE = Shapes.block();


	public DragonAltarBlock(Properties properties){
		super(properties);
		registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context){
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder){
		builder.add(FACING);
	}

	@Override
	public void appendHoverText(ItemStack p_190948_1_,
		@Nullable
			BlockGetter p_190948_2_, List<Component> p_190948_3_, TooltipFlag p_190948_4_){
		super.appendHoverText(p_190948_1_, p_190948_2_, p_190948_3_, p_190948_4_);
		p_190948_3_.add(Component.translatable("ds.description.dragonAltar"));
	}

	@Override
	public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos position, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult result) {
		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}

		DragonStateHandler handler = DragonUtils.getHandler(player);

		if (ServerConfig.altarUsageCooldown > 0 && handler.altarCooldown > 0) {
			//Show the current cooldown in minutes and seconds in cases where the cooldown is set high in the config
			int minutes = (int) (Functions.ticksToMinutes(handler.altarCooldown));
			int seconds = (int) (Functions.ticksToSeconds(handler.altarCooldown - Functions.minutesToTicks(minutes)));
			player.sendSystemMessage(Component.translatable("ds.cooldown.active", (minutes > 0 ? minutes + "m " : "") + seconds + "s"));
			return InteractionResult.FAIL;
		} else {
			NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new OpenDragonAltar());
			handler.altarCooldown = Functions.secondsToTicks(ServerConfig.altarUsageCooldown);
			handler.hasUsedAltar = true;
			return InteractionResult.CONSUME;
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context){
		return SHAPE;
	}
}