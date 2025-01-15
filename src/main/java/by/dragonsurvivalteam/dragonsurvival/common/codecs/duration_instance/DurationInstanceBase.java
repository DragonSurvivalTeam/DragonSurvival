package by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.Storage;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.StorageEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilities;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.attachment.AttachmentType;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
public class DurationInstanceBase<B extends Storage<I>, I extends DurationInstance<?> & StorageEntry> {
    public static final Codec<DurationInstanceBase<?, ?>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(DurationInstanceBase::id),
            LevelBasedValue.CODEC.optionalFieldOf("duration", DragonAbilities.INFINITE_DURATION).forGetter(DurationInstanceBase::duration),
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("early_removal_condition").forGetter(DurationInstanceBase::earlyRemovalCondition),
            ResourceLocation.CODEC.optionalFieldOf("custom_icon").forGetter(DurationInstanceBase::customIcon),
            Codec.BOOL.optionalFieldOf("is_hidden", false).forGetter(DurationInstanceBase::isHidden)
    ).apply(instance, DurationInstanceBase::new));

    private final ResourceLocation id;
    private final LevelBasedValue duration;
    private final Optional<LootItemCondition> earlyRemovalCondition;
    private final Optional<ResourceLocation> customIcon;
    private final boolean isHidden;

    public DurationInstanceBase(final DurationInstanceBase<?, ?> base) {
        this(base.id(), base.duration(), base.earlyRemovalCondition(), base.customIcon(), base.isHidden());
    }

    public DurationInstanceBase(final ResourceLocation id, final LevelBasedValue duration, final Optional<LootItemCondition> earlyRemovalCondition, final Optional<ResourceLocation> customIcon, final boolean isHidden) {
        this.id = id;
        this.duration = duration;
        this.earlyRemovalCondition = earlyRemovalCondition;
        this.customIcon = customIcon;
        this.isHidden = isHidden;
    }

    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        int newDuration = (int) duration.calculate(ability.level());

        B data = target.getData(type());
        I instance = data.get(id);

        if (instance != null && instance.appliedAbilityLevel() == ability.level() && instance.currentDuration() == newDuration) {
            return;
        }

        data.remove(target, instance);
        data.add(target, createInstance(dragon, ability, newDuration));
    }

    public void remove(final Entity target) {
        B data = target.getData(type());
        data.remove(target, data.get(id));
    }

    public static Builder create(final ResourceLocation id) {
        return new Builder(id);
    }

    public ResourceLocation id() {
        return id;
    }

    public LevelBasedValue duration() {
        return duration;
    }

    public Optional<LootItemCondition> earlyRemovalCondition() {
        return earlyRemovalCondition;
    }

    public Optional<ResourceLocation> customIcon() {
        return customIcon;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public I createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, int currentDuration) {
        throw new AssertionError("This methods requires an override");
    }

    public AttachmentType<B> type() {
        throw new AssertionError("This methods requires an override");
    }

    public static class Builder {
        private final ResourceLocation id;
        private LevelBasedValue duration;
        private Optional<LootItemCondition> earlyRemovalCondition = Optional.empty();
        private Optional<ResourceLocation> customIcon = Optional.empty();
        private boolean isHidden = false;

        public Builder(final ResourceLocation id) {
            this.id = id;
        }

        public Builder infinite() {
            this.duration = DragonAbilities.INFINITE_DURATION;
            return this;
        }

        public Builder duration(final LevelBasedValue duration) {
            this.duration = duration;
            return this;
        }

        public Builder earlyRemoval(final LootItemCondition condition) {
            this.earlyRemovalCondition = Optional.ofNullable(condition);
            return this;
        }

        public Builder customIcon(final ResourceLocation icon) {
            this.customIcon = Optional.ofNullable(icon);
            return this;
        }

        public Builder hidden() {
            this.isHidden = true;
            return this;
        }

        public DurationInstanceBase<?, ?> build() {
            return new DurationInstanceBase<>(id, duration, earlyRemovalCondition, customIcon, isHidden);
        }
    }
}
