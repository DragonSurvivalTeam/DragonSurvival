package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import by.dragonsurvivalteam.dragonsurvival.server.containers.SourceOfMagicContainer;
import by.dragonsurvivalteam.dragonsurvival.server.tileentity.SourceOfMagicTileEntity;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class SourceOfMagicScreen extends AbstractContainerScreen<SourceOfMagicContainer> {
    static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/source_of_magic_ui.png");
    static final ResourceLocation CAVE_NEST0 = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/cave_source_of_magic_0.png");
    static final ResourceLocation CAVE_NEST1 = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/cave_source_of_magic_1.png");
    static final ResourceLocation FOREST_NEST0 = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/forest_source_of_magic_0.png");
    static final ResourceLocation FOREST_NEST1 = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/forest_source_of_magic_1.png");
    static final ResourceLocation SEA_NEST0 = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/sea_source_of_magic_0.png");
    static final ResourceLocation SEA_NEST1 = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/source_of_magic/sea_source_of_magic_1.png");
    private final SourceOfMagicTileEntity nest;

    private final Player player;

    public SourceOfMagicScreen(SourceOfMagicContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);
        nest = screenContainer.nestEntity;
        player = inv.player;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new HelpButton(leftPos + 12, topPos + 12, 12, 12, "ds.help.source_of_magic", 0));
    }

    @Override
    protected void renderLabels(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY) { /* Nothing to do */ }

    @Override
    protected void renderBg(@NotNull final GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight);

        boolean hasItem = !nest.getItem(0).isEmpty();
        Block block = nest.getBlockState().getBlock();

        ResourceLocation resourceLocation = null;

        if (DSBlocks.CAVE_SOURCE_OF_MAGIC.get().equals(block)) {
            resourceLocation = hasItem ? CAVE_NEST1 : CAVE_NEST0;
        } else if (DSBlocks.FOREST_SOURCE_OF_MAGIC.get().equals(block)) {
            resourceLocation = hasItem ? FOREST_NEST1 : FOREST_NEST0;
        } else if (DSBlocks.SEA_SOURCE_OF_MAGIC.get().equals(block)) {
            resourceLocation = hasItem ? SEA_NEST1 : SEA_NEST0;
        }

        if (resourceLocation != null) {
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1f);
            guiGraphics.blit(resourceLocation, leftPos + 8, topPos + 8, 0, 0, 160, 49, 160, 49);
        }
    }
}