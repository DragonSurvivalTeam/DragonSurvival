package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import by.dragonsurvivalteam.dragonsurvival.util.Triple;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.Tags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Allows for: <br>
 * - normal {@link ResourceLocation} <br>
 * - tags by prefixing a {@link ResourceLocation} with '#' (e.g. '#minecraft:doors') <br>
 * - Regex in namespace and / or path (e.g. '.*:.*_bow')
 */
public class ResourceLocationWrapper {
    /** These are the regex meta characters that can start a valid regular expression */
    private static final List<Character> VALID_REGEX_START = List.of('.', '^', '[', '(', '\\');
    private static final int NAMESPACE = 0;
    private static final int PATH = 1;

    public static <T> Set<ResourceLocation> getEntries(final String location, final Registry<T> registry) {
        if (location.startsWith("#")) {
            Optional<HolderSet.Named<T>> optional = registry.getTag(TagKey.create(registry.key(), ResourceLocation.parse(location.substring(1))));
            //noinspection DataFlowIssue -> key is expected to be present
            return optional.map(entries -> entries.stream().map(entry -> entry.getKey().location()).collect(Collectors.toSet())).orElse(Set.of());
        } else {
            ResourceLocation parsed = ResourceLocation.tryParse(location);

            if (parsed != null) {
                // The user needs to make sure their regex-based location is not a valid location by itself
                // e.g. 'namespace:some_item.' is a valid resource location
                return Set.of(parsed);
            } else {
                String[] split = location.split(":");

                if (split.length != 2) {
                    return Set.of();
                }

                String namespace = split[NAMESPACE];

                Pattern namespacePattern = ResourceLocation.isValidNamespace(namespace) ? null : Pattern.compile(namespace);
                Pattern pathPattern = Pattern.compile(split[PATH]);

                Set<ResourceLocation> locations = new HashSet<>();
                Predicate<String> namespaceValidation = toCheck -> namespacePattern == null ? toCheck.equals(namespace) : namespacePattern.matcher(toCheck).matches();

                for (ResourceLocation key : registry.keySet()) {
                    if (namespaceValidation.test(key.getNamespace()) && pathPattern.matcher(key.getPath()).matches()) {
                        locations.add(key);
                    }
                }

                return locations;
            }
        }
    }

    public static <T> Set<ResourceKey<T>> map(final String location, final Registry<T> registry) {
        return map(getEntries(location, registry), registry);
    }

    public static <T> Set<ResourceKey<T>> map(final Set<ResourceLocation> locations, final Registry<T> registry) {
        Set<ResourceKey<T>> keys = new HashSet<>();

        for (ResourceLocation location : locations) {
            keys.add(ResourceKey.create(registry.key(), location));
        }

        return keys;
    }

    /**
     * Converts the {@link net.minecraft.tags.TagKey} to the format accepted by {@link by.dragonsurvivalteam.dragonsurvival.common.codecs.ResourceLocationWrapper} </br>
     * (Format example: #minecraft:ores)
     */
    public static String convert(final TagKey<?> tag) {
        return "#" + tag.location();
    }

    /** Helper method to format the {@link net.minecraft.resources.ResourceKey} into a string in the format of a resource location */
    public static String convert(final ResourceKey<?> key) {
        return convert(key.location());
    }

    /** Helper method to format the resource location into a string */
    public static String convert(final ResourceLocation location) {
        return location.toString();
    }

    /** Returns the translation of the resources by unwrapping them through {@link by.dragonsurvivalteam.dragonsurvival.common.codecs.ResourceLocationWrapper#convert(String, net.minecraft.core.Registry)} */
    public static List<MutableComponent> getTranslations(final List<String> resources, final Registry<?> registry, final Translation.Type type) {
        List<MutableComponent> components = new ArrayList<>();

        for (String resource : resources) {
            var converted = ResourceLocationWrapper.convert(resource, registry);

            converted.first().ifPresent(tag -> components.add(Component.translatable(Tags.getTagTranslationKey(tag))));
            converted.second().ifPresent(key -> components.add(Component.translatable(type.wrap(key))));
            converted.third().ifPresent(set -> components.add(DSLanguageProvider.formatList(set, key -> Component.translatable(type.wrap(key)))));
        }

        return components;
    }

    /**
     * May return one of the following: </br>
     * - {@link net.minecraft.tags.TagKey} if the resource starts with '#' </br>
     * - {@link net.minecraft.resources.ResourceKey} if the resource is a valid resource location </br>
     * - {@link java.util.Set} of {@link net.minecraft.resources.ResourceKey} otherwise (since it is a regex entry)
     */
    public static <T> Triple<TagKey<T>, ResourceKey<T>, Set<ResourceKey<T>>> convert(final String resource, final Registry<T> registry) {
        if (resource.startsWith("#")) {
            return Triple.of(TagKey.create(registry.key(), ResourceLocation.parse(resource.substring(1))), null, null);
        }

        if (ResourceLocation.tryParse(resource) != null) {
            return Triple.of(null, ResourceKey.create(registry.key(), ResourceLocation.parse(resource)), null);
        }

        return Triple.of(null, null, map(resource, registry));
    }

    public static Codec<String> validatedCodec() {
        return Codec.STRING.validate(value -> {
            boolean isValid;

            if (value.startsWith("#")) {
                isValid = ResourceLocation.tryParse(value.substring(1)) != null;
            } else {
                isValid = validateRegexResourceLocation(value);
            }

            if (!isValid) {
                return DataResult.error(() -> "[" + value + "] is not a valid resource location");
            }

            return DataResult.success(value);
        });
    }

    public static boolean validateRegexResourceLocation(final String location) {
        String[] data = location.split(":", 2);

        if (data.length != 2) {
            return false;
        }

        String namespace = data[0];

        if (namespace.startsWith("#")) {
            namespace = namespace.substring(1);
        }

        String path = data[1];

        if (ResourceLocation.tryParse(location) != null) {
            return true;
        }

        if (ResourceLocation.isValidNamespace(namespace) && isValidRegex(path)) {
            return true;
        }

        if (ResourceLocation.isValidPath(path) && isValidRegex(namespace)) {
            return true;
        }

        return isValidRegex(namespace) && isValidRegex(path);
    }

    private static boolean isValidRegex(final String string) {
        char firstCharacter = string.charAt(0);

        if (!ResourceLocation.isAllowedInResourceLocation(firstCharacter) && !VALID_REGEX_START.contains(firstCharacter)) {
            // If the regex starts with an invalid resource location character it needs to be a valid regex start character
            return false;
        }

        try {
            Pattern.compile(string);
            return true;
        } catch (PatternSyntaxException ignored) {
            return false;
        }
    }
}
