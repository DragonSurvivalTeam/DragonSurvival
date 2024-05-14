package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects;

import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.EnumSkinLayer;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.NBTInterface;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.Lazy;

import java.util.HashMap;

public class SkinPreset implements NBTInterface{
	public HashMap<DragonLevel, Lazy<SkinAgeGroup>> skinAges = new HashMap<>();

	public SkinPreset(){
		for(DragonLevel level : DragonLevel.values()){
			skinAges.computeIfAbsent(level, (_level)->Lazy.of(()->new SkinAgeGroup(_level)));
		}
	}

	public void initDefaults(DragonStateHandler handler){
		initDefaults(handler.getType());
	}

	public void initDefaults(AbstractDragonType type){
		for(DragonLevel level : DragonLevel.values()){
			skinAges.put(level, Lazy.of(()->new SkinAgeGroup(level, type)));
		}
	}

	@Override
	public CompoundTag writeNBT(){
		CompoundTag nbt = new CompoundTag();

		for(DragonLevel level : DragonLevel.values()){
			nbt.put(level.name, skinAges.getOrDefault(level, Lazy.of(()->new SkinAgeGroup(level))).get().writeNBT());
		}

		return nbt;
	}

	@Override
	public void readNBT(CompoundTag base){
		for(DragonLevel level : DragonLevel.values()){
			skinAges.put(level,
					Lazy.of(()->{
						SkinAgeGroup ageGroup = new SkinAgeGroup(level);
						CompoundTag nbt = base.getCompound(level.name);
						ageGroup.readNBT(nbt);
						return ageGroup;
					})
			);
		}
	}

	public static class SkinAgeGroup implements NBTInterface{
		public DragonLevel level;
		public HashMap<EnumSkinLayer, Lazy<LayerSettings>> layerSettings = new HashMap<>();

		public boolean wings = true;
		public boolean defaultSkin = false;

		public SkinAgeGroup(DragonLevel level, AbstractDragonType type){
			this(level);
			for(EnumSkinLayer layer : EnumSkinLayer.values()){
				layerSettings.put(layer, Lazy.of(()->new LayerSettings(DragonEditorRegistry.getDefaultPart(type, level, layer))));
			}
		}

		public SkinAgeGroup(DragonLevel level){
			this.level = level;

			for(EnumSkinLayer layer : EnumSkinLayer.values()){
				layerSettings.computeIfAbsent(layer, s -> Lazy.of(LayerSettings::new));
			}
		}

		@Override
		public CompoundTag writeNBT(){
			CompoundTag nbt = new CompoundTag();

			nbt.putBoolean("wings", wings);
			nbt.putBoolean("defaultSkin", defaultSkin);

			for(EnumSkinLayer layer : EnumSkinLayer.values()){
				nbt.put(layer.name(), layerSettings.getOrDefault(layer, Lazy.of(LayerSettings::new)).get().writeNBT());
			}

			return nbt;
		}

		@Override
		public void readNBT(CompoundTag base){
			wings = base.getBoolean("wings");
			defaultSkin = base.getBoolean("defaultSkin");

			for(EnumSkinLayer layer : EnumSkinLayer.values()){
				layerSettings.put(layer, Lazy.of(()->{
					LayerSettings ageGroup = new LayerSettings();
					CompoundTag nbt = base.getCompound(layer.name());
					ageGroup.readNBT(nbt);
					return ageGroup;
				}));
			}
		}
	}
}