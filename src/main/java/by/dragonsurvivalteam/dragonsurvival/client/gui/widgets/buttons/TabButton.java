package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.MagicHUD;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.AbilityScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.InventoryScreenHandler;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.SkinsScreen;
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

import static by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenDragonInventory.SendOpenDragonInventoryAndMaintainCursorPosition;


public class TabButton extends Button {
    public enum Type {
        @Translation(type = Translation.Type.GUI, comments = "Inventory")
        INVENTORY_TAB,
        @Translation(type = Translation.Type.GUI, comments = "Abilities")
        ABILITY_TAB,
        @Translation(type = Translation.Type.GUI, comments = "If you have any questions about Dragon Survival, visit the wiki on Github!")
        GITHUB_REMINDER_TAB,
        @Translation(type = Translation.Type.GUI, comments = "Skin customization")
        SKINS_TAB;
    }

    private final Type type;
    private final Screen parent;

    public TabButton(int x, int y, Type type, Screen parent) {
        super(x, y, 28, 32, Component.empty(), action -> { /* Nothing to do */ }, DEFAULT_NARRATION);
        this.type = type;
        this.parent = parent;

        setTooltip(Tooltip.create(Component.translatable(Translation.Type.GUI.wrap(type.toString().toLowerCase(Locale.ENGLISH)))));
    }

    private boolean setInventoryScreen(Screen sourceScreen) {
        if (sourceScreen instanceof InventoryScreen) {
            Minecraft.getInstance().setScreen(new InventoryScreen(Minecraft.getInstance().player));
            PacketDistributor.sendToServer(new RequestOpenInventory.Data());
            return true;
        } else if (sourceScreen instanceof DragonInventoryScreen) {
            SendOpenDragonInventoryAndMaintainCursorPosition();
            return true;
        }

        return false;
    }

    @Override
    public void onPress() {
        if (!isCurrent())
            switch (type) {
                case INVENTORY_TAB -> {
                    boolean setSuccessfully = false;
                    if (parent instanceof AbilityScreen) {
                        if (((AbilityScreen) parent).sourceScreen != null) {
                            setSuccessfully = setInventoryScreen(((AbilityScreen) parent).sourceScreen);
                        }
                    } else if (parent instanceof SkinsScreen) {
                        if (((SkinsScreen) parent).sourceScreen != null) {
                            setSuccessfully = setInventoryScreen(((SkinsScreen) parent).sourceScreen);
                        }
                    }

                    if (!setSuccessfully) {
                        if (InventoryScreenHandler.dragonInventory) {
                            SendOpenDragonInventoryAndMaintainCursorPosition();
                        } else {
                            Minecraft.getInstance().setScreen(new InventoryScreen(Minecraft.getInstance().player));
                            PacketDistributor.sendToServer(new RequestOpenInventory.Data());
                        }
                    }
                }
                case ABILITY_TAB -> Minecraft.getInstance().setScreen(new AbilityScreen(parent));
                case SKINS_TAB -> Minecraft.getInstance().setScreen(new SkinsScreen(parent));
            }
    }

    public boolean isCurrent() {
        return switch (type) {
            case INVENTORY_TAB -> parent instanceof DragonInventoryScreen || parent instanceof InventoryScreen;
            case ABILITY_TAB -> parent instanceof AbilityScreen;
            case SKINS_TAB -> parent instanceof SkinsScreen;
            default -> false;
        };
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float p_230431_4_) {
        if (isCurrent()) {
            guiGraphics.blit(MagicHUD.WIDGET_TEXTURES, getX(), getY(), type == Type.INVENTORY_TAB ? 0 : 28, 0, 28, 32);
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
}