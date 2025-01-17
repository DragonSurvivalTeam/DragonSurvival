package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonAltarBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.HelmetBlock;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class DataBlockModelProvider extends BlockModelProvider {
    public DataBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, DragonSurvival.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        DSBlocks.REGISTRY.getEntries().forEach((holder) -> {
            if (holder.get() instanceof DragonAltarBlock) {
                withExistingParent(holder.getId().getPath(), BLOCK_FOLDER + "/orientable")
                        .texture("down", ResourceLocation.fromNamespaceAndPath(modid, BLOCK_FOLDER + "/" + holder.getId().getPath() + "_top"))
                        .texture("east", ResourceLocation.fromNamespaceAndPath(modid, BLOCK_FOLDER + "/" + holder.getId().getPath() + "_east"))
                        .texture("north", ResourceLocation.fromNamespaceAndPath(modid, BLOCK_FOLDER + "/" + holder.getId().getPath() + "_north"))
                        .texture("particle", ResourceLocation.fromNamespaceAndPath(modid, BLOCK_FOLDER + "/" + holder.getId().getPath() + "_top"))
                        .texture("south", ResourceLocation.fromNamespaceAndPath(modid, BLOCK_FOLDER + "/" + holder.getId().getPath() + "_south"))
                        .texture("up", ResourceLocation.fromNamespaceAndPath(modid, BLOCK_FOLDER + "/" + holder.getId().getPath() + "_top"))
                        .texture("west", ResourceLocation.fromNamespaceAndPath(modid, BLOCK_FOLDER + "/" + holder.getId().getPath() + "_west"));
            } else if (holder.get() instanceof HelmetBlock) {
                withExistingParent(holder.getId().getPath(), BLOCK_FOLDER + "/" + "skull")
                        .texture("all", ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "block/" + holder.getId().getPath()));
            }
        });
    }
}
