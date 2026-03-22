package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class DataItemModelProvider extends ModelProvider {
    private static final ModelTemplate SPAWN_EGG_TEMPLATE = new ModelTemplate(
            java.util.Optional.of(Identifier.withDefaultNamespace("item/template_spawn_egg")),
            java.util.Optional.empty()
    );
    private static final Set<String> MANUALLY_AUTHORED = Set.of(
            "dragon_hunting_mesh",
            "hunter_partisan",
            "hunter_partisan_diamond",
            "hunter_partisan_netherite",
            "hunter_key",
            "dark_key",
            "light_key",
            "dragon_soul"
    );

    public DataItemModelProvider(final PackOutput output, final String modId) {
        super(output, modId);
    }

    @Override
    protected void registerModels(@NotNull final BlockModelGenerators blockModels, @NotNull final ItemModelGenerators itemModels) {
        registerItemModels(itemModels);
    }

    public static void registerItemModels(@NotNull final ItemModelGenerators itemModels) {
        DSItems.REGISTRY.getEntries().forEach(holder -> {
            Item item = holder.get();
            String name = holder.getId().getPath();

            if (item instanceof BlockItem) {
                return;
            }

            if (MANUALLY_AUTHORED.contains(name)) {
                itemModels.declareCustomModelItem(item);
                return;
            }

            if (item instanceof SpawnEggItem) {
                Identifier model = SPAWN_EGG_TEMPLATE.create(item, new TextureMapping(), itemModels.modelOutput);
                itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
                return;
            }

            if (item.components().has(DataComponents.WEAPON)) {
                itemModels.generateFlatItem(item, ModelTemplates.FLAT_HANDHELD_ITEM);
                return;
            }

            itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        });
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Survival Item models";
    }
}
