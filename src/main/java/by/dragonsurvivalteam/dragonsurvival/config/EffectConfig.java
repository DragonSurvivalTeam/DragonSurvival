package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.ResourceLocationWrapper;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

import java.util.Set;

// FIXME :: add generic codec config and handle custom configs through that
public class EffectConfig implements CustomConfig {
    private static final String SPLIT = ";";

    private final Set<ResourceLocation> effects;
    private final int duration;
    private final int amplifier;
    private final double durationMultiplier;
    private final double amplifierMultiplier;

    private final String originalData;

    /** Required to handle creation, parsing etc. (created through reflection) */
    public EffectConfig() {
        this(Set.of(), 0, 0, 0, 0, "");
    }

    private EffectConfig(final Set<ResourceLocation> effects, final int duration, final int amplifier, final double durationMultiplier, final double amplifierMultiplier, final String originalData) {
        this.effects = effects;
        this.duration = duration;
        this.amplifier = amplifier;
        this.durationMultiplier = durationMultiplier;
        this.amplifierMultiplier = amplifierMultiplier;
        this.originalData = originalData;
    }

    public static EffectConfig create(final Holder<MobEffect> effect, final int duration, final int amplifier, final double durationMultiplier, final double amplifierMultiplier) {
        String data = effect.getRegisteredName() + SPLIT + duration + SPLIT + amplifier + SPLIT + durationMultiplier + SPLIT + amplifierMultiplier;
        //noinspection DataFlowIssue -> key is present
        return new EffectConfig(Set.of(effect.getKey().location()), duration, amplifier, durationMultiplier, amplifierMultiplier, data);
    }

    public void applyEffects(final Player player, int level) {
        effects.forEach(resource -> BuiltInRegistries.MOB_EFFECT.getHolder(resource).ifPresent(effect -> {
            int duration = (int) (this.duration * (1 + level * durationMultiplier));
            int amplifier = (int) ((1 + this.amplifier) * (1 + level * amplifierMultiplier));

            // Subtract 1 from the amplifier since the calculation was done with +1 to it for level 0 amplifier
            MobEffectInstance instance = new MobEffectInstance(effect, duration, amplifier - 1, false, true);
            player.addEffect(instance);
        }));
    }

    @Override
    public String convert() {
        return originalData;
    }

    @Override
    public boolean validate(final Object configValue) {
        if (configValue instanceof String string) {
            String[] elements = string.split(SPLIT);

            if (elements.length != 5) {
                return false;
            }

            if (!ResourceLocationWrapper.validateRegexResourceLocation(elements[EFFECTS])) {
                return false;
            }

            try {
                int duration = Integer.parseInt(elements[DURATION]);
                int amplifier = Integer.parseInt(elements[AMPLIFIER]);
                double durationMultiplier = Double.parseDouble(elements[DURATION_MULTIPLIER]);
                double amplifierMultiplier = Double.parseDouble(elements[AMPLIFIER_MULTIPLIER]);

                if (duration <= 0 || amplifier < 0 || durationMultiplier < 0 || amplifierMultiplier < 0) {
                    return false;
                }
            } catch (NumberFormatException ignored) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public CustomConfig parse(final String data) {
        String[] elements = data.split(SPLIT);

        Set<ResourceLocation> entries = ResourceLocationWrapper.getEntries(elements[EFFECTS], BuiltInRegistries.MOB_EFFECT);
        int duration = Integer.parseInt(elements[DURATION]);
        int amplifier = Integer.parseInt(elements[AMPLIFIER]);
        double durationMultiplier = Double.parseDouble(elements[DURATION_MULTIPLIER]);
        double amplifierMultiplier = Double.parseDouble(elements[AMPLIFIER_MULTIPLIER]);

        return new EffectConfig(entries, duration, amplifier, durationMultiplier, amplifierMultiplier, data);
    }

    private static final int EFFECTS = 0;
    private static final int DURATION = 1;
    private static final int AMPLIFIER = 2;
    private static final int DURATION_MULTIPLIER = 3;
    private static final int AMPLIFIER_MULTIPLIER = 4;
}
