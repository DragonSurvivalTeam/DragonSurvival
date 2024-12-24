package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.GrowthHUD;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.GrowthComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.TabButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.input.Keybind;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenuToggle;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenInventory;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonInventoryScreen extends EffectRenderingInventoryScreen<DragonContainer> {
    @Translation(type = Translation.Type.MISC, comments = "§6Dragon Tab§r")
    private static final String TOGGLE = Translation.Type.GUI.wrap("dragon_inventory.toggle");

    @Translation(type = Translation.Type.MISC, comments = "Toggle showing claws and teeth textures on your model.")
    private static final String TOGGLE_CLAWS = Translation.Type.GUI.wrap("dragon_inventory.toggle_claws");

    @Translation(type = Translation.Type.MISC, comments = "Open vanilla inventory screen")
    private static final String TOGGLE_VANILLA_INVENTORY = Translation.Type.GUI.wrap("dragon_inventory.toggle_vanilla_inventory");

    @Translation(type = Translation.Type.MISC, comments = {
            "■ A dragon is §6born§r with strong claws and teeth, but you can make them even better! Just put §6any tools§r§f here in your claw slots and your bare paw will borrow their aspect as long as they are intact.",
            "§7■ Does not stack with «Claws and Teeth» skill, which only applies if these slots are empty."
    })
    private static final String HELP_CLAWS = Translation.Type.GUI.wrap("help.claws");

    @Translation(type = Translation.Type.MISC, comments = "■ §6Stage§r§7: ")
    private static final String GROWTH_STAGE = Translation.Type.GUI.wrap("dragon_inventory.growth_stage");

    @Translation(type = Translation.Type.MISC, comments = "■ §6Age§r§7: %s")
    private static final String GROWTH_AGE = Translation.Type.GUI.wrap("dragon_inventory.growth_age");

    @Translation(type = Translation.Type.MISC, comments = {
            "\n■ All dragons will gradually grow as time passes, improving their attributes. At certain growth stages, your appearance will change, and your growth will slow.§r",
            "§7■ A Star Bone will revert your growth slightly, and a Star Heart will completely stop you from growing. The biggest dragons can take other players for a ride!§r",
            "§7■ Growth items:§r"
    })
    private static final String GROWTH_INFO = Translation.Type.GUI.wrap("dragon_inventory.growth_info");

    public static final ResourceLocation INVENTORY_TOGGLE_BUTTON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/inventory_button.png");

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dragon_inventory.png");
    private static final ResourceLocation CLAWS_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dragon_claws.png");
    private static final ResourceLocation DRAGON_CLAW_BUTTON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dragon_claws_button.png");
    private static final ResourceLocation DRAGON_CLAW_CHECKMARK = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dragon_claws_checked.png");

    public static double mouseX = -1;
    public static double mouseY = -1;

    private static final int MAX_SHOWN = 5;
    private int scroll;
    private boolean resetScroll;

    private boolean clawsMenu;
    private final Player player;
    private boolean buttonClicked;
    private boolean isGrowthIconHovered;
    private final List<ExtendedButton> clawMenuButtons = new ArrayList<>();

    public DragonInventoryScreen(DragonContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        player = inv.player;
        clawsMenu = ClawInventoryData.getData(player).isMenuOpen();
        imageWidth = 203;
        imageHeight = 166;
    }

    @Override
    @SuppressWarnings("DataFlowIssue") // minecraft gets set from setScreen -> init
    protected void init() {
        super.init();

        if (mouseX != -1 && mouseY != -1) {
            InputConstants.grabOrReleaseMouse(minecraft.getWindow().getWindow(), 212993, mouseX, mouseY);
            mouseX = -1;
            mouseY = -1;
        }

        leftPos = (width - imageWidth) / 2;

        ClawInventoryData clawInventory = ClawInventoryData.getData(player);

        addRenderableWidget(new TabButton(leftPos, topPos - 28, TabButton.Type.INVENTORY_TAB, this));
        addRenderableWidget(new TabButton(leftPos + 28, topPos - 26, TabButton.Type.ABILITY_TAB, this));
        addRenderableWidget(new TabButton(leftPos + 57, topPos - 26, TabButton.Type.GITHUB_REMINDER_TAB, this));
        addRenderableWidget(new TabButton(leftPos + 86, topPos - 26, TabButton.Type.SKINS_TAB, this));

        ExtendedButton clawToggle = new ExtendedButton(leftPos + 27, topPos + 10, 11, 11, Component.empty(), button -> {
            clawsMenu = !clawsMenu;
            clearWidgets();
            init();

            PacketDistributor.sendToServer(new SyncDragonClawsMenuToggle.Data(clawsMenu));
            clawInventory.setMenuOpen(clawsMenu);
        }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                guiGraphics.blit(DRAGON_CLAW_BUTTON, getX(), getY(), 0, 0, 11, 11, 11, 11);
            }
        };
        clawToggle.setTooltip(Tooltip.create(Component.translatable(TOGGLE)));
        addRenderableWidget(clawToggle);

        // Growth icon in the claw menu
        ExtendedButton growthIcon = new ExtendedButton(leftPos - 58, topPos - 35, 32, 32, Component.empty(), btn -> {
        }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                isGrowthIconHovered = isHovered();
            }
        };
        addRenderableWidget(growthIcon);
        clawMenuButtons.add(growthIcon);

        // Info button at the bottom of the claw menu
        HelpButton infoButton = new HelpButton(leftPos - 80 + 34, topPos + 112, 9, 9, HELP_CLAWS, true);
        addRenderableWidget(infoButton);
        clawMenuButtons.add(infoButton);

        // Button to enable / disable the rendering of claws
        ExtendedButton clawRenderButton = new ExtendedButton(leftPos - 80 + 34, topPos + 140, 9, 9, Component.empty(), p_onPress_1_ -> {
            boolean claws = !clawInventory.shouldRenderClaws;

            clawInventory.shouldRenderClaws = claws;
            ConfigHandler.updateConfigValue("render_dragon_claws", clawInventory.shouldRenderClaws);
            PacketDistributor.sendToServer(new SyncDragonClawRender.Data(player.getId(), claws));
        }) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                ClawInventoryData clawInventory = ClawInventoryData.getData(player);
                if (clawInventory.shouldRenderClaws) {
                    guiGraphics.pose().pushPose();
                    guiGraphics.pose().translate(0, 0, 100);
                    guiGraphics.blit(DRAGON_CLAW_CHECKMARK, getX(), getY(), 0, 0, 9, 9, 9, 9);
                    guiGraphics.pose().popPose();
                }
            }
        };
        clawRenderButton.setTooltip(Tooltip.create(Component.translatable(TOGGLE_CLAWS)));
        addRenderableWidget(clawRenderButton);
        clawMenuButtons.add(clawRenderButton);

        if (InventoryScreenHandler.inventoryToggle) {
            ExtendedButton inventoryToggle = new ExtendedButton(leftPos + imageWidth - 28, height / 2 - 30 + 47, 20, 18, Component.empty(), p_onPress_1_ -> {
                Minecraft.getInstance().setScreen(new InventoryScreen(player));
                PacketDistributor.sendToServer(new RequestOpenInventory.Data());
            }) {
                @Override
                public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                    float u = 0f;
                    float v = isHoveredOrFocused() ? 20f : 0f;
                    guiGraphics.blit(INVENTORY_TOGGLE_BUTTON, getX(), getY(), u, v, 20, 18, 256, 256);
                }
            };
            inventoryToggle.setTooltip(Tooltip.create(Component.translatable(TOGGLE_VANILLA_INVENTORY)));
            addRenderableWidget(inventoryToggle);
        }
    }

    @Override
    protected void renderLabels(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY) { /* Nothing to do */ }

    @Override
    protected void renderBg(@NotNull final GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.enableBlend();
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        RenderSystem.disableBlend();

        DragonStateHandler handler = DragonStateProvider.getData(player);

        int scissorY1 = topPos + 77;
        int scissorX1 = leftPos + 101;
        int scissorX0 = leftPos + 25;
        int scissorY0 = topPos + 8;

        // In order to scale up the smaller dragon sizes, since they are too small otherwise
        int scale = (int) (20 + ((DragonStage.MAX_HANDLED_SIZE - handler.getSize()) * 0.25));
        scale = Math.clamp(scale, 10, 40); // Very large dragon sizes (above the default max. size) will have a < 20 scale value

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, scissorX0, scissorY0, scissorX1, scissorY1, scale, 0, mouseX, mouseY, player);

        if (!clawsMenu) {
            return;
        }

        guiGraphics.blit(CLAWS_TEXTURE, leftPos - 80, topPos, 0, 0, 77, 170);
        int circleX = leftPos - 58;
        int circleY = topPos - 35;
        guiGraphics.blit(handler.getType().value().getGrowthIcon(handler.getStage()), circleX + 6, circleY + 6, 150, 0, 0, 20, 20, 20, 20);
    }

    @Override
    public boolean mouseReleased(double p_mouseReleased_1_, double p_mouseReleased_3_, int p_mouseReleased_5_) {
        if (buttonClicked) {
            buttonClicked = false;
            return true;
        } else {
            return super.mouseReleased(p_mouseReleased_1_, p_mouseReleased_3_, p_mouseReleased_5_);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);

        if (Keybind.DRAGON_INVENTORY.get().isActiveAndMatches(mouseKey)) {
            onClose();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isGrowthIconHovered) {
            scroll += (int) -scrollY; // invert the value so that scrolling down shows further entries
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        clawMenuButtons.forEach(button -> button.visible = clawsMenu);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);

        if (isGrowthIconHovered) {
            if (resetScroll) {
                resetScroll = false;
                scroll = 0;
            }

            DragonStateHandler handler = DragonStateProvider.getData(player);
            DragonStage dragonStage = handler.getStage().value();

            double percentage = Math.clamp(dragonStage.getProgress(handler.getSize()), 0, 1);
            String ageInformation = NumberFormat.getPercentInstance().format(percentage);

            if (handler.isGrowing) {
                double sizeToTicks = (dragonStage.sizeRange().max() - dragonStage.sizeRange().min()) / dragonStage.ticksUntilGrown();
                double missingSize = dragonStage.sizeRange().max() - handler.getSize();
                Functions.Time time = Functions.Time.fromTicks((int) (missingSize / sizeToTicks));

                if (time.hasTime()) {
                    ageInformation += " (" + time.format() + ")";
                }
            } else {
                ageInformation += " (§4--:--:--§r)";
            }

            List<GrowthComponent> growthItems = new ArrayList<>();

            handler.getStage().value().growthItems().forEach(growthItem -> {
                // A bit of wasted processing since not all are shown
                growthItem.items().forEach(item -> growthItems.add(new GrowthComponent(item.value(), growthItem.growthInTicks())));
            });

            if (growthItems.size() <= MAX_SHOWN) {
                scroll = 0;
            } else {
                scroll = Math.clamp(scroll, 0, growthItems.size() - MAX_SHOWN);
            }

            int max = Math.min(growthItems.size(), scroll + MAX_SHOWN);

            List<Either<FormattedText, TooltipComponent>> components = new ArrayList<>();
            components.add(Either.left(Component.translatable(GROWTH_STAGE).append(DragonStage.translatableName(Objects.requireNonNull(handler.getStage().getKey())))));
            components.add(Either.left(Component.translatable(GROWTH_AGE, ageInformation)));
            components.add(Either.left(Component.translatable(GROWTH_INFO).append(Component.literal(" [" + Math.min(growthItems.size(), scroll + MAX_SHOWN) + " / " + growthItems.size() + "]").withStyle(ChatFormatting.DARK_GRAY))));

            for (int i = scroll; i < max; i++) {
                components.add(Either.right(growthItems.get(i)));
            }

            graphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, components, mouseX, mouseY, ItemStack.EMPTY);
        } else {
            resetScroll = true;
        }
    }
}