package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;

public class MineBlockUnderLavaTrigger extends SimpleCriterionTrigger<MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance> {
    public void trigger(ServerPlayer pPlayer, Block block) {
        this.trigger(pPlayer, triggerInstance -> {
            if (triggerInstance.block.isPresent()) {
                return triggerInstance.block.get().value().equals(block);
            }
            return true;
        });
    }

    @Override
    public Codec<MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance> codec() {
        return MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance.CODEC;
    }

    public record MineBlockUnderLavaInstance(Optional<ContextAwarePredicate> player, Optional<Holder<Block>> block) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance::player),
                BuiltInRegistries.BLOCK.holderByNameCodec().optionalFieldOf("block").forGetter(MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance::block)
        ).apply(instance, MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance::new));
    }
}
