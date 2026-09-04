package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ResourceLocationWrapper;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigType;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import com.electronwill.nightconfig.core.EnumGetMethod;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the annotated classes to handle the config values <br>
 * Normally it's a one way setting from the {@link ForgeConfigSpec.ConfigValue} fields to the class fields <br>
 * (The exception being {@link ConfigHandler#updateConfigValue(String, Object)})
 */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ConfigHandler {
    /** Contains the default values (specified in-code) <br> The key is {@link ConfigOption#key()} */
    private static final HashMap<String, Object> DEFAULT_CONFIG_VALUES = new HashMap<>();
    /** Contains the {@link ConfigType} variants <br> The key is {@link ConfigOption#key()} */
    private static final HashMap<String, ConfigType> CONFIG_TYPES = new HashMap<>();
    /** Contains all {@link ConfigOption} entries <br> The key is {@link ConfigOption#key()} */
    private static final HashMap<String, ConfigOption> CONFIG_OBJECTS = new HashMap<>();
    /** Contains all fields which have a {@link ConfigOption} annotation <br> The key is {@link ConfigOption#key()} */
    private static final HashMap<String, Field> CONFIG_FIELDS = new HashMap<>();
    /** Contains all config keys per side (i.e. client or server) */
    private static final HashMap<ConfigSide, Set<String>> CONFIG_KEYS = new HashMap<>();
    /** Contains all config values */
    private static final HashMap<String, ForgeConfigSpec.ConfigValue<?>> CONFIG_VALUES = new HashMap<>();
    /** The active server config and its last observed raw values, used to support Configured's in-memory server updates. */
    private static @Nullable ModConfig serverConfig;
    private static volatile Map<String, Object> serverConfigSnapshot = Map.of();
    /** Mapping between from a registry entry like {@link Item} to its registry like {@link BuiltInRegistries#ITEM} */
    private static final HashMap<Class<?>, Registry<?>> REGISTRY_MAP = new HashMap<>();

    private static void initRegistryMapping() {
        REGISTRY_MAP.put(Item.class, BuiltInRegistries.ITEM);
        REGISTRY_MAP.put(Block.class, BuiltInRegistries.BLOCK);
        REGISTRY_MAP.put(EntityType.class, BuiltInRegistries.ENTITY_TYPE);
        REGISTRY_MAP.put(BlockEntityType.class, BuiltInRegistries.BLOCK_ENTITY_TYPE);
        REGISTRY_MAP.put(Biome.class, BuiltInRegistries.BIOME_SOURCE);
        REGISTRY_MAP.put(MobEffect.class, BuiltInRegistries.MOB_EFFECT);
        REGISTRY_MAP.put(Potion.class, BuiltInRegistries.POTION);
    }

    private static List<Field> getFields() {
        List<Field> instances = new ArrayList<>();
        Type annotationType = Type.getType(ConfigOption.class);

        ModList.get().getAllScanData().forEach(scanData -> {
            List<ModFileScanData.AnnotationData> targets = scanData.getAnnotations().stream()
                    .filter(annotationData -> annotationData.targetType() == ElementType.FIELD && annotationType.equals(annotationData.annotationType()))
                    .toList();

            targets.forEach(annotationData -> {
                ModAnnotation.EnumHolder sidesValue = (ModAnnotation.EnumHolder) annotationData.annotationData().get("side");
                Dist side = Objects.equals(sidesValue.getValue(), "CLIENT") ? Dist.CLIENT : Dist.DEDICATED_SERVER;

                if (side == FMLEnvironment.dist || side == Dist.DEDICATED_SERVER) {
                    try {
                        DragonSurvival.LOGGER.debug("Loading class [{}] for config option [{}]", annotationData.clazz().getClassName(), annotationData.memberName());
                        Class<?> classType = Class.forName(annotationData.clazz().getClassName());
                        Field field = classType.getDeclaredField(annotationData.memberName());
                        instances.add(field);
                    } catch (Exception e) {
                        DragonSurvival.LOGGER.error(e);
                    }
                }
            });
        });

        return instances;
    }

    public static void initConfig() {
        List<String> duplicateKeys = new ArrayList<>();
        initRegistryMapping();

        getFields().forEach(field -> {
            // There are no per-instance configs
            if (!Modifier.isStatic(field.getModifiers())) {
                return;
            }

            ConfigOption configOption = field.getAnnotation(ConfigOption.class);

            try {
                // null because it's a static access (i.e. no instance)
                DEFAULT_CONFIG_VALUES.put(configOption.key(), field.get(null));
            } catch (IllegalAccessException e) {
                DragonSurvival.LOGGER.error("There was a problem while trying to get the default config value of [{}]", ConfigHandler.createConfigPath(configOption), e);
            }

            CONFIG_FIELDS.put(configOption.key(), field);
            CONFIG_OBJECTS.put(configOption.key(), configOption);

            ConfigType configType = field.getAnnotation(ConfigType.class);

            if (configType != null) {
                CONFIG_TYPES.put(configOption.key(), configType);
            }

            boolean keyAdded = CONFIG_KEYS.computeIfAbsent(configOption.side(), key -> new HashSet<>()).add(configOption.key());

            if (!keyAdded) {
                duplicateKeys.add(configOption.key());
            }
        });

        if (!duplicateKeys.isEmpty()) {
            throw new IllegalStateException("Tried to add duplicate config keys: " + duplicateKeys);
        }

        ModLoadingContext loadingContext = ModLoadingContext.get();

        if (FMLLoader.getDist().isClient()) {
            Pair<ClientConfig, ForgeConfigSpec> clientConfig = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
            loadingContext.registerConfig(ModConfig.Type.CLIENT, clientConfig.getRight());
        }

        Pair<ServerConfig, ForgeConfigSpec> serverConfig = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        loadingContext.registerConfig(ModConfig.Type.SERVER, serverConfig.getRight());
    }

    public static void resetConfigValues(final ConfigSide side) {
        CONFIG_KEYS.get(side).forEach(ConfigHandler::resetConfigValue);
    }

    @SuppressWarnings({"rawtypes", "unchecked"}) // ignore
    public static void resetConfigValue(final String configKey) {
        ForgeConfigSpec.ConfigValue configValue = CONFIG_VALUES.get(configKey);
        configValue.set(configValue.getDefault());

        Field field = CONFIG_FIELDS.get(configKey);

        try {
            field.set(null, convertToFieldValue(field, configKey));
        } catch (IllegalAccessException exception) {
            DragonSurvival.LOGGER.error("Failed to update the field [{}] with the default config value", field.getName(), exception);
        }
    }

    public static Field getField(final String configKey) {
        Field field = CONFIG_FIELDS.get(configKey);

        if (field == null) {
            throw new IllegalArgumentException("There is no field for the supplied config key [" + configKey + "]");
        }

        return field;
    }

    public static void createConfigEntries(final ForgeConfigSpec.Builder builder, final ConfigSide side) {
        for (String key : CONFIG_KEYS.getOrDefault(side, Set.of())) {
            ConfigOption configOption = CONFIG_OBJECTS.get(key);
            Field field = CONFIG_FIELDS.get(key);
            Object defaultValues = DEFAULT_CONFIG_VALUES.get(configOption.key());

            List<Translation> translations = DSLanguageProvider.getTranslations(field).stream().filter(translation -> translation.type() == Translation.Type.CONFIGURATION).toList();

            if (translations.size() != 1) {
                throw new IllegalStateException("Configuration key [" + key + "] had [" + translations.size() + "] translations - expected 1");
            }

            Translation translation = translations.get(0);

            // Get the category - if none is present put it in the 'general' category
            String[] categories = configOption.category() != null && configOption.category().length > 0 ? configOption.category() : new String[]{"general"};

            for (String category : categories) {
                builder.translation(LangKey.CATEGORY_PREFIX + category);
                builder.push(category);
            }

            builder.comment(translation.comments());
            builder.translation(translation.type().prefix + translation.key());

            if (configOption.worldRestart()) {
                builder.worldRestart();
            }

            if (configOption.gameRestart()) {
                builder.worldRestart();
            }

            try {
                ConfigRange range = field.isAnnotationPresent(ConfigRange.class) ? field.getAnnotation(ConfigRange.class) : null;
                boolean hasRange = range != null;

                // Fill the configuration options (define the key, default value and predicate to check if the option is valid)
                if (defaultValues instanceof Integer value) {
                    int minValue = Integer.MIN_VALUE;
                    int maxValue = Integer.MAX_VALUE;

                    if (hasRange) {
                        minValue = Double.isNaN(range.min()) ? minValue : (int) range.min();
                        maxValue = Double.isNaN(range.max()) ? maxValue : (int) range.max();
                    }

                    CONFIG_VALUES.put(key, builder.defineInRange(configOption.key(), value, minValue, maxValue));
                } else if (defaultValues instanceof Float value) {
                    double minValue = Float.MIN_VALUE;
                    double maxValue = Float.MAX_VALUE;

                    if (hasRange) {
                        minValue = Double.isNaN(range.min()) ? minValue : range.min();
                        maxValue = Double.isNaN(range.max()) ? maxValue : range.max();
                    }

                    CONFIG_VALUES.put(key, builder.defineInRange(configOption.key(), value, minValue, maxValue));
                } else if (defaultValues instanceof Long value) {
                    long minValue = Long.MIN_VALUE;
                    long maxValue = Long.MAX_VALUE;

                    if (hasRange) {
                        minValue = Double.isNaN(range.min()) ? minValue : (long) range.min();
                        maxValue = Double.isNaN(range.max()) ? maxValue : (long) range.max();
                    }

                    CONFIG_VALUES.put(key, builder.defineInRange(configOption.key(), value, minValue, maxValue));
                } else if (defaultValues instanceof Double value) {
                    double minValue = Double.MIN_VALUE;
                    double maxValue = Double.MAX_VALUE;

                    if (hasRange) {
                        minValue = Double.isNaN(range.min()) ? minValue : range.min();
                        maxValue = Double.isNaN(range.max()) ? maxValue : range.max();
                    }

                    CONFIG_VALUES.put(key, builder.defineInRange(configOption.key(), value, minValue, maxValue));
                } else if (defaultValues instanceof Boolean value) {
                    CONFIG_VALUES.put(key, builder.define(configOption.key(), value.booleanValue()));
                } else if (field.getType().isEnum()) {
                    //noinspection unchecked,rawtypes -> ignored
                    CONFIG_VALUES.put(key, builder.defineEnum(configOption.key(), (Enum) defaultValues, ((Enum<?>) defaultValues).getClass().getEnumConstants()));
                } else if (defaultValues instanceof List<?> list) {
                    // Dragon Survival allows empty config lists.
                    ForgeConfigSpec.ConfigValue<?> configList = null;

                    boolean handledList = false;

                    // Convert custom config list to a string-based list for the 'ModConfig$ConfigValue' field
                    if (field.getGenericType() instanceof ParameterizedType listParameter) { // Get the type parameter from List<CustomConfig>
                        String className = listParameter.getActualTypeArguments()[0].getTypeName();

                        try {
                            Class<?> customConfigType = Class.forName(className);

                            if (CustomConfig.class.isAssignableFrom(customConfigType)) {
                                List<String> customList = list.stream().map(customConfig -> ((CustomConfig) customConfig).convert()).toList();
                                configList = buildList(builder, configOption, customList, configValue -> CustomConfig.getInstance(customConfigType).validate(configValue));
                                handledList = true;
                            }
                        } catch (ClassNotFoundException exception) {
                            DragonSurvival.LOGGER.error("A problem occurred while trying to handle the config [{}]", configOption.key(), exception);
                        }
                    }

                    if (!handledList) {
                        configList = buildList(builder, configOption, list, configValue -> checkSpecific(configOption, configValue));
                    }

                    CONFIG_VALUES.put(key, configList);
                } else if (defaultValues instanceof CustomConfig value) {
                    CONFIG_VALUES.put(key, builder.define(configOption.key(), value.convert()));
                } else if (defaultValues instanceof String value) {
                    CONFIG_VALUES.put(key, builder.define(configOption.key(), value));
                } else {
                    // This will likely run into a 'com.electronwill.nightconfig.core.io.WritingException: Unsupported value type' exception
                    ForgeConfigSpec.ConfigValue<Object> value = builder.define(configOption.key(), defaultValues);
                    CONFIG_VALUES.put(key, value);
                    DragonSurvival.LOGGER.warn("Potential issue found for configuration: [{}]", configOption.key());
                }
            } catch (Exception e) {
                DragonSurvival.LOGGER.error("Invalid configuration found: [{}]", configOption.key(), e);
            }

            for (int i = 0; i < categories.length; i++) {
                builder.pop();
            }
        }
    }

    private static ForgeConfigSpec.ConfigValue<?> buildList(final ForgeConfigSpec.Builder builder, final ConfigOption config, final List<?> defaultValues, final Predicate<Object> validation) {
        return builder.defineListAllowEmpty(
                List.of(config.key()),
                () -> defaultValues,
                configValue -> {
                    if (validation.test(configValue)) {
                        return true;
                    }

                    // To figure out which entry in the list has problems
                    DragonSurvival.LOGGER.debug("Config entry [{}] of config [{}] was invalid", configValue, config.key());
                    return false;
                }
        );
    }

    /** More specific checks depending on the config type */
    private static boolean checkSpecific(final ConfigOption configOption, final Object configValue) {
        switch (configOption.validation()) {
            case RESOURCE_LOCATION -> {
                return ResourceLocation.tryParse((String) configValue) != null;
            }
            case RESOURCE_LOCATION_REGEX -> {
                return ResourceLocationWrapper.validateRegexResourceLocation(configValue.toString());
            }
            case RESOURCE_LOCATION_NUMBER -> {
                String[] split = ((String) configValue).split(":");

                if (split.length != 3) {
                    return false;
                }

                if (ResourceLocation.tryParse(split[0] + ":" + split[1]) == null) {
                    return false;
                }

                return ConfigUtils.validateInteger(split[2]);
            }
            case RESOURCE_LOCATION_OPTIONAL_NUMBER -> {
                String[] split = ((String) configValue).split(":");

                if (split.length < 2) {
                    return false;
                }

                if (ResourceLocation.tryParse(split[0] + ":" + split[1]) == null) {
                    return false;
                }

                if (split.length == 3) {
                    return ConfigUtils.validateInteger(split[2]);
                }

                return split.length == 2;
            }
            case RESOURCE_LOCATION_2_OPTIONAL_NUMBERS -> {
                String[] split = ((String) configValue).split(":");

                if (split.length < 2) {
                    return false;
                }

                if (ResourceLocation.tryParse(split[0] + ":" + split[1]) == null) {
                    return false;
                }

                if (split.length == 4) {
                    return ConfigUtils.validateInteger(split[2]) && ConfigUtils.validateDouble(split[3]);
                }
            }
        }

        return true;
    }

    /**
     * If {@link ConfigType} is used then said config entries will go through here <br>
     * This also means it cannot be used if the config entries contain additional information (e.g. like the food configs)
     *
     * @param registry Registry to check data for
     * @param location Value to parse
     * @param <T>      Types which can be used in a registry (e.g. Item or Block)
     * @return Either a list of the resolved tag or the resource element
     */
    private static <T> List<T> parseResourceLocation(@NotNull final Registry<T> registry, final String location) {
        // There are configuration which have additional information after the resource location (e.g. food configuration)
        ResourceLocation resourceLocation = ResourceLocation.tryParse(location);

        if (resourceLocation == null) {
            // Only split the namespace from the (potential) regex path
            String[] splitLocation = location.split(":", 2);

            if (splitLocation.length < 2) {
                return List.of();
            }

            // Try parsing regex if it's not a valid resource location
            List<T> list = new ArrayList<>();

            registry.registryKeySet().forEach(key -> {
                ResourceLocation keyLocation = key.location();

                if (keyLocation.getNamespace().equals(splitLocation[0])) {
                    Pattern pattern = Pattern.compile(splitLocation[1]);

                    Matcher matcher = pattern.matcher(keyLocation.getPath());

                    if (matcher.matches()) {
                        list.add(registry.get(key));
                    }
                }
            });

            return list;
        }

        if (registry.containsKey(resourceLocation)) {
            Optional<Holder.Reference<T>> optional = registry.getHolder(ResourceKey.create(registry.key(), resourceLocation));

            if (optional.isPresent() && optional.get().isBound()) {
                return List.of(optional.get().value());
            }
        } else {
            Optional<TagKey<T>> tag = registry.getTagNames().filter(registryTag -> registryTag.location().equals(resourceLocation)).findAny();

            if (tag.isPresent()) {
                List<T> list = new ArrayList<>();

                registry.holders().forEach(holder -> holder.tags().forEach(
                                holderTag -> {
                                    if (tag.get().equals(holderTag)) {
                                        list.add(holder.value());
                                    }
                                }
                        )
                );

                return list;
            }
        }

        return List.of();
    }

    /**
     * @param field        The class field which dictates the type to set
     * @param value        The value (from {@link ForgeConfigSpec.ConfigValue}) which will be converted to the class field type
     * @param registryType (Optional) The type of registry object (e.g. {@link Block})
     * @return The converted value for the field
     */
    @SuppressWarnings({"unchecked", "rawtypes"}) // should be fine
    private static @Nullable Object convertToFieldValue(final Field field, final Object value, @Nullable final Class<?> registryType) {
        if (field.getGenericType() instanceof ParameterizedType listParameter) {
            try {
                Class<?> classType = Class.forName(listParameter.getActualTypeArguments()[0].getTypeName());

                // Check for string since the list itself goes through here as well
                if (CustomConfig.class.isAssignableFrom(classType) && value instanceof String string) {
                    return CustomConfig.getInstance(classType).parse(string);
                }
            } catch (ClassNotFoundException exception) {
                DragonSurvival.LOGGER.error("A problem occurred while trying to parse a custom config entry: {}", value);
            }
        }

        if (value instanceof String string) {
            if (field.getType().isEnum()) {
                Class<? extends Enum> cs = (Class<? extends Enum<?>>) field.getType();
                return EnumGetMethod.ORDINAL_OR_NAME.get(value, cs);
            }

            Registry<?> registry = REGISTRY_MAP.get(registryType);

            if (registry != null) {
                List<?> list = parseResourceLocation(registry, string);

                if (field.getGenericType() instanceof List<?>) {
                    return list.isEmpty() ? List.of(string) : list;
                } else {
                    return list.isEmpty() ? null : string;
                }
            }
        }

        if (value instanceof Number number) {
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

        return value;
    }

    @SubscribeEvent
    public static void handleConfigLoading(final ModConfigEvent.Loading event) {
        handleConfigChange(event.getConfig());
    }

    @SubscribeEvent
    public static void handleConfigReloading(final ModConfigEvent.Reloading event) {
        handleConfigChange(event.getConfig());
    }

    /** Sets the values of the config fields */
    private static void handleConfigChange(final ModConfig config) {
        ModConfig.Type type = config.getType();
        ConfigSide side = type == ModConfig.Type.SERVER ? ConfigSide.SERVER : ConfigSide.CLIENT;

        if (type == ModConfig.Type.SERVER) {
            serverConfig = config;
            serverConfigSnapshot = getServerConfigSnapshot();
        }

        Set<String> configKeys = CONFIG_KEYS.get(side);

        for (String configKey : configKeys) {
            try {
                if (CONFIG_VALUES.containsKey(configKey) && CONFIG_FIELDS.containsKey(configKey)) {
                    Field field = ConfigHandler.CONFIG_FIELDS.get(configKey);

                    if (field != null) {
                        Object value = convertToFieldValue(field, configKey);

                        if (value != null) {
                            field.set(null, value);
                        }
                    }
                }
            } catch (IllegalAccessException | IllegalArgumentException exception) {
                DragonSurvival.LOGGER.error("An error occurred while setting the config [{}]", configKey, exception);
            }
        }
    }

    /**
     * Configured updates server configs directly in memory, without firing a {@link ModConfigEvent.Reloading} event.
     * Detect that update on the server thread so the config-backed fields and the world config file stay in sync.
     */
    public static void synchronizeConfiguredServerConfig(final TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !ModList.get().isLoaded("configured") || serverConfig == null) {
            return;
        }

        Map<String, Object> currentValues = getServerConfigSnapshot();

        if (!currentValues.equals(serverConfigSnapshot)) {
            // Configured replaces entries in ModConfig's underlying data instead of using
            // ConfigValue#set. Clear Forge's per-value caches so the field update reads
            // the data Configured just received rather than the previous values.
            CONFIG_KEYS.getOrDefault(ConfigSide.SERVER, Set.of()).forEach(
                    configKey -> CONFIG_VALUES.get(configKey).clearCache()
            );
            handleConfigChange(serverConfig);
            serverConfig.save();
            serverConfigSnapshot = currentValues;
        }
    }

    /**
     * Uses the backing config data rather than {@link ForgeConfigSpec.ConfigValue#get()}.
     * Forge caches values returned by {@code get()}, which would otherwise hide updates
     * Configured makes directly to the backing data.
     */
    private static Map<String, Object> getServerConfigSnapshot() {
        HashMap<String, Object> values = new HashMap<>();

        for (String configKey : CONFIG_KEYS.getOrDefault(ConfigSide.SERVER, Set.of())) {
            Object value = serverConfig.getConfigData().get(CONFIG_VALUES.get(configKey).getPath());
            values.put(configKey, value instanceof Collection<?> collection ? List.copyOf(collection) : value);
        }

        return Map.copyOf(values);
    }

    /**
     * Update the {@link ForgeConfigSpec.ConfigValue} and class field with the new value <br>
     * (Currently only used for the ui when enabling / disabling claws e.g.)
     *
     * @param configKey The config key of the {@link ConfigOption}
     * @param newValue  Thew value that will be set
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void updateConfigValue(final String configKey, final Object newValue) {
        ForgeConfigSpec.ConfigValue valueHolder = CONFIG_VALUES.get(configKey);

        if (valueHolder == null) {
            DragonSurvival.LOGGER.error("There is no known config for [{}]", configKey);
            return;
        }


        if (newValue != null) {
            Object convertedValue = convertToConfigValue(newValue);
            boolean isValid = isValidConfigValue(configKey, convertedValue);

            if (!isValid) {
                DragonSurvival.LOGGER.error("Tried to set an invalid value [{}] for the config [{}]", convertedValue, configKey);
                return;
            }

            // Set the value for the (hidden) config value field
            valueHolder.set(convertedValue);
            valueHolder.save();
        } else {
            resetConfigValue(configKey);
        }

        try {
            // Set the value for the class field
            Field field = ConfigHandler.CONFIG_FIELDS.get(configKey);
            // Since conversion is handled here (e.g. you can't set a float field with the value '0', you'd need to specify '0f')
            field.set(null, convertToFieldValue(field, configKey));
        } catch (IllegalAccessException | IllegalArgumentException | NullPointerException exception) {
            DragonSurvival.LOGGER.error("An error occurred while trying to update the config [{}] with the value [{}]", configKey, newValue, exception);
        }
    }

    private static boolean isValidConfigValue(final String configKey, final Object value) {
        Object defaultValue = DEFAULT_CONFIG_VALUES.get(configKey);
        ConfigOption config = CONFIG_OBJECTS.get(configKey);
        Field field = CONFIG_FIELDS.get(configKey);

        if (value == null || defaultValue == null || config == null || field == null) {
            return false;
        }

        if (defaultValue instanceof Number && value instanceof Number number) {
            ConfigRange range = field.getAnnotation(ConfigRange.class);

            if (range == null) {
                return true;
            }

            double numericValue = number.doubleValue();
            return (Double.isNaN(range.min()) || numericValue >= range.min())
                    && (Double.isNaN(range.max()) || numericValue <= range.max());
        }

        if (defaultValue instanceof Collection<?> && value instanceof Collection<?> collection) {
            return collection.stream().allMatch(entry -> checkSpecific(config, entry));
        }

        if (defaultValue instanceof String && value instanceof String) {
            return checkSpecific(config, value);
        }

        return defaultValue.getClass().isInstance(value);
    }

    /**
     * Get the relevant data that is supposed to be stored in the {@link ForgeConfigSpec.ConfigValue} field
     *
     * @return The result of {@link ConfigHandler#getRelevantConfigValue(Object)} (lists will convert their entries using that method)
     */
    private static Object convertToConfigValue(final Object object) {
        Object result;

        if (object instanceof Collection<?> collection) {
            Collection<Object> list = new ArrayList<>();

            for (Object listElement : collection) {
                list.add(getRelevantConfigValue(listElement));
            }

            result = list;
        } else {
            result = getRelevantConfigValue(object);
        }

        return result;
    }

    /**
     * Get the relevant data that is supposed to be stored in the {@link ForgeConfigSpec.ConfigValue} field <br>
     *
     * @return Most likely a string or number value
     */
    @SuppressWarnings("deprecation") // ignore
    private static Object getRelevantConfigValue(final Object object) {
        if (object instanceof Registry<?> registry) {
            return registry.key().location();
        }

        if (object instanceof CustomConfig customConfig) {
            return customConfig.convert();
        }

        if (object instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        if (object instanceof Item item) {
            return item.builtInRegistryHolder().key().location();
        }

        if (object instanceof Block block) {
            return block.builtInRegistryHolder().key().location();
        }

        return object;
    }

    /**
     * Retrieves the current config value (from {@link ForgeConfigSpec.ConfigValue}) <br>
     * Said value will then be converted to match the class field <br>
     * See {@link ConfigHandler#convertToFieldValue(Field, Object, Class)} for more information
     */
    private static @Nullable Object convertToFieldValue(final Field field, final String configKey) {
        Object configValue = CONFIG_VALUES.get(configKey).get();
        ConfigType configType = CONFIG_TYPES.get(configKey);
        Class<?> registryType = configType != null ? configType.value() : null;

        Object result;

        if (Collection.class.isAssignableFrom(field.getType())) {
            Collection<?> collection = (Collection<?>) configValue;
            ArrayList<Object> resultList = new ArrayList<>();

            for (Object listValue : collection) {
                Object value = convertToFieldValue(field, listValue, registryType);

                // Could be null if the registry entry is not present (e.g. certain mod is not loaded)
                if (value != null) {
                    resultList.add(value);
                }
            }

            result = resultList;
        } else if (CustomConfig.class.isAssignableFrom(field.getType())) {
            return CustomConfig.getInstance(field.getType()).parse((String) configValue);
        } else {
            result = configValue;
        }

        return convertToFieldValue(field, result, registryType);
    }

    /**
     * @param type   Class of the resource type
     * @param values Resource locations
     * @param <T>    Types which can be used in a registry (e.g. Item or Block)
     * @return HashSet of the resource element and the resolved tag
     */
    @SuppressWarnings("unchecked") // should be fine
    public static <T> HashSet<T> getResourceElements(final Class<T> type, final List<String> values) {
        Registry<T> registry = (Registry<T>) REGISTRY_MAP.getOrDefault(type, null);
        HashSet<T> hashSet = new HashSet<>();

        for (String rawResourceLocation : values) {
            if (rawResourceLocation == null) {
                continue;
            }

            hashSet.addAll(parseResourceLocation(registry, rawResourceLocation));
        }

        return hashSet;
    }

    private static String createConfigPath(final ConfigOption configOption) {
        return createConfigPath(configOption.category(), configOption.key());
    }

    private static String createConfigPath(final String[] category, final String key) {
        StringBuilder path = new StringBuilder();

        for (String pathElement : category) {
            path.append(pathElement).append(".");
        }

        path.append(key);

        return path.toString();
    }
}
