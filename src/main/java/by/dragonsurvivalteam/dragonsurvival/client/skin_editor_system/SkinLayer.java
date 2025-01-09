package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;

import java.util.Locale;

public enum SkinLayer {
    // The translation is empty because currently the text is shown behind the dropdown elements for the skin part selections
    @Translation(type = Translation.Type.SKIN_PART, comments = "")
    BASE("Base", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "")
    BOTTOM("Bottom", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "")
    EYES("Eyes", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "")
    HORNS("Horns", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "")
    SPIKES("Spikes", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "")
    CLAWS("Claws", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "")
    TEETH("Teeth", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "")
    MAGIC("Magic", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "")
    EXTRA("Extra", false),
    EXTRA1("Extra", false),
    EXTRA2("Extra", false),
    EXTRA3("Extra", false),
    EXTRA4("Extra", false),
    EXTRA5("Extra", false),
    EXTRA6("Extra", false),
    EXTRA7("Extra", false);

    public final String name;
    public final boolean base;

    SkinLayer(final String name, boolean base) {
        this.name = name;
        this.base = base;
    }

    public String getNameUpperCase() {
        return name.toUpperCase(Locale.ENGLISH);
    }

    public String getNameLowerCase() {
        return name.toLowerCase(Locale.ENGLISH);
    }
}