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

    @Translation(type = Translation.Type.EMOTE, comments = "Sitting Curled")
    public static final String SITTING_CURLED = "sitting_curled_tail_left";

    public static void registerEmoteSets(final BootstrapContext<DragonEmoteSet> context) {
        context.register(DEFAULT_EMOTES,
                new DragonEmoteSet(
                        List.of(
                                new DragonEmote(BLEND_ANGRY, 1.0, -1, true, true, true, false, false, true, Optional.empty()),
                                new DragonEmote(SITTING_CURLED, 1.0, -1, true, true, true, true, false, false, Optional.empty())
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
