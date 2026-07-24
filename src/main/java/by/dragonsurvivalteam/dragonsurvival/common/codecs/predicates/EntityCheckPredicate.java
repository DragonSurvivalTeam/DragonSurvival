package by.dragonsurvivalteam.dragonsurvival.common.codecs.predicates;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntitySubPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record EntityCheckPredicate(Optional<Type> checkFor) implements EntitySubPredicate {
    public static final MapCodec<EntityCheckPredicate> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Type.CODEC.optionalFieldOf("check_for").forGetter(EntityCheckPredicate::checkFor)
    ).apply(instance, EntityCheckPredicate::new));

    public enum Type implements StringRepresentable {
        LIVING_ENTITY("living_entity"),
        ENEMY("enemy"),
        TAMED("tamed"),
        ANIMAL("animal"),
        ITEM("item"),
        EXPERIENCE_ORB("experience_orb");

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        private final String name;

        Type(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    @Override
    @SuppressWarnings("RedundantIfStatement") // ignore for clarity
    public boolean matches(@NotNull final Entity entity, @NotNull final ServerLevel level, @Nullable final Vec3 position) {
        if (checkFor().isPresent()) {
            boolean isValid = switch (checkFor().get()) {
                case LIVING_ENTITY -> entity instanceof LivingEntity;
                case ENEMY -> entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER;
                case TAMED -> entity instanceof TamableAnimal tamable && tamable.isTame();
                case ANIMAL -> entity instanceof Animal;
                case ITEM -> entity instanceof ItemEntity;
                case EXPERIENCE_ORB -> entity instanceof ExperienceOrb;
            };

            if (!isValid) {
                return false;
            }
        }

        return true;
    }

    @Override
    public @NotNull MapCodec<? extends EntitySubPredicate> codec() {
        return CODEC;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
    public static class Builder {
        private Optional<Type> type = Optional.empty();

        public static EntityCheckPredicate.Builder start() {
            return new EntityCheckPredicate.Builder();
        }

        public EntityCheckPredicate.Builder type(final Type type) {
            this.type = Optional.of(type);
            return this;
        }

        public EntityCheckPredicate.Builder living() {
            this.type = Optional.of(Type.LIVING_ENTITY);
            return this;
        }

        public EntityCheckPredicate.Builder enemy() {
            this.type = Optional.of(Type.ENEMY);
            return this;
        }

        public EntityCheckPredicate.Builder tamed() {
            this.type = Optional.of(Type.TAMED);
            return this;
        }

        public EntityCheckPredicate.Builder item() {
            this.type = Optional.of(Type.ITEM);
            return this;
        }

        public EntityCheckPredicate.Builder experienceOrb() {
            this.type = Optional.of(Type.EXPERIENCE_ORB);
            return this;
        }

        public EntityCheckPredicate build() {
            return new EntityCheckPredicate(type);
        }
    }
}
