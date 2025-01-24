package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.input.Keybind;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawMenuToggle;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenDragonInventory;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AltarData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.blaze3d.platform.InputConstants;
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

import java.util.Objects;

@EventBusSubscriber(Dist.CLIENT)
public class InventoryScreenHandler {
    @Translation(comments = "Open dragon inventory screen")
    private static final String TOGGLE_DRAGON_INVENTORY = Translation.Type.GUI.wrap("inventory.toggle_dragon_inventory");

    @Translation(comments = "Open dragon altar")
    private static final String TOGGLE_DRAGON_ALTAR = Translation.Type.GUI.wrap("inventory.toggle_dragon_altar");

    @Translation(key = "dragon_inventory", type = Translation.Type.CONFIGURATION, comments = "If enabled the default inventory is replaced with a custom inventory")
    @ConfigOption(side = ConfigSide.CLIENT, category = "inventory", key = "dragon_inventory")
    public static Boolean dragonInventory = true;

    @Translation(key = "inventory_toggle", type = Translation.Type.CONFIGURATION, comments = "If enabled there will be a button that lets you switch between the custom and vanilla inventory")
    @ConfigOption(side = ConfigSide.CLIENT, category = "inventory", key = "inventory_toggle")
    public static Boolean inventoryToggle = true;

    private static final ResourceLocation DS_LOGO = DragonSurvival.res("textures/gui/ds_logo.png");
    public static final ResourceLocation INVENTORY_TOGGLE_BUTTON = DragonSurvival.res("textures/gui/inventory_button.png");

    private static ExtendedButton altarOpenButton;
    private static ExtendedButton dragonInventoryButton;
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
            PacketDistributor.sendToServer(RequestOpenDragonInventory.INSTANCE);
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
    public static void hideOrShowAltarButton(final ScreenEvent.Render.Pre event) {
        if (altarOpenButton != null && event.getScreen() instanceof InventoryScreen) {
            Player player = Minecraft.getInstance().player;
            //noinspection DataFlowIssue -> player is present
            AltarData data = AltarData.getData(player);
            altarOpenButton.visible = !data.hasUsedAltar && !DragonStateProvider.isDragon(player);
        }
    }



    // We need to constantly update the position of these buttons in case the crafting tab gets opened and causes the screen to shift
    @SubscribeEvent
    public static void moveButtonsIfCraftingTabIsOpened(ScreenEvent.Render.Pre renderEvent) {
        if (renderEvent.getScreen() instanceof InventoryScreen screen) {
            if (altarOpenButton != null) {
                altarOpenButton.setPosition(screen.getGuiLeft() + 138, screen.height / 2 - 32);
            }
            if (dragonInventoryButton != null) {
                dragonInventoryButton.setPosition(screen.getGuiLeft() + 128, screen.height / 2 - 22);
            }
        }
    }

