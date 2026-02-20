package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class TooltipItem extends Item {
    private final Function<ItemStack, String> keySupplier;

    /** If no key is provided {@link Identifier#getPath()} of the item holder will be used instead */
    public TooltipItem(final Item.Properties properties, final @Nullable String key) {
        super(properties);
        //noinspection DataFlowIssue -> this is a Holder$Reference not a Holder$Direct, meaning it's fine
        this.keySupplier = stack -> Objects.requireNonNullElseGet(key, () -> stack.getItemHolder().getKey().identifier().getPath());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        String key = Translation.Type.DESCRIPTION.wrap(keySupplier.apply(stack));

        if (I18n.exists(key)) {
            tooltipAdder.accept(Component.translatable(key));
        }
    }
}
