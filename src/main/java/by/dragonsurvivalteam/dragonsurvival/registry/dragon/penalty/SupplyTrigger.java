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
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;

import java.util.List;

public record SupplyTrigger(String id, Holder<Attribute> attributeToUseAsBase, int triggerRate, float reductionRateMultiplier, float regenerationRate, List<RecoveryItems> recoveryItems, boolean displayLikeHungerBar) implements PenaltyTrigger {
    @Translation(comments = " after %s seconds")
    private static final String PENALTY_SUPPLY_TRIGGER = Translation.Type.GUI.wrap("penalty.supply_trigger");

    @Translation(comments = " on every game tick")
    private static final String PENALTY_SUPPLY_TRIGGER_CONSTANT = Translation.Type.GUI.wrap("penalty.supply_trigger.constant");

    public static final MapCodec<SupplyTrigger> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(SupplyTrigger::id),
            Attribute.CODEC.optionalFieldOf("attribute", DSAttributes.PENALTY_RESISTANCE_TIME).forGetter(SupplyTrigger::attributeToUseAsBase),
            Codec.INT.fieldOf("trigger_rate").forGetter(SupplyTrigger::triggerRate),
            Codec.FLOAT.fieldOf("reduction_rate").forGetter(SupplyTrigger::reductionRateMultiplier),
            Codec.FLOAT.fieldOf("regeneration_rate").forGetter(SupplyTrigger::regenerationRate),
            RecoveryItems.CODEC.codec().listOf().fieldOf("recovery_items").forGetter(SupplyTrigger::recoveryItems),
            Codec.BOOL.optionalFieldOf("display_like_hunger_bar", false).forGetter(SupplyTrigger::displayLikeHungerBar)
    ).apply(instance, SupplyTrigger::new));

    public record RecoveryItems(HolderSet<Item> items, HolderSet<Potion> potions, float percentRestored) {
        public static final MapCodec<RecoveryItems> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(RecoveryItems::items),
                RegistryCodecs.homogeneousList(Registries.POTION).fieldOf("potions").forGetter(RecoveryItems::potions),
                Codec.FLOAT.fieldOf("percent_restored").forGetter(RecoveryItems::percentRestored)
        ).apply(instance, RecoveryItems::new));
    }

    public boolean matches(final ServerPlayer dragon, boolean conditionMatched) {
        PenaltySupply penaltySupply = dragon.getData(DSDataAttachments.PENALTY_SUPPLY);

        if (conditionMatched) {
            penaltySupply.reduce(dragon, id);
        } else {
            penaltySupply.regenerate(dragon, id);
            return false;
        }

        if (dragon.level().getGameTime() % triggerRate() == 0) {
            return !penaltySupply.hasSupply(id);
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