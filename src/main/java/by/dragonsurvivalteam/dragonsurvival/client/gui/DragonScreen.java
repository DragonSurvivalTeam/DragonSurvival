package by.dragonsurvivalteam.dragonsurvival.client.gui;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.settings.ConfigSideSelectionScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.DSButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.DSImageButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.Keybind;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonGrowthHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.claw.DragonClawsMenuToggle;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenInventory;
import by.dragonsurvivalteam.dragonsurvival.network.container.SortInventoryPacket;
import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.awt.Color;
import java.util.*;

public class DragonScreen extends EffectRenderingInventoryScreen<DragonContainer>{
	public static final ResourceLocation INVENTORY_TOGGLE_BUTTON = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/inventory_button.png");
	public static final ResourceLocation SORTING_BUTTON = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/sorting_button.png");
	public static final ResourceLocation SETTINGS_BUTTON = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/settings_button.png");
	static final ResourceLocation BACKGROUND = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/dragon_inventory.png");
	private static final ResourceLocation CLAWS_TEXTURE = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/dragon_claws.png");
	private static final ResourceLocation DRAGON_CLAW_BUTTON = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/dragon_claws_button.png");
	private static final ResourceLocation DRAGON_CLAW_CHECKMARK = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/dragon_claws_checked.png");
	private final Player player;
	public boolean clawsMenu = false;
	private boolean buttonClicked;

	private static HashMap<String, ResourceLocation> textures;

	static {
		initResources();
	}

	private static void initResources() {
		textures = new HashMap<>();

		Set<String> keys = DragonTypes.staticTypes.keySet();

		for (String key : keys) {
			AbstractDragonType type = DragonTypes.staticTypes.get(key);

			String start = "textures/gui/growth/";
			String end = ".png";

			for (int i = 1; i <= DragonLevel.values().length; i++) {
				String growthResource = createTextureKey(type, "growth", "_" + i);
				textures.put(growthResource, new ResourceLocation(DragonSurvivalMod.MODID, start + growthResource + end));
			}

			String circleResource = createTextureKey(type, "circle", "");
			textures.put(circleResource, new ResourceLocation(DragonSurvivalMod.MODID, start + circleResource + end));
		}
	}

	private static String createTextureKey(final AbstractDragonType type, final String textureType, final String addition) {
		return textureType + "_" + type.getTypeName().toLowerCase() + addition;
	}

