package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.PermanentEnchantmentItem;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks.DS_BLOCKS;

public class DSCreativeTabs {
    @Translation(type = Translation.Type.MISC, comments = "Dragon Survival")
    private static final String CREATIVE_TAB = Translation.Type.GUI.wrap("creative_tab");

    public static final DeferredRegister<CreativeModeTab> DS_CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DragonSurvival.MODID);

    private static final List<Holder<Item>> HIDDEN = Arrays.asList(
            DSItems.HUNTING_NET,
            DSItems.LIGHTNING_TEXTURE_ITEM,
            DSItems.PASSIVE_MAGIC_BEACON,
            DSItems.PASSIVE_PEACE_BEACON,
            DSItems.PASSIVE_FIRE_BEACON,
            DSItems.INACTIVE_MAGIC_DRAGON_BEACON,
            DSItems.INACTIVE_PEACE_DRAGON_BEACON,
            DSItems.INACTIVE_FIRE_DRAGON_BEACON,
            DSItems.BOLAS
    );

    private static final CreativeModeTab.DisplayItemsGenerator BLOCK_ITEM_GENERATOR = (parameters, output) -> Stream.of(DS_BLOCKS).forEach(holder -> holder.getEntries().forEach(entry -> output.accept(entry.value())));
    private static final CreativeModeTab.DisplayItemsGenerator ITEM_GENERATOR = (parameters, output) -> Stream.of(DSItems.DS_ITEMS).forEach(holder -> holder.getEntries().forEach(entry -> {
        if (entry.value() instanceof PermanentEnchantmentItem || !(HIDDEN.contains(entry) || entry.value().toString().contains("skeleton"))) {
            output.accept(entry.value());
        }
    }));

    public static Holder<CreativeModeTab> DS_TAB = DS_CREATIVE_MODE_TABS.register("dragon_survival", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(DSItems.ELDER_DRAGON_BONE))
            .title(Component.translatable(CREATIVE_TAB))
            .displayItems(BLOCK_ITEM_GENERATOR)
            .displayItems(ITEM_GENERATOR)
            .build()
    );
}
