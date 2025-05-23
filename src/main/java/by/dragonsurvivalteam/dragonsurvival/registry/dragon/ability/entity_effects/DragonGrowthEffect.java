package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.BlockVision;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record DragonGrowthEffect(DragonGrowthEffect.GrowthType growth_type, LevelBasedValue amount, LevelBasedValue probability, Optional<EntityPredicate> validEntities) implements AbilityEntityEffect {
    public static final MapCodec<DragonGrowthEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GrowthType.CODEC.fieldOf("growth_type").forGetter(DragonGrowthEffect::growth_type),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(DragonGrowthEffect::amount),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(DragonGrowthEffect::probability),
            EntityPredicate.CODEC.optionalFieldOf("valid_entities").forGetter(DragonGrowthEffect::validEntities)
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

            if (probability.calculate(ability.level()) < dragon.getRandom().nextDouble()) {
                return;
            }

            double amount = amount().calculate(ability.level());
            double targetGrowth = switch (growth_type) {
                case GrowthType.ADD -> handler.getGrowth() + amount;
                case GrowthType.SUBTRACT -> handler.getGrowth() - amount;
                default -> amount;
            };

            ServerLevel level = dragon.serverLevel();
            if (validEntities.isEmpty() || validEntities.get().matches(level, dragon.position(), target)) {
                handler.setDesiredGrowth(targetPlayer, targetGrowth);
            }
        }
    }

    public enum GrowthType implements StringRepresentable {
        SET("set"), ADD("add"), SUBTRACT("subtract");

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

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
