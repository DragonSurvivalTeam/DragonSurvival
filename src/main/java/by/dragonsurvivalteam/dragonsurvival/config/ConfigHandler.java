package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.google.gson.reflect.TypeToken;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.common.ForgeConfigSpec.LongValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation.EnumHolder;
import net.minecraftforge.forgespi.language.ModFileScanData;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Supplier;

@EventBusSubscriber( modid = DragonSurvivalMod.MODID, bus = Bus.MOD )
public class ConfigHandler{

	public static ClientConfig CLIENT;
	public static ForgeConfigSpec clientSpec;
	public static ServerConfig SERVER;
	public static ForgeConfigSpec serverSpec;

	public static HashMap<String, Object> defaultConfigValues = new HashMap<>();
	public static HashMap<String, ConfigOption> configObjects = new HashMap<>();
	public static HashMap<String, Field> configFields = new HashMap<>();
	public static HashMap<Dist, List<String>> configs = new HashMap<>();
	public static HashMap<String, ForgeConfigSpec.ConfigValue> configValues = new HashMap<>();

	private static List<Field> getFields() {
		List<Field> instances = new ArrayList<>();

		Type annotationType = Type.getType(ConfigOption.class);
		ModList.get().getAllScanData().forEach(s -> {
			List<ModFileScanData.AnnotationData> ebsTargets = s.getAnnotations().stream().filter(s1 -> s1.targetType() == ElementType.FIELD).filter(annotationData -> annotationType.equals(annotationData.annotationType())).toList();

			ebsTargets.forEach(ad ->  {
				EnumHolder sidesValue = (EnumHolder)ad.annotationData().get("side");
				Dist side = Dist.valueOf(sidesValue.getValue());

				if(side == FMLEnvironment.dist){
					try{
						Class<?> c = Class.forName(ad.clazz().getClassName());
						Field fe = c.getDeclaredField(ad.memberName());
						instances.add(fe);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			});
		});
		return instances;
	}

	public static void initConfig(){
		List<Field> set = getFields();

		set.forEach(s -> {
			if(!Modifier.isStatic(s.getModifiers()))
				return;

			ConfigOption option = s.getAnnotation(ConfigOption.class);

			if(option.side() == Dist.CLIENT && FMLEnvironment.dist != Dist.CLIENT) return;

			try{
				defaultConfigValues.put(option.key(), s.get(null));
			}catch(IllegalAccessException e){
				e.printStackTrace();
			}
			configFields.put(option.key(), s);
			configObjects.put(option.key(), option);

			configs.computeIfAbsent(option.side(), c -> new ArrayList<>());
			configs.get(option.side()).add(option.key());
		});

		Pair<ClientConfig, ForgeConfigSpec> client = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
		CLIENT = client.getLeft();
		clientSpec = client.getRight();

		Pair<ServerConfig, ForgeConfigSpec> server = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
		SERVER = server.getLeft();
		serverSpec = server.getRight();

		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientSpec);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverSpec);

	}

	public static void addConfigs(ForgeConfigSpec.Builder builder, Dist side){
		for(String key : configs.getOrDefault(side, Collections.emptyList())){
			ConfigOption option = configObjects.get(key);
			Field fe = configFields.get(key);

			for(String string : option.category()){
				builder.push(string);
			}

			if(option.comment() != null && option.comment().length > 0){
				builder.comment(option.comment());
			}

			if(!option.localization().isBlank()){
				builder.translation(option.localization());
			}

			try{
				Object ob = convertToString(fe.get(null));
				if(fe.isAnnotationPresent(ConfigRange.class)){
					ConfigRange range = fe.getAnnotation(ConfigRange.class);
					if(fe.getType().equals(int.class) || fe.getType().equals(Integer.class)){
						IntValue value = builder.defineInRange(option.key(), ((Number)ob).intValue(), (int)range.min(), (int)range.max());
						configValues.put(key, value);
					}else if(fe.getType().equals(float.class) || fe.getType().equals(Float.class)){
						DoubleValue value = builder.defineInRange(option.key(), ((Number)ob).floatValue(), range.min(), range.max());
						configValues.put(key, value);
					}else if(fe.getType().equals(long.class) || fe.getType().equals(Long.class)){
						LongValue value = builder.defineInRange(option.key(), ((Number)ob).longValue(), (long)range.min(), (long)range.max());
						configValues.put(key, value);
					}else if(fe.getType().equals(double.class) || fe.getType().equals(Double.class)){
						DoubleValue value = builder.defineInRange(option.key(), ((Number)ob).doubleValue(), range.min(), range.max());
						configValues.put(key, value);
					}

				}else if(fe.getType().isEnum()){
					EnumValue value = builder.defineEnum(option.key(), (Enum)fe.get(null), ((Enum<?>)fe.get(null)).getClass().getEnumConstants());
					configValues.put(key, value);
				}else{
					ForgeConfigSpec.ConfigValue<Object> value = builder.define(option.key(), ob);
					configValues.put(key, value);
				}
			}catch(Exception e){
				e.printStackTrace();
			}


			for(int i = 0; i < option.category().length; i++){
				builder.pop();
			}
		}
	}

