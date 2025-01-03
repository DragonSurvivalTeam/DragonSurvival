package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.CaveDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.ForestDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.abilities.SeaDragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DSDragonAbilityTags extends TagsProvider<DragonAbility> {
    public static final TagKey<DragonAbility> ORDER = key("order");

    public DSDragonAbilityTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider, @Nullable final ExistingFileHelper helper) {
        super(output, DragonAbility.REGISTRY, provider, DragonSurvival.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(ORDER)
                // Cave :: active
                .add(CaveDragonAbilities.NETHER_BREATH)
                .add(CaveDragonAbilities.FIRE_BALL)
                .add(CaveDragonAbilities.STURDY_SKIN)
                .add(CaveDragonAbilities.LAVA_VISION)
                // Cave :: passive (manual leveling)
                .add(CaveDragonAbilities.CAVE_MAGIC)
                .add(CaveDragonAbilities.CAVE_ATHLETICS)
                .add(CaveDragonAbilities.CONTRAST_SHOWER)
                .add(CaveDragonAbilities.BURN)
                // Cave :: passive
                .add(CaveDragonAbilities.CAVE_CLAWS_AND_TEETH)
                .add(CaveDragonAbilities.CAVE_WINGS)
                .add(CaveDragonAbilities.FIRE_IMMUNITY)
                .add(CaveDragonAbilities.LAVA_SWIMMING)
                // Forest :: active
                .add(ForestDragonAbilities.POISON_BREATH)
                .add(ForestDragonAbilities.SPIKE)
                .add(ForestDragonAbilities.INSPIRATION)
                .add(ForestDragonAbilities.HUNTER)
                // Forest :: passive (manual leveling)
                .add(ForestDragonAbilities.FOREST_MAGIC)
                .add(ForestDragonAbilities.FOREST_ATHLETICS)
                .add(ForestDragonAbilities.LIGHT_IN_DARKNESS)
                .add(ForestDragonAbilities.CLIFFHANGER)
                // Forest :: passive
                .add(ForestDragonAbilities.FOREST_CLAWS_AND_TEETH)
                .add(ForestDragonAbilities.FOREST_WINGS)
                .add(ForestDragonAbilities.FOREST_IMMUNITY)
                // Sea :: active
                .add(SeaDragonAbilities.STORM_BREATH)
                .add(SeaDragonAbilities.BALL_LIGHTNING)
                .add(SeaDragonAbilities.SOUL_REVELATION)
                .add(SeaDragonAbilities.SEA_EYES)
                // Sea :: passive (manual leveling)
                .add(SeaDragonAbilities.SEA_MAGIC)
                .add(SeaDragonAbilities.SEA_ATHLETICS)
                .add(SeaDragonAbilities.HYDRATION)
                .add(SeaDragonAbilities.SPECTRAL_IMPACT)
                // Sea :: passive
                .add(SeaDragonAbilities.SEA_CLAWS_AND_TEETH)
                .add(SeaDragonAbilities.SEA_WINGS)
                .add(SeaDragonAbilities.ELECTRIC_IMMUNITY)
                .add(SeaDragonAbilities.AMPHIBIOUS);
    }

    public static TagKey<DragonAbility> key(final String path) {
        return TagKey.create(DragonAbility.REGISTRY, DragonSurvival.res(path));
    }
}
