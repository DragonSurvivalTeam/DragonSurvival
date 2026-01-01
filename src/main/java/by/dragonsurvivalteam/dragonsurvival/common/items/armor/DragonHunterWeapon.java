package by.dragonsurvivalteam.dragonsurvival.common.items.armor;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@ParametersAreNonnullByDefault
public class DragonHunterWeapon extends Item implements PermanentEnchantmentItem {
    private final List<Pair<ResourceKey<Enchantment>, Integer>> enchantments;
    private final String descriptionKey;

    public DragonHunterWeapon(final Properties properties, final String descriptionKey, final List<Pair<ResourceKey<Enchantment>, Integer>> enchantments) {
        super(properties);
        this.descriptionKey = descriptionKey;
        this.enchantments = enchantments;
    }

    @Override
    public List<Pair<ResourceKey<Enchantment>, Integer>> enchantments() {
        return enchantments;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.@NotNull TooltipContext context, @NotNull TooltipDisplay tooltipDisplay, @NotNull Consumer<Component> tooltipAdder, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(Component.translatable(Translation.Type.DESCRIPTION.wrap(descriptionKey)));
    }
}
