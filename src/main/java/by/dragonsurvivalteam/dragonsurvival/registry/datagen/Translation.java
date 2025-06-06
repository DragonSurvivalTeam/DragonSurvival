package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.Tags;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The following field types have special behaviour when no {@link Translation#key()} is supplied: <br>
 * - {@link Enum} will use {@link DSLanguageProvider#enumClassKey(Class)} and {@link DSLanguageProvider#enumValue(Enum)} <br>
 * - {@link String} annotated with the type {@link Type#NONE} will use its stored value, not wrapping anything <br>
 * - {@link String} annotated with the type {@link Type#EMOTE} will use its stored value, wrapped with emote <br>
 * - {@link Holder} will use {@link Holder#getKey()} -> {@link ResourceKey#location()} -> {@link ResourceLocation#getPath()} to determine the wrapped value <br>
 * - {@link TagKey} will use {@link Tags#getTagTranslationKey(TagKey)} <br>
 * - {@link ResourceKey} will use {@link ResourceKey#location()} -> {@link ResourceLocation#getPath()} to determine the wrapped value <br>
 * - {@link ResourceLocation} will use {@link ResourceLocation#getPath()} to determine the wrapped value <br>
 */
@Target({ElementType.FIELD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Translation.Translations.class)
public @interface Translation {
    /** If it's empty the key will be derived from the field (behaviour depends on the field type) */
    String key() default "";

    Type type() default Type.NONE;

    String locale() default "en_us";

    /**
     * Translation for the key <br>
     * Comment entries will be separated by a newline (\n)
     */
    String[] comments();

    // To allow multiple translations (potentially of differing types) to be set on one field
    @Target({ElementType.FIELD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Translations {
        Translation[] value();
    }

    enum Type { // TODO :: follow vanilla syntax and always define it as <type>.<namespace>?
        ITEM("item." + DragonSurvival.MODID + ".", ""),
        BLOCK("block." + DragonSurvival.MODID + ".", ""),
        ENTITY("entity." + DragonSurvival.MODID + ".", ""),
        FLUID("fluid." + DragonSurvival.MODID + ".", ""),
        ATTRIBUTE("attribute." + DragonSurvival.MODID + ".", ""),
        ATTRIBUTE_DESCRIPTION("attribute." + DragonSurvival.MODID + ".", ".desc"),
        EFFECT("effect." + DragonSurvival.MODID + ".", ""),
        EFFECT_DESCRIPTION("effect." + DragonSurvival.MODID + ".", ".desc"),
        ENCHANTMENT("enchantment." + DragonSurvival.MODID + ".", ""),
        ENCHANTMENT_DESCRIPTION("enchantment." + DragonSurvival.MODID + ".", ".desc"),
        /** Only has one argument (the player that died) */
        DEATH("death.attack." + DragonSurvival.MODID + ".", ""),
        /** Has two arguments (the player that died and the attacking entity) */
        DEATH_PLAYER("death.attack." + DragonSurvival.MODID + ".", ".player"),
        /** Has three arguments (the player that died, the attacking entity and used item) */
        DEATH_ITEM("death.attack." + DragonSurvival.MODID + ".", ".item"),

        DAMAGE_TYPE("damage_type." + DragonSurvival.MODID + ".", ""),

        // Internal
        DESCRIPTION(DragonSurvival.MODID + ".description.", ""),
        DESCRIPTION_ADDITION(DragonSurvival.MODID + ".description.addition.", ""),
        GUI(DragonSurvival.MODID + ".gui.", ""),
        KEYBIND(DragonSurvival.MODID + ".keybind.", ""),

        ADVANCEMENT(DragonSurvival.MODID + ".advancement.", ""),
        ADVANCEMENT_DESCRIPTION(DragonSurvival.MODID + ".advancement.", ".desc"),

        CONFIGURATION(DragonSurvival.MODID + ".configuration.", ".tooltip"),

        // May be used externally
        ABILITY("dragon_ability." + DragonSurvival.MODID + ".", ""),
        ABILITY_DESCRIPTION("dragon_ability." + DragonSurvival.MODID + ".", ".desc"),

        DRAGON_SPECIES("dragon_species." + DragonSurvival.MODID + ".", ""),
        DRAGON_SPECIES_ALTAR_DESCRIPTION("dragon_species." + DragonSurvival.MODID + ".", ".altar.desc"),
        DRAGON_SPECIES_INVENTORY_DESCRIPTION("dragon_species." + DragonSurvival.MODID + ".", ".banner.desc"),
        DRAGON_SPECIES_LOCKED("dragon_species." + DragonSurvival.MODID + ".", ".locked"),

        PENALTY("dragon_penalty." + DragonSurvival.MODID + ".", ""),
        PENALTY_DESCRIPTION("dragon_penalty." + DragonSurvival.MODID + ".", ".desc"),

        PROJECTILE("projectile." + DragonSurvival.MODID + ".", ""),

        SKIN_PART("skin_part." + DragonSurvival.MODID + ".", ""),

        EMOTE("dragon_emote." + DragonSurvival.MODID + ".", ""),

        BODY("dragon_body." + DragonSurvival.MODID + ".", ""),
        BODY_DESCRIPTION("dragon_body." + DragonSurvival.MODID + ".", ".desc"),
        BODY_WINGS("dragon_body." + DragonSurvival.MODID + ".", ".wings"),
        BODY_WINGS_DESCRIPTION("dragon_body." + DragonSurvival.MODID + ".", ".wings.desc"),

        STAGE("dragon_stage." + DragonSurvival.MODID + ".", ""),
        STAGE_DESCRIPTION("dragon_stage." + DragonSurvival.MODID + ".", ".desc"),

        COMMAND("command." + DragonSurvival.MODID + ".", ""), // TODO :: replace with 'gui'
        VILLAGER_PROFESSION("entity.minecraft.villager." + DragonSurvival.MODID + ".", ""),

        /**
         * When used on {@link String} and no specified key it's expected that the string contains the translation key <br>
         * Otherwise it generally means the key is being handled in a special way / no prefix is required <br>
         * (Usually meaning that {@link Translation.Type#wrap(ResourceLocation)} is not used)
         */
        NONE("", "");

        public final String prefix;
        public final String suffix;

        Type(final String prefix, final String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String wrap(final String key) {
            return prefix + key + suffix;
        }

        /** See {@link Translation.Type#wrap(String, String)} */
        public String wrap(final Holder<?> holder) {
            //noinspection DataFlowIssue -> key is present
            return wrap(holder.getKey());
        }

        /** See {@link Translation.Type#wrap(String, String)} */
        public String wrap(final ResourceKey<?> key) {
            return wrap(key.location());
        }

        /** See {@link Translation.Type#wrap(String, String)} */
        public String wrap(final ResourceLocation location) {
            return wrap(location.getNamespace(), location.getPath());
        }

        /** To replace the default {@link DragonSurvival#MODID} with an external one */
        public String wrap(final String modid, final String key) {
            return prefix.replace(DragonSurvival.MODID, modid) + key + suffix;
        }

        /** Expects the key in the format of {@link Translation.Type#wrap(String)} */
        public String unwrap(final String key) {
            return key.substring(prefix.length(), key.length() - suffix.length());
        }
    }
}
