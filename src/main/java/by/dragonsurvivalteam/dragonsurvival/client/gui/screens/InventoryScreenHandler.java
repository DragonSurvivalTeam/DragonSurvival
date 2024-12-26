package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.input.Keybind;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenDragonInventory;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AltarData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;
import static by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenDragonInventory.SendOpenDragonInventoryAndMaintainCursorPosition;

@EventBusSubscriber(Dist.CLIENT)
public class InventoryScreenHandler {
    @Translation(type = Translation.Type.MISC, comments = "Open dragon inventory screen")
    private static final String TOGGLE_DRAGON_INVENTORY = Translation.Type.GUI.wrap("inventory.toggle_dragon_inventory");

    @Translation(type = Translation.Type.MISC, comments = "Open dragon altar")
    private static final String TOGGLE_DRAGON_ALTAR = Translation.Type.GUI.wrap("inventory.toggle_dragon_altar");

    @Translation(key = "dragon_inventory", type = Translation.Type.CONFIGURATION, comments = "If enabled the default inventory is replaced with a custom inventory")
    @ConfigOption(side = ConfigSide.CLIENT, category = "inventory", key = "dragon_inventory")
    public static Boolean dragonInventory = true;

    @Translation(key = "dragon_tabs", type = Translation.Type.CONFIGURATION, comments = "If enabled tabs will be added to the vanilla inventory")
    @ConfigOption(side = ConfigSide.CLIENT, category = "inventory", key = "dragon_tabs")
    public static Boolean dragonTabs = true;

    @Translation(key = "inventory_toggle", type = Translation.Type.CONFIGURATION, comments = "If enabled there will be a button that lets you switch between the custom and vanilla inventory")
    @ConfigOption(side = ConfigSide.CLIENT, category = "inventory", key = "inventory_toggle")
    public static Boolean inventoryToggle = true;

    private static final ResourceLocation DS_LOGO = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui_new/ds_logo.png");

    private static ExtendedButton altarOpenButton;
    private static ExtendedButton creativeModeDragonInventoryButton;

    @SubscribeEvent
    public static void onOpenScreen(ScreenEvent.Opening openEvent) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (!dragonInventory) {
            return;
        }
        if (Minecraft.getInstance().screen != null) {
            return;
        }
        if (player == null || player.isCreative() || !DragonStateProvider.isDragon(player)) {
            return;
        }

        if (openEvent.getScreen() instanceof InventoryScreen) {
            openEvent.setCanceled(true);
            PacketDistributor.sendToServer(new RequestOpenDragonInventory.Data());
        }
    }

    @SubscribeEvent
    public static void removeCraftingButtonInOtherCreativeModeTabs(ScreenEvent.Render.Pre renderEvent) {
        if (renderEvent.getScreen() instanceof CreativeModeInventoryScreen screen) {
            if (creativeModeDragonInventoryButton != null) {
                creativeModeDragonInventoryButton.visible = screen.isInventoryOpen();
            }
        }
    }

    @SubscribeEvent
    public static void hideOrShowAltarButton(ScreenEvent.Render.Pre renderEvent) {
        Screen screen = renderEvent.getScreen();
        if (screen instanceof InventoryScreen) {
            Player player = Minecraft.getInstance().player;
            AltarData data = AltarData.getData(player);
            if (altarOpenButton == null) {
                altarOpenButton.visible = !data.hasUsedAltar;
            }
        }
    }

    @SubscribeEvent
    public static void addCraftingButton(ScreenEvent.Init.Post initGuiEvent) {
        Screen sc = initGuiEvent.getScreen();

        if (sc instanceof InventoryScreen screen) {
            if (ServerConfig.allowDragonChoiceFromInventory) {
                altarOpenButton = new ExtendedButton(screen.getGuiLeft() + 138, screen.height / 2 - 32, 32, 32, Component.empty(), p_onPress_1_ -> {
                    ClientProxy.handleOpenDragonAltar();
                }) {
                    @Override
                    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        guiGraphics.blit(DS_LOGO, getX(), getY(), 0, 0, 32, 32, 32, 32);
                    }
                };
                altarOpenButton.setTooltip(Tooltip.create(Component.translatable(TOGGLE_DRAGON_ALTAR)));
                initGuiEvent.addListener(altarOpenButton);
            }
        }

        if (!DragonStateProvider.isDragon(Minecraft.getInstance().player)) {
            return;
        }

        // Dragon only UI
        if (sc instanceof InventoryScreen screen) {
            if (dragonTabs) {
                initGuiEvent.addListener(new TabButton(screen.getGuiLeft(), screen.getGuiTop() - 28, TabButton.Type.INVENTORY_TAB, screen));
                initGuiEvent.addListener(new TabButton(screen.getGuiLeft() + 28, screen.getGuiTop() - 26, TabButton.Type.ABILITY_TAB, screen));
                initGuiEvent.addListener(new TabButton(screen.getGuiLeft() + 57, screen.getGuiTop() - 26, TabButton.Type.GITHUB_REMINDER_TAB, screen));
                initGuiEvent.addListener(new TabButton(screen.getGuiLeft() + 86, screen.getGuiTop() - 26, TabButton.Type.SKINS_TAB, screen));
            }

            if (inventoryToggle) {
                ExtendedButton inventoryToggle = new ExtendedButton(screen.getGuiLeft() + 128, screen.height / 2 - 22, 20, 18, Component.empty(), p_onPress_1_ -> {
                    SendOpenDragonInventoryAndMaintainCursorPosition();
                }) {
                    @Override
                    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        float u = 21f;
                        float v = isHoveredOrFocused() ? 20f : 0f;
                        guiGraphics.blit(DragonInventoryScreen.INVENTORY_TOGGLE_BUTTON, getX(), getY(), u, v, 20, 18, 256, 256);
                    }
                };
                inventoryToggle.setTooltip(Tooltip.create(Component.translatable(TOGGLE_DRAGON_INVENTORY)));
                initGuiEvent.addListener(inventoryToggle);
            }
        }

        if (sc instanceof CreativeModeInventoryScreen screen) {
            if (inventoryToggle) {
                creativeModeDragonInventoryButton = new ExtendedButton(screen.getGuiLeft() + 128 + 20, screen.height / 2 - 50, 20, 18, Component.empty(), p_onPress_1_ -> {
                    SendOpenDragonInventoryAndMaintainCursorPosition();
                }) {
                    @Override
                    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        float u = 21f;
                        float v = isHoveredOrFocused() ? 20f : 0f;
                        guiGraphics.blit(DragonInventoryScreen.INVENTORY_TOGGLE_BUTTON, getX(), getY(), u, v, 20, 18, 256, 256);
                    }
                };
                creativeModeDragonInventoryButton.setTooltip(Tooltip.create(Component.translatable(TOGGLE_DRAGON_INVENTORY)));
                initGuiEvent.addListener(creativeModeDragonInventoryButton);
            }
        }
    }

    @SubscribeEvent
    public static void handleKey(final InputEvent.Key event) {
        if (!Keybind.DRAGON_INVENTORY.isKey(event.getKey()) || event.getAction() != Keybind.KEY_PRESSED) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;

        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        Screen screen = Minecraft.getInstance().screen;

        if (screen == null) {
            PacketDistributor.sendToServer(new RequestOpenDragonInventory.Data());
        } else if (screen instanceof DragonInventoryScreen || screen instanceof AbilityScreen || screen instanceof SkinsScreen) {
            player.closeContainer();
        }
    }
}
