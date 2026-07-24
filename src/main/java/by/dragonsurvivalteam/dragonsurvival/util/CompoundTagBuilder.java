package by.dragonsurvivalteam.dragonsurvival.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public class CompoundTagBuilder {
    private final CompoundTag tag = new CompoundTag();

    public static CompoundTagBuilder tag() {
        return new CompoundTagBuilder();
    }

    public CompoundTagBuilder putTag(final String key, final Tag value) {
        tag.put(key, value);
        return this;
    }

    public CompoundTagBuilder putString(final String key, final String value) {
        tag.putString(key, value);
        return this;
    }

    public CompoundTagBuilder putInt(final String key, int value) {
        tag.putInt(key, value);
        return this;
    }

    public CompoundTag build() {
        return tag;
    }
}