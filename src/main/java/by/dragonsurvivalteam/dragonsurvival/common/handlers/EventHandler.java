package by.dragonsurvivalteam.dragonsurvival.common.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.network.status.PlayerJumpSync;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonModifiers;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ElytraItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@SuppressWarnings( "unused" )
@Mod.EventBusSubscriber
public class EventHandler{


	@SubscribeEvent
	public static void playerTick(PlayerTickEvent event){
		if (event.phase == Phase.START || !(event.player instanceof ServerPlayer serverPlayer) || serverPlayer.isDeadOrDying()) {
			return;
		}

		DragonStateProvider.getCap(serverPlayer).ifPresent(handler -> {
			if (handler.altarCooldown > 0) {
				handler.altarCooldown--;
			}

			if (!ServerConfig.startWithDragonChoice || handler.hasUsedAltar || serverPlayer.tickCount < Functions.secondsToTicks(5)) {
				return;
			}

			if (!handler.isDragon()) {
				NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new OpenDragonAltar());
				handler.hasUsedAltar = true;
			}
		});
	}

	static int cycle = 0;

	/**
	 * Check every 2 seconds
	 */
	//TODO add Elytra from other mods
	@SubscribeEvent
	public static void removeElytraFromDragon(TickEvent.PlayerTickEvent playerTickEvent){
		if(!ServerConfig.dragonsAllowedToUseElytra && playerTickEvent.phase == TickEvent.Phase.START){
			Player player = playerTickEvent.player;
			DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
				if(dragonStateHandler.isDragon() && player instanceof ServerPlayer && cycle >= 40){
					//chestplate slot is #38
					ItemStack stack = player.getInventory().getItem(38);
					Item item = stack.getItem();
					if(item instanceof ElytraItem){
						player.drop(player.getInventory().removeItemNoUpdate(38), true, false);
					}
					cycle = 0;
				}else{
					cycle++;
				}
			});
		}
	}

	/**
	 * Adds dragon avoidance goal
	 */
	@SubscribeEvent
	public static void onJoin(EntityJoinLevelEvent joinWorldEvent){
		Entity entity = joinWorldEvent.getEntity();
		if(entity instanceof Animal && !(entity instanceof Wolf || entity instanceof Hoglin)){
			((Animal)entity).goalSelector.addGoal(5, new AvoidEntityGoal((Animal)entity, Player.class, living -> DragonUtils.isDragon((Player)living) && !((Player)living).hasEffect(DragonEffects.ANIMAL_PEACE), 20.0F, 1.3F, 1.5F, s -> true));
		}
	}

	@SubscribeEvent( priority = EventPriority.HIGHEST )
	public static void expDrops(BlockEvent.BreakEvent breakEvent){
		if(DragonUtils.isDragon(breakEvent.getPlayer())){
			if(breakEvent.getExpToDrop() > 0){
				int bonusLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE, breakEvent.getPlayer());
				int silklevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, breakEvent.getPlayer());
				breakEvent.setExpToDrop(breakEvent.getState().getExpDrop(breakEvent.getLevel(), RandomSource.create(), breakEvent.getPos(), bonusLevel, silklevel));
			}
		}
	}

	@SubscribeEvent
	public static void createAltar(PlayerInteractEvent.RightClickBlock rightClickBlock){
		if(!ServerConfig.altarCraftable){
			return;
		}

		ItemStack itemStack = rightClickBlock.getItemStack();
		if(itemStack.getItem() == DSItems.elderDragonBone){
			if(!rightClickBlock.getEntity().isSpectator()){

				final Level world = rightClickBlock.getLevel();
				final BlockPos blockPos = rightClickBlock.getPos();
				BlockState blockState = world.getBlockState(blockPos);
				final Block block = blockState.getBlock();

				boolean replace = false;
				rightClickBlock.getEntity().isSpectator();
				rightClickBlock.getEntity().isCreative();
				BlockPlaceContext deirection = new BlockPlaceContext(rightClickBlock.getLevel(), rightClickBlock.getEntity(), rightClickBlock.getHand(), rightClickBlock.getItemStack(), new BlockHitResult(new Vec3(0, 0, 0), rightClickBlock.getEntity().getDirection(), blockPos, false));
				if(block == Blocks.STONE){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_stone.getStateForPlacement(deirection));
					replace = true;
				}else if(block == Blocks.MOSSY_COBBLESTONE){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_mossy_cobblestone.getStateForPlacement(deirection));
					replace = true;
				}else if(block == Blocks.SANDSTONE){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_sandstone.getStateForPlacement(deirection));
					replace = true;
				}else if(block == Blocks.RED_SANDSTONE){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_red_sandstone.getStateForPlacement(deirection));
					replace = true;
				}else if(ResourceHelper.getKey(block).getPath().contains(ResourceHelper.getKey(Blocks.OAK_LOG).getPath())){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_oak_log.getStateForPlacement(deirection));
					replace = true;
				}else if(ResourceHelper.getKey(block).getPath().contains(ResourceHelper.getKey(Blocks.BIRCH_LOG).getPath())){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_birch_log.getStateForPlacement(deirection));
					replace = true;
				}else if(block == Blocks.PURPUR_BLOCK){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_purpur_block.getStateForPlacement(deirection));
					replace = true;
				}else if(block == Blocks.NETHER_BRICKS){
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_nether_bricks.getStateForPlacement(deirection));
					replace = true;
				}else if(block == Blocks.BLACKSTONE){
					rightClickBlock.getEntity().getDirection();
					world.setBlockAndUpdate(blockPos, DSBlocks.dragon_altar_blackstone.getStateForPlacement(deirection));
					replace = true;
				}

				if(replace){
					if(!rightClickBlock.getEntity().isCreative()){
						itemStack.shrink(1);
					}
					rightClickBlock.setCanceled(true);
					world.playSound(rightClickBlock.getEntity(), blockPos, SoundEvents.WITHER_SPAWN, SoundSource.PLAYERS, 1, 1);
					rightClickBlock.setCancellationResult(InteractionResult.SUCCESS);
				}
			}
		}
	}

	@SubscribeEvent
	public static void returnBeacon(PlayerEvent.ItemCraftedEvent craftedEvent){
		Container inventory = craftedEvent.getInventory();
		ItemStack result = craftedEvent.getCrafting();
		int rem = ContainerHelper.clearOrCountMatchingItems(inventory, item -> item.getItem() == DSItems.passiveFireBeacon
			|| item.getItem() == DSItems.passiveMagicBeacon
			|| item.getItem() == DSItems.passivePeaceBeacon, 1, true);
		if(rem == 0 && result.getItem() == DSBlocks.dragonBeacon.asItem()){
			craftedEvent.getEntity().addItem(new ItemStack(Items.BEACON));
		}
	}

	@SubscribeEvent
	public static void returnNetherStarHeart(PlayerEvent.ItemCraftedEvent craftedEvent){
		Container inventory = craftedEvent.getInventory();
		ItemStack result = craftedEvent.getCrafting();
		int rem = ContainerHelper.clearOrCountMatchingItems(inventory, item -> item.getItem() == DSItems.starHeart, 1, true);
		if(rem == 0 && result.getItem() == DSItems.starHeart.asItem()){
			craftedEvent.getEntity().addItem(new ItemStack(Items.NETHER_STAR));
		}
	}

	@SubscribeEvent
	public static void returnNetherStarBone(PlayerEvent.ItemCraftedEvent craftedEvent){
		Container inventory = craftedEvent.getInventory();
		ItemStack result = craftedEvent.getCrafting();
		int rem = ContainerHelper.clearOrCountMatchingItems(inventory, item -> item.getItem() == DSItems.starBone, 1, true);
		if(rem == 0 && result.getItem() == DSItems.starBone.asItem()){
			craftedEvent.getEntity().addItem(new ItemStack(Items.NETHER_STAR));
		}
	}

	@SubscribeEvent
	public static void onJump(LivingJumpEvent jumpEvent){
		final LivingEntity living = jumpEvent.getEntity();


		if(living.getEffect(DragonEffects.TRAPPED) != null){
			Vec3 deltaMovement = living.getDeltaMovement();
			living.setDeltaMovement(deltaMovement.x, deltaMovement.y < 0 ? deltaMovement.y : 0, deltaMovement.z);
			living.setJumping(false);
			return;
		}

		DragonStateProvider.getCap(living).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon()){
				Double jumpBonus = 0.0;
				if (dragonStateHandler.getBody() != null) {
					jumpBonus = DragonModifiers.getJumpBonus(dragonStateHandler);
				}

				living.push(0, jumpBonus, 0);

				if(living instanceof ServerPlayer){
					if(living.getServer().isSingleplayer()){
						NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerJumpSync(living.getId(), 20)); // 42
					}else{
						NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerJumpSync(living.getId(), 10)); // 21
					}
				}
			}
		});
	}
}