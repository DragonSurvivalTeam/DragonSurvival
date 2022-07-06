package by.dragonsurvivalteam.dragonsurvival.client.handlers;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.AbilityScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.utils.TooltipRender;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.SkillProgressButton;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.common.capability.provider.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.misc.DragonType;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.ScreenEvent.DrawScreenEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.Color;
import java.util.List;

@Mod.EventBusSubscriber( Dist.CLIENT )
public class ToolTipHandler{
	private static final ResourceLocation tooltip_1 = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/magic_tips_0.png");
	private static final ResourceLocation tooltip_2 = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/magic_tips_1.png");

	@ConfigOption( side = Dist.CLIENT, category = "tooltips", key = "tooltipChanges", comment = "Should the mod be allowed ot change the color and appearance of tooltips?" )
	public static Boolean tooltipChanges = true;

	@ConfigOption( side = Dist.CLIENT, category = "tooltips", key = "dragonFoodTooltips", comment = "Should dragon foods have their tooltip color changed to show which type of dragon can consume it?" )
	public static Boolean dragonFoodTooltips = true;

	@ConfigOption( side = Dist.CLIENT, category = "tooltips", key = "helpTooltips", comment = "Should the effect of the help tooltips be enabled?" )
	public static Boolean helpTooltips = true;

	@ConfigOption( side = Dist.CLIENT, category = "tooltips", key = "alwaysShowHelpTooltip", comment = "Always show the help tooltip border" )
	public static Boolean alwaysShowHelpTooltip = false;


	private static boolean blink = false;
	private static int tick = 0;


	@SubscribeEvent
	public static void checkIfDragonFood(ItemTooltipEvent tooltipEvent){
		if(tooltipEvent.getPlayer() != null){
			Item item = tooltipEvent.getItemStack().getItem();
			List<Component> toolTip = tooltipEvent.getToolTip();
			if(DragonFoodHandler.getSafeEdibleFoods(DragonType.CAVE).contains(item)){
				toolTip.add(new TranslatableComponent("ds.cave.dragon.food"));
			}
			if(DragonFoodHandler.getSafeEdibleFoods(DragonType.FOREST).contains(item)){
				toolTip.add(new TranslatableComponent("ds.forest.dragon.food"));
			}
			if(DragonFoodHandler.getSafeEdibleFoods(DragonType.SEA).contains(item)){
				toolTip.add(new TranslatableComponent("ds.sea.dragon.food"));
			}
		}
	}


	@SubscribeEvent
	public static void itemDescriptions(ItemTooltipEvent event){
		if(event.getPlayer() != null){
			Item item = event.getItemStack().getItem();
			List<Component> toolTip = event.getToolTip();

			if(item == DSBlocks.fireDragonBeacon.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.passiveFireBeacon"));
			}
			if(item == DSBlocks.magicDragonBeacon.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.passiveMagicBeacon"));
			}
			if(item == DSBlocks.peaceDragonBeacon.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.passivePeaceBeacon"));
			}
			if(item == DSBlocks.caveDoor.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.caveDoor"));
			}
			if(item == DSBlocks.forestDoor.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.forestDoor"));
			}
			if(item == DSBlocks.seaDoor.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.seaDoor"));
			}
			if(item == DSBlocks.legacyDoor.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.legacyDoor"));
			}
			if(item == DSBlocks.helmet1.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.grayHelmet"));
			}
			if(item == DSBlocks.helmet2.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.goldHelmet"));
			}
			if(item == DSBlocks.helmet3.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.blackHelmet"));
			}
			if(item == DSBlocks.dragonBeacon.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.dragonBeacon"));
			}
			if(item == DSBlocks.dragonMemoryBlock.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.dragonMemoryBlock"));
			}
			if(item == DSBlocks.seaSourceOfMagic.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.sea_source_of_magic"));
			}
			if(item == DSBlocks.forestSourceOfMagic.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.forest_source_of_magic"));
			}
			if(item == DSBlocks.caveSourceOfMagic.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.cave_source_of_magic"));
			}
			if(item == DSBlocks.dragonPressurePlate.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.dragon_pressure_plate"));
			}
			if(item == DSBlocks.humanPressurePlate.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.human_pressure_plate"));
			}
			if(item == DSBlocks.seaPressurePlate.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.sea_dragon_pressure_plate"));
			}
			if(item == DSBlocks.forestPressurePlate.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.forest_dragon_pressure_plate"));
			}
			if(item == DSBlocks.cavePressurePlate.asItem()){
				toolTip.add(new TranslatableComponent("ds.description.cave_dragon_pressure_plate"));
			}
		}
	}

