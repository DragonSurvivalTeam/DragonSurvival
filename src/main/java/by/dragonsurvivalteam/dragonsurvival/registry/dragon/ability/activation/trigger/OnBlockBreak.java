package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
            BlockPos blockPos = event.getPos();
            BlockState blockState = event.getState();
            LootContext context = Condition.blockContext(player, blockPos, blockState);

            List<DragonAbilityInstance> passive_abilities = MagicData.getData(player).filterPassiveByTrigger(trigger -> trigger.type() == TriggerType.ON_BLOCK_BREAK && trigger.test(context));

            @NotNull Map<Boolean, @NotNull List<DragonAbilityInstance>> split_abilities = passive_abilities.stream().collect(Collectors.groupingBy(DragonAbilityInstance::isCancel));
            if (split_abilities.containsKey(true) && !split_abilities.get(true).isEmpty()) {
                split_abilities.get(true).forEach(ability -> ability.tick(player));
                event.setCanceled(true);
                return;
            }
            if (split_abilities.containsKey(false)) {
                split_abilities.get(false).forEach(ability -> ability.tick(player));
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
