package by.dragonsurvivalteam.dragonsurvival.client.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.client.handlers.Keybind;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncAbilityCasting;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber( Dist.CLIENT )
public class ClientCastingHandler{
	public static final byte StatusIdle = 0;
	public static final byte StatusInProgress = 1;
	public static final byte StatusStop = 2;
	public static byte status = StatusIdle;
	public static boolean hasCast = false;
	public static long castStartTime = -1;
	private static int castSlot = -1;

	@SubscribeEvent
	public static void abilityKeyBindingChecks(TickEvent.ClientTickEvent clientTickEvent){
		Minecraft instance = Minecraft.getInstance();
		if(instance.player == null || instance.level == null || clientTickEvent.phase != TickEvent.Phase.END)
			return;

		Player player = instance.player;
		if(player.isSpectator() || !DragonUtils.isDragon(player))
			return;

		DragonStateHandler dragonStateHandler = DragonUtils.getHandler(player);

		boolean isKeyDown = Keybind.USE_ABILITY.isDown() || ClientConfig.alternateCastMode && (
								Keybind.ABILITY1.isDown() && dragonStateHandler.getMagicData().getSelectedAbilitySlot() == 0
								|| Keybind.ABILITY2.isDown() && dragonStateHandler.getMagicData().getSelectedAbilitySlot() == 1
								|| Keybind.ABILITY3.isDown() && dragonStateHandler.getMagicData().getSelectedAbilitySlot() == 2
								|| Keybind.ABILITY4.isDown() && dragonStateHandler.getMagicData().getSelectedAbilitySlot() == 3);

		int slot = dragonStateHandler.getMagicData().getSelectedAbilitySlot();
		ActiveDragonAbility ability = dragonStateHandler.getMagicData().getAbilityFromSlot(castSlot);
		
		if (ability == null) {
			castSlot = slot;
			ability = dragonStateHandler.getMagicData().getAbilityFromSlot(castSlot);
		}

		if (castSlot != slot) { // ability slot has been changed, cancel any casts and put skills on cooldown if needed
			status = StatusIdle;
			//System.out.println(player + " ability changed from " + ability.getName() + " to " + dragonStateHandler.getMagicData().getAbilityFromSlot(slot).getName() + ".");
			if (castStartTime != -1 && ability.canCastSkill(player)) {
				NetworkHandler.CHANNEL.sendToServer(new SyncAbilityCasting(player.getId(), true, castSlot, ability.saveNBT(), castStartTime, player.level().getGameTime()));
				//System.out.println(ability.getName() + " finished casting due to swap.");
			}
			hasCast = false;
			ability.onKeyReleased(player);
			castStartTime = -1;
			castSlot = slot;
			return;
		}

		if(status == StatusIdle && isKeyDown) {
			castStartTime = -1;
			status = StatusInProgress;
		}

		if(status == StatusInProgress && !isKeyDown){
			castStartTime = -1;
			status = StatusStop;
		}

		if(!isKeyDown){
			castStartTime = -1;
			hasCast = false;
		}

		if(status == StatusInProgress && ability.canCastSkill(player) ){
			if (castStartTime == -1)
				castStartTime = player.level().getGameTime();
			NetworkHandler.CHANNEL.sendToServer(new SyncAbilityCasting(player.getId(), true, castSlot, ability.saveNBT(), castStartTime, player.level().getGameTime()));
		} else if(status == StatusStop || status == StatusInProgress && !ability.canCastSkill(player) && castStartTime != -1){
			NetworkHandler.CHANNEL.sendToServer(new SyncAbilityCasting(player.getId(), false, castSlot, ability.saveNBT(), castStartTime, player.level().getGameTime()));
			ability.onKeyReleased(player);
			status = StatusIdle;
			castStartTime = -1;
		}
		castSlot = slot;
	}
}