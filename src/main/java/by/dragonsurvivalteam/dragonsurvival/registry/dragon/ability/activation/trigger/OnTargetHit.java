package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Optional;

public record OnTargetHit(Optional<LootItemCondition> condition) implements ActivationTrigger {
    public static final MapCodec<OnTargetHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("condition").forGetter(OnTargetHit::condition)
    ).apply(instance, OnTargetHit::new));

    public static void trigger(final LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (!handler.isDragon()) {
                return;
            }

            LootContext context = Condition.damageContext(player.serverLevel(), event.getEntity(), event.getSource(), player.getMainHandItem());
            MagicData.getData(player).filterPassiveByTrigger(trigger -> trigger.type() == TriggerType.ON_TARGET_HIT && trigger.test(context)).forEach(ability -> ability.tick(player));
        }
    }

    @Override
    public boolean test(final LootContext context) {
        return this.condition.map(condition -> condition.test(context)).orElse(true);
    }

    @Override
    public TriggerType type() {
        return TriggerType.ON_TARGET_HIT;
    }

    @Override
    public MapCodec<? extends ActivationTrigger> codec() {
        return CODEC;
    }
}
