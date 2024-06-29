package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.AbilityScreen;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.common.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SkillProgressButton extends Button{
	private final int slot;
	private final AbilityScreen screen;
	private ActiveDragonAbility ability;

	public SkillProgressButton(int x, int y, int slot, AbilityScreen screen){
		super(x, y, 16, 16, Component.empty(), button -> {}, DEFAULT_NARRATION);
		this.slot = slot;
		this.screen = screen;
	}

	@Override
	public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float p_230431_4_){
		ResourceLocation texture = ability != null ? ability.getIcon() : AbilityButton.BLANK_TEXTURE;

		if(screen.unlockAbleSkills.size() > slot){
			ActiveDragonAbility ability1 = screen.unlockAbleSkills.get(slot);

			if (ability1 != null && ability1.getIcon() != null) {
				texture = ability1.getIcon();
				ability = ability1;
			}
		} else {
			texture = AbilityButton.BLANK_TEXTURE;
		}

		if (texture == AbilityButton.BLANK_TEXTURE) {
			ability = null;
		}

		guiGraphics.blit(AbilityButton.BLANK_2_TEXTURE, getX() - 1, getY() - 1, 0, 0, 18, 18, 18, 18);
		guiGraphics.blit(texture, getX(), getY(), 0, 0, 16, 16, 16, 16);

		if (ability != null) {
			DragonStateHandler handler = DragonStateProvider.getOrGenerateHandler(Minecraft.getInstance().player);

			if (handler.isDragon()) {
				DragonAbility playerAbility = DragonAbilities.getSelfAbility(Minecraft.getInstance().player, ability.getClass());

				if (ability.getLevel() > playerAbility.getLevel() + 1) {
					guiGraphics.fill(getX(), getY(), getX() + 16, getY() + 16, new Color(0.25F, 0.25F, 0.25F, 0.75F).getRGB());
				}

				if (isHoveredOrFocused()) {
					ChatFormatting format = Objects.equals(handler.getType(), DragonTypes.CAVE) ? ChatFormatting.DARK_RED : Objects.equals(handler.getType(), DragonTypes.SEA) ? ChatFormatting.AQUA : Objects.equals(handler.getType(), DragonTypes.FOREST) ? ChatFormatting.GREEN : ChatFormatting.WHITE;
					Component component = ability.getTitle();
					ArrayList<Component> description = new ArrayList<>(List.of(Component.empty().append(component).append(" (" + ability.getLevel() + " / " + ability.getMaxLevel() + ")").withStyle(format)));

					if (!ability.getLevelUpInfo().isEmpty()) {
						description.addAll(ability.getLevelUpInfo());
					}

					int requiredLevel = ability.getCurrentRequiredLevel();

					if (requiredLevel != -1) {
						description.add(Component.translatable("ds.skill.required_level", requiredLevel).withStyle(ChatFormatting.WHITE));
					}

					guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, description, mouseX, mouseY);
				}
			}
		}
	}
}