	private static Object convertObject(Object ob){
		if(ob instanceof ForgeRegistryEntry<?> forge){
			return forge.getRegistryName().toString();
		}

		if(ob instanceof Enum<?>){
			return ((Enum)ob).name();
		}

		return ob;
	}

	private static final HashMap<Class<?>, Tuple<Supplier<IForgeRegistry<? extends IForgeRegistryEntry<?>>>, Supplier<ResourceKey<? extends Registry<?>>>>> REGISTRY_HASH_MAP = new HashMap<>();
	static{
		REGISTRY_HASH_MAP.put(Item.class, new Tuple<>(() -> ForgeRegistries.ITEMS, () -> Registry.ITEM_REGISTRY));
		REGISTRY_HASH_MAP.put(Block.class,  new Tuple<>(() -> ForgeRegistries.BLOCKS, () -> Registry.BLOCK_REGISTRY));
		REGISTRY_HASH_MAP.put(EntityType.class,  new Tuple<>(() -> ForgeRegistries.ENTITIES, () -> Registry.ENTITY_TYPE_REGISTRY));
		REGISTRY_HASH_MAP.put(BlockEntityType.class, new Tuple<>(() -> ForgeRegistries.BLOCK_ENTITIES, () -> Registry.BLOCK_ENTITY_TYPE_REGISTRY));
		REGISTRY_HASH_MAP.put(Biome.class,  new Tuple<>(() -> ForgeRegistries.BIOMES, () -> Registry.BIOME_REGISTRY));
		REGISTRY_HASH_MAP.put(MobEffect.class,  new Tuple<>(() -> ForgeRegistries.MOB_EFFECTS, () -> Registry.MOB_EFFECT_REGISTRY));
		REGISTRY_HASH_MAP.put(Potion.class,  new Tuple<>(() -> ForgeRegistries.POTIONS, () -> Registry.POTION_REGISTRY));
	}

	public static <T extends IForgeRegistryEntry<T>> List<T> parseObject(Class<T> type, ResourceLocation location){
		Tuple<Supplier<IForgeRegistry<? extends IForgeRegistryEntry<?>>>, Supplier<ResourceKey<? extends Registry<?>>>> ent = REGISTRY_HASH_MAP.getOrDefault(type, null);
		if(ent != null){
			if(ForgeRegistries.ITEMS.containsKey(location)){
				Optional<? extends Holder<?>> optional = ent.getA().get().getHolder(location);
				if(optional.isPresent() && optional.get().isBound()){
					return List.of((T)optional.get().value());
				}
			}else{
				TagKey<T> tagKey = TagKey.create((ResourceKey<? extends Registry<T>>)ent.getB().get(), location);

				if(tagKey.isFor(ent.getA().get().getRegistryKey())){
					ITagManager<T> manager = (ITagManager<T>)ent.getA().get().tags();
					return (List<T>)manager.getTag(tagKey).stream().toList();
				}
			}
		}

		return Collections.emptyList();
	}

	private static Object loadObject(Field fe, Object ob){
		if(ob instanceof String key){
			if(fe.getType().isEnum()){
				Class<? extends Enum> cs = (Class<? extends Enum>)fe.getType();
				return EnumGetMethod.ORDINAL_OR_NAME.get(ob, cs);
			}

			ResourceLocation location = ResourceLocation.tryParse(key);
			List<?> ls = parseObject((Class<? extends IForgeRegistryEntry>)fe.getType(), location);

			if(ls != null){
				if(fe.getGenericType() instanceof List<?>){
					return ls;
				}else{
					return ls.stream().findFirst().orElseGet(() -> null);
				}
			}
		}

		if(ob instanceof Double db){
			if(fe.getType().equals(int.class) || fe.getType().equals(Integer.class)){
				return db.intValue();
			}

			if(fe.getType().equals(float.class) || fe.getType().equals(Float.class)){
				return db.floatValue();
			}

			if(fe.getType().equals(long.class) || fe.getType().equals(Long.class)){
				return db.longValue();
			}

			return db;
		}

		return ob;
	}

