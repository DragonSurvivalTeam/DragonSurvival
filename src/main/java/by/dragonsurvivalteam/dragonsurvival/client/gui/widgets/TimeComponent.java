package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthItem;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;

import java.util.function.BiFunction;

public record TimeComponent(Item item, int ticks, BiFunction<Item, Integer, Component> description) implements TooltipComponent {
    @Translation(comments = "%s %s")
    private static final String TIME = Translation.Type.GUI.wrap("time_of");

    @Translation(comments = "Stops or starts natural growth")
    private static final String SWITCHES_GROWTH = Translation.Type.GUI.wrap("switches_growth");

    public static final BiFunction<Item, Integer, Component> DEFAULT = (item, ticks) -> Component.translatable(item.getDescriptionId()).append(": ").append(translateTime(ticks));

    public static final BiFunction<Item, Integer, Component> GROWTH = (item, ticks) -> {
        Player player = DragonSurvival.PROXY.getLocalPlayer();
        //noinspection DataFlowIssue -> player is expected to be present
        DragonStateHandler handler = DragonStateProvider.getData(player);

        //noinspection deprecation,OptionalGetWithoutIsPresent -> ignore / the item is expected to be part of the entries at this point
        GrowthItem growthItem = handler.stage().value().growthItems().stream().filter(entry -> entry.items().contains(item.builtInRegistryHolder())).findFirst().get();

        Component growthInfo = ticks == 0 ? Component.translatable(SWITCHES_GROWTH) : translateTime(ticks);

        if (growthItem.maximumUsages() == GrowthItem.INFINITE_USAGES) {
            return Component.translatable(item.getDescriptionId()).append(": ").append(growthInfo);
        }

        int numberOfUses = handler.getGrowthUses(item);
        MutableComponent usage = Component.translatable(String.format(" (%s / %s)", growthItem.maximumUsages() - numberOfUses, growthItem.maximumUsages()));

        if (numberOfUses == growthItem.maximumUsages()) {
            usage = usage.withStyle(ChatFormatting.DARK_GRAY);
        }

        return Component.translatable(item.getDescriptionId()).append(": ").append(growthInfo).append(usage);
    };

    private static Component translateTime(int ticks) {
        return Component.translatable(TIME, ticks > 0 ? "+" : "-", Functions.Time.fromTicks(ticks).format()).withStyle(ticks > 0 ? ChatFormatting.GREEN : ChatFormatting.RED);
    }
}
