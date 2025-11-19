package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import org.jetbrains.annotations.Nullable;

public record BlockBreakEffect(BlockPredicate validBlocks, LevelBasedValue probability, boolean dropLoot) implements AbilityBlockEffect {
    public static final MapCodec<BlockBreakEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            // TODO 1.22 :: Is this even needed / useful, considering the existing targeting logic?
            BlockPredicate.CODEC.fieldOf("valid_blocks").forGetter(BlockBreakEffect::validBlocks),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(BlockBreakEffect::probability),
            // TODO 1.22 :: change default to true since by itself you'd expect this "harvest" effect to drop the loot
            Codec.BOOL.optionalFieldOf("drop_loot", false).forGetter(BlockBreakEffect::dropLoot)
    ).apply(instance, BlockBreakEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, @Nullable final Direction direction) {
        if (probability.calculate(ability.level()) < dragon.getRandom().nextDouble()) {
            return;
        }

        if (validBlocks.test(dragon.serverLevel(), position)) {
            dragon.serverLevel().destroyBlock(position, dropLoot);
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
