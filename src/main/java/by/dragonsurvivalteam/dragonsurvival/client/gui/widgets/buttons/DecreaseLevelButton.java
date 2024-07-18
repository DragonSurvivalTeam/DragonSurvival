package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ArrowButton;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.magic.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.magic.common.passive.PassiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.network.NetworkHandler;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSkillLevelChangeCost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class DecreaseLevelButton extends ArrowButton{
	private final int slot;
	private PassiveDragonAbility ability;

	public DecreaseLevelButton(int x, int y, int slot){
		super(x, y, 16, 16, false, Button::onPress);

		this.slot = slot;
	}

	@Override
	public void onPress(){
		DragonStateProvider.getCap(Minecraft.getInstance().player).ifPresent(cap -> {
			ability = cap.getMagicData().getPassiveAbilityFromSlot(slot);

			if(ability != null){
				int newLevel = ability.getLevel() - 1;

				if (newLevel >= ability.getMinLevel()){
					NetworkHandler.CHANNEL.sendToServer(new SyncSkillLevelChangeCost(newLevel, ability.getName(), -1));
                    DragonAbilities.setAbilityLevel(Minecraft.getInstance().player, ability.getClass(), newLevel);
                }
            }
		});
	}

	@Override
	public void render(@NotNull final GuiGraphics guiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
		super.render(guiGraphics, pMouseX, pMouseY, pPartialTick);

		if (isHovered()) {
			DragonStateProvider.getCap(Minecraft.getInstance().player).ifPresent(cap -> {
				ability = cap.getMagicData().getPassiveAbilityFromSlot(slot);

				if (ability != null) {
					if (ability.getLevel() > ability.getMinLevel()) {
						guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("ds.skill.level.down", (int) Math.max(0, ability.getLevelCost() * 0.0F)), pMouseX, pMouseY);
					}
				}
			});
		}
	}
}