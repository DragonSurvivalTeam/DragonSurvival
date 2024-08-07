package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import static by.dragonsurvivalteam.dragonsurvival.util.DragonLevel.*;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncSize;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@SuppressWarnings("unused")
@EventBusSubscriber( modid = DragonSurvivalMod.MODID )
public class DragonGrowthHandler{
	public static long newbornToYoung = TimeUnit.SECONDS.convert(3, TimeUnit.HOURS);
	public static long youngToAdult = TimeUnit.SECONDS.convert(15, TimeUnit.HOURS);
	public static long adultToAncient = TimeUnit.SECONDS.convert(24, TimeUnit.HOURS);
	public static long ancient = TimeUnit.SECONDS.convert(30, TimeUnit.DAYS);

	@SubscribeEvent
	public static void onItemUse(PlayerInteractEvent.RightClickItem event){
		ItemStack stack = event.getItemStack();
		Item item = stack.getItem();

		Player player = event.getEntity();
		Level world = player.getCommandSenderWorld();

		DragonStateProvider.getCap(player).ifPresent(handler -> {
			if(!handler.isDragon()){
				return;
			}

			double size = handler.getSize();

			if(size >= ServerConfig.maxGrowthSize){
				return;
			}

			boolean canContinue = false;

			HashSet<Item> newbornList = ConfigHandler.getResourceElements(Item.class, ServerConfig.growNewborn);
			HashSet<Item> youngList = ConfigHandler.getResourceElements(Item.class, ServerConfig.growYoung);
			HashSet<Item> adultList = ConfigHandler.getResourceElements(Item.class, ServerConfig.growAdult);

			HashSet<Item> allowedItems = new HashSet<>();

			switch(handler.getLevel()){
				case NEWBORN:
					if(newbornList.contains(item)){
						canContinue = true;
					}else if(youngList.contains(item) || adultList.contains(item)){
						allowedItems = newbornList;
					}

					break;
				case YOUNG:
					if(youngList.contains(item)){
						canContinue = true;
					}else if(newbornList.contains(item) || adultList.contains(item)){
						allowedItems = youngList;
					}

					break;
				case ADULT:
					if(adultList.contains(item)){
						canContinue = true;
					}else if(newbornList.contains(item) || youngList.contains(item)){
						allowedItems = adultList;
					}

					break;
			}

			if(!canContinue){
				if(!allowedItems.isEmpty() && world.isClientSide()){
					List<String> displayData = allowedItems.stream().map(i -> new ItemStack(i).getDisplayName().getString()).toList();
					StringBuilder result = new StringBuilder();

					for(int i = 0; i < displayData.size(); i++){
						String entry = displayData.get(i);

						result.append(entry).append(i + 1 < displayData.size() ? ", " : "");
					}

					player.displayClientMessage(Component.translatable("ds.invalid_grow_item", result), false);
				}

				return;
			}

			int increment = getIncrement(item, handler.getLevel());
			size += increment;
			handler.setSize(size, player);

			if(!player.isCreative()){
				event.getItemStack().shrink(1);
			}

			if(world.isClientSide()){
				return;
			}

			PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncSize.Data(player.getId(), size));
		});
	}

	public static int getIncrement(Item item, DragonLevel level){
		HashSet<Item> newbornList = ConfigHandler.getResourceElements(Item.class, ServerConfig.growNewborn);
		HashSet<Item> youngList = ConfigHandler.getResourceElements(Item.class, ServerConfig.growYoung);
		HashSet<Item> adultList = ConfigHandler.getResourceElements(Item.class, ServerConfig.growAdult);

		int increment = 0;

		if(item == DSItems.STAR_BONE){
			return -2;
		}

//TODO Add the ability to control this numbers with configs

		switch(level){
			case NEWBORN:
				if(adultList.contains(item)){
					increment = 3;
				}else if(youngList.contains(item)){
					increment = 2;
				}else if(newbornList.contains(item)){
					increment = 1;
				}
				break;
			case YOUNG:
				if(adultList.contains(item)){
					increment = 2;
				}else if(youngList.contains(item)){
					increment = 1;
				}
				break;

			case ADULT:
				if(adultList.contains(item)){
					increment = 1;
				}
				break;
		}
		return increment;
	}

	@SubscribeEvent
	public static void onPlayerUpdate(PlayerTickEvent.Pre event){
		if(!ServerConfig.alternateGrowing){
			return;
		}

		Player player = event.getEntity();
		Level world = player.getCommandSenderWorld();

		if(world.isClientSide()){
			return;
		}

		if(!DragonStateProvider.isDragon(player)){
			return;
		}

		if(player.tickCount % (60 * 20) != 0){
			return;
		}

		DragonStateProvider.getCap(player).ifPresent(handler -> {
			if(handler.growing){
                /*
                    1. Newborn - young = 3-4 h
                    2. Young - adult = 15-20h
                    3. Adult - maximum growth = 24h
                    4. After maximum growth. = 30 days for max growth
                 */

				double d;
				double timeIncrement = 60 * 20;

				if(handler.getSize() < YOUNG.size){
					d = (YOUNG.size - NEWBORN.size) / (newbornToYoung * 20.0) * timeIncrement * ServerConfig.newbornGrowthModifier;
				}else if(handler.getSize() < ADULT.size){
					d = (ADULT.size - YOUNG.size) / (youngToAdult * 20.0) * timeIncrement * ServerConfig.youngGrowthModifier;
				}else if(handler.getSize() < ADULT.maxSize){
					d = (40 - ADULT.size) / (adultToAncient * 20.0) * timeIncrement * ServerConfig.adultGrowthModifier;
				}else{
					d = (60 - 40) / (ancient * 20.0) * timeIncrement * ServerConfig.maxGrowthModifier;
				}

				double size = handler.getSize() + d;
				size = Math.min(size, ServerConfig.maxGrowthSize);

				if(handler.getSize() != size){
					handler.setSize(size, player);

					PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncSize.Data(player.getId(), size));
					player.refreshDimensions();
				}
			}
		});
	}
}