package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;

import java.util.Optional;

public record DragonEmote(String animationKey, double speed, int duration, boolean loops, boolean blend, boolean locksHead, boolean locksTail, boolean thirdPerson, boolean canMove, Optional<Sound> sound) {
    public static final Codec<DragonEmote> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("animation_key").forGetter(DragonEmote::animationKey),
            Codec.DOUBLE.fieldOf("speed").forGetter(DragonEmote::speed),
            Codec.INT.optionalFieldOf("duration", -1).forGetter(DragonEmote::duration),
            Codec.BOOL.optionalFieldOf("loops", false).forGetter(DragonEmote::loops),
            Codec.BOOL.fieldOf("blend").forGetter(DragonEmote::blend),
            Codec.BOOL.fieldOf("locks_head").forGetter(DragonEmote::locksHead),
            Codec.BOOL.fieldOf("locks_tail").forGetter(DragonEmote::locksTail),
            Codec.BOOL.fieldOf("third_person").forGetter(DragonEmote::thirdPerson),
            Codec.BOOL.fieldOf("can_move").forGetter(DragonEmote::canMove),
            Sound.CODEC.optionalFieldOf("sound").forGetter(DragonEmote::sound)
    ).apply(instance, DragonEmote::new));

    public static final StreamCodec<ByteBuf, DragonEmote> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public record Sound(SoundEvent soundEvent, float volume, float pitch, int interval) {
        public static final Codec<Sound> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("sound_event").forGetter(Sound::soundEvent),
                Codec.FLOAT.fieldOf("volume").forGetter(Sound::volume),
                Codec.FLOAT.fieldOf("pitch").forGetter(Sound::pitch),
                Codec.INT.fieldOf("interval").forGetter(Sound::interval)
        ).apply(instance, Sound::new));
    }

    public Component name() {
        return Component.translatable(Translation.Type.EMOTE.wrap(animationKey));
    }
}
