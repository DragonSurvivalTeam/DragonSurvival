package by.dragonsurvivalteam.dragonsurvival.common.items;


import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
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

public class SpinGrantItem extends Item{
	public SpinGrantItem(Properties p_i48487_1_){
		super(p_i48487_1_);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand p_77659_3_){
		DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(player);

		if(handler.isDragon()){
			if(!world.isClientSide()){
				handler.getMovementData().spinLearned = !handler.getMovementData().spinLearned;
				NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SyncSpinStatus(player.getId(), handler.getMovementData().spinAttack, handler.getMovementData().spinCooldown, handler.getMovementData().spinLearned));

				if(!player.isCreative()){
					player.getItemInHand(p_77659_3_).shrink(1);
				}
			}

			player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, 1F, 0F);
			return InteractionResultHolder.success(player.getItemInHand(p_77659_3_));
		}

		return super.use(world, player, p_77659_3_);
	}

	@Override
	public void appendHoverText(ItemStack p_77624_1_,
		@Nullable
			Level p_77624_2_, List<Component> p_77624_3_, TooltipFlag p_77624_4_){
		super.appendHoverText(p_77624_1_, p_77624_2_, p_77624_3_, p_77624_4_);
		p_77624_3_.add(Component.translatable("ds.description.spin_grant"));
	}
}