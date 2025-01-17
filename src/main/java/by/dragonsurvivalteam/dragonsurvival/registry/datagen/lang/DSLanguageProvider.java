package by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Tiers;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.modscan.ModAnnotation;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.apache.commons.lang3.text.WordUtils;
import org.objectweb.asm.Type;

import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class DSLanguageProvider extends LanguageProvider {
    private final String locale;
    private final CompletableFuture<HolderLookup.Provider> lookup;

    public DSLanguageProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> lookup, final String locale) {
        super(output, DragonSurvival.MODID, locale);
        this.lookup = lookup;
        this.locale = locale;
    }

    public static Component enumClass(final Enum<?> enumValue) {
        return Component.translatable(enumClassKey(enumValue));
    }

    public static Component enumValue(final Enum<?> enumValue) {
        return Component.translatable(enumClassKey(enumValue) + "." + enumValue.toString().toLowerCase(Locale.ENGLISH));
    }

    /** See {@link DSLanguageProvider#enumClassKey(Class)} */
    private static String enumClassKey(final Enum<?> enumValue) {
        return enumClassKey(enumValue.getClass());
    }

    private static String enumValueKey(final Enum<?> enumValue) {
        return enumClassKey(enumValue) + "." + enumValue.toString().toLowerCase(Locale.ENGLISH);
    }

    /** Replace 'SomeDefinedClass' with 'enum.some_defined_class' for the translation key */
    private static String enumClassKey(final Class<?> classType) {
        return "enum." + classType.getSimpleName().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ENGLISH);
    }

    @Override
    protected void addTranslations() {
        // This list contains a separate entry for each annotation - therefor we don't need to check for the Translations list element
        Set<ModFileScanData.AnnotationData> annotationDataSet = ModList.get().getModFileById(DragonSurvival.MODID).getFile().getScanResult().getAnnotations();

        handleTranslationAnnotations(annotationDataSet);
        handleConfigCategories(annotationDataSet);
        handleVanilla();

        handleParts();

        // It seems only built-in registries are present (which excludes dragon species)
        // Therefor we have to handle these manually (because the tags are dynamically created)
        add(Tags.getTagTranslationKey(DSItemTags.key(LangKey.DRAGON_FOOD.apply(BuiltInDragonSpecies.CAVE.location()))), "Cave Dragon Food");
        add(Tags.getTagTranslationKey(DSItemTags.key(LangKey.DRAGON_FOOD.apply(BuiltInDragonSpecies.FOREST.location()))), "Forest Dragon Food");
        add(Tags.getTagTranslationKey(DSItemTags.key(LangKey.DRAGON_FOOD.apply(BuiltInDragonSpecies.SEA.location()))), "Sea Dragon Food");
    }

    private void handleVanilla() {
        for (ResourceKey<DamageType> damageType : ResourceHelper.keys(lookup.join(), Registries.DAMAGE_TYPE)) {
            add(Translation.Type.DAMAGE_TYPE.wrap(damageType.location()), capitalize(damageType.location().getPath()));
        }

        // Used by 'HarvestBonuses'
        for (Tiers tier : Tiers.values()) {
            add(enumValueKey(tier), capitalize(tier.toString().toLowerCase(Locale.ENGLISH)));
        }

        // Tags are not available during data generation
        add(Tags.getTagTranslationKey(DamageTypeTags.IS_FIRE), "Fire");

        add(Tags.getTagTranslationKey(BlockTags.MINEABLE_WITH_PICKAXE), "Mineable with Pickaxe");
        add(Tags.getTagTranslationKey(BlockTags.MINEABLE_WITH_AXE), "Mineable with Axe");
        add(Tags.getTagTranslationKey(BlockTags.MINEABLE_WITH_SHOVEL), "Mineable with Shovel");
    }

    private void handleTranslationAnnotations(final Set<ModFileScanData.AnnotationData> annotationDataSet) {
        Type translationType = Type.getType(Translation.class);

        for (ModFileScanData.AnnotationData annotationData : annotationDataSet) {
            if (!annotationData.annotationType().equals(translationType)) {
                continue;
            }

            // Default values of annotations are not stored in the annotation data map
            String locale = (String) annotationData.annotationData().get("locale");

            if (locale != null && !locale.equals(this.locale)) {
                continue;
            }

            String key = (String) annotationData.annotationData().get("key");
            Optional<ModAnnotation.EnumHolder> optionalEnum = Optional.ofNullable((ModAnnotation.EnumHolder) annotationData.annotationData().get("type"));
            Translation.Type type = optionalEnum.map(holder -> Translation.Type.valueOf(holder.value())).orElse(Translation.Type.NONE);
            //noinspection unchecked -> type is correct
            List<String> comments = (List<String>) annotationData.annotationData().get("comments");

            if (key == null && annotationData.targetType() == ElementType.FIELD) {
                try {
                    // Only static fields are supported - non-static types will throw an NullPointerException when retrieving the field value
                    // Currently that is intended since annotating a non-static field with a translation would be a user error
                    Field field = Class.forName(annotationData.clazz().getClassName()).getDeclaredField(annotationData.memberName());
                    field.setAccessible(true);

                    if (Holder.class.isAssignableFrom(field.getType())) {
                        Holder<?> holder = (Holder<?>) field.get(null);
                        //noinspection DataFlowIssue -> only a problem if we work with Holder$Direct which should not be the case here
                        add(type.wrap(holder.getKey().location().getPath()), format(comments));
                        continue;
                    }

                    if (TagKey.class.isAssignableFrom(field.getType())) {
                        TagKey<?> tag = (TagKey<?>) field.get(null);
                        add(Tags.getTagTranslationKey(tag), format(comments));
                        continue;
                    }

                    if (ResourceKey.class.isAssignableFrom(field.getType())) {
                        ResourceKey<?> resourceKey = (ResourceKey<?>) field.get(null);
                        add(type.wrap(resourceKey.location().getPath()), format(comments));
                        continue;
                    }

                    if (ResourceLocation.class.isAssignableFrom(field.getType())) {
                        ResourceLocation resourceLocation = (ResourceLocation) field.get(null);
                        add(type.wrap(resourceLocation.getPath()), format(comments));
                        continue;
                    }

                    if (type == Translation.Type.NONE && String.class.isAssignableFrom(field.getType())) {
                        String translationKey = (String) field.get(null);
                        add(translationKey, format(comments));
                        continue;
                    }

                    if (type == Translation.Type.EMOTE && String.class.isAssignableFrom(field.getType())) {
                        String translationKey = (String) field.get(null);
                        add(type.wrap(translationKey), format(comments));
                        continue;
                    }

                    // For advancement translations the field will only contain the path which will also be used for the advancement itself
                    if ((type == Translation.Type.ADVANCEMENT || type == Translation.Type.ADVANCEMENT_DESCRIPTION) && String.class.isAssignableFrom(field.getType())) {
                        String path = (String) field.get(null);
                        add(type.wrap(path), format(comments));
                        continue;
                    }

                    if (field.getType().isEnum()) {
                        Enum<?> value = (Enum<?>) field.get(null);

                        if (type == Translation.Type.NONE) {
                            add(enumValueKey(value), format(comments));
                        } else {
                            // If special handling is needed (e.g. keybind)
                            add(type.wrap(value.toString().toLowerCase(Locale.ENGLISH)), format(comments));
                        }

                        continue;
                    }
                } catch (ReflectiveOperationException exception) {
                    throw new RuntimeException("An error occurred while trying to get the translations from [" + annotationData + "]", exception);
                }
            }

            if (key == null && annotationData.targetType() == ElementType.TYPE) {
                try {
                    Class<?> classType = Class.forName(annotationData.memberName());

                    if (classType.isEnum()) {
                        add(enumClassKey(classType), format(comments));
                        continue;
                    }
                } catch (ReflectiveOperationException exception) {
                    throw new RuntimeException("An error occurred while trying to get the translations from [" + annotationData + "]", exception);
                }
            }

            if (key == null || key.isEmpty()) {
                throw new IllegalStateException("Empty keys are not supported on that field type - annotation data: [" + annotationData + "]");
            }

            try {
                add(type.wrap(key), format(comments));
            } catch (IllegalStateException exception) {
                // Log extra information to make debugging easier
                DragonSurvival.LOGGER.error("Invalid translation entry due to a duplicate key issue [{}]", annotationData);
                throw exception;
            }

            if (type == Translation.Type.CONFIGURATION) {
                String capitalized = capitalize(key);

                if (capitalized.length() > 25) {
                    DragonSurvival.LOGGER.warn("Translation [{}] for the key [{}] might be too long for the configuration screen", capitalized, key);
                }

                add(type.prefix + key, capitalized);
            }
        }
    }

    private void handleConfigCategories(final Set<ModFileScanData.AnnotationData> annotationDataSet) {
        Type configOptionType = Type.getType(ConfigOption.class);
        List<String> categoriesAdded = new ArrayList<>();

        for (ModFileScanData.AnnotationData annotationData : annotationDataSet) {
            if (!annotationData.annotationType().equals(configOptionType)) {
                continue;
            }

            //noinspection unchecked -> it is the correct type
            List<String> categories = (List<String>) annotationData.annotationData().get("category");

            if (categories == null || categories.isEmpty()) {
                categories = List.of("general");
            }

            categories.forEach(category -> {
                if (categoriesAdded.contains(category)) {
                    return;
                }

                categoriesAdded.add(category);
                String key = LangKey.CATEGORY_PREFIX + category;
                add(key, capitalize(category));
            });
        }
    }

    /** Currently only intended to be used for the configuration fields */
    public static List<Translation> getTranslations(final Field field) {
        Translation translation = field.getAnnotation(Translation.class);

        if (translation != null) {
            return List.of(translation);
        }

        Translation.Translations translations = field.getAnnotation(Translation.Translations.class);

        if (translations != null) {
            return List.of(translations.value());
        }

        return List.of();
    }

    /** See {@link DSLanguageProvider#format(String...)} */
    private String format(final List<String> comments) {
        return format(comments.toArray(new String[0]));
    }

    /** Separates the comment elements by a new line */
    private String format(final String... comments) {
        StringBuilder comment = new StringBuilder();

        for (int line = 0; line < comments.length; line++) {
            comment.append(comments[line]);

            // Don't add a new line to the last line
            if (line != comments.length - 1) {
                comment.append("\n");
            }
        }

        return comment.toString();
    }

    /** See {@link DSLanguageProvider#capitalize(String...)} */
    private String capitalize(final String snakeCaseString) {
        return capitalize(snakeCaseString.split("_"));
    }

    /** Formats the parts from 'some, string, parts' into 'Some String Parts' */
    @SuppressWarnings("deprecation") // ignore
    private String capitalize(final String... components) {
        if (components.length == 1) {
            return WordUtils.capitalize(components[0]);
        }

        StringBuilder capitalized = new StringBuilder();

        for (int i = 0; i < components.length; i++) {
            capitalized.append(WordUtils.capitalize(components[i]));

            // Don't add a white space to the last element
            if (i != components.length - 1) {
                capitalized.append(" ");
            }
        }

        return capitalized.toString();
    }

    /**
     * Currently a bandaid solution since there doesn't seem to be a good way to properly translate these elements <br>
     * The files would probably need to be named sth. like 'dragonsurvival.skin_part.cave.eye.large_pupils' for it to be possible
     */
    private void handleParts() {
        add(Translation.Type.SKIN_PART.wrap("cave.none"), "");
        add(Translation.Type.SKIN_PART.wrap("sea.none"), "");
        add(Translation.Type.SKIN_PART.wrap("forest.none"), "");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_1"), "Dragon");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_2"), "Large Pupils");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_3"), "Observer");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_4"), "Cute");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_5"), "Snake");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_6"), "Drake");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_7"), "Rounded");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_8"), "Gecko");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_9"), "Curious");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_10"), "Crocodile");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_11"), "Surprised");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_12"), "Blank");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_13"), "Narrow");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_14"), "Simple");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_15"), "Raised");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_16"), "Layered");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_17"), "Empathic");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_18"), "Faded");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_19"), "Fresh");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_20"), "Square");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_21"), "Eccentric");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_22"), "Smoke");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_23"), "Pupil");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_24"), "Gritty");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_25"), "Dark");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_26"), "Glitter");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_27"), "Twinkle");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_28"), "Slanted");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_29"), "Diagonal");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_30"), "Chain");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_31"), "Hourglass");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_32"), "Lozenge");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_33"), "Triangle");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_34"), "Lizard");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_35"), "Frog");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_36"), "Crescent");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_37"), "Wave");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_38"), "Sclera");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_39"), "Pale");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_40"), "Dim");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_41"), "Spark");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_42"), "Light");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_43"), "Cross");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_44"), "Unusual");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_45"), "Quadro");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_46"), "Lens");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_47"), "Cog");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_48"), "Multicolor");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_49"), "Sharp");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_50"), "Keen");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_51"), "Gradient");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_52"), "Radiant");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_53"), "Blackhole");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_54"), "Striped");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_55"), "Beetle");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_56"), "Possessed");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_57"), "Spiral");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_58"), "Hypno");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_59"), "Gem");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_60"), "Spectre");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_61"), "Rainbow");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_62"), "Fear");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_63"), "Amphibian");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_64"), "Fish");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_65"), "Pretty");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_66"), "Heart");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_67"), "Star");
        add(Translation.Type.SKIN_PART.wrap("cave.eyes_68"), "Evil");
        add(Translation.Type.SKIN_PART.wrap("cave.base_1"), "Stone Body");
        add(Translation.Type.SKIN_PART.wrap("cave.base_2"), "Fire Clay");
        add(Translation.Type.SKIN_PART.wrap("cave.base_3"), "Charred Rock");
        add(Translation.Type.SKIN_PART.wrap("cave.base_4"), "Stony Dirt");
        add(Translation.Type.SKIN_PART.wrap("cave.base_5"), "Marble Body");
        add(Translation.Type.SKIN_PART.wrap("cave.base_6"), "Scales of Fire");
        add(Translation.Type.SKIN_PART.wrap("cave.base_7"), "Large Scales");
        add(Translation.Type.SKIN_PART.wrap("cave.base_8"), "Ancient Scales");
        add(Translation.Type.SKIN_PART.wrap("cave.base_9"), "Asbestos Fur");
        add(Translation.Type.SKIN_PART.wrap("cave.bottom_1"), "Smooth Plates");
        add(Translation.Type.SKIN_PART.wrap("cave.bottom_2"), "Large Plates");
        add(Translation.Type.SKIN_PART.wrap("cave.bottom_3"), "Fire Plates");
        add(Translation.Type.SKIN_PART.wrap("cave.bottom_4"), "Lava Eater");
        add(Translation.Type.SKIN_PART.wrap("cave.bottom_5"), "Fire Plates");
        add(Translation.Type.SKIN_PART.wrap("cave.bottom_6"), "Glowing Furnace");
        add(Translation.Type.SKIN_PART.wrap("cave.bottom_7"), "Asbestos");
        add(Translation.Type.SKIN_PART.wrap("cave.all_extra_1"), "Crown");
        add(Translation.Type.SKIN_PART.wrap("cave.all_extra_2"), "Beak");
        add(Translation.Type.SKIN_PART.wrap("cave.all_extra_3"), "Nose Axe");
        add(Translation.Type.SKIN_PART.wrap("cave.all_extra_4"), "Jaw Muscles");
        add(Translation.Type.SKIN_PART.wrap("cave.all_extra_5"), "Tongue");
        add(Translation.Type.SKIN_PART.wrap("cave.all_extra_6"), "Soft Paws");
        add(Translation.Type.SKIN_PART.wrap("cave.all_extra_7"), "Regular Paws");
        add(Translation.Type.SKIN_PART.wrap("cave.all_extra_8"), "Fur Paws");
        add(Translation.Type.SKIN_PART.wrap("cave.all_extra_9"), "Warden Tail");
        add(Translation.Type.SKIN_PART.wrap("cave.all_extra_10"), "Warden Paws");
        add(Translation.Type.SKIN_PART.wrap("cave.all_extra_11"), "Warden Body");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_1"), "Speleothems");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_2"), "Trike Frill");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_3"), "Pointy Ears");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_4"), "Straight Ears");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_body_1"), "Amethyst Outgrowths");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_body_2"), "Lava Side");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_body_3"), "Lava Back");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_body_4"), "Lava Back Stains");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_jewelry_1"), "Saddle");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_jewelry_2"), "Saddle with Supplies");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_jewelry_3"), "Rings Gold");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_jewelry_4"), "Rings Copper");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_jewelry_5"), "Collar");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_moustache_1"), "Small Mustache");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_moustache_2"), "Big Mustache");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_mouth_1"), "Lava Mouth");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_mouth_2"), "Hot Mouth");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_paws_1"), "Stone Paws");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_paws_2"), "Hot Paws");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_paws_3"), "Lava Paws");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_tail_1"), "Little Mace");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_tail_2"), "Medium Mace");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_tail_3"), "Big Mace");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_tail_4"), "Dedicurus Tail");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_tail_5"), "Ankylosaurus Tail");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_wings_1"), "Lava Feather Wings");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_wings_2"), "Stone Wings");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_wings_3"), "Stone Wings Top");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_wings_4"), "Stone Wings Bottom");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_wings_5"), "Amethyst Wings Top");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_wings_6"), "Amethyst Wings Bottom");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_wings_7"), "Lava Wings Top");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_wings_8"), "Lava Wings Bottom");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_extra_wings_9"), "Star Wings");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_fins_1"), "Fire Feathers");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_fins_2"), "Stone Feathers");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_fins_3"), "Sharp Feathers");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_fins_4"), "Parrot Feathers");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_fins_5"), "Amethyst Feathers");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_fins_6"), "Smaller Dots");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_fins_7"), "Blazing Wings");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_1"), "Thorn Brows");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_2"), "Twisted Brows");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_3"), "Front Horn");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_4"), "Twisted Front Horn");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_5"), "Thick Nose Horn");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_6"), "Twisted Thick Nose Horn");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_7"), "Long Nose");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_8"), "Twisted Long Nose");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_9"), "Rhino Horn");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_10"), "Twisted Rhino Horn");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_11"), "Unicorn Horn");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_12"), "Twisted Unicorn Horn");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_13"), "Trike Horns");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_14"), "Twisted Trike Horns");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_15"), "Elbow Horns");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_16"), "Twisted Elbow Horns");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_17"), "Horn Back Spikes");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_18"), "Twisted Horn Back Spikes");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_19"), "Black Long Nose");
        add(Translation.Type.SKIN_PART.wrap("cave.extra_horns_20"), "Black Nose Horn");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_magic_1"), "Mechanisms");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_magic_2"), "Swords");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_magic_3"), "Arrows");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_magic_4"), "Meander");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_magic_5"), "Time");
        add(Translation.Type.SKIN_PART.wrap("cave.cave_magic_6"), "Echo");
        add(Translation.Type.SKIN_PART.wrap("cave.teeth_1"), "Small Teeth");
        add(Translation.Type.SKIN_PART.wrap("cave.teeth_2"), "Regular Teeth");
        add(Translation.Type.SKIN_PART.wrap("cave.teeth_3"), "Small Fangs");
        add(Translation.Type.SKIN_PART.wrap("cave.teeth_4"), "Big Fangs");
        add(Translation.Type.SKIN_PART.wrap("cave.teeth_5"), "Boars Fangs");
        add(Translation.Type.SKIN_PART.wrap("cave.teeth_6"), "Regular Fangs");
        add(Translation.Type.SKIN_PART.wrap("cave.teeth_7"), "Lower Big Fangs");
        add(Translation.Type.SKIN_PART.wrap("cave.teeth_8"), "Two Rows Of Teeth");
        add(Translation.Type.SKIN_PART.wrap("cave.teeth_9"), "Crooked Teeth");
        add(Translation.Type.SKIN_PART.wrap("cave.teeth_10"), "Crooked Fangs");
        add(Translation.Type.SKIN_PART.wrap("cave.teeth_11"), "Evil Teeth");
        add(Translation.Type.SKIN_PART.wrap("cave.teeth_12"), "Chinese Fangs");
        add(Translation.Type.SKIN_PART.wrap("cave.claw_1"), "Three Red Claws");
        add(Translation.Type.SKIN_PART.wrap("cave.claw_2"), "Scabrous Claws");
        add(Translation.Type.SKIN_PART.wrap("cave.claw_3_cave"), "Cave Claws");
        add(Translation.Type.SKIN_PART.wrap("cave.claw_3_cave_1"), "Cave Newborn Claws");
        add(Translation.Type.SKIN_PART.wrap("cave.claw_3_cave_2"), "Cave Young Claws");
        add(Translation.Type.SKIN_PART.wrap("cave.claw_3_cave_3"), "Cave Adult Claws");
        add(Translation.Type.SKIN_PART.wrap("cave.claw_4"), "Huge Claws");
        add(Translation.Type.SKIN_PART.wrap("cave.claw_5"), "Square Claws");
        add(Translation.Type.SKIN_PART.wrap("cave.claw_6"), "Sharp Claws");
        add(Translation.Type.SKIN_PART.wrap("cave.claw_7_cave"), "Lava Claws");
        add(Translation.Type.SKIN_PART.wrap("cave.spikes_1"), "Spineback");
        add(Translation.Type.SKIN_PART.wrap("cave.spikes_2"), "Low");
        add(Translation.Type.SKIN_PART.wrap("cave.spikes_3"), "Tall");
        add(Translation.Type.SKIN_PART.wrap("cave.spikes_4"), "Amethyst");
        add(Translation.Type.SKIN_PART.wrap("cave.spikes_5"), "Asbestos Wool");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_44"), "Ashen");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_19"), "Netherite");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_20"), "Blackstone");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_1"), "Triple");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_3"), "Twisted Triple");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_17"), "Double");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_25"), "Twisted Double");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_11"), "Bent");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_27"), "Twisted Bent");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_5"), "Long");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_26"), "Twisted Long");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_4"), "Wide");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_6"), "Twisted Wide");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_8"), "Upper");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_28"), "Twisted Upper");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_12"), "Lower");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_29"), "Twisted Lower");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_9"), "Pinecone");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_14"), "Twisted Pinecone");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_31"), "Short");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_32"), "Twisted Short");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_33"), "Bull");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_34"), "Twisted Bull");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_35"), "Ram");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_36"), "Twisted Ram");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_15"), "Twigs");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_42"), "Twisted Twigs");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_10"), "Soldier");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_38"), "Twisted Soldier");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_2"), "Royal");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_18"), "Twisted Royal");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_16"), "Infernal");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_23"), "Twisted Infernal");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_24"), "Tree");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_43"), "Twisted Tree");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_7"), "Guard");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_37"), "Twisted Guard");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_39"), "Defender");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_40"), "Twisted Defender");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_13"), "Sorcerer");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_30"), "Twisted Sorcerer");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_49"), "Stump");
        add(Translation.Type.SKIN_PART.wrap("cave.horns_50"), "Trident");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_1"), "Snake");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_2"), "Gecko");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_3"), "Cute");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_4"), "Curious");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_5"), "Rounded");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_6"), "Crocodile");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_7"), "Drake");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_8"), "Surprised");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_9"), "Blank");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_10"), "Observer");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_11"), "Large Pupils");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_12"), "Dragon");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_13"), "Narrow");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_14"), "Simple");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_15"), "Raised");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_16"), "Layered");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_17"), "Empathic");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_18"), "Faded");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_19"), "Fresh");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_20"), "Square");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_21"), "Eccentric");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_22"), "Smoke");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_23"), "Pupil");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_24"), "Gritty");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_25"), "Dark");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_26"), "Glitter");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_27"), "Twinkle");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_28"), "Slanted");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_29"), "Diagonal");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_30"), "Chain");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_31"), "Hourglass");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_32"), "Lozenge");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_33"), "Triangle");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_34"), "Lizard");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_35"), "Frog");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_36"), "Crescent");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_37"), "Wave");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_38"), "Sclera");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_39"), "Pale");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_40"), "Dim");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_41"), "Spark");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_42"), "Light");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_43"), "Cross");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_44"), "Unusual");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_45"), "Quadro");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_46"), "Lens");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_47"), "Cog");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_48"), "Multicolor");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_49"), "Sharp");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_50"), "Keen");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_51"), "Gradient");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_52"), "Radiant");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_53"), "Blackhole");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_54"), "Striped");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_55"), "Beetle");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_56"), "Possessed");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_57"), "Spiral");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_58"), "Hypno");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_59"), "Gem");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_60"), "Spectre");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_61"), "Rainbow");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_62"), "Fear");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_63"), "Amphibian");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_64"), "Fish");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_65"), "Pretty");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_66"), "Heart");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_67"), "Star");
        add(Translation.Type.SKIN_PART.wrap("sea.eyes_68"), "Evil");
        add(Translation.Type.SKIN_PART.wrap("sea.base_1"), "Deepwater");
        add(Translation.Type.SKIN_PART.wrap("sea.base_2"), "Ocean");
        add(Translation.Type.SKIN_PART.wrap("sea.base_3"), "River");
        add(Translation.Type.SKIN_PART.wrap("sea.base_4"), "Fish");
        add(Translation.Type.SKIN_PART.wrap("sea.base_5"), "Ice");
        add(Translation.Type.SKIN_PART.wrap("sea.base_6"), "Large Scales");
        add(Translation.Type.SKIN_PART.wrap("sea.base_7"), "Ancient Scales");
        add(Translation.Type.SKIN_PART.wrap("sea.base_8"), "Wet Fur");
        add(Translation.Type.SKIN_PART.wrap("sea.bottom_1"), "Snowy");
        add(Translation.Type.SKIN_PART.wrap("sea.bottom_2"), "Bright");
        add(Translation.Type.SKIN_PART.wrap("sea.bottom_3"), "Frozen");
        add(Translation.Type.SKIN_PART.wrap("sea.bottom_4"), "Waves");
        add(Translation.Type.SKIN_PART.wrap("sea.bottom_5"), "Orca");
        add(Translation.Type.SKIN_PART.wrap("sea.bottom_6"), "Plates");
        add(Translation.Type.SKIN_PART.wrap("sea.bottom_7"), "Furry Bottom");
        add(Translation.Type.SKIN_PART.wrap("sea.bottom_8"), "Flat Bottom");
        add(Translation.Type.SKIN_PART.wrap("sea.all_extra_1"), "Crown");
        add(Translation.Type.SKIN_PART.wrap("sea.all_extra_2"), "Beak");
        add(Translation.Type.SKIN_PART.wrap("sea.all_extra_3"), "Nose Axe");
        add(Translation.Type.SKIN_PART.wrap("sea.all_extra_4"), "Jaw Muscles");
        add(Translation.Type.SKIN_PART.wrap("sea.all_extra_5"), "Tongue");
        add(Translation.Type.SKIN_PART.wrap("sea.all_extra_6"), "Soft Paws");
        add(Translation.Type.SKIN_PART.wrap("sea.all_extra_7"), "Regular Paws");
        add(Translation.Type.SKIN_PART.wrap("sea.all_extra_8"), "Fur Paws");
        add(Translation.Type.SKIN_PART.wrap("sea.all_extra_9"), "Warden Tail");
        add(Translation.Type.SKIN_PART.wrap("sea.all_extra_10"), "Warden Paws");
        add(Translation.Type.SKIN_PART.wrap("sea.all_extra_11"), "Warden Body");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_1"), "Small Mane");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_2"), "Big Mane");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_3"), "Pointy Ears");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_4"), "Straight Ears");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_5"), "Frill Trike");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_6"), "Frill Small");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_7"), "Frill Big");
        add(Translation.Type.SKIN_PART.wrap("sea.bonus_eyes"), "Bonus Eyes");
        add(Translation.Type.SKIN_PART.wrap("sea.glob_tail"), "Glob Tail");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_body_1"), "Balanus");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_body_2"), "Battle Scars");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_body_3"), "Glow Dots");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_body_4"), "Back Glow Dots");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_jewelry_1"), "Saddle");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_jewelry_2"), "Saddle with Supplies");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_jewelry_3"), "Rings Gold");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_jewelry_4"), "Rings Copper");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_jewelry_5"), "Collar");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_moustache_1"), "Small Mustache");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_moustache_2"), "Big Mustache");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_tail_1"), "Dedicurus Tail");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_tail_2"), "Ankylosaurus Tail");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_tail_3"), "Crystal Lizard");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_1"), "Round Wings Top");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_2"), "Round Wings Bottom");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_3"), "Wind Wings Top");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_4"), "Wind Wings Bottom");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_5"), "Ocean Wings Top");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_6"), "Ocean Wings Bottom");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_7"), "Penguins Dream");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_8"), "Wings Edge");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_9"), "Wing Patterns Top");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_10"), "Wing Patterns Bottom");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_11"), "Star Wings Bottom");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_12"), "Star Wings Top");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_13"), "Dots Wings Bottom");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_extra_wings_14"), "Dots Wings Top");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_fins_1"), "Fire Feathers");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_fins_2"), "Stone Feathers");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_fins_3"), "Sharp Feathers");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_fins_4"), "Parrot Feathers");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_fins_5"), "Amethyst Feathers");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_fins_6"), "Smaller Dots");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_fins_7"), "Blazing Wings");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_1"), "Thorn Brows");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_2"), "Twisted Brows");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_3"), "Front Horn");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_4"), "Twisted Front Horn");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_5"), "Thick Nose Horn");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_6"), "Twisted Thick Nose Horn");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_7"), "Long Nose");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_8"), "Twisted Long Nose");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_9"), "Rhino Horn");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_10"), "Twisted Rhino Horn");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_11"), "Unicorn Horn");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_12"), "Twisted Unicorn Horn");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_13"), "Trike Horns");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_14"), "Twisted Trike Horns");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_15"), "Elbow Horns");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_16"), "Twisted Elbow Horns");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_17"), "Horn Back Spikes");
        add(Translation.Type.SKIN_PART.wrap("sea.extra_horns_18"), "Twisted Horn Back Spikes");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_magic_1"), "Mechanisms");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_magic_2"), "Swords");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_magic_3"), "Arrows");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_magic_4"), "Meander");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_magic_5"), "Time");
        add(Translation.Type.SKIN_PART.wrap("sea.sea_magic_6"), "Echo");
        add(Translation.Type.SKIN_PART.wrap("sea.teeth_1"), "Small Teeth");
        add(Translation.Type.SKIN_PART.wrap("sea.teeth_2"), "Regular Teeth");
        add(Translation.Type.SKIN_PART.wrap("sea.teeth_3"), "Small Fangs");
        add(Translation.Type.SKIN_PART.wrap("sea.teeth_4"), "Big Fangs");
        add(Translation.Type.SKIN_PART.wrap("sea.teeth_5"), "Boars Fangs");
        add(Translation.Type.SKIN_PART.wrap("sea.teeth_6"), "Regular Fangs");
        add(Translation.Type.SKIN_PART.wrap("sea.teeth_7"), "Lower Big Fangs");
        add(Translation.Type.SKIN_PART.wrap("sea.teeth_8"), "Two Rows Of Teeth");
        add(Translation.Type.SKIN_PART.wrap("sea.teeth_9"), "Crooked Teeth");
        add(Translation.Type.SKIN_PART.wrap("sea.teeth_10"), "Crooked Fangs");
        add(Translation.Type.SKIN_PART.wrap("sea.teeth_11"), "Evil Teeth");
        add(Translation.Type.SKIN_PART.wrap("sea.teeth_12"), "Chinese Fangs");
        add(Translation.Type.SKIN_PART.wrap("sea.claw_1"), "Three Red Claws");
        add(Translation.Type.SKIN_PART.wrap("sea.claw_2"), "Scabrous Claws");
        add(Translation.Type.SKIN_PART.wrap("sea.claw_3_sea"), "Sea Claws");
        add(Translation.Type.SKIN_PART.wrap("sea.claw_3_sea_1"), "Sea Newborn Claws");
        add(Translation.Type.SKIN_PART.wrap("sea.claw_3_sea_2"), "Sea Young Claws");
        add(Translation.Type.SKIN_PART.wrap("sea.claw_3_sea_3"), "Sea Adult Claws");
        add(Translation.Type.SKIN_PART.wrap("sea.claw_4"), "Huge Claws");
        add(Translation.Type.SKIN_PART.wrap("sea.claw_5"), "Square Claws");
        add(Translation.Type.SKIN_PART.wrap("sea.claw_6"), "Sharp Claws");
        add(Translation.Type.SKIN_PART.wrap("sea.claw_7_sea"), "Golden Claws");
        add(Translation.Type.SKIN_PART.wrap("sea.spikes_1"), "Echinoidea");
        add(Translation.Type.SKIN_PART.wrap("sea.spikes_2"), "Membrane");
        add(Translation.Type.SKIN_PART.wrap("sea.spikes_3"), "Sea King");
        add(Translation.Type.SKIN_PART.wrap("sea.spikes_4"), "Kelp");
        add(Translation.Type.SKIN_PART.wrap("sea.spikes_5"), "Oarfish");
        add(Translation.Type.SKIN_PART.wrap("sea.spikes_6"), "Prism");
        add(Translation.Type.SKIN_PART.wrap("sea.spikes_7"), "Newt");
        add(Translation.Type.SKIN_PART.wrap("sea.spikes_8"), "Fish");
        add(Translation.Type.SKIN_PART.wrap("sea.spikes_9"), "Ice Lord");
        add(Translation.Type.SKIN_PART.wrap("sea.spikes_10"), "Glacier");
        add(Translation.Type.SKIN_PART.wrap("sea.spikes_11"), "Woolly Mane");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_45"), "River");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_46"), "Sea");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_21"), "Ocean");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_1"), "Triple");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_3"), "Twisted Triple");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_17"), "Double");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_25"), "Twisted Double");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_11"), "Bent");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_27"), "Twisted Bent");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_5"), "Long");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_26"), "Twisted Long");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_4"), "Wide");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_6"), "Twisted Wide");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_8"), "Upper");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_28"), "Twisted Upper");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_12"), "Lower");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_29"), "Twisted Lower");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_9"), "Pinecone");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_14"), "Twisted Pinecone");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_31"), "Short");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_32"), "Twisted Short");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_33"), "Bull");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_34"), "Twisted Bull");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_35"), "Ram");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_36"), "Twisted Ram");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_15"), "Twigs");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_42"), "Twisted Twigs");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_10"), "Soldier");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_38"), "Twisted Soldier");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_2"), "Royal");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_18"), "Twisted Royal");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_16"), "Infernal");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_23"), "Twisted Infernal");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_24"), "Tree");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_43"), "Twisted Tree");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_7"), "Guard");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_37"), "Twisted Guard");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_39"), "Defender");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_40"), "Twisted Defender");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_13"), "Sorcerer");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_30"), "Twisted Sorcerer");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_49"), "Stump");
        add(Translation.Type.SKIN_PART.wrap("sea.horns_50"), "Trident");
        add(Translation.Type.SKIN_PART.wrap("forest.base_0"), "Meadow");
        add(Translation.Type.SKIN_PART.wrap("forest.base_1"), "Dry Season");
        add(Translation.Type.SKIN_PART.wrap("forest.base_2"), "Autumn Forest");
        add(Translation.Type.SKIN_PART.wrap("forest.base_3"), "Wood");
        add(Translation.Type.SKIN_PART.wrap("forest.base_4"), "Large Scales");
        add(Translation.Type.SKIN_PART.wrap("forest.base_5"), "Ancient Scales");
        add(Translation.Type.SKIN_PART.wrap("forest.base_6"), "Thick Grass");
        add(Translation.Type.SKIN_PART.wrap("forest.bottom_1"), "Deep Stripes");
        add(Translation.Type.SKIN_PART.wrap("forest.bottom_2"), "Overgrown");
        add(Translation.Type.SKIN_PART.wrap("forest.bottom_3"), "Soft Grass");
        add(Translation.Type.SKIN_PART.wrap("forest.bottom_4"), "Plates");
        add(Translation.Type.SKIN_PART.wrap("forest.bottom_5"), "Swamp Sludge");
        add(Translation.Type.SKIN_PART.wrap("forest.bottom_6"), "Fur Belly");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_1"), "Snake");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_2"), "Gecko");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_3"), "Cute");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_4"), "Curious");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_5"), "Rounded");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_6"), "Crocodile");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_7"), "Drake");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_8"), "Surprised");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_9"), "Blank");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_10"), "Observer");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_11"), "Large Pupils");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_12"), "Dragon");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_13"), "Narrow");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_14"), "Simple");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_15"), "Raised");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_16"), "Layered");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_17"), "Empathic");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_18"), "Faded");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_19"), "Fresh");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_20"), "Square");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_21"), "Eccentric");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_22"), "Smoke");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_23"), "Pupil");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_24"), "Gritty");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_25"), "Dark");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_26"), "Glitter");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_27"), "Twinkle");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_28"), "Slanted");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_29"), "Diagonal");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_30"), "Chain");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_31"), "Hourglass");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_32"), "Lozenge");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_33"), "Triangle");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_34"), "Lizard");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_35"), "Frog");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_36"), "Crescent");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_37"), "Wave");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_38"), "Sclera");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_39"), "Pale");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_40"), "Dim");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_41"), "Spark");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_42"), "Light");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_43"), "Cross");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_44"), "Unusual");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_45"), "Quadro");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_46"), "Lens");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_47"), "Cog");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_48"), "Multicolor");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_49"), "Sharp");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_50"), "Keen");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_51"), "Gradient");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_52"), "Radiant");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_53"), "Blackhole");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_54"), "Striped");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_55"), "Beetle");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_56"), "Possessed");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_57"), "Spiral");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_58"), "Hypno");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_59"), "Gem");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_60"), "Spectre");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_61"), "Rainbow");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_62"), "Fear");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_63"), "Amphibian");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_64"), "Fish");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_65"), "Pretty");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_66"), "Heart");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_67"), "Star");
        add(Translation.Type.SKIN_PART.wrap("forest.eyes_68"), "Evil");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_fins_1"), "Fire Feathers");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_fins_2"), "Stone Feathers");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_fins_3"), "Sharp Feathers");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_fins_4"), "Parrot Feathers");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_fins_5"), "Amethyst Feathers");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_fins_6"), "Smaller Dots");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_fins_7"), "Blazing Wings");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_47"), "Sprout");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_48"), "Sapling");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_20"), "Wood");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_1"), "Triple");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_3"), "Twisted Triple");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_17"), "Double");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_25"), "Twisted Double");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_11"), "Bent");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_27"), "Twisted Bent");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_5"), "Long");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_26"), "Twisted Long");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_4"), "Wide");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_6"), "Twisted Wide");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_8"), "Upper");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_28"), "Twisted Upper");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_12"), "Lower");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_29"), "Twisted Lower");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_9"), "Pinecone");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_14"), "Twisted Pinecone");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_31"), "Short");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_32"), "Twisted Short");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_33"), "Bull");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_34"), "Twisted Bull");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_35"), "Ram");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_36"), "Twisted Ram");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_15"), "Twigs");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_42"), "Twisted Twigs");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_10"), "Soldier");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_38"), "Twisted Soldier");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_2"), "Royal");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_18"), "Twisted Royal");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_16"), "Infernal");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_23"), "Twisted Infernal");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_24"), "Tree");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_43"), "Twisted Tree");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_7"), "Guard");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_37"), "Twisted Guard");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_39"), "Defender");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_40"), "Twisted Defender");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_13"), "Sorcerer");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_30"), "Twisted Sorcerer");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_49"), "Stump");
        add(Translation.Type.SKIN_PART.wrap("forest.horns_50"), "Trident");
        add(Translation.Type.SKIN_PART.wrap("forest.spikes_1"), "Amaranth");
        add(Translation.Type.SKIN_PART.wrap("forest.spikes_2"), "Wildfire");
        add(Translation.Type.SKIN_PART.wrap("forest.spikes_3"), "Old Leaves");
        add(Translation.Type.SKIN_PART.wrap("forest.spikes_4"), "Cactus");
        add(Translation.Type.SKIN_PART.wrap("forest.spikes_5"), "Thorny Bush");
        add(Translation.Type.SKIN_PART.wrap("forest.spikes_6"), "Lush Bushes");
        add(Translation.Type.SKIN_PART.wrap("forest.spikes_7"), "Thickets");
        add(Translation.Type.SKIN_PART.wrap("forest.spikes_8"), "Spineback");
        add(Translation.Type.SKIN_PART.wrap("forest.all_extra_1"), "Crown");
        add(Translation.Type.SKIN_PART.wrap("forest.all_extra_2"), "Beak");
        add(Translation.Type.SKIN_PART.wrap("forest.all_extra_3"), "Nose Axe");
        add(Translation.Type.SKIN_PART.wrap("forest.all_extra_4"), "Jaw Muscles");
        add(Translation.Type.SKIN_PART.wrap("forest.all_extra_5"), "Tongue");
        add(Translation.Type.SKIN_PART.wrap("forest.all_extra_6"), "Soft Paws");
        add(Translation.Type.SKIN_PART.wrap("forest.all_extra_7"), "Regular Paws");
        add(Translation.Type.SKIN_PART.wrap("forest.all_extra_8"), "Fur Paws");
        add(Translation.Type.SKIN_PART.wrap("forest.all_extra_9"), "Warden Tail");
        add(Translation.Type.SKIN_PART.wrap("forest.all_extra_10"), "Warden Paws");
        add(Translation.Type.SKIN_PART.wrap("forest.all_extra_11"), "Warden Body");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_1"), "Pointy Ears");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_2"), "Straight Ears");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_3"), "Chest Leaves");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_4"), "Chest Honeycomb");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_5"), "Chest Roots");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_6"), "Frill Trike");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_7"), "Frill Big");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_8"), "Frill Small");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_9"), "Big Eyebrows");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_10"), "Mushroom Spike");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_body_1"), "Plates");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_body_2"), "Color Point");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_body_3"), "Moss Back");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_body_4"), "Tiger Back");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_body_5"), "Leaves Back");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_jewelry_1"), "Saddle");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_jewelry_2"), "Saddle with Supplies");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_jewelry_3"), "Rings Gold");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_jewelry_4"), "Rings Copper");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_jewelry_5"), "Collar");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_moustache_1"), "Small Mustache");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_moustache_2"), "Big Mustache");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_1"), "Scorpio Small");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_2"), "Scorpio Big");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_3"), "Tail Root");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_4"), "Dedicurus Tail");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_5"), "Ankylosaurus Tail");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_6"), "Palm Leaf");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_7"), "Patterned Leaf");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_8"), "Fern");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_9"), "Amaranth");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_10"), "Bushy Tail");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_11"), "Firetail");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_12"), "Redtail");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_tail_13"), "Clumsy Situation");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_wings_1"), "Wing Forest Top");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_wings_2"), "Wing Forest Bottom");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_wings_3"), "Wings Autumn Top");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_wings_4"), "Stone Autumn Bottom");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_wings_5"), "Wings Amaranth Top");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_wings_6"), "Wings Amaranth Bottom");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_wings_7"), "Wings Creek Top");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_wings_8"), "Wings Creek Bottom");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_wings_9"), "Green Feathers");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_extra_wings_10"), "Red Edge");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_1"), "Thorn Brows");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_2"), "Twisted Brows");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_3"), "Front Horn");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_4"), "Twisted Front Horn");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_5"), "Thick Nose Horn");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_6"), "Twisted Thick Nose Horn");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_7"), "Long Nose");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_8"), "Twisted Long Nose");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_9"), "Rhino Horn");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_10"), "Twisted Rhino Horn");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_11"), "Unicorn Horn");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_12"), "Twisted Unicorn Horn");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_13"), "Trike Horns");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_14"), "Twisted Trike Horns");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_15"), "Elbow Horns");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_16"), "Twisted Elbow Horns");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_17"), "Horn Back Spikes");
        add(Translation.Type.SKIN_PART.wrap("forest.extra_horns_18"), "Twisted Horn Back Spikes");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_magic_1"), "Mechanisms");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_magic_2"), "Swords");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_magic_3"), "Arrows");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_magic_4"), "Meander");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_magic_5"), "Time");
        add(Translation.Type.SKIN_PART.wrap("forest.forest_magic_6"), "Echo");
        add(Translation.Type.SKIN_PART.wrap("forest.teeth_1"), "Small Teeth");
        add(Translation.Type.SKIN_PART.wrap("forest.teeth_2"), "Regular Teeth");
        add(Translation.Type.SKIN_PART.wrap("forest.teeth_3"), "Small Fangs");
        add(Translation.Type.SKIN_PART.wrap("forest.teeth_4"), "Big Fangs");
        add(Translation.Type.SKIN_PART.wrap("forest.teeth_5"), "Boars Fangs");
        add(Translation.Type.SKIN_PART.wrap("forest.teeth_6"), "Regular Fangs");
        add(Translation.Type.SKIN_PART.wrap("forest.teeth_7"), "Lower Big Fangs");
        add(Translation.Type.SKIN_PART.wrap("forest.teeth_8"), "Two Rows Of Teeth");
        add(Translation.Type.SKIN_PART.wrap("forest.teeth_9"), "Crooked Teeth");
        add(Translation.Type.SKIN_PART.wrap("forest.teeth_10"), "Crooked Fangs");
        add(Translation.Type.SKIN_PART.wrap("forest.teeth_11"), "Evil Teeth");
        add(Translation.Type.SKIN_PART.wrap("forest.teeth_12"), "Chinese Fangs");
        add(Translation.Type.SKIN_PART.wrap("forest.claw_1"), "Three Red Claws");
        add(Translation.Type.SKIN_PART.wrap("forest.claw_2"), "Scabrous Claws");
        add(Translation.Type.SKIN_PART.wrap("forest.claw_3_forest"), "Forest Claws");
        add(Translation.Type.SKIN_PART.wrap("forest.claw_3_forest_1"), "Forest Dark Claws");
        add(Translation.Type.SKIN_PART.wrap("forest.claw_4"), "Huge Claws");
        add(Translation.Type.SKIN_PART.wrap("forest.claw_5"), "Square Claws");
        add(Translation.Type.SKIN_PART.wrap("forest.claw_6"), "Sharp Claws");
        add(Translation.Type.SKIN_PART.wrap("forest.claw_7_forest"), "Diamond Claws");
    }
}
