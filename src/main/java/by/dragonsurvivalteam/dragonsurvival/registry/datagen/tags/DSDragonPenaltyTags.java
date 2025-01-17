package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalties;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DSDragonPenaltyTags extends TagsProvider<DragonPenalty> {
    @Translation(comments = "Cave Dragon Penalties")
    public static final TagKey<DragonPenalty> CAVE = key("cave_dragon");
    @Translation(comments = "Sea Dragon Penalties")
    public static final TagKey<DragonPenalty> SEA = key("sea_dragon");
    @Translation(comments = "Forest Dragon Penalties")
    public static final TagKey<DragonPenalty> FOREST = key("forest_dragon");

    public DSDragonPenaltyTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider, @Nullable final ExistingFileHelper helper) {
        super(output, DragonPenalty.REGISTRY, provider, DragonSurvival.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(CAVE)
                .add(DragonPenalties.SNOW_AND_RAIN_WEAKNESS)
                .add(DragonPenalties.WATER_WEAKNESS)
                .add(DragonPenalties.ITEM_BLACKLIST)
                .add(DragonPenalties.WATER_POTION_WEAKNESS)
                .add(DragonPenalties.SNOWBALL_WEAKNESS)
                .add(DragonPenalties.WATER_SPLASH_POTION_WEAKNESS);

        tag(SEA)
                .add(DragonPenalties.THIN_SKIN)
                .add(DragonPenalties.ITEM_BLACKLIST);

        tag(FOREST)
                .add(DragonPenalties.FEAR_OF_DARKNESS)
                .add(DragonPenalties.ITEM_BLACKLIST);
    }

    public static TagKey<DragonPenalty> key(final String path) {
        return TagKey.create(DragonPenalty.REGISTRY, DragonSurvival.res(path));
    }
}
