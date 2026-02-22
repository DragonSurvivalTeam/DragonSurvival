package by.dragonsurvivalteam.dragonsurvival.registry.datagen.datapacks;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSDragonAbilityTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class NoPenaltiesAbilityProvider extends KeyTagProvider<DragonAbility> {

    public NoPenaltiesAbilityProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, DragonAbility.REGISTRY, lookupProvider, DragonSurvival.MODID);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(DSDragonAbilityTags.CAVE).remove(CaveDragonAbilities.CONTRAST_SHOWER);
        tag(DSDragonAbilityTags.SEA).remove(SeaDragonAbilities.HYDRATION);
        tag(DSDragonAbilityTags.FOREST).remove(ForestDragonAbilities.LIGHT_IN_DARKNESS);
    }
}
