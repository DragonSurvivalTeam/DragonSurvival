package by.dragonsurvivalteam.dragonsurvival.common.dragon_types;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.NBTInterface;
import com.mojang.datafixers.util.Pair;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

public abstract class AbstractDragonType implements NBTInterface, Comparable<AbstractDragonType> {
	public int slotForBonus;
	public abstract String getTypeName();
	public abstract void onPlayerUpdate(Player player, DragonStateHandler handler);
	public abstract boolean isInManaCondition(Player player, DragonStateHandler cap);
	public abstract void onPlayerDeath();
	
	//Not implemented
	public abstract List<Pair<ItemStack, FoodData>> validFoods(Player player, DragonStateHandler handler);
	public abstract List<TagKey<Block>> mineableBlocks();
	@Override
	public int compareTo(@NotNull AbstractDragonType o){
		return getTypeName().compareTo(o.getTypeName());
	}

	@Override
	public boolean equals(Object obj){
		if(obj instanceof AbstractDragonType type){
			return Objects.equals(type.getSubtypeName(), getSubtypeName());
		}
		return super.equals(obj);
	}

	@Override
	public String toString(){
		return getTypeName();
	}

	public String getSubtypeName() {
		return getTypeName();
	}

	public String getTypeNameUpperCase() {
		return getTypeName().toUpperCase(Locale.ENGLISH);
	}

	public String getTypeNameLowerCase() {
		return getTypeName().toLowerCase(Locale.ENGLISH);
	}
}