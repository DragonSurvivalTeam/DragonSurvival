package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public interface Copyable<T extends ValueIOSerializable> {
    default T self() {
        //noinspection unchecked -> ignore
        return (T) this;
    }

    default @Nullable T copy(final HolderLookup.Provider provider) {
        try {
            //noinspection unchecked -> ignore
            T copy = (T) getClass().getDeclaredConstructor().newInstance();
            TagValueOutput valueOutput = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
            self().serialize(valueOutput);
            ValueInput valueInput = TagValueInput.create(ProblemReporter.DISCARDING, provider, valueOutput.buildResult());
            copy.deserialize(valueInput);
            return copy;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException error) {
            DragonSurvival.LOGGER.error("Failed to copy [{}]", this.getClass().getName(), error);
            return null;
        }
    }
}
