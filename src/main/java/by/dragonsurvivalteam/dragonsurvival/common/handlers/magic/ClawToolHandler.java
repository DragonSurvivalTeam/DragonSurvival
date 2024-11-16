package by.dragonsurvivalteam.dragonsurvival.common.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.ClawInventory;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncBrokenTool;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.ToolUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent.PickupXp;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;

@EventBusSubscriber
public class ClawToolHandler{
	@SubscribeEvent
	public static void experiencePickup(PickupXp event){
		Player player = event.getEntity();

		DragonStateProvider.getCap(player).ifPresent(cap -> {
			ArrayList<ItemStack> stacks = new ArrayList<>();

			for(int i = 0; i < ClawInventory.Slot.size(); i++){
				ItemStack clawStack = cap.getClawToolData().getClawsInventory().getItem(i);
				int mending = clawStack.getEnchantmentLevel(Enchantments.MENDING);

				if(mending > 0 && clawStack.isDamaged()){
					stacks.add(clawStack);
				}
			}

			if (!stacks.isEmpty()) {
				ItemStack repairTime = stacks.get(player.getRandom().nextInt(stacks.size()));
				if(!repairTime.isEmpty() && repairTime.isDamaged()){

					int i = Math.min((int)(event.getOrb().value * repairTime.getXpRepairRatio()), repairTime.getDamageValue());
					event.getOrb().value -= i * 2;
					repairTime.setDamageValue(repairTime.getDamageValue() - i);
				}
			}

			event.getOrb().value = Math.max(0, event.getOrb().value);
			player.detectEquipmentUpdates();
		});
	}

	// This needs to happen as early as possible to make sure drops are added before other mods interact with them
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void playerDieEvent(LivingDropsEvent event){
		Entity ent = event.getEntity();

		if(ent instanceof Player player){
			if(!player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY) && !ServerConfig.keepClawItems){
				DragonStateHandler handler = DragonUtils.getHandler(player);

				for(int i = 0; i < ClawInventory.Slot.size(); i++){
					ItemStack stack = handler.getClawToolData().getClawsInventory().getItem(i);

					if(!stack.isEmpty()){
						if (!EnchantmentHelper.hasVanishingCurse(stack)) {
							event.getDrops().add(new ItemEntity(player.level, player.getX(), player.getY(), player.getZ(), stack));
						}

						handler.getClawToolData().getClawsInventory().setItem(i, ItemStack.EMPTY);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void dropBlocksMinedByPaw(PlayerEvent.HarvestCheck harvestCheck){
		if(!ServerConfig.bonuses || !ServerConfig.clawsAreTools){
			return;
		}
		Player playerEntity = harvestCheck.getEntity();
		DragonStateProvider.getCap(playerEntity).ifPresent(dragonStateHandler -> {
			if(dragonStateHandler.isDragon()){
				ItemStack stack = playerEntity.getMainHandItem();
				BlockState blockState = harvestCheck.getTargetBlock();
				if(ToolUtils.shouldUseDragonTools(stack) && !harvestCheck.canHarvest()){
					harvestCheck.setCanHarvest(dragonStateHandler.canHarvestWithPaw(blockState));
				}
			}
		});
	}

	public static ItemStack getDragonHarvestTool(final Player player, final BlockState state){
		ItemStack mainStack = player.getInventory().getSelected();
		float newSpeed = 0F;

		if (!ToolUtils.shouldUseDragonTools(mainStack)) {
			return mainStack;
		}

		ItemStack harvestTool = mainStack;
		DragonStateHandler handler = DragonUtils.getHandler(player);

		for (int i = 1; i < ClawInventory.Slot.size(); i++) {
			ItemStack breakingItem = handler.getClawToolData().getClawsInventory().getItem(i);

			if (!breakingItem.isEmpty() && breakingItem.isCorrectToolForDrops(state)) {
				float tempSpeed = breakingItem.getDestroySpeed(state);

				if (breakingItem.getItem() instanceof DiggerItem item) {
					tempSpeed = item.getDestroySpeed(breakingItem, state);
				}

				if (tempSpeed > newSpeed) {
					newSpeed = tempSpeed;
					harvestTool = breakingItem;
				}
			}
		}

		return harvestTool;
	}

	public static Pair<ItemStack, Integer> getDragonHarvestToolAndSlot(final Player player, final BlockState state) {
		ItemStack mainStack = player.getInventory().getSelected();
		float newSpeed = 0F;

		if (!ToolUtils.shouldUseDragonTools(mainStack)) {
			return Pair.of(mainStack, -1);
		}

		ItemStack harvestTool = mainStack;
		DragonStateHandler handler = DragonUtils.getHandler(player);
		int toolSlot = -1;

		for (int i = 0; i < ClawInventory.Slot.size(); i++) {
			ItemStack breakingItem = handler.getClawToolData().getClawsInventory().getItem(i);

			if (!breakingItem.isEmpty() && breakingItem.isCorrectToolForDrops(state)) {
				float tempSpeed = breakingItem.getDestroySpeed(state);

				if (breakingItem.getItem() instanceof DiggerItem item) {
					tempSpeed = item.getDestroySpeed(breakingItem, state);
				}

				if (tempSpeed > newSpeed) {
					newSpeed = tempSpeed;
					harvestTool = breakingItem;
					toolSlot = i;
				}
			}
		}

		return Pair.of(harvestTool, toolSlot);
	}

	public static ItemStack getDragonHarvestTool(final Player player) {
		ItemStack mainStack = player.getInventory().getSelected();

		if (!ToolUtils.shouldUseDragonTools(mainStack)) {
			return mainStack;
		}

		Level world = player.level;
		BlockHitResult result = Item.getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);

		if (result.getType() != HitResult.Type.MISS) {
			BlockState state = world.getBlockState(result.getBlockPos());

			return getDragonHarvestTool(player, state);
		}

		return mainStack;
	}

	/**
	 *
	 * @return Only the sword in the dragon tool slot <br>
	 * Returns {@link ItemStack#EMPTY} if the player is holding any sort of tool
	 */
	public static ItemStack getDragonSword(final LivingEntity player) {
		if (!(player instanceof Player)) {
			return ItemStack.EMPTY;
		}

		ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);

		if (!ToolUtils.shouldUseDragonTools(itemInHand)) {
			return ItemStack.EMPTY;
		}

		DragonStateHandler cap = DragonUtils.getHandler(player);

		return cap.getClawToolData().getClawsInventory().getItem(0);
	}

	/** Handle tool breaking for the dragon */
	@SubscribeEvent
	public static void onToolBreak(final PlayerDestroyItemEvent event) {
		if (event.getHand() == null) return;
		Player player = event.getEntity();

		if (DragonUtils.isDragon(player)) {
			ItemStack clawTool = getDragonHarvestTool(player);

			if (ItemStack.matches(clawTool, event.getOriginal())) {
				player.broadcastBreakEvent(event.getHand());
			} else {
				if (!player.level.isClientSide) {
					DragonStateHandler handler = DragonUtils.getHandler(player);

					if (handler.switchedTool || handler.switchedWeapon) {
						player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                        NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SyncBrokenTool(player.getId(), handler.switchedTool ? handler.switchedToolSlot : ClawInventory.Slot.SWORD.ordinal()));
						return;
					}

					SimpleContainer clawsInventory = handler.getClawToolData().getClawsInventory();

					// When a tool breaks its data (the item with its tags etc.) stay there, only the stack gets set to air (and reduced by 1)
					for (int i = 0; i < ClawInventory.Slot.size(); i++) {
						ItemStack dragonTool = clawsInventory.getItem(i);

						if (event.getOriginal().getItem() == dragonTool.getItem()) {
							clawsInventory.setItem(i, ItemStack.EMPTY);
							break;
						}
					}

					NetworkHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SyncDragonClawsMenu(player.getId(), handler.getClawToolData().isMenuOpen(), clawsInventory));
				}
			}
		}
	}

