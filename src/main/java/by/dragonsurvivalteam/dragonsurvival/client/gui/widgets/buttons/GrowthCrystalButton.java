package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GrowthCrystalButton extends ExtendedButton {
    public static int MAX_LINES_SHOWN = 10;

    private final Holder<DragonStage> stage;

    private int scrollAmount;
    private int maxScroll = Integer.MAX_VALUE;

    public GrowthCrystalButton(int xPos, int yPos, final Holder<DragonStage> stage) {
        super(xPos, yPos, 8, 16, Component.empty(), action -> { /* Nothing to do */ });
        this.stage = stage;
        setTooltip();
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //noinspection DataFlowIssue -> player is present
        DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);
        double percentageFull = stage.value().getProgress(handler.getSize());

        if (percentageFull > 1) {
            graphics.blit(handler.getType().value().miscResources().growthCrystal().full(), getX(), getY(), 0, 0, width, height, 8, 16);
            return;
        }

        graphics.blit(handler.getType().value().miscResources().growthCrystal().empty(), getX(), getY(), 0, 0, width, height, 8, 16);

        if (percentageFull > 0) {
            int scissorHeight = (int) ((1 - percentageFull) * height);
            graphics.enableScissor(getX(), getY() + (height - scissorHeight), getX() + width, getY() + height);
            graphics.blit(handler.getType().value().miscResources().growthCrystal().full(), getX(), getY(), 0, 0, width, height, 8, 16);
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
                setTooltip();
            }

            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isHovered() {
        boolean isHovered = super.isHovered();

        if (!isHovered) {
            scrollAmount = 0;
        }

        return isHovered;
    }

    private void setTooltip() {
        List<Component> components = new ArrayList<>();

        components.add(Component.translatable(LangKey.GROWTH_STAGE).append(DragonStage.translatableName(Objects.requireNonNull(stage.getKey()))));
        components.add(Component.translatable(LangKey.GROWTH_STARTING_SIZE, stage.value().sizeRange().min()));
        components.add(Component.translatable(LangKey.GROWTH_MAX_SIZE, stage.value().sizeRange().max()));
        components.add(Component.translatable(LangKey.GROWTH_TIME, stage.value().getTimeToGrowFormatted(false)));
        components.add(Component.translatable(LangKey.GROWTH_HARVEST_LEVEL_BONUS, stage.value().harvestLevelBonus()));
        components.add(Component.translatable(LangKey.GROWTH_BREAK_SPEED_MULTIPLIER, stage.value().breakSpeedMultiplier()));

        if (stage.value().destructionData().isPresent()) {
            components.add(Component.translatable(LangKey.GROWTH_CAN_DESTROY_BLOCKS, stage.value().destructionData().get().blockDestructionSize()));
            components.add(Component.translatable(LangKey.GROWTH_CAN_CRUSH_ENTITIES, stage.value().destructionData().get().crushingSize()));
        }

        components.add(Component.translatable(LangKey.GROWTH_MODIFIERS_AT_MAX_SIZE));

        for (Modifier modifier : stage.value().modifiers()) {
            MutableComponent name = modifier.getFormattedDescription((int) stage.value().sizeRange().max());
            components.add(name);
        }

        maxScroll = components.size();
        scrollAmount = Math.clamp(scrollAmount, 0, maxScroll());
        MutableComponent growthStageTooltip = Component.empty();

        for (int i = scrollAmount; i < components.size(); i++) {
            if (i - scrollAmount > MAX_LINES_SHOWN) {
                break;
            }

            if (i - scrollAmount < MAX_LINES_SHOWN) {
                growthStageTooltip = growthStageTooltip.append(components.get(i)).append("\n");
            } else {
                growthStageTooltip = growthStageTooltip.append(components.get(i));
            }
        }

        setTooltip(Tooltip.create(growthStageTooltip));
    }

    private int maxScroll() {
        return Math.max(0, maxScroll - MAX_LINES_SHOWN);
    }
}
