package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.settings.ResetSettingsButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown.DropdownList;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.DSSlider;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ExtendedCheckbox;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorHandler;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.DragonEditorObject;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.LayerSettings;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ScreenUtils;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.awt.Color;
import java.util.List;
import java.util.function.Supplier;

public class HueSelectorComponent extends AbstractContainerEventHandler implements Widget{
	private final ExtendedButton hueReset;
	private final ExtendedButton saturationReset;
	private final ExtendedButton brightnessReset;
	private final Checkbox glowing;
	private final DragonEditorScreen screen;
	private final int x;
	private final int y;
	private final int xSize;
	private final int ySize;
	private final Supplier<LayerSettings> settings;
	public boolean visible;
	private DSSlider hueSlider;
	private DSSlider saturationSlider;
	private DSSlider brightnessSlider;

	public HueSelectorComponent(DragonEditorScreen screen, int x, int y, int xSize, int ySize, EnumSkinLayer layer){
		this.screen = screen;
		this.x = x;
		this.y = y;
		this.xSize = xSize;
		this.ySize = ySize;

		settings = () -> screen.preset.skinAges.get(screen.level).get().layerSettings.get(layer).get();
		LayerSettings set = settings.get();
		DragonEditorObject.Texture text = DragonEditorHandler.getSkin(FakeClientPlayerUtils.getFakePlayer(0, screen.handler), layer, set.selectedSkin, screen.handler.getType());

		glowing = new ExtendedCheckbox(x + 3, y, xSize - 5, 10, 10, Component.translatable("ds.gui.dragon_editor.glowing"), set.glowing, s -> {
			settings.get().glowing = s.selected();
			screen.handler.getSkinData().compileSkin();
		});

		float[] hsb = new float[]{set.hue, set.saturation, set.brightness};

		if(text == null){
			hsb[0] = 0.5f;
			hsb[1] = 0.5f;
			hsb[2] = 0.5f;
		} else if (!set.modifiedColor) {
			hsb[0] = text.average_hue;
			hsb[1] = 0.5f;
			hsb[2] = 0.5f;
		}

		hueSlider = new DSSlider(x + 3, y + 12, xSize - 26, 20, Component.empty(), Component.empty(), 0, 360, hsb[0] * 360.0f, true){
			@Override
			protected void applyValue(){
				super.applyValue();

				float value = (hueSlider.getValueInt()) / 360f;
				float value1 = (saturationSlider.getValueInt()) / 360f;
				float value2 = (brightnessSlider.getValueInt()) / 360f;

				settings.get().hue = value;
				settings.get().saturation = value1;
				settings.get().brightness = value2;
                settings.get().modifiedColor = text != null && (Float.compare(Math.round(value * 360), Math.round(text.average_hue * 360)) != 0 || !(Math.abs(value1 - 0.5f) < 0.05) || !(Math.abs(value2 - 0.5f) < 0.05));

				DragonEditorScreen.handler.getSkinData().compileSkin();
				screen.update();
			}

			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){
				if(visible){
					isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
					mStack.pushPose();
					RenderingUtils.renderPureColorSquare(mStack, x, y, width, height);
					mStack.popPose();

					renderBg(mStack, Minecraft.getInstance(), mouseX, mouseY);
				}
			}
		};


