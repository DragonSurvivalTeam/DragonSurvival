package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.config.obj.*;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.google.common.primitives.Primitives;
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
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;
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
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

@EventBusSubscriber( modid = DragonSurvivalMod.MODID, bus = Bus.MOD )
public class ConfigHandler{

	public static ClientConfig CLIENT;
	public static ForgeConfigSpec clientSpec;
	public static ServerConfig SERVER;
	public static ForgeConfigSpec serverSpec;

	public static HashMap<String, Object> defaultConfigValues = new HashMap<>(); // Contains the default values
	public static HashMap<String, ConfigOption> configObjects = new HashMap<>(); // Contains config options
	public static HashMap<String, Field> configFields = new HashMap<>(); // Contains all annotated config fields
	public static HashMap<ConfigSide, List<String>> configs = new HashMap<>(); // Contains all config keys per side
	public static HashMap<String, ForgeConfigSpec.ConfigValue<?>> configValues = new HashMap<>(); // contains all config values

	private static final HashMap<Class<?>, Tuple<Supplier<IForgeRegistry<?>>, Supplier<ResourceKey<? extends Registry<?>>>>> REGISTRY_HASH_MAP = new HashMap<>();

	public static void initTypes() {
		REGISTRY_HASH_MAP.put(Item.class, new Tuple<>(() -> ForgeRegistries.ITEMS, () -> Registry.ITEM_REGISTRY));
		REGISTRY_HASH_MAP.put(Block.class,  new Tuple<>(() -> ForgeRegistries.BLOCKS, () -> Registry.BLOCK_REGISTRY));
		REGISTRY_HASH_MAP.put(EntityType.class,  new Tuple<>(() -> ForgeRegistries.ENTITY_TYPES, () -> Registry.ENTITY_TYPE_REGISTRY));
		REGISTRY_HASH_MAP.put(BlockEntityType.class, new Tuple<>(() -> ForgeRegistries.BLOCK_ENTITY_TYPES, () -> Registry.BLOCK_ENTITY_TYPE_REGISTRY));
		REGISTRY_HASH_MAP.put(Biome.class,  new Tuple<>(() -> ForgeRegistries.BIOMES, () -> Registry.BIOME_REGISTRY));
		REGISTRY_HASH_MAP.put(MobEffect.class,  new Tuple<>(() -> ForgeRegistries.MOB_EFFECTS, () -> Registry.MOB_EFFECT_REGISTRY));
		REGISTRY_HASH_MAP.put(Potion.class,  new Tuple<>(() -> ForgeRegistries.POTIONS, () -> Registry.POTION_REGISTRY));
	}

