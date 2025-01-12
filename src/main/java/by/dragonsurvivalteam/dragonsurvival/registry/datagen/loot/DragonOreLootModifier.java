package by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class DragonOreLootModifier extends LootModifier {
    public static final Supplier<MapCodec<DragonOreLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, DragonOreLootModifier::new)));

    public DragonOreLootModifier(final LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull final ObjectArrayList<ItemStack> generatedLoot, final LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);

        if (!(entity instanceof Player player) || origin == null || state == null || !state.is(Tags.Blocks.ORES)) {
            return generatedLoot;
        }

        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        int fortuneLevel = 0;

        if (tool != null) {
            if (EnchantmentUtils.getLevel(player.level(), Enchantments.SILK_TOUCH, tool) != 0) {
                return generatedLoot;
            }

            fortuneLevel = EnchantmentUtils.getLevel(player.level(), Enchantments.FORTUNE, tool);
        }

        BlockPos position = BlockPos.containing(origin);
        int experience = state.getExpDrop(context.getLevel(), position, null, null, ItemStack.EMPTY);

        if (experience > 0) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            int fortuneRoll = 1;

            if (fortuneLevel >= 1) {
                fortuneRoll = context.getRandom().nextInt(fortuneLevel) + 1;
            }

            if (handler.isDragon()) {
                if (context.getRandom().nextDouble() < ServerConfig.dragonOreDustChance) {
                    generatedLoot.add(new ItemStack(DSItems.ELDER_DRAGON_DUST, fortuneRoll));
                }

                if (context.getRandom().nextDouble() < ServerConfig.dragonOreBoneChance) {
                    generatedLoot.add(new ItemStack(DSItems.ELDER_DRAGON_BONE, fortuneRoll));
                }
            } else {
                if (context.getRandom().nextDouble() < ServerConfig.humanOreDustChance) {
                    generatedLoot.add(new ItemStack(DSItems.ELDER_DRAGON_DUST, fortuneRoll));
                }

                if (context.getRandom().nextDouble() < ServerConfig.humanOreBoneChance) {
                    generatedLoot.add(new ItemStack(DSItems.ELDER_DRAGON_BONE, fortuneRoll));
                }
            }
        }

        return generatedLoot;
    }

    @Override
    public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
