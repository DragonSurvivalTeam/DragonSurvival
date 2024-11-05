package by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;

import java.util.Locale;

public enum EnumSkinLayer {
    @Translation(type = Translation.Type.SKIN_PART, comments = "Base")
    BASE("Base", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "Bottom")
    BOTTOM("Bottom", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "Eyes")
    EYES("Eyes", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "Horns")
    HORNS("Horns", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "Spikes")
    SPIKES("Spikes", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "Claws")
    CLAWS("Claws", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "Teeth")
    TEETH("Teeth", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "Magic")
    MAGIC("Magic", true),
    @Translation(type = Translation.Type.SKIN_PART, comments = "Extra")
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

    EnumSkinLayer(final String name, boolean base) {
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