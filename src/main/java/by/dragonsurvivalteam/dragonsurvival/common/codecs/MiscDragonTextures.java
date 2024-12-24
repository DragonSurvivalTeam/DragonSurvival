package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ColorRGBA;

import java.util.List;

public record MiscDragonTextures(
        ResourceLocation foodSprites,
        ResourceLocation manaSprites,
        ResourceLocation altarBanner,
        ResourceLocation sourceOfMagicBackgroundPassive,
        ResourceLocation sourceOfMagicBackgroundActive,
        ResourceLocation castBar,
        ResourceLocation helpButton,
        ResourceLocation growthBarFill,
        List<GrowthIcon> growthIcons,
        ColorRGBA primaryColor,
        ColorRGBA secondaryColor
) {
    // TODO :: should all of these be defined?
    //  could be colored: mana_sprites / help_button / growth_bar_fill
    public static final Codec<MiscDragonTextures> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("food_sprites").forGetter(MiscDragonTextures::foodSprites),
            ResourceLocation.CODEC.fieldOf("mana_sprites").forGetter(MiscDragonTextures::manaSprites),
            ResourceLocation.CODEC.fieldOf("altar_banner").forGetter(MiscDragonTextures::altarBanner),
            ResourceLocation.CODEC.fieldOf("source_of_magic_background_passive").forGetter(MiscDragonTextures::sourceOfMagicBackgroundPassive),
            ResourceLocation.CODEC.fieldOf("source_of_magic_background_active").forGetter(MiscDragonTextures::sourceOfMagicBackgroundPassive),
            ResourceLocation.CODEC.fieldOf("ability_bar").forGetter(MiscDragonTextures::castBar),
            ResourceLocation.CODEC.fieldOf("help_button").forGetter(MiscDragonTextures::helpButton),
            ResourceLocation.CODEC.fieldOf("growth_bar_fill").forGetter(MiscDragonTextures::growthBarFill),
            GrowthIcon.CODEC.listOf().fieldOf("growth_icons").forGetter(MiscDragonTextures::growthIcons),
            ColorRGBA.CODEC.fieldOf("primary_color").forGetter(MiscDragonTextures::primaryColor),
            ColorRGBA.CODEC.fieldOf("secondary_color").forGetter(MiscDragonTextures::secondaryColor)
    ).apply(instance, MiscDragonTextures::new));

    public static final ResourceLocation DEFAULT_GROWTH_ICON = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/stage/cave/newborn_hover.png");
}
