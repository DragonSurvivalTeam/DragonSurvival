package by.dragonsurvivalteam.dragonsurvival.common.criteria;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MineBlockUnderLavaTrigger extends SimpleCriterionTrigger<MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance> {
    public void trigger(ServerPlayer player, BlockState state) {
        // If no block is specified it will act as any block should trigger the advancement
        this.trigger(player, instance -> instance.block.map(state::is).orElse(true));
    }

    @Override
    public @NotNull Codec<MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance> codec() {
        return MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance.CODEC;
    }

    public record MineBlockUnderLavaInstance(Optional<ContextAwarePredicate> player, Optional<HolderSet<Block>> block) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance::player),
                RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("block").forGetter(MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance::block)
        ).apply(instance, MineBlockUnderLavaTrigger.MineBlockUnderLavaInstance::new));
    }
}
