package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public record ExplodeBlockEffect(LevelBasedValue probability, LevelBasedValue power, boolean fire, Holder<DamageType> damageType) implements AbilityBlockEffect {
    public static final MapCodec<ExplodeBlockEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(ExplodeBlockEffect::probability),
            LevelBasedValue.CODEC.fieldOf("power").forGetter(ExplodeBlockEffect::power),
            Codec.BOOL.fieldOf("fire").orElse(true).forGetter(ExplodeBlockEffect::fire),
            DamageType.CODEC.fieldOf("damage_type").forGetter(ExplodeBlockEffect::damageType)
        ).apply(instance, ExplodeBlockEffect::new)
    );

    @Override
    public void apply(ServerPlayer dragon, DragonAbilityInstance ability, BlockPos position, @Nullable Direction direction) {
        dragon.serverLevel().explode(dragon, new DamageSource(damageType, dragon), null, position.getCenter(), power.calculate(ability.level()), fire, Level.ExplosionInteraction.BLOCK);
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
