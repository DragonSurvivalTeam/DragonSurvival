
package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.DragonScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.CaveDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.ForestDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.SeaDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonSizeHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.CaveDragon.passive.ContrastShowerAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.ForestDragon.passive.LightInDarknessAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.abilities.SeaDragon.passive.WaterAbility;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.RequestClientData;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonInventory;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonInventory.SendOpenDragonInventoryAndMaintainCursorPosition;

@OnlyIn( Dist.CLIENT )
@Mod.EventBusSubscriber( Dist.CLIENT )
public class ClientEvents{

	public static final ResourceLocation DRAGON_HUD = new ResourceLocation(DragonSurvivalMod.MODID + ":textures/gui/dragon_hud.png");
	/**
	 * Durations of jumps
	 */
	public static ConcurrentHashMap<Integer, Integer> dragonsJumpingTicks = new ConcurrentHashMap<>(20);
	public static double mouseX = -1;
	public static double mouseY = -1;
	@ConfigOption( side = ConfigSide.CLIENT, category = "inventory", key = "dragonInventory", comment = "Should the default inventory be replaced as a dragon?" )
	public static Boolean dragonInventory = true;
	@ConfigOption( side = ConfigSide.CLIENT, category = "inventory", key = "dragonTabs", comment = "Should dragon tabs be added to the default player inventory?" )
	public static Boolean dragonTabs = true;
	@ConfigOption( side = ConfigSide.CLIENT, category = "inventory", key = "inventoryToggle", comment = "Should the buttons for toggeling between dragon and normaly inventory be added?" )
	public static Boolean inventoryToggle = true;
	private static ItemStack BOLAS;

	@SubscribeEvent
	public static void decreaseJumpDuration(TickEvent.PlayerTickEvent playerTickEvent){
		if(playerTickEvent.phase == TickEvent.Phase.END){
			Player player = playerTickEvent.player;
			dragonsJumpingTicks.computeIfPresent(player.getId(), (playerEntity1, integer) -> integer > 0 ? integer - 1 : integer);
		}
	}

	@OnlyIn( Dist.CLIENT )
	public static void sendClientData(RequestClientData message){
		if(message.type == null){
			return;
		}

		Player player = Minecraft.getInstance().player;
		if(player != null){
			NetworkHandler.CHANNEL.sendToServer(new SyncDragonClawRender(player.getId(), ClientDragonRender.renderDragonClaws));
			NetworkHandler.CHANNEL.sendToServer(new SyncDragonSkinSettings(player.getId(), ClientDragonRender.renderNewbornSkin, ClientDragonRender.renderYoungSkin, ClientDragonRender.renderAdultSkin));

			DragonStateProvider.getCap(player).ifPresent(cap -> {
				if(cap.isDragon() && DragonEditorRegistry.getSavedCustomizations() != null){
					int currentSelected = DragonEditorRegistry.getSavedCustomizations().current.getOrDefault(message.type.getTypeName().toUpperCase(), new HashMap<>()).getOrDefault(message.level, 0);
					SkinPreset preset = DragonEditorRegistry.getSavedCustomizations().skinPresets.getOrDefault(message.type.getTypeName().toUpperCase(), new HashMap<>()).getOrDefault(currentSelected, new SkinPreset());
					NetworkHandler.CHANNEL.sendToServer(new SyncPlayerSkinPreset(player.getId(), preset));
				}
			});
		}
	}

	@SubscribeEvent
	public static void onOpenScreen(ScreenEvent.Opening openEvent){
		LocalPlayer player = Minecraft.getInstance().player;

		if(!dragonInventory){
			return;
		}
		if(Minecraft.getInstance().screen != null){
			return;
		}
		if(player == null || player.isCreative() || !DragonUtils.isDragon(player)){
			return;
		}

		if(openEvent.getScreen() instanceof InventoryScreen){
			openEvent.setCanceled(true);
			NetworkHandler.CHANNEL.sendToServer(new OpenDragonInventory());
		}
	}

