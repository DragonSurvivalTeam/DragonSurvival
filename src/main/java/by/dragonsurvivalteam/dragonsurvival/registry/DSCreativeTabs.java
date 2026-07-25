package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SkeletonPieceBlock;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.Arrays;
import java.util.List;

public class DSCreativeTabs {
    @Translation(comments = "Dragon Survival")
    private static final String CREATIVE_TAB = Translation.Type.GUI.wrap("creative_tab");

    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DragonSurvival.MODID);

    private static final List<RegistryObject<Item>> HIDDEN = Arrays.asList(
            // Test items
            DSItems.MAGIC_STICK,
            // Dummy items
            DSItems.HUNTING_NET,
            DSItems.LIGHTNING_TEXTURE_ITEM,
            DSItems.BOLAS,
            DSItems.ACTIVATED_DRAGON_BEACON,
            DSItems.FOREST_ICON,
            DSItems.CAVE_ICON,
            DSItems.SEA_ICON,
            DSItems.FOREST_FULL_ICON,
            DSItems.CAVE_FULL_ICON,
            DSItems.SEA_FULL_ICON,
            DSItems.ACTIVATED_DRAGON_BEACON,
            DSItems.CAVE_BEACON,
            DSItems.FOREST_BEACON,
            DSItems.SEA_BEACON
    );

    private static final CreativeModeTab.DisplayItemsGenerator BLOCK_ITEM_GENERATOR = (parameters, output) -> DSBlocks.REGISTRY.getEntries().forEach(entry -> {
            if (entry.get() instanceof SkeletonPieceBlock) {
            return;
        }

                output.accept(entry.get());
    });

    private static final CreativeModeTab.DisplayItemsGenerator ITEM_GENERATOR = (parameters, output) -> DSItems.REGISTRY.getEntries().forEach(entry -> {
        if (HIDDEN.contains(entry)) {
            return;
        }

            if (entry.get() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SkeletonPieceBlock) {
            return;
        }

                output.accept(entry.get());
    });

    public static RegistryObject<CreativeModeTab> DS_TAB = REGISTRY.register("dragon_survival", () -> CreativeModeTab.builder()
            .icon(() -> new ItemStack(DSItems.ELDER_DRAGON_BONE.get()))
            .title(Component.translatable(CREATIVE_TAB))
            .displayItems(BLOCK_ITEM_GENERATOR)
            .displayItems(ITEM_GENERATOR)
            .build()
    );
}
