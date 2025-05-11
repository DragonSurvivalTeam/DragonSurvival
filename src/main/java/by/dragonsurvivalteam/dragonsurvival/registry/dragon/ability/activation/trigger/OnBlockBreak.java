package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.PassiveActivation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.List;
import java.util.Optional;

public record OnBlockBreak(Optional<LootItemCondition> condition, Optional<String> cancel_variant) implements ActivationTrigger {
    public static final MapCodec<OnBlockBreak> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("condition").forGetter(OnBlockBreak::condition),
            Codec.STRING.optionalFieldOf("cancel_variant").forGetter(OnBlockBreak::cancel_variant)
    ).apply(instance, OnBlockBreak::new));

    public static void trigger(final BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            DragonStateHandler handler = DragonStateProvider.getData(player);

            if (!handler.isDragon()) {
                return;
            }
            ServerLevel level = player.serverLevel();
            BlockPos blockPos = event.getPos();
            BlockState blockState = event.getState();
            LootContext context = Condition.blockContext(player, blockPos, blockState);


            List<DragonAbilityInstance> abilities = MagicData.getData(player).filterPassiveByTrigger(trigger -> trigger.type() == TriggerType.ON_BLOCK_BREAK && trigger.test(context));
            abilities.forEach(ability -> ability.tick(player));

            boolean silky = abilities.stream().anyMatch(instance -> instance.value().activation() instanceof PassiveActivation passive && passive.trigger() instanceof OnBlockBreak trigger && trigger.getCancelVariant().equals("silk"));
            boolean indestructible = abilities.stream().anyMatch(instance -> instance.value().activation() instanceof PassiveActivation passive && passive.trigger() instanceof OnBlockBreak trigger && trigger.getCancelVariant().equals("indestructible"));

            if (silky || indestructible) {
                if (silky) {
                    ItemStack blockStack = new ItemStack(blockState.getBlock().asItem(), 1);
                    ItemEntity blockStackEntity = new ItemEntity(level, blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockStack);
                    level.addFreshEntity(blockStackEntity);
                    level.destroyBlock(blockPos, false);
                }
                event.setCanceled(true);
            }
        }
    }

    @Override
    public boolean test(final LootContext context) {
        return this.condition.map(condition -> condition.test(context)).orElse(true);
    }

    public String getCancelVariant() {
        return this.cancel_variant.orElse("");
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
