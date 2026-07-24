package by.dragonsurvivalteam.dragonsurvival.common.codecs.ability.animation;

public enum AnimationKey {
    CAST_MASS_BUFF("cast_mass_buff"),
    MASS_BUFF("mass_buff"),

    CAST_SELF_BUFF("cast_self_buff"),
    SELF_BUFF("self_buff"),

    CAST_MAGIC_ALT("cast_magic_alt"),
    MAGIC_ALT("magic_alt"),

    SPELL_CHARGE("spell_charge"),
    BREATH("breath");

    private final String name;

    AnimationKey(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
