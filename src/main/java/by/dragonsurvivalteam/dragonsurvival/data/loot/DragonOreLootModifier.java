package by.dragonsurvivalteam.dragonsurvival.data.loot;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.data.DataBlockTagProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
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
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class DragonOreLootModifier extends LootModifier {
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "require_experience_drop_for_dragon_ore", comment = "Require the ore to drop experience for it to drop dragon ore items")
    public static boolean requireExperienceDropForDragonOre = true;

    @ConfigRange(min = 0.0, max = 1.0)
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "humanOreDustChance", comment = "The odds of dust dropping when a human harvests an ore.")
    public static Double humanOreDustChance = 0.1;

    @ConfigRange(min = 0.0, max = 1.0)
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "dragonOreDustChance", comment = "The odds of dust dropping when a dragon harvests an ore.")
    public static Double dragonOreDustChance = 0.2;

    @ConfigRange(min = 0.0, max = 1.0)
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "humanOreBoneChance", comment = "The odds of a bone dropping when a human harvests an ore.")
    public static Double humanOreBoneChance = 0.0;

    @ConfigRange(min = 0.0, max = 1.0)
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "dragonOreBoneChance", comment = "The odds of a bone dropping when a dragon harvests an ore.")
    public static Double dragonOreBoneChance = 0.01;

    // No codec at the moment. This is just a formality.
    public static final Supplier<Codec<DragonOreLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, DragonOreLootModifier::new)));

    public DragonOreLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);

        if (!(entity instanceof Player) || origin == null || state == null || !state.is(DataBlockTagProvider.DRAGON_ORE_DROP)) {
            return generatedLoot;
        }

        ItemStack tool = context.getParamOrNull(LootContextParams.TOOL);
        int fortuneLevel = 0;

        if (tool != null) {
            if (tool.getEnchantmentLevel(Enchantments.SILK_TOUCH) > 0) {
                return generatedLoot;
            }

            fortuneLevel = tool.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE);
        }

        if (!requireExperienceDropForDragonOre || state.getExpDrop(context.getLevel(), context.getRandom(), new BlockPos(origin), fortuneLevel, 0) > 0) {
            int fortuneRoll = 1;

            if (fortuneLevel >= 1) {
                fortuneRoll = context.getRandom().nextInt(fortuneLevel) + 1;
            }

            if (DragonUtils.isDragon(entity)) {
                if (context.getRandom().nextDouble() < dragonOreDustChance) {
                    generatedLoot.add(new ItemStack(DSItems.elderDragonDust, fortuneRoll));
                }

                if (context.getRandom().nextDouble() < dragonOreBoneChance) {
                    generatedLoot.add(new ItemStack(DSItems.elderDragonBone, fortuneRoll));
                }
            } else {
                if (context.getRandom().nextDouble() < humanOreDustChance) {
                    generatedLoot.add(new ItemStack(DSItems.elderDragonDust, fortuneRoll));
                }

                if (context.getRandom().nextDouble() < humanOreBoneChance) {
                    generatedLoot.add(new ItemStack(DSItems.elderDragonBone, fortuneRoll));
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
