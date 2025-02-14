package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

public record AltarBehaviour(Optional<LootItemCondition> unlockCondition, Optional<Visibility> visibility) {
    public static final Codec<AltarBehaviour> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("unlock_condition").forGetter(AltarBehaviour::unlockCondition),
            Visibility.CODEC.optionalFieldOf("visibility").forGetter(AltarBehaviour::visibility)
    ).apply(instance, AltarBehaviour::new));

    public enum Visibility implements StringRepresentable {
        ALWAYS_VISIBLE,
        ALWAYS_HIDDEN,
        VISIBLE_IF_LOCKED;

        public static final Codec<Visibility> CODEC = StringRepresentable.fromEnum(Visibility::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ENGLISH);
        }
    }

    public record Entry(Holder<DragonSpecies> species, boolean isUnlocked) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DragonSpecies.CODEC.fieldOf("species").forGetter(Entry::species),
                Codec.BOOL.fieldOf("is_unlocked").forGetter(Entry::isUnlocked)
        ).apply(instance, Entry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                DragonSpecies.STREAM_CODEC, Entry::species,
                ByteBufCodecs.BOOL, Entry::isUnlocked,
                Entry::new
        );
    }
}