	@SubscribeEvent
	public static void addCraftingButton(ScreenEvent.Init.Post initGuiEvent){
		Screen sc = initGuiEvent.getScreen();

		if(!DragonUtils.isDragon(Minecraft.getInstance().player)){
			return;
		}

		if(sc instanceof InventoryScreen screen){
			if(dragonTabs){
				initGuiEvent.addListener(new TabButton(screen.getGuiLeft(), screen.getGuiTop() - 28, TabButton.TabType.INVENTORY, screen));

				initGuiEvent.addListener(new TabButton(screen.getGuiLeft() + 28, screen.getGuiTop() - 26, TabButton.TabType.ABILITY, screen));

				initGuiEvent.addListener(new TabButton(screen.getGuiLeft() + 57, screen.getGuiTop() - 26, TabButton.TabType.GITHUB_REMINDER, screen));

				initGuiEvent.addListener(new TabButton(screen.getGuiLeft() + 86, screen.getGuiTop() - 26, TabButton.TabType.SKINS, screen));
			}

			if(inventoryToggle){
				initGuiEvent.addListener(new ImageButton(screen.getGuiLeft() + 128, screen.height / 2 - 22, 20, 18, 20, 0, 19, DragonScreen.INVENTORY_TOGGLE_BUTTON, p_onPress_1_ -> {
					SendOpenDragonInventoryAndMaintainCursorPosition();
				}){
					@Override
					public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
						super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);

						if(isHoveredOrFocused()){
							ArrayList<Component> description = new ArrayList<>(List.of(Component.translatable("ds.gui.toggle_inventory.dragon")));
							guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, description, mouseX, mouseY);
						}
					}
				});
			}
		}

		if (sc instanceof CreativeModeInventoryScreen screen) {
			if (inventoryToggle) {
				initGuiEvent.addListener(new ImageButton(screen.getGuiLeft() + 128 + 20, screen.height / 2 - 50, 20, 18, 20, 0, 19, DragonScreen.INVENTORY_TOGGLE_BUTTON, p_onPress_1_ -> {
					SendOpenDragonInventoryAndMaintainCursorPosition();
				}) {
					@Override
					public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
						active = visible = screen.isInventoryOpen();
						super.render(guiGraphics, mouseX, mouseY, partialTick);
					}

					@Override
					public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTick) {
						super.renderWidget(guiGraphics, mouseX, mouseY, pPartialTick);

						if (isHovered()) {
							guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("ds.gui.toggle_inventory.dragon"), mouseX, mouseY);
						}
					}
				});
			}
		}
	}

	@SubscribeEvent
	@OnlyIn( Dist.CLIENT )
	public static void removeFireOverlay(RenderBlockScreenEffectEvent event){
		LocalPlayer player = Minecraft.getInstance().player;
		DragonStateProvider.getCap(player).ifPresent(cap -> {
			if(cap.isDragon() && Objects.equals(cap.getType(), DragonTypes.CAVE) && event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE){
				event.setCanceled(true);
			}
		});
	}

	@SubscribeEvent
	public static void renderTrap(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> postEvent){
		LivingEntity entity = postEvent.getEntity();
		if(entity.getEffect(DragonEffects.TRAPPED) != null) {
			int light = postEvent.getPackedLight();
			int overlayCoords = LivingEntityRenderer.getOverlayCoords(entity, 0);
			MultiBufferSource buffers = postEvent.getMultiBufferSource();
			PoseStack matrixStack = postEvent.getPoseStack();
			float scale = entity.getEyeHeight();
			if(entity instanceof Player) {
				DragonStateHandler handler = DragonUtils.getHandler(entity);
				if(handler != null && handler.isDragon()) {
					scale = (float)DragonSizeHandler.calculateDragonEyeHeight(handler.getSize(), ServerConfig.hitboxGrowsPastHuman);
				}
			}
			renderBolas(light, overlayCoords, buffers, matrixStack, scale);
		}
	}

	public static void renderBolas(int light, int overlayCoords, MultiBufferSource buffers, PoseStack matrixStack, float eyeHeight){
		matrixStack.pushPose();
		matrixStack.translate(0, 0.9f + eyeHeight / 8.f, 0);
		matrixStack.scale(1.6f + eyeHeight / 8.f, 1.6f + eyeHeight / 8.f, 1.6f + eyeHeight / 8.f);
		if(BOLAS == null){
			BOLAS = new ItemStack(DSItems.huntingNet);
		}
		Minecraft.getInstance().getItemRenderer().renderStatic(BOLAS, ItemDisplayContext.NONE, light, overlayCoords, matrixStack, buffers, Minecraft.getInstance().level, 0);
		matrixStack.popPose();
	}

	@SubscribeEvent
	public static void unloadWorld(LevelEvent.Unload worldEvent){
		ClientDragonRender.playerDragonHashMap.clear();
	}

	public static String getMaterial(String texture, ItemStack clawItem){
		if (clawItem.getItem() instanceof TieredItem item) {
			Tier tier = item.getTier();

			if (tier == Tiers.NETHERITE) {
				texture += "netherite_";
			} else if (tier == Tiers.DIAMOND) {
				texture += "diamond_";
			} else if (tier == Tiers.IRON) {
				texture += "iron_";
			} else if (tier == Tiers.GOLD) {
				texture += "gold_";
			} else if (tier == Tiers.STONE) {
				texture += "stone_";
			} else if (tier == Tiers.WOOD) {
				texture += "wooden_";
			} else {
				texture += "moded_";
			}

			return texture;
		}

		return texture + "moded_";
	}

	public static void renderOverlay(final DragonStateHandler handler, final ForgeGui forgeGUI, final GuiGraphics guiGraphics) {
		LocalPlayer localPlayer = Minecraft.getInstance().player;

		if (localPlayer == null || !forgeGUI.shouldDrawSurvivalElements()) {
			return;
		}

		int rightHeight;

		if (handler.getType() instanceof SeaDragonType seaDragonType) {
			if (seaDragonType.timeWithoutWater > 0 && ServerConfig.penalties && ServerConfig.seaTicksWithoutWater != 0) {
				RenderSystem.enableBlend();

				rightHeight = forgeGUI.rightHeight;
				forgeGUI.rightHeight += 10;

				int maxTimeWithoutWater = ServerConfig.seaTicksWithoutWater;
				WaterAbility waterAbility = DragonAbilities.getSelfAbility(localPlayer, WaterAbility.class);

				if (waterAbility != null) {
					maxTimeWithoutWater += Functions.secondsToTicks(waterAbility.getDuration());
				}

				double timeWithoutWater = maxTimeWithoutWater - seaDragonType.timeWithoutWater;
				boolean flag = false;

				if (timeWithoutWater < 0) {
					flag = true;
					timeWithoutWater = Math.abs(timeWithoutWater);
				}

				final int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
				final int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
				final int full = flag ? Mth.floor(timeWithoutWater * 10.0D / maxTimeWithoutWater) : Mth.ceil((timeWithoutWater - 2) * 10.0D / maxTimeWithoutWater);
				final int partial = Mth.ceil(timeWithoutWater * 10.0D / maxTimeWithoutWater) - full;

				for (int i = 0; i < full + partial; ++i) {
					guiGraphics.blit(DRAGON_HUD, left - i * 8 - 9, top, flag ? 18 : i < full ? 0 : 9, 36, 9, 9);
				}

				RenderSystem.disableBlend();
			}
		} else if (handler.getType() instanceof CaveDragonType caveDragonType) {
			if (caveDragonType.timeInRain > 0 && ServerConfig.penalties && ServerConfig.caveRainDamage != 0.0) {
				RenderSystem.enableBlend();

				rightHeight = forgeGUI.rightHeight;
				forgeGUI.rightHeight += 10;

				ContrastShowerAbility contrastShower = DragonAbilities.getSelfAbility(localPlayer, ContrastShowerAbility.class);
				int maxRainTime = 0;

				if (contrastShower != null) {
					maxRainTime += Functions.secondsToTicks(contrastShower.getDuration());
				}

				final int timeInRain = maxRainTime - Math.min(caveDragonType.timeInRain, maxRainTime);

				final int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
				final int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
				final int full = Mth.ceil((double) (timeInRain - 2) * 10.0D / maxRainTime);
				final int partial = Mth.ceil((double) timeInRain * 10.0D / maxRainTime) - full;

				for (int i = 0; i < full + partial; ++i) {
					guiGraphics.blit(DRAGON_HUD, left - i * 8 - 9, top, i < full ? 0 : 9, 54, 9, 9);
				}

				RenderSystem.disableBlend();
			}

			if (caveDragonType.lavaAirSupply < ServerConfig.caveLavaSwimmingTicks && ServerConfig.bonuses && ServerConfig.caveLavaSwimmingTicks != 0 && ServerConfig.caveLavaSwimming) {
				RenderSystem.enableBlend();

				rightHeight = forgeGUI.rightHeight;
				forgeGUI.rightHeight += 10;

				int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
				int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
				int full = Mth.ceil((double) (caveDragonType.lavaAirSupply - 2) * 10.0D / ServerConfig.caveLavaSwimmingTicks);
				int partial = Mth.ceil((double) caveDragonType.lavaAirSupply * 10.0D / ServerConfig.caveLavaSwimmingTicks) - full;

				for (int i = 0; i < full + partial; ++i) {
					guiGraphics.blit(DRAGON_HUD, left - i * 8 - 9, top, i < full ? 0 : 9, 27, 9, 9);
				}

				RenderSystem.disableBlend();
			}
		} else if (handler.getType() instanceof ForestDragonType forestDragonType) {
			if (forestDragonType.timeInDarkness > 0 && ServerConfig.penalties && ServerConfig.forestStressTicks != 0 && !localPlayer.hasEffect(DragonEffects.STRESS)) {
				RenderSystem.enableBlend();

				rightHeight = forgeGUI.rightHeight;
				forgeGUI.rightHeight += 10;

				int maxTimeInDarkness = ServerConfig.forestStressTicks;
				LightInDarknessAbility lightInDarkness = DragonAbilities.getSelfAbility(localPlayer, LightInDarknessAbility.class);

				if (lightInDarkness != null) {
					maxTimeInDarkness += Functions.secondsToTicks(lightInDarkness.getDuration());
				}

				final int timeInDarkness = maxTimeInDarkness - Math.min(forestDragonType.timeInDarkness, maxTimeInDarkness);

				final int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
				final int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
				final int full = Mth.ceil((double) (timeInDarkness - 2) * 10.0D / maxTimeInDarkness);
				final int partial = Mth.ceil((double) timeInDarkness * 10.0D / maxTimeInDarkness) - full;

				for (int i = 0; i < full + partial; ++i) {
					guiGraphics.blit(DRAGON_HUD, left - i * 8 - 9, top, i < full ? 0 : 9, 45, 9, 9);
				}

				RenderSystem.disableBlend();
			}
		}
	}
}
