package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonGrowthHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.input.Keybind;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenuToggle;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenInventory;
import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import java.awt.*;
import java.util.*;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

public class DragonInventoryScreen extends EffectRenderingInventoryScreen<DragonContainer>{
	public static final ResourceLocation INVENTORY_TOGGLE_BUTTON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory_button.png");
	public static final ResourceLocation SETTINGS_BUTTON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/settings_button.png");
	static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dragon_inventory.png");
	private static final ResourceLocation CLAWS_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dragon_claws.png");
	private static final ResourceLocation DRAGON_CLAW_BUTTON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dragon_claws_button.png");
	private static final ResourceLocation DRAGON_CLAW_CHECKMARK = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dragon_claws_checked.png");
	public static double mouseX = -1;
	public static double mouseY = -1;
	private final List<ExtendedButton> clawMenuButtons = new ArrayList<>();
	private final Player player;
	public boolean clawsMenu = false;
	private boolean buttonClicked;
	private boolean isGrowthIconHovered;

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
				textures.put(growthResource, ResourceLocation.fromNamespaceAndPath(MODID, start + growthResource + end));
			}

			String circleResource = createTextureKey(type, "circle", "");
			textures.put(circleResource, ResourceLocation.fromNamespaceAndPath(MODID, start + circleResource + end));
		}
	}

	private static String createTextureKey(final AbstractDragonType type, final String textureType, final String addition) {
		return textureType + "_" + type.getTypeNameLowerCase() + addition;
	}

	public DragonInventoryScreen(DragonContainer screenContainer, Inventory inv, Component titleIn){
		super(screenContainer, inv, titleIn);
		player = inv.player;

		DragonStateProvider.getCap(player).ifPresent(cap -> clawsMenu = cap.getClawToolData().isMenuOpen());

		imageWidth = 203;
		imageHeight = 166;
	}
	@Override
	protected void init(){
		super.init();

		if(mouseX != -1 && mouseY != -1){
			if(minecraft.getWindow() != null){
				InputConstants.grabOrReleaseMouse(minecraft.getWindow().getWindow(), 212993, mouseX, mouseY);
				mouseX = -1;
				mouseY = -1;
			}
		}

		leftPos = (width - imageWidth) / 2;

		DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(player);

		addRenderableWidget(new TabButton(leftPos, topPos - 28, TabButton.TabType.INVENTORY, this));
		addRenderableWidget(new TabButton(leftPos + 28, topPos - 26, TabButton.TabType.ABILITY, this));
		addRenderableWidget(new TabButton(leftPos + 57, topPos - 26, TabButton.TabType.GITHUB_REMINDER, this));
		addRenderableWidget(new TabButton(leftPos + 86, topPos - 26, TabButton.TabType.SKINS, this));

		ExtendedButton clawToggle = new ExtendedButton(leftPos + 27, topPos + 10, 11, 11, Component.empty(), button -> {
			clawsMenu = !clawsMenu;
			clearWidgets();
			init();

			PacketDistributor.sendToServer(new SyncDragonClawsMenuToggle.Data(clawsMenu));
			DragonStateProvider.getCap(player).ifPresent(cap -> cap.getClawToolData().setMenuOpen(clawsMenu));
		}){
			@Override
			public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				guiGraphics.blit(DRAGON_CLAW_BUTTON, getX(), getY(), 0, 0, 11, 11, 11, 11);
			}
		};
		clawToggle.setTooltip(Tooltip.create(Component.translatable("ds.gui.claws")));
		addRenderableWidget(clawToggle);

		// Growth icon in the claw menu
		ExtendedButton growthIcon = new ExtendedButton(leftPos - 58, topPos - 35, 32, 32, Component.empty(), btn -> {}){
			@Override
			public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				isGrowthIconHovered = isHovered();
			}
		};
		addRenderableWidget(growthIcon);
		clawMenuButtons.add(growthIcon);

		// Info button at the bottom of the claw menu
		HelpButton infoButton = new HelpButton(leftPos - 80 + 34, topPos + 112, 9, 9, "ds.skill.help.claws", 0, true);
		addRenderableWidget(infoButton);
		clawMenuButtons.add(infoButton);

		// Button to enable / disable the rendering of claws
		ExtendedButton clawRenderButton = new ExtendedButton(leftPos - 80 + 34, topPos + 140, 9, 9, Component.empty(), p_onPress_1_ -> {
			boolean claws = !handler.getClawToolData().shouldRenderClaws;

			handler.getClawToolData().shouldRenderClaws = claws;
			ConfigHandler.updateConfigValue("rendering/renderDragonClaws", handler.getClawToolData().shouldRenderClaws);
			PacketDistributor.sendToServer(new SyncDragonClawRender.Data(player.getId(), claws)
			);
		})
		{
			@Override
			public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
				DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(player);

				if (handler.getClawToolData().shouldRenderClaws) {
					guiGraphics.pose().pushPose();
					guiGraphics.pose().translate(0, 0, 100);
					guiGraphics.blit(DRAGON_CLAW_CHECKMARK, getX(), getY(), 0, 0, 9, 9, 9, 9);
					guiGraphics.pose().popPose();
				}
			}
		};
		clawRenderButton.setTooltip(Tooltip.create(Component.translatable("ds.gui.claws.rendering")));
		addRenderableWidget(clawRenderButton);
		clawMenuButtons.add(clawRenderButton);

		if(InventoryScreenHandler.inventoryToggle){
			ExtendedButton inventoryToggle = new ExtendedButton(leftPos + imageWidth - 28, height / 2 - 30 + 47, 20, 18, Component.empty(), p_onPress_1_ -> {
				Minecraft.getInstance().setScreen(new InventoryScreen(player));
				PacketDistributor.sendToServer(new RequestOpenInventory.Data());
			}){
				@Override
				public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
					float u = 0f;
					float v = isHoveredOrFocused() ? 20f : 0f;
					guiGraphics.blit(INVENTORY_TOGGLE_BUTTON, getX(), getY(), u, v, 20, 18, 256, 256);
				}
			};
			inventoryToggle.setTooltip(Tooltip.create(Component.translatable("ds.gui.toggle_inventory.vanilla")));
			addRenderableWidget(inventoryToggle);
		}
	}

	@Override
	protected void renderLabels(@NotNull final GuiGraphics guiGraphics, int p_230451_2_, int p_230451_3_) { /* Nothing to do */ }

	@Override
	protected void renderBg(@NotNull final GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
		RenderSystem.enableBlend();
		guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
		RenderSystem.disableBlend();

		if(clawsMenu){
			guiGraphics.blit(CLAWS_TEXTURE, leftPos - 80, topPos, 0, 0, 77, 170);
		}

		if(clawsMenu){
			if (textures == null || textures.isEmpty()) {
				initResources();
			}

			DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(player);

			double curSize = handler.getSize();
			float progress = 0;

			if(handler.getLevel() == DragonLevel.NEWBORN){
				progress = (float)((curSize - DragonLevel.NEWBORN.size) / (DragonLevel.YOUNG.size - DragonLevel.NEWBORN.size));
			}else if(handler.getLevel() == DragonLevel.YOUNG){
				progress = (float)((curSize - DragonLevel.YOUNG.size) / (DragonLevel.ADULT.size - DragonLevel.YOUNG.size));
			}else if(handler.getLevel() == DragonLevel.ADULT && handler.getSize() < 40){
				progress = (float)((curSize - DragonLevel.ADULT.size) / (40 - DragonLevel.ADULT.size));
			}else if(handler.getLevel() == DragonLevel.ADULT){
				progress = (float)((curSize - 40) / (ServerConfig.maxGrowthSize - 40));
			}

			int size = 34;
			int thickness = 5;
			int circleX = leftPos - 58;
			int circleY = topPos - 35;
			int sides = 6;

			int radius = size / 2;

			Color c = new Color(99, 99, 99);

			RenderSystem.setShaderColor(c.brighter().getRed() / 255.0f, c.brighter().getBlue() / 255.0f, c.brighter().getGreen() / 255.0f, 1.0f);
			RenderingUtils.drawSmoothCircle(guiGraphics, circleX + radius, circleY + radius, radius, sides, 1, 0);

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1.0f);
			RenderSystem.setShaderTexture(0, textures.get(createTextureKey(handler.getType(), "circle", "")));
			RenderingUtils.drawTexturedCircle(guiGraphics, circleX + radius, circleY + radius, radius, 0.5, 0.5, 0.5, sides, progress, -0.5);

			RenderSystem.setShaderColor(c.getRed() / 255.0f, c.getBlue() / 255.0f, c.getGreen() / 255.0f, 1.0f);
			RenderingUtils.drawSmoothCircle(guiGraphics, circleX + radius, circleY + radius, radius - thickness, sides, 1, 0);

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1.0f);
			guiGraphics.blit(textures.get(createTextureKey(handler.getType(), "growth", "_" + (handler.getLevel().ordinal() + 1))), circleX + 6, circleY + 6, 150, 0, 0, 20, 20, 20, 20);
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

		if (Keybind.DRAGON_INVENTORY.get().isActiveAndMatches(mouseKey)) {
			onClose();
			return true;
		}

		return super.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_);
	}

	@Override
	public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		clawMenuButtons.forEach(
				button -> button.visible = clawsMenu
		);

		super.render(guiGraphics, mouseX, mouseY, partialTick);

		DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(player);
		int scissorX0 =  leftPos;
		int scissorY0 = topPos;
		int scissorX1 = 140 + leftPos;
		// 0.55 is the smallest height mult we have (for the north body type) so we use that as the base
		float bodyHeightAddition = (float) ((((handler.getBody().getHeightMult() - 0.55) / 0.45) * -0.6) - 0.5);
		int scissorY1 = 140 + topPos;
		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(0, 0, 100);
		InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, scissorX0, scissorY0, scissorX1, scissorY1, 20, bodyHeightAddition, mouseX, mouseY, minecraft.player);
		guiGraphics.pose().popPose();

		renderTooltip(guiGraphics, mouseX, mouseY);

		if (isGrowthIconHovered) {
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
				double increment = (40 - DragonLevel.ADULT.size) / (DragonGrowthHandler.adultToAncient * 20.0) * ServerConfig.adultGrowthModifier;
				seconds = missing / increment / 20;
			}else if(handler.getLevel() == DragonLevel.ADULT){
				age += (int)(ServerConfig.maxGrowthSize - handler.getLevel().size);

				double missing = ServerConfig.maxGrowthSize - handler.getSize();
				double increment = (ServerConfig.maxGrowthSize - 40) / (DragonGrowthHandler.ancient * 20.0) * ServerConfig.maxGrowthModifier;
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

			HashSet<Item> newbornList = ConfigHandler.getResourceElements(Item.class, ServerConfig.growNewborn);
			HashSet<Item> youngList = ConfigHandler.getResourceElements(Item.class, ServerConfig.growYoung);
			HashSet<Item> adultList = ConfigHandler.getResourceElements(Item.class, ServerConfig.growAdult);

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

			List<Component> components = List.of(
					Component.translatable("ds.gui.growth_stage", handler.getLevel().getName()),
					Component.translatable("ds.gui.growth_age", age),
					Component.translatable("ds.gui.growth_help", result.toString())
			);

			guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, components, mouseX, mouseY);
		}
	}
}