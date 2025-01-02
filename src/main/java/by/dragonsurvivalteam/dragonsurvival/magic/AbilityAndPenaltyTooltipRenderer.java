package by.dragonsurvivalteam.dragonsurvival.magic;

import by.dragonsurvivalteam.dragonsurvival.client.gui.AbilityTooltipPositioner;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.UpgradeType;
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

import java.util.ArrayList;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class AbilityAndPenaltyTooltipRenderer {
    @Translation(comments = "Active Ability")
    private static final String ACTIVE = Translation.Type.ABILITY.wrap("general.active");

    @Translation(comments = "Passive Ability")
    private static final String PASSIVE = Translation.Type.ABILITY.wrap("general.passive");

    @Translation(comments = "Penalty")
    private static final String PENALTY = Translation.Type.ABILITY.wrap("general.innate");

    @Translation(comments = "Info")
    private static final String INFO = Translation.Type.ABILITY.wrap("general.info");

    @Translation(comments = "Hold ‘Shift’ for info")
    private static final String INFO_SHIFT = Translation.Type.ABILITY.wrap("general.info_shift");

    private static final ResourceLocation BARS = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/widget_bars.png");

    private static final int MAX_SHOWN_LINES = 15;
    private static int maxScrollAmount = Integer.MAX_VALUE;

    public static int maxScroll() {
        return Math.max(0, maxScrollAmount - MAX_SHOWN_LINES);
    }

    public static void drawTooltip(
            @NotNull final GuiGraphics graphics,
            int x,
            int y,
            final List<Component> shiftInfo,
            final List<FormattedCharSequence> description,
            int colorXPos,
            int colorYPos,
            final String headerTranslationKey,
            final Component name,
            final Color tooltipBackgroundColor,
            int maxLevel,
            int abilityLevel,
            final ResourceLocation icon,
            int scrollAmount
    ) {
        FormattedText textContents = formatText(shiftInfo);

        int maxLineWidth = 150; // Tooltip#MAX_WIDTH is 170
        // -10 to leave some space so it doesn't overflow into the main description window
        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(textContents, maxLineWidth - 10);
        maxScrollAmount = lines.size();
        scrollAmount = Math.clamp(scrollAmount, 0, maxScroll());

        List<FormattedCharSequence> shownLines = new ArrayList<>();

        for (int line = scrollAmount; line < lines.size(); line++) {
            if (shownLines.size() == MAX_SHOWN_LINES) {
                break;
            }

            shownLines.add(lines.get(line));
        }

        int backgroundWidth = 150 + 5;
        int backgroundHeight = 35 + 24 + description.size() * 9;
        int sideWidth = Screen.hasShiftDown() ? maxLineWidth : 15;
        int sideHeight = Screen.hasShiftDown() ? 36 + shownLines.size() * 9 : backgroundHeight - 10;

        ClientTooltipPositioner positioner = new AbilityTooltipPositioner(Screen.hasShiftDown() ? sideWidth : 0);
        Vector2ic position = positioner.positionTooltip(graphics.guiWidth(), graphics.guiHeight(), x, y, backgroundWidth, Math.max(sideHeight, backgroundHeight));

        int trueX = position.x();
        int trueY = position.y();

        if (!shiftInfo.isEmpty()) {
            // Backing for info tab
            graphics.blitWithBorder(BARS, trueX - (Screen.hasShiftDown() ? maxLineWidth : 10), trueY + 3, 40, 20, sideWidth, sideHeight, 20, 20, 3);
            // Top bar for info tab
            graphics.blitWithBorder(BARS, trueX - (Screen.hasShiftDown() ? maxLineWidth : 10) + 3, trueY + 9, colorXPos, colorYPos, Screen.hasShiftDown() ? maxLineWidth : 15, 20, 20, 20, 3);

            if (Screen.hasShiftDown()) {
                graphics.drawString(Minecraft.getInstance().font, Component.translatable(INFO), trueX - maxLineWidth + 10, trueY + 15, -1);

                for (int line = 0; line < shownLines.size(); line++) {
                    graphics.drawString(Minecraft.getInstance().font, shownLines.get(line), trueX - maxLineWidth + 5, trueY + 5 + 28 + line * 9, DSColors.GRAY);
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
            graphics.drawCenteredString(Minecraft.getInstance().font, Component.empty().append(abilityLevel + "/" + maxLevel), trueX + 150 - 18, trueY + 9, -1);
            graphics.drawCenteredString(Minecraft.getInstance().font, name, trueX + 150 / 2, trueY + 9, -1);
        } else {
            graphics.drawCenteredString(Minecraft.getInstance().font, name, trueX + 150 / 2 + 10, trueY + 9, -1);
        }

        for (int line = 0; line < description.size(); ++line) {
            graphics.drawString(Minecraft.getInstance().font, description.get(line), trueX + 5, trueY + 47 + line * 9, -5592406);
        }

        if (!shiftInfo.isEmpty()) {
            graphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable(INFO_SHIFT).withStyle(ChatFormatting.DARK_GRAY), trueX + 150 / 2, trueY + 47 + (description.size() - 1) * 9, 0);
        }

        graphics.blitSprite(icon, trueX + 5, trueY + 5, 16, 16);
    }

    private static FormattedText formatText(final List<Component> shiftInfo) {
        // Starts with a null value to make sure the first line is not a new line
        // This way we can properly offset the start of the text below the header when scrolling and not scrolling
        FormattedText textContents = null;

        for (Component component : shiftInfo) {
            if (textContents == null) {
                textContents = FormattedText.composite(component);
            } else {
                textContents = FormattedText.composite(textContents, Component.literal("\n"));
                textContents = FormattedText.composite(textContents, component);
            }
        }

        if (textContents == null) {
            textContents = FormattedText.composite(Component.empty());
        }

        return textContents;
    }

    public static void drawAbilityTooltip(@NotNull final GuiGraphics guiGraphics, int x, int y, final DragonAbilityInstance ability, int scrollAmount) {
        int colorXPos = 0;
        int colorYPos = !ability.isPassive() ? 20 : 0;

        FormattedText rawDescription = Component.translatable(Translation.Type.ABILITY_DESCRIPTION.wrap(ability.location()));
        List<Component> info = ability.value().getInfo(Minecraft.getInstance().player, ability);

        UpgradeType<?> upgrade = ability.value().upgrade().orElse(null);

        if (upgrade != null && ability.level() < upgrade.maxLevel()) {
            rawDescription = FormattedText.composite(rawDescription, Component.empty().append("\n\n"));
            MutableComponent upgradeComponent = upgrade.getDescription(ability.level() + 1);
            rawDescription = FormattedText.composite(rawDescription, upgradeComponent.withColor(Color.GREEN.getColor()));
        }

        if (!info.isEmpty()) {
            rawDescription = FormattedText.composite(rawDescription, Component.empty().append("\n\n"));
        }

        List<FormattedCharSequence> description = Minecraft.getInstance().font.split(rawDescription, 150 - 7);
        Color color = ability.isPassive() ? new Color(DSColors.withAlpha(DSColors.PASSIVE_BACKGROUND, 1f)) : new Color(DSColors.withAlpha(DSColors.ACTIVE_BACKGROUND, 1f));
        drawTooltip(guiGraphics, x, y, info, description, colorXPos, colorYPos, ability.isPassive() ? PASSIVE : ACTIVE, ability.getName(), color, ability.getMaxLevel(), ability.level(), ability.getIcon(), scrollAmount);
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
        drawTooltip(guiGraphics, x, y, components, formattedDescription, colorXPos, colorYPos, PENALTY, name, Color.ofRGB(145, 46, 46), -1, -1, penalty.value().icon(), 0);
    }
}