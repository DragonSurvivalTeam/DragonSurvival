package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;

import java.util.Optional;

public record DragonEmote(String animationKey, boolean isBlend, boolean loops, boolean locksHead, boolean locksTail, boolean thirdPerson, Optional<Sound> sound) {
    public static final Codec<DragonEmote> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("animation_key").forGetter(DragonEmote::animationKey),
            Codec.BOOL.fieldOf("is_blend").forGetter(DragonEmote::isBlend),
            Codec.BOOL.fieldOf("loops").forGetter(DragonEmote::loops),
            Codec.BOOL.fieldOf("locks_head").forGetter(DragonEmote::locksHead),
            Codec.BOOL.fieldOf("locks_tail").forGetter(DragonEmote::locksTail),
            Codec.BOOL.fieldOf("third_person").forGetter(DragonEmote::thirdPerson),
            Sound.CODEC.optionalFieldOf("sound").forGetter(DragonEmote::sound)
    ).apply(instance, DragonEmote::new));


    public record Sound(SoundEvent soundEvent, float volume, float pitch, int interval) {
        public static final Codec<Sound> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("sound_event").forGetter(Sound::soundEvent),
                Codec.FLOAT.fieldOf("volume").forGetter(Sound::volume),
                Codec.FLOAT.fieldOf("pitch").forGetter(Sound::pitch),
                Codec.INT.fieldOf("interval").forGetter(Sound::interval)
        ).apply(instance, Sound::new));
    }
}
