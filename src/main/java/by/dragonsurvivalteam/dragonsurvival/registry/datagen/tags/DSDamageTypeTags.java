package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DSDamageTypeTags extends DamageTypeTagsProvider {
    public static TagKey<DamageType> DRAGON_BREATH = key("dragon_breath");
    public static TagKey<DamageType> DRAGON_MAGIC = key("dragon_magic");

    public DSDamageTypeTags(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, DragonSurvival.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        addToVanillaTags();

        tag(DRAGON_BREATH)
                .add(DSDamageTypes.DRAGON_BREATH)
                .add(DSDamageTypes.BURN)
                .add(DSDamageTypes.DRAIN)
                .add(DSDamageTypes.LIGHTNING_BREATH);

        tag(DRAGON_MAGIC)
                .add(DSDamageTypes.FIRE_BREATH)
                .add(DSDamageTypes.BURN)
                .add(DSDamageTypes.POISON_BREATH)
                .add(DSDamageTypes.DRAIN)
                .add(DSDamageTypes.LIGHTNING_BREATH)
                .add(DSDamageTypes.SPECTRAL_IMPACT)
                .add(DSDamageTypes.BALL_LIGHTNING)
                .add(DSDamageTypes.DRAGON_BREATH)
                .add(DamageTypes.FIREBALL);

        // Used in enchantments
        tag(key("anti_dragon")).add(DSDamageTypes.ANTI_DRAGON);
    }

    private void addToVanillaTags() {
        tag(DamageTypeTags.BYPASSES_ARMOR)
                .add(DSDamageTypes.DRAIN)
                .add(DSDamageTypes.BURN)
                .add(DSDamageTypes.SPECTRAL_IMPACT);

        tag(DamageTypeTags.BYPASSES_COOLDOWN)
                .add(DSDamageTypes.SPIKE);

        tag(DamageTypeTags.IS_FIRE)
                .add(DSDamageTypes.BURN)
                .add(DSDamageTypes.FIRE_BREATH);

        tag(DamageTypeTags.IS_LIGHTNING)
                .add(DSDamageTypes.LIGHTNING_BREATH);

        tag(DamageTypeTags.NO_KNOCKBACK)
                .add(DSDamageTypes.BURN)
                .add(DSDamageTypes.DRAIN)
                .add(DSDamageTypes.CRUSHED)
                .add(DSDamageTypes.DEHYDRATION)
                .add(DSDamageTypes.WATER_BURN)
                .add(DSDamageTypes.RAIN_BURN)
                .add(DSDamageTypes.BALL_LIGHTNING);
    }

    private static TagKey<DamageType> key(@NotNull final String name) {
        return TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, name));
    }
}
