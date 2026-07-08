package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.AbilityTooltipPositioner;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.UpgradeType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;

public class AbilityAndPenaltyTooltipRenderer {
    @Translation(comments = "Hold 'Shift' for info")
    private static final String INFO_SHIFT = Translation.Type.GUI.wrap("general.info_shift");

    @Translation(comments = "§4Manually disabled§r")
    private static final String MANUALLY_DISABLED = Translation.Type.GUI.wrap("general.manually_disabled");

    private static final Identifier EFFECT_HEADER = DragonSurvival.res("ability_effect_header");
    private static final Identifier BARS = DragonSurvival.res("textures/gui/widget_bars.png");
    private static final int BARS_TEXTURE_SIZE = 256;
    private static final int MAX_SHOWN_LINES = 15;
    private static int maxScrollAmount = Integer.MAX_VALUE;

    public static int maxScroll() {
        return Math.max(0, maxScrollAmount - MAX_SHOWN_LINES);
    }

    public static void drawTooltip(
            @NotNull final GuiGraphicsExtractor graphics,
            int x,
            int y,
            final List<Component> shiftInfo,
            final FormattedText rawDescription,
            int colorXPos,
            int colorYPos,
            final String headerTranslationKey,
            final Component name,
            int tooltipBackgroundColor,
            int maxLevel,
            int abilityLevel,
            final Identifier icon,
            int scrollAmount
    ) {
        graphics.nextStratum();

        FormattedText textContents = formatText(shiftInfo);

        int maxLineWidth = 150; // Tooltip#MAX_WIDTH is 170
        // -10 to leave some space so it doesn't overflow into the main description window
        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(textContents, maxLineWidth - 10);
        maxScrollAmount = lines.size();
        scrollAmount = Math.clamp(scrollAmount, 0, maxScroll());

        // Need to format the text for the bottom info at this point to adjust the height of the background
        List<FormattedCharSequence> bottomInfoLines;

        if (!shiftInfo.isEmpty()) {
            bottomInfoLines = Minecraft.getInstance().font.split(Component.translatable(INFO_SHIFT).withStyle(ChatFormatting.DARK_GRAY), maxLineWidth - 10);
        } else {
            bottomInfoLines = new ArrayList<>();
        }

        // The effect header is meant to separate multiple effects from each other
        // Currently we append this header at a point in time where we don't know the actual order of the effects
        // That's why we use this check to skip the first header line
        boolean skipFirstLine = !lines.isEmpty() && isEffectHeader(lines.getFirst());

        // '65' is roughly the amount of space needed for the other components so that the name and level can be centered without overlap
        int backgroundWidth = Math.max(150, Minecraft.getInstance().font.width(name) + 65);
        List<FormattedCharSequence> description = Minecraft.getInstance().font.split(rawDescription, backgroundWidth - 7);

        int backgroundHeight = 20 + 27 + (description.size() + bottomInfoLines.size()) * 9;
        boolean hasShiftDown = Minecraft.getInstance().hasShiftDown();
        int sideWidth = hasShiftDown ? maxLineWidth : 15;
        int sideHeight = hasShiftDown ? 36 + Math.min(skipFirstLine ? lines.size() - 1 : lines.size(), MAX_SHOWN_LINES) * 9 : backgroundHeight - 10;

        ClientTooltipPositioner positioner = new AbilityTooltipPositioner(hasShiftDown ? sideWidth : 0);
        Vector2ic position = positioner.positionTooltip(graphics.guiWidth(), graphics.guiHeight(), x, y, maxLineWidth + 5, Math.max(sideHeight, backgroundHeight));

        int trueX = position.x();
        int trueY = position.y();

        if (!shiftInfo.isEmpty()) {
            blitWithBorder(graphics, BARS, trueX - (hasShiftDown ? maxLineWidth : 10), trueY + 3, 40, 20, sideWidth, sideHeight, 20, 20, 3);
            blitWithBorder(graphics, BARS, trueX - (hasShiftDown ? maxLineWidth : 10) + 3, trueY + 9, colorXPos, colorYPos, hasShiftDown ? maxLineWidth : 15, 20, 20, 20, 3);

            if (hasShiftDown) {
                graphics.text(Minecraft.getInstance().font, Component.translatable(LangKey.INFO), trueX - maxLineWidth + 10, trueY + 15, -1);
                int counter = 0;

                for (int line = scrollAmount; line < lines.size(); line++) {
                    FormattedCharSequence text = lines.get(line);

                    int startPosition = trueX - maxLineWidth + 5;
                    int textY = trueY + 5 + 28 + counter * 9;

                    if (line == 0 && skipFirstLine) {
                        continue;
                    }

                    if (isEffectHeader(text)) {
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EFFECT_HEADER, startPosition, textY - 4, maxLineWidth - 10, 9, DSColors.withAlpha(DSColors.GOLD, 1));
                    } else {
                        graphics.text(Minecraft.getInstance().font, text, startPosition, textY, DSColors.withAlpha(DSColors.GRAY, 1));
                    }

                    counter++;

                    if (counter == MAX_SHOWN_LINES) {
                        break;
                    }
                }
            }
        }

