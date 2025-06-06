package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.PrimordialAnchorBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class DataItemModelProvider extends ItemModelProvider {
    public DataItemModelProvider(final PackOutput output, final ExistingFileHelper existingFileHelper) {
        super(output, DragonSurvival.MODID, existingFileHelper);
    }

    private static final List<String> BLOCK_MODELS_AS_BASIC = List.of(
            "door",
            "source",
            "helmet",
            "beacon"
    );

    private static final List<String> MANUALLY_AUTHORED = List.of(
            "dragon_hunting_mesh",
            "hunter_partisan",
            "hunter_partisan_diamond",
            "hunter_partisan_netherite",
            "hunter_key",
            "dark_key",
            "light_key",
            "dragon_soul"
    );

    @Override
    protected void registerModels() {
        DSItems.REGISTRY.getEntries().forEach((holder) -> {
            if (holder.get() instanceof BlockItem blockItem && BLOCK_MODELS_AS_BASIC.stream().noneMatch(blockItem.toString()::contains)) {
                if (blockItem.toString().contains("skeleton")) {
                    // Parse the string up to "_skin"
                    String[] split = holder.getId().getPath().split("_skin");

                    // The last character has the number for the skin to select, so parse it
                    String skin = split[1].substring(split[1].length() - 1);

                    getBuilder(blockItem.toString())
                            .parent(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, BLOCK_FOLDER + "/" + split[0])))
                            .texture("skeleton_texture", ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, BLOCK_FOLDER + "/" + "skeleton_dragon_" + skin))
                            .transforms().transform(ItemDisplayContext.GUI).rotation(30, 160, 0).scale(0.5f).end();
                } else if (blockItem.toString().contains("vault")) {
                    getBuilder(blockItem.toString()).parent(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, BLOCK_FOLDER + "/" + holder.getId().getPath()) + "_inactive"));
                } else if (blockItem.getBlock() instanceof TreasureBlock) {
                    // Show the 1 layer texture
                    ResourceLocation parent = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, BLOCK_FOLDER + "/" + holder.getId().getPath() + "2");
                    getBuilder(blockItem.toString()).parent(new ModelFile.UncheckedModelFile(parent));
                } else if (blockItem.getBlock() instanceof PrimordialAnchorBlock) {
                    getBuilder(blockItem.toString())
                            .parent(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, BLOCK_FOLDER + "/" + holder.getId().getPath())))
                            .texture("1", ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, BLOCK_FOLDER + "/" + "primordial_anchor_empty"));
                } else {
                    getBuilder(blockItem.toString()).parent(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, BLOCK_FOLDER + "/" + holder.getId().getPath())));
                }
            } else {
                if (MANUALLY_AUTHORED.stream().anyMatch(holder.getId().getPath()::contains)) {
                    return;
                }

                if (holder.get() instanceof SpawnEggItem item) {
                    getBuilder(item.toString()).parent(new ModelFile.UncheckedModelFile(ResourceLocation.withDefaultNamespace("item/template_spawn_egg")));
                } else if (holder.get() instanceof SwordItem item) {
                    ResourceLocation itemLoc = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item));
                    getBuilder(itemLoc.toString())
                            .parent(new ModelFile.UncheckedModelFile("item/handheld"))
                            .texture("layer0", ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "item/" + itemLoc.getPath()));
                } else {
                    basicItem(holder.get());
                }
            }
        });
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Survival Item models";
    }
}