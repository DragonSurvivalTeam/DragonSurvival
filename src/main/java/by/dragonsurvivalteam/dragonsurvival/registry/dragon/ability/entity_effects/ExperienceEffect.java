package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.ExperienceUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;

public record ExperienceEffect(ExperienceType experienceType, LevelBasedValue amount, LevelBasedValue probability) implements AbilityEntityEffect {
    @Translation(comments = "§6■ Adjusts the current experience§r %s, increasing them by %s")
    public static final String ADJUST_POSITIVE = Translation.Type.GUI.wrap("experience.adjust_positive");

    @Translation(comments = "§6■ Adjusts the current experience§r %s, reducing them by %s")
    public static final String ADJUST_NEGATIVE = Translation.Type.GUI.wrap("experience.adjust_negative");

    @Translation(comments = "levels")
    public static final String LEVELS = Translation.Type.GUI.wrap("experience.points");

    @Translation(comments = "points")
    public static final String POINTS = Translation.Type.GUI.wrap("experience.levels");

    public static final MapCodec<ExperienceEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExperienceType.CODEC.fieldOf("experience_type").forGetter(ExperienceEffect::experienceType),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(ExperienceEffect::amount),
            LevelBasedValue.CODEC.optionalFieldOf("probability", LevelBasedValue.constant(1)).forGetter(ExperienceEffect::probability)
    ).apply(instance, ExperienceEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof ServerPlayer player)) {
            return;
        }

        if (dragon.getRandom().nextDouble() > probability.calculate(ability.level())) {
            return;
        }

        int amount = (int) this.amount.calculate(ability.level());

        // The only method that properly handles the experience progress and total experience fields is 'giveExperiencePoints'
        switch (experienceType) {
            case LEVELS -> player.giveExperiencePoints(ExperienceUtils.getTotalExperience(amount));
            case POINTS -> player.giveExperiencePoints(amount);
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        int amount = (int) this.amount.calculate(ability.level());
        String translationKey;

        if (amount > 0) {
            translationKey = ADJUST_NEGATIVE;
        } else {
            translationKey = ADJUST_POSITIVE;
            amount = -amount;
        }

        MutableComponent component = Component.translatable(translationKey, DSColors.dynamicValue(experienceType.getTranslation()), DSColors.dynamicValue(amount));
        float probability = this.probability.calculate(ability.level());

        if (probability < 1) {
            component.append(Component.translatable(LangKey.ABILITY_EFFECT_CHANCE, DSColors.dynamicValue(NumberFormat.getPercentInstance().format(probability))));
        }

        return List.of(component);
    }

    public enum ExperienceType implements StringRepresentable {
        LEVELS("levels", Component.translatable(ExperienceEffect.LEVELS)),
        POINTS("points", Component.translatable(ExperienceEffect.POINTS));

        public static final Codec<ExperienceType> CODEC = StringRepresentable.fromEnum(ExperienceType::values);
        private final String name;
        private final MutableComponent translation;

        ExperienceType(final String name, final MutableComponent translation) {
            this.name = name;
            this.translation = translation;
        }

        public MutableComponent getTranslation() {
            return translation;
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
