package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.AbilityScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.SkillProgressButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.List;
import java.util.Objects;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ToolTipHandler{
	private static final ResourceLocation tooltip_1 = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/magic_tips_0.png");
	private static final ResourceLocation tooltip_2 = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/magic_tips_1.png");

	@ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "tooltipChanges", comment = "Should the mod be allowed ot change the color and appearance of tooltips?")
	public static Boolean tooltipChanges = true;

	@ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "hideUnsafeFood", comment = "Should the tooltip be hidden for unsafe (negative effects) food?")
	public static Boolean hideUnsafeFood = true;

	@ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "dragonFoodTooltips", comment = "Should dragon foods have their tooltip color changed to show which type of dragon can consume it?")
	public static Boolean dragonFoodTooltips = true;

	@ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "helpTooltips", comment = "Should the effect of the help tooltips be enabled?")
	public static Boolean helpTooltips = true;

	@ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "alwaysShowHelpTooltip", comment = "Always show the help tooltip border")
	public static Boolean alwaysShowHelpTooltip = false;

	@ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "hideAppleskinTooltip", comment = "Hide the AppleSkin tooltip if you're a dragon. The tooltip will only show correct food values for humans.")
	public static Boolean hideAppleskinTooltip = true;

	private final static ResourceLocation ICONS = new ResourceLocation(DragonSurvivalMod.MODID, "food_tooltip_icon_font");


	private static boolean blink = false;
	private static int tick = 0;


	@SubscribeEvent
	public static void checkIfDragonFood(ItemTooltipEvent tooltipEvent){
		if(tooltipEvent.getEntity() != null){
			Item item = tooltipEvent.getItemStack().getItem();
			List<Component> toolTip = tooltipEvent.getToolTip();

			if(DragonFoodHandler.getEdibleFoods(DragonTypes.CAVE).contains(item)){
				toolTip.add(createFoodTooltip(item, DragonTypes.CAVE, ChatFormatting.RED, "\uEA02", "\uEA05"));
			}

			if(DragonFoodHandler.getEdibleFoods(DragonTypes.FOREST).contains(item)){
				toolTip.add(createFoodTooltip(item, DragonTypes.FOREST, ChatFormatting.GREEN, "\uEA01", "\uEA04"));
			}

			if(DragonFoodHandler.getEdibleFoods(DragonTypes.SEA).contains(item)){
				toolTip.add(createFoodTooltip(item, DragonTypes.SEA, ChatFormatting.DARK_AQUA, "\uEA03", "\uEA06"));
			}
		}
	}

	private static MutableComponent createFoodTooltip(final Item item, final AbstractDragonType type, final ChatFormatting color, final String nutritionIcon, final String saturationIcon) {
		MutableComponent component = Component.translatable("ds." + type.getTypeName() + ".dragon.food");
		FoodProperties properties = DragonFoodHandler.getFoodProperties(item.getDefaultInstance(), type);

		String nutrition = "0";
		String saturation = "0";

		if (properties != null) {
			float nutritionValue = properties.getNutrition();
			float saturationValue = properties.getNutrition() * properties.getSaturationModifier() * 2f;

			// 1 Icon = 2 points (e.g. 10 nutrition icons for a maximum food level of 20)
			nutrition = String.format("%.1f", nutritionValue / 2);
			saturation = String.format("%.1f", saturationValue / 2);
		}

		MutableComponent nutritionIconComponent = Component.literal(nutritionIcon).withStyle(Style.EMPTY.withFont(ICONS));
		MutableComponent nutritionComponent = Component.literal(": " + nutrition + " ").withStyle(color);

		MutableComponent saturationIconComponent = Component.literal(saturationIcon).withStyle(Style.EMPTY.withFont(ICONS));
		MutableComponent saturationComponent = Component.literal(" / " + saturation + " ").withStyle(color);

		return component.append(nutritionComponent).append(nutritionIconComponent).append(saturationComponent).append(saturationIconComponent);
	}

	@SubscribeEvent
	public static void itemDescriptions(ItemTooltipEvent event){
		if(event.getEntity() != null){
			Item item = event.getItemStack().getItem();
			List<Component> toolTip = event.getToolTip();

			if(item == DSBlocks.fireDragonBeacon.asItem()){
				toolTip.add(Component.translatable("ds.description.passiveFireBeacon"));
			}
			if(item == DSBlocks.magicDragonBeacon.asItem()){
				toolTip.add(Component.translatable("ds.description.passiveMagicBeacon"));
			}
			if(item == DSBlocks.peaceDragonBeacon.asItem()){
				toolTip.add(Component.translatable("ds.description.passivePeaceBeacon"));
			}
			if(item == DSBlocks.caveDoor.asItem()){
				toolTip.add(Component.translatable("ds.description.caveDoor"));
			}
			if(item == DSBlocks.forestDoor.asItem()){
				toolTip.add(Component.translatable("ds.description.forestDoor"));
			}
			if(item == DSBlocks.seaDoor.asItem()){
				toolTip.add(Component.translatable("ds.description.seaDoor"));
			}
			if(item == DSBlocks.legacyDoor.asItem()){
				toolTip.add(Component.translatable("ds.description.legacyDoor"));
			}
			if(item == DSBlocks.helmet1.asItem()){
				toolTip.add(Component.translatable("ds.description.grayHelmet"));
			}
			if(item == DSBlocks.helmet2.asItem()){
				toolTip.add(Component.translatable("ds.description.goldHelmet"));
			}
			if(item == DSBlocks.helmet3.asItem()){
				toolTip.add(Component.translatable("ds.description.blackHelmet"));
			}
			if(item == DSBlocks.dragonBeacon.asItem()){
				toolTip.add(Component.translatable("ds.description.dragonBeacon"));
			}
			if(item == DSBlocks.dragonMemoryBlock.asItem()){
				toolTip.add(Component.translatable("ds.description.dragonMemoryBlock"));
			}
			if(item == DSBlocks.seaSourceOfMagic.asItem()){
				toolTip.add(Component.translatable("ds.description.sea_source_of_magic"));
			}
			if(item == DSBlocks.forestSourceOfMagic.asItem()){
				toolTip.add(Component.translatable("ds.description.forest_source_of_magic"));
			}
			if(item == DSBlocks.caveSourceOfMagic.asItem()){
				toolTip.add(Component.translatable("ds.description.cave_source_of_magic"));
			}
			if(item == DSBlocks.dragonPressurePlate.asItem()){
				toolTip.add(Component.translatable("ds.description.dragon_pressure_plate"));
			}
			if(item == DSBlocks.humanPressurePlate.asItem()){
				toolTip.add(Component.translatable("ds.description.human_pressure_plate"));
			}
			if(item == DSBlocks.seaPressurePlate.asItem()){
				toolTip.add(Component.translatable("ds.description.sea_dragon_pressure_plate"));
			}
			if(item == DSBlocks.forestPressurePlate.asItem()){
				toolTip.add(Component.translatable("ds.description.forest_dragon_pressure_plate"));
			}
			if(item == DSBlocks.cavePressurePlate.asItem()){
				toolTip.add(Component.translatable("ds.description.cave_dragon_pressure_plate"));
			}
		}
	}

	@SubscribeEvent
	public static void postScreenRender(ScreenEvent.Render.Post event) {
		if (Minecraft.getInstance().screen != null && Minecraft.getInstance().screen.children != null) {
			for (GuiEventListener button : Minecraft.getInstance().screen.children) {
				if (button instanceof HelpButton helpButton) {
					if (helpButton.isHoveredOrFocused()) {
						helpButton.renderTooltip(event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
					}
				}
			}
		}
	}

	/** Renders the special border elements for the info tooltips */
	@SubscribeEvent
	public static void onTooltipEvent(RenderTooltipEvent.Pre event){
		boolean render = isHelpText();

		if(!render){
			return;
		}

		if(!blink){
			if(tick >= Functions.secondsToTicks(30)){
				blink = true;
				tick = 0;
			}
		}else{
			if(tick >= Functions.secondsToTicks(5)){
				blink = false;
				tick = 0;
			}
		}

		tick++;

		Screen screen = Minecraft.getInstance().screen;

		int width = event.getComponents().stream().map(s -> s.getWidth(Minecraft.getInstance().font)).max(Integer::compareTo).orElse(0);
		int height = event.getComponents().stream().map(ClientTooltipComponent::getHeight).reduce(Integer::sum).orElse(0);

		int x = event.getX() + 12;
		int y = event.getY() - 12;

		if(x + width > screen.width){
			x -= 28 + width;
		}

		if(y + height + 6 > screen.height){
			y = screen.height - height - 6;
		}

		int texWidth = 128;
		int texHeight = 128;

		event.getGraphics().blit(blink ? tooltip_2 : tooltip_1, x - 8 - 6, y - 8 - 6, 400, 1, 1 % texHeight, 16, 16, texWidth, texHeight);
		event.getGraphics().blit(blink ? tooltip_2 : tooltip_1, x + width - 8 + 6, y - 8 - 6, 400,texWidth - 16 - 1, 1 % texHeight, 16, 16, texWidth, texHeight);

		event.getGraphics().blit(blink ? tooltip_2 : tooltip_1, x - 8 - 6, y + height - 8 + 6, 400,1, 1 % texHeight + 16, 16, 16, texWidth, texHeight);
		event.getGraphics().blit(blink ? tooltip_2 : tooltip_1, x + width - 8 + 6, y + height - 8 + 6, 400,texWidth - 16 - 1, 1 % texHeight + 16, 16, 16, texWidth, texHeight);

		event.getGraphics().blit(blink ? tooltip_2 : tooltip_1, x + width / 2 - 47, y - 16, 400,16 + 2 * texWidth + 1, 1 % texHeight, 94, 16, texWidth, texHeight);
		event.getGraphics().blit(blink ? tooltip_2 : tooltip_1, x + width / 2 - 47, y + height, 400,16 + 2 * texWidth + 1, 1 % texHeight + 16, 94, 16, texWidth, texHeight);
	}

	private static boolean isHelpText(){
		if(!tooltipChanges || !helpTooltips){
			return false;
		}
		if(Minecraft.getInstance().level == null){
			return false;
		}
		if(alwaysShowHelpTooltip){
			return true;
		}
		if(Minecraft.getInstance().screen == null){
			return false;
		}

		for(GuiEventListener btn : Minecraft.getInstance().screen.children){
			if(btn instanceof HelpButton && ((HelpButton)btn).isHoveredOrFocused()){
				return true;
			}
		}

		return false;
	}

	@SubscribeEvent
	public static void onTooltipColorEvent(RenderTooltipEvent.Color event){
		if(!tooltipChanges){
			return;
		}
		boolean render = isHelpText();
		boolean screen = Minecraft.getInstance().screen instanceof AbilityScreen;

		ItemStack stack = event.getItemStack();

		boolean isSeaFood = dragonFoodTooltips && !stack.isEmpty() && DragonFoodHandler.getEdibleFoods(DragonTypes.SEA).contains(stack.getItem());
		boolean isForestFood = dragonFoodTooltips && !stack.isEmpty() && DragonFoodHandler.getEdibleFoods(DragonTypes.FOREST).contains(stack.getItem());
		boolean isCaveFood = dragonFoodTooltips && !stack.isEmpty() && DragonFoodHandler.getEdibleFoods(DragonTypes.CAVE).contains(stack.getItem());
		int foodCount = (isSeaFood ? 1 : 0) + (isForestFood ? 1 : 0) + (isCaveFood ? 1 : 0);

		boolean isFood = foodCount == 1;

		boolean button = false;

		if(screen){
			for(GuiEventListener widget : ((AbilityScreen)Minecraft.getInstance().screen).widgetList()){
				if(widget instanceof SkillProgressButton && ((SkillProgressButton)widget).isHoveredOrFocused()){
					button = true;
					break;
				}
			}
		}

		if(render){
			int top = new Color(154, 132, 154).getRGB();
			int bottom = new Color(89, 68, 89).getRGB();

			event.setBorderStart(top);
			event.setBorderEnd(bottom);
		}else if(screen || isFood){
			AbstractDragonType type = DragonUtils.getDragonType(Minecraft.getInstance().player);
			Color topColor = null;
			Color bottomColor = null;

			if(type != null){
				if(Objects.equals(type, DragonTypes.SEA) && button || isSeaFood){
					topColor = new Color(93, 201, 255);
					bottomColor = new Color(49, 109, 144);
				}else if(Objects.equals(type, DragonTypes.FOREST) && button || isForestFood){
					topColor = new Color(0, 255, 148);
					bottomColor = new Color(4, 130, 82);
				}else if(Objects.equals(type, DragonTypes.CAVE) && button || isCaveFood){
					topColor = new Color(255, 118, 133);
					bottomColor = new Color(139, 66, 74);
				}
			}

			if(topColor != null){
				event.setBorderStart(topColor.getRGB());
			}

			if(bottomColor != null){
				event.setBorderEnd(bottomColor.getRGB());
			}
		}
	}
}