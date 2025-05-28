package by.dragonsurvivalteam.dragonsurvival.data.loot;

import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.data.DSEntityTypeTags;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class DragonHeartLootModifier extends LootModifier {
    @ConfigRange(min = 0.0, max = 1.0)
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "dragonHeartShardChance", comment = "Determines the chance (in %) of dragon heart shards dropping from entities with a maximum health between 14 and 20")
    public static Double DRAGON_HEART_SHARD_CHANCE = 0.03;

    @ConfigRange(min = 0.0, max = 1.0)
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "weakDragonHeartChance", comment = "Determines the chance (in %) of weak dragon hearts dropping from entities with a maximum health between 20 and 50")
    public static Double WEAK_DRAGON_HEART_CHANCE = 0.01;

    @ConfigRange(min = 0.0, max = 1.0)
    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "elderDragonHeartChance", comment = "Determines the chance (in %) of elder dragon hearts dropping from entities with a maximum health above 50")
    public static Double ELDER_DRAGON_HEART_CHANCE = 0.01;

    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "is_dragon_heart_shard_whitelist", comment = "Should the entity type tag 'drops_dragon_heart_shard' be treated as a whitelist instead of a blacklist?")
    public static Boolean DRAGON_HEART_SHARD_WHITELIST = false;

    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "is_weak_dragon_heart_whitelist", comment = "Should the entity type tag 'drops_weak_dragon_heart' be treated as a whitelist instead of a blacklist?")
    public static Boolean WEAK_DRAGON_HEART_WHITELIST = false;

    @ConfigOption(side = ConfigSide.SERVER, category = "drops", key = "is_elder_Dragon_heart_whitelist", comment = "Should the entity type tag 'drops_elder_dragon_heart' be treated as a whitelist instead of a blacklist?")
    public static Boolean ELDER_DRAGON_HEART_WHITELIST = false;

    // No codec at the moment. This is just a formality.
    public static final Supplier<Codec<DragonHeartLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, DragonHeartLootModifier::new)));

    public DragonHeartLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
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

        if (!DragonUtils.isDragon(player)) {
            return generatedLoot;
        }

        float health = livingEntity.getMaxHealth();

        boolean canDropDragonHeartShard = canDropHeart(health, 14, 20, DSEntityTypeTags.DROPS_DRAGON_HEART_SHARD, entity, DRAGON_HEART_SHARD_WHITELIST);
        boolean canDropWeakDragonHeart = canDropHeart(health, 20, 50, DSEntityTypeTags.DROPS_WEAK_DRAGON_HEART, entity, WEAK_DRAGON_HEART_WHITELIST);
        boolean canDropElderDragonHeart = canDropHeart(health, 50, Float.MAX_VALUE, DSEntityTypeTags.DROPS_ELDER_DRAGON_HEART, entity, ELDER_DRAGON_HEART_WHITELIST);

        int lootingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.MOB_LOOTING, player);

        if (canDropDragonHeartShard && context.getRandom().nextInt(100) <= DRAGON_HEART_SHARD_CHANCE * 100 + lootingLevel * (DRAGON_HEART_SHARD_CHANCE * 100 / 4)) {
            generatedLoot.add(new ItemStack(DSItems.dragonHeartShard));
        }

        if (canDropWeakDragonHeart && context.getRandom().nextInt(100) <= WEAK_DRAGON_HEART_CHANCE * 100 + lootingLevel * (WEAK_DRAGON_HEART_CHANCE * 100 / 4)) {
            generatedLoot.add(new ItemStack(DSItems.weakDragonHeart));
        }

        if (canDropElderDragonHeart && context.getRandom().nextInt(100) <= ELDER_DRAGON_HEART_CHANCE * 100 + lootingLevel * (ELDER_DRAGON_HEART_CHANCE * 100 / 4)) {
            generatedLoot.add(new ItemStack(DSItems.elderDragonHeart));
        }

        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
