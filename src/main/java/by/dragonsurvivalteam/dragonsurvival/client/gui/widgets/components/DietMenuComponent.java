package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.components;

import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryCache;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DietMenuComponent implements ScrollableComponent, Renderable {
    @Translation(comments = "There is no custom dragon diet")
    private static final String NO_CUSTOM_DIET = Translation.Type.GUI.wrap("diet_menu.no_custom_diet");

    private static final int VISIBLE_MAX_ROWS = 3;
    private static final int ITEMS_PER_ROW = 7;
    private static final int ITEM_SIZE = 18;

    private final Holder<DragonSpecies> dragonSpecies;
    private @Nullable ItemStack hovered;

    private final int x;
    private final int y;
    private final int maxX;
    private final int maxY;

    private int scrollAmount;

    public DietMenuComponent(final Holder<DragonSpecies> dragonSpecies, int x, int y) {
        this.dragonSpecies = dragonSpecies;
        this.x = x;
        this.y = y;
        this.maxX = x + ITEMS_PER_ROW * ITEM_SIZE;
        this.maxY = y + VISIBLE_MAX_ROWS * ITEM_SIZE;
    }

    public @Nullable ItemStack getHovered() {
        return hovered;
    }

    @Override
    public void scroll(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isHovered(mouseX, mouseY)) {
            // invert the value so that scrolling down shows further entries
            scrollAmount = Math.clamp(scrollAmount + (int) -scrollY, 0, maxScroll());
        }
    }

    @Override
    public void render(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        List<Item> items = DietEntryCache.getDietItems(dragonSpecies);

        if (items.isEmpty()) {
            MutableComponent component = Component.translatable(NO_CUSTOM_DIET);
            List<FormattedCharSequence> formatted = Minecraft.getInstance().font.split(component, maxX - x);

            int startX = x + ITEMS_PER_ROW * ITEM_SIZE / 2;
            int startY = y + VISIBLE_MAX_ROWS * ITEM_SIZE / 2 - Minecraft.getInstance().font.lineHeight / 2;

            for (int row = 0; row < formatted.size(); row++) {
                FormattedCharSequence text = formatted.get(row);
                int xPosition = startX - Minecraft.getInstance().font.width(text) / 2;
                int yPosition = startY + row * (Minecraft.getInstance().font.lineHeight + 2);
                graphics.drawString(Minecraft.getInstance().font, text, xPosition, yPosition, DSColors.WHITE, false);
            }

            return;
        }

        int processedRows = 0;
        int processedItems = 0;
        hovered = null;

        for (int i = scrollAmount * ITEMS_PER_ROW; i < items.size(); i++) {
            int itemX = x + (processedItems % ITEMS_PER_ROW) * ITEM_SIZE;
            int itemY = y + (processedRows * ITEM_SIZE);

            ItemStack stack = items.get(i).getDefaultInstance();
            graphics.renderFakeItem(stack, itemX, itemY);

            if (mouseX >= itemX && mouseX < itemX + ITEM_SIZE && mouseY >= itemY && mouseY < itemY + ITEM_SIZE) {
                graphics.renderTooltip(Minecraft.getInstance().font, stack, mouseX, mouseY);
                hovered = stack;
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

        if (maxScroll() > 0) {
            String text = "[" + scrollAmount + "/" + maxScroll() + "]";
            int xPosition = x + (ITEMS_PER_ROW * ITEM_SIZE / 2) - Minecraft.getInstance().font.width(text) / 2;
            graphics.drawString(Minecraft.getInstance().font, text, xPosition, maxY + 3, DSColors.WHITE, false);
        }
    }

    private boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= maxX && mouseY >= y && mouseY <= maxY;
    }

    private int maxScroll() {
        List<Item> items = DietEntryCache.getDietItems(dragonSpecies);

        if (items.isEmpty()) {
            return 0;
        }

        return Math.max(0, (int) Math.ceil((double) items.size() / ITEMS_PER_ROW) - VISIBLE_MAX_ROWS);
    }
}
