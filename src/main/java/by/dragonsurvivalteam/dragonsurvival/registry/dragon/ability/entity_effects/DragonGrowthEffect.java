package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

public record DragonGrowthEffect(DragonGrowthEffect.GrowthType growth_type, DragonGrowthEffect.ActionType action_type, LevelBasedValue amount, LevelBasedValue probability) implements AbilityEntityEffect {
    public static final MapCodec<DragonGrowthEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GrowthType.CODEC.fieldOf("growth_type").forGetter(DragonGrowthEffect::growth_type),
            ActionType.CODEC.fieldOf("action_type").forGetter(DragonGrowthEffect::action_type),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(DragonGrowthEffect::amount),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(DragonGrowthEffect::probability)
    ).apply(instance, DragonGrowthEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (target instanceof Player targetPlayer) {
            DragonStateHandler handler = DragonStateProvider.getData(targetPlayer);

            if (!handler.isDragon()) {
                return;
            }

            if (dragon.getRandom().nextDouble() > probability().calculate(ability.level())) {
                return;
            }

            double amount = amount().calculate(ability.level());
            if (action_type == ActionType.PERCENT) {
                Holder<DragonStage> dragonStage = handler.stage();
                MiscCodecs.Bounds growthRange = dragonStage.value().growthRange();
                double maxSize = growthRange.max();
                double minSize = growthRange.min();
                // Maybe get maximum overall size instead of the current stage?
                if (growth_type == GrowthType.ADD) {
                    amount = amount * 0.01 * (maxSize - minSize);
                } else {
                    amount = amount * 0.01 * (maxSize + minSize);
                }
            }

            double targetGrowth = amount;
            if (growth_type == GrowthType.ADD) {
                targetGrowth += handler.getGrowth();
            }

            handler.setDesiredGrowth(targetPlayer, targetGrowth);
        }
    }

    public enum GrowthType implements StringRepresentable {
        SET("set"), ADD("add");

        public static final Codec<DragonGrowthEffect.GrowthType> CODEC = StringRepresentable.fromEnum(DragonGrowthEffect.GrowthType::values);
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

        public static final Codec<DragonGrowthEffect.ActionType> CODEC = StringRepresentable.fromEnum(DragonGrowthEffect.ActionType::values);
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
