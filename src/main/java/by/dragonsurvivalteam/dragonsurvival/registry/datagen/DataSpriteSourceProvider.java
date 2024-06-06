package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SpriteSourceProvider;

public class DataSpriteSourceProvider extends SpriteSourceProvider {

    public DataSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId, ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, modId, existingFileHelper);
    }

    @Override
    protected void gather() {
        atlas(SpriteSourceProvider.BLOCKS_ATLAS)
                .addSource(new SingleFile(new ResourceLocation(DragonSurvivalMod.MODID, "te/star/cage"), Optional.empty()))
                .addSource(new SingleFile(new ResourceLocation(DragonSurvivalMod.MODID, "te/star/wind"), Optional.empty()))
                .addSource(new SingleFile(new ResourceLocation(DragonSurvivalMod.MODID, "te/star/open_eye"), Optional.empty()))
                .addSource(new SingleFile(new ResourceLocation(DragonSurvivalMod.MODID, "te/star/wind_vertical"), Optional.empty()))
                .addSource(new SingleFile(new ResourceLocation(DragonSurvivalMod.MODID, "gui/dragon_claws_axe"), Optional.empty()))
                .addSource(new SingleFile(new ResourceLocation(DragonSurvivalMod.MODID, "gui/dragon_claws_pickaxe"), Optional.empty()))
                .addSource(new SingleFile(new ResourceLocation(DragonSurvivalMod.MODID, "gui/dragon_claws_shovel"), Optional.empty()))
                .addSource(new SingleFile(new ResourceLocation(DragonSurvivalMod.MODID, "gui/dragon_claws_sword"), Optional.empty()));
    }
}
