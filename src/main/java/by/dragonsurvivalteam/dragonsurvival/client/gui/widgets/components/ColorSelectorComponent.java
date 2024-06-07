package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown.DropdownList;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ColorPickerButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ExtendedCheckbox;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonEditorObject.Texture;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import com.google.common.collect.ImmutableList;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

public class ColorSelectorComponent extends AbstractContainerEventHandler implements Renderable {
	private final ExtendedButton colorPicker;
	//private final Checkbox glowing;
	private final DragonEditorScreen screen;
	private final int x;
	private final int y;
	private final int xSize;
	private final int ySize;
	private final Supplier<LayerSettings> settings;
	public boolean visible;

	public ColorSelectorComponent(DragonEditorScreen screen, int x, int y, int xSize, int ySize, EnumSkinLayer layer){
		this.screen = screen;
		this.x = x;
		this.y = y;
		this.xSize = xSize;
		this.ySize = ySize;

		settings = () -> screen.preset.skinAges.get(screen.level).get().layerSettings.get(layer).get();

		LayerSettings set = settings.get();
		Texture text = DragonEditorHandler.getSkin(FakeClientPlayerUtils.getFakePlayer(0, screen.handler), layer, set.selectedSkin, screen.handler.getType());

		// FIXME
		/*glowing = new ExtendedCheckbox(x + 3, y, xSize - 5, 10, 10, Component.translatable("ds.gui.dragon_editor.glowing"), set.glowing, s -> {
			settings.get().glowing = s.selected();
			screen.handler.getSkinData().compileSkin();
		});*/

		Color defaultC = Color.decode(text.defaultColor);

		if(set.modifiedColor){
			defaultC = Color.getHSBColor(set.hue, set.saturation, set.brightness);
		}

		colorPicker = new ColorPickerButton(x + 3, y + 11, xSize - 5, ySize - 11, defaultC, c -> {
			float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);

			settings.get().hue = hsb[0];
			settings.get().saturation = hsb[1];
			settings.get().brightness = hsb[2];
			settings.get().modifiedColor = true;

			screen.handler.getSkinData().compileSkin();
			screen.update();
		});
	}

	@Override
	public boolean isMouseOver(double pMouseX, double pMouseY){
		return visible && pMouseY >= (double)y - 3 && pMouseY <= (double)y + ySize + 3 && pMouseX >= (double)x && pMouseX <= (double)x + xSize;
	}

	@Override
	public List<? extends GuiEventListener> children(){
		return ImmutableList.of(colorPicker); // FIXME /*glowing)*/;
	}

	@Override
	public void render(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks){
		guiGraphics.pose().pushPose();
		// Render pop-up menu content above the other elements
		guiGraphics.pose().translate(0, 0, 150);
		guiGraphics.blitWithBorder(DropdownList.BACKGROUND_TEXTURE, x, y - 3, 0, 0, xSize, ySize + 6, 32, 32, 10, 10, 10, 10);
		colorPicker.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
		// FIXME
		//glowing.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
		guiGraphics.pose().popPose();
	}
}