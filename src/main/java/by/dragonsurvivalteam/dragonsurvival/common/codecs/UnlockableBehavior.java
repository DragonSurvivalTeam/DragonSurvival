package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
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

public record UnlockableBehavior(Optional<LootItemCondition> unlockCondition, Optional<Visibility> visibility) {
    public static final Codec<UnlockableBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("unlock_condition").forGetter(UnlockableBehavior::unlockCondition),
            Visibility.CODEC.optionalFieldOf("visibility").forGetter(UnlockableBehavior::visibility)
    ).apply(instance, UnlockableBehavior::new));

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

    public record SpeciesEntry(Holder<DragonSpecies> species, boolean isUnlocked) {
        public static final Codec<SpeciesEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DragonSpecies.CODEC.fieldOf("species").forGetter(SpeciesEntry::species),
                Codec.BOOL.fieldOf("is_unlocked").forGetter(SpeciesEntry::isUnlocked)
        ).apply(instance, SpeciesEntry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SpeciesEntry> STREAM_CODEC = StreamCodec.composite(
                DragonSpecies.STREAM_CODEC, SpeciesEntry::species,
                ByteBufCodecs.BOOL, SpeciesEntry::isUnlocked,
                SpeciesEntry::new
        );
    }

    public record BodyEntry(Holder<DragonBody> body, boolean isUnlocked) {
        public static final Codec<BodyEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DragonBody.CODEC.fieldOf("body").forGetter(BodyEntry::body),
                Codec.BOOL.fieldOf("is_unlocked").forGetter(BodyEntry::isUnlocked)
        ).apply(instance, BodyEntry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, BodyEntry> STREAM_CODEC = StreamCodec.composite(
                DragonBody.STREAM_CODEC, BodyEntry::body,
                ByteBufCodecs.BOOL, BodyEntry::isUnlocked,
                BodyEntry::new
        );
    }
}
