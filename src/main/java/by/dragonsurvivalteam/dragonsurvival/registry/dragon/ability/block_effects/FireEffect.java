package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public record FireEffect(LevelBasedValue igniteProbability) implements AbilityBlockEffect {
    public static final MapCodec<FireEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("ignite_probability").forGetter(FireEffect::igniteProbability)
    ).apply(instance, FireEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, final Direction direction) {
        BlockState state = dragon.serverLevel().getBlockState(position);
        Block block = state.getBlock();

        if (block instanceof TntBlock tnt) {
            tnt.onCaughtFire(state, dragon.level(), position, direction, dragon);
            dragon.level().setBlock(position, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
        } else if (block instanceof CampfireBlock && !state.getValue(CampfireBlock.LIT)) {
            dragon.level().setBlock(position, state.setValue(CampfireBlock.LIT, true), Block.UPDATE_ALL_IMMEDIATE);
        } else if (state.getOptionalValue(BlockStateProperties.SNOWY).orElse(false)) {
            dragon.level().setBlock(position, state.setValue(BlockStateProperties.SNOWY, false), Block.UPDATE_ALL_IMMEDIATE);
        } else if (FireBlock.canBePlacedAt(dragon.level(), position, direction) && dragon.getRandom().nextDouble() < igniteProbability.calculate(ability.level())) {
            BlockState fireBlockState = FireBlock.getState(dragon.level(), position);
            dragon.level().setBlock(position, fireBlockState, Block.UPDATE_ALL_IMMEDIATE);
            state.onCaughtFire(dragon.level(), position, direction, dragon);
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
