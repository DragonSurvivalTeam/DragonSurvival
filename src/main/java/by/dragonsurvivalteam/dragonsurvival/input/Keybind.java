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
import net.neoforged.neoforge.common.util.Lazy;
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
    PREVIOUS_ABILITY(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_F),

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
    TOGGLE_DESTRUCTION(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_RIGHT_ALT),

    @Translation(type = Translation.Type.KEYBIND, comments = "Open dragon inventory")
    OPEN_DRAGON_INVENTORY(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN),

    @Translation(type = Translation.Type.KEYBIND, comments = "Open ability menu")
    OPEN_ABILITY_MENU(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN),

    @Translation(type = Translation.Type.KEYBIND, comments = "Open species menu")
    OPEN_SPECIES_MENU(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN),

    @Translation(type = Translation.Type.KEYBIND, comments = "Open emote menu")
    OPEN_EMOTE_MENU(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN),

    @Translation(type = Translation.Type.KEYBIND, comments = "Open skins menu")
    OPEN_SKINS_MENU(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN),

    @Translation(type = Translation.Type.KEYBIND, comments = "Toggle summon behaviour (+ SHIFT)")
    TOGGLE_SUMMON_BEHAVIOUR(KeyConflictContext.IN_GAME, GLFW.GLFW_KEY_UNKNOWN);

    @Translation(comments = "Dragon Survival")
    private static final String CATEGORY = Translation.Type.KEYBIND.wrap("category");

    public static final int KEY_RELEASED = 0;
    public static final int KEY_PRESSED = 1;
    public static final int KEY_HELD = 2;

    private final IKeyConflictContext keyConflictContext;
    private final int defaultKey;

    private @Nullable Lazy<KeyMapping> keyMapping;

    Keybind(final IKeyConflictContext keyConflictContext, int defaultKey) {
        this.keyConflictContext = keyConflictContext;
        this.defaultKey = defaultKey;
    }

    @SubscribeEvent
    public static void registerAllKeys(final RegisterKeyMappingsEvent event) {
        for (Keybind keybind : values()) {
            event.register(keybind.get());
        }
    }

    public KeyMapping get() {
        if (keyMapping == null) {
            // Initialize here due to needing access to the category
            keyMapping = Lazy.of(() -> new KeyMapping(Translation.Type.KEYBIND.wrap(toString().toLowerCase(Locale.ENGLISH)), keyConflictContext, InputConstants.Type.KEYSYM, defaultKey, CATEGORY));
        }

        return keyMapping.get();
    }

    /**
     * Mirror for {@link KeyMapping#consumeClick()}
     * Tries to consume a click triggered by {@link KeyMapping#click(InputConstants.Key)}.
     *
     * @return True if a click was consumed. False if the key has no clicks to consume.
     */
    public boolean consumeClick() {
        return get().consumeClick();
    }

    /**
     * Mirror for {@link KeyMapping#isDown()}
     *
     * @return True if the key is down (in the current KeyConflictContext).
     */
    public boolean isDown() {
        return get().isDown();
    }

    /**
     * Mirror for {@link KeyMapping#getKey()}
     *
     * @return Key for this KeyMapping.
     */
    public InputConstants.Key getKey() {
        return get().getKey();
    }

    /** Checks if the supplied key code (see {@link InputConstants}) matches the key */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // ignore
    public boolean isKey(int keyCode) {
        return getKey().getValue() == keyCode;
    }
}
