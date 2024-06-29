package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown.DropdownValueEntry;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

public class DragonDropdownValueEntry extends DropdownValueEntry
{
	private final int num;
	private final String value;
	private final String localeString;
	private final Consumer<String> setter;
	private final DragonEditorDropdownButton source;
	private final Component message;
	private ExtendedButton button;

	public DragonDropdownValueEntry(DragonEditorDropdownButton source, int num, String value, String localeString, Consumer<String> setter) {
		super(source, num, value, setter);
		this.num = num;
		this.value = value;
		this.setter = setter;
		this.source = source;
		this.localeString = localeString;
		message = Component.translatable(DragonEditorScreen.partToTranslation(value));
	}

	@Override
	public List<? extends GuiEventListener> children(){
		return ImmutableList.of(button);
	}

	@Override
	public void render(GuiGraphics guiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks){
		if(button == null){
			Component displayString = Component.literal(localeString);
			button = new ExtendedButton(pLeft + 3, 0, pWidth - 12, pHeight + 1, displayString, pButton -> {}){
					@Override
					public Component getMessage(){
						return message;
					}

					@Override
					public void onPress(){
						source.current = value;
						setter.accept(value);
						source.onPress();
					}
			};
		} else {
			button.setY(pTop);
			button.visible = source.visible;
			button.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
		}
	}
}