	@Mod.EventBusSubscriber(modid = DragonSurvivalMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class Event_busHandler {
		@SubscribeEvent
		public void modifyBreakSpeed(final PlayerEvent.BreakSpeed event) {
			if (!ServerConfig.bonuses || !ServerConfig.clawsAreTools) {
				return;
			}

			Player player = event.getEntity();
			ItemStack mainStack = player.getMainHandItem();
			DragonStateHandler handler = DragonUtils.getHandler(player);

			if (!handler.switchedTool && !ToolUtils.shouldUseDragonTools(mainStack)) {
				// Bonus does not apply to held tools

				return;
			}

			if (!handler.isDragon()) {
				return;
			}

			BlockState state = event.getState();
			float originalSpeed = event.getOriginalSpeed();

			float bonus = 0;
			float unlockedBonus = 0;

			if (handler.getLevel() == DragonLevel.NEWBORN && ServerConfig.bonusUnlockedAt == DragonLevel.NEWBORN) {
				unlockedBonus = ServerConfig.bonusBreakSpeed;
			} else if (handler.getLevel() == DragonLevel.YOUNG && ServerConfig.bonusUnlockedAt != DragonLevel.ADULT) {
				unlockedBonus = ServerConfig.bonusBreakSpeed;
			} else if (handler.getLevel() == DragonLevel.ADULT) {
				unlockedBonus = ServerConfig.bonusBreakSpeedAdult;
				bonus = ServerConfig.baseBreakSpeedAdult;
			}

			for (int i = 0; i < ClawInventory.Slot.size(); i++) {
				ItemStack clawTool = handler.getClawToolData().getClawsInventory().getItem(i);

				if (state.requiresCorrectToolForDrops() && clawTool.isCorrectToolForDrops(state) || clawTool.getDestroySpeed(state) > 1) {
					bonus /= ServerConfig.bonusBreakSpeedReduction;
					break;
				}
			}

			for (TagKey<Block> tagKey : handler.getType().mineableBlocks()) {
				if (state.is(tagKey)) {
					bonus = unlockedBonus;

					break;
				}
			}

			// Don't discard the changes other mods already did to the harvest speed
			event.setNewSpeed(event.getNewSpeed() * Math.max(1, bonus));
		}
	}
}