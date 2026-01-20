package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DSProfessionTags extends TagsProvider<VillagerProfession> {
    @Translation(comments = "Professions that cannot be pillaged")
    public static final TagKey<VillagerProfession> PILLAGE_BLACKLIST = TagKey.create(BuiltInRegistries.VILLAGER_PROFESSION.key(), DragonSurvival.res("pillage_blacklist"));

    public DSProfessionTags(PackOutput output, ResourceKey<? extends Registry<VillagerProfession>> registryKey, CompletableFuture<HolderLookup.Provider> lookupProvider, String modId) {
        super(output, registryKey, lookupProvider, modId);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        getOrCreateRawBuilder(PILLAGE_BLACKLIST);
    }
}
