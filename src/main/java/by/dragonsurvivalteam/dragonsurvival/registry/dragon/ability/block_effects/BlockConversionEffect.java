package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;

import java.util.List;

public record BlockConversionEffect(List<BlockConversionData> blockConversions, LevelBasedValue probability) implements AbilityBlockEffect {
    public static final MapCodec<BlockConversionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockConversionData.CODEC.listOf().fieldOf("conversion_data").forGetter(BlockConversionEffect::blockConversions),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(BlockConversionEffect::probability)
    ).apply(instance, BlockConversionEffect::new));

    public record BlockConversionData(HolderSet<Block> blocksFrom, List<BlockTo> blocksTo) {
        public static final Codec<BlockConversionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks_from").forGetter(BlockConversionData::blocksFrom),
                BlockTo.CODEC.listOf().fieldOf("blocks_to").forGetter(BlockConversionData::blocksTo)
        ).apply(instance, BlockConversionData::new));
    }

    public record BlockTo(Holder<Block> blockTo, double chance) {
        public static final Codec<BlockTo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(BlockTo::blockTo),
                Codec.DOUBLE.fieldOf("chance").forGetter(BlockTo::chance)
        ).apply(instance, BlockTo::new));
    }

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, final Direction direction) {
        if (dragon.getRandom().nextDouble() < probability().calculate(ability.level())) {
            return;
        }

        if (blockConversions().isEmpty()) {
            return;
        }


        for(BlockConversionData data : blockConversions) {
            if (data.blocksFrom().size() == 0) {
                continue;
            }

            if (data.blocksFrom().contains(dragon.serverLevel().getBlockState(position).getBlockHolder())) {
                double chance = dragon.getRandom().nextDouble();
                double sumOfOdds = 0;
                for (BlockTo blockTo : data.blocksTo) {
                    sumOfOdds += blockTo.chance();
                }

                chance *= sumOfOdds;
                for (BlockTo blockTo : data.blocksTo) {
                    chance -= blockTo.chance();
                    if (chance <= 0) {
                        dragon.serverLevel().setBlock(position, blockTo.blockTo().value().defaultBlockState(), Block.UPDATE_ALL);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
