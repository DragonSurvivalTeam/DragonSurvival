package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ManaHandler;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncMana;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;

public record ManaRecoveryEffect(ActionType actionType, AdjustmentType adjustmentType, LevelBasedValue amount, LevelBasedValue probability) implements AbilityEntityEffect {
    @Translation(comments = "§6■ Adjust current mana§r by setting it to %s")
    public static final String ADJUST_SET = Translation.Type.GUI.wrap("mana_recovery.adjust_set");

    @Translation(comments = "§6■ Adjust current mana§r by adding %s to it")
    public static final String ADJUST_ADD = Translation.Type.GUI.wrap("mana_recovery.adjust_add");

    public static final MapCodec<ManaRecoveryEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ActionType.CODEC.fieldOf("action_type").forGetter(ManaRecoveryEffect::actionType),
            AdjustmentType.CODEC.fieldOf("adjustment_type").forGetter(ManaRecoveryEffect::adjustmentType),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(ManaRecoveryEffect::amount),
            LevelBasedValue.CODEC.optionalFieldOf("probability", LevelBasedValue.constant(1)).forGetter(ManaRecoveryEffect::probability)
    ).apply(instance, ManaRecoveryEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof Player player) || !DragonStateProvider.isDragon(player)) {
            return;
        }

        if (dragon.getRandom().nextDouble() > probability.calculate(ability.level())) {
            return;
        }

        float amount = this.amount.calculate(ability.level());

        MagicData magic = MagicData.getData(player);
        float max = ManaHandler.getMaxMana(player);

        float base = switch (actionType) {
            case ADD -> magic.getCurrentMana();
            case SET -> 0;
        };

        float adjustment = switch (adjustmentType) {
            case PERCENT -> amount * max;
            case FLAT -> amount;
        };

        magic.setCurrentMana(Math.min(max, base + adjustment));
        // Usually there is no need to sync since mana related logic occurs on both sides
        PacketDistributor.sendToPlayer(dragon, new SyncMana(magic.getCurrentMana()));
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        String value = switch (adjustmentType) {
            case PERCENT -> NumberFormat.getPercentInstance().format(amount.calculate(ability.level()));
            case FLAT -> String.valueOf(amount.calculate(ability.level()));
        };

        MutableComponent component = switch (actionType) {
            case SET -> Component.translatable(ADJUST_SET, DSColors.dynamicValue(value));
            case ADD -> Component.translatable(ADJUST_ADD, DSColors.dynamicValue(value));
        };

        float probability = this.probability.calculate(ability.level());

        if (probability < 1) {
            component.append(Component.translatable(LangKey.ABILITY_EFFECT_CHANCE, DSColors.dynamicValue(NumberFormat.getPercentInstance().format(probability))));
        }

        return List.of(component);
    }

    public enum ActionType implements StringRepresentable {
        SET("set"), ADD("add");

        public static final Codec<ActionType> CODEC = StringRepresentable.fromEnum(ActionType::values);
        private final String name;

        ActionType(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    public enum AdjustmentType implements StringRepresentable {
        PERCENT("percent"), FLAT("flat");

        public static final Codec<AdjustmentType> CODEC = StringRepresentable.fromEnum(AdjustmentType::values);
        private final String name;

        AdjustmentType(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
