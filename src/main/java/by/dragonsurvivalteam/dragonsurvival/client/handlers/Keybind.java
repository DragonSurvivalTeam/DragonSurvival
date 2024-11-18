package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ClientConfig;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonInventory;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncDragonAbilitySlot;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber( Dist.CLIENT )
public class Keybind {

	public static KeyMapping TOGGLE_WINGS;
	public static KeyMapping DRAGON_INVENTORY;

	public static KeyMapping NEXT_ABILITY;
	public static KeyMapping PREV_ABILITY;

	public static KeyMapping USE_ABILITY;
	public static KeyMapping TOGGLE_ABILITIES;

	public static KeyMapping ABILITY1;
	public static KeyMapping ABILITY2;
	public static KeyMapping ABILITY3;
	public static KeyMapping ABILITY4;

	public static KeyMapping SPIN_ABILITY;
	public static KeyMapping FREE_LOOK;

	public static void registerKeys(final RegisterKeyMappingsEvent evt) {
		
		TOGGLE_WINGS = new KeyMapping("ds.keybind.wings", GLFW.GLFW_KEY_G, "ds.keybind.category");
		TOGGLE_WINGS.setKeyConflictContext(KeyConflictContext.IN_GAME);
		evt.register(TOGGLE_WINGS);

		DRAGON_INVENTORY = new KeyMapping("ds.keybind.dragon_inv", GLFW.GLFW_KEY_UNKNOWN, "ds.keybind.category");
		DRAGON_INVENTORY.setKeyConflictContext(KeyConflictContext.IN_GAME);
		evt.register(DRAGON_INVENTORY);

		USE_ABILITY = new KeyMapping("ds.keybind.use_ability", GLFW.GLFW_KEY_C, "ds.keybind.category");
		USE_ABILITY.setKeyConflictContext(KeyConflictContext.IN_GAME);
		evt.register(USE_ABILITY);

		TOGGLE_ABILITIES = new KeyMapping("ds.keybind.toggle_abilities", GLFW.GLFW_KEY_X, "ds.keybind.category");
		TOGGLE_ABILITIES.setKeyConflictContext(KeyConflictContext.IN_GAME);
		evt.register(TOGGLE_ABILITIES);

		NEXT_ABILITY = new KeyMapping("ds.keybind.next_ability", GLFW.GLFW_KEY_R, "ds.keybind.category");
		NEXT_ABILITY.setKeyConflictContext(KeyConflictContext.IN_GAME);
		evt.register(NEXT_ABILITY);

		PREV_ABILITY = new KeyMapping("ds.keybind.prev_ability", GLFW.GLFW_KEY_F, "ds.keybind.category");
		PREV_ABILITY.setKeyConflictContext(KeyConflictContext.IN_GAME);
		evt.register(PREV_ABILITY);

		ABILITY1 = new KeyMapping("ds.keybind.ability1", GLFW.GLFW_KEY_KP_1, "ds.keybind.category");
		ABILITY1.setKeyConflictContext(KeyConflictContext.IN_GAME);
		evt.register(ABILITY1);

		ABILITY2 = new KeyMapping("ds.keybind.ability2", GLFW.GLFW_KEY_KP_2, "ds.keybind.category");
		ABILITY2.setKeyConflictContext(KeyConflictContext.IN_GAME);
		evt.register(ABILITY2);

		ABILITY3 = new KeyMapping("ds.keybind.ability3", GLFW.GLFW_KEY_KP_3, "ds.keybind.category");
		ABILITY3.setKeyConflictContext(KeyConflictContext.IN_GAME);
		evt.register(ABILITY3);

		ABILITY4 = new KeyMapping("ds.keybind.ability4", GLFW.GLFW_KEY_KP_4, "ds.keybind.category");
		ABILITY4.setKeyConflictContext(KeyConflictContext.IN_GAME);
		evt.register(ABILITY4);

		SPIN_ABILITY = new KeyMapping("ds.keybind.spin", GLFW.GLFW_KEY_V, "ds.keybind.category");
		SPIN_ABILITY.setKeyConflictContext(KeyConflictContext.GUI);
		evt.register(SPIN_ABILITY);

		FREE_LOOK = new KeyMapping("ds.keybind.free_look", GLFW.GLFW_KEY_LEFT_ALT, "ds.keybind.category");
		FREE_LOOK.setKeyConflictContext(KeyConflictContext.IN_GAME);
		evt.register(FREE_LOOK);
	}

