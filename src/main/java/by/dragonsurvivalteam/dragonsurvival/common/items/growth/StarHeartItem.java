package by.dragonsurvivalteam.dragonsurvival.common.items.growth;


import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncGrowthState;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.List;

public class StarHeartItem extends Item{
	public StarHeartItem(Properties p_i48487_1_){
		super(p_i48487_1_);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand p_77659_3_){
		if(!world.isClientSide()){
			DragonStateHandler handler = DragonUtils.getHandler(player);

			if(handler.isDragon()){
				handler.growing = !handler.growing;
				player.sendSystemMessage(Component.translatable(handler.growing ? "ds.growth.now_growing" : "ds.growth.no_growth"));

				NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer)player), new SyncGrowthState(handler.growing));
				return InteractionResultHolder.success(player.getItemInHand(p_77659_3_));
			}
		}

		return super.use(world, player, p_77659_3_);
	}

	@Override
	public void appendHoverText(ItemStack p_77624_1_,
		@Nullable
			Level p_77624_2_, List<Component> p_77624_3_, TooltipFlag p_77624_4_){
		super.appendHoverText(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
		p_77624_3_.add(Component.translatable("ds.description.starHeart"));
	}
}