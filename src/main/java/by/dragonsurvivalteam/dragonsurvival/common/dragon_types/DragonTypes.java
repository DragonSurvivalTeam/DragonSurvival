package by.dragonsurvivalteam.dragonsurvival.common.dragon_types;

import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.CaveDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.ForestDragonType;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.types.SeaDragonType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

public class DragonTypes {
	private static final HashMap<String, Supplier<AbstractDragonType>> classMappings = new HashMap<>();
	public static final HashMap<String, AbstractDragonType> staticTypes = new HashMap<>();
	public static final HashMap<String, AbstractDragonType> staticSubtypes = new HashMap<>();
	public static final HashMap<String, ArrayList<AbstractDragonType>> subtypeMap = new HashMap<>();

	public static CaveDragonType CAVE;
	public static SeaDragonType SEA;
	public static ForestDragonType FOREST;

	public static void registerTypes(){
		CAVE = registerType(CaveDragonType::new);
        SEA = registerType(SeaDragonType::new);
		FOREST = registerType(ForestDragonType::new);
	}

	public static <T extends AbstractDragonType> T registerType(Supplier<T> constructor){
		T type = constructor.get();
		String lcName = type.getTypeNameLowerCase();
		classMappings.put(lcName, (Supplier<AbstractDragonType>)constructor);
		staticTypes.put(lcName, type);
		staticSubtypes.put(lcName, type);
		subtypeMap.computeIfAbsent(lcName, s -> new ArrayList<AbstractDragonType>());
		subtypeMap.get(lcName).add(type);
		return type;
	}
	
	public static <T extends AbstractDragonType> T registerSubtype(Supplier<T> constructor) {
		T subtype = constructor.get();
		String lcSubName = subtype.getSubtypeName().toLowerCase(Locale.ENGLISH);
		String lcTypeName = subtype.getTypeNameLowerCase();
		classMappings.put(lcSubName, (Supplier<AbstractDragonType>) constructor);
		
		staticSubtypes.put(lcSubName, subtype);
		subtypeMap.computeIfAbsent(lcTypeName, s -> new ArrayList<>());
		subtypeMap.get(lcTypeName).add(subtype);
		return subtype;
	}

	public static AbstractDragonType getStatic(String name){
		return staticTypes.get(name.toLowerCase(Locale.ENGLISH));
	}
	
	public static AbstractDragonType getStaticSubtype(String name) {
		return staticSubtypes.get(name.toLowerCase(Locale.ENGLISH));
	}

	public static List<String> getTypes(){
		return staticTypes.keySet().stream().toList();
	}

	public static List<String> getAllSubtypes(){
		ArrayList<String> res = new ArrayList<String>();
		for (String type : getTypes()) {
			for (AbstractDragonType subtype : subtypeMap.get(type)) {
				res.add(subtype.getSubtypeName());
			}
		}
		return res;
	}

	public static AbstractDragonType getSupertype(AbstractDragonType type) {
		return getStatic(type.getTypeName());
	}
	
	public static List<AbstractDragonType> getSubtypesOfType(String name) {
		return subtypeMap.getOrDefault(staticTypes.getOrDefault(name, null).getTypeName(), new ArrayList<AbstractDragonType>());
	}

	public static AbstractDragonType newDragonTypeInstance(String name){
		return classMappings.containsKey(name.toLowerCase()) ? classMappings.get(name.toLowerCase()).get() : null;
	}
}