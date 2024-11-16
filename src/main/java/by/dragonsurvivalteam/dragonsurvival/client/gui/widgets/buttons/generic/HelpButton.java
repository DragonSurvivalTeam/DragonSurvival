package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HelpButton extends ExtendedButton {
	public static final ResourceLocation texture = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/help_button.png");
	public String text;
	private final List<Component> tooltip;
	public int variation;
	public AbstractDragonType type;

	public HelpButton(int x, int y, int sizeX, int sizeY, String text, int variation){
		this(DragonUtils.getDragonType(Minecraft.getInstance().player), x, y, sizeX, sizeY, text, variation);
	}

	public HelpButton(AbstractDragonType type, int x, int y, int sizeX, int sizeY, String text, int variation){
		super(x, y, sizeX, sizeY, Component.empty(), s -> {});
		this.text = text;
		this.variation = variation;
		this.type = type;

		tooltip = new ArrayList<>();

		if (text != null && !text.isBlank()) {
			for (String string : I18n.get(text).split("\n")) {
				tooltip.add(Component.literal(string));
			}
		}
	}

	@Override
	public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
		RenderSystem.setShaderTexture(0, texture);

		float size = variation == 0 ? 18f : 22f;
		float xSize = (float)(width + (variation == 0 ? 0 : 2)) / size;
		float ySize = (float)(height + (variation == 0 ? 0 : 2)) / size;

		int offset = 0;

		if (isHoveredOrFocused()) {
			int id;

			if (type == null) {
				id = 4;
			} else {
				id = switch (type.getTypeName()) {
					case "cave" -> 1;
					case "forest" -> 2;
					case "sea" -> 3;
					default -> 0;
				};
			}

			offset += (int) (id * size);
		}

		guiGraphics.pose().pushPose();
		guiGraphics.pose().translate(getX() - getX() * xSize, getY() - getY() * ySize, 0);
		guiGraphics.pose().scale(xSize, ySize, 0);

		if (variation == 0) {
			guiGraphics.blit(texture, getX(), getY(), 0, (float) offset, 18, 18, 256, 256);
		} else {
			guiGraphics.blit(texture, getX() - 1, getY() - 1, 18, (float) offset, 22, 22, 256, 256);
		}

		guiGraphics.pose().popPose();
	}

	/** To prevent the tooltip from getting overlayed by the screen. See postScreenRender in ToolTipHandler.java. */
	public void renderTooltip(final GuiGraphics guiGraphics, int mouseX, int mouseY) {
		guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_){
		return false;
	}
}