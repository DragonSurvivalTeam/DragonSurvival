package by.dragonsurvivalteam.dragonsurvival.config.obj;

public enum Validation {
    /** Checks for {@link net.minecraft.resources.ResourceLocation} */
    RESOURCE_LOCATION,
    /** Checks the namespace for {@link net.minecraft.resources.ResourceLocation} while allowing regex for the path */
    RESOURCE_LOCATION_REGEX,
    /** Checks for {@link net.minecraft.resources.ResourceLocation} and 1 {@link Integer} */
    RESOURCE_LOCATION_NUMBER,
    /** Checks for {@link net.minecraft.resources.ResourceLocation} and 1 optional {@link Integer} */
    RESOURCE_LOCATION_OPTIONAL_NUMBER,
    /** Checks for {@link net.minecraft.resources.ResourceLocation}, 1 optional {@link Integer} and 1 optional {@link Double} */
    RESOURCE_LOCATION_2_OPTIONAL_NUMBERS,
    /** Default check */
    DEFAULT
}
