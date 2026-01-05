package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAbilityScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ClickHoverButton;
import by.dragonsurvivalteam.dragonsurvival.network.magic.AttemptManualUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperiencePointsUpgrade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LevelButton extends ClickHoverButton {
    private static final Identifier DOWNGRADE_CLICK = DragonSurvival.res("textures/gui/ability_screen/arrow_left_upgrade_click.png");
    private static final Identifier DOWNGRADE_HOVER = DragonSurvival.res("textures/gui/ability_screen/arrow_left_upgrade_hover.png");
    private static final Identifier DOWNGRADE_MAIN = DragonSurvival.res("textures/gui/ability_screen/arrow_left_upgrade_main.png");

    public static final Identifier UPGRADE_CLICK = DragonSurvival.res("textures/gui/ability_screen/arrow_right_upgrade_click.png");
    private static final Identifier UPGRADE_HOVER = DragonSurvival.res("textures/gui/ability_screen/arrow_right_upgrade_hover.png");
    private static final Identifier UPGRADE_MAIN = DragonSurvival.res("textures/gui/ability_screen/arrow_right_upgrade_main.png");

    private static final int WIDTH = 16;
    private static final int HEIGHT = 16;

    private final Type type;
    private final DragonAbilityInstance ability;

    public enum Type {
        DOWNGRADE(DOWNGRADE_CLICK, DOWNGRADE_HOVER, DOWNGRADE_MAIN),
        UPGRADE(UPGRADE_CLICK, UPGRADE_HOVER, UPGRADE_MAIN);

        public final Identifier click;
        public final Identifier hover;
        public final Identifier main;

        Type(final Identifier click, final Identifier hover, final Identifier main) {
            this.click = click;
            this.hover = hover;
            this.main = main;
        }
    }

    public void resetDimensions() {
        this.width = WIDTH;
        this.height = HEIGHT;
    }

    public LevelButton(final Type type, final DragonAbilityInstance ability, int xPos, int yPos) {
        super(xPos, yPos, WIDTH, HEIGHT, 0, 0, 16, 16, Component.empty(), button -> {
            switch (type) {
                case DOWNGRADE -> ClientPacketDistributor.sendToServer(new AttemptManualUpgrade(ability.key(), ExperiencePointsUpgrade.Type.DOWNGRADE));
                case UPGRADE -> ClientPacketDistributor.sendToServer(new AttemptManualUpgrade(ability.key(), ExperiencePointsUpgrade.Type.UPGRADE));
            }
        }, type.click, type.hover, type.main);

        this.type = type;
        this.ability = ability;
    }

    public int getExperienceModification() {
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);

        if (player.hasInfiniteMaterials()) {
            return 0;
        }

        return ability.value().upgrade().map(type -> {
            if (type instanceof ExperiencePointsUpgrade upgrade) {
                return upgrade.getExperience(ability, getExperienceType());
            }

            return 0;
        }).orElse(0);
    }

    public boolean canModify() {
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);

        return ability.value().upgrade().map(type -> {
            if (type instanceof ExperiencePointsUpgrade upgrade) {
               return upgrade.canModifyLevel(player, ability, getExperienceType());
            }

            return false;
        }).orElse(false);
    }

    private ExperiencePointsUpgrade.Type getExperienceType() {
        return type == Type.UPGRADE ? ExperiencePointsUpgrade.Type.UPGRADE : ExperiencePointsUpgrade.Type.DOWNGRADE;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!canModify()) {
            return;
        }

        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (Minecraft.getInstance().screen instanceof DragonAbilityScreen abilityScreen && isHovered()) {
            abilityScreen.lastHoveredLevelButton = this;
        }
    }

    @Override
    public void onPress(@NotNull InputWithModifiers inputWithModifiers) {
        if (canModify()) {
            super.onPress(inputWithModifiers);
        }
    }
}
