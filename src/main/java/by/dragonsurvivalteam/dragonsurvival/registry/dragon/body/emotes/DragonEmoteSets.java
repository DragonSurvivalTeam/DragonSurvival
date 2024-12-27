package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class DragonEmoteSets {
    public static final ResourceKey<DragonEmoteSet> DEFAULT_EMOTES = key("default_emotes");


    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend - Fix Head and Tail")
    public static final String BLEND_HEAD_LOCKED = "head_turn";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend - Angry")
    public static final String BLEND_ANGRY = "blend_angry";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend - Charge")
    public static final String BLEND_SPELL_CHARGE = "spell_charge";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend - Sad")
    public static final String BLEND_SAD = "blend_sad";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend - Blep ")
    public static final String BLEND_BLEP = "blend_blep";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend - Tongue")
    public static final String BLEND_MAW = "blend_maw";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend - Lick")
    public static final String BLEND_LICKING = "blend_licking";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend - Yaw")
    public static final String BLEND_YAWNING = "blend_yawning";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend - Sniff")
    public static final String BLEND_SNIFFING = "blend_sniffing";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend - One Bite")
    public static final String BLEND_SPELL_SHOT = "spell_shot";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend - Bites")
    public static final String BLEND_BITE = "bite";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend - Open Mouth")
    public static final String BLEND_BREATH= "breath";

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
                                DragonEmote.Builder.of(BLEND_HEAD_LOCKED).blend().locksHead().locksTail().build(),
                                DragonEmote.Builder.of(BLEND_ANGRY).blend().build(),
                                DragonEmote.Builder.of(BLEND_SPELL_CHARGE).blend().build(),
                                DragonEmote.Builder.of(BLEND_SAD).blend().build(),
                                DragonEmote.Builder.of(BLEND_BLEP).blend().build(),
                                DragonEmote.Builder.of(BLEND_MAW).blend().build(),
                                DragonEmote.Builder.of(BLEND_LICKING).loops().blend().build(),
                                DragonEmote.Builder.of(BLEND_YAWNING).loops().blend().build(),
                                DragonEmote.Builder.of(BLEND_SNIFFING).loops().blend().build(),
                                DragonEmote.Builder.of(BLEND_SPELL_SHOT).blend().build(),
                                DragonEmote.Builder.of(BLEND_BITE).loops().blend().build(),
                                DragonEmote.Builder.of(BLEND_BREATH).blend().build(),
                                DragonEmote.Builder.of(SITTING_CURLED).build(),
                                DragonEmote.Builder.of(SITTING_CURLED, SITTING_CURLED_HEAD_LOCKED).locksHead().locksTail().build(),
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
