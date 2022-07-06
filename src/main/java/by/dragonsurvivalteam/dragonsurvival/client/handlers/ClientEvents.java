package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.DragonAltarGUI;
import by.dragonsurvivalteam.dragonsurvival.client.gui.DragonScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.render.CaveLavaFluidRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.skinPartSystem.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.skinPartSystem.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.common.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.common.capability.provider.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.Bolas;
import by.dragonsurvivalteam.dragonsurvival.common.items.DSItems;
import by.dragonsurvivalteam.dragonsurvival.common.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.common.magic.abilities.Passives.ContrastShowerAbility;
import by.dragonsurvivalteam.dragonsurvival.common.magic.abilities.Passives.LightInDarknessAbility;
import by.dragonsurvivalteam.dragonsurvival.common.magic.abilities.Passives.WaterAbility;
import by.dragonsurvivalteam.dragonsurvival.common.magic.common.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.common.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.misc.DragonType;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.RequestClientData;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonInventory;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.entity.player.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber( Dist.CLIENT )
public class ClientEvents{

	public static final ResourceLocation DRAGON_HUD = new ResourceLocation(DragonSurvivalMod.MODID + ":textures/gui/dragon_hud.png");
	/**
	 * Durations of jumps
	 */
	public static ConcurrentHashMap<Integer, Integer> dragonsJumpingTicks = new ConcurrentHashMap<>(20);
	public static double mouseX = -1;
	public static double mouseY = -1;
	@ConfigOption( side = Dist.CLIENT, category = "inventory", key = "dragonInventory", comment = "Should the default inventory be replaced as a dragon?" )
	public static Boolean dragonInventory = true;
	@ConfigOption( side = Dist.CLIENT, category = "inventory", key = "dragonTabs", comment = "Should dragon tabs be added to the default player inventory?" )
	public static Boolean dragonTabs = true;
	@ConfigOption( side = Dist.CLIENT, category = "inventory", key = "inventoryToggle", comment = "Should the buttons for toggeling between dragon and normaly inventory be added?" )
	public static Boolean inventoryToggle = true;
	private static ItemStack BOLAS;
	private static boolean wasCaveDragon = false;
	private static LiquidBlockRenderer prevFluidRenderer;

	@SubscribeEvent
	public static void decreaseJumpDuration(TickEvent.PlayerTickEvent playerTickEvent){
		if(playerTickEvent.phase == TickEvent.Phase.END){
			Player player = playerTickEvent.player;
			dragonsJumpingTicks.computeIfPresent(player.getId(), (playerEntity1, integer) -> integer > 0 ? integer - 1 : integer);
		}
	}

	@OnlyIn( Dist.CLIENT )
	public static void sendClientData(RequestClientData message){
		Player player = Minecraft.getInstance().player;

		if(player != null && player.level != null){
			NetworkHandler.CHANNEL.sendToServer(new SyncDragonClawRender(player.getId(), ClientDragonRender.renderDragonClaws));
			NetworkHandler.CHANNEL.sendToServer(new SyncDragonSkinSettings(player.getId(), ClientDragonRender.renderNewbornSkin, ClientDragonRender.renderYoungSkin, ClientDragonRender.renderAdultSkin));

			DragonStateProvider.getCap(player).ifPresent(cap -> {
				cap.getMagic().getAbilities();

				if(DragonEditorRegistry.savedCustomizations != null){
					int currentSelected = DragonEditorRegistry.savedCustomizations.current.getOrDefault(message.type, new HashMap<>()).getOrDefault(message.level, 0);
					SkinPreset preset = DragonEditorRegistry.savedCustomizations.skinPresets.getOrDefault(message.type, new HashMap<>()).getOrDefault(currentSelected, new SkinPreset());
					NetworkHandler.CHANNEL.sendToServer(new SyncPlayerSkinPreset(player.getId(), preset));
				}
			});

			if(player == Minecraft.getInstance().player){
				DragonStateProvider.getCap(player).ifPresent(cap -> {
					cap.hasUsedAltar = cap.hasUsedAltar || cap.isDragon();

					if(!cap.hasUsedAltar && ServerConfig.startWithDragonChoice){
						Minecraft.getInstance().setScreen(new DragonAltarGUI());
						cap.hasUsedAltar = true;
					}
				});
			}
		}
	}

