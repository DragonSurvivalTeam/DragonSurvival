package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

public class DragonEmoteSets {
    public static final ResourceKey<DragonEmoteSet> DEFAULT_EMOTES = key("default_emotes");


    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Fix Head")
    public static final String BLEND_HEAD_LOCKED = "empty";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Angry")
    public static final String BLEND_ANGRY = "blend_angry";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Charge")
    public static final String BLEND_SPELL_CHARGE = "spell_charge";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Sad")
    public static final String BLEND_SAD = "blend_sad";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Close Eyes")
    public static final String BLEND_CLOSE_EYES = "blend_close_eyes";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Blep")
    public static final String BLEND_BLEP = "blend_blep";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Tongue")
    public static final String BLEND_MAW = "blend_maw";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Lick")
    public static final String BLEND_LICKING = "blend_licking";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Yaw")
    public static final String BLEND_YAWNING = "blend_yawning";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Waving Paw")
    public static final String BLEND_DIG = "blend_dig";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Sniff")
    public static final String BLEND_SNIFFING = "blend_sniffing";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Bites")
    public static final String BLEND_BITE = "spell_shot";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r One Bite")
    public static final String BLEND_ONE_BITE = "bite";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Talk")
    public static final String BLEND_TALK = "eat_item_left";

    @Translation(type = Translation.Type.EMOTE, comments = "§6Blend:§r Open Mouth")
    public static final String BLEND_BREATH = "breath";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Just Sit")
    public static final String SIT = "sit";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r L Curled")
    public static final String SITTING_CURLED_L = "sitting_curled_tail_left";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r R Curled")
    public static final String SITTING_CURLED_R = "sitting_curled_tail_right";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Magic")
    public static final String SITTING_MAGIC = "sit_on_magic_source";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Item")
    public static final String SITTING_ITEM = "sitting_spinning_item";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Sad")
    public static final String SITTING_SAD = "sad_sitting";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Hug")
    public static final String SITTING_HUG = "hugging_sitting";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Hug Alt")
    public static final String SITTING_HUG_WING = "hugging_sitting_winged";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Holding Paws")
    public static final String SITTING_PAWS = "holding_paws_sitting";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Shy")
    public static final String SITTING_SHY = "shy_sitting";

    @Translation(type = Translation.Type.EMOTE, comments = "§5Sit:§r Leaned Back")
    public static final String SITTING_LEANED_BACK = "resting_leaning_on_wall";

    @Translation(type = Translation.Type.EMOTE, comments = "§4Rest:§r L Lying")
    public static final String REST_LYING_L = "resting_left";

    @Translation(type = Translation.Type.EMOTE, comments = "§4Rest:§r R Lying")
    public static final String REST_LYING_R = "resting_right";

    @Translation(type = Translation.Type.EMOTE, comments = "§4Rest:§r L Lying Alt")
    public static final String REST_LYING_WINGED_L = "resting_covering_with_wing_left";

    @Translation(type = Translation.Type.EMOTE, comments = "§4Rest:§r R Lying Alt")
    public static final String REST_LYING_WINGED_R = "resting_covering_with_wing_right";

    @Translation(type = Translation.Type.EMOTE, comments = "§4Rest:§r On Back")
    public static final String REST_ON_BACK = "resting_on_back";

    @Translation(type = Translation.Type.EMOTE, comments = "§4Rest:§r Straight")
    public static final String REST_STRAIGHT = "resting_straight";

    @Translation(type = Translation.Type.EMOTE, comments = "§4Rest:§r On Side")
    public static final String REST_ON_SIDE = "sleeping_on_side_left";

    @Translation(type = Translation.Type.EMOTE, comments = "§4Rest:§r Straight On Side")
    public static final String REST_STRAIGHT_ON_SIDE = "sleeping_on_side_straight_left";

    @Translation(type = Translation.Type.EMOTE, comments = "§4Rest:§r L Sleep")
    public static final String REST_SLEEP_L = "sleep_left";

    @Translation(type = Translation.Type.EMOTE, comments = "§4Rest:§r R Sleep")
    public static final String REST_SLEEP_R = "sleep_right";

    @Translation(type = Translation.Type.EMOTE, comments = "§4Rest:§r Shy")
    public static final String REST_SHY = "shy_lying";

    @Translation(type = Translation.Type.EMOTE, comments = "§3Fun:§r Rocking On Back")
    public static final String FUN_ROCKING = "rocking_on_back";

    @Translation(type = Translation.Type.EMOTE, comments = "§3Fun:§r Spin")
    public static final String FUN_SPIN = "spinning_on_back";

