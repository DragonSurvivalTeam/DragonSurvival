package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
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
        HoverIcon growthLeftArrow,
        HoverIcon growthRightArrow,
        FillIcon growthCrystal,
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
            HoverIcon.CODEC.fieldOf("growth_left_arrow").forGetter(MiscDragonTextures::growthLeftArrow),
            HoverIcon.CODEC.fieldOf("growth_right_arrow").forGetter(MiscDragonTextures::growthRightArrow),
            FillIcon.CODEC.fieldOf("growth_crystal").forGetter(MiscDragonTextures::growthCrystal),
            ColorRGBA.CODEC.fieldOf("primary_color").forGetter(MiscDragonTextures::primaryColor),
            ColorRGBA.CODEC.fieldOf("secondary_color").forGetter(MiscDragonTextures::secondaryColor)
    ).apply(instance, MiscDragonTextures::new));

    public record HoverIcon(ResourceLocation hoverIcon, ResourceLocation icon) {
        public static final Codec<HoverIcon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("hover_icon").forGetter(HoverIcon::hoverIcon),
                ResourceLocation.CODEC.fieldOf("icon").forGetter(HoverIcon::icon)
        ).apply(instance, HoverIcon::new));
    }

    public record FillIcon(ResourceLocation empty, ResourceLocation full) {
        public static final Codec<FillIcon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("empty").forGetter(FillIcon::empty),
                ResourceLocation.CODEC.fieldOf("full").forGetter(FillIcon::full)
        ).apply(instance, FillIcon::new));
    }

    public static final ResourceLocation DEFAULT_GROWTH_HOVER_ICON = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/stage/cave/newborn_stage_hover.png");
    public static final ResourceLocation DEFAULT_GROWTH_BASE_ICON = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/stage/cave/newborn_stage_main.png");
    public static final GrowthIcon DEFAULT_GROWTH_ICON = new GrowthIcon(DEFAULT_GROWTH_HOVER_ICON, DEFAULT_GROWTH_BASE_ICON, DragonStages.newborn);
}
