package by.dragonsurvivalteam.dragonsurvival.common.items.armor;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.AttributeOperation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@ParametersAreNonnullByDefault
public class DragonHunterWeapon extends SwordItem implements PermanentEnchantmentItem {
    private static final UUID PARTISAN_BLOCK_REACH_UUID = UUID.nameUUIDFromBytes("dragonsurvival:partisan_block_reach".getBytes(StandardCharsets.UTF_8));
    private static final UUID PARTISAN_ENTITY_REACH_UUID = UUID.nameUUIDFromBytes("dragonsurvival:partisan_attack_reach".getBytes(StandardCharsets.UTF_8));

    private final List<Pair<ResourceKey<Enchantment>, Integer>> enchantments;
    private final String descriptionKey;
    private final boolean extendedReach;

    public DragonHunterWeapon(final Tier tier, final int attackDamageModifier, final float attackSpeedModifier, final boolean extendedReach,
                              final Properties properties, final String descriptionKey,
                              final List<Pair<ResourceKey<Enchantment>, Integer>> enchantments) {
        super(tier, attackDamageModifier, attackSpeedModifier, properties);
        this.descriptionKey = descriptionKey;
        this.enchantments = enchantments;
        this.extendedReach = extendedReach;
    }

    @Override
    public List<Pair<ResourceKey<Enchantment>, Integer>> enchantments() {
        return enchantments;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(final EquipmentSlot slot) {
        Multimap<Attribute, AttributeModifier> modifiers = super.getDefaultAttributeModifiers(slot);

        if (!extendedReach || slot != EquipmentSlot.MAINHAND) {
            return modifiers;
        }

        return ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .putAll(modifiers)
                .put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(PARTISAN_BLOCK_REACH_UUID, "dragonsurvival:partisan_block_reach", 1, AttributeOperation.ADD_VALUE.legacy()))
                .put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(PARTISAN_ENTITY_REACH_UUID, "dragonsurvival:partisan_attack_reach", 1, AttributeOperation.ADD_VALUE.legacy()))
                .build();
    }

    @Override
    public void appendHoverText(@NotNull final ItemStack stack, @Nullable final Level level, @NotNull final List<Component> tooltips, @NotNull final TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltips, flag);
        tooltips.add(Component.translatable(Translation.Type.DESCRIPTION.wrap(descriptionKey)));
    }
}
