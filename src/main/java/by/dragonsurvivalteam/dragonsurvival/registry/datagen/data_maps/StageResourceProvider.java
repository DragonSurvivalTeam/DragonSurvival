package by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDataMaps;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks.AncientDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class StageResourceProvider extends DataMapProvider {
    public StageResourceProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected void gather() {
        builder(DSDataMaps.STAGE_RESOURCES)
                .add(BuiltInDragonSpecies.CAVE_DRAGON, caveDragon(), false)
                .add(BuiltInDragonSpecies.FOREST_DRAGON, forestDragon(), false)
                .add(BuiltInDragonSpecies.SEA_DRAGON, seaDragon(), false);
    }

    private Map<ResourceKey<DragonStage>, StageResources.StageResource> caveDragon() {
        Map<ResourceKey<DragonStage>, StageResources.StageResource> resources = new HashMap<>();

        resources.put(DragonStages.newborn, new StageResources.StageResource(
                new StageResources.GrowthIcon(
                        DragonSurvival.res("textures/gui/custom/stage/cave/newborn_stage_hover.png"),
                        DragonSurvival.res("textures/gui/custom/stage/cave/newborn_stage_main.png")
                ),
                new StageResources.DefaultSkin(
                        DragonSurvival.res("textures/dragon/cave_dragon/newborn.png"),
                        DragonSurvival.res("textures/dragon/cave_dragon/newborn_glow.png")
                )
        ));

        resources.put(DragonStages.young, new StageResources.StageResource(
                new StageResources.GrowthIcon(
                        DragonSurvival.res("textures/gui/custom/stage/cave/young_stage_hover.png"),
                        DragonSurvival.res("textures/gui/custom/stage/cave/young_stage_main.png")
                ),
                new StageResources.DefaultSkin(
                        DragonSurvival.res("textures/dragon/cave_dragon/young.png"),
                        DragonSurvival.res("textures/dragon/cave_dragon/young_glow.png")
                )
        ));

        resources.put(DragonStages.adult, new StageResources.StageResource(
                new StageResources.GrowthIcon(
                        DragonSurvival.res("textures/gui/custom/stage/cave/adult_stage_hover.png"),
                        DragonSurvival.res("textures/gui/custom/stage/cave/adult_stage_main.png")
                ),
                new StageResources.DefaultSkin(
                        DragonSurvival.res("textures/dragon/cave_dragon/adult.png"),
                        DragonSurvival.res("textures/dragon/cave_dragon/adult_glow.png")
                )
        ));

        resources.put(AncientDatapack.ancient, new StageResources.StageResource(
                new StageResources.GrowthIcon(
                        DragonSurvival.res("textures/gui/custom/stage/cave/ancient_stage_hover.png"),
                        DragonSurvival.res("textures/gui/custom/stage/cave/ancient_stage_main.png")
                ),
                new StageResources.DefaultSkin(
                        DragonSurvival.res("textures/dragon/cave_dragon/ancient.png"),
                        DragonSurvival.res("textures/dragon/cave_dragon/ancient_glow.png")
                )
        ));

        return resources;
    }

    private Map<ResourceKey<DragonStage>, StageResources.StageResource> forestDragon() {
        Map<ResourceKey<DragonStage>, StageResources.StageResource> resources = new HashMap<>();

        resources.put(DragonStages.newborn, new StageResources.StageResource(
                new StageResources.GrowthIcon(
                        DragonSurvival.res("textures/gui/custom/stage/forest/newborn_stage_hover.png"),
                        DragonSurvival.res("textures/gui/custom/stage/forest/newborn_stage_main.png")
                ),
                new StageResources.DefaultSkin(
                        DragonSurvival.res("textures/dragon/forest_dragon/newborn.png"),
                        DragonSurvival.res("textures/dragon/forest_dragon/newborn_glow.png")
                )
        ));

        resources.put(DragonStages.young, new StageResources.StageResource(
                new StageResources.GrowthIcon(
                        DragonSurvival.res("textures/gui/custom/stage/forest/young_stage_hover.png"),
                        DragonSurvival.res("textures/gui/custom/stage/forest/young_stage_main.png")
                ),
                new StageResources.DefaultSkin(
                        DragonSurvival.res("textures/dragon/forest_dragon/young.png"),
                        DragonSurvival.res("textures/dragon/forest_dragon/young_glow.png")
                )
        ));

        resources.put(DragonStages.adult, new StageResources.StageResource(
                new StageResources.GrowthIcon(
                        DragonSurvival.res("textures/gui/custom/stage/forest/adult_stage_hover.png"),
                        DragonSurvival.res("textures/gui/custom/stage/forest/adult_stage_main.png")
                ),
                new StageResources.DefaultSkin(
                        DragonSurvival.res("textures/dragon/forest_dragon/adult.png"),
                        DragonSurvival.res("textures/dragon/forest_dragon/adult_glow.png")
                )
        ));

        resources.put(AncientDatapack.ancient, new StageResources.StageResource(
                new StageResources.GrowthIcon(
                        DragonSurvival.res("textures/gui/custom/stage/forest/ancient_stage_hover.png"),
                        DragonSurvival.res("textures/gui/custom/stage/forest/ancient_stage_main.png")
                ),
                new StageResources.DefaultSkin(
                        DragonSurvival.res("textures/dragon/forest_dragon/ancient.png"),
                        DragonSurvival.res("textures/dragon/forest_dragon/ancient_glow.png")
                )
        ));

        return resources;
    }

    private Map<ResourceKey<DragonStage>, StageResources.StageResource> seaDragon() {
        Map<ResourceKey<DragonStage>, StageResources.StageResource> resources = new HashMap<>();

        resources.put(DragonStages.newborn, new StageResources.StageResource(
                new StageResources.GrowthIcon(
                        DragonSurvival.res("textures/gui/custom/stage/sea/newborn_stage_hover.png"),
                        DragonSurvival.res("textures/gui/custom/stage/sea/newborn_stage_main.png")
                ),
                new StageResources.DefaultSkin(
                        DragonSurvival.res("textures/dragon/sea_dragon/newborn.png"),
                        DragonSurvival.res("textures/dragon/sea_dragon/newborn_glow.png")
                )
        ));

        resources.put(DragonStages.young, new StageResources.StageResource(
                new StageResources.GrowthIcon(
                        DragonSurvival.res("textures/gui/custom/stage/sea/young_stage_hover.png"),
                        DragonSurvival.res("textures/gui/custom/stage/sea/young_stage_main.png")
                ),
                new StageResources.DefaultSkin(
                        DragonSurvival.res("textures/dragon/sea_dragon/young.png"),
                        DragonSurvival.res("textures/dragon/sea_dragon/young_glow.png")
                )
        ));

        resources.put(DragonStages.adult, new StageResources.StageResource(
                new StageResources.GrowthIcon(
                        DragonSurvival.res("textures/gui/custom/stage/sea/adult_stage_hover.png"),
                        DragonSurvival.res("textures/gui/custom/stage/sea/adult_stage_main.png")
                ),
                new StageResources.DefaultSkin(
                        DragonSurvival.res("textures/dragon/sea_dragon/adult.png"),
                        DragonSurvival.res("textures/dragon/sea_dragon/adult_glow.png")
                )
        ));

        resources.put(AncientDatapack.ancient, new StageResources.StageResource(
                new StageResources.GrowthIcon(
                        DragonSurvival.res("textures/gui/custom/stage/sea/ancient_stage_hover.png"),
                        DragonSurvival.res("textures/gui/custom/stage/sea/ancient_stage_main.png")
                ),
                new StageResources.DefaultSkin(
                        DragonSurvival.res("textures/dragon/sea_dragon/ancient.png"),
                        DragonSurvival.res("textures/dragon/sea_dragon/ancient_glow.png")
                )
        ));

        return resources;
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Stage Resources";
    }
}
