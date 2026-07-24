package by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSEntityTypeTags;
import by.dragonsurvivalteam.dragonsurvival.util.EnchantmentUtils;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class DragonHeartLootModifier extends LootModifier {
    @ConfigRange(min = 0, max = 1)
    @Translation(key = "dragon_heart_shard_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of dragon heart shards dropping from entities with a maximum health between 14 and 20")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "dragon_heart_shard_chance")
    public static Double DRAGON_HEART_SHARD_CHANCE = 0.03;

    @ConfigRange(min = 0, max = 1)
    @Translation(key = "weak_dragon_heart_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of weak dragon hearts dropping from entities with a maximum health between 20 and 50")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "weak_dragon_heart_chance")
    public static Double WEAK_DRAGON_HEART_CHANCE = 0.01;

    @ConfigRange(min = 0, max = 1)
    @Translation(key = "elder_dragon_heart_chance", type = Translation.Type.CONFIGURATION, comments = "Determines the chance (in %) of elder dragon hearts dropping from entities with a maximum health above 50")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "elder_dragon_heart_chance")
    public static Double ELDER_DRAGON_HEART_CHANCE = 0.01;

    @Translation(key = "dragon_heart_white_list", type = Translation.Type.CONFIGURATION, comments = "Should the entity type tag 'drops_dragon_heart_shard' be treated as a whitelist instead of a blacklist?")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "dragon_heart_white_list")
    public static Boolean DRAGON_HEART_SHARD_WHITELIST = false;

    @Translation(key = "weak_dragon_heart_white_list", type = Translation.Type.CONFIGURATION, comments = "Should the entity type tag 'drops_weak_dragon_heart' be treated as a whitelist instead of a blacklist?")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "weak_dragon_heart_white_list")
    public static Boolean WEAK_DRAGON_HEART_WHITELIST = false;

    @Translation(key = "elder_dragon_heart_white_list", type = Translation.Type.CONFIGURATION, comments = "Should the entity type tag 'drops_elder_dragon_heart' be treated as a whitelist instead of a blacklist?")
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "elder_dragon_heart_white_list")
    public static Boolean ELDER_DRAGON_HEART_WHITELIST = false;

    public static final Supplier<MapCodec<DragonHeartLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, DragonHeartLootModifier::new)));

    public DragonHeartLootModifier(final LootItemCondition[] conditions) {
        super(conditions);
    }

    private static boolean canDropHeart(float health, float min, float max, final TagKey<EntityType<?>> tag, final Entity entity, boolean isWhiteList) {
        if (health < min || health > max) {
            return false;
        }

        return entity.getType().is(tag) == isWhiteList;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(@NotNull final ObjectArrayList<ItemStack> generatedLoot, final LootContext context) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);

        if (!(entity instanceof LivingEntity livingEntity) || /* Players don't drop the hearts */ entity instanceof Player) {
            return generatedLoot;
        }

        Player player = context.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);

        if (player == null) {
            // If it wasn't killed by a player, don't drop anything
            return generatedLoot;
        }

        if (!DragonStateProvider.isDragon(player)) {
            return generatedLoot;
        }

        float health = livingEntity.getMaxHealth();

        // TODO :: make these health values configurable or use some other way in general (predicates?)
        boolean canDropDragonHeartShard = canDropHeart(health, 14, 20, DSEntityTypeTags.DROPS_DRAGON_HEART_SHARD, entity, DRAGON_HEART_SHARD_WHITELIST);
        boolean canDropWeakDragonHeart = canDropHeart(health, 20, 50, DSEntityTypeTags.DROPS_WEAK_DRAGON_HEART, entity, WEAK_DRAGON_HEART_WHITELIST);
        boolean canDropElderDragonHeart = canDropHeart(health, 50, Float.MAX_VALUE, DSEntityTypeTags.DROPS_ELDER_DRAGON_HEART, entity, ELDER_DRAGON_HEART_WHITELIST);

        int lootingLevel = EnchantmentUtils.getLevel(player, Enchantments.LOOTING);

        // TODO :: why divide by 4?
        if (canDropDragonHeartShard && context.getRandom().nextInt(100) <= DRAGON_HEART_SHARD_CHANCE * 100 + lootingLevel * (DRAGON_HEART_SHARD_CHANCE * 100 / 4)) {
            generatedLoot.add(new ItemStack(DSItems.DRAGON_HEART_SHARD));
        }

        if (canDropWeakDragonHeart && context.getRandom().nextInt(100) <= WEAK_DRAGON_HEART_CHANCE * 100 + lootingLevel * (WEAK_DRAGON_HEART_CHANCE * 100 / 4)) {
            generatedLoot.add(new ItemStack(DSItems.WEAK_DRAGON_HEART));
        }

        if (canDropElderDragonHeart && context.getRandom().nextInt(100) <= ELDER_DRAGON_HEART_CHANCE * 100 + lootingLevel * (ELDER_DRAGON_HEART_CHANCE * 100 / 4)) {
            generatedLoot.add(new ItemStack(DSItems.ELDER_DRAGON_HEART));
        }

        return generatedLoot;
    }

    @Override
    public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
