package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.Optional;

public record OnSelfHit(Optional<LootItemCondition> condition) implements ActivationTrigger {
    public static final MapCodec<OnSelfHit> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("condition").forGetter(OnSelfHit::condition)
    ).apply(instance, OnSelfHit::new));

    public static void trigger(final LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (!handler.isDragon()) {
                return;
            }

            LootContext context = Condition.damageContext(player.serverLevel(), player, event.getSource(), event.getSource().getEntity() instanceof LivingEntity livingEntity ? livingEntity.getMainHandItem() : ItemStack.EMPTY);
            MagicData.getData(player).filterPassiveByTrigger(trigger -> trigger.type() == TriggerType.ON_SELF_HIT && trigger.test(context))
                    .forEach(ability -> {
                        if (!ability.triggered) {
                            ability.triggered = true;
                            ability.tick(player);
                        }
                    });
        }
    }

    @Override
    public boolean test(final LootContext context) {
        return this.condition.map(condition -> condition.test(context)).orElse(true);
    }

    @Override
    public TriggerType type() {
        return TriggerType.ON_SELF_HIT;
    }

    @Override
    public MapCodec<? extends ActivationTrigger> codec() {
        return CODEC;
    }
}