		hueReset = new ExtendedButton(x + 3 + xSize - 26, y + 12, 20, 20, Component.empty(), s -> {hueSlider.setValue(Math.round(text.average_hue * 360.0f));})
		{
			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){
				super.renderButton(mStack, mouseX, mouseY, partial);
				RenderSystem.setShaderTexture(0, ResetSettingsButton.texture);
				blit(mStack, x + 2, y + 2, 0, 0, 16, 16, 16, 16);
			}
		};

		saturationSlider = new DSSlider(x + 3, y + 22 + 12, xSize - 26, 20, Component.empty(), Component.empty(), 0, 360, hsb[1] * 360, true){
			@Override
			protected void applyValue(){
				super.applyValue();

				float value = (hueSlider.getValueInt()) / 360f;
				float value1 = (saturationSlider.getValueInt()) / 360f;
				float value2 = (brightnessSlider.getValueInt()) / 360f;

				settings.get().hue = value;
				settings.get().saturation = value1;
				settings.get().brightness = value2;

				settings.get().modifiedColor = text != null && (Float.compare(Math.round(value * 360), Math.round(text.average_hue * 360)) != 0 || !(Math.abs(value1 - 0.5f) < 0.05) || !(Math.abs(value2 - 0.5f) < 0.05));

				DragonEditorScreen.handler.getSkinData().compileSkin();
				screen.update();
			}

			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){
				if(visible){
					isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
					float value1 = (hueSlider.getValueInt() + 180) / 360f;

					mStack.pushPose();
					int col1 = Color.getHSBColor(value1, 0f, 1f).getRGB();
					int col2 = Color.getHSBColor(value1, 1f, 1f).getRGB();

					RenderingUtils.drawGradientRect(mStack.last().pose(), 200, x, y, x + width, y + height, new int[]{col2, col1, col1, col2});
					mStack.translate(0, 0, 200);
					renderBg(mStack, Minecraft.getInstance(), mouseX, mouseY);
					mStack.popPose();
				}
			}
		};

		saturationReset = new ExtendedButton(x + 3 + xSize - 26, y + 22 + 12, 20, 20, Component.empty(), button -> saturationSlider.setValue(180)) {
			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){
				mStack.pushPose();
				super.renderButton(mStack, mouseX, mouseY, partial);
				RenderSystem.setShaderTexture(0, ResetSettingsButton.texture);
				blit(mStack, x + 2, y + 2, 0, 0, 16, 16, 16, 16);
				mStack.popPose();
			}
		};

		brightnessReset = new ExtendedButton(x + 3 + xSize - 26, y + 44 + 12, 20, 20, Component.empty(), button -> brightnessSlider.setValue(180)) {
			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){
				mStack.pushPose();
				super.renderButton(mStack, mouseX, mouseY, partial);
				RenderSystem.setShaderTexture(0, ResetSettingsButton.texture);
				blit(mStack, x + 2, y + 2, 0, 0, 16, 16, 16, 16);
				mStack.popPose();
			}
		};

		brightnessSlider = new DSSlider(x + 3, y + 44 + 12, xSize - 26, 20, Component.empty(), Component.empty(), 0, 360, hsb[2] * 360, true){
			@Override
			protected void applyValue(){
				super.applyValue();

				float value = (hueSlider.getValueInt()) / 360f;
				float value1 = (saturationSlider.getValueInt()) / 360f;
				float value2 = (brightnessSlider.getValueInt()) / 360f;

				settings.get().hue = value;
				settings.get().saturation = value1;
				settings.get().brightness = value2;
				settings.get().modifiedColor = text != null && (Float.compare(Math.round(value * 360), Math.round(text.average_hue * 360)) != 0 || !(Math.abs(value1 - 0.5f) < 0.05) || !(Math.abs(value2 - 0.5f) < 0.05));

				DragonEditorScreen.handler.getSkinData().compileSkin();
				screen.update();
			}

			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){
				if(visible){
					isHovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
					float value1 = (hueSlider.getValueInt() + 180) / 360f;

					int col1 = Color.getHSBColor(value1, 1f, 0f).getRGB();
					int col2 = Color.getHSBColor(value1, 1f, 1f).getRGB();

					RenderingUtils.drawGradientRect(mStack.last().pose(), 200, x, y, x + width, y + height, new int[]{col2, col1, col1, col2});
					renderBg(mStack, Minecraft.getInstance(), mouseX, mouseY);
				}
			}
		};
	}

	@Override
	public boolean isMouseOver(double pMouseX, double pMouseY){
		return visible && pMouseY >= (double)y - 3 && pMouseY <= (double)y + ySize + 3 && pMouseX >= (double)x && pMouseX <= (double)x + xSize;
	}

	@Override
	public List<? extends GuiEventListener> children(){
		return ImmutableList.of(hueSlider, saturationSlider, brightnessSlider, hueReset, saturationReset, brightnessReset, glowing);
	}

	@Override
	public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
		ScreenUtils.blitWithBorder(pMatrixStack, DropdownList.BACKGROUND_TEXTURE, x, y - 3, 0, 0, xSize, ySize + 6, 32, 32, 10, 10, 10, 10, (float)500);

		glowing.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

		hueReset.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		saturationReset.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		brightnessReset.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

		hueSlider.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		saturationSlider.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		brightnessSlider.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}
}