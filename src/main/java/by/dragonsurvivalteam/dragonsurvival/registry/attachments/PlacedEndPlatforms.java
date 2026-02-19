package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class PlacedEndPlatforms implements ValueIOSerializable {
    private final Set<Identifier> platforms = new HashSet<>();

    public boolean wasPlaced(final Identifier resource) {
        return platforms.contains(resource);
    }

    public void addPlatform(final Identifier resource) {
        platforms.add(resource);
    }

    @Override
    public void serialize(@NotNull final ValueOutput valueOutput) {
        platforms.forEach(resource -> valueOutput.putBoolean(resource.toString(), true));
    }

    @Override
    public void deserialize(@NotNull final ValueInput valueInput) {
        platforms.clear();
        valueInput.keySet().forEach(key -> platforms.add(Identifier.tryParse(key)));
    }
}
