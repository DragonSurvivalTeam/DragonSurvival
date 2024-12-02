package by.dragonsurvivalteam.dragonsurvival.registry.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.GrowthIcon;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscDragonTextures;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.datapacks.AncientDatapack;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ColorRGBA;

import java.util.List;

public class DragonTypes {


    // TODO: Translation key here, also handle translation keys now for this data type
    public static final ResourceKey<DragonType> CAVE = key("cave");

    public static void registerTypes(final BootstrapContext<DragonType> context) {
        context.register(CAVE, new DragonType(
                HolderSet.empty(),
                HolderSet.empty(),
                HolderSet.direct(context.lookup(DragonAbility.REGISTRY).getOrThrow(DragonAbilities.NETHER_BREATH)),
                List.of(),
                new MiscDragonTextures(
                        DragonSurvival.res("food_icons/cave_food_sprites.png"),
                        DragonSurvival.res("mana_icons/cave_mana_sprites.png"),
                        DragonSurvival.res("dragon_altar/cave_altar_banner.png"),
                        DragonSurvival.res("source_of_magic/cave_source_of_magic_0.png"),
                        DragonSurvival.res("source_of_magic/cave_source_of_magic_1.png"),
                        DragonSurvival.res("casting_bars/cave_cast_bar.png"),
                        DragonSurvival.res("help_button/cave_help_button.png"),
                        DragonSurvival.res("growth/circle_cave.png"),
                        List.of(new GrowthIcon(
                                        DragonSurvival.res("growth/cave/newborn.png"),
                                        DragonStages.newborn
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("growth/cave/young.png"),
                                        DragonStages.young
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("growth/cave/adult.png"),
                                        DragonStages.adult
                                ),
                                new GrowthIcon(
                                        DragonSurvival.res("growth/cave/ancient.png"),
                                        AncientDatapack.ancient
                                )
                        ),
                        new ColorRGBA(16711680),
                        new ColorRGBA(16711680)
                )
        ));
    }

    public static ResourceKey<DragonType> key(final ResourceLocation location) {
        return ResourceKey.create(DragonType.REGISTRY, location);
    }

    private static ResourceKey<DragonType> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
