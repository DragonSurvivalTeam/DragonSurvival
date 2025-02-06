package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GrowthCrystalButton extends ExtendedButton {
    public static final int MAX_LINES_SHOWN = 10;

    private final Holder<DragonStage> stage;
    private List<FormattedCharSequence> tooltip = List.of();

    private int scrollAmount;
    private int maxScroll = Integer.MAX_VALUE;

    public GrowthCrystalButton(int xPos, int yPos, final Holder<DragonStage> stage) {
        super(xPos, yPos, 8, 16, Component.empty(), action -> { /* Nothing to do */ });
        this.stage = stage;
        updateTooltip();
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //noinspection DataFlowIssue -> player is present
        DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
        double percentageFull = stage.value().getProgress(handler.getGrowth());

        if (percentageFull > 1) {
            graphics.blit(handler.species().value().miscResources().growthCrystal().full(), getX(), getY(), 0, 0, width, height, 8, 16);
            return;
        }

        graphics.blit(handler.species().value().miscResources().growthCrystal().empty(), getX(), getY(), 0, 0, width, height, 8, 16);

        if (percentageFull > 0) {
            int scissorHeight = (int) (percentageFull * height);
            graphics.enableScissor(getX(), getY() + (height - scissorHeight), getX() + width, getY() + height);
            graphics.blit(handler.species().value().miscResources().growthCrystal().full(), getX(), getY(), 0, 0, width, height, 8, 16);
            graphics.disableScissor();
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isHovered()) {
            int oldScrollAmount = scrollAmount;

            // invert the value so that scrolling down shows further entries
            scrollAmount = Math.clamp(scrollAmount + (int) -scrollY, 0, maxScroll());

            if (oldScrollAmount != scrollAmount) {
                updateTooltip();
            }

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isHovered() {
        boolean isHovered = super.isHovered();

        if (!isHovered && scrollAmount > 0) {
            scrollAmount = 0;
            updateTooltip();
        }

        if (isHovered) {
            //noinspection DataFlowIssue -> screen is not null
            Minecraft.getInstance().screen.setTooltipForNextRenderPass(tooltip);
        }

        return isHovered;
    }

    private void updateTooltip() {
        List<Component> components = new ArrayList<>();

        components.add(Component.translatable(LangKey.GROWTH_STAGE).append(DragonStage.translatableName(Objects.requireNonNull(stage.getKey()))));
        components.add(Component.translatable(LangKey.GROWTH_STARTING_AMOUNT, stage.value().growthRange().min()));
        components.add(Component.translatable(LangKey.GROWTH_MAX_AMOUNT, stage.value().growthRange().max()));
        components.add(Component.translatable(LangKey.GROWTH_TIME, stage.value().getTimeToGrowFormatted(false)));

        stage.value().destructionData().ifPresent(data -> {
            components.add(Component.translatable(LangKey.GROWTH_CAN_DESTROY_BLOCKS, data.blockDestructionGrowth()));
            components.add(Component.translatable(LangKey.GROWTH_CAN_CRUSH_ENTITIES, data.crushingGrowth()));
        });

        components.add(Component.translatable(LangKey.GROWTH_MODIFIERS_AT_MAX_GROWTH));

        for (Modifier modifier : stage.value().modifiers()) {
            MutableComponent name = modifier.getFormattedDescription((int) stage.value().growthRange().max(), true);
            components.add(name);
        }

        MutableComponent tooltip = Component.empty();

        for (int i = 0; i < components.size(); i++) {
            if (i == components.size() - 1) {
                tooltip = tooltip.append(components.get(i));
            } else {
                tooltip = tooltip.append(components.get(i)).append("\n");
            }
        }

        // The default width (170) of 'Tooltip#splitTooltip' is not enough
        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(tooltip, 200);
        List<FormattedCharSequence> shownTooltip = new ArrayList<>();

        maxScroll = lines.size();
        scrollAmount = Math.clamp(scrollAmount, 0, maxScroll());

        for (int line = scrollAmount; line < lines.size(); line++) {
            if (line - scrollAmount == MAX_LINES_SHOWN) {
                break;
            }

            shownTooltip.add(lines.get(line));
        }

        // TODO :: add scroll icon or sth. at the bottom like that (if it can still be scrolled)

        this.tooltip = shownTooltip;
    }

    private int maxScroll() {
        return Math.max(0, maxScroll - MAX_LINES_SHOWN);
    }
}
