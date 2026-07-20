package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.PrimordialAnchorBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SkeletonPieceBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.DragonHunterWeapon;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import com.geckolib.renderer.internal.GeckolibItemSpecialRenderer;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.properties.select.DisplayContext;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VaultBlock;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public class DataItemModelProvider extends ModelProvider {
    private static final String BLOCK_FOLDER = "block";
    private static final String ITEM_FOLDER = "item";
    private static final TextureSlot PRIMORDIAL_ANCHOR_SLOT = TextureSlot.create("2");
    private static final TextureSlot SKELETON_TEXTURE_SLOT = TextureSlot.create("skeleton_texture");

    private static final Set<String> BLOCK_MODELS_AS_BASIC = Set.of(
            "door",
            "source",
            "helmet",
            "beacon"
    );

    private static final Set<String> MANUALLY_AUTHORED = Set.of(
            "ambusher_spawn_egg",
            "dragon_hunting_mesh",
            "griffin_spawn_egg",
            "hound_spawn_egg",
            "hunter_partisan",
            "hunter_partisan_diamond",
            "hunter_partisan_netherite",
            "knight_spawn_egg",
            "leader_spawn_egg",
            "spearman_spawn_egg",
            "dragon_soul"
    );

    private static final Set<String> GUI_ICON_KEYS = Set.of(
            "dark_key",
            "hunter_key",
            "light_key"
    );

    // See createDragonSoul in DataBlockStateProvider
    private static final Set<String> REGISTERED_ELSEWHERE = Set.of(
            "dragon_soul"
    );

    public DataItemModelProvider(final PackOutput output, final String modId) {
        super(output, modId);
    }

    @Override
    protected void registerModels(@NotNull final BlockModelGenerators blockModels, @NotNull final ItemModelGenerators itemModels) {
        registerItemModels(blockModels, itemModels);
    }

    public static void registerItemModels(@NotNull final BlockModelGenerators blockModels, @NotNull final ItemModelGenerators itemModels) {
        DSItems.REGISTRY.getEntries().forEach(holder -> {
            Item item = holder.get();
            String name = holder.getId().getPath();

            if (REGISTERED_ELSEWHERE.contains(name)) {
                return;
            }

            if (GUI_ICON_KEYS.contains(name)) {
                registerKeyItemDefinition(itemModels, item, name);
                return;
            }

            if (MANUALLY_AUTHORED.contains(name)) {
                itemModels.declareCustomModelItem(item);
                return;
            }

            if (item instanceof BlockItem blockItem) {
                registerBlockItemModel(blockModels, itemModels, blockItem, name);
                return;
            }

            if (item instanceof DragonHunterWeapon) {
                itemModels.generateFlatItem(item, ModelTemplates.FLAT_HANDHELD_ITEM);
                return;
            }

            itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        });
    }

    private static void registerKeyItemDefinition(@NotNull final ItemModelGenerators itemModels, final Item item, final String name) {
        ItemModel.Unbaked baseModel = ItemModelUtils.specialModel(itemModel(name), new GeckolibItemSpecialRenderer.Unbaked<>());
        ItemModel.Unbaked guiModel = ItemModelUtils.plainModel(itemModel(name + "_icon"));

        itemModels.itemModelOutput.accept(
                item,
                ItemModelUtils.select(
                        new DisplayContext(),
                        baseModel,
                        ItemModelUtils.when(ItemDisplayContext.GUI, guiModel)
                )
        );
    }

    private static void registerBlockItemModel(@NotNull final BlockModelGenerators blockModels, @NotNull final ItemModelGenerators itemModels, final BlockItem item, final String name) {
        Block block = item.getBlock();

        if (BLOCK_MODELS_AS_BASIC.stream().anyMatch(name::contains)) {
            itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        } else if (block instanceof SkeletonPieceBlock) {
            registerSkeletonItemModel(itemModels, item, name);
        } else if (block instanceof VaultBlock) {
            registerParentedItemModel(itemModels, item, name, blockModel(name + "_inactive"));
        } else if (block instanceof TreasureBlock) {
            registerParentedItemModel(itemModels, item, name, blockModel(name + "2"));
        } else if (block instanceof PrimordialAnchorBlock) {
            registerPrimordialAnchorItemModel(itemModels, item, name);
        } else {
            registerParentedItemModel(itemModels, item, name, blockModel(name));
        }
    }

    private static void registerSkeletonItemModel(@NotNull final ItemModelGenerators itemModels, final Item item, final String name) {
        String[] split = name.split("_skin");
        String skin = split[1].substring(split[1].length() - 1);
        Material skeletonTexture = blockMaterial("skeleton_dragon_" + skin);
        Identifier model = new ModelTemplate(Optional.of(blockModel(split[0])), Optional.empty(), SKELETON_TEXTURE_SLOT, TextureSlot.PARTICLE)
                .create(itemModel(name), new TextureMapping()
                        .put(SKELETON_TEXTURE_SLOT, skeletonTexture)
                        .put(TextureSlot.PARTICLE, skeletonTexture), itemModels.modelOutput);

        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
    }

    private static void registerPrimordialAnchorItemModel(@NotNull final ItemModelGenerators itemModels, final Item item, final String name) {
        Identifier model = new ModelTemplate(Optional.of(blockModel(name)), Optional.empty(), PRIMORDIAL_ANCHOR_SLOT)
                .create(itemModel(name), TextureMapping.singleSlot(PRIMORDIAL_ANCHOR_SLOT, blockMaterial("primordial_anchor_empty")), itemModels.modelOutput);

        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
    }

    private static void registerParentedItemModel(@NotNull final ItemModelGenerators itemModels, final Item item, final String name, final Identifier parent) {
        Identifier model = new ModelTemplate(Optional.of(parent), Optional.empty()).create(itemModel(name), new TextureMapping(), itemModels.modelOutput);
        itemModels.itemModelOutput.accept(item, ItemModelUtils.plainModel(model));
    }

    private static Identifier blockModel(final String path) {
        return DragonSurvival.res(BLOCK_FOLDER + "/" + path);
    }

    private static Identifier blockTexture(final String path) {
        return DragonSurvival.res(BLOCK_FOLDER + "/" + path);
    }

    private static Material blockMaterial(final String path) {
        return new Material(blockTexture(path));
    }

    private static Identifier itemModel(final String path) {
        return DragonSurvival.res(ITEM_FOLDER + "/" + path);
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Survival Item models";
    }
}
