package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
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

public record DragonGrowthEffect(GrowthType growth_type, ActionType action_type, LevelBasedValue amount, LevelBasedValue probability) implements AbilityEntityEffect {
    @Translation(comments = "§6■ Adjust the growth§r by setting it to %s of the current stage")
    public static final String ADJUST_SET_PERCENT = Translation.Type.GUI.wrap("growth_effect.adjust_set.percent");

    @Translation(comments = "§6■ Adjust the growth§r by setting it to %s")
    public static final String ADJUST_SET_FLAT = Translation.Type.GUI.wrap("growth_effect.adjust_set.flat");

    @Translation(comments = "§6■ Adjust the growth§r by adding %s of the current stage bounds to it")
    public static final String ADJUST_ADD_PERCENT = Translation.Type.GUI.wrap("growth_effect.adjust_add.percent");

    @Translation(comments = "§6■ Adjust the growth§r by adding %s to it")
    public static final String ADJUST_ADD_FLAT = Translation.Type.GUI.wrap("growth_effect.adjust_add.flat");

    public static final MapCodec<DragonGrowthEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            // FIXME 1.22 :: change these field names to match ManaRecoveryEffect
            GrowthType.CODEC.fieldOf("growth_type").forGetter(DragonGrowthEffect::growth_type),
            ActionType.CODEC.fieldOf("action_type").forGetter(DragonGrowthEffect::action_type),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(DragonGrowthEffect::amount),
            LevelBasedValue.CODEC.optionalFieldOf("probability", LevelBasedValue.constant(1)).forGetter(DragonGrowthEffect::probability)
    ).apply(instance, DragonGrowthEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (target instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (!handler.isDragon()) {
                return;
            }

            if (dragon.getRandom().nextDouble() > probability.calculate(ability.level())) {
                return;
            }

            double amount = this.amount.calculate(ability.level());
            double difference = amount;
            double base = 0; // Value that will be used for the combination of SET & FLAT

            if (growth_type == GrowthType.ADD) {
                base = handler.getGrowth();
            }

            if (action_type == ActionType.PERCENT) {
                MiscCodecs.Bounds growthRange = handler.stage().value().growthRange();
                double maxGrowth = growthRange.max();
                double minGrowth = growthRange.min();
                // TODO :: add some other parameter that works off of the overall growth (stage progression min & max)
                //         by using 'handler.getStages(player.registryAccess())'
                difference = amount * (maxGrowth - minGrowth);

                if (growth_type == GrowthType.SET) {
                    base = minGrowth;
                }
            }

            handler.setDesiredGrowth(player, base + difference);
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        MutableComponent component = switch (growth_type) {
            case SET -> switch (action_type) {
                case PERCENT -> Component.translatable(ADJUST_SET_PERCENT, DSColors.dynamicValue(NumberFormat.getPercentInstance().format(amount.calculate(ability.level()))));
                case FLAT -> Component.translatable(ADJUST_SET_FLAT, DSColors.dynamicValue(amount.calculate(ability.level())));
            };
            case ADD -> switch (action_type) {
                case PERCENT -> Component.translatable(ADJUST_ADD_PERCENT, DSColors.dynamicValue(NumberFormat.getPercentInstance().format(amount.calculate(ability.level()))));
                case FLAT -> Component.translatable(ADJUST_ADD_FLAT, DSColors.dynamicValue(amount.calculate(ability.level())));
            };
        };

        float probability = this.probability.calculate(ability.level());

        if (probability < 1) {
            component.append(Component.translatable(LangKey.ABILITY_EFFECT_CHANCE, DSColors.dynamicValue(NumberFormat.getPercentInstance().format(probability))));
        }

        return List.of(component);
    }

    public enum GrowthType implements StringRepresentable {
        SET("set"), ADD("add");

        public static final Codec<GrowthType> CODEC = StringRepresentable.fromEnum(GrowthType::values);
        private final String name;

        GrowthType(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    public enum ActionType implements StringRepresentable {
        PERCENT("percent"), FLAT("flat");

        public static final Codec<ActionType> CODEC = StringRepresentable.fromEnum(ActionType::values);
        private final String name;

        ActionType(final String name) {
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
