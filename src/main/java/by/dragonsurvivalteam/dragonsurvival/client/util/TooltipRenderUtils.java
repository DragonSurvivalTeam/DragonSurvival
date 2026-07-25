package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.mixins.client.GuiGraphicsAccess;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public final class TooltipRenderUtils {
    private TooltipRenderUtils() {
    }

    public static void renderTooltipFromElements(final GuiGraphics graphics, final Font fallbackFont,
                                                  final List<Either<FormattedText, TooltipComponent>> elements,
                                                  int mouseX, int mouseY, final ItemStack stack) {
        Font font = ForgeHooksClient.getTooltipFont(stack, fallbackFont);
        RenderTooltipEvent.GatherComponents event = new RenderTooltipEvent.GatherComponents(
                stack, graphics.guiWidth(), graphics.guiHeight(), new ArrayList<>(elements), -1);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            return;
        }

        int tooltipTextWidth = event.getTooltipElements().stream()
                .mapToInt(element -> element.map(font::width, component -> 0))
                .max()
                .orElse(0);
        boolean needsWrap = false;
        int tooltipX = mouseX + 12;

        if (tooltipX + tooltipTextWidth + 4 > graphics.guiWidth()) {
            tooltipX = mouseX - 16 - tooltipTextWidth;

            if (tooltipX < 4) {
                tooltipTextWidth = mouseX > graphics.guiWidth() / 2
                        ? mouseX - 20
                        : graphics.guiWidth() - 16 - mouseX;
                needsWrap = true;
            }
        }

        if (event.getMaxWidth() > 0 && tooltipTextWidth > event.getMaxWidth()) {
            tooltipTextWidth = event.getMaxWidth();
            needsWrap = true;
        }

        final int maxWidth = tooltipTextWidth;
        List<ClientTooltipComponent> components = needsWrap
                ? event.getTooltipElements().stream()
                        .flatMap(element -> element.map(
                                text -> splitLine(text, font, maxWidth),
                                component -> Stream.of(ClientTooltipComponent.create(component))))
                        .toList()
                : event.getTooltipElements().stream()
                        .map(element -> element.map(
                                text -> ClientTooltipComponent.create(text instanceof Component component
                                        ? component.getVisualOrderText()
                                        : Language.getInstance().getVisualOrder(text)),
                                ClientTooltipComponent::create))
                        .toList();

        GuiGraphicsAccess access = (GuiGraphicsAccess) graphics;
        access.dragonSurvival$setTooltipStack(stack);

        try {
            access.dragonSurvival$renderTooltipInternal(font, components, mouseX, mouseY, DefaultTooltipPositioner.INSTANCE);
        } finally {
            access.dragonSurvival$setTooltipStack(ItemStack.EMPTY);
        }
    }

    private static Stream<ClientTooltipComponent> splitLine(final FormattedText text, final Font font, int maxWidth) {
        if (text instanceof Component component && component.getString().isEmpty()) {
            return Stream.of(ClientTooltipComponent.create(component.getVisualOrderText()));
        }

        return font.split(text, maxWidth).stream().map(ClientTooltipComponent::create);
    }
}
