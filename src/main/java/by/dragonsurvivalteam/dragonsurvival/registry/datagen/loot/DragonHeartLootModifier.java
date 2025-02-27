package by.dragonsurvivalteam.dragonsurvival.registry.datagen.loot;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
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

        boolean canDropWeakDragonHeart = canDropHeart(health, 14, 20, DSEntityTypeTags.DROPS_WEAK_DRAGON_HEART, entity, ServerConfig.weakDragonHeartWhiteList);
        boolean canDropNormalDragonHeart = canDropHeart(health, 20, 50, DSEntityTypeTags.DROPS_NORMAL_DRAGON_HEART, entity, ServerConfig.dragonHeartWhiteList);
        boolean canDropElderDragonHeart = canDropHeart(health, 50, Float.MAX_VALUE, DSEntityTypeTags.DROPS_ELDER_DRAGON_HEART, entity, ServerConfig.elderDragonHeartWhiteList);

        int lootingLevel = EnchantmentUtils.getLevel(player, Enchantments.LOOTING);

        // TODO :: why divide by 4?
        if (canDropWeakDragonHeart && context.getRandom().nextInt(100) <= ServerConfig.weakDragonHeartChance * 100 + lootingLevel * (ServerConfig.weakDragonHeartChance * 100 / 4)) {
            generatedLoot.add(new ItemStack(DSItems.WEAK_DRAGON_HEART));
        }

        if (canDropNormalDragonHeart && context.getRandom().nextInt(100) <= ServerConfig.dragonHeartShardChance * 100 + lootingLevel * (ServerConfig.dragonHeartShardChance * 100 / 4)) {
            generatedLoot.add(new ItemStack(DSItems.DRAGON_HEART_SHARD));
        }

        if (canDropElderDragonHeart && context.getRandom().nextInt(100) <= ServerConfig.elderDragonHeartChance * 100 + lootingLevel * (ServerConfig.elderDragonHeartChance * 100 / 4)) {
            generatedLoot.add(new ItemStack(DSItems.ELDER_DRAGON_HEART));
        }

        return generatedLoot;
    }

    @Override
    public @NotNull MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
