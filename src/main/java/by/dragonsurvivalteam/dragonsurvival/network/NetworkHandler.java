package by.dragonsurvivalteam.dragonsurvival.network;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.network.claw.DragonClawsMenuToggle;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncBrokenTool;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.network.config.SyncBooleanConfig;
import by.dragonsurvivalteam.dragonsurvival.network.config.SyncEnumConfig;
import by.dragonsurvivalteam.dragonsurvival.network.config.SyncListConfig;
import by.dragonsurvivalteam.dragonsurvival.network.config.SyncNumberConfig;
import by.dragonsurvivalteam.dragonsurvival.network.container.*;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.SyncEmote;
import by.dragonsurvivalteam.dragonsurvival.network.flight.*;
import by.dragonsurvivalteam.dragonsurvival.network.magic.*;
import by.dragonsurvivalteam.dragonsurvival.network.player.*;
import by.dragonsurvivalteam.dragonsurvival.network.status.*;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.CompleteDataSync;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;


public class NetworkHandler{
	private static final String PROTOCOL_VERSION = "2";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(DragonSurvivalMod.MODID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	public static int nextPacketId = 0;

	public static void setup(){
		register(SyncTreasureRestStatus.class, new SyncTreasureRestStatus());
		register(SyncMagicSourceStatus.class, new SyncMagicSourceStatus());

		//Generic packets
		register(SynchronizeDragonCap.class, new SynchronizeDragonCap());
		register(SyncDragonMovement.class, new SyncDragonMovement());
		register(SyncDeltaMovement.class, new SyncDeltaMovement());
		register(SyncDragonTypeData.class, new SyncDragonTypeData());
		register(SyncChatEvent.class, new SyncChatEvent());
		register(PlayerJumpSync.class, new PlayerJumpSync());
		register(RefreshDragons.class, new RefreshDragons());
		register(SyncAltarCooldown.class, new SyncAltarCooldown());
		register(OpenDragonAltar.class, new OpenDragonAltar());

		register(RequestClientData.class, new RequestClientData());
		register(CompleteDataSync.class, new CompleteDataSync());

		//Status
		register(SyncGrowthState.class, new SyncGrowthState());
		register(SyncSize.class, new SyncSize());
		register(DiggingStatus.class, new DiggingStatus());

		register(SyncPlayerSkinPreset.class, new SyncPlayerSkinPreset());
		register(OpenDragonEditorPacket.class, new OpenDragonEditorPacket());

		//Flight
		register(SyncFlyingStatus.class, new SyncFlyingStatus());
		register(SyncFlightSpeed.class, new SyncFlightSpeed());
		register(SyncSpinStatus.class, new SyncSpinStatus());
		register(RequestSpinResync.class, new RequestSpinResync());

		//Inventory
		register(OpenInventory.class, new OpenInventory());
		register(SortInventoryPacket.class, new SortInventoryPacket());

		//Render settings
		register(SyncDragonClawRender.class, new SyncDragonClawRender());
		register(SyncDragonSkinSettings.class, new SyncDragonSkinSettings());
		register(DragonClawsMenuToggle.class, new DragonClawsMenuToggle());
		register(SyncDragonClawsMenu.class, new SyncDragonClawsMenu());

		//Ability packets
		register(OpenDragonInventory.class, new OpenDragonInventory());
		register(SyncMagicStats.class, new SyncMagicStats());
		register(SyncMagicCap.class, new SyncMagicCap());
		register(SyncDragonAbilitySlot.class, new SyncDragonAbilitySlot());
		register(SyncAbilityCasting.class, new SyncAbilityCasting());
		register(SyncSkillLevelChangeCost.class, new SyncSkillLevelChangeCost());

		//Potion sync
		register(SyncPotionRemovedEffect.class, new SyncPotionRemovedEffect());
		register(SyncPotionAddedEffect.class, new SyncPotionAddedEffect());

		//Emote packets
		register(SyncEmote.class, new SyncEmote());

		//Config
		register(SyncBooleanConfig.class, new SyncBooleanConfig());
		register(SyncNumberConfig.class, new SyncNumberConfig());
		register(SyncEnumConfig.class, new SyncEnumConfig());
		register(SyncListConfig.class, new SyncListConfig());

		CHANNEL.registerMessage(nextPacketId++, SyncBrokenTool.class, SyncBrokenTool::encode, SyncBrokenTool::decode, SyncBrokenTool::handle);
	}

	public static <T> void register(Class<T> clazz, IMessage<T> message){
		CHANNEL.registerMessage(nextPacketId++, clazz, message::encode, message::decode, message::handle);
	}
}