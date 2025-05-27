package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

public record DragonGrowthEffect(GrowthType growth_type, ActionType action_type, LevelBasedValue amount, LevelBasedValue probability) implements AbilityEntityEffect {
    public static final MapCodec<DragonGrowthEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GrowthType.CODEC.fieldOf("growth_type").forGetter(DragonGrowthEffect::growth_type),
            ActionType.CODEC.fieldOf("action_type").forGetter(DragonGrowthEffect::action_type),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(DragonGrowthEffect::amount),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(DragonGrowthEffect::probability)
    ).apply(instance, DragonGrowthEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (target instanceof Player player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (!handler.isDragon()) {
                return;
            }

            if (dragon.getRandom().nextDouble() > probability().calculate(ability.level())) {
                return;
            }

            double amount = amount().calculate(ability.level());
            double difference = amount;
            double base = 0;

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

    /**
     * {@link ActionType#PERCENT} works based on the growth range of the current stage <br>
     * {@link ActionType#FLAT} works on the current growth (for {@link GrowthType#ADD} or with 0 (for {@link GrowthType#SET})
     */
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