	@SubscribeEvent
	public static void onKey(InputEvent.Key keyInputEvent){
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = Minecraft.getInstance().player;

		if(player == null || !DragonUtils.isDragon(minecraft.player))
			return;

		DragonStateHandler dragonStateHandler = DragonUtils.getHandler(player);

		if(DRAGON_INVENTORY.consumeClick()){
			if(minecraft.screen == null){
				NetworkHandler.CHANNEL.sendToServer(new OpenDragonInventory());
			}else{
				player.closeContainer();
			}

		}else if(TOGGLE_ABILITIES.consumeClick()){
			dragonStateHandler.getMagicData().setRenderAbilities(!dragonStateHandler.getMagicData().shouldRenderAbilities());
			NetworkHandler.CHANNEL.sendToServer(new SyncDragonAbilitySlot(player.getId(), dragonStateHandler.getMagicData().getSelectedAbilitySlot(), dragonStateHandler.getMagicData().shouldRenderAbilities()));

		}else if(NEXT_ABILITY.consumeClick()){
			int nextSlot = dragonStateHandler.getMagicData().getSelectedAbilitySlot() == 3 ? 0 : dragonStateHandler.getMagicData().getSelectedAbilitySlot() + 1;
			dragonStateHandler.getMagicData().setSelectedAbilitySlot(nextSlot);
			NetworkHandler.CHANNEL.sendToServer(new SyncDragonAbilitySlot(player.getId(), dragonStateHandler.getMagicData().getSelectedAbilitySlot(), dragonStateHandler.getMagicData().shouldRenderAbilities()));

		}else if(PREV_ABILITY.consumeClick()){
			int nextSlot = dragonStateHandler.getMagicData().getSelectedAbilitySlot() == 0 ? 3 : dragonStateHandler.getMagicData().getSelectedAbilitySlot() - 1;
			dragonStateHandler.getMagicData().setSelectedAbilitySlot(nextSlot);
			NetworkHandler.CHANNEL.sendToServer(new SyncDragonAbilitySlot(player.getId(), dragonStateHandler.getMagicData().getSelectedAbilitySlot(), dragonStateHandler.getMagicData().shouldRenderAbilities()));

		}


		if(!ClientConfig.alternateCastMode){
			if(ABILITY1.consumeClick()){
				if(dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()) != null) dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()).onKeyReleased(Minecraft.getInstance().player);
				dragonStateHandler.getMagicData().setSelectedAbilitySlot(0);
				NetworkHandler.CHANNEL.sendToServer(new SyncDragonAbilitySlot(player.getId(), dragonStateHandler.getMagicData().getSelectedAbilitySlot(), dragonStateHandler.getMagicData().shouldRenderAbilities()));
			}else if(ABILITY2.consumeClick()){
				if(dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()) != null) dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()).onKeyReleased(Minecraft.getInstance().player);
				dragonStateHandler.getMagicData().setSelectedAbilitySlot(1);
				NetworkHandler.CHANNEL.sendToServer(new SyncDragonAbilitySlot(player.getId(), dragonStateHandler.getMagicData().getSelectedAbilitySlot(), dragonStateHandler.getMagicData().shouldRenderAbilities()));
			}else if(ABILITY3.consumeClick()){
				if(dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()) != null) dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()).onKeyReleased(Minecraft.getInstance().player);
				dragonStateHandler.getMagicData().setSelectedAbilitySlot(2);
				NetworkHandler.CHANNEL.sendToServer(new SyncDragonAbilitySlot(player.getId(), dragonStateHandler.getMagicData().getSelectedAbilitySlot(), dragonStateHandler.getMagicData().shouldRenderAbilities()));
			}else if(ABILITY4.consumeClick()){
				if(dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()) != null) dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()).onKeyReleased(Minecraft.getInstance().player);
				dragonStateHandler.getMagicData().setSelectedAbilitySlot(3);
				NetworkHandler.CHANNEL.sendToServer(new SyncDragonAbilitySlot(player.getId(), dragonStateHandler.getMagicData().getSelectedAbilitySlot(), dragonStateHandler.getMagicData().shouldRenderAbilities()));
			}
		}else{
			if(ABILITY1.isDown()){
				if(dragonStateHandler.getMagicData().getSelectedAbilitySlot() != 0){
					if(dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()) != null) dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()).onKeyReleased(Minecraft.getInstance().player);
					dragonStateHandler.getMagicData().setSelectedAbilitySlot(0);
					NetworkHandler.CHANNEL.sendToServer(new SyncDragonAbilitySlot(player.getId(), dragonStateHandler.getMagicData().getSelectedAbilitySlot(), dragonStateHandler.getMagicData().shouldRenderAbilities()));
				}
			}else if(ABILITY2.isDown()){
				if(dragonStateHandler.getMagicData().getSelectedAbilitySlot() != 1){
					if(dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()) != null) dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()).onKeyReleased(Minecraft.getInstance().player);
					dragonStateHandler.getMagicData().setSelectedAbilitySlot(1);
					NetworkHandler.CHANNEL.sendToServer(new SyncDragonAbilitySlot(player.getId(), dragonStateHandler.getMagicData().getSelectedAbilitySlot(), dragonStateHandler.getMagicData().shouldRenderAbilities()));
				}
			}else if(ABILITY3.isDown()){
				if(dragonStateHandler.getMagicData().getSelectedAbilitySlot() != 2){
					if(dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()) != null) dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()).onKeyReleased(Minecraft.getInstance().player);
					dragonStateHandler.getMagicData().setSelectedAbilitySlot(2);
					NetworkHandler.CHANNEL.sendToServer(new SyncDragonAbilitySlot(player.getId(), dragonStateHandler.getMagicData().getSelectedAbilitySlot(), dragonStateHandler.getMagicData().shouldRenderAbilities()));
				}
			}else if(ABILITY4.isDown()){
				if(dragonStateHandler.getMagicData().getSelectedAbilitySlot() != 3){
					if(dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()) != null) dragonStateHandler.getMagicData().getAbilityFromSlot(dragonStateHandler.getMagicData().getSelectedAbilitySlot()).onKeyReleased(Minecraft.getInstance().player);
					dragonStateHandler.getMagicData().setSelectedAbilitySlot(3);
					NetworkHandler.CHANNEL.sendToServer(new SyncDragonAbilitySlot(player.getId(), dragonStateHandler.getMagicData().getSelectedAbilitySlot(), dragonStateHandler.getMagicData().shouldRenderAbilities()));
				}
			}
		}
	}
}