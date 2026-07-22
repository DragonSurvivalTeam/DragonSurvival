package by.dragonsurvivalteam.dragonsurvival.compat.overgeared;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class Overgeared {
    // Needs custom handling for their tools, see https://github.com/phuccom000/Overgeared/blob/master/src/main/java/net/stirdrem/overgeared/util/ModTags.java
    // Mainly due to their tools not referencing any vanilla tiers
    public static final TagKey<Block> NEEDS_STEEL_TOOL = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("overgeared", "needs_steel_tool"));
    public static final TagKey<Block> NEEDS_COPPER_TOOL = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("overgeared", "needs_copper_tool"));

    // This tag contains 'needs_diamond_tool', meaning it's between iron and diamond
    public static final TagKey<Block> INCORRECT_FOR_STEEL_TOOL = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("overgeared", "incorrect_for_steel_tool"));

    // This tag contains 'needs_diamond_tool', 'needs_steel_tool' and 'needs_iron_tool', meaning it's between stone and iron
    public static final TagKey<Block> INCORRECT_FOR_COPPER_TOOL = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("overgeared", "incorrect_for_copper_tool"));
}