    @Translation(type = Translation.Type.EMOTE, comments = "§3Fun:§r SPIN FAST")
    public static final String FUN_SPIN_FAST = "spinning_on_back_fast";

    @Translation(type = Translation.Type.EMOTE, comments = "§3Fun:§r Sit Vibing")
    public static final String FUN_VIBING = "vibing_sitting";

    @Translation(type = Translation.Type.EMOTE, comments = "§3Fun:§r Dance")
    public static final String FUN_DANCE = "vibing_standing";

    @Translation(type = Translation.Type.EMOTE, comments = "§3Fun:§r Shaking")
    public static final String FUN_SHAKING = "shaking_standing";

    @Translation(type = Translation.Type.EMOTE, comments = "§3Fun:§r Flapping Wings")
    public static final String FUN_WINGS = "flapping_wings_standing";

    @Translation(type = Translation.Type.EMOTE, comments = "§3Fun:§r Flapping Wings Alt")
    public static final String FUN_WINGS_BIPED = "flapping_wings_standing_biped";

    @Translation(type = Translation.Type.EMOTE, comments = "§3Fun:§r Hold Passenger (rider)")
    public static final String FUN_PASSENGER = "holding_passenger_standing_biped";

    @Translation(type = Translation.Type.EMOTE, comments = "§2Misc:§r Fly")
    public static final String MISC_FLY = "fly";

    @Translation(type = Translation.Type.EMOTE, comments = "§2Misc:§r Soaring")
    public static final String MISC_SOARING = "fly_soaring";

    @Translation(type = Translation.Type.EMOTE, comments = "§2Misc:§r Fly Land")
    public static final String MISC_LAND = "fly_land";

    @Translation(type = Translation.Type.EMOTE, comments = "§2Misc:§r Swim")
    public static final String MISC_SWIM = "swim_fast";

    @Translation(type = Translation.Type.EMOTE, comments = "§1Sound:§r Purr")
    public static final String SOUND_PURR = "purr";

    @Translation(type = Translation.Type.EMOTE, comments = "§1Sound:§r Purreow")
    public static final String SOUND_PURREOW = "purreow";

    @Translation(type = Translation.Type.EMOTE, comments = "§1Sound:§r Hungry")
    public static final String SOUND_STRAY_AMBIENT = "stray";

    @Translation(type = Translation.Type.EMOTE, comments = "§1Sound:§r Loud Angry Roar")
    public static final String SOUND_ROAR = "roar";

    @Translation(type = Translation.Type.EMOTE, comments = "§1Sound:§r Hiss")
    public static final String SOUND_HISS = "hiss";

    @Translation(type = Translation.Type.EMOTE, comments = "§1Sound:§r Growl")
    public static final String SOUND_GROWL = "growl";

    @Translation(type = Translation.Type.EMOTE, comments = "§1Sound:§r Pft")
    public static final String SOUND_CAMEL = "camel";

    @Translation(type = Translation.Type.EMOTE, comments = "§1Sound:§r Hurr")
    public static final String SOUND_BEAR = "bear";

    @Translation(type = Translation.Type.EMOTE, comments = "§1Sound:§r Happy")
    public static final String SOUND_HAPPY = "happy";


