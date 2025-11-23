package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncCooldown;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public record CooldownRecoveryEffect(
        Optional<HolderSet<DragonAbility>> abilities,
        ActionType actionType,
        AdjustmentType adjustmentType,
        LevelBasedValue amount,
        LevelBasedValue probability,
        boolean excludeThis
) implements AbilityEntityEffect {
    @Translation(comments = "Adjusts the cooldown of %s, setting it to %s")
    public static final String ADJUST_SET = Translation.Type.GUI.wrap("cooldown.adjust_set");

    @Translation(comments = "Adjusts the cooldown of %s, reducing it by %s")
    public static final String ADJUST_REDUCE = Translation.Type.GUI.wrap("cooldown.adjust_reduce");

    @Translation(comments = "every ability")
    public static final String EVERY_ABILITY = Translation.Type.GUI.wrap("cooldown.every_ability");

    public static final MapCodec<CooldownRecoveryEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(DragonAbility.REGISTRY).optionalFieldOf("abilities").forGetter(CooldownRecoveryEffect::abilities),
            ActionType.CODEC.fieldOf("action_type").forGetter(CooldownRecoveryEffect::actionType),
            AdjustmentType.CODEC.fieldOf("adjustment_type").forGetter(CooldownRecoveryEffect::adjustmentType),
            LevelBasedValue.CODEC.fieldOf("amount").forGetter(CooldownRecoveryEffect::amount),
            LevelBasedValue.CODEC.optionalFieldOf("probability", LevelBasedValue.constant(1)).forGetter(CooldownRecoveryEffect::probability),
            Codec.BOOL.optionalFieldOf("exclude_this", true).forGetter(CooldownRecoveryEffect::excludeThis)
    ).apply(instance, CooldownRecoveryEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof ServerPlayer player) || !DragonStateProvider.isDragon(player)) {
            return;
        }

        if (dragon.getRandom().nextDouble() > probability.calculate(ability.level())) {
            return;
        }

        float rawAmount = this.amount.calculate(ability.level());
        MagicData magic = MagicData.getData(player);

        // If no abilities are specified, we adjust the cooldown of every known ability the target has
        Set<ResourceKey<DragonAbility>> keys = this.abilities
                .map(abilities -> abilities.stream().map(Holder::getKey).collect(Collectors.toSet()))
                .orElse(magic.getAbilities().keySet());

        for (ResourceKey<DragonAbility> key : keys) {
            if (excludeThis && key == ability.key()) {
                continue;
            }

            DragonAbilityInstance instance = magic.getAbility(key);

            if (instance != null) {
                int cooldown = instance.value().activation().getCooldown(instance.level());

                if (cooldown == 0) {
                    continue;
                }

                int adjustment = switch (adjustmentType) {
                    case PERCENT -> (int) (rawAmount * cooldown);
                    case FLAT -> (int) rawAmount;
                };

                int previous = instance.cooldown();

                switch (actionType) {
                    case REDUCE -> instance.setCooldown(previous - adjustment);
                    case SET -> instance.setCooldown(adjustment);
                }

                if (instance.cooldown() != previous) {
                    PacketDistributor.sendToPlayer(player, new SyncCooldown(instance.key(), instance.cooldown()));
                }
            }
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        MutableComponent target = abilities.map(abilities -> Functions.translateHolderSet(abilities, Translation.Type.ABILITY))
                .orElse(Component.translatable(EVERY_ABILITY));

        String value = switch (adjustmentType) {
            case PERCENT -> NumberFormat.getPercentInstance().format(amount.calculate(ability.level()));
            case FLAT -> String.valueOf((int) amount.calculate(ability.level()));
        };

        String translationKey = switch (actionType) {
            case SET -> ADJUST_SET;
            case REDUCE -> ADJUST_REDUCE;
        };

        return List.of(Component.translatable(translationKey, DSColors.dynamicValue(target), DSColors.dynamicValue(value)));
    }

    public enum ActionType implements StringRepresentable {
        SET("set"), REDUCE("reduce");

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