        blitWithBorder(graphics, BARS, trueX - 2, trueY - 4, 40, 20, backgroundWidth + 5, backgroundHeight, 20, 20, 3, 3, 3, 3);
        blitWithBorder(graphics, BARS, trueX, trueY + 3, colorXPos, colorYPos, backgroundWidth, 20, 20, 20, 3);
        blitWithBorder(graphics, BARS, trueX, trueY, 0, 100, 26, 26, 24, 24, 3);

        graphics.centeredText(Minecraft.getInstance().font, Component.translatable(headerTranslationKey), trueX + backgroundWidth / 2, trueY + 30, tooltipBackgroundColor);

        if (maxLevel > DragonAbilityInstance.MIN_LEVEL) {
            graphics.centeredText(Minecraft.getInstance().font, Component.empty().append(abilityLevel + "/" + maxLevel), trueX + backgroundWidth - 18, trueY + 9, -1);
            graphics.centeredText(Minecraft.getInstance().font, name, trueX + backgroundWidth / 2, trueY + 9, -1);
        } else {
            graphics.centeredText(Minecraft.getInstance().font, name, trueX + backgroundWidth / 2 + 10, trueY + 9, -1);
        }

        for (int line = 0; line < description.size(); line++) {
            graphics.text(Minecraft.getInstance().font, description.get(line), trueX + 5, trueY + 47 + line * 9, -5592406);
        }

