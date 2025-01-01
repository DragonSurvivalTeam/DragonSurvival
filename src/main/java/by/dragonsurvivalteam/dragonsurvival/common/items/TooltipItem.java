package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class TooltipItem extends Item {
    private final Function<ItemStack, String> keySupplier;

    /** If no key is provided {@link ResourceLocation#getPath()} of the item holder will be used instead */
    public TooltipItem(final Item.Properties properties, final @Nullable String key) {
        super(properties);
        //noinspection DataFlowIssue -> this is a Holder$Reference not a Holder$Direct, meaning it's fine
        this.keySupplier = stack -> Objects.requireNonNullElseGet(key, () -> stack.getItemHolder().getKey().location().getPath());
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @NotNull final Item.TooltipContext context, @NotNull final List<Component> tooltips, @NotNull final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltips, flag);
        String key = Translation.Type.DESCRIPTION.wrap(keySupplier.apply(stack));

        if (I18n.exists(key)) {
            tooltips.add(Component.translatable(key));
        }
    }
}
