package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
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
    @Translation(comments = "Dragon Ability Sorting Order")
    public static final TagKey<DragonAbility> ORDER = key("order");

    @Translation(comments = "Cave Dragon Abilities")
    public static final TagKey<DragonAbility> CAVE = key("cave_dragon");
    @Translation(comments = "Sea Dragon Abilities")
    public static final TagKey<DragonAbility> SEA = key("sea_dragon");
    @Translation(comments = "Forest Dragon Abilities")
    public static final TagKey<DragonAbility> FOREST = key("forest_dragon");

    public DSDragonAbilityTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider, @Nullable final ExistingFileHelper helper) {
        super(output, DragonAbility.REGISTRY, provider, DragonSurvival.MODID, helper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(CAVE)
                // Active
                .add(CaveDragonAbilities.NETHER_BREATH)
                .add(CaveDragonAbilities.FIRE_BALL)
                .add(CaveDragonAbilities.FURNACE_HEAT)
                .add(CaveDragonAbilities.LAVA_VISION)
                .add(CaveDragonAbilities.STURDY_SKIN)
                .add(CaveDragonAbilities.FRIENDLY_FIRE)
                // Passive (manual leveling)
                .add(CaveDragonAbilities.CAVE_MAGIC)
                .add(CaveDragonAbilities.CAVE_ATHLETICS)
                .add(CaveDragonAbilities.CONTRAST_SHOWER)
                .add(CaveDragonAbilities.BURN)
                // Passive
                .add(CaveDragonAbilities.FIRE_IMMUNITY)
                .add(CaveDragonAbilities.LAVA_SWIMMING)
                .add(CaveDragonAbilities.CAVE_CLAWS_AND_TEETH)
                .add(CaveDragonAbilities.CAVE_WINGS)
                .add(CaveDragonAbilities.CAVE_SPIN);

        tag(SEA)
                // Active
                .add(SeaDragonAbilities.STORM_BREATH)
                .add(SeaDragonAbilities.BALL_LIGHTNING)
                .add(SeaDragonAbilities.SEA_EYES)
                .add(SeaDragonAbilities.ORE_GLOW)
                .add(SeaDragonAbilities.SOUL_REVELATION)
                // Passive (manual leveling)
                .add(SeaDragonAbilities.SEA_MAGIC)
                .add(SeaDragonAbilities.SEA_ATHLETICS)
                .add(SeaDragonAbilities.HYDRATION)
                .add(SeaDragonAbilities.SPECTRAL_IMPACT)
                // Passive
                .add(SeaDragonAbilities.ELECTRIC_IMMUNITY)
                .add(SeaDragonAbilities.SEA_CLAWS_AND_TEETH)
                .add(SeaDragonAbilities.AMPHIBIOUS)
                .add(SeaDragonAbilities.DIVER)
                .add(SeaDragonAbilities.SEA_WINGS)
                .add(SeaDragonAbilities.SEA_SPIN);

        tag(FOREST)
                // Active
                .add(ForestDragonAbilities.FOREST_BREATH)
                .add(ForestDragonAbilities.SPIKE)
                .add(ForestDragonAbilities.HUNTER)
                .add(ForestDragonAbilities.SUN_BREATH)
                .add(ForestDragonAbilities.INSPIRATION)
                // Passive (manual leveling)
                .add(ForestDragonAbilities.FOREST_MAGIC)
                .add(ForestDragonAbilities.FOREST_ATHLETICS)
                .add(ForestDragonAbilities.LIGHT_IN_DARKNESS)
                .add(ForestDragonAbilities.CLIFFHANGER)
                // Passive
                .add(ForestDragonAbilities.FOREST_IMMUNITY)
                .add(ForestDragonAbilities.FOREST_CLAWS_AND_TEETH)
                .add(ForestDragonAbilities.FOREST_WINGS)
                .add(ForestDragonAbilities.FOREST_SPIN);

        tag(ORDER) // TODO :: if the tags are added instead, are the orders within them respected / guaranteed?
                // Cave :: active
                .add(CaveDragonAbilities.NETHER_BREATH)
                .add(CaveDragonAbilities.FIRE_BALL)
                .add(CaveDragonAbilities.FURNACE_HEAT)
                .add(CaveDragonAbilities.LAVA_VISION)
                .add(CaveDragonAbilities.STURDY_SKIN)
                .add(CaveDragonAbilities.FRIENDLY_FIRE)
                // Cave :: passive (manual leveling)
                .add(CaveDragonAbilities.CAVE_MAGIC)
                .add(CaveDragonAbilities.CAVE_ATHLETICS)
                .add(CaveDragonAbilities.CONTRAST_SHOWER)
                .add(CaveDragonAbilities.BURN)
                // Cave :: passive
                .add(CaveDragonAbilities.FIRE_IMMUNITY)
                .add(CaveDragonAbilities.LAVA_SWIMMING)
                .add(CaveDragonAbilities.CAVE_CLAWS_AND_TEETH)
                .add(CaveDragonAbilities.CAVE_WINGS)
                .add(CaveDragonAbilities.CAVE_SPIN)
                // Forest :: active
                .add(ForestDragonAbilities.FOREST_BREATH)
                .add(ForestDragonAbilities.SPIKE)
                .add(ForestDragonAbilities.HUNTER)
                .add(ForestDragonAbilities.SUN_BREATH)
                .add(ForestDragonAbilities.INSPIRATION)
                // Forest :: passive (manual leveling)
                .add(ForestDragonAbilities.FOREST_MAGIC)
                .add(ForestDragonAbilities.FOREST_ATHLETICS)
                .add(ForestDragonAbilities.LIGHT_IN_DARKNESS)
                .add(ForestDragonAbilities.CLIFFHANGER)
                // Forest :: passive
                .add(ForestDragonAbilities.FOREST_IMMUNITY)
                .add(ForestDragonAbilities.FOREST_CLAWS_AND_TEETH)
                .add(ForestDragonAbilities.FOREST_WINGS)
                .add(ForestDragonAbilities.FOREST_SPIN)
                // Sea :: active
                .add(SeaDragonAbilities.STORM_BREATH)
                .add(SeaDragonAbilities.BALL_LIGHTNING)
                .add(SeaDragonAbilities.SEA_EYES)
                .add(SeaDragonAbilities.ORE_GLOW)
                .add(SeaDragonAbilities.SOUL_REVELATION)
                // Sea :: passive (manual leveling)
                .add(SeaDragonAbilities.SEA_MAGIC)
                .add(SeaDragonAbilities.SEA_ATHLETICS)
                .add(SeaDragonAbilities.HYDRATION)
                .add(SeaDragonAbilities.SPECTRAL_IMPACT)
                // Sea :: passive
                .add(SeaDragonAbilities.ELECTRIC_IMMUNITY)
                .add(SeaDragonAbilities.SEA_CLAWS_AND_TEETH)
                .add(SeaDragonAbilities.AMPHIBIOUS)
                .add(SeaDragonAbilities.DIVER)
                .add(SeaDragonAbilities.SEA_WINGS)
                .add(SeaDragonAbilities.SEA_SPIN);
    }

    public static TagKey<DragonAbility> key(final String path) {
        return TagKey.create(DragonAbility.REGISTRY, DragonSurvival.res(path));
    }
}
