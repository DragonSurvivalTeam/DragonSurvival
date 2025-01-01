package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class DragonEmoteSets {
    public static final ResourceKey<DragonEmoteSet> DEFAULT_EMOTES = key("default_emotes");


    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Fix Head")
    public static final String BLEND_HEAD_LOCKED = "head_turn";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Angry")
    public static final String BLEND_ANGRY = "blend_angry";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Charge")
    public static final String BLEND_SPELL_CHARGE = "spell_charge";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Sad")
    public static final String BLEND_SAD = "blend_sad";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Blep ")
    public static final String BLEND_BLEP = "blend_blep";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Tongue")
    public static final String BLEND_MAW = "blend_maw";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Lick")
    public static final String BLEND_LICKING = "blend_licking";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Yaw")
    public static final String BLEND_YAWNING = "blend_yawning";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Sniff")
    public static final String BLEND_SNIFFING = "blend_sniffing";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Bites")
    public static final String BLEND_BITE = "spell_shot";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r One Bite")
    public static final String BLEND_ONE_BITE = "bite";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Open Mouth")
    public static final String BLEND_BREATH = "breath";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Wing Hug")
    public static final String BLEND_WING =  "blend_wing_hug";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Just Sit")
    public static final String SIT = "sit";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Curled")
    public static final String SITTING_CURLED = "sitting_curled_tail_left";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Magic")
    public static final String SITTING_MAGIC = "sit_on_magic_source";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Item")
    public static final String SITTING_ITEM = "sitting_spinning_item";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Sad")
    public static final String SITTING_SAD = "sad_sitting";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Hug")
    public static final String SITTING_HUG = "hugging_sitting";



    public static void registerEmoteSets(final BootstrapContext<DragonEmoteSet> context) {
        context.register(DEFAULT_EMOTES,
                new DragonEmoteSet(
                        List.of(
                                DragonEmote.Builder.of(BLEND_HEAD_LOCKED).loops().blend().locksHead().locksTail().canMove().build(),
                                DragonEmote.Builder.of(BLEND_ANGRY).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_SPELL_CHARGE).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_SAD).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_BLEP).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_MAW).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_LICKING).blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_YAWNING).blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_SNIFFING).blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_BITE).loops().canMove().blend().build(),
                                DragonEmote.Builder.of(BLEND_ONE_BITE).blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_BREATH).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_WING).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(SIT).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_CURLED).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_MAGIC).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_ITEM).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_SAD).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_HUG).loops().canMove().build()
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
