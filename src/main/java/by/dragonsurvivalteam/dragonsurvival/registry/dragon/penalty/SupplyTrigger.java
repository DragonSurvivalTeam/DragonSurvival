package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Optional;

public record SupplyTrigger(
        ResourceLocation supplyType,
        Holder<Attribute> attributeToUseAsBase,
        int triggerRate,
        float reductionRate,
        float regenerationRate,
        List<RecoveryItem> recoveryItems,
        boolean displayLikeHungerBar,
        Optional<ParticleOptions> particlesOnTrigger
) implements PenaltyTrigger {
    @Translation(comments = " every %s seconds")
    private static final String PENALTY_SUPPLY_TRIGGER = Translation.Type.GUI.wrap("penalty.supply_trigger");

    @Translation(comments = " on every game tick")
    private static final String PENALTY_SUPPLY_TRIGGER_CONSTANT = Translation.Type.GUI.wrap("penalty.supply_trigger.constant");

    public static final MapCodec<SupplyTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("supply_type").forGetter(SupplyTrigger::supplyType),
            Attribute.CODEC.optionalFieldOf("attribute", DSAttributes.PENALTY_RESISTANCE_TIME).forGetter(SupplyTrigger::attributeToUseAsBase),
            ExtraCodecs.intRange(1, Integer.MAX_VALUE).fieldOf("trigger_rate").forGetter(SupplyTrigger::triggerRate),
            Codec.FLOAT.fieldOf("reduction_rate").forGetter(SupplyTrigger::reductionRate),
            Codec.FLOAT.fieldOf("regeneration_rate").forGetter(SupplyTrigger::regenerationRate),
            RecoveryItem.CODEC.codec().listOf().optionalFieldOf("recovery_items", List.of()).forGetter(SupplyTrigger::recoveryItems),
            Codec.BOOL.optionalFieldOf("display_like_hunger_bar", false).forGetter(SupplyTrigger::displayLikeHungerBar),
            ParticleTypes.CODEC.optionalFieldOf("particles_on_trigger").forGetter(SupplyTrigger::particlesOnTrigger)
    ).apply(instance, SupplyTrigger::new));

    public record RecoveryItem(List<ItemPredicate> itemPredicates, float percentRestored) {
        public static final MapCodec<RecoveryItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemPredicate.CODEC.listOf().fieldOf("item_predicates").forGetter(RecoveryItem::itemPredicates),
                Codec.FLOAT.fieldOf("percent_restored").forGetter(RecoveryItem::percentRestored)
        ).apply(instance, RecoveryItem::new));
    }

    public boolean matches(final ServerPlayer dragon, boolean conditionMatched) {
        PenaltySupply supply = dragon.getData(DSDataAttachments.PENALTY_SUPPLY);
        supply.tick(supplyType);

        if (conditionMatched) {
            supply.reduce(dragon, supplyType);
        } else {
            supply.regenerate(dragon, supplyType);
            return false;
        }

        if (supply.getCurrentTick(supplyType) >= triggerRate) {
            particlesOnTrigger.ifPresent(particle -> {
                for (int i = 0; i < 2; i++) {
                    ((ServerLevel) dragon.level()).sendParticles(particle, dragon.getX() + (dragon.getRandom().nextDouble() - 0.5D) * 0.5D, dragon.getEyeY() + (dragon.getRandom().nextDouble() - 0.5D) * 0.5D, dragon.getZ() + (dragon.getRandom().nextDouble() - 0.5D) * 0.5D, 1, 0, -dragon.getRandom().nextDouble() * 0.25D, 0, 0.025F);
                }
            });

            supply.resetTick(supplyType);
            return !supply.hasSupply(supplyType);
        } else {
            return false;
        }
    }

    @Override
    public MutableComponent getDescription(final Player dragon) {
        AttributeInstance attribute = dragon.getAttribute(attributeToUseAsBase);

        if (attribute == null) {
            return Component.empty();
        } else {
            double seconds = Functions.ticksToSeconds((int) attribute.getValue());

            if (seconds == 0) {
                return Component.translatable(PENALTY_SUPPLY_TRIGGER_CONSTANT);
            } else {
                return Component.translatable(PENALTY_SUPPLY_TRIGGER, DSColors.dynamicValue(String.format("%.1f", seconds)));
            }
        }
    }

    @Override
    public MapCodec<? extends PenaltyTrigger> codec() {
        return CODEC;
    }
}