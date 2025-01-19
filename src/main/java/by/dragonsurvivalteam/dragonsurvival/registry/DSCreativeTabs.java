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

public class DSCreativeTabs {
    @Translation(comments = "Dragon Survival")
    private static final String CREATIVE_TAB = Translation.Type.GUI.wrap("creative_tab");

    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DragonSurvival.MODID);

    // TODO :: handle this by an annotation?
    private static final List<Holder<Item>> HIDDEN = Arrays.asList(
            DSItems.HUNTING_NET,
            DSItems.LIGHTNING_TEXTURE_ITEM,
            DSItems.BOLAS,
            DSItems.ACTIVATED_DRAGON_BEACON
    );

    private static final CreativeModeTab.DisplayItemsGenerator BLOCK_ITEM_GENERATOR = (parameters, output) -> Stream.of(DSBlocks.REGISTRY).forEach(holder -> holder.getEntries().forEach(entry -> output.accept(entry.value())));
    private static final CreativeModeTab.DisplayItemsGenerator ITEM_GENERATOR = (parameters, output) -> Stream.of(DSItems.REGISTRY).forEach(holder -> holder.getEntries().forEach(entry -> {
        if (entry.value() instanceof PermanentEnchantmentItem || !(HIDDEN.contains(entry) || entry.value().toString().contains("skeleton"))) {
            output.accept(entry.value());
        }
    }));

    public static Holder<CreativeModeTab> DS_TAB = REGISTRY.register("dragon_survival", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(DSItems.ELDER_DRAGON_BONE))
            .title(Component.translatable(CREATIVE_TAB))
            .displayItems(BLOCK_ITEM_GENERATOR)
            .displayItems(ITEM_GENERATOR)
            .build()
    );
}
