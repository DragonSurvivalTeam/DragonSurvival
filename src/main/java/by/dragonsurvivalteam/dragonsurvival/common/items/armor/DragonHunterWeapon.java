package by.dragonsurvivalteam.dragonsurvival.common.items.armor;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@ParametersAreNonnullByDefault
public class DragonHunterWeapon extends SwordItem implements PermanentEnchantmentItem {
    private final List<Pair<ResourceKey<Enchantment>, Integer>> enchantments;
    private final String descriptionKey;

    public DragonHunterWeapon(final Tier tier, final Properties properties, final String descriptionKey, final List<Pair<ResourceKey<Enchantment>, Integer>> enchantments) {
        super(tier, properties);
        this.descriptionKey = descriptionKey;
        this.enchantments = enchantments;
    }

    @Override
    public List<Pair<ResourceKey<Enchantment>, Integer>> enchantments() {
        return enchantments;
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @NotNull final Item.TooltipContext context, @NotNull final List<Component> tooltips, @NotNull final TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltips, flag);
        tooltips.add(Component.translatable(Translation.Type.DESCRIPTION.wrap(descriptionKey)));
    }
}
