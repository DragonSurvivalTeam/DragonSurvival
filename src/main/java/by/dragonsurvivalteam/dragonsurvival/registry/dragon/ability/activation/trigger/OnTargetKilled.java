package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.Optional;

public record OnTargetKilled(Optional<LootItemCondition> condition) implements ActivationTrigger<LootContext> {
    @Translation(comments = "On Target Killed")
    private static final String TRANSLATION = Translation.Type.TRIGGER_TYPE.wrap("on_target_killed");

    public static final MapCodec<OnTargetKilled> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("condition").forGetter(OnTargetKilled::condition)
    ).apply(instance, OnTargetKilled::new));

    public static void trigger(final LivingEntity entity, final DamageSource source) {
        if (source.getEntity() instanceof ServerPlayer player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (!handler.isDragon()) {
                return;
            }

            LootContext context = Condition.damageContext(player.serverLevel(), entity, source, null);
            MagicData.getData(player).filterPassiveByTrigger(trigger -> trigger instanceof OnTargetKilled onTargetKilled && onTargetKilled.test(context))
                    .forEach(ability -> ability.tick(player)); // We do not check for 'triggered' here since chain-reactions seem reasonable here
        }
    }

    @Override
    public boolean test(final LootContext context) {
        return this.condition.map(condition -> condition.test(context)).orElse(true);
    }

    @Override
    public Component translation() {
        return Component.translatable(TRANSLATION);
    }

    @Override
    public MapCodec<? extends ActivationTrigger<?>> codec() {
        return CODEC;
    }
}