	@SubscribeEvent
	public static void onOpenScreen(ScreenOpenEvent openEvent){
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
	public static void addCraftingButton(ScreenEvent.InitScreenEvent.Post initGuiEvent){
		Screen sc = initGuiEvent.getScreen();

		if(!DragonUtils.isDragon(Minecraft.getInstance().player)){
			return;
		}

		if(sc instanceof InventoryScreen){
			InventoryScreen screen = (InventoryScreen)sc;

			if(dragonTabs){
				initGuiEvent.addListener(new TabButton(screen.getGuiLeft(), screen.getGuiTop() - 28, 0, screen){
					@Override
					public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_){
						super.renderButton(p_230431_1_, p_230431_2_, p_230431_3_, p_230431_4_);
						this.x = screen.getGuiLeft();
					}
				});

				initGuiEvent.addListener(new TabButton(screen.getGuiLeft() + 28, screen.getGuiTop() - 26, 1, screen){
					@Override
					public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_){
						super.renderButton(p_230431_1_, p_230431_2_, p_230431_3_, p_230431_4_);
						this.x = screen.getGuiLeft() + 28;
					}
				});

				initGuiEvent.addListener(new TabButton(screen.getGuiLeft() + 57, screen.getGuiTop() - 26, 2, screen){
					@Override
					public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_){
						super.renderButton(p_230431_1_, p_230431_2_, p_230431_3_, p_230431_4_);
						this.x = screen.getGuiLeft() + 57;
					}
				});

				initGuiEvent.addListener(new TabButton(screen.getGuiLeft() + 86, screen.getGuiTop() - 26, 3, screen){
					@Override
					public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_){
						super.renderButton(p_230431_1_, p_230431_2_, p_230431_3_, p_230431_4_);
						this.x = screen.getGuiLeft() + 86;
					}
				});
			}

			if(inventoryToggle){
				initGuiEvent.addListener(new ImageButton(screen.getGuiLeft() + 128, screen.height / 2 - 22, 20, 18, 20, 0, 19, DragonScreen.INVENTORY_TOGGLE_BUTTON, p_onPress_1_ -> {
					NetworkHandler.CHANNEL.sendToServer(new OpenDragonInventory());
				}){
					@Override
					public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_){
						super.renderButton(p_230431_1_, p_230431_2_, p_230431_3_, p_230431_4_);
						this.x = screen.getGuiLeft() + 128;

						if(isHoveredOrFocused()){
							ArrayList<Component> description = new ArrayList<>(Arrays.asList(new TranslatableComponent("ds.gui.toggle_inventory.dragon")));
							Minecraft.getInstance().screen.renderComponentTooltip(p_230431_1_, description, p_230431_2_, p_230431_3_);
						}
					}
				});
			}
		}

		if(sc instanceof CreativeModeInventoryScreen){
			CreativeModeInventoryScreen screen = (CreativeModeInventoryScreen)sc;

			if(inventoryToggle){
				initGuiEvent.addListener(new ImageButton(screen.getGuiLeft() + 128 + 20, screen.height / 2 - 50, 20, 18, 20, 0, 19, DragonScreen.INVENTORY_TOGGLE_BUTTON, p_onPress_1_ -> {
					NetworkHandler.CHANNEL.sendToServer(new OpenDragonInventory());
				}){
					@Override
					public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_){
						this.active = this.visible = screen.getSelectedTab() == CreativeModeTab.TAB_INVENTORY.getId();
						super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
					}

					@Override
					public void renderButton(PoseStack p_230431_1_, int p_230431_2_, int p_230431_3_, float p_230431_4_){
						super.renderButton(p_230431_1_, p_230431_2_, p_230431_3_, p_230431_4_);
						if(isHoveredOrFocused()){
							ArrayList<Component> description = new ArrayList<>(Arrays.asList(new TranslatableComponent("ds.gui.toggle_inventory.dragon")));
							Minecraft.getInstance().screen.renderComponentTooltip(p_230431_1_, description, p_230431_2_, p_230431_3_);
						}
					}
				});
			}
		}
	}

	@SubscribeEvent
	@OnlyIn( Dist.CLIENT )
	public static void removeFireOverlay(RenderBlockOverlayEvent event){
		LocalPlayer player = Minecraft.getInstance().player;
		DragonStateProvider.getCap(player).ifPresent(cap -> {
			if(cap.isDragon() && cap.getType() == DragonType.CAVE && event.getOverlayType() == OverlayType.FIRE){
				event.setCanceled(true);
			}
		});
	}

	@SubscribeEvent
	public static void renderTrap(RenderLivingEvent.Pre<LivingEntity, EntityModel<LivingEntity>> postEvent){
		LivingEntity entity = postEvent.getEntity();
		if(!(entity instanceof Player) && entity.getAttributes().hasAttribute(Attributes.MOVEMENT_SPEED)){
			AttributeModifier bolasTrap = new AttributeModifier(Bolas.DISABLE_MOVEMENT, "Bolas trap", -entity.getAttribute(Attributes.MOVEMENT_SPEED).getValue(), AttributeModifier.Operation.ADDITION);
			if(entity.getAttribute(Attributes.MOVEMENT_SPEED).hasModifier(bolasTrap)){
				int light = postEvent.getPackedLight();
				int overlayCoords = LivingEntityRenderer.getOverlayCoords(entity, 0);
				MultiBufferSource buffers = postEvent.getMultiBufferSource();
				PoseStack matrixStack = postEvent.getPoseStack();
				renderBolas(light, overlayCoords, buffers, matrixStack);
			}
		}
	}

	public static void renderBolas(int light, int overlayCoords, MultiBufferSource buffers, PoseStack matrixStack){
		matrixStack.pushPose();
		matrixStack.scale(3, 3, 3);
		matrixStack.translate(0, 0.5, 0);
		if(BOLAS == null){
			BOLAS = new ItemStack(DSItems.huntingNet);
		}
		Minecraft.getInstance().getItemRenderer().renderStatic(BOLAS, TransformType.NONE, light, overlayCoords, matrixStack, buffers, 0);
		matrixStack.popPose();
	}

	@SubscribeEvent
	public static void unloadWorld(WorldEvent.Unload worldEvent){
		ClientDragonRender.playerDragonHashMap.clear();
	}

	public static String getMaterial(String texture, ItemStack clawItem){
		TieredItem item = (TieredItem)clawItem.getItem();
		Tier tier = item.getTier();
		if(tier == Tiers.NETHERITE){
			texture += "netherite_";
		}else if(tier == Tiers.DIAMOND){
			texture += "diamond_";
		}else if(tier == Tiers.IRON){
			texture += "iron_";
		}else if(tier == Tiers.GOLD){
			texture += "gold_";
		}else if(tier == Tiers.STONE){
			texture += "stone_";
		}else if(tier == Tiers.WOOD){
			texture += "wooden_";
		}else{
			texture += "moded_";
		}
		return texture;
	}

	//TODO Disabled until a solution to dynamically change RenderLayers is found
	//@SubscribeEvent
	@OnlyIn( Dist.CLIENT )
	public static void onRenderWorldLastEvent(RenderLevelLastEvent event){
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		DragonStateProvider.getCap(player).ifPresent(playerStateHandler -> {
			if(playerStateHandler.getType() == DragonType.CAVE && ServerConfig.bonuses && ServerConfig.caveLavaSwimming){
				if(!wasCaveDragon){
					if(player.hasEffect(DragonEffects.LAVA_VISION)){
						RenderType lavaType = RenderType.translucent();
						ItemBlockRenderTypes.setRenderLayer(Fluids.LAVA, lavaType);
						ItemBlockRenderTypes.setRenderLayer(Fluids.FLOWING_LAVA, lavaType);
						prevFluidRenderer = minecraft.getBlockRenderer().liquidBlockRenderer;
						minecraft.getBlockRenderer().liquidBlockRenderer = new CaveLavaFluidRenderer();
						minecraft.levelRenderer.allChanged();
					}
				}else{
					if(!player.hasEffect(DragonEffects.LAVA_VISION)){
						if(prevFluidRenderer != null){
							RenderType lavaType = RenderType.solid();
							ItemBlockRenderTypes.setRenderLayer(Fluids.LAVA, lavaType);
							ItemBlockRenderTypes.setRenderLayer(Fluids.FLOWING_LAVA, lavaType);
							minecraft.getBlockRenderer().liquidBlockRenderer = prevFluidRenderer;
						}
						minecraft.levelRenderer.allChanged();
					}
				}
			}else{
				if(wasCaveDragon){
					if(prevFluidRenderer != null){
						RenderType lavaType = RenderType.solid();
						ItemBlockRenderTypes.setRenderLayer(Fluids.LAVA, lavaType);
						ItemBlockRenderTypes.setRenderLayer(Fluids.FLOWING_LAVA, lavaType);
						minecraft.getBlockRenderer().liquidBlockRenderer = prevFluidRenderer;
					}
					minecraft.levelRenderer.allChanged();
				}
			}
			wasCaveDragon = playerStateHandler.getType() == DragonType.CAVE && player.hasEffect(DragonEffects.LAVA_VISION);
		});
	}

	public static void onRenderOverlayPreTick(ForgeIngameGui gui, PoseStack mStack, float partialTicks, int width, int height){
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;

		if(mc.options.hideGui || !gui.shouldDrawSurvivalElements()){
			return;
		}

		if(!DragonUtils.isDragon(player)){
			ForgeIngameGui.AIR_LEVEL_ELEMENT.render(gui, mStack, partialTicks, width, height);
			return;
		}

		DragonStateProvider.getCap(player).ifPresent(playerStateHandler -> {
			int rightHeight = 0;

			//            if (playerStateHandler.getType() == DragonType.SEA && ServerConfig.bonuses && ServerConfig.seaSwimmingBonuses) event.setCanceled(true);
			if(playerStateHandler.getDebuffData().timeWithoutWater > 0 && playerStateHandler.getType() == DragonType.SEA && ServerConfig.penalties && ServerConfig.seaTicksWithoutWater != 0){
				RenderSystem.enableBlend();
				RenderSystem.setShaderTexture(0, DRAGON_HUD);

				if(Minecraft.getInstance().gui instanceof ForgeIngameGui){
					rightHeight = ((ForgeIngameGui)Minecraft.getInstance().gui).right_height;
					((ForgeIngameGui)Minecraft.getInstance().gui).right_height += 10;
				}

				int maxTimeWithoutWater = ServerConfig.seaTicksWithoutWater;
				DragonAbility waterAbility = playerStateHandler.getMagic().getAbility(DragonAbilities.WATER);

				if(waterAbility != null){
					maxTimeWithoutWater += Functions.secondsToTicks(((WaterAbility)waterAbility).getDuration());
				}

				double timeWithoutWater = maxTimeWithoutWater - playerStateHandler.getDebuffData().timeWithoutWater;
				boolean flag = false;
				if(timeWithoutWater < 0){
					flag = true;
					timeWithoutWater = Math.abs(timeWithoutWater);
				}

				final int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
				final int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
				final int full = flag ? Mth.floor(timeWithoutWater * 10.0D / maxTimeWithoutWater) : Mth.ceil((timeWithoutWater - 2) * 10.0D / maxTimeWithoutWater);
				final int partial = Mth.ceil(timeWithoutWater * 10.0D / maxTimeWithoutWater) - full;

				for(int i = 0; i < full + partial; ++i){
					Minecraft.getInstance().gui.blit(mStack, left - i * 8 - 9, top, (flag ? 18 : i < full ? 0 : 9), 36, 9, 9);
				}


				RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);
				RenderSystem.disableBlend();
			}
			if(playerStateHandler.getLavaAirSupply() < ServerConfig.caveLavaSwimmingTicks && playerStateHandler.getType() == DragonType.CAVE && ServerConfig.bonuses && ServerConfig.caveLavaSwimmingTicks != 0 && ServerConfig.caveLavaSwimming){
				RenderSystem.enableBlend();
				RenderSystem.setShaderTexture(0, DRAGON_HUD);

				if(Minecraft.getInstance().gui instanceof ForgeIngameGui){
					rightHeight = ((ForgeIngameGui)Minecraft.getInstance().gui).right_height;
					((ForgeIngameGui)Minecraft.getInstance().gui).right_height += 10;
				}

				final int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
				final int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
				final int full = Mth.ceil((double)(playerStateHandler.getLavaAirSupply() - 2) * 10.0D / ServerConfig.caveLavaSwimmingTicks);
				final int partial = Mth.ceil((double)playerStateHandler.getLavaAirSupply() * 10.0D / ServerConfig.caveLavaSwimmingTicks) - full;

				for(int i = 0; i < full + partial; ++i){
					Minecraft.getInstance().gui.blit(mStack, left - i * 8 - 9, top, (i < full ? 0 : 9), 27, 9, 9);
				}

				RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);
				RenderSystem.disableBlend();
			}
			if(playerStateHandler.getDebuffData().timeInDarkness > 0 && playerStateHandler.getType() == DragonType.FOREST && ServerConfig.penalties && ServerConfig.forestStressTicks != 0 && !player.hasEffect(DragonEffects.STRESS)){
				RenderSystem.enableBlend();
				RenderSystem.setShaderTexture(0, DRAGON_HUD);

				if(Minecraft.getInstance().gui instanceof ForgeIngameGui){
					rightHeight = ((ForgeIngameGui)Minecraft.getInstance().gui).right_height;
					((ForgeIngameGui)Minecraft.getInstance().gui).right_height += 10;
				}

				int maxTimeInDarkness = ServerConfig.forestStressTicks;
				DragonAbility lightInDarkness = playerStateHandler.getMagic().getAbility(DragonAbilities.LIGHT_IN_DARKNESS);

				if(lightInDarkness != null){
					maxTimeInDarkness += Functions.secondsToTicks(((LightInDarknessAbility)lightInDarkness).getDuration());
				}

				final int timeInDarkness = maxTimeInDarkness - Math.min(playerStateHandler.getDebuffData().timeInDarkness, maxTimeInDarkness);

				final int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
				final int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
				final int full = Mth.ceil((double)(timeInDarkness - 2) * 10.0D / maxTimeInDarkness);
				final int partial = Mth.ceil((double)timeInDarkness * 10.0D / maxTimeInDarkness) - full;

				for(int i = 0; i < full + partial; ++i){
					Minecraft.getInstance().gui.blit(mStack, left - i * 8 - 9, top, (i < full ? 0 : 9), 45, 9, 9);
				}

				RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);
				RenderSystem.disableBlend();
			}

			if(playerStateHandler.getDebuffData().timeInRain > 0 && playerStateHandler.getType() == DragonType.CAVE && ServerConfig.penalties && ServerConfig.caveRainDamage != 0.0){
				RenderSystem.enableBlend();
				RenderSystem.setShaderTexture(0, DRAGON_HUD);

				if(Minecraft.getInstance().gui instanceof ForgeIngameGui){
					rightHeight = ((ForgeIngameGui)Minecraft.getInstance().gui).right_height;
					((ForgeIngameGui)Minecraft.getInstance().gui).right_height += 10;
				}

				DragonAbility contrastShower = playerStateHandler.getMagic().getAbility(DragonAbilities.CONTRAST_SHOWER);
				int maxRainTime = 0;

				if(contrastShower != null){
					maxRainTime += Functions.secondsToTicks(((ContrastShowerAbility)contrastShower).getDuration());
				}


				final int timeInRain = maxRainTime - Math.min(playerStateHandler.getDebuffData().timeInRain, maxRainTime);

				final int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
				final int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
				final int full = Mth.ceil((double)(timeInRain - 2) * 10.0D / maxRainTime);
				final int partial = Mth.ceil((double)timeInRain * 10.0D / maxRainTime) - full;

				for(int i = 0; i < full + partial; ++i){
					Minecraft.getInstance().gui.blit(mStack, left - i * 8 - 9, top, (i < full ? 0 : 9), 54, 9, 9);
				}

				RenderSystem.setShaderTexture(0, Gui.GUI_ICONS_LOCATION);
				RenderSystem.disableBlend();
			}
		});
	}
}