	private static List<Field> getFields() {
		List<Field> instances = new ArrayList<>();

		Type annotationType = Type.getType(ConfigOption.class);
		ModList.get().getAllScanData().forEach(s -> {
			List<ModFileScanData.AnnotationData> ebsTargets = s.getAnnotations().stream().filter(s1 -> s1.targetType() == ElementType.FIELD).filter(annotationData -> annotationType.equals(annotationData.annotationType())).toList();

			ebsTargets.forEach(ad ->  {
				EnumHolder sidesValue = (EnumHolder)ad.annotationData().get("side");
				Dist side = Objects.equals(sidesValue.getValue(), "CLIENT") ? Dist.CLIENT : Dist.DEDICATED_SERVER;

				if(side == FMLEnvironment.dist || side == Dist.DEDICATED_SERVER){
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

	public static void initConfig() {
		initTypes();

		List<Field> fields = getFields();

		fields.forEach(field -> {
			if (!Modifier.isStatic(field.getModifiers())) {
				return;
			}

			ConfigOption configOption = field.getAnnotation(ConfigOption.class);

			try {
				defaultConfigValues.put(configOption.key(), field.get(null));
			} catch (IllegalAccessException e) {
				DragonSurvivalMod.LOGGER.error("There was a problem while trying to get the default config value of [" + ConfigHandler.createConfigPath(configOption) + "]", e);
			}

			configFields.put(configOption.key(), field);
			configObjects.put(configOption.key(), configOption);

			configs.computeIfAbsent(configOption.side(), key -> new ArrayList<>()).add(configOption.key());
		});

		if (FMLEnvironment.dist.isClient()) {
			Pair<ClientConfig, ForgeConfigSpec> clientConfig = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
			CLIENT = clientConfig.getLeft();
			clientSpec = clientConfig.getRight();

			ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, clientSpec);
		}

		Pair<ServerConfig, ForgeConfigSpec> serverConfig = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
		SERVER = serverConfig.getLeft();
		serverSpec = serverConfig.getRight();

		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, serverSpec);
	}

	public static void addConfigs(final ForgeConfigSpec.Builder builder, final ConfigSide side) {
		for (String key : configs.getOrDefault(side, Collections.emptyList())) {
			ConfigOption configOption = configObjects.get(key);
			Field field = configFields.get(key);
			Object defaultValues = defaultConfigValues.get(configOption.key());

			// Get the category - if none is present put it in the `general` category
			String[] categories = configOption.category() != null && configOption.category().length > 0 ? configOption.category() : new String[]{"general"};
			String[] comment = configOption.comment() != null ? configOption.comment() : new String[0];

			for (String category : categories) {
				builder.push(category);
			}

			builder.comment(comment);

			if (!configOption.localization().isBlank()) {
				builder.translation(configOption.localization());
			}

			try {
				Object tt = Primitives.isWrapperType(defaultValues.getClass()) ? Primitives.wrap(defaultValues.getClass()).cast(defaultValues) : defaultValues;

				ConfigRange range = field.isAnnotationPresent(ConfigRange.class) ? field.getAnnotation(ConfigRange.class) : null;
				boolean rang = range != null;

				// Fill the configuration options (define the key, default value and predicate to check if the option is valid)
				if (tt instanceof Integer intVal) {
					IntValue value = builder.defineInRange(configOption.key(), intVal, rang ? (int) range.min() : Integer.MIN_VALUE, rang ? (int) range.max() : Integer.MAX_VALUE);
					configValues.put(key, value);
				} else if (tt instanceof Float floatVal) {
					DoubleValue value = builder.defineInRange(configOption.key(), floatVal, rang ? range.min() : Float.MIN_VALUE, rang ? range.max() : Float.MAX_VALUE);
					configValues.put(key, value);
				} else if (tt instanceof Long longVal) {
					LongValue value = builder.defineInRange(configOption.key(), longVal, rang ? (long) range.min() : Long.MIN_VALUE, rang ? (long) range.max() : Long.MAX_VALUE);
					configValues.put(key, value);
				} else if (tt instanceof Double doubleVal) {
					DoubleValue value = builder.defineInRange(configOption.key(), doubleVal, rang ? range.min() : Double.MIN_VALUE, rang ? range.max() : Double.MAX_VALUE);
					configValues.put(key, value);
				} else if (tt instanceof Boolean boolValue) {
					BooleanValue value = builder.define(configOption.key(), (boolean) boolValue);
					configValues.put(key, value);
				} else if (field.getType().isEnum()) {
					EnumValue<?> value = builder.defineEnum(configOption.key(), (Enum) defaultValues, ((Enum<?>) defaultValues).getClass().getEnumConstants());
					configValues.put(key, value);
				} else if(tt instanceof List<?> list) { // TODO :: Numeric lists?
					ConfigValue<List<?>> value = builder.defineList(configOption.key(), list, configValue -> field.isAnnotationPresent(IgnoreConfigCheck.class) || checkConfig(configOption, configValue));
					configValues.put(key, value);
				} else {
					ConfigValue<Object> value = builder.define(configOption.key(), defaultValues);
					configValues.put(key, value);
					DragonSurvivalMod.LOGGER.warn("Potential issue found for configuration: [" + configOption.key() + "]");
				}
			} catch (Exception e) {
				DragonSurvivalMod.LOGGER.error("Invalid configuration found: [" + configOption.key() + "]", e);
			}

			for (int i = 0; i < categories.length; i++) {
				builder.pop();
			}
		}
	}

	public static boolean checkConfig(final ConfigOption configOption, final Object configValue) {
		return checkConfig(configOption.key(), configValue);
	}

	public static boolean checkConfig(final String key, final Object configValue) {
		return checkSpecific(key, configValue);
	}

	/** More specific checks depending on the config type */
	public static boolean checkSpecific(final String key, final Object configValue) {
		// TODO :: Maybe specifiy the full path?
		// Food options
		if (key.equals("caveDragon") || key.equals("forestDragon") || key.equals("seaDragon")) {
			if (configValue instanceof String string) {
				// namespace:item_id:hunger:saturation
				String[] split = string.split(":");

				if (split.length == 2) {
					return ResourceLocation.isValidResourceLocation(string);
				} else if (split.length == 4) {
					return ResourceLocation.isValidResourceLocation(split[0] + ":" + split[1]);
				}
			}

			return false;
		}

		if (key.equals("hurtfulToCaveDragon") || key.equals("hurtfulToForestDragon") || key.equals("hurtfulToSeaDragon")) {
			if (configValue instanceof String string) {
				// namespace:item_id:damage
				String[] split = string.split(":");

				if (split.length == 3) {
					return ResourceLocation.isValidResourceLocation(split[0] + ":" + split[1]);
				}
			}

			return false;
		}

		// Blacklist Item Regex
		if (key.equals("blacklistedItemsRegex")) {
			if (configValue instanceof String string) {
				// namespace:regex
				// TODO :: Handle `:` or `"` in some way for regex purposes?
				int length = string.split(":").length;
				return length == 2;
			}

			return false;
		}

		// Blacklisted Slots
		if (key.equals("blacklistedSlots")) {
			try {
				Integer.parseInt(String.valueOf(configValue));
			} catch (NumberFormatException e) {
				return false;
			}

			return true;
		}

		// Dirt transformations (forest dragon breath)
		if (key.equals("dirtTransformationBlocks")) {
			if (configValue instanceof String string) {
				String[] data = string.split(":");

				if (data.length == 3) {
					return isInteger(data[2]) && ResourceLocation.isValidResourceLocation(data[0] + ":" + data[1]);
				}

				return false;
			}
		}

		String string = String.valueOf(configValue);

		if (string.split(":").length == 2) {
			// Simply checking for this at the start is unsafe since `awtaj` is a valid resource location but can cause problems if it's just some gibberish
			return ResourceLocation.isValidResourceLocation(string);
		}

		return false;
	}

	private static boolean isInteger(final String string) {
		try {
			Integer.parseInt(String.valueOf(string));
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/** Get the relevant string value from an object for specific types */
	private static Object getRelevantString(final Object object) {
		// TODO :: Might not be used / relevant at the moment?
		if (object instanceof IForgeRegistry<?> forgeRegistry) {
			return forgeRegistry.getRegistryName().toString();
		}

		if (object instanceof Enum<?> enumValue) {
			return enumValue.name();
		}

		return object;
	}

	/**
	 * @param type Class of the resource type
	 * @param location Value to parse
	 * @return Either a list of the resolved tag or the resource element
	 * @param <T> Types which can be used in a registry (e.g. Item or Block)
	 */
	public static <T> List<T> parseResourceLocation(final Class<T> type, final ResourceLocation location) {
		Tuple<Supplier<IForgeRegistry<?>>, Supplier<ResourceKey<? extends Registry<?>>>> registry = REGISTRY_HASH_MAP.getOrDefault(type, null);

		if (registry != null) {
			if (registry.getA().get().containsKey(location)) {
				Optional<? extends Holder<?>> optional = registry.getA().get().getHolder(location);

				if (optional.isPresent() && optional.get().isBound()) {
					return List.of((T) optional.get().value());
				}
			} else {
				TagKey<T> tagKey = TagKey.create((ResourceKey<? extends Registry<T>>) registry.getB().get(), location);

				if (tagKey.isFor(registry.getA().get().getRegistryKey())) {
					ITagManager<T> manager = (ITagManager<T>) registry.getA().get().tags();
					return manager.getTag(tagKey).stream().toList();
				}
			}
		}

		return Collections.emptyList();
	}

	/**
	 * If the value is a {@link String} it can return the following:
	 * <ul>
	 *     <li>Enum value if the field is an enum</li>
	 *     <li>Resource entries from {@link ConfigHandler#parseResourceLocation(Class, ResourceLocation)}</li>
	 *     <li>Otherwise just the original string value</li>
	 * </ul>
	 *
	 * Otherwise, it will check if the value is a {@link Number} and return the correct value for that<br>
	 * If it's also not a number the original value will be returned
	 */
	private static Object getRelevantValue(final Field field, final Object object) {
		if (object instanceof String stringValue) {
			if (field.getType().isEnum()) {
				Class<? extends Enum> cs = (Class<? extends Enum>) field.getType();
				return EnumGetMethod.ORDINAL_OR_NAME.get(object, cs);
			}

			ResourceLocation location = ResourceLocation.tryParse(stringValue);
			List<?> list = parseResourceLocation((Class<? extends IForgeRegistry>) field.getType(), location);

			if (list != null) {
				if (field.getGenericType() instanceof List<?>) {
					return list.isEmpty() ? List.of(stringValue) : list;
				} else {
					return list.isEmpty() ? null : stringValue;
				}
			}
		}

		if (object instanceof Number number) {
			if (field.getType().equals(double.class) || field.getType().equals(Double.class)) {
				return number.doubleValue();
			}

			if (field.getType().equals(int.class) || field.getType().equals(Integer.class)) {
				return number.intValue();
			}

			if (field.getType().equals(long.class) || field.getType().equals(Long.class)) {
				return number.longValue();
			}

			if (field.getType().equals(float.class) || field.getType().equals(Float.class)) {
				return number.floatValue();
			}

			return number;
		}

		return object;
	}

	@SubscribeEvent
	public static void onModConfig(final ModConfigEvent event) {
		onModConfig(event.getConfig().getType());
	}

	/** Sets the values of the config fields */
	public static void onModConfig(final ModConfig.Type type) {
		ConfigSide side = type == ModConfig.Type.SERVER ? ConfigSide.SERVER : ConfigSide.CLIENT;
		List<String> configList = configs.get(side);

		for (String config : configList) {
			try {
				if (configValues.containsKey(config) && configFields.containsKey(config)) {
					Field field = ConfigHandler.configFields.get(config);

					if (field != null) {
						Object object = convertFromGeneric(field, configValues.get(config).get());

						if (object != null) {
							field.set(null, object);
						}
					}
				}
			} catch (IllegalAccessException e) {
				DragonSurvivalMod.LOGGER.error("An error occured while setting the config [" + config + "]", e);
			}
		}
	}

	public static void updateConfigValue(final String configKey, final Object configValue) {
		if (configValues.containsKey(configKey)) {
			updateConfigValue(configValues.get(configKey), configValue);
		}
	}

	/** Update and save the config */
	public static void updateConfigValue(final ForgeConfigSpec.ConfigValue config, final Object configValue) {
		config.set(convertToString(configValue));
		config.save();

		ConfigHandler.configValues.entrySet().stream().filter(configList -> configList.getValue() == config).findFirst().ifPresent(configList -> {
			try {
				Field field = ConfigHandler.configFields.get(configList.getKey());

				if (field != null) {
					Object object = convertFromGeneric(field, configValue);

					if (object != null) {
						field.set(null, object);
					}
				}
			} catch (IllegalAccessException e) {
				DragonSurvivalMod.LOGGER.error("An error occured while trying to update the config [" + config.getPath() + "] with the value [" + configValue + "]", e);
			}
		});
	}

	@Nullable
	private static Object convertToString(final Object object) {
		Object result;

		if (object instanceof Collection<?> collection) {
			Collection<Object> list = new ArrayList<>();

			for (Object listElement : collection) {
				list.add(getRelevantString(listElement));
			}

			result = list;
		} else {
			result = getRelevantString(object);
		}

		return result;
	}

	/** See {@link ConfigHandler#getRelevantValue(Field, Object)} for more info */
	private static Object convertFromGeneric(final Field field, final Object object) throws IllegalAccessException {
		Object result;

		if (field.getType().isAssignableFrom(Collection.class)) {
			ArrayList<Object> list = new ArrayList<>();

			for (Object listValue : (Collection<?>) object) {
				list.add(getRelevantValue(field, listValue));
			}

			result = list;
		} else {
			result = object;
		}

		return getRelevantValue(field, result);
	}

	/**
	 * @param type Class of the resource type
	 * @param values Resource locations
	 * @return List of the resource element and the resolved tag
	 * @param <T> Types which can be used in a registry (e.g. Item or Block)
	 */
	public static <T> List<T> getResourceElements(final Class<T> type, final List<?> values) {
		Tuple<Supplier<IForgeRegistry<?>>, Supplier<ResourceKey<? extends Registry<?>>>> registry = REGISTRY_HASH_MAP.getOrDefault(type, null);
		ArrayList<T> list = new ArrayList<>();

		for (Object object : values) {
			if (object == null) {
				continue;
			}

			if (object instanceof String stringValue) {
				ResourceLocation location = ResourceLocation.tryParse(stringValue);
				Optional<? extends Holder<?>> optional = registry.getA().get().getHolder(location);
				optional.ifPresent(holder -> list.add((T) holder.value()));

				TagKey<T> tagKey = TagKey.create((ResourceKey<? extends Registry<T>>) registry.getB().get(), location);

				if (tagKey.isFor(registry.getA().get().getRegistryKey())) {
					ITagManager<T> manager = (ITagManager<T>) registry.getA().get().tags();
					list.addAll(manager.getTag(tagKey).stream().toList());
				}
			}
		}

		return list;
	}

	public static String createConfigPath(final String[] category, final String key) {
		StringBuilder path = new StringBuilder();

		for (String pathElement : category) {
			path.append(pathElement).append(".");
		}

		path.append(key);

		return path.toString();
	}

	public static String createConfigPath(final ConfigOption configOption) {
		return createConfigPath(configOption.category(), configOption.key());
	}

	public static boolean isResource(final ConfigOption configOption) {
		Field field = ConfigHandler.configFields.get(configOption.key());
		Class<?> checkType = Primitives.unwrap(field.getType());

		if (field.isAnnotationPresent(ConfigType.class)) {
			ConfigType type = field.getAnnotation(ConfigType.class);
			checkType = Primitives.unwrap(type.value());
		}

		if (ItemLike.class.isAssignableFrom(checkType) || checkType == Block.class || checkType == EntityType.class || checkType == MobEffect.class || checkType == Biome.class) {
			return true;
		}

		return false;
	}
}