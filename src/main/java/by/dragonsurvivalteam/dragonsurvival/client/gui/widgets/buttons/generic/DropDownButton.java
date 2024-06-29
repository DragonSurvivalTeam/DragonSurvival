package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown.DropdownEntry;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown.DropdownList;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown.DropdownValueEntry;
import by.dragonsurvivalteam.dragonsurvival.mixins.AccessorScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

public class DropDownButton extends ExtendedButton {
	public static final int maxItems = 4;
	public String[] values;
	public String current;
	public Consumer<String> setter;
	public List<FormattedCharSequence> tooltip;
	public boolean toggled;
	public DropdownList list;
	public AbstractWidget renderButton;
	public Component message;

	public DropDownButton(int x, int y, int xSize, int ySize, String current, String[] values, Consumer<String> setter){
		super(x, y, xSize, ySize, Component.empty(), pButton -> {});
		this.values = values;
		this.setter = setter;
		this.current = current;
		updateMessage();
	}

	public void updateMessage(){
		if(current != null)
			message = Component.translatable(DragonEditorScreen.partToTranslation(current));
	}

	@Override
	public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		// TODO :: Is this safe?
		if(toggled && (!visible || !isHoveredOrFocused() && !list.isMouseOver(mouseX, mouseY))){
			toggled = false;
			Screen screen = Minecraft.getInstance().screen;
			screen.children().removeIf(s -> s == list);
			screen.children().removeIf(s -> s == renderButton);
			screen.renderables.removeIf(s -> s == list);
			screen.renderables.removeIf(s -> s == renderButton);
		}


		if(toggled && list != null){
			Screen screen = Minecraft.getInstance().screen;
			int offset = screen.height - (getY() + height + 80);
			list.reposition(getX(), getY() + height + Math.min(offset, 0), width, (int)(Math.max(1, Math.min(values.length, maxItems)) * (height * 1.5f)));
		}

		Minecraft mc = Minecraft.getInstance();
		int k = !isActive() ? 0 : isHoveredOrFocused() ? 2 : 1;
		guiGraphics.blitSprite(SPRITES.get(this.active, this.isHoveredOrFocused()), getX(), getY(), width, height);
		guiGraphics.renderOutline(getX(), getY(), width, height, k);

		Component buttonText = getMessage();
		int strWidth = mc.font.width(buttonText);
		int ellipsisWidth = mc.font.width("...");

		if (strWidth > width - 6 && strWidth > ellipsisWidth) {
			buttonText = Component.empty().append(mc.font.substrByWidth(buttonText, width - 6 - ellipsisWidth).getString() + "...");
		}

		guiGraphics.drawCenteredString(mc.font, buttonText, getX() + width / 2, getY() + (height - 8) / 2, getFGColor());
	}


	@Override
	public @NotNull Component getMessage(){
		return message;
	}

	@Override
	public void onClick(double pMouseX, double pMouseY){
		List<? extends GuiEventListener> list = Minecraft.getInstance().screen.children().stream().filter(s -> s.isMouseOver(pMouseX, pMouseY)).toList();

		if(list.size() == 1)
			onPress();
	}
	@Override
	public void onPress(){
		Screen screen = Minecraft.getInstance().screen;

		if(!toggled){
			int offset = screen.height - (getY() + height + 80);
			list = new DropdownList(getX(), getY() + height + Math.min(offset, 0), width, (int)(Math.max(1, Math.min(values.length, maxItems)) * (height * 1.5f)), 16);
			DropdownEntry center = null;

			for(int i = 0; i < values.length; i++){
				String val = values[i];
				DropdownEntry ent = createEntry(i, val);
				list.addEntry(ent);

				if(Objects.equals(val, current))
					center = ent;
			}

			if(center != null)
				list.centerScrollOn(center);

			boolean hasBorder = false;
			if(!screen.children().isEmpty()){
				screen.renderables.add(0, list);
				screen.renderables.add(list);
				((AccessorScreen)screen).children().add(0, list);
				((AccessorScreen)screen).children().add(list);

				for(GuiEventListener child : screen.children())
					if(child instanceof ContainerObjectSelectionList){
						if(((ContainerObjectSelectionList<?>)child).visible){
							hasBorder = true;
							break;
						}
					}
			}else{
				((AccessorScreen)screen).children().add(list);
				screen.renderables.add(list);
			}

			boolean finalHasBorder = hasBorder;
			renderButton = new ExtendedButton(0, 0, 0, 0, Component.empty(), pButton -> {}){
				@Override
				public void renderWidget(@NotNull final GuiGraphics guiGraphics, int p_230430_2_, int p_230430_3_, float p_230430_4_){
					active = visible = false;
					list.visible = DropDownButton.this.visible;

					if(finalHasBorder)
						RenderSystem.enableScissor(0, (int)(32 * Minecraft.getInstance().getWindow().getGuiScale()), Minecraft.getInstance().getWindow().getScreenWidth(), Minecraft.getInstance().getWindow().getScreenHeight() - (int)(32 * Minecraft.getInstance().getWindow().getGuiScale()) * 2);

					if(list.visible)
						list.render(guiGraphics, p_230430_2_, p_230430_3_, p_230430_4_);

					if(finalHasBorder)
						RenderSystem.disableScissor();
				}
			};
			((AccessorScreen)screen).children().add(renderButton);
			screen.renderables.add(renderButton);
		}else{
			screen.children().removeIf(s -> s == list);
			screen.children().removeIf(s -> s == renderButton);
			screen.renderables.removeIf(s -> s == list);
			screen.renderables.removeIf(s -> s == renderButton);
		}

		toggled = !toggled;
		updateMessage();
	}

	public DropdownEntry createEntry(int pos, String val){
		return new DropdownValueEntry(this, pos, val, setter);
	}

	@Override
	public Tooltip getTooltip(){
		if (tooltip == null) {
			return Tooltip.create(Component.empty());
		}

		return Tooltip.create(Component.literal(tooltip.toString()));
	}
}