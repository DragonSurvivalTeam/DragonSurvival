package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.DragonFoodHandler;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ManaHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/** Handle food which can replenish mana */
public class DragonTreatItem extends Item {
	public AbstractDragonType type;

	public DragonTreatItem(final AbstractDragonType type, final Properties foodProperties) {
		super(foodProperties.food(new FoodProperties.Builder().nutrition(1).alwaysEat().saturationMod(0.4F).meat().effect(() -> new MobEffectInstance(MobEffects.HUNGER, 20 * 15, 0), 1.0F).build()));
		this.type = type;
	}

	@Override
	public @NotNull ItemStack finishUsingItem(@NotNull final ItemStack stack, @NotNull final Level level, @NotNull final LivingEntity livingEntity) {
		boolean isEdible;

		if (livingEntity instanceof Player player) {
			AbstractDragonType playerType = DragonUtils.getDragonType(player);

			if (DragonUtils.isDragonType(playerType, type)) {
				ManaHandler.replenishMana(player, ManaHandler.getMaxMana(player));
				player.addEffect(new MobEffectInstance(DragonEffects.SOURCE_OF_MAGIC, Functions.minutesToTicks(1)));
			}

			isEdible = DragonFoodHandler.isEdible(stack, playerType);
		} else {
			isEdible = stack.isEdible();
		}

		if (isEdible) {
			return livingEntity.eat(level, stack);
		}

		return stack;
	}

	@Override
	public void appendHoverText(@NotNull final ItemStack itemStack, @Nullable final Level level, @NotNull final List<Component> components, @NotNull final TooltipFlag tooltipFlag) {
		super.appendHoverText(itemStack, level, components, tooltipFlag);
		components.add(Component.translatable("ds.description." + type.getTypeName().toLowerCase() + "DragonTreat"));
	}
}