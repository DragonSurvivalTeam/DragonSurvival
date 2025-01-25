package by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSBlockTags;
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
    @Translation(key = "require_experience_drop_for_dragon_ore", type = Translation.Type.CONFIGURATION, comments = "Require the ore to drop experience for it to drop dragon ore items")
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "require_experience_drop_for_dragon_ore")
    public static boolean requireExperienceDropForDragonOre = true;

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "human_ore_dust_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of dust dropping when a human harvests an ore block")
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "human_ore_dust_chance")
    public static Double humanOreDustChance = 0.1;

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "dragon_ore_dust_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of dust dropping when a dragon harvests an ore block")
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "dragon_ore_dust_chance")
    public static Double dragonOreDustChance = 0.2;

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "human_ore_bone_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of bones dropping when a human harvests an ore block")
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "human_ore_bone_chance")
    public static Double humanOreBoneChance = 0.0;

    @ConfigRange(min = 0.0, max = 1.0)
    @Translation(key = "dragon_ore_bone_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of bones dropping when a dragon harvests an ore block")
    @ConfigOption(side = ConfigSide.SERVER, category = {"drops", "ore"}, key = "dragon_ore_bone_chance")
    public static Double dragonOreBoneChance = 0.01;

    public static final Supplier<MapCodec<DragonOreLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, DragonOreLootModifier::new)));

    public DragonOreLootModifier(final LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull final ObjectArrayList<ItemStack> generatedLoot, final LootContext context) {
        BlockState state = context.getParamOrNull(LootContextParams.BLOCK_STATE);
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        Vec3 origin = context.getParamOrNull(LootContextParams.ORIGIN);

        if (!(entity instanceof Player player) || origin == null || state == null || !state.is(DSBlockTags.DRAGON_ORE_DROP)) {
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

        if (experience > 0 || !requireExperienceDropForDragonOre) {
            DragonStateHandler handler = DragonStateProvider.getData(player);
            int fortuneRoll = 1;

            if (fortuneLevel >= 1) {
                fortuneRoll = context.getRandom().nextInt(fortuneLevel) + 1;
            }

            if (handler.isDragon()) {
                if (context.getRandom().nextDouble() < dragonOreDustChance) {
                    generatedLoot.add(new ItemStack(DSItems.ELDER_DRAGON_DUST, fortuneRoll));
                }

                if (context.getRandom().nextDouble() < dragonOreBoneChance) {
                    generatedLoot.add(new ItemStack(DSItems.ELDER_DRAGON_BONE, fortuneRoll));
                }
            } else {
                if (context.getRandom().nextDouble() < humanOreDustChance) {
                    generatedLoot.add(new ItemStack(DSItems.ELDER_DRAGON_DUST, fortuneRoll));
                }

                if (context.getRandom().nextDouble() < humanOreBoneChance) {
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
