package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.MagicHUD;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.*;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenDragonInventory;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenInventory;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class TabButton extends Button {
    public enum Type {
        @Translation(type = Translation.Type.GUI, comments = "Inventory")
        INVENTORY_TAB,
        @Translation(type = Translation.Type.GUI, comments = "Abilities")
        ABILITY_TAB,
        @Translation(type = Translation.Type.GUI, comments = "Species Info")
        SPECIES_TAB,
        @Translation(type = Translation.Type.GUI, comments = "Skin customization")
        SKINS_TAB,
        @Translation(type = Translation.Type.GUI, comments = "Emotes")
        EMOTES_TAB
    }

    private final Type type;
    private final Screen parent;

    public TabButton(int x, int y, final Type type, final Screen parent) {
        super(x, y, 28, 32, Component.empty(), action -> { /* Nothing to do */ }, DEFAULT_NARRATION);
        this.type = type;
        this.parent = parent;

        setTooltip(Tooltip.create(Component.translatable(Translation.Type.GUI.wrap(type.toString().toLowerCase(Locale.ENGLISH)))));
    }

    private boolean setInventoryScreen(Screen sourceScreen) {
        if (sourceScreen instanceof InventoryScreen) {
            //noinspection DataFlowIssue -> player is present
            Minecraft.getInstance().setScreen(new InventoryScreen(Minecraft.getInstance().player));
            PacketDistributor.sendToServer(new RequestOpenInventory.Data());
            return true;
        } else if (sourceScreen instanceof DragonInventoryScreen) {
            RequestOpenDragonInventory.SendOpenDragonInventoryAndMaintainCursorPosition();
            return true;
        }

        return false;
    }

    @Override
    public void onPress() {
        if (isCurrent()) {
            return;
        }

        switch (type) {
            case INVENTORY_TAB -> {
                boolean setSuccessfully = false;

                if (parent instanceof DragonAbilityScreen abilityScreen && abilityScreen.sourceScreen != null) {
                    setSuccessfully = setInventoryScreen(abilityScreen.sourceScreen);
                } else if (parent instanceof DragonSkinsScreen skinsScreen && skinsScreen.sourceScreen != null) {
                    setSuccessfully = setInventoryScreen(skinsScreen.sourceScreen);
                }

                if (setSuccessfully) {
                    return;
                }

                if (InventoryScreenHandler.dragonInventory) {
                    RequestOpenDragonInventory.SendOpenDragonInventoryAndMaintainCursorPosition();
                } else {
                    //noinspection DataFlowIssue -> player is present
                    Minecraft.getInstance().setScreen(new InventoryScreen(Minecraft.getInstance().player));
                    PacketDistributor.sendToServer(new RequestOpenInventory.Data());
                }
            }
            case ABILITY_TAB -> Minecraft.getInstance().setScreen(new DragonAbilityScreen(parent));
            case SKINS_TAB -> Minecraft.getInstance().setScreen(new DragonSkinsScreen(parent));
            case SPECIES_TAB -> Minecraft.getInstance().setScreen(new DragonSpeciesScreen());
            case EMOTES_TAB -> Minecraft.getInstance().setScreen(new DragonEmoteScreen());
        }
    }

    public boolean isCurrent() {
        return switch (type) {
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
            guiGraphics.blit(MagicHUD.WIDGET_TEXTURES, getX() + 2, getY() + 2 + (isCurrent() ? 2 : 0), type.ordinal() * 24, 67, 24, 24);
        } else {
            guiGraphics.blit(MagicHUD.WIDGET_TEXTURES, getX() + 2, getY() + 2 + (isCurrent() ? 2 : 0), type.ordinal() * 24, 41, 24, 24);
        }
    }

    public static void addTabButtonsToScreen(Screen screen, int offsetX, int offsetY, TabButton.Type typeSelected) {
        for(int i = 0; i < Type.values().length; i++) {
            Type type = Type.values()[i];
            if (type == typeSelected) {
                ((ScreenAccessor)screen).dragonSurvival$addRenderableWidget(new TabButton(offsetX + 1 + (i * 28), offsetY - 2, type, screen));
            } else {
                ((ScreenAccessor)screen).dragonSurvival$addRenderableWidget(new TabButton(offsetX + (i * 28), offsetY, type, screen));
            }
        }
    }
}