package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DSEnchantmentTags extends EnchantmentTagsProvider {
    public DSEnchantmentTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, DragonSurvival.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        addToVanillaTags();

        // Used in enchantments
        tag(exclusiveSet("anti_dragon"))
                .add(DSEnchantments.DRAGONSBANE)
                .add(DSEnchantments.DRAGONSBONK)
                .add(DSEnchantments.DRAGONSBOON);

        // Used in enchantments
        tag(exclusiveSet("evil_dragon")) // FIXME
                .add(DSEnchantments.BLOOD_SIPHON)
                .add(DSEnchantments.DRACONIC_SUPERIORITY)
                .add(DSEnchantments.MURDERERS_CUNNING)
                .add(DSEnchantments.OVERWHELMING_MIGHT)
                .add(DSEnchantments.CURSE_OF_OUTLAW);

        // Used in enchantments
        tag(exclusiveSet("good_dragon")) // FIXME
                .add(DSEnchantments.AERODYNAMIC_MASTERY)
                .add(DSEnchantments.COMBAT_RECOVERY)
                .add(DSEnchantments.SACRED_SCALES)
                .add(DSEnchantments.UNBREAKABLE_SPIRIT)
                .add(DSEnchantments.CURSE_OF_KINDNESS);

        // Used in enchantments
        tag(exclusiveSet("size_changing"))
                .add(DSEnchantments.SHRINK);
    }

    private void addToVanillaTags() {
        // Enchantments in this tag...
        // - won't be enchanted on to random loot, traded equipment or equipment from spawned mobs
        // - won't appear in the enchantment table
        // - won't naturally appear in trades
        // - double trade prices
        // (these behaviours can be changed by adding them manually to other specific vanilla enchantment tags)
        tag(EnchantmentTags.TREASURE).add(DSEnchantments.DRAGONSBONK);
    }

    private static TagKey<Enchantment> exclusiveSet(@NotNull final String path) {
        return key("exclusive_set/" + path);
    }

    private static TagKey<Enchantment> key(@NotNull final String path) {
        return TagKey.create(Registries.ENCHANTMENT, DragonSurvival.res(path));
    }
}
