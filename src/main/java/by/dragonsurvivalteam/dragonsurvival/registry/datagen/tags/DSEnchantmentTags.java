package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
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
        tag(exclusiveSet("dark_dragon"))
                .add(DSEnchantments.BLOOD_SIPHON)
                .add(DSEnchantments.DRACONIC_SUPERIORITY)
                .add(DSEnchantments.MURDERERS_CUNNING)
                .add(DSEnchantments.OVERWHELMING_MIGHT)
                .add(DSEnchantments.CURSE_OF_OUTLAW);

        // Used in enchantments
        tag(exclusiveSet("light_dragon"))
                .add(DSEnchantments.AERODYNAMIC_MASTERY)
                .add(DSEnchantments.COMBAT_RECOVERY)
                .add(DSEnchantments.SACRED_SCALES)
                .add(DSEnchantments.UNBREAKABLE_SPIRIT)
                .add(DSEnchantments.CURSE_OF_KINDNESS);

        // Used in enchantments
        tag(exclusiveSet("size_changing")) // FIXME :: rename here and in the enchantments
                .add(DSEnchantments.SHRINK);
    }

    private void addToVanillaTags() {
        /* Explanation about the tags:
            - treasure: included in other tags
            - non_treasure: included in other tags
            - double_trade_price: contains 'treasure'
            - in_enchanting_table: contains 'non_treasure'
            - on_mob_spawn_equipment: contains 'non_treasure'
            - on_trade_equipment: contains 'non_treasure'
            - on_random_loot: contains 'non_treasure'
            - tradeable: contains 'non_treasure'
        */
    }

    private static TagKey<Enchantment> exclusiveSet(@NotNull final String path) {
        return key("exclusive_set/" + path);
    }

    private static TagKey<Enchantment> key(@NotNull final String path) {
        return TagKey.create(Registries.ENCHANTMENT, DragonSurvival.res(path));
    }
}
