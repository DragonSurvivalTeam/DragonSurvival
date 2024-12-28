package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DietMenuComponent implements ScrollableComponent, Renderable {
    private static final int VISIBLE_MAX_ROWS = 3;
    private static final int ITEMS_PER_ROW = 7;
    private static final int ITEM_SIZE = 18;

    private final Holder<DragonType> dragonSpecies;

    private final int x;
    private final int y;
    private final int maxX;
    private final int maxY;

    private int scrollAmount;

    public DietMenuComponent(final Holder<DragonType> dragonSpecies, int x, int y) {
        this.dragonSpecies = dragonSpecies;
        this.x = x;
        this.y = y;
        this.maxX = x + ITEMS_PER_ROW * ITEM_SIZE;
        this.maxY = y + VISIBLE_MAX_ROWS * ITEM_SIZE;
    }

    @Override
    public void scroll(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isHovered(mouseX, mouseY)) {
            int maxRows = maxScroll();

            if (maxRows > VISIBLE_MAX_ROWS) {
                // invert the value so that scrolling down shows further entries
                scrollAmount = Math.clamp(scrollAmount + (int) -scrollY, 0, maxRows);
            }
        }
    }

    @Override
    public void render(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // TODO :: draw centered string 'no custom diet' or sth. like that if the diet is empty
//        graphics.fill(x, y, maxX, maxY, 0xFFFF0000);

        List<Item> items = dragonSpecies.value().getDietItems();
        int processedRows = 0;
        int processedItems = 0;

        for (int i = scrollAmount * ITEMS_PER_ROW; i < items.size(); i++) {
            int itemX = x + (processedItems % ITEMS_PER_ROW) * ITEM_SIZE;
            int itemY = y + (processedRows * ITEM_SIZE);

            ItemStack stack = items.get(i).getDefaultInstance();
            graphics.renderFakeItem(stack, itemX, itemY);

            if (mouseX >= itemX && mouseX < itemX + ITEM_SIZE && mouseY >= itemY && mouseY < itemY + ITEM_SIZE) {
                graphics.renderTooltip(Minecraft.getInstance().font, stack, mouseX, mouseY);
            }

            processedItems++;

            if (processedItems == ITEMS_PER_ROW) {
                processedItems = 0;
                processedRows++;
            }

            if (processedRows == VISIBLE_MAX_ROWS) {
                break;
            }
        }

        String text = "[" + scrollAmount + "/" + maxScroll() + "]";
        int xPosition = x + (ITEMS_PER_ROW * ITEM_SIZE / 2) - Minecraft.getInstance().font.width(text) / 2;
        graphics.drawString(Minecraft.getInstance().font, text, xPosition, maxY + 3, DSColors.WHITE, false);
    }

    private boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= maxX && mouseY >= y && mouseY <= maxY;
    }

    private int maxScroll() {
        List<Item> items = dragonSpecies.value().getDietItems();

        if (items.isEmpty()) {
            return 0;
        }

        return (int) Math.ceil((double) items.size() / ITEMS_PER_ROW) - VISIBLE_MAX_ROWS;
    }
}
