package by.dragonsurvivalteam.dragonsurvival.util;


import com.google.gson.annotations.SerializedName;
import net.minecraft.client.resources.language.I18n;

import java.util.Locale;

public enum DragonLevel{
	@SerializedName(value = "NEWBORN", alternate = "BABY")
	NEWBORN(14, 20, 1.1f, "newborn"),
	YOUNG(20, 30, 1.6f, "young"),
	ADULT(30, 40, 2.1f, "adult");

	public final int size, maxSize;
	public final float jumpHeight;
	public final String name;

	DragonLevel(int size, int maxSize, float jumpHeight, String name_){
		this.size = size;
		this.maxSize = maxSize;
		this.jumpHeight = jumpHeight;
		name = name_;
	}

	public String getName(){
		return I18n.get("ds.level." + name);
	}

	public String getNameLowerCase() {
		return getName().toLowerCase(Locale.ENGLISH);
	}
}