package by.dragonsurvivalteam.dragonsurvival.util;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;

public interface Copyable<T extends INBTSerializable</* Need to be specific to avoid 'Tag cannot be converted to CAP#1' compile error */ CompoundTag>> {
    default T self() {
        //noinspection unchecked -> ignore
        return (T) this;
    }


    default @Nullable T copy(final HolderLookup.Provider provider) {
        try {
            //noinspection unchecked -> ignore
            T copy = (T) getClass().getDeclaredConstructor().newInstance();
            copy.deserializeNBT(provider, self().serializeNBT(provider));
            return copy;
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException error) {
            DragonSurvival.LOGGER.error("Failed to copy [{}]", this.getClass().getName(), error);
            return null;
        }
    }
}