        if (!bottomInfoLines.isEmpty()) {
            var font = Minecraft.getInstance().font;

            for (int i = 0; i < bottomInfoLines.size(); i++) {
                FormattedCharSequence line = bottomInfoLines.get(i);
                graphics.text(font, line, trueX + backgroundWidth / 2 - font.width(line) / 2, trueY + 47 + (description.size() + i) * 9, DSColors.withAlpha(DSColors.DARK_GRAY, 1));
            }
        }

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, trueX + 5, trueY + 5, 16, 16);
    }

    private static void blitWithBorder(final GuiGraphicsExtractor graphics, final Identifier texture, final int x, final int y, final int u, final int v, final int width, final int height, final int regionWidth, final int regionHeight, final int border) {
        blitWithBorder(graphics, texture, x, y, u, v, width, height, regionWidth, regionHeight, border, border, border, border);
    }

    private static void blitWithBorder(
            final GuiGraphicsExtractor graphics,
            final Identifier texture,
            final int x,
            final int y,
            final int u,
            final int v,
            final int width,
            final int height,
            final int regionWidth,
            final int regionHeight,
            final int leftBorder,
            final int rightBorder,
            final int topBorder,
            final int bottomBorder
    ) {
        int innerSourceWidth = Math.max(0, regionWidth - leftBorder - rightBorder);
        int innerSourceHeight = Math.max(0, regionHeight - topBorder - bottomBorder);
        int innerWidth = Math.max(0, width - leftBorder - rightBorder);
        int innerHeight = Math.max(0, height - topBorder - bottomBorder);

        blitRegion(graphics, texture, x, y, u, v, leftBorder, topBorder, leftBorder, topBorder);
        blitRegion(graphics, texture, x + width - rightBorder, y, u + regionWidth - rightBorder, v, rightBorder, topBorder, rightBorder, topBorder);
        blitRegion(graphics, texture, x, y + height - bottomBorder, u, v + regionHeight - bottomBorder, leftBorder, bottomBorder, leftBorder, bottomBorder);
        blitRegion(graphics, texture, x + width - rightBorder, y + height - bottomBorder, u + regionWidth - rightBorder, v + regionHeight - bottomBorder, rightBorder, bottomBorder, rightBorder, bottomBorder);

        blitRegion(graphics, texture, x + leftBorder, y, u + leftBorder, v, innerWidth, topBorder, innerSourceWidth, topBorder);
        blitRegion(graphics, texture, x + leftBorder, y + height - bottomBorder, u + leftBorder, v + regionHeight - bottomBorder, innerWidth, bottomBorder, innerSourceWidth, bottomBorder);
        blitRegion(graphics, texture, x, y + topBorder, u, v + topBorder, leftBorder, innerHeight, leftBorder, innerSourceHeight);
        blitRegion(graphics, texture, x + width - rightBorder, y + topBorder, u + regionWidth - rightBorder, v + topBorder, rightBorder, innerHeight, rightBorder, innerSourceHeight);
        blitRegion(graphics, texture, x + leftBorder, y + topBorder, u + leftBorder, v + topBorder, innerWidth, innerHeight, innerSourceWidth, innerSourceHeight);
    }

    private static void blitRegion(
            final GuiGraphicsExtractor graphics,
            final Identifier texture,
            final int x,
            final int y,
            final int u,
            final int v,
            final int width,
            final int height,
            final int sourceWidth,
            final int sourceHeight
    ) {
        if (width <= 0 || height <= 0 || sourceWidth <= 0 || sourceHeight <= 0) {
            return;
        }

        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, sourceWidth, sourceHeight, BARS_TEXTURE_SIZE, BARS_TEXTURE_SIZE);
    }

    private static boolean isEffectHeader(final FormattedCharSequence text) {
        StringBuilder builder = new StringBuilder();

        text.accept((charPosition, style, character) -> {
            builder.append(Character.toString(character));
            return true;
        });

        return builder.toString().equals(AbilityTargeting.EFFECT_HEADER);
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

    public static void drawAbilityTooltip(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, int x, int y, final DragonAbilityInstance ability, int scrollAmount) {
        int colorXPos = 0;
        int colorYPos = !ability.isPassive() ? 20 : 0;

        Component abilityDescription = Component.translatable(Translation.Type.ABILITY_DESCRIPTION.wrap(ability.identifier()));
        FormattedText rawDescription;

        if (!ability.isEnabled() && ability.isDisabled(true)) {
            rawDescription = Component.translatable(MANUALLY_DISABLED).append("\n\n").append(abilityDescription);
        } else {
            rawDescription = abilityDescription;
        }

        List<Component> info = ability.value().getInfo(Minecraft.getInstance().player, ability);
        UpgradeType<?> upgrade = ability.value().upgrade().orElse(null);

        if (upgrade != null && ability.level() < upgrade.maxLevel()) {
            MutableComponent upgradeDescription = upgrade.getDescription(ability.level() + 1);

            if (upgradeDescription.getContents() != PlainTextContents.EMPTY) {
                rawDescription = FormattedText.composite(rawDescription, Component.empty().append("\n\n"));
                rawDescription = FormattedText.composite(rawDescription, upgradeDescription.withColor(DSColors.GREEN));
            }
        }

        if (!info.isEmpty()) {
            rawDescription = FormattedText.composite(rawDescription, Component.empty().append("\n\n"));
        }

        int color = ability.isPassive() ? ARGB.color(1.0f, DSColors.PASSIVE_BACKGROUND) : ARGB.color(1.0f, DSColors.ACTIVE_BACKGROUND);
        drawTooltip(GuiGraphicsExtractor, x, y, info, rawDescription, colorXPos, colorYPos, ability.isPassive() ? LangKey.PASSIVE_ABILITY : LangKey.ACTIVE_ABILITY, ability.getName(), color, ability.getMaxLevel(), ability.level(), ability.getIcon(), scrollAmount);
    }

    public static void drawPenaltyTooltip(@NotNull final GuiGraphicsExtractor GuiGraphicsExtractor, int x, int y, final Holder<DragonPenalty> penalty) {
        int colorXPos = 20;
        int colorYPos = 0;

        //noinspection DataFlowIssue -> key is present
        FormattedText description = Component.translatable(Translation.Type.PENALTY_DESCRIPTION.wrap(penalty.getKey().identifier()));
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

        Component name = Component.translatable(Translation.Type.PENALTY.wrap(penalty.getKey().identifier()));
        Identifier icon = penalty.value().icon().orElse(MissingTextureAtlasSprite.getLocation());
        drawTooltip(GuiGraphicsExtractor, x, y, components, description, colorXPos, colorYPos, LangKey.PENALTY, name, ARGB.color(145, 46, 46), -1, -1, icon, 0);
    }
}
