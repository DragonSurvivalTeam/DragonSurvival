package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.TimeComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.SourceOfMagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.server.containers.SourceOfMagicContainer;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicBlockEntity;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class SourceOfMagicScreen extends AbstractContainerScreen<SourceOfMagicContainer> {
    @Translation(comments = "■ This source of magic can be charged with with the following items:")
    private static final String HELP = Translation.Type.GUI.wrap("source_of_magic.help");

    private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/source_of_magic_ui.png");

    private static final ResourceLocation CAVE_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/cave_source_of_magic_0.png");
    private static final ResourceLocation CAVE_FILLED = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/cave_source_of_magic_1.png");

    private static final ResourceLocation FOREST_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/forest_source_of_magic_0.png");
    private static final ResourceLocation FOREST_FILLED = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/forest_source_of_magic_1.png");

    private static final ResourceLocation SEA_MAIN = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/sea_source_of_magic_0.png");
    private static final ResourceLocation SEA_FILLED = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/sea_source_of_magic_1.png");

    private static final int MAX_SHOWN = 5;

    private HelpButton helpButton;
    private final SourceOfMagicBlockEntity blockEntity;
    private int scrollAmount;

    public SourceOfMagicScreen(final SourceOfMagicContainer container, final Inventory inventory, final Component title) {
        super(container, inventory, title);
        blockEntity = container.blockEntity;
    }

    @Override
    protected void init() {
        super.init();
        helpButton = new HelpButton(leftPos + 12, topPos + 12, 12, 12, getTooltip());
        addRenderableWidget(helpButton);
    }

    @Override
    protected void renderLabels(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY) { /* Nothing to do */ }

    @Override
    public boolean mouseScrolled(final double mouseX, final double mouseY, final double scrollX, final double scrollY) {
        if (helpButton.isHovered()) {
            int oldScroll = scrollAmount;
            // invert the value so that scrolling down shows further entries
            scrollAmount = Math.clamp(scrollAmount + (int) -scrollY, 0, maxScroll());

            if (oldScroll != scrollAmount) {
                helpButton.setTooltip(getTooltip());
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void render(@NotNull final GuiGraphics graphics, final int mouseX, final int mouseY, final float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull final GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        boolean hasItem = blockEntity.getCurrentDuration() > 0;
        Block block = blockEntity.getBlockState().getBlock();

        ResourceLocation resource = null;

        if (block == DSBlocks.CAVE_SOURCE_OF_MAGIC.get()) {
            resource = hasItem ? CAVE_FILLED : CAVE_MAIN;
        } else if (block == DSBlocks.FOREST_SOURCE_OF_MAGIC.get()) {
            resource = hasItem ? FOREST_FILLED : FOREST_MAIN;
        } else if (block == DSBlocks.SEA_SOURCE_OF_MAGIC.get()) {
            resource = hasItem ? SEA_FILLED : SEA_MAIN;
        }

        if (resource != null) {
            guiGraphics.blit(resource, leftPos + 8, topPos + 8, 0, 0, 160, 49, 160, 49);
        }
    }

    private List<Either<FormattedText, TooltipComponent>> getTooltip() {
        List<Either<FormattedText, TooltipComponent>> tooltip = new ArrayList<>();
        List<SourceOfMagicData.Consumable> consumables = blockEntity.getConsumables();
        // Show the highest duration at the top
        consumables.sort(Comparator.comparingInt(SourceOfMagicData.Consumable::duration).reversed());

        for (int i = scrollAmount; i < consumables.size(); i++) {
            if (tooltip.size() == MAX_SHOWN) {
                break;
            }

            SourceOfMagicData.Consumable consumable = consumables.get(i);
            tooltip.add(Either.right(new TimeComponent(consumable.item(), consumable.duration(), TimeComponent.DEFAULT)));
        }

        tooltip.addFirst(Either.left(Component.translatable(HELP)));
        return tooltip;
    }

    private int maxScroll() {
        if (blockEntity == null) {
            return 0;
        }

        return Math.max(0, blockEntity.consumableAmount() - MAX_SHOWN);
    }
}