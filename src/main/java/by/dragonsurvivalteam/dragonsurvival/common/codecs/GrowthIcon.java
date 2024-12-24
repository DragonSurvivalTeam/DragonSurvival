package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record GrowthIcon(ResourceLocation icon, ResourceLocation iconGrayscale, ResourceKey<DragonStage> dragonStage) {
    public static final Codec<GrowthIcon> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("icon").forGetter(GrowthIcon::icon),
            ResourceLocation.CODEC.fieldOf("icon_hover").forGetter(GrowthIcon::iconGrayscale),
            ResourceKey.codec(DragonStage.REGISTRY).fieldOf("dragon_stage").forGetter(GrowthIcon::dragonStage)
    ).apply(instance, GrowthIcon::new));
}
