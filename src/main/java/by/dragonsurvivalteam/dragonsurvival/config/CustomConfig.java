package by.dragonsurvivalteam.dragonsurvival.config;

import java.lang.reflect.InvocationTargetException;

public interface CustomConfig {
    /** Value in config syntax (e.g. 'my_key=some_value,other_key=another_value') */
    String convert();

    boolean validate(final Object configValue);

    /** @param data In the format that {@link CustomConfig#convert()} would return */
    CustomConfig parse(final String data);

    /** @param configClass is expected to be an instance of {@link CustomConfig} */
    static CustomConfig getInstance(final Class<?> configClass) throws AssertionError {
        try {
            return (CustomConfig) configClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            throw new AssertionError("Failed to handle custom config of [" + configClass.getName() + "]", exception);
        }
    }
}
