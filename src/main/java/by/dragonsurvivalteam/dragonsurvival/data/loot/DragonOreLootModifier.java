package by.dragonsurvivalteam.dragonsurvival.data.loot;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class DragonOreLootModifier extends LootModifier {

    // No codec at the moment. This is just a formality.
    public static final Supplier<Codec<DragonOreLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, DragonOreLootModifier::new)));

    TagKey<Block> ores = BlockTags.create(new ResourceLocation("forge", "ores"));

    public DragonOreLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (context.hasParam(LootContextParams.BLOCK_STATE)) {
            BlockState blockState = context.getParamOrNull(LootContextParams.BLOCK_STATE);
            if (blockState != null) {
                if (blockState.is(ores)) {
                    Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
                    if (entity instanceof Player player) {
                        Vec3 breakPos = context.getParamOrNull(LootContextParams.ORIGIN);
                        if (breakPos != null) {
                            ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
                            int fortuneLevel = 0;
                            int silkTouchLevel = 0;
                            if (tool != null) {
                                fortuneLevel = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
                                silkTouchLevel = tool.getEnchantmentLevel(Enchantments.SILK_TOUCH);
                            }
                            BlockPos blockPos =  new BlockPos((int) breakPos.x, (int) breakPos.y, (int) breakPos.z);
                            int expDrop = blockState.getExpDrop(context.getLevel(), context.getRandom(), blockPos, fortuneLevel, silkTouchLevel);
                            if(expDrop > 0) {
                                DragonStateHandler handler = DragonUtils.getHandler(player);
                                int fortuneRoll = 1;
                                if (fortuneLevel >= 1)
                                    fortuneRoll = context.getRandom().nextInt(fortuneLevel) + 1;
                                if(handler.isDragon()) {
                                    if(context.getRandom().nextDouble() < ServerConfig.dragonOreDustChance){
                                        generatedLoot.add(new ItemStack(DSItems.elderDragonDust, fortuneRoll));
                                    }

                                    if(context.getRandom().nextDouble() < ServerConfig.dragonOreBoneChance){
                                        generatedLoot.add(new ItemStack(DSItems.elderDragonDust, fortuneRoll));
                                    }
                                } else {
                                    if(context.getRandom().nextDouble() < ServerConfig.humanOreDustChance){
                                        generatedLoot.add(new ItemStack(DSItems.elderDragonDust, fortuneRoll));
                                    }

                                    if(context.getRandom().nextDouble() < ServerConfig.humanOreBoneChance){
                                        generatedLoot.add(new ItemStack(DSItems.elderDragonDust, fortuneRoll));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
