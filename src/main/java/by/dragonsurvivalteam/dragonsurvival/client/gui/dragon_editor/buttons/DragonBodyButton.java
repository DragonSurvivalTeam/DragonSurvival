package by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class DragonBodyButton extends Button {
	private final DragonEditorScreen dragonEditorScreen;
	private final AbstractDragonBody dragonBody;
	private final ResourceLocation texture_location;
	private final int pos;
	private final boolean locked;
	
	public DragonBodyButton(DragonEditorScreen dragonEditorScreen, int x, int y, int xSize, int ySize, AbstractDragonBody dragonBody, int pos, boolean locked) {
		super(x, y, xSize, ySize, Component.literal(dragonBody.toString()), btn -> {
			if (!locked) {
				dragonEditorScreen.dragonBody = dragonBody;
				dragonEditorScreen.update();
			}
		}, DEFAULT_NARRATION);

		this.texture_location = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/body_type_icon_" + dragonEditorScreen.dragonType.getTypeNameLowerCase() + ".png");
		this.dragonEditorScreen = dragonEditorScreen;
		this.dragonBody = dragonBody;
		this.pos = pos;
		this.locked = locked;

		setTooltip(Tooltip.create(Component.translatable("ds.gui.body_types." + dragonBody.getBodyName().toLowerCase(Locale.ENGLISH) + ".tooltip")));
	}

	@Override
	public void render(@NotNull GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTicks){
		active = visible = dragonEditorScreen.showUi;
		super.render(guiGraphics, pMouseX, pMouseY, pPartialTicks);
	}

	@Override
	public void renderWidget(@NotNull GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		RenderSystem.setShaderTexture(0, texture_location);
		RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);

		int i = 0;
		if (this.dragonBody.equals(dragonEditorScreen.dragonBody)) {
			i = 2;
		} else if (this.locked) {
			i = 3;
		} else if (this.isHoveredOrFocused()) {
			i = 1;
		}
		guiGraphics.blit(this.texture_location, getX(), getY(), pos * this.width, i * this.height, this.width, this.height, 256, 256);
	}
}
