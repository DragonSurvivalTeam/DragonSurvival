package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class ResourceLocationWrapper {
    /** These are the regex meta characters that can start a valid regular expression */
    private static final List<Character> VALID_REGEX_START = List.of('.', '^', '[', '(', '\\');

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
                String namespace = split[0];
                Pattern path = Pattern.compile(split[1]);

                Set<ResourceLocation> locations = new HashSet<>();

                for (ResourceLocation key : registry.keySet()) {
                    if (key.getNamespace().equals(namespace) && path.matcher(key.getPath()).matches()) {
                        locations.add(key);
                    }
                }

                return locations;
            }
        }
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

        if (!ResourceLocation.isValidNamespace(data[0])) {
            return false;
        }

        if (ResourceLocation.isValidPath(data[1])) {
            return true;
        }

        char firstCharacter = data[1].charAt(0);

        if (!ResourceLocation.isAllowedInResourceLocation(firstCharacter) && !VALID_REGEX_START.contains(firstCharacter)) {
            // If the regex starts with an invalid resource location character it needs to be a valid regex start character
            return false;
        }

        try {
            Pattern.compile(data[1]);
            return true;
        } catch (PatternSyntaxException ignored) {
            return false;
        }
    }
}
