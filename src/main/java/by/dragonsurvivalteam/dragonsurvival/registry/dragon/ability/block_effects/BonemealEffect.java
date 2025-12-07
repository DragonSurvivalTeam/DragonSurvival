package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

public record BonemealEffect(LevelBasedValue attempts, LevelBasedValue probability) implements AbilityBlockEffect {
    public static final MapCodec<BonemealEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LevelBasedValue.CODEC.fieldOf("attempts").forGetter(BonemealEffect::attempts),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(BonemealEffect::probability)
    ).apply(instance, BonemealEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, final Direction direction) {
        if (dragon.getRandom().nextDouble() > probability.calculate(ability.level())) {
            return;
        }

        for (int i = 0; i < attempts.calculate(ability.level()); i++) {
            // We need to re-fetch the state every time since some blocks may modify the block at the current position
            // For example: Saplings grow a tree and always return 'true' for being a valid bonemeal target
            // This results in another attempt to grow the tree, which fails and re-places the sapling into the trunk of the grown tree
            BlockState state = dragon.serverLevel().getBlockState(position);

            if (state.getBlock() instanceof BonemealableBlock bonemealableBlock) {
                if (!bonemealableBlock.isValidBonemealTarget(dragon.level(), position, state)) {
                    return;
                }

                bonemealableBlock.performBonemeal(dragon.serverLevel(), dragon.getRandom(), position, state);
                // '15' is the particle count, see BoneMealItem#addGrowthParticles
                dragon.level().levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, position, 15);
            }
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