    @SubscribeEvent
    public static void addCraftingButton(ScreenEvent.Init.Post initGuiEvent) {
        Screen sc = initGuiEvent.getScreen();

        if (sc instanceof InventoryScreen screen) {
            if (ServerConfig.allowDragonChoiceFromInventory) {
                altarOpenButton = new ExtendedButton(screen.getGuiLeft() + 138, screen.height / 2 - 32, 32, 32, Component.empty(), button -> {
                    ClientProxy.openDragonAltar();
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
            if (inventoryToggle) {
                dragonInventoryButton = new ExtendedButton(screen.getGuiLeft() + 128, screen.height / 2 - 22, 20, 18, Component.empty(), p_onPress_1_ -> {
                    openDragonInventory();
                }) {
                    @Override
                    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        float u = 21f;
                        float v = isHoveredOrFocused() ? 20f : 0f;
                        guiGraphics.blit(INVENTORY_TOGGLE_BUTTON, getX(), getY(), u, v, 20, 18, 256, 256);
                    }
                };
                dragonInventoryButton.setTooltip(Tooltip.create(Component.translatable(TOGGLE_DRAGON_INVENTORY)));
                initGuiEvent.addListener(dragonInventoryButton);
            }
        }

        if (sc instanceof CreativeModeInventoryScreen screen) {
            if (inventoryToggle) {
                creativeModeDragonInventoryButton = new ExtendedButton(screen.getGuiLeft() + 128 + 20, screen.height / 2 - 50, 20, 18, Component.empty(), p_onPress_1_ -> {
                    openDragonInventory();
                }) {
                    @Override
                    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                        float u = 21f;
                        float v = isHoveredOrFocused() ? 20f : 0f;
                        guiGraphics.blit(INVENTORY_TOGGLE_BUTTON, getX(), getY(), u, v, 20, 18, 256, 256);
                    }
                };
                creativeModeDragonInventoryButton.setTooltip(Tooltip.create(Component.translatable(TOGGLE_DRAGON_INVENTORY)));
                initGuiEvent.addListener(creativeModeDragonInventoryButton);
            }
        }
    }

    @SubscribeEvent
    public static void handleKey(final InputEvent.Key event) {
        if (event.getAction() != InputConstants.PRESS) {
            return;
        }

        if (!DragonStateProvider.isDragon(Minecraft.getInstance().player)) {
            return;
        }

        Screen screen = Minecraft.getInstance().screen;
        InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());

        if (screen == null) {
            if (Keybind.DRAGON_INVENTORY.isDown(key)) {
                PacketDistributor.sendToServer(RequestOpenDragonInventory.INSTANCE);
            } else if (Keybind.SKINS_MENU.isDown(key)) {
                Minecraft.getInstance().setScreen(new DragonSkinsScreen());
            } else if (Keybind.ABILITY_MENU.isDown(key)) {
                Minecraft.getInstance().setScreen(new DragonAbilityScreen());
            } else if (Keybind.SPECIES_MENU.isDown(key)) {
                Minecraft.getInstance().setScreen(new DragonSpeciesScreen());
            } else if (Keybind.EMOTE_MENU.isDown(key)) {
                Minecraft.getInstance().setScreen(new DragonEmoteScreen());
            }
        } else if (isInventoryTab(screen)) {
            switchOrClose(key, screen);
        }
    }

    private static void switchOrClose(final InputConstants.Key key, final Screen screen) {
        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);

        if (Keybind.DRAGON_INVENTORY.matches(key)) {
            if (screen instanceof DragonInventoryScreen) {
                player.closeContainer();
            } else {
                PacketDistributor.sendToServer(RequestOpenDragonInventory.INSTANCE);
            }
        } else if (Keybind.ABILITY_MENU.matches(key)) {
            if (screen instanceof DragonAbilityScreen) {
                player.closeContainer();
            } else {
                Minecraft.getInstance().setScreen(new DragonAbilityScreen());
            }
        } else if (Keybind.SPECIES_MENU.matches(key)) {
            if (screen instanceof DragonSpeciesScreen) {
                player.closeContainer();
            } else {
                Minecraft.getInstance().setScreen(new DragonSpeciesScreen());
            }
        } else if (Keybind.SKINS_MENU.matches(key)) {
            if (screen instanceof DragonSkinsScreen) {
                player.closeContainer();
            } else {
                Minecraft.getInstance().setScreen(new DragonSkinsScreen());
            }
        } else if (Keybind.EMOTE_MENU.matches(key)) {
            if (screen instanceof DragonEmoteScreen) {
                player.closeContainer();
            } else {
                Minecraft.getInstance().setScreen(new DragonEmoteScreen());
            }
        }
    }

    /** Maintains the cursor position (since the inventory normally resets the position to the screen) */
    public static void openDragonInventory() {
        DragonInventoryScreen.mouseX = Minecraft.getInstance().mouseHandler.xpos();
        DragonInventoryScreen.mouseY = Minecraft.getInstance().mouseHandler.ypos();

        PacketDistributor.sendToServer(RequestOpenDragonInventory.INSTANCE);
        //noinspection DataFlowIssue -> player is present
        ClawInventoryData data = ClawInventoryData.getData(Minecraft.getInstance().player);
        PacketDistributor.sendToServer(new SyncDragonClawMenuToggle(data.isMenuOpen()));
    }

    private static boolean isInventoryTab(final Screen screen) {
        return screen instanceof DragonInventoryScreen || screen instanceof DragonAbilityScreen || screen instanceof DragonSkinsScreen || screen instanceof DragonSpeciesScreen || screen instanceof DragonEmoteScreen;
    }
}
