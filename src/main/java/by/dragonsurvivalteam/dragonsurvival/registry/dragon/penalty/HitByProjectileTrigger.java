package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;

public record HitByProjectileTrigger(EntityType<?> projectileType) implements PenaltyTrigger {
    public static final MapCodec<HitByProjectileTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("projectileType").forGetter(HitByProjectileTrigger::projectileType)
    ).apply(instance, HitByProjectileTrigger::new));

    @Override
    public boolean hasCustomTrigger() {
        return true;
    }

    @Override
    public MapCodec<? extends PenaltyTrigger> codec() {
        return CODEC;
    }

    @Override
    public boolean matches(ServerPlayer dragon, boolean conditionMatched) {
        return conditionMatched;
    }
}