	public DragonScreen(DragonContainer screenContainer, Inventory inv, Component titleIn){
		super(screenContainer, inv, titleIn);
		passEvents = true;
		player = inv.player;

		DragonStateProvider.getCap(player).ifPresent(cap -> clawsMenu = cap.getClawToolData().isMenuOpen());

		imageWidth = 203;
		imageHeight = 166;
	}
	@Override
	protected void init(){
		super.init();

		if(ClientEvents.mouseX != -1 && ClientEvents.mouseY != -1){
			if(minecraft.getWindow() != null){
				InputConstants.grabOrReleaseMouse(minecraft.getWindow().getWindow(), 212993, ClientEvents.mouseX, ClientEvents.mouseY);
				ClientEvents.mouseX = -1;
				ClientEvents.mouseY = -1;
			}
		}

		leftPos = (width - imageWidth) / 2;

		DragonStateHandler handler = DragonUtils.getHandler(player);

		addRenderableWidget(new TabButton(leftPos, topPos - 28, TabButton.TabType.INVENTORY, this));
		addRenderableWidget(new TabButton(leftPos + 28, topPos - 26, TabButton.TabType.ABILITY, this));
		addRenderableWidget(new TabButton(leftPos + 57, topPos - 26, TabButton.TabType.GITHUB_REMINDER, this));
		addRenderableWidget(new TabButton(leftPos + 86, topPos - 26, TabButton.TabType.SKINS, this));

		addRenderableWidget(new DSButton(leftPos + 27, topPos + 10, 11, 11 , p_onPress_1_ -> {
			clawsMenu = !clawsMenu;
			clearWidgets();
			init();

			NetworkHandler.CHANNEL.sendToServer(new DragonClawsMenuToggle(clawsMenu));
			DragonStateProvider.getCap(player).ifPresent(cap -> cap.getClawToolData().setMenuOpen(clawsMenu));
		}, Component.translatable("ds.gui.claws")){
			@Override
			public void renderButton(PoseStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				stack.pushPose();
				RenderSystem.disableDepthTest();
				RenderSystem.setShaderTexture(0, DRAGON_CLAW_BUTTON);
				blit(stack, x, y, 0, 0, 11, 11, 11, 11);
				RenderSystem.enableDepthTest();
				stack.popPose();
			}
		});

		addRenderableWidget(new HelpButton(leftPos - 58, topPos - 40, 32, 32, null, 0){
			@Override
			public void renderButton(PoseStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_){
				visible = clawsMenu;
				active = clawsMenu;
			}

			@Override
			public void renderToolTip(PoseStack stack, int mouseX, int mouseY){
				String age = (int)handler.getSize() - handler.getLevel().size + "/";
				double seconds = 0;

				if(handler.getLevel() == DragonLevel.NEWBORN){
					age += DragonLevel.YOUNG.size - handler.getLevel().size;
					double missing = DragonLevel.YOUNG.size - handler.getSize();
					double increment = (DragonLevel.YOUNG.size - DragonLevel.NEWBORN.size) / (DragonGrowthHandler.newbornToYoung * 20.0) * ServerConfig.newbornGrowthModifier;
					seconds = missing / increment / 20;
				}else if(handler.getLevel() == DragonLevel.YOUNG){
					age += DragonLevel.ADULT.size - handler.getLevel().size;

					double missing = DragonLevel.ADULT.size - handler.getSize();
					double increment = (DragonLevel.ADULT.size - DragonLevel.YOUNG.size) / (DragonGrowthHandler.youngToAdult * 20.0) * ServerConfig.youngGrowthModifier;
					seconds = missing / increment / 20;
				}else if(handler.getLevel() == DragonLevel.ADULT && handler.getSize() < 40){
					age += 40 - handler.getLevel().size;

					double missing = 40 - handler.getSize();
					double increment = (40 - DragonLevel.ADULT.size) / (DragonGrowthHandler.adultToMax * 20.0) * ServerConfig.adultGrowthModifier;
					seconds = missing / increment / 20;
				}else if(handler.getLevel() == DragonLevel.ADULT && handler.getSize() >= 40){
					age += (int)(ServerConfig.maxGrowthSize - handler.getLevel().size);

					double missing = ServerConfig.maxGrowthSize - handler.getSize();
					double increment = (ServerConfig.maxGrowthSize - 40) / (DragonGrowthHandler.beyond * 20.0) * ServerConfig.maxGrowthModifier;
					seconds = missing / increment / 20;
				}

				if(seconds != 0){
					int minutes = (int)(seconds / 60);
					seconds -= minutes * 60;

					int hours = minutes / 60;
					minutes -= hours * 60;

					String hourString = hours > 0 ? hours >= 10 ? Integer.toString(hours) : "0" + hours : "00";
					String minuteString = minutes > 0 ? minutes >= 10 ? Integer.toString(minutes) : "0" + minutes : "00";

					if(handler.growing){
						age += " (" + hourString + ":" + minuteString + ")";
					}else{
						age += " (§4--:--§r)";
					}
				}

				ArrayList<Item> allowedList = new ArrayList<>();

				List<Item> newbornList = ConfigHandler.getResourceElements(Item.class, ServerConfig.growNewborn);
				List<Item> youngList = ConfigHandler.getResourceElements(Item.class, ServerConfig.growYoung);
				List<Item> adultList = ConfigHandler.getResourceElements(Item.class, ServerConfig.growAdult);

				if(handler.getSize() < DragonLevel.YOUNG.size){
					allowedList.addAll(newbornList);
				}else if(handler.getSize() < DragonLevel.ADULT.size){
					allowedList.addAll(youngList);
				}else{
					allowedList.addAll(adultList);
				}

				List<String> displayData = allowedList.stream().map(i -> new ItemStack(i).getDisplayName().getString()).toList();
				StringJoiner result = new StringJoiner(", ");
				displayData.forEach(result::add);

				ArrayList<Component> description = new ArrayList<>(Arrays.asList(Component.translatable("ds.gui.growth_stage", handler.getLevel().getName()), Component.translatable("ds.gui.growth_age", age), Component.translatable("ds.gui.growth_help", result)));
				Minecraft.getInstance().screen.renderComponentTooltip(stack, description, mouseX, mouseY);
			}

			@Override
			public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_){
				isHovered = p_230430_2_ >= x && p_230430_3_ >= y && p_230430_2_ < x + width && p_230430_3_ < y + height;
			}
		});