    public static void registerEmoteSets(final BootstrapContext<DragonEmoteSet> context) {
        context.register(DEFAULT_EMOTES,
                new DragonEmoteSet(
                        List.of(
                                DragonEmote.Builder.of(BLEND_HEAD_LOCKED).loops().blend().locksHead().locksTail().canMove().build(),
                                DragonEmote.Builder.of(BLEND_ANGRY).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_SPELL_CHARGE).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_SAD).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_CLOSE_EYES).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_BLEP).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_MAW).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_LICKING).blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_YAWNING).blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_BREATH).loops().loops().blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_SNIFFING).blend().sound(SoundEvents.FOX_SNIFF, 60, 1.0f, 0.1f).canMove().build(),
                                DragonEmote.Builder.of(BLEND_DIG).blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_BITE).loops().canMove().blend().build(),
                                DragonEmote.Builder.of(BLEND_ONE_BITE).blend().canMove().build(),
                                DragonEmote.Builder.of(BLEND_TALK).loops().blend().canMove().build(),
                                DragonEmote.Builder.of(SIT).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_CURLED_L).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_CURLED_R).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_MAGIC).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_ITEM).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_SAD).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_HUG).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_HUG_WING).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_PAWS).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_SHY).loops().canMove().build(),
                                DragonEmote.Builder.of(SITTING_LEANED_BACK).loops().canMove().build(),
                                DragonEmote.Builder.of(REST_LYING_L).loops().canMove().build(),
                                DragonEmote.Builder.of(REST_LYING_R).loops().canMove().build(),
                                DragonEmote.Builder.of(REST_LYING_WINGED_L).loops().canMove().build(),
                                DragonEmote.Builder.of(REST_LYING_WINGED_R).loops().canMove().build(),
                                DragonEmote.Builder.of(REST_SLEEP_L).loops().sound(SoundEvents.FOX_SLEEP, 100, 1.0f, 0.1f).canMove().build(),
                                DragonEmote.Builder.of(REST_SLEEP_R).loops().sound(SoundEvents.FOX_SLEEP, 100, 1.0f, 0.1f).canMove().build(),
                                DragonEmote.Builder.of(REST_ON_SIDE).loops().canMove().build(),
                                DragonEmote.Builder.of(REST_STRAIGHT).loops().canMove().build(),
                                DragonEmote.Builder.of(REST_ON_BACK).loops().canMove().build(),
                                DragonEmote.Builder.of(REST_STRAIGHT_ON_SIDE).loops().canMove().build(),
                                DragonEmote.Builder.of(REST_SHY).loops().canMove().build(),
                                DragonEmote.Builder.of(FUN_ROCKING).loops().canMove().build(),
                                DragonEmote.Builder.of(FUN_SPIN, FUN_SPIN).loops().canMove().build(),
                                DragonEmote.Builder.of(FUN_SPIN, FUN_SPIN_FAST).loops().canMove().speed(2.0).build(),
                                DragonEmote.Builder.of(FUN_VIBING).loops().canMove().build(),
                                DragonEmote.Builder.of(FUN_DANCE).loops().canMove().build(),
                                DragonEmote.Builder.of(FUN_WINGS_BIPED).loops().canMove().build(),
                                DragonEmote.Builder.of(FUN_WINGS).loops().canMove().build(),
                                DragonEmote.Builder.of(FUN_PASSENGER).loops().canMove().build(),
                                DragonEmote.Builder.of(FUN_SHAKING).canMove().sound(SoundEvents.WOLF_SHAKE, 60, 1.0f, 0.1f).build(),
                                DragonEmote.Builder.of(MISC_SWIM).loops().canMove().build(),
                                DragonEmote.Builder.of(MISC_LAND).loops().canMove().build(),
                                DragonEmote.Builder.of(MISC_SOARING).loops().canMove().build(),
                                DragonEmote.Builder.of(MISC_FLY).loops().canMove().build(),
                                DragonEmote.Builder.of(BLEND_HEAD_LOCKED, SOUND_PURR).sound(SoundEvents.CAT_PURR, 160, 1.5f, 0.8f).blend().build(),
                                DragonEmote.Builder.of(BLEND_HEAD_LOCKED, SOUND_PURREOW).sound(SoundEvents.CAT_PURREOW, 160, 2.0f, 0.3f).blend().build(),
                                DragonEmote.Builder.of(BLEND_TALK, SOUND_STRAY_AMBIENT).speed(0.1).sound(SoundEvents.CAT_STRAY_AMBIENT, 160, 2.0f, 0.2f).blend().build(),
                                DragonEmote.Builder.of(BLEND_BREATH, SOUND_ROAR).speed(0.1).sound(SoundEvents.ENDER_DRAGON_GROWL, 160, 0.3f, 0.1f).blend().build(),
                                DragonEmote.Builder.of(BLEND_BREATH, SOUND_HISS).speed(0.2).sound(SoundEvents.CAT_HISS, 160, 1.5f, 0.2f).blend().build(),
                                DragonEmote.Builder.of(BLEND_SPELL_CHARGE, SOUND_GROWL).speed(0.001).sound(SoundEvents.WOLF_GROWL, 160, 1.0f, 0.2f).blend().build(),
                                DragonEmote.Builder.of(BLEND_TALK, SOUND_CAMEL).speed(0.01).sound(SoundEvents.CAMEL_AMBIENT, 160, 1.0f, 0.2f).blend().build(),
                                DragonEmote.Builder.of(BLEND_TALK, SOUND_BEAR).speed(0.02).sound(SoundEvents.POLAR_BEAR_AMBIENT, 160, 1.0f, 0.2f).blend().build(),
                                DragonEmote.Builder.of(BLEND_BREATH, SOUND_HAPPY).speed(0.01).sound(SoundEvents.SNIFFER_HAPPY, 160, 0.5f, 0.8f).blend().build()
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
