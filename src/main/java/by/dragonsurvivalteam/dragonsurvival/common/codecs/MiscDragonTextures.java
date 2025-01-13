package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public record MiscDragonTextures(
        ResourceLocation foodSprites,
        ManaSprites manaSprites,
        ResourceLocation altarBanner,
        ResourceLocation castBar,
        ResourceLocation helpButton,
        List<GrowthIcon> growthIcons,
        HoverIcon growthLeftArrow,
        HoverIcon growthRightArrow,
        FillIcon growthCrystal,
        FoodTooltip foodTooltip,
        TextColor primaryColor,
        TextColor secondaryColor
) {
    public static final Codec<MiscDragonTextures> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("food_sprites").forGetter(MiscDragonTextures::foodSprites), // TODO :: use vanilla food bar by default or have it optional and render vanilla bar if missing
            ManaSprites.CODEC.fieldOf("mana_sprites").forGetter(MiscDragonTextures::manaSprites), // TODO :: can have default texture (gray and color with primary color)
            ResourceLocation.CODEC.fieldOf("altar_banner").forGetter(MiscDragonTextures::altarBanner),
            ResourceLocation.CODEC.fieldOf("ability_bar").forGetter(MiscDragonTextures::castBar), // TODO :: could have a simple cast bar without a dragon as default
            ResourceLocation.CODEC.fieldOf("help_button").forGetter(MiscDragonTextures::helpButton), // TODO :: can be optional and use gray / green as default if missing
            GrowthIcon.CODEC.listOf().optionalFieldOf("growth_icons", List.of()).forGetter(MiscDragonTextures::growthIcons),
            HoverIcon.CODEC.fieldOf("growth_left_arrow").forGetter(MiscDragonTextures::growthLeftArrow),
            HoverIcon.CODEC.fieldOf("growth_right_arrow").forGetter(MiscDragonTextures::growthRightArrow),
            FillIcon.CODEC.fieldOf("growth_crystal").forGetter(MiscDragonTextures::growthCrystal),
            FoodTooltip.CODEC.fieldOf("food_tooltip").forGetter(MiscDragonTextures::foodTooltip),
            TextColor.CODEC.fieldOf("primary_color").forGetter(MiscDragonTextures::primaryColor),
            TextColor.CODEC.fieldOf("secondary_color").forGetter(MiscDragonTextures::secondaryColor)
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

    public record ManaSprites(ResourceLocation full, ResourceLocation reserved, ResourceLocation recovery, ResourceLocation empty) {
        public static final Codec<ManaSprites> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("full").forGetter(ManaSprites::full),
                ResourceLocation.CODEC.fieldOf("reserved").forGetter(ManaSprites::reserved),
                ResourceLocation.CODEC.fieldOf("recovery").forGetter(ManaSprites::recovery),
                ResourceLocation.CODEC.fieldOf("empty").forGetter(ManaSprites::empty)
        ).apply(instance, ManaSprites::new));
    }

    public record FoodTooltip(ResourceLocation font, String nutritionIcon, String saturationIcon, Optional<TextColor> color) {
        public static final ResourceLocation DEFAULT_FOOD_TOOLTIP_FONT = DragonSurvival.res("food_tooltip_icon_font");

        public static final Codec<FoodTooltip> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("font", DEFAULT_FOOD_TOOLTIP_FONT).forGetter(FoodTooltip::font),
                // TODO :: use human icons by default? would only be guaranteed to work if we use a non-custom font file
                //  these could be optional and we use some default font file if they are missing
                Codec.STRING.fieldOf("nutrition_icon").forGetter(FoodTooltip::nutritionIcon),
                Codec.STRING.fieldOf("saturation_icon").forGetter(FoodTooltip::saturationIcon),
                TextColor.CODEC.optionalFieldOf("color").forGetter(FoodTooltip::color)
        ).apply(instance, FoodTooltip::new));
    }

    public static final ResourceLocation DEFAULT_GROWTH_HOVER_ICON = DragonSurvival.res("textures/gui/stage/cave/newborn_stage_hover.png");
    public static final ResourceLocation DEFAULT_GROWTH_BASE_ICON = DragonSurvival.res("textures/gui/stage/cave/newborn_stage_main.png");
    public static final GrowthIcon DEFAULT_GROWTH_ICON = new GrowthIcon(DEFAULT_GROWTH_HOVER_ICON, DEFAULT_GROWTH_BASE_ICON, DragonStages.newborn);
}
