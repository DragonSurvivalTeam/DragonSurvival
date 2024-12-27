package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class DragonEmoteSets {
    public static final ResourceKey<DragonEmoteSet> DEFAULT_EMOTES = key("default_emotes");

    @Translation(type = Translation.Type.EMOTE, comments = "Angry")
    public static final String BLEND_ANGRY = "blend_angry";

    @Translation(type = Translation.Type.EMOTE, comments = "Sitting Curled")
    public static final String SITTING_CURLED = "sitting_curled_tail_left";

    @Translation(type = Translation.Type.EMOTE, comments = "Sitting Curled (Head Locked)")
    public static final String SITTING_CURLED_HEAD_LOCKED = "sitting_curled_tail_left_head_locked";

    @Translation(type = Translation.Type.EMOTE, comments = "Sitting Curled No loop")
    public static final String SITTING_CURLED_NO_LOOP = "sitting_curled_tail_left_no_loop";

    public static void registerEmoteSets(final BootstrapContext<DragonEmoteSet> context) {
        context.register(DEFAULT_EMOTES,
                new DragonEmoteSet(
                        List.of(
                                DragonEmote.Builder.of(BLEND_ANGRY).loops().blend().build(),
                                DragonEmote.Builder.of(SITTING_CURLED).loops().locksHead().build(),
                                DragonEmote.Builder.of(SITTING_CURLED, SITTING_CURLED_HEAD_LOCKED).loops().locksHead().locksTail().build(),
                                DragonEmote.Builder.of(SITTING_CURLED, SITTING_CURLED_NO_LOOP).build()
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
