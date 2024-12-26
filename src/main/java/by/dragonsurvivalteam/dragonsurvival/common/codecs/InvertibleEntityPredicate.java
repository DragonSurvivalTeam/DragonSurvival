package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record InvertibleEntityPredicate(EntityPredicate predicate, boolean isInverted) {
    public static final Codec<InvertibleEntityPredicate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityPredicate.CODEC.fieldOf("predicate").forGetter(InvertibleEntityPredicate::predicate),
            Codec.BOOL.optionalFieldOf("is_inverted", false).forGetter(InvertibleEntityPredicate::isInverted)
    ).apply(instance, InvertibleEntityPredicate::new));

    public static InvertibleEntityPredicate normal(final EntityPredicate predicate) {
        return new InvertibleEntityPredicate(predicate, false);
    }

    public static InvertibleEntityPredicate inverted(final EntityPredicate predicate) {
        return new InvertibleEntityPredicate(predicate, true);
    }

    public boolean matches(final ServerLevel level, final Vec3 position, final Entity entity) {
        if (isInverted) {
            return !predicate.matches(level, position, entity);
        } else {
            return predicate.matches(level, position, entity);
        }
    }
}
