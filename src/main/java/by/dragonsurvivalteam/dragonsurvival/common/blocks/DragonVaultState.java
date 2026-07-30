package by.dragonsurvivalteam.dragonsurvival.common.blocks;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum DragonVaultState implements StringRepresentable {
    INACTIVE("inactive", 6),
    ACTIVE("active", 12),
    UNLOCKING("unlocking", 12),
    EJECTING("ejecting", 12);

    private final String serializedName;
    private final int lightLevel;

    DragonVaultState(final String serializedName, final int lightLevel) {
        this.serializedName = serializedName;
        this.lightLevel = lightLevel;
    }

    @Override
    public @NotNull String getSerializedName() {
        return serializedName;
    }

    public int lightLevel() {
        return lightLevel;
    }
}
