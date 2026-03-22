package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DataSpriteSourceProvider extends SpriteSourceProvider {

    private static final Identifier BLOCKS_ATLAS = Identifier.withDefaultNamespace("blocks");

    public DataSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, lookupProvider, modId);
    }

    @Override
    protected void gather() {
        atlas(BLOCKS_ATLAS)
                .addSource(new SingleFile(Identifier.fromNamespaceAndPath(MODID, "gui/dragon_claws_axe"), Optional.empty()))
                .addSource(new SingleFile(Identifier.fromNamespaceAndPath(MODID, "gui/dragon_claws_pickaxe"), Optional.empty()))
                .addSource(new SingleFile(Identifier.fromNamespaceAndPath(MODID, "gui/dragon_claws_shovel"), Optional.empty()))
                .addSource(new SingleFile(Identifier.fromNamespaceAndPath(MODID, "gui/dragon_claws_sword"), Optional.empty()));
    }
}
