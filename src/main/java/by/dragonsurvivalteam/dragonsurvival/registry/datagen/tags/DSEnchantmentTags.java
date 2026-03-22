package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEnchantments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DSEnchantmentTags extends EnchantmentTagsProvider {
    @Translation(comments = "Anti-Dragon (Exclusive Set)")
    public static final TagKey<Enchantment> ANTI_DRAGON = exclusiveSet("anti_dragon");

    public DSEnchantmentTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, DragonSurvival.MODID);
    }

    @Override
    protected void addTags(@NotNull HolderLookup.Provider provider) {
        addToVanillaTags();

        // Used in enchantments
        tag(ANTI_DRAGON)
                .add(DSEnchantments.DRAGONSBANE);

        // Used in enchantments
        tag(exclusiveSet("dark_dragon"))
                .addOptional(DSEnchantments.BLOOD_SIPHON)
                .addOptional(DSEnchantments.DRACONIC_SUPERIORITY)
                .addOptional(DSEnchantments.MURDERERS_CUNNING)
                .addOptional(DSEnchantments.OVERWHELMING_MIGHT)
                .addOptional(DSEnchantments.CURSE_OF_OUTLAW);

        // Used in enchantments
        tag(exclusiveSet("light_dragon"))
                .addOptional(DSEnchantments.AERODYNAMIC_MASTERY)
                .addOptional(DSEnchantments.COMBAT_RECOVERY)
                .addOptional(DSEnchantments.SACRED_SCALES)
                .addOptional(DSEnchantments.UNBREAKABLE_SPIRIT)
                .addOptional(DSEnchantments.CURSE_OF_KINDNESS);
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
