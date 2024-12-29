package by.dragonsurvivalteam.dragonsurvival.magic;

import by.dragonsurvivalteam.dragonsurvival.client.gui.AbilityTooltipPositioner;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.upgrade.Upgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2ic;
import software.bernie.geckolib.util.Color;

import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class AbilityAndPenaltyTooltipRenderer {
    @Translation(type = Translation.Type.MISC, comments = "Active Ability")
    private static final String ACTIVE = Translation.Type.ABILITY.wrap("general.active");

    @Translation(type = Translation.Type.MISC, comments = "Passive Ability")
    private static final String PASSIVE = Translation.Type.ABILITY.wrap("general.passive");

    @Translation(type = Translation.Type.MISC, comments = "Penalty")
    private static final String PENALTY = Translation.Type.ABILITY.wrap("general.innate");

    @Translation(type = Translation.Type.MISC, comments = "Info")
    private static final String INFO = Translation.Type.ABILITY.wrap("general.info");

    @Translation(type = Translation.Type.MISC, comments = "Hold ‘Shift’ for info")
    private static final String INFO_SHIFT = Translation.Type.ABILITY.wrap("general.info_shift");

    private static final ResourceLocation BARS = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/widget_bars.png");

    public static void drawTooltip(@NotNull final GuiGraphics graphics, int x, int y, List<Component> info, List<FormattedCharSequence> description, int colorXPos, int colorYPos, String headerTranslationKey, Component name, Color tooltipBackgroundColor, int maxLevel, int level, ResourceLocation icon) {
        FormattedText textContents = Component.empty();
        for (Component component : info) {
            textContents = FormattedText.composite(textContents, Component.empty().append("\n"));
            textContents = FormattedText.composite(textContents, component);
        }

        int extraWidth1 = (int) (150 / 1.25);
        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(textContents, extraWidth1 - 5);
        int longest = lines.stream().map(s -> Minecraft.getInstance().font.width(s) + 20).max(Integer::compareTo).orElse(0);
        int extraWidth = Math.min(longest, extraWidth1);

        int backgroundWidth = 150 + 5;
        int backgroundHeight = 35 + 24 + description.size() * 9;
        int sideWidth = Screen.hasShiftDown() ? extraWidth : 15;
        int sideHeight = Screen.hasShiftDown() ? 27 + lines.size() * 9 : backgroundHeight - 10;
        ClientTooltipPositioner positioner = new AbilityTooltipPositioner(Screen.hasShiftDown() ? sideWidth : 0);
        Vector2ic position = positioner.positionTooltip(graphics.guiWidth(), graphics.guiHeight(), x, y, backgroundWidth, Math.max(sideHeight, backgroundHeight));
        int trueX = position.x();
        int trueY = position.y();

        if (!info.isEmpty()) {
            // Backing for info tab
            graphics.blitWithBorder(BARS, trueX - (Screen.hasShiftDown() ? extraWidth : 10), trueY + 3, 40, 20, sideWidth, sideHeight, 20, 20, 3);
            // Top bar for info tab
            graphics.blitWithBorder(BARS, trueX - (Screen.hasShiftDown() ? extraWidth : 10) + 3, trueY + 9, colorXPos, colorYPos, Screen.hasShiftDown() ? extraWidth : 15, 20, 20, 20, 3);

            if (Screen.hasShiftDown()) {
                graphics.drawString(Minecraft.getInstance().font, Component.translatable(INFO), trueX - extraWidth + 10, trueY + 15, -1);

                for (int line = 0; line < lines.size(); ++line) {
                    graphics.drawString(Minecraft.getInstance().font, lines.get(line), trueX - extraWidth + 5, trueY + 5 + 18 + line * 9, DSColors.GRAY);
                }
            }
        }

        // Background
        graphics.blitWithBorder(BARS, trueX - 2, trueY - 4, 40, 20, backgroundWidth, backgroundHeight, 20, 20, 3, 3, 3, 3);
        // Top bar
        graphics.blitWithBorder(BARS, trueX, trueY + 3, colorXPos, colorYPos, 150, 20, 20, 20, 3);
        // Backing square for ability icon
        graphics.blitWithBorder(BARS, trueX, trueY, 0, 100, 26, 26, 24, 24, 3);

        graphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable(headerTranslationKey), trueX + 150 / 2, trueY + 30, tooltipBackgroundColor.getColor());

        if (maxLevel > DragonAbilityInstance.MIN_LEVEL) {
            graphics.drawCenteredString(Minecraft.getInstance().font, Component.empty().append(level + "/" + maxLevel), trueX + 150 - 18, trueY + 9, -1);
            graphics.drawCenteredString(Minecraft.getInstance().font, name, trueX + 150 / 2, trueY + 9, -1);
        } else {
            graphics.drawCenteredString(Minecraft.getInstance().font, name, trueX + 150 / 2 + 10, trueY + 9, -1);
        }

        for (int k1 = 0; k1 < description.size(); ++k1) {
            graphics.drawString(Minecraft.getInstance().font, description.get(k1), trueX + 5, trueY + 47 + k1 * 9, -5592406);
        }

        if (!info.isEmpty()) {
            graphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable(INFO_SHIFT).withStyle(ChatFormatting.DARK_GRAY), trueX + 150 / 2, trueY + 47 + (description.size() - 1) * 9, 0);
        }

        graphics.blitSprite(icon, trueX + 5, trueY + 5, 16, 16);
    }

    public static void drawAbilityTooltip(@NotNull final GuiGraphics guiGraphics, int x, int y, final DragonAbilityInstance ability) {
        int colorXPos = 0;
        int colorYPos = !ability.isPassive() ? 20 : 0;

        FormattedText rawDescription = Component.translatable(Translation.Type.ABILITY_DESCRIPTION.wrap(ability.location()));
        List<Component> info = ability.getInfo(Minecraft.getInstance().player);

        Upgrade upgrade = ability.value().upgrade().orElse(null);

        if (upgrade != null && ability.level() < upgrade.maximumLevel()) {
            rawDescription = FormattedText.composite(rawDescription, Component.empty().append("\n\n"));
            MutableComponent upgradeComponent = upgrade.getDescription(ability.level());
            rawDescription = FormattedText.composite(rawDescription, upgradeComponent.withColor(Color.GREEN.getColor()));
        }

        if (!info.isEmpty()) {
            rawDescription = FormattedText.composite(rawDescription, Component.empty().append("\n\n"));
        }

        List<FormattedCharSequence> description = Minecraft.getInstance().font.split(rawDescription, 150 - 7);
        Color color = ability.isPassive() ? new Color(DSColors.withAlpha(DSColors.PASSIVE_BACKGROUND, 1f)) : new Color(DSColors.withAlpha(DSColors.ACTIVE_BACKGROUND, 1f));
        drawTooltip(guiGraphics, x, y, info, description, colorXPos, colorYPos, ability.isPassive() ? PASSIVE : ACTIVE, ability.getName(), color, ability.getMaxLevel(), ability.level(), ability.getIcon());
    }


    public static void drawPenaltyTooltip(@NotNull final GuiGraphics guiGraphics, int x, int y, final Holder<DragonPenalty> penalty) {
        int colorXPos = 20;
        int colorYPos = 0;

        //noinspection DataFlowIssue -> key is present
        FormattedText description = Component.translatable(Translation.Type.PENALTY_DESCRIPTION.wrap(penalty.getKey().location()));
        Component component = penalty.value().getDescription(Minecraft.getInstance().player);

        List<Component> components;

        if (component.getContents() == PlainTextContents.EMPTY) {
            components = List.of();
        } else {
            components = List.of(component);
        }

        if (!components.isEmpty()) {
            description = FormattedText.composite(description, Component.empty().append("\n\n"));
        }

        List<FormattedCharSequence> formattedDescription = Minecraft.getInstance().font.split(description, 150 - 7);
        Component name = Component.translatable(Translation.Type.PENALTY.wrap(penalty.getKey().location()));
        drawTooltip(guiGraphics, x, y, components, formattedDescription, colorXPos, colorYPos, PENALTY, name, Color.ofRGB(145, 46, 46), -1, -1, penalty.value().icon());
    }
}