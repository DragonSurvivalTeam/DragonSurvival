package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

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
