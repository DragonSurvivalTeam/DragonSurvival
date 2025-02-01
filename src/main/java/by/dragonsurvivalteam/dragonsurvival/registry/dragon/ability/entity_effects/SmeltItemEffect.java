package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.LevelBasedValue;

import java.util.Optional;

public record SmeltItemEffect(Optional<ItemPredicate> itemPredicate, LevelBasedValue probability, boolean dropsExperience) implements AbilityEntityEffect {
    public static final MapCodec<SmeltItemEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemPredicate.CODEC.optionalFieldOf("item_predicate").forGetter(SmeltItemEffect::itemPredicate),
            LevelBasedValue.CODEC.optionalFieldOf("probability", LevelBasedValue.constant(1)).forGetter(SmeltItemEffect::probability),
            Codec.BOOL.optionalFieldOf("drops_experience", true).forGetter(SmeltItemEffect::dropsExperience)
    ).apply(instance, SmeltItemEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof ItemEntity itemEntity)) {
            return;
        }

        if (probability.calculate(ability.level()) < dragon.getRandom().nextDouble()) {
            return;
        }

        ItemStack stack = itemEntity.getItem();

        if (itemPredicate.map(predicate -> predicate.test(stack)).orElse(true)) {
            dragon.level().getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), dragon.level())
                    .ifPresent(recipe -> {
                        ItemStack result = recipe.value().getResultItem(dragon.registryAccess());

                        if (result.isEmpty()) {
                            return;
                        }

                        // Result is amount of smelting 1 item - therefor calculate the actual resulting amount
                        itemEntity.setItem(result.copyWithCount(stack.getCount() * result.getCount()));

                        if (!dropsExperience) {
                            return;
                        }

                        float experience = recipe.value().getExperience() * stack.getCount();

                        if (experience > 0) {
                            dragon.giveExperiencePoints((int) experience);
                        }
                    });
        }
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
