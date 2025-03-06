package by.dragonsurvivalteam.dragonsurvival.input;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/** Implementation is inspired from Create */
@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public enum Keybind {
    @Translation(type = Translation.Type.KEYBIND, comments = "Toggle flight")
    TOGGLE_FLIGHT(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_G),

    @Translation(type = Translation.Type.KEYBIND, comments = "Use ability")
    USE_ABILITY(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_C),

    @Translation(type = Translation.Type.KEYBIND, comments = "Toggle ability bar")
    TOGGLE_ABILITIES(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_X),

    @Translation(type = Translation.Type.KEYBIND, comments = "Next ability")
    NEXT_ABILITY(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_R),

    @Translation(type = Translation.Type.KEYBIND, comments = "Previous ability")
    PREVIOUS_ABILITY(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_R, KeyModifier.SHIFT),

    @Translation(type = Translation.Type.KEYBIND, comments = "Skill #1")
    ABILITY1(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_KP_1),

    @Translation(type = Translation.Type.KEYBIND, comments = "Skill #2")
    ABILITY2(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_KP_2),

    @Translation(type = Translation.Type.KEYBIND, comments = "Skill #3")
    ABILITY3(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_KP_3),

    @Translation(type = Translation.Type.KEYBIND, comments = "Skill #4")
    ABILITY4(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_KP_4),

    @Translation(type = Translation.Type.KEYBIND, comments = "Use the Spin ability")
    SPIN_ABILITY(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_V),

    @Translation(type = Translation.Type.KEYBIND, comments = "Activate free look (hold)")
    FREE_LOOK(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_LEFT_ALT),

    @Translation(type = Translation.Type.KEYBIND, comments = "Toggle large dragon destruction")
    TOGGLE_LARGE_DRAGON_DESTRUCTION(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN),

    @Translation(type = Translation.Type.KEYBIND, comments = "Toggle multi mining")
    TOGGLE_MULTI_MINING(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN),

    @Translation(type = Translation.Type.KEYBIND, comments = "Dragon inventory")
    DRAGON_INVENTORY(KeyConflictContext.UNIVERSAL, GLFW.GLFW_KEY_UNKNOWN),

    @Translation(type = Translation.Type.KEYBIND, comments = "Ability menu")
    ABILITY_MENU(KeyConflictContext.UNIVERSAL, GLFW.GLFW_KEY_UNKNOWN),

    @Translation(type = Translation.Type.KEYBIND, comments = "Species menu")
    SPECIES_MENU(KeyConflictContext.UNIVERSAL, GLFW.GLFW_KEY_UNKNOWN),

    @Translation(type = Translation.Type.KEYBIND, comments = "Emote menu")
    EMOTE_MENU(KeyConflictContext.UNIVERSAL, GLFW.GLFW_KEY_UNKNOWN),

    @Translation(type = Translation.Type.KEYBIND, comments = "Skins menu")
    SKINS_MENU(KeyConflictContext.UNIVERSAL, GLFW.GLFW_KEY_UNKNOWN),

    @Translation(type = Translation.Type.KEYBIND, comments = "Toggle summon behaviour (+ SHIFT)")
    TOGGLE_SUMMON_BEHAVIOUR(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN);

    @Translation(comments = "Dragon Survival")
    private static final String CATEGORY = Translation.Type.KEYBIND.wrap("category");

    private final IKeyConflictContext keyConflictContext;
    private final KeyModifier defaultModifier;
    private final int defaultKey;

    private @Nullable KeyMapping keyMapping;

    Keybind(final IKeyConflictContext keyConflictContext, int defaultKey) {
        this(keyConflictContext, defaultKey, null);
    }

    Keybind(final IKeyConflictContext keyConflictContext, int defaultKey, KeyModifier defaultModifier) {
        this.keyConflictContext = keyConflictContext;
        this.defaultKey = defaultKey;
        this.defaultModifier = defaultModifier;
    }

    @SubscribeEvent
    public static void registerAllKeys(final RegisterKeyMappingsEvent event) {
        for (Keybind keybind : values()) {
            event.register(keybind.get());
        }
    }

    public KeyMapping get() {
        if (keyMapping == null) {
            String translationKey = Translation.Type.KEYBIND.wrap(name().toLowerCase(Locale.ENGLISH));

            if (defaultModifier == null) {
                keyMapping = new KeyMapping(translationKey, keyConflictContext, InputConstants.Type.KEYSYM, defaultKey, CATEGORY);
            } else {
                keyMapping = new KeyMapping(translationKey, keyConflictContext, defaultModifier, InputConstants.Type.KEYSYM, defaultKey, CATEGORY);
            }
        }

        return keyMapping;
    }

    /**
     * This cannot be used to check if the key was pressed <br>
     * Since the click count will increase when the key is held down <br>
     */
    public boolean consumeClick() {
        return get().consumeClick();
    }

    public boolean matches(final InputConstants.Key input) {
        return matches(input, true);
    }

    public boolean matches(final InputConstants.Key input, final boolean checkModifiers) {
        KeyMapping mapping = get();

        if (checkModifiers && !mapping.isConflictContextAndModifierActive()) {
            return false;
        }

        return mapping.getKey().equals(input);
    }
}
