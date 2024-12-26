package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncAddPenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncRemovePenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

import java.util.Optional;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record DragonPenalty(ResourceLocation icon, Optional<LootItemCondition> condition, PenaltyEffect effect, PenaltyTrigger trigger) {
    public static final Codec<DragonPenalty> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("icon").forGetter(DragonPenalty::icon),
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("condition").forGetter(DragonPenalty::condition),
            PenaltyEffect.CODEC.fieldOf("effect").forGetter(DragonPenalty::effect),
            PenaltyTrigger.CODEC.fieldOf("trigger").forGetter(DragonPenalty::trigger)
    ).apply(instance, DragonPenalty::new));

    public static final ResourceKey<Registry<DragonPenalty>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_penalties"));
    public static final Codec<Holder<DragonPenalty>> CODEC = RegistryFixedCodec.create(REGISTRY);

    public void apply(final ServerPlayer dragon) {
        PenaltySupply penaltySupply = dragon.getData(DSDataAttachments.PENALTY_SUPPLY);

        if (trigger instanceof SupplyTrigger supplyTrigger) {
            AttributeInstance resistance = dragon.getAttribute(supplyTrigger.attributeToUseAsBase());
            int resistanceTime = resistance != null ? (int) resistance.getValue() : 0;

            if (penaltySupply.getMaxSupply(supplyTrigger.id()) != resistanceTime) {
                penaltySupply.initialize(supplyTrigger.id(), resistanceTime, supplyTrigger.reductionRateMultiplier(), supplyTrigger.regenerationRate());
                PacketDistributor.sendToPlayer(dragon, new SyncAddPenaltySupply(supplyTrigger.id(), resistanceTime, supplyTrigger.reductionRateMultiplier(), supplyTrigger.regenerationRate()));
            }
        }

        if (trigger.matches(dragon, condition.map(condition -> condition.test(EntityPredicate.createContext(dragon, dragon))).orElse(true))) {
            effect.apply(dragon);
        }
    }

    public void remove(final ServerPlayer dragon) {
        PenaltySupply supply = dragon.getData(DSDataAttachments.PENALTY_SUPPLY);

        if (trigger instanceof SupplyTrigger supplyTrigger) {
            supply.remove(supplyTrigger.id());
            PacketDistributor.sendToPlayer(dragon, new SyncRemovePenaltySupply(supplyTrigger.id()));
        }
    }

    public MutableComponent getDescription(Player dragon) {
        return effect.getDescription().append(trigger.getDescription(dragon));
    }

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }
}
