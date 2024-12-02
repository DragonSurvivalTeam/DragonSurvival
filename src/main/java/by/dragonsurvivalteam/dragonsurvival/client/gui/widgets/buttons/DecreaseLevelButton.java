package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.PlusMinusButton;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DecreaseLevelButton extends PlusMinusButton {
    @Translation(type = Translation.Type.MISC, comments = "§aDowngrade the skill, gaining§r %s §alevels worth of experience points§r")
    private static final String LEVEL_DOWN = Translation.Type.GUI.wrap("level_down");

    public int gainedLevels;

    private final int slot;
    private DragonAbilityInstance ability;

    public DecreaseLevelButton(int x, int y, int slot) {
        super(x, y, 16, 16, false, Button::onPress);
        this.slot = slot;
    }

    @Override
    public void onPress() {
        // FIXME
        /*LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        MagicData data = MagicData.getData(player);

        ability = data.getAbilityFromSlot(slot);

        if (ability != null) {
            int newLevel = ability.getLevel() - 1;

            if (newLevel >= ability.getMinLevel()) {
                PacketDistributor.sendToServer(new SyncSkillLevelChangeCost(ability.getName(), newLevel, -1));
                DragonAbilities.setAbilityLevel(Minecraft.getInstance().player, ability.getClass(), newLevel);
            }
        }*/
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);
        // FIXME
        /*if (isHovered()) {
            //noinspection DataFlowIssue -> player is present
            MagicData data = MagicData.getData(Minecraft.getInstance().player);
            ability = data.getAbilityFromSlot(slot);

            if (ability != null && ability.getLevel() > ability.getMinLevel()) {
                gainedLevels = ability.getLevelCost(-1);
                graphics.renderTooltip(Minecraft.getInstance().font, Component.translatable(LEVEL_DOWN, gainedLevels), mouseX, mouseY);
            } else {
                gainedLevels = 0;
            }
        }*/
    }
}