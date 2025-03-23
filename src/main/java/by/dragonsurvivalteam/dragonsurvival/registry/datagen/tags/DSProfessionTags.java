package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DSProfessionTags extends TagsProvider<VillagerProfession> {
    @Translation(comments = "Professions that cannot be pillaged")
    public static final TagKey<VillagerProfession> PILLAGE_BLACKLIST = TagKey.create(BuiltInRegistries.VILLAGER_PROFESSION.key(), DragonSurvival.res("pillage_blacklist"));

    public DSProfessionTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> lookup, @Nullable final ExistingFileHelper helper) {
        super(output, BuiltInRegistries.VILLAGER_PROFESSION.key(), lookup, DragonSurvival.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(PILLAGE_BLACKLIST);
    }
}
