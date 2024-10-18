package by.dragonsurvivalteam.dragonsurvival.common.items.growth;


import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncGrowthState;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAdvancementTriggers;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class StarHeartItem extends Item{
	public StarHeartItem(Properties p_i48487_1_){
		super(p_i48487_1_);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand p_77659_3_){
		if(!world.isClientSide() && player instanceof ServerPlayer serverPlayer){
			DSAdvancementTriggers.USE_STAR_HEART.get().trigger(serverPlayer);
			DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(player);

			if(handler.isDragon()){
				handler.growing = !handler.growing;
				player.sendSystemMessage(Component.translatable(handler.growing ? "ds.growth.now_growing" : "ds.growth.no_growth"));
				PacketDistributor.sendToPlayer(serverPlayer, new SyncGrowthState.Data(handler.growing));
				return InteractionResultHolder.success(player.getItemInHand(p_77659_3_));
			}
		}

		return super.use(world, player, p_77659_3_);
	}

	@Override
	public void appendHoverText(@NotNull ItemStack pStack, Item.@NotNull TooltipContext pContext, @NotNull List<Component> pTooltipComponents, @NotNull TooltipFlag pTooltipFlag){
		super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
		pTooltipComponents.add(Component.translatable("ds.description.starHeart"));
	}
}