	@SubscribeEvent
	public static void postScreenRender(DrawScreenEvent.Post event){
		if(Minecraft.getInstance().screen != null && Minecraft.getInstance().screen.children != null){
			if(Minecraft.getInstance().screen instanceof TooltipRender){
				for(GuiEventListener btn : Minecraft.getInstance().screen.children){
					if(btn instanceof AbstractWidget){
						if(((AbstractWidget)btn).isHoveredOrFocused()){
							((AbstractWidget)btn).renderToolTip(event.getPoseStack(), event.getMouseX(), event.getMouseY());
						}
					}
				}

			}else{
				for(GuiEventListener btn : Minecraft.getInstance().screen.children){
					if(btn instanceof AbstractWidget){
						if(btn instanceof TooltipRender && ((AbstractWidget)btn).isHoveredOrFocused()){
							((AbstractWidget)btn).renderToolTip(event.getPoseStack(), event.getMouseX(), event.getMouseY());
						}
					}
				}
			}
		}
	}

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

		int width = event.getComponents().stream().map((s) -> s.getWidth(Minecraft.getInstance().font)).max(Integer::compareTo).orElse(0);
		int height = event.getComponents().stream().map(ClientTooltipComponent::getHeight).reduce(Integer::sum).orElse(0);

		int x = event.getX() + 12;
		int y = event.getY() - 12;

		if(x + width > screen.width){
			x -= 28 + width;
		}

		if(y + height + 6 > screen.height){
			y = screen.height - height - 6;
		}

		PoseStack matrix = event.getPoseStack();
		RenderSystem.setShaderTexture(0, blink ? tooltip_2 : tooltip_1);

		int texWidth = 128;
		int texHeight = 128;

		matrix.pushPose();
		//RenderSystem.enableBlend();
		matrix.translate(0, 0, 710.0);

		Gui.blit(matrix, x - 8 - 6, y - 8 - 6, 1, 1 % texHeight, 16, 16, texWidth, texHeight);
		Gui.blit(matrix, x + width - 8 + 6, y - 8 - 6, texWidth - 16 - 1, 1 % texHeight, 16, 16, texWidth, texHeight);

		Gui.blit(matrix, x - 8 - 6, y + height - 8 + 6, 1, 1 % texHeight + 16, 16, 16, texWidth, texHeight);
		Gui.blit(matrix, x + width - 8 + 6, y + height - 8 + 6, texWidth - 16 - 1, 1 % texHeight + 16, 16, 16, texWidth, texHeight);

		Gui.blit(matrix, x + (width / 2) - 47, y - 16, 16 + 2 * texWidth + 1, 1 % texHeight, 94, 16, texWidth, texHeight);
		Gui.blit(matrix, x + (width / 2) - 47, y + height, 16 + 2 * texWidth + 1, 1 % texHeight + 16, 94, 16, texWidth, texHeight);

		//RenderSystem.disableBlend();
		matrix.popPose();
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

		boolean isSeaFood = dragonFoodTooltips && !stack.isEmpty() && DragonFoodHandler.getSafeEdibleFoods(DragonType.SEA).contains(stack.getItem());
		boolean isForestFood = dragonFoodTooltips && !stack.isEmpty() && DragonFoodHandler.getSafeEdibleFoods(DragonType.FOREST).contains(stack.getItem());
		boolean isCaveFood = dragonFoodTooltips && !stack.isEmpty() && DragonFoodHandler.getSafeEdibleFoods(DragonType.CAVE).contains(stack.getItem());
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
			DragonType type = DragonStateProvider.getCap(Minecraft.getInstance().player).map((cap) -> cap.getType()).get();
			Color topColor = null;
			Color bottomColor = null;

			if(type == DragonType.SEA && button || isSeaFood){
				topColor = new Color(93, 201, 255);
				bottomColor = new Color(49, 109, 144);
			}else if(type == DragonType.FOREST && button || isForestFood){
				topColor = new Color(0, 255, 148);
				bottomColor = new Color(4, 130, 82);
			}else if(type == DragonType.CAVE && button || isCaveFood){
				topColor = new Color(255, 118, 133);
				bottomColor = new Color(139, 66, 74);
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