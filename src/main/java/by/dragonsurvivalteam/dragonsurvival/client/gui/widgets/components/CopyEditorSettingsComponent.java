package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.client.gui.DragonAltarGUI;
import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.buttons.CopySettingsButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.dropdown.DropdownList;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ExtendedCheckbox;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset.SkinAgeGroup;
import by.dragonsurvivalteam.dragonsurvival.client.util.TooltipRendering;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.ScreenUtils;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import net.minecraftforge.common.util.Lazy;

import java.util.List;

public class CopyEditorSettingsComponent extends AbstractContainerEventHandler implements Widget{
	private final ExtendedButton confirm;
	private final ExtendedButton cancel;
	private final Checkbox newborn;
	private final Checkbox young;
	private final Checkbox adult;
	private final CopySettingsButton btn;
	private final DragonEditorScreen screen;
	private final int x;
	private final int y;
	private final int xSize;
	private final int ySize;
	public boolean visible;

	public CopyEditorSettingsComponent(DragonEditorScreen screen, CopySettingsButton btn, int x, int y, int xSize, int ySize){
		this.screen = screen;
		this.x = x;
		this.y = y;
		this.xSize = xSize;
		this.ySize = ySize;
		this.btn = btn;

		confirm = new ExtendedButton(x + xSize / 2 - 18, y + ySize - 15, 15, 15, Component.empty(), null){
			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){
				setMessage(Component.empty());
				super.renderButton(mStack, mouseX, mouseY, partial);
				RenderSystem.setShaderTexture(0, DragonAltarGUI.CONFIRM_BUTTON);
				blit(mStack, x + 1, y, 0, 0, 15, 15, 15, 15);

				if(isHovered){
					TooltipRendering.drawHoveringText(mStack, Component.translatable("ds.gui.dragon_editor.tooltip.done"), mouseX, mouseY);
				}
			}

			@Override
			public void onPress(){
				SkinAgeGroup preset = screen.preset.skinAges.getOrDefault(screen.level, Lazy.of(()->new SkinAgeGroup(screen.level))).get();

				screen.doAction();

				if(newborn.active && newborn.selected()){
					screen.preset.skinAges.put(DragonLevel.NEWBORN, Lazy.of(()->{
						SkinAgeGroup ageGroup = new SkinAgeGroup(DragonLevel.NEWBORN);
						ageGroup.readNBT(preset.writeNBT());
						return ageGroup;
					}));
				}

				if(young.active && young.selected()){
					screen.preset.skinAges.put(DragonLevel.YOUNG, Lazy.of(()->{
						SkinAgeGroup ageGroup = new SkinAgeGroup(DragonLevel.YOUNG);
						ageGroup.readNBT(preset.writeNBT());
						return ageGroup;
					}));
				}

				if(adult.active && adult.selected()){
					screen.preset.skinAges.put(DragonLevel.ADULT, Lazy.of(()->{
						SkinAgeGroup ageGroup = new SkinAgeGroup(DragonLevel.ADULT);
						ageGroup.readNBT(preset.writeNBT());
						return ageGroup;
					}));
				}

				screen.update();
				btn.onPress();
			}
		};

		cancel = new ExtendedButton(x + xSize / 2 + 3, y + ySize - 15, 15, 15, Component.empty(), null){
			@Override
			public void renderButton(PoseStack mStack, int mouseX, int mouseY, float partial){
				setMessage(Component.empty());
				super.renderButton(mStack, mouseX, mouseY, partial);
				RenderSystem.setShaderTexture(0, DragonAltarGUI.CANCEL_BUTTON);
				blit(mStack, x, y, 0, 0, 15, 15, 15, 15);

				if(isHovered){
					TooltipRendering.drawHoveringText(mStack, Component.translatable("ds.gui.dragon_editor.tooltip.cancel"), mouseX, mouseY);
				}
			}


			@Override
			public void onPress(){
				btn.onPress();
			}
		};

		newborn = new ExtendedCheckbox(x + 5, y + 12, xSize - 10, 10, 10, Component.translatable("ds.level.newborn"), false, s -> {}){
			@Override
			public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
				super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
				if(screen.level == DragonLevel.NEWBORN){
					selected = true;
					active = false;
				}else{
					active = true;
				}
			}
		};
		young = new ExtendedCheckbox(x + 5, y + 27, xSize - 10, 10, 10, Component.translatable("ds.level.young"), false, s -> {}){
			@Override
			public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
				super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
				if(screen.level == DragonLevel.YOUNG){
					selected = true;
					active = false;
				}else{
					active = true;
				}
			}
		};
		adult = new ExtendedCheckbox(x + 5, y + 27 + 15, xSize - 10, 10, 10, Component.translatable("ds.level.adult"), false, s -> {}){
			@Override
			public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
				super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
				if(screen.level == DragonLevel.ADULT){
					selected = true;
					active = false;
				}else{
					active = true;
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
		return ImmutableList.of(confirm, cancel, newborn, young, adult);
	}

	@Override
	public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
		ScreenUtils.blitWithBorder(pMatrixStack, DropdownList.BACKGROUND_TEXTURE, x, y - 3, 0, 0, xSize, ySize + 6, 32, 32, 10, 10, 10, 10, (float)10);
		confirm.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		cancel.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		newborn.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		young.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		adult.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
		Gui.drawCenteredString(pMatrixStack, Minecraft.getInstance().font, Component.translatable("ds.gui.dragon_editor.copy_to"), x + xSize / 2, y + 1, 14737632);
	}
}