	public static boolean isType(Field fe, Class<?> c){
		TypeToken<?> check = TypeToken.get(c);
		TypeToken<?> token = TypeToken.get(fe.getType());

		if(fe.getGenericType() instanceof ParameterizedType prType){
			TypeToken<?> token1 = TypeToken.get(prType.getActualTypeArguments()[0]);
			if(check.isAssignableFrom(token1)) return true;
		}

		return check.isAssignableFrom(token);
	}

	@SubscribeEvent
	public static void onModConfig(ModConfigEvent.Reloading event){
		onModConfig(event.getConfig().getType());
	}

	@SubscribeEvent
	public static void onModConfig(ModConfigEvent.Loading event){
		onModConfig(event.getConfig().getType());
	}

	public static void onModConfig(ModConfig.Type type){
		for(String s : configs.getOrDefault(type == ModConfig.Type.SERVER ? Dist.DEDICATED_SERVER : Dist.CLIENT, Collections.emptyList())){
			try{
				if(configValues.containsKey(s) && configFields.containsKey(s)){
					Field fe = ConfigHandler.configFields.get(s);

					if(fe != null){
						Object obj = convertFromString(fe, configValues.get(s).get());

						if(obj != null){
							fe.set(null, obj);
						}
					}
				}
			}catch(IllegalAccessException e){
				e.printStackTrace();
			}
		}
	}

	public static void updateConfigValue(String conf, Object value){
		if(configValues.containsKey(conf)){
			updateConfigValue(configValues.get(conf), value);
		}
	}

	public static void updateConfigValue(ForgeConfigSpec.ConfigValue conf, Object value){
		Util.ioPool().execute(() -> {
			conf.set(convertToString(value));
			conf.save();
		});

		ConfigHandler.configValues.entrySet().stream().filter((c) -> c.getValue() == conf).findFirst().ifPresent(key -> {
			try{
				Field fe = ConfigHandler.configFields.get(key.getKey());

				if(fe != null){
					Object obj = convertFromString(fe, value);

					if(obj != null){
						fe.set(null, obj);
					}
				}
			}catch(IllegalAccessException e){
				e.printStackTrace();
			}
		});
	}

	@Nullable
	private static Object convertToString(Object ob){
		if(ob instanceof Collection<?>){
			Collection<Object> obs = new ArrayList<>();

			for(Object o : ((Collection<?>)ob)){
				obs.add(convertObject(o));
			}

			ob = obs;
		}else{
			ob = convertObject(ob);
		}
		return ob;
	}

	private static Object convertFromString(Field fe, Object obj) throws IllegalAccessException{
		if(fe.getType().isAssignableFrom(Collection.class)){
			ArrayList ls = new ArrayList<>();

			for(Object o : ((Collection)obj)){
				ls.add(loadObject(fe, o));
			}

			obj = ls;
		}

		return loadObject(fe, obj);
	}

	public static <T extends IForgeRegistryEntry<T>> List<T> configList(Class<T> type, List<?> in){
		ArrayList<T> list = new ArrayList<>();

		Tuple<Supplier<IForgeRegistry<?>>, Supplier<ResourceKey<? extends Registry<?>>>> ent = REGISTRY_HASH_MAP.getOrDefault(type, null);

		for(Object o : in){
			if(o == null) continue;

			if(o instanceof String tex){
				ResourceLocation location = ResourceLocation.tryParse(tex);
				if(ent.getA().get().containsKey(location)){
					Optional<? extends Holder<?>> optional = ent.getA().get().getHolder(location);
					optional.ifPresent(s -> list.add((T)s.value()));
				}else{
					TagKey<T> tagKey = TagKey.create((ResourceKey<? extends Registry<T>>)ent.getB().get(), location);
					if(tagKey.isFor(ent.getA().get().getRegistryKey())){
						ITagManager<T> manager = (ITagManager<T>)ent.getA().get().tags();
						list.addAll((List<T>)manager.getTag(tagKey).stream().toList());
					}
				}
			}
		}

		return list;
	}
}