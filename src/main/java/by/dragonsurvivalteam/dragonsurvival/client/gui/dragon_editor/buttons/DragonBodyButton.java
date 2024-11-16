package by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.utils.TooltipProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class DragonBodyButton extends Button implements TooltipProvider {
	private DragonEditorScreen dragonEditorScreen;
	private AbstractDragonBody dragonBody;
	private ResourceLocation texture_location;

	private final Component tooltip;

	private int pos;
	private boolean locked;
	
	public DragonBodyButton(DragonEditorScreen dragonEditorScreen, int x, int y, int xSize, int ySize, AbstractDragonBody dragonBody, int pos, boolean locked) {
		super(x, y, xSize, ySize, Component.literal(dragonBody.toString()), btn -> {
			if (!locked) {
				dragonEditorScreen.dragonBody = dragonBody;
				dragonEditorScreen.update();
			}
		});
		this.texture_location = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/body_type_icon_" + dragonEditorScreen.dragonType.getTypeName().toLowerCase() + ".png");
		this.dragonEditorScreen = dragonEditorScreen;
		this.dragonBody = dragonBody;
		this.tooltip = Component.translatable("ds.gui.body_types." + dragonBody.getBodyName().toLowerCase() + ".tooltip");
		this.pos = pos;
		this.locked = locked;
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float pPartialTicks){
		active = visible = dragonEditorScreen.showUi;
		super.render(pose, mouseX, mouseY, pPartialTicks);
	}

	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
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
		this.blit(pPoseStack, this.x, this.y, pos * this.width, i * this.height, this.width, this.height);
		//RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
	}

	@Override
	public List<Component> getTooltip() {
		return List.of(tooltip);
	}
}
