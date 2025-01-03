package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.AbilityButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.LevelButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.ClickHoverButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.AbilityColumnsComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components.ScrollableComponent;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.UpgradeType;
import by.dragonsurvivalteam.dragonsurvival.util.ExperienceUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonAbilityScreen extends Screen {
    @Translation(comments = {
            "■ §6Active skills§r are used in combat.",
            "- §9Skill power§r scales off your current experience level. The higher your EXP level, the stronger your active skills.",
            "- §9Experience or mana§r points are used to cast spells.",
            "- §9Controls§r - check in-game Minecraft control settings! You can drag and drop skill icons around."
    })
    private static final String HELP_ACTIVE = Translation.Type.GUI.wrap("help.active_abilities");

    @Translation(comments = {
            "■ §aPassive skills§r are upgraded by spending experience levels.",
            "- §9Mana§r - do not forget use the Source of Magic and Dragons Treats for an infinite supply of mana!",
            "- §9More information§r can be found on our Wiki and in our Discord. Check the Curseforge mod page."
    })
    private static final String HELP_PASSIVE = Translation.Type.GUI.wrap("help.passive_abilities");

    @Translation(comments = {
            "■ §6Active skills§r are used in combat.",
            "- §9Skill power§r scales off your current experience level. The higher your EXP level, the stronger your active skills.",
            "- §9Experience or mana§r points are used to cast spells.",
            "- §9Controls§r - check in-game Minecraft control settings! You can drag and drop skill icons around.",
            "",
            "■ §aPassive skills§r are upgraded by spending experience levels.",
            "- §9Mana§r - do not forget use the Source of Magic and Dragons Treats for an infinite supply of mana!",
            "- §9More information§r can be found on our Wiki and in our Discord. Check the Curseforge mod page."
    })
    private static final String HELP_PASSIVE_ACTIVE = Translation.Type.GUI.wrap("help.passive_active_abilities");

    @Translation(comments = {
            "■ §dAbility assignment§r - drag and drop §6Active skills§r to the §9hotbar§r.",
            "- The §9hotbar§r is used to quickly access your active skills."
    })
    private static final String HELP_ABILITY_ASSIGNMENT = Translation.Type.GUI.wrap("help.ability_assignment");

    @Translation(comments = "■ §dInnate skills§r are a dragon's quirks, and represent the benefits and drawbacks of each dragon type.")
    private static final String HELP_INNATE = Translation.Type.GUI.wrap("help.innate_abilities");

    private static final ResourceLocation BACKGROUND_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/background_main.png");
    private static final ResourceLocation BACKGROUND_SIDE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/background_side.png");
    private static final ResourceLocation EXP_EMPTY = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/exp_empty.png");
    private static final ResourceLocation EXP_FULL = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/exp_full.png");
    private static final ResourceLocation LEFT_PANEL_ARROW_CLICK = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/addition_arrow_left_click.png");
    private static final ResourceLocation LEFT_PANEL_ARROW_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/addition_arrow_left_hover.png");
    private static final ResourceLocation LEFT_PANEL_ARROW_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/addition_arrow_left_main.png");
    private static final ResourceLocation INFO_HOVER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/info_hover.png");
    private static final ResourceLocation INFO_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/ability_screen/info_main.png");

    public Screen sourceScreen;
    public LevelButton hoveredLevelButton;

    private Holder<DragonSpecies> dragonSpecies;
    private int guiLeft;
    private int guiTop;

    private boolean leftWindowOpen;
    private final List<AbstractWidget> leftWindowWidgets = new ArrayList<>();
    private final List<ScrollableComponent> scrollableComponents = new ArrayList<>();

    public DragonAbilityScreen(Screen sourceScreen) {
        super(Component.empty());
        this.sourceScreen = sourceScreen;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        for (ScrollableComponent component : scrollableComponents) {
            component.scroll(mouseX, mouseY, scrollX, scrollY);
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft == null || minecraft.player == null) {
            return;
        }

        renderBlurredBackground(partialTick);

        int startX = guiLeft + 8;
        int startY = guiTop - 28;

        if (leftWindowOpen) {
            graphics.blit(BACKGROUND_SIDE, startX - 50, startY, 0, 0, 48, 203);
        }

        graphics.blit(BACKGROUND_MAIN, startX, startY, 0, 0, 256, 256);

        for (ScrollableComponent component : scrollableComponents) {
            component.update();
        }

        if (dragonSpecies != null) {
            // 'Player#experienceProgress' is somehow different from our calculation result causing the hover progress to not match up if we use it
            int totalExperience = ExperienceUtils.getTotalExperience(minecraft.player);
            float progress = (float) (totalExperience - ExperienceUtils.getTotalExperience(minecraft.player.experienceLevel)) / ExperienceUtils.getExperienceForLevelAfter(minecraft.player.experienceLevel);

            // Draw XP bars
            float leftExpBarProgress = Math.min(1f, Math.min(0.5f, progress) * 2);

            int barYPos = startY + 10;
            int leftBarX = startX + 10;
            int rightBarX = startX + 136;

            graphics.blit(EXP_EMPTY, leftBarX, barYPos, 0, 0, 93, 6, 93, 6);
            graphics.blit(EXP_EMPTY, rightBarX, barYPos, 0, 0, 93, 6, 93, 6);
            graphics.blit(EXP_FULL, leftBarX, barYPos, 0, 0, (int) (93 * leftExpBarProgress), 6, 93, 6);

            if (progress > 0.5) {
                float rightExpBarProgress = Math.min(1f, Math.min(0.5f, progress - 0.5f) * 2);
                graphics.blit(EXP_FULL, rightBarX, barYPos, 0, 0, (int) (93 * rightExpBarProgress), 6, 93, 6);
            }

            if (true) { // FIXME :: for comparison (will be removed later)
                graphics.blit(EXP_FULL, leftBarX, barYPos + 8, 0, 0, (int) (93 * Math.min(1f, Math.min(0.5f, minecraft.player.experienceProgress) * 2)), 6, 93, 6);

                if (minecraft.player.experienceProgress > 0.5) {
                    float rightExpBarProgress = Math.min(1f, Math.min(0.5f, minecraft.player.experienceProgress - 0.5f) * 2);
                    graphics.blit(EXP_FULL, rightBarX, barYPos + 8, 0, 0, (int) (93 * rightExpBarProgress), 6, 93, 6);
                }
            }

            int experienceModification;

            if (hoveredLevelButton == null || !hoveredLevelButton.canModifyLevel()) {
                experienceModification = 0;
            } else {
                experienceModification = hoveredLevelButton.getExperienceModification();
            }

            int newExperience = totalExperience + experienceModification;
            int newLevel = Math.max(0, ExperienceUtils.getLevel(newExperience));

            if (experienceModification != 0) {
                // Used to show the new experience progress of the new level
                // The level difference itself is shown through the rendered level number
                float hoverProgress = (float) (newExperience - ExperienceUtils.getTotalExperience(newLevel)) / ExperienceUtils.getExperienceForLevelAfter(newLevel);
                float leftExpBarHoverProgress = Math.min(0.5f, hoverProgress) * 2;
                float rightExpBarHoverProgress = Math.min(0.5f, hoverProgress - leftExpBarHoverProgress / 2) * 2;

                if (experienceModification < 0) {
                    graphics.setColor(1, 0, 0, 1);
                } else {
                    graphics.setColor(0.6f, 0.2f, 0.85f, 1);
                }

                drawExperienceBar(graphics, barYPos, leftBarX, leftExpBarHoverProgress);

                if (rightExpBarHoverProgress > 0) {
                    drawExperienceBar(graphics, barYPos, rightBarX, rightExpBarHoverProgress);
                }

                graphics.setColor(1, 1, 1, 1);
            }

            int greenFontColor = 0x57882F;
            int redFontColor = 0xE4472F; // TODO :: use darker color
            // TODO :: add neutral color for no change
            int color = experienceModification < 0 ? redFontColor : greenFontColor;

            Component expectedLevel = Component.literal(String.valueOf(newLevel)).withColor(color);

            int expLevelXPos = ((rightBarX + leftBarX) / 2 + 48 - minecraft.font.width(expectedLevel) / 2) - 1;
            int expLevelYPos = barYPos - 1;
            graphics.drawString(minecraft.font, expectedLevel, expLevelXPos, expLevelYPos, 0, false);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawExperienceBar(final GuiGraphics guiGraphics, int y, int initialX, float hoverProgress) {
        guiGraphics.blit(EXP_FULL, initialX, y, 0, 0, (int) (93 * hoverProgress), 6, 93, 6);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        // Don't render the vanilla background, it darkens the UI in an undesirable way
    }

    @Override
    public void init() {
        int xSize = 256;
        int ySize = 256;

        guiLeft = (width - xSize) / 2;
        guiTop = (height - ySize / 2) / 2;
        leftWindowOpen = true;

        int startX = guiLeft - 2;
        int startY = guiTop + 2;

        //Inventory
        TabButton.addTabButtonsToScreen(this, startX + 17, startY - 56, TabButton.Type.ABILITY_TAB);

        //noinspection DataFlowIssue -> player is present
        MagicData data = MagicData.getData(minecraft.player);
        // TODO :: need some consistent order - could do it based on a tag (that is what enchantments and dragon bodies are doing)
        //  or on some other factors (level -> name e.g.)
        List<DragonAbilityInstance> actives = data.getActiveAbilities();
        List<DragonAbilityInstance> upgradablePassives = data.getPassiveAbilities(UpgradeType.IS_MANUAL);
        List<DragonAbilityInstance> constantPassives = data.getPassiveAbilities(UpgradeType.IS_MANUAL.negate());

        scrollableComponents.add(new AbilityColumnsComponent(this, guiLeft + 35, guiTop, 40, 20, 0.8f, 0.5f, actives));
        scrollableComponents.add(new AbilityColumnsComponent(this, guiLeft + 111, guiTop, 40, 20, 0.8f, 0.5f, upgradablePassives));
        scrollableComponents.add(new AbilityColumnsComponent(this,guiLeft + 186, guiTop, 40, 20, 0.8f, 0.5f, constantPassives));

        // Left panel (hotbar)
        for (int i = 0; i < MagicData.HOTBAR_SLOTS; i++) {
            AbstractWidget widget = new AbilityButton(guiLeft - 35, guiTop + i * 40, data.fromSlot(i), this, true, i);
            addRenderableWidget(widget);
            leftWindowWidgets.add(widget);
            widget.visible = leftWindowOpen;
        }

        AbstractWidget leftHelpButton = new HelpButton(guiLeft - 24, startY - 25, 13, 13, HELP_ABILITY_ASSIGNMENT, INFO_MAIN, INFO_HOVER);
        addRenderableWidget(leftHelpButton);
        leftWindowWidgets.add(leftHelpButton);
        leftHelpButton.visible = leftWindowOpen;

        addRenderableWidget(new ClickHoverButton(guiLeft, guiTop + 69, 10, 17, 0, 1, 18, 18, Component.empty(), button -> {
            leftWindowOpen = !leftWindowOpen;
            for(AbstractWidget widget : leftWindowWidgets) {
                widget.visible = leftWindowOpen;
            }
        }, LEFT_PANEL_ARROW_CLICK, LEFT_PANEL_ARROW_HOVER, LEFT_PANEL_ARROW_MAIN));

        addRenderableWidget(new HelpButton(guiLeft + 122, startY + 263 / 2 + 25, 12, 12, HELP_PASSIVE_ACTIVE));
    }


    @Override
    public void tick() {
        //noinspection DataFlowIssue -> players should be present
        DragonStateHandler data = DragonStateProvider.getData(minecraft.player);

        if (dragonSpecies != data.species()) {
            dragonSpecies = data.species();
            clearWidgets();
            init();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}