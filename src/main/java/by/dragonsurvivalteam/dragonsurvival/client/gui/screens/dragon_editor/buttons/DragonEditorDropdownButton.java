package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown.DropdownEntry;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown.DropdownList;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.DropDownButton;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonEditorObject;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.subcapabilities.SkinCap;
import by.dragonsurvivalteam.dragonsurvival.mixins.AccessorScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

public class DragonEditorDropdownButton extends DropDownButton{
	private final DragonEditorScreen dragonEditorScreen;
	private final EnumSkinLayer layers;
	private final String dragonType;

	public DragonEditorDropdownButton(DragonEditorScreen dragonEditorScreen, int x, int y, int xSize, int ySize, String current, String[] values, EnumSkinLayer layers){
		super(x, y, xSize, ySize, current, values, selected -> {
			dragonEditorScreen.preset.skinAges.get(dragonEditorScreen.level).get().layerSettings.get(layers).get().selectedSkin = DragonEditorScreen.partToTechnical(selected);
			dragonEditorScreen.handler.getSkinData().compileSkin();
			dragonEditorScreen.update();
		});
		this.dragonEditorScreen = dragonEditorScreen;
		this.layers = layers;
		this.dragonType = dragonEditorScreen.handler.getTypeName().toLowerCase();
	}

	@Override
	public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float pPartialTicks){
		active = visible = dragonEditorScreen.showUi;
		super.renderWidget(guiGraphics, mouseX, mouseY, pPartialTicks);
		String currentValue = DragonEditorScreen.partToTranslation(dragonEditorScreen.preset.skinAges.get(dragonEditorScreen.level).get().layerSettings.get(layers).get().selectedSkin);

		if (!Objects.equals(currentValue, current)) {
			current = DragonEditorScreen.partToTranslation(currentValue);
			updateMessage();
		}

		List<String> valueList = DragonEditorHandler.getKeys(dragonEditorScreen.dragonType, dragonEditorScreen.dragonBody, layers);

		if(layers != EnumSkinLayer.BASE){
			valueList.addFirst(SkinCap.defaultSkinValue);
		}

		valueList = valueList.stream().map(DragonEditorScreen::partToTranslation).toList();

		values = valueList.toArray(new String[0]);
		active = !dragonEditorScreen.preset.skinAges.get(dragonEditorScreen.level).get().defaultSkin;
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
				DropdownEntry ent = createEntry(i, val, val);
				list.addEntry(ent);

				if(Objects.equals(val, current))
					center = ent;
			}

			if(center != null)
				list.centerScrollOn(center);

			boolean hasBorder = false;
			if(!screen.children().isEmpty()){
				screen.renderables.addFirst(list);
				screen.renderables.add(list);

				((AccessorScreen)screen).children().addFirst(list);
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
				public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick){
					active = visible = false;
					list.visible = DragonEditorDropdownButton.this.visible;

					if(finalHasBorder)
						RenderSystem.enableScissor(0, (int)(32 * Minecraft.getInstance().getWindow().getGuiScale()), Minecraft.getInstance().getWindow().getScreenWidth(), Minecraft.getInstance().getWindow().getScreenHeight() - (int)(32 * Minecraft.getInstance().getWindow().getGuiScale()) * 2);

					if(list.visible)
						list.render(graphics, mouseX, mouseY, partialTick);

					if(finalHasBorder)
						RenderSystem.disableScissor();
				}
			};
			((AccessorScreen)screen).children().add(renderButton);
			screen.renderables.add(renderButton);
		}else{
			LayerSettings settings = dragonEditorScreen.preset.skinAges.get(dragonEditorScreen.level).get().layerSettings.get(layers).get();
			DragonEditorObject.Texture text = DragonEditorHandler.getSkin(FakeClientPlayerUtils.getFakePlayer(0, DragonEditorScreen.handler), layers, settings.selectedSkin, dragonEditorScreen.dragonType);
			if (text != null && !settings.modifiedColor) {
				settings.hue = text.average_hue;
			}

			screen.children().removeIf(s -> s == list);
			screen.children().removeIf(s -> s == renderButton);
			screen.renderables.removeIf(s -> s == list);
			screen.renderables.removeIf(s -> s == renderButton);
		}

		toggled = !toggled;
		updateMessage();
	}

	public DropdownEntry createEntry(int pos, String val, String localeString){
		return new DragonDropdownValueEntry(this, pos, val, localeString, setter);
	}
}