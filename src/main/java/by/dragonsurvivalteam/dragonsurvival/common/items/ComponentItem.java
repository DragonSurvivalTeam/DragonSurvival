package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ComponentItem<T> extends Item {
    private final DSDataComponents.Component<T> component;
    private final T defaultValue;

    public ComponentItem(
            final Properties properties,
            final DSDataComponents.Component<T> component,
            final T defaultValue
    ) {
        super(properties);
        this.component = component;
        this.defaultValue = defaultValue;
    }

    @Override
    public @NotNull ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        component.set(stack, defaultValue);
        return stack;
    }
}
