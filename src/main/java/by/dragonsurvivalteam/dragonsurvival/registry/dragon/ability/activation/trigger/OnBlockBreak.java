package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.Optional;

public record OnBlockBreak(Optional<LootItemCondition> condition) implements ActivationTrigger {
    public static final MapCodec<OnBlockBreak> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("condition").forGetter(OnBlockBreak::condition)
    ).apply(instance, OnBlockBreak::new));


    public static void trigger(final BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (!handler.isDragon()) {
                return;
            }
            ServerLevel level = player.serverLevel();
            BlockPos blockPos = event.getPos();
            BlockState origBlockState = event.getState();
            LootContext context = Condition.blockContext(player, blockPos, origBlockState);

            MagicData.getData(player).filterPassiveByTrigger(trigger -> trigger.type() == TriggerType.ON_BLOCK_BREAK && trigger.test(context)).forEach(ability -> ability.tick(player));

            /** Cancel because the block changed after the abilities ticked */
            if (level.getBlockState(blockPos) != origBlockState) {
                event.setCanceled(true);
            }
        }
    }

    @Override
    public boolean test(final LootContext context) {
        return this.condition.map(condition -> condition.test(context)).orElse(true);
    }

    @Override
    public TriggerType type() {
        return TriggerType.ON_BLOCK_BREAK;
    }

    @Override
    public MapCodec<? extends ActivationTrigger> codec() {
        return CODEC;
    }

}
