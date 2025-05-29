package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record DropItemEffect(List<ItemSlot> items, MovementType movement, Optional<ConfigurableSound> sound) implements AbilityEntityEffect {
    public static final MapCodec<DropItemEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemSlot.CODEC.listOf().fieldOf("items").forGetter(DropItemEffect::items),
            MovementType.CODEC.fieldOf("direction").forGetter(DropItemEffect::movement),
            ConfigurableSound.CODEC.optionalFieldOf("sound").forGetter(DropItemEffect::sound)
    ).apply(instance, DropItemEffect::new));

    public record ItemSlot(EquipmentSlot slot, Optional<ItemPredicate> predicate, Optional<LevelBasedValue> probability) {
        public static final Codec<ItemSlot> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EquipmentSlot.CODEC.fieldOf("slot").forGetter(ItemSlot::slot),
                ItemPredicate.CODEC.optionalFieldOf("predicate").forGetter(ItemSlot::predicate),
                LevelBasedValue.CODEC.optionalFieldOf("probability").forGetter(ItemSlot::probability)
        ).apply(instance, ItemSlot::new));

        public boolean test(final LivingEntity entity, final int level) {
            ItemStack stack = entity.getItemBySlot(slot);

            if (stack.isEmpty()) {
                return false;
            }

            if (probability.map(probability -> entity.getRandom().nextDouble() > probability.calculate(level)).orElse(false)) {
                return false;
            }

            return predicate.map(predicate -> predicate.test(stack)).orElse(true);
        }
    }

    public record ConfigurableSound(Holder<SoundEvent> sound, Optional<LevelBasedValue> volume, Optional<LevelBasedValue> pitch) {
        public static final Codec<ConfigurableSound> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            SoundEvent.CODEC.fieldOf("sound").forGetter(ConfigurableSound::sound),
            LevelBasedValue.CODEC.optionalFieldOf("volume").forGetter(ConfigurableSound::volume),
            LevelBasedValue.CODEC.optionalFieldOf("pitch").forGetter(ConfigurableSound::pitch)
        ).apply(instance, ConfigurableSound::new));

        public void playSound(final Entity target, final Level level, final int vol, final int pit) {
            float resultVol = volume.map(volume -> volume.calculate(vol)).orElse(0.0F);
            if (resultVol <= 0) {
                return;
            }
            float resultPit = pitch.map(pitch -> pitch.calculate(pit)).orElse(0.0F);
            level.playSound(null, target, sound.value(), SoundSource.BLOCKS, resultVol, resultPit);
        }
    }

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (target instanceof LivingEntity entity) {

            Vec3 delta = Vec3.ZERO;
            int direction = switch (movement) {
                case MovementType.TOWARDS -> 1;
                case MovementType.AWAY -> -1;
                default -> 0;
            };

            if (direction != 0) {
                // Maybe normalize and add an intensity instead of using the distance?
                delta = target.getEyePosition().subtract(dragon.getEyePosition()).multiply(direction, direction, direction);
            }

            int numRemoved = 0;
            for (ItemSlot item : items) {
                if (item.test(entity, ability.level())) {
                    ItemStack stack = entity.getItemBySlot(item.slot());
                    entity.setItemSlot(item.slot(), ItemStack.EMPTY);
                    entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), stack, delta.x(), delta.y(), delta.z()));
                    numRemoved++;
                }
            }

            int finalNumRemoved = numRemoved;
            sound.ifPresent(configurableSound -> configurableSound.playSound(target, dragon.level(), finalNumRemoved, finalNumRemoved));
        }
    }

    public enum MovementType implements StringRepresentable {
        NONE("none"), TOWARDS("towards"), AWAY("away");

        public static final Codec<MovementType> CODEC = StringRepresentable.fromEnum(MovementType::values);
        private final String name;

        MovementType(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
