package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

public class DragonEmoteSets {
    public static final ResourceKey<DragonEmoteSet> DEFAULT_EMOTES = key("default_emotes");

    @Translation(type = Translation.Type.EMOTE, comments = "Angry")
    public static final String BLEND_ANGRY = "blend_angry";

    public static void registerEmoteSets(final BootstrapContext<DragonEmoteSet> context) {
        context.register(DEFAULT_EMOTES,
                new DragonEmoteSet(
                        List.of(
                                new DragonEmote(
                                        BLEND_ANGRY,
                                        true,
                                        true,
                                        true,
                                        true,
                                        false,
                                        Optional.empty()
                                )
                        )
                )
        );
    }

    public static ResourceKey<DragonEmoteSet> key(final ResourceLocation location) {
        return ResourceKey.create(DragonEmoteSet.REGISTRY, location);
    }

    private static ResourceKey<DragonEmoteSet> key(final String path) {
        return key(DragonSurvival.res(path));
    }
}
