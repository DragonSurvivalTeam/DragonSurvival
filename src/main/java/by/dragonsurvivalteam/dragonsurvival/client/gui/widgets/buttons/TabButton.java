package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.MagicHUD;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.*;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class TabButton extends Button {
    public enum TabButtonType {
        @Translation(comments = "Dragon Inventory")
        INVENTORY_TAB,
        @Translation(comments = "Abilities Info")
        ABILITY_TAB,
        @Translation(comments = "Species Info")
        SPECIES_TAB,
        @Translation(comments = "Skin Info")
        SKINS_TAB,
        @Translation(comments = "Emotes")
        EMOTES_TAB
    }

    private final TabButtonType tabButtonType;
    private final Screen parent;

    public TabButton(int x, int y, final TabButtonType tabButton, final Screen parent) {
        super(x, y, 28, 32, Component.empty(), action -> { /* Nothing to do */ }, DEFAULT_NARRATION);
        this.tabButtonType = tabButton;
        this.parent = parent;

        setTooltip(Tooltip.create(DSLanguageProvider.enumValue(tabButton)));
    }

    @Override
    public void onPress() {
        if (isCurrent()) {
            return;
        }

        switch (tabButtonType) {
            case INVENTORY_TAB -> InventoryScreenHandler.openDragonInventory();
            case ABILITY_TAB -> Minecraft.getInstance().setScreen(new DragonAbilityScreen());
            case SKINS_TAB -> Minecraft.getInstance().setScreen(new DragonSkinsScreen());
            case SPECIES_TAB -> Minecraft.getInstance().setScreen(new DragonSpeciesScreen());
            case EMOTES_TAB -> Minecraft.getInstance().setScreen(new DragonEmoteScreen());
        }
    }

    public boolean isCurrent() {
        return switch (tabButtonType) {
            case INVENTORY_TAB -> parent instanceof DragonInventoryScreen || parent instanceof InventoryScreen;
            case ABILITY_TAB -> parent instanceof DragonAbilityScreen;
            case SKINS_TAB -> parent instanceof DragonSkinsScreen;
            case SPECIES_TAB -> parent instanceof DragonSpeciesScreen;
            case EMOTES_TAB -> parent instanceof DragonEmoteScreen;
        };
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float p_230431_4_) {
        if (isCurrent()) {
            guiGraphics.blit(MagicHUD.WIDGET_TEXTURES, getX(), getY(), 28, 0, 28, 32);
        } else if (isHovered()) {
            guiGraphics.blit(MagicHUD.WIDGET_TEXTURES, getX(), getY(), 84, 0, 28, 32);
        } else {
            guiGraphics.blit(MagicHUD.WIDGET_TEXTURES, getX(), getY(), 56, 0, 28, 32);
        }

        if (isHovered() || isCurrent()) {
            guiGraphics.blit(MagicHUD.WIDGET_TEXTURES, getX() + 2, getY() + 2 + (isCurrent() ? 2 : 0), tabButtonType.ordinal() * 24, 67, 24, 24);
        } else {
            guiGraphics.blit(MagicHUD.WIDGET_TEXTURES, getX() + 2, getY() + 2 + (isCurrent() ? 2 : 0), tabButtonType.ordinal() * 24, 41, 24, 24);
        }
    }

    public static void addTabButtonsToScreen(final Screen screen, int offsetX, int offsetY, final TabButtonType selectedButton) {
        boolean ignoreAbilityTab = false;

        if(Minecraft.getInstance().player.getData(DSDataAttachments.MAGIC).getAbilities().isEmpty()) {
            ignoreAbilityTab = true;
        }

        if(ignoreAbilityTab) {
            for(int i = 0; i < TabButtonType.values().length; i++) {
                TabButtonType tabButton = TabButtonType.values()[i];

                if(tabButton == TabButtonType.ABILITY_TAB) {
                    continue;
                }

                int additionalOffset = 0;
                if(tabButton.ordinal() > TabButtonType.ABILITY_TAB.ordinal()) {
                    additionalOffset = -28;
                }

                if(tabButton == selectedButton) {
                    ((ScreenAccessor)screen).dragonSurvival$addRenderableWidget(new TabButton(offsetX + additionalOffset + 1 + (i * 28), offsetY - 2, tabButton, screen));
                } else {
                    ((ScreenAccessor)screen).dragonSurvival$addRenderableWidget(new TabButton(offsetX + additionalOffset + (i * 28), offsetY, tabButton, screen));
                }
            }
        } else {
            for(int i = 0; i < TabButtonType.values().length; i++) {
                TabButtonType tabButton = TabButtonType.values()[i];

                if(tabButton == selectedButton) {
                    ((ScreenAccessor)screen).dragonSurvival$addRenderableWidget(new TabButton(offsetX + 1 + (i * 28), offsetY - 2, tabButton, screen));
                } else {
                    ((ScreenAccessor)screen).dragonSurvival$addRenderableWidget(new TabButton(offsetX + (i * 28), offsetY, tabButton, screen));
                }
            }
        }
    }
}