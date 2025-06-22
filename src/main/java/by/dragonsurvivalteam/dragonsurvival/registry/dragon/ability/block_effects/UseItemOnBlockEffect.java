package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record UseItemOnBlockEffect(ItemStack item, LevelBasedValue probability, Optional<Holder<SoundEvent>> sound, Optional<BlockPredicate> validBlocks) implements AbilityBlockEffect {
    public static final MapCodec<UseItemOnBlockEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemStack.CODEC.fieldOf("item").forGetter(UseItemOnBlockEffect::item),
            LevelBasedValue.CODEC.fieldOf("probability").forGetter(UseItemOnBlockEffect::probability),
            SoundEvent.CODEC.optionalFieldOf("sound").forGetter(UseItemOnBlockEffect::sound),
            BlockPredicate.CODEC.optionalFieldOf("valid_blocks").forGetter(UseItemOnBlockEffect::validBlocks)
    ).apply(instance, UseItemOnBlockEffect::new));

    @Override
    public void apply(ServerPlayer dragon, DragonAbilityInstance ability, BlockPos position, @Nullable Direction direction) {
        if (dragon.getRandom().nextDouble() > probability().calculate(ability.level())) {
            return;
        }

        ServerLevel level = dragon.serverLevel();
        if (validBlocks.isEmpty() || validBlocks.get().test(dragon.serverLevel(), position)) {

            ItemStack newStack = new ItemStack(item.getItem());
            InteractionResult ir = newStack.useOn(
                    new UseOnContext(level, dragon, InteractionHand.MAIN_HAND, newStack,
                    new BlockHitResult(dragon.position(), direction, position, false))
            );
            if (ir.consumesAction()) {
                sound.ifPresent(soundHolder -> dragon.level().playSound(null, position, soundHolder.value(), SoundSource.BLOCKS, 1, 1));
            }
        }
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
