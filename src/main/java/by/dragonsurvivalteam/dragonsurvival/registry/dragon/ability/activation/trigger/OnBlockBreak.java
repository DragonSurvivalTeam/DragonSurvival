package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.trigger;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.PassiveActivation;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public record OnBlockBreak(Optional<LootItemCondition> condition, Optional<CancelVariants> cancel_variant) implements ActivationTrigger {
    public static final MapCodec<OnBlockBreak> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            LootItemCondition.DIRECT_CODEC.optionalFieldOf("condition").forGetter(OnBlockBreak::condition),
            CancelVariants.CODEC.optionalFieldOf("cancel_variant").forGetter(OnBlockBreak::cancel_variant)
    ).apply(instance, OnBlockBreak::new));

    private static final ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE, 1);

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

            boolean silky = false;
            boolean indestructible = false;

            for (DragonAbilityInstance ability : abilities) {
                ability.tick(player);
                CancelVariants cancelVariant = ((OnBlockBreak) ((PassiveActivation) ability.value().activation()).trigger()).getCancelVariant();
                silky |= cancelVariant == CancelVariants.SILK;
                indestructible |= cancelVariant == CancelVariants.INDESTRUCTIBLE;
            }

            if (silky || indestructible) {
                if (silky) {
                    initializePickaxe();
                    BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(blockPos) : null;
                    Block.dropResources(blockState, level, blockPos, blockEntity, player, pickaxe);
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

    private static void initializePickaxe() {
        Holder<Enchantment> silk = EnchantmentUtils.getHolder(Enchantments.SILK_TOUCH);
        if (pickaxe.getEnchantmentLevel(silk) != 1) {
            pickaxe.enchant(silk, 1);
        }
    }

    public CancelVariants getCancelVariant() {
        return this.cancel_variant.orElse(CancelVariants.NONE);
    }

    @Override
    public TriggerType type() {
        return TriggerType.ON_BLOCK_BREAK;
    }

    @Override
    public MapCodec<? extends ActivationTrigger> codec() {
        return CODEC;
    }

    public enum CancelVariants implements StringRepresentable {
        SILK("silk"), INDESTRUCTIBLE("indestructible"), NONE("none");

        public static final Codec<CancelVariants> CODEC = StringRepresentable.fromEnum(CancelVariants::values);
        private final String name;

        CancelVariants(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
}
