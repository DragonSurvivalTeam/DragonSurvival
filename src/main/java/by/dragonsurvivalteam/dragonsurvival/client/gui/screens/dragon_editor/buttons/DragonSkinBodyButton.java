package by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.buttons;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.SkinsScreen;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class DragonSkinBodyButton extends Button {
	private final SkinsScreen screen;
	private final AbstractDragonBody dragonBody;
	private final ResourceLocation location;
	private final int pos;
	
	public DragonSkinBodyButton(SkinsScreen screen, int x, int y, int xSize, int ySize, AbstractDragonBody body, int pos) {
		super(x, y, xSize, ySize, Component.literal(body.toString()), btn -> {
			screen.dragonBody = body;
		}, DEFAULT_NARRATION);
		location = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/body_type_icon_skintab.png");
		this.screen = screen;
		this.dragonBody = body;
		this.pos = pos;
	}

	@Override
	public void renderWidget(GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		int i = 0;
		if (this.dragonBody.equals(screen.handler.getBody())) {
			i = 2;
		} else if (this.isHoveredOrFocused()) {
			i = 1;
		}
		guiGraphics.blit(location, getX(), getY(), pos * this.width, i * this.height, this.width, this.height);
	}
}
