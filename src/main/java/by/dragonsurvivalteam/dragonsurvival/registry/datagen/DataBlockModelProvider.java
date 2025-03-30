package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonAltarBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.HelmetBlock;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;

public class DataBlockModelProvider extends BlockModelProvider {
    private static final String PREFIX = BLOCK_FOLDER + "/"; 
    
    public DataBlockModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, DragonSurvival.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        DSBlocks.REGISTRY.getEntries().forEach((holder) -> {
            if (holder.get() instanceof DragonAltarBlock) {
                withExistingParent(holder.getId().getPath(), BLOCK_FOLDER + "/orientable")
                        .texture("down", DragonSurvival.res(PREFIX + holder.getId().getPath() + "_top"))
                        .texture("east", DragonSurvival.res(PREFIX + holder.getId().getPath() + "_east"))
                        .texture("north", DragonSurvival.res(PREFIX + holder.getId().getPath() + "_north"))
                        .texture("particle", DragonSurvival.res(PREFIX + holder.getId().getPath() + "_top"))
                        .texture("south", DragonSurvival.res(PREFIX + holder.getId().getPath() + "_south"))
                        .texture("up", DragonSurvival.res(PREFIX + holder.getId().getPath() + "_top"))
                        .texture("west", DragonSurvival.res(PREFIX + holder.getId().getPath() + "_west"));
            } else if (holder.get() instanceof HelmetBlock) {
                withExistingParent(holder.getId().getPath(), BLOCK_FOLDER + "/" + "skull")
                        .texture("all", DragonSurvival.res(PREFIX + holder.getId().getPath()));
            } else if (holder == DSBlocks.CHOCOLATE_DRAGON_TREASURE) {
                ResourceLocation top = DragonSurvival.res(PREFIX + holder.getId().getPath());
                ResourceLocation side = DragonSurvival.res(PREFIX + holder.getId().getPath() + "_side");
                ResourceLocation bottom = DragonSurvival.res(PREFIX + holder.getId().getPath() + "_bottom");

                orientableWithBottom(holder.getId().getPath(), side, side, bottom, top);

                for (int height : List.of(2, 4, 6, 8, 10, 12, 14)) {
                    orientableWithBottom(holder.getId().getPath() + height, side, side, bottom, top)
                            .element()
                            .face(Direction.DOWN).texture("#bottom").cullface(Direction.DOWN).end()
                            .face(Direction.UP).texture("#top").cullface(Direction.UP).end()
                            .face(Direction.NORTH).texture("#side").cullface(Direction.NORTH).end()
                            .face(Direction.SOUTH).texture("#side").cullface(Direction.SOUTH).end()
                            .face(Direction.WEST).texture("#side").cullface(Direction.WEST).end()
                            .face(Direction.EAST).texture("#side").cullface(Direction.EAST).end()
                            .to(16, height, 16);
                }
            }
        });
    }
}
