package by.dragonsurvivalteam.dragonsurvival.compat;

public enum ModID {
    VISTA("vista"),
    IRIS("iris"),
    SODIUM("sodium"),
    JEI("jei"),
    COSMETIC_ARMOR_REWORKED("cosmeticarmorreworked"),
    SOPHISTICATED_BACKPACKS("sophisticatedbackpacks"),
    CURIOS("curios"),
    CREATE("create"),
    BEE_ADDON("bee_queen_ds"),
    DESERT_ADDON("desert_monster_ds"),
    FREECAM("freecam"),
    SILENTGEMS("silentgems"),
    EMI("emi"),
    TFC("tfc");

    private final String modid;

    ModID(final String modid) {
        this.modid = modid;
    }

    public String value() {
        return modid;
    }

    public boolean isLoaded() {
        return ModCheck.isModLoaded(modid);
    }
}