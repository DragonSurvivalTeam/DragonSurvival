package by.dragonsurvivalteam.dragonsurvival.registry;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class DSEnchantments {
    private static ResourceKey<Enchantment> register(String key) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(MODID, key));
    }

    public static ResourceKey<Enchantment> BOLAS = register("bolas");

    public static ResourceKey<Enchantment> DRAGONSBANE = register("dragonsbane");
    public static ResourceKey<Enchantment> DRAGONSBOON = register("dragonsboon");
    public static ResourceKey<Enchantment> DRAGONSBONK = register("dragonsbonk");
    public static ResourceKey<Enchantment> SHRINK = register("shrink");

    public static ResourceKey<Enchantment> BLOOD_SIPHON = register("blood_siphon");
    public static ResourceKey<Enchantment> MURDERERS_CUNNING = register("murderers_cunning");
    public static ResourceKey<Enchantment> OVERWHELMING_MIGHT = register("overwhelming_might");
    public static ResourceKey<Enchantment> DRACONIC_SUPERIORITY = register("draconic_superiority");

    public static ResourceKey<Enchantment> COMBAT_RECOVERY = register("combat_recovery");
    public static ResourceKey<Enchantment> UNBREAKABLE_SPIRIT = register("unbreakable_spirit");
    public static ResourceKey<Enchantment> AERODYNAMIC_MASTERY = register("aerodynamic_mastery");
    public static ResourceKey<Enchantment> SACRED_SCALES = register("sacred_scales");

    public static ResourceKey<Enchantment> CURSE_OF_OUTLAW = register("curse_of_outlaw");
    public static ResourceKey<Enchantment> CURSE_OF_KINDNESS = register("curse_of_kindness");
}
