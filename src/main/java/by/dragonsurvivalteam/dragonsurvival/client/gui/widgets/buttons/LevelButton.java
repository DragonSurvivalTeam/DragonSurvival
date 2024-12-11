package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.AbilityScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ClickHoverButton;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.Upgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.ExperienceUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LevelButton extends ClickHoverButton {
    private static final ResourceLocation DOWNGRADE_CLICK = DragonSurvival.res( "textures/gui/ability_screen/arrow_left_upgrade_click.png");
    private static final ResourceLocation DOWNGRADE_HOVER = DragonSurvival.res( "textures/gui/ability_screen/arrow_left_upgrade_hover.png");
    private static final ResourceLocation DOWNGRADE_MAIN = DragonSurvival.res( "textures/gui/ability_screen/arrow_left_upgrade_main.png");

    public static final ResourceLocation UPGRADE_CLICK = DragonSurvival.res( "textures/gui/ability_screen/arrow_right_upgrade_click.png");
    private static final ResourceLocation UPGRADE_HOVER = DragonSurvival.res( "textures/gui/ability_screen/arrow_right_upgrade_hover.png");
    private static final ResourceLocation UPGRADE_MAIN = DragonSurvival.res( "textures/gui/ability_screen/arrow_right_upgrade_main.png");

    private final Type type;
    private final DragonAbilityInstance ability;

    public enum Type {
        DOWNGRADE(DOWNGRADE_CLICK, DOWNGRADE_HOVER, DOWNGRADE_MAIN),
        UPGRADE(UPGRADE_CLICK, UPGRADE_HOVER, UPGRADE_MAIN);

        public final ResourceLocation click;
        public final ResourceLocation hover;
        public final ResourceLocation main;

        Type(final ResourceLocation click, final ResourceLocation hover, final ResourceLocation main) {
            this.click = click;
            this.hover = hover;
            this.main = main;
        }
    }

    public LevelButton(final Type type, final DragonAbilityInstance ability, int xPos, int yPos) {
        super(xPos, yPos, 9, 14, 0, 0, 16, 16, Component.empty(), button -> {
            int modification = ((LevelButton) button).getExperienceModification();

            if (modification == 0) {
                return;
            }

            LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);

            switch (type) {
                case DOWNGRADE -> {
                    player.giveExperiencePoints(modification);
                    ability.setLevel(ability.level() - 1);
                }
                case UPGRADE -> {
                    float availableExperience = ExperienceUtils.getTotalExperience(player);

                    if (availableExperience >= modification) {
                        player.giveExperiencePoints(modification);
                        ability.setLevel(ability.level() + 1);
                    }
                }
            }

            // TODO :: send packet to server to update ability level
        }, type.click, type.hover, type.main);

        this.type = type;
        this.ability = ability;
    }

    @SuppressWarnings("DataFlowIssue") // player is present
    public int getExperienceModification() {
        if (!isHovered()) {
            return 0;
        }

        if (!canModify()) {
            return 0;
        }

        return switch (type) {
            // TODO :: is -1 correct? for lookup yes but other scaling?
            case DOWNGRADE -> (int) MagicData.getData(Minecraft.getInstance().player).getCost(ability.key(), -1);
            case UPGRADE -> (int) -MagicData.getData(Minecraft.getInstance().player).getCost(ability.key(), 0);
        };
    }

    public boolean canModify() {
        //noinspection OptionalGetWithoutIsPresent -> upgrade is present
        Upgrade upgrade = ability.value().upgrade().get();

        return switch (type) {
            case DOWNGRADE -> ability.level() > DragonAbilityInstance.MIN_LEVEL;
            case UPGRADE -> ability.level() < upgrade.maximumLevel();
        };
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (Minecraft.getInstance().screen instanceof AbilityScreen abilityScreen && isHovered()) {
            abilityScreen.hoveredLevelButton = this;
        }
    }
}
