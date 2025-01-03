package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.BlockPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

public record BlockBreakEffect(BlockPredicate validBlocks, LevelBasedValue probability) implements AbilityBlockEffect {
    public static final MapCodec<BlockBreakEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockPredicate.CODEC.fieldOf("valid_blocks").forGetter(BlockBreakEffect::validBlocks),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(BlockBreakEffect::probability)
    ).apply(instance, BlockBreakEffect::new));

    @Override
    public void apply(ServerPlayer dragon, DragonAbilityInstance ability, BlockPos position, @Nullable Direction direction) {
        if (dragon.getRandom().nextDouble() < probability.calculate(ability.level())) {
            return;
        }

        // This allows the state to be cached and not retrieved for every check
        BlockInWorld block = new BlockInWorld(dragon.serverLevel(), position, false);

        if (validBlocks.matches(block)) {
            dragon.serverLevel().destroyBlock(position, true);
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
