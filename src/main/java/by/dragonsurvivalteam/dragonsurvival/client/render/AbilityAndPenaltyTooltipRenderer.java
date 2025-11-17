package by.dragonsurvivalteam.dragonsurvival.client.render;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.AbilityTooltipPositioner;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.targeting.AbilityTargeting;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.UpgradeType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
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

public class AbilityAndPenaltyTooltipRenderer {
    @Translation(comments = "Hold 'Shift' for info")
    private static final String INFO_SHIFT = Translation.Type.GUI.wrap("general.info_shift");

    @Translation(comments = "§4Manually disabled§r")
    private static final String MANUALLY_DISABLED = Translation.Type.GUI.wrap("general.manually_disabled");

    private static final ResourceLocation EFFECT_HEADER = DragonSurvival.res("ability_effect_header");
    private static final ResourceLocation BARS = DragonSurvival.res("textures/gui/widget_bars.png");

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
            final FormattedText rawDescription,
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

        boolean skipFirstLine = !lines.isEmpty() && isEffectHeader(lines.getFirst());

        // '65' is roughly the amount of space needed for the other components so that the name and level can be centered without overlap
        int backgroundWidth = Math.max(150, Minecraft.getInstance().font.width(name) + 65);
        List<FormattedCharSequence> description = Minecraft.getInstance().font.split(rawDescription, backgroundWidth - 7);

        int backgroundHeight = 35 + 24 + description.size() * 9;
        int sideWidth = Screen.hasShiftDown() ? maxLineWidth : 15;
        int sideHeight = Screen.hasShiftDown() ? 36 + Math.min(skipFirstLine ? lines.size() - 1 : lines.size(), MAX_SHOWN_LINES) * 9 : backgroundHeight - 10;

        ClientTooltipPositioner positioner = new AbilityTooltipPositioner(Screen.hasShiftDown() ? sideWidth : 0);
        Vector2ic position = positioner.positionTooltip(graphics.guiWidth(), graphics.guiHeight(), x, y, maxLineWidth + 5, Math.max(sideHeight, backgroundHeight));

        int trueX = position.x();
        int trueY = position.y();

        if (!shiftInfo.isEmpty()) {
            // Backing for info tab
            graphics.blitWithBorder(BARS, trueX - (Screen.hasShiftDown() ? maxLineWidth : 10), trueY + 3, 40, 20, sideWidth, sideHeight, 20, 20, 3);
            // Top bar for info tab
            graphics.blitWithBorder(BARS, trueX - (Screen.hasShiftDown() ? maxLineWidth : 10) + 3, trueY + 9, colorXPos, colorYPos, Screen.hasShiftDown() ? maxLineWidth : 15, 20, 20, 20, 3);

            if (Screen.hasShiftDown()) {
                graphics.drawString(Minecraft.getInstance().font, Component.translatable(LangKey.INFO), trueX - maxLineWidth + 10, trueY + 15, -1);
                int counter = 0;

                for (int line = scrollAmount; line < lines.size(); line++) {
                    FormattedCharSequence text = lines.get(line);

                    int startPosition = trueX - maxLineWidth + 5;
                    int textY = trueY + 5 + 28 + counter * 9;

                    if (line == 0 && skipFirstLine) {
                        continue;
                    }

                    if (isEffectHeader(text)) {
                        RenderingUtils.setShaderColor(DSColors.withAlpha(DSColors.GOLD, 1));
                        graphics.blitSprite(EFFECT_HEADER, startPosition, textY - 4, maxLineWidth - 10, 9);
                        RenderSystem.setShaderColor(1, 1, 1, 1);
                    } else {
                        graphics.drawString(Minecraft.getInstance().font, text, startPosition, textY, DSColors.GRAY);
                    }

                    counter++;

                    if (counter == MAX_SHOWN_LINES) {
                        break;
                    }
                }
            }
        }

        // Background of the main description
        graphics.blitWithBorder(BARS, trueX - 2, trueY - 4, 40, 20, backgroundWidth + 5, backgroundHeight, 20, 20, 3, 3, 3, 3);
        // Top bar of the main description
        graphics.blitWithBorder(BARS, trueX, trueY + 3, colorXPos, colorYPos, backgroundWidth, 20, 20, 20, 3);
        // Backing square for ability icon
        graphics.blitWithBorder(BARS, trueX, trueY, 0, 100, 26, 26, 24, 24, 3);

        graphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable(headerTranslationKey), trueX + backgroundWidth / 2, trueY + 30, tooltipBackgroundColor.getColor());

        if (maxLevel > DragonAbilityInstance.MIN_LEVEL) {
            graphics.drawCenteredString(Minecraft.getInstance().font, Component.empty().append(abilityLevel + "/" + maxLevel), trueX + backgroundWidth - 18, trueY + 9, -1);
            graphics.drawCenteredString(Minecraft.getInstance().font, name, trueX + backgroundWidth / 2, trueY + 9, -1);
        } else {
            graphics.drawCenteredString(Minecraft.getInstance().font, name, trueX + backgroundWidth / 2 + 10, trueY + 9, -1);
        }

        for (int line = 0; line < description.size(); line++) {
            graphics.drawString(Minecraft.getInstance().font, description.get(line), trueX + 5, trueY + 47 + line * 9, -5592406);
        }

        if (!shiftInfo.isEmpty()) {
            graphics.drawCenteredString(Minecraft.getInstance().font, Component.translatable(INFO_SHIFT).withStyle(ChatFormatting.DARK_GRAY), trueX + backgroundWidth / 2, trueY + 47 + (description.size() - 1) * 9, 0);
        }

        graphics.blitSprite(icon, trueX + 5, trueY + 5, 16, 16);
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

    public static void drawAbilityTooltip(@NotNull final GuiGraphics guiGraphics, int x, int y, final DragonAbilityInstance ability, int scrollAmount) {
        int colorXPos = 0;
        int colorYPos = !ability.isPassive() ? 20 : 0;

        Component abilityDescription = Component.translatable(Translation.Type.ABILITY_DESCRIPTION.wrap(ability.location()));
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
                rawDescription = FormattedText.composite(rawDescription, upgradeDescription.withColor(Color.GREEN.getColor()));
            }
        }

        if (!info.isEmpty()) {
            rawDescription = FormattedText.composite(rawDescription, Component.empty().append("\n\n"));
        }

        Color color = ability.isPassive() ? new Color(DSColors.withAlpha(DSColors.PASSIVE_BACKGROUND, 1f)) : new Color(DSColors.withAlpha(DSColors.ACTIVE_BACKGROUND, 1f));
        drawTooltip(guiGraphics, x, y, info, rawDescription, colorXPos, colorYPos, ability.isPassive() ? LangKey.PASSIVE_ABILITY : LangKey.ACTIVE_ABILITY, ability.getName(), color, ability.getMaxLevel(), ability.level(), ability.getIcon(), scrollAmount);
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

        Component name = Component.translatable(Translation.Type.PENALTY.wrap(penalty.getKey().location()));
        ResourceLocation icon = penalty.value().icon().orElse(MissingTextureAtlasSprite.getLocation());
        drawTooltip(guiGraphics, x, y, components, description, colorXPos, colorYPos, LangKey.PENALTY, name, Color.ofRGB(145, 46, 46), -1, -1, icon, 0);
    }
}