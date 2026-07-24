package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class PlacedEndPlatforms implements INBTSerializable<CompoundTag> {
    private final Set<ResourceLocation> platforms = new HashSet<>();

    public boolean wasPlaced(final ResourceLocation resource) {
        return platforms.contains(resource);
    }

    public void addPlatform(final ResourceLocation resource) {
        platforms.add(resource);
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        platforms.forEach(resource -> tag.putBoolean(resource.toString(), true));
        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        platforms.clear();
        tag.getAllKeys().forEach(key -> platforms.add(ResourceLocation.tryParse(key)));
    }
}