		addRenderableWidget(new HelpButton(leftPos - 80 + 34, topPos + 112, 9, 9, "ds.skill.help.claws", 0){
			@Override
			public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_){
				visible = clawsMenu;
				active = clawsMenu;
				super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
			}
		});

		// Button to enable / disable the rendering of claws
		addRenderableWidget(new DSButton(leftPos - 80 + 34, topPos + 140, 9, 9, null, p_onPress_1_ -> {
			boolean claws = !handler.getClawToolData().shouldRenderClaws;

			handler.getClawToolData().shouldRenderClaws = claws;
			ConfigHandler.updateConfigValue("renderDragonClaws", handler.getClawToolData().shouldRenderClaws);
			NetworkHandler.CHANNEL.sendToServer(new SyncDragonClawRender(player.getId(), claws));
		}, Component.translatable("ds.gui.claws.rendering")){
			@Override
			public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_){
				active = clawsMenu;
				DragonStateHandler handler = DragonUtils.getHandler(player);

				if(handler.getClawToolData().shouldRenderClaws && clawsMenu){
					RenderSystem.setShaderTexture(0, DRAGON_CLAW_CHECKMARK);
					blit(p_230430_1_, x, y, 0, 0, 9, 9, 9, 9);
				}
				isHovered = p_230430_2_ >= x && p_230430_3_ >= y && p_230430_2_ < x + width && p_230430_3_ < y + height;
			}
		});

		//DSButton
		if(ClientEvents.inventoryToggle){
			addRenderableWidget(new DSImageButton(leftPos + imageWidth - 28, height / 2 - 30 + 47, 20, 18, 0, 0, 19, INVENTORY_TOGGLE_BUTTON, p_onPress_1_ -> {
				Minecraft.getInstance().setScreen(new InventoryScreen(player));
				NetworkHandler.CHANNEL.sendToServer(new OpenInventory());
			}, Component.translatable("ds.gui.toggle_inventory.vanilla")));
		}

		addRenderableWidget(new DSImageButton(leftPos + imageWidth - 28, height / 2 - 1, 20, 18, 0, 0, 18, SORTING_BUTTON, p_onPress_1_ -> {
			NetworkHandler.CHANNEL.sendToServer(new SortInventoryPacket());
		}, Component.translatable("ds.gui.sort")));

		addRenderableWidget(new DSImageButton(leftPos + imageWidth - 28, height / 2 + 35, 20, 18, 0, 0, 18, SETTINGS_BUTTON, p_onPress_1_ -> {
			Minecraft.getInstance().setScreen(new ConfigSideSelectionScreen(this, Minecraft.getInstance().options, Component.translatable("ds.gui.tab_button.4")));
		}, Component.translatable("ds.gui.tab_button.4")));
	}

	@Override
	protected void renderLabels(PoseStack stack, int p_230451_2_, int p_230451_3_){}

	@Override
	protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY){
		renderBackground(stack);

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, BACKGROUND);
		RenderSystem.enableBlend();
		blit(stack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		RenderSystem.disableBlend();
		DragonStateHandler handler = DragonUtils.getHandler(player);

		if(clawsMenu){
			RenderSystem.setShaderTexture(0, CLAWS_TEXTURE);
			blit(stack, leftPos - 80, topPos, 0, 0, 77, 170);
		}

		if(clawsMenu){
			if (textures == null || textures.isEmpty()) {
				initResources();
			}

			double curSize = handler.getSize();
			float progress = 0;

			if(handler.getLevel() == DragonLevel.NEWBORN){
				progress = (float)((curSize - DragonLevel.NEWBORN.size) / (DragonLevel.YOUNG.size - DragonLevel.NEWBORN.size));
			}else if(handler.getLevel() == DragonLevel.YOUNG){
				progress = (float)((curSize - DragonLevel.YOUNG.size) / (DragonLevel.ADULT.size - DragonLevel.YOUNG.size));
			}else if(handler.getLevel() == DragonLevel.ADULT && handler.getSize() < 40){
				progress = (float)((curSize - DragonLevel.ADULT.size) / (40 - DragonLevel.ADULT.size));
			}else if(handler.getLevel() == DragonLevel.ADULT && handler.getSize() >= 40){
				progress = (float)((curSize - 40) / (ServerConfig.maxGrowthSize - 40));
			}

			int size = 34;
			int thickness = 5;
			int circleX = leftPos - 58;
			int circleY = topPos - 40;
			int sides = 6;

			int radius = size / 2;

			stack.pushPose();

			RenderSystem.disableTexture();
			Color c = new Color(99, 99, 99);

			RenderSystem.setShaderColor(c.brighter().getRed() / 255.0f, c.brighter().getBlue() / 255.0f, c.brighter().getGreen() / 255.0f, 1.0f);
			RenderingUtils.drawSmoothCircle(stack, circleX + radius, circleY + radius, radius, sides, 1, 0);

			RenderSystem.enableTexture();
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1.0f);
			RenderSystem.setShaderTexture(0, textures.get(createTextureKey(handler.getType(), "circle", "")));
			RenderingUtils.drawTexturedCircle(stack, circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, sides, progress, -0.5);

			RenderSystem.disableTexture();
			RenderSystem.setShaderColor(c.getRed() / 255.0f, c.getBlue() / 255.0f, c.getGreen() / 255.0f, 1.0f);
			RenderingUtils.drawSmoothCircle(stack, circleX + radius, circleY + radius, radius - thickness, sides, 1, 0);
			RenderSystem.enableTexture();

			stack.translate(0, 0, 150); // Don't get overlayed by other rendered elements
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1.0f);
			RenderSystem.setShaderTexture(0, textures.get(createTextureKey(handler.getType(), "growth", "_" + (handler.getLevel().ordinal() + 1))));
			blit(stack, circleX + 6, circleY + 6, 0, 0, 20, 20, 20, 20);

			stack.popPose();
		}
	}



	@Override
	public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_){
		if(buttonClicked){
			buttonClicked = false;
			return true;
		}else{
			return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
		}
	}

	@Override
	public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_){
		InputConstants.Key mouseKey = InputConstants.getKey(p_231046_1_, p_231046_2_);

		if(Keybind.DRAGON_INVENTORY.isActiveAndMatches(mouseKey)){
			onClose();
			return true;
		}

		return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
	}

	@Override
	public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
		renderTooltip(pPoseStack, pMouseX, pMouseY);

		DragonStateHandler handler = DragonUtils.getHandler(player);

		pPoseStack.pushPose();

		RenderSystem.enableScissor((int)((leftPos + 26) * Minecraft.getInstance().getWindow().getGuiScale()), (int)(height * Minecraft.getInstance().getWindow().getGuiScale() - (topPos + 79) * Minecraft.getInstance().getWindow().getGuiScale()), (int)(76 * Minecraft.getInstance().getWindow().getGuiScale()), (int)(70 * Minecraft.getInstance().getWindow().getGuiScale()));
		double renderedSize = Math.min(handler.getSize(), ServerConfig.DEFAULT_MAX_GROWTH_SIZE) / 6;
		pPoseStack.translate(0, 10., 0);
		InventoryScreen.renderEntityInInventory(leftPos + 65, topPos + 65 + (int)(renderedSize * 1.25), (int)renderedSize + 15, (float)(leftPos + 51 - pMouseX), (float)(topPos + 75 - 50 - pMouseY), minecraft.player);
		RenderSystem.disableScissor();

		pPoseStack.popPose();
	}
}