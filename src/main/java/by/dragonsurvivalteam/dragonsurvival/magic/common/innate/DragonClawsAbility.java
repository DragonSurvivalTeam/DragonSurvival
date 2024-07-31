package by.dragonsurvivalteam.dragonsurvival.magic.common.innate;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.TierSortingRegistry;
import net.minecraftforge.fml.loading.FMLEnvironment;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Locale;

import static by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes.*;

public abstract class DragonClawsAbility extends InnateDragonAbility {
	@Override
	public int getMaxLevel() {
		return 1;
	}

	@Override
	public int getMinLevel() {
		return 1;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public ArrayList<Component> getInfo() {
		DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);

		ArrayList<Component> components = super.getInfo();
		components.add(Component.translatable("ds.skill.tool_type." + getName()));

		Pair<Tiers, Integer> harvestInfo = getHarvestInfo();

		if (harvestInfo != null) {
			components.add(Component.translatable("ds.skill.harvest_level", I18n.get("ds.skill.harvest_level." + harvestInfo.getFirst().name().toLowerCase(Locale.ENGLISH))));
		}

		double damageBonus = handler.isDragon() ? handler.getLevel() == DragonLevel.ADULT ? ServerConfig.adultBonusDamage : handler.getLevel() == DragonLevel.YOUNG ? ServerConfig.youngBonusDamage : ServerConfig.babyBonusDamage : 0;

		if (damageBonus > 0.0) {
			components.add(Component.translatable("ds.skill.claws.damage", "+" + damageBonus));
		}

		return components;
	}

	@Override
	public int getLevel() {
		Pair<Tiers, Integer> harvestInfo = getHarvestInfo();
		int textureId = harvestInfo != null ? harvestInfo.getSecond() : 0;

		return FMLEnvironment.dist == Dist.CLIENT ? textureId : 0;
	}

	@OnlyIn(Dist.CLIENT)
	public @Nullable Pair<Tiers, Integer> getHarvestInfo() {
		DragonStateHandler handler = DragonUtils.getHandler(Minecraft.getInstance().player);

		if (handler.getType() == null) {
			return null;
		}

		Item item = handler.getInnateFakeTool().getItem();

		if (!(item instanceof TieredItem tieredItem && tieredItem.getTier() instanceof Tiers tier)) {
			return Pair.of(Tiers.WOOD, 0);
		}

		int textureId = 0;

		if (Tiers.WOOD.equals(tier)) {
			textureId = 1;
		} else if(Tiers.STONE.equals(tier)) {
			textureId = 2;
		} else if(Tiers.IRON.equals(tier)) {
			textureId = 3;
		} else if(Tiers.GOLD.equals(tier)) {
			// FIXME :: If only innate is relevant then this can never be reached (same harvest level as wood)
			textureId = 4;
		} else if(Tiers.DIAMOND.equals(tier)) {
			textureId = 5;
		} else if(Tiers.NETHERITE.equals(tier)) {
			textureId = 6;
		}

		// TODO :: What about the texture for 7?

		return Pair.of(tier, textureId);
	}
}