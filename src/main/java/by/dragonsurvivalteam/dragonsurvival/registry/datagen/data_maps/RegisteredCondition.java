package by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.google.gson.JsonObject;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import org.jetbrains.annotations.NotNull;

public record RegisteredCondition<T>(ResourceKey<T> registryKey) implements ICondition {
    private static final ResourceLocation ID = DragonSurvival.res("registered");

    private RegisteredCondition(final ResourceLocation registryType, final ResourceLocation registryName) {
        this(ResourceKey.create(ResourceKey.createRegistryKey(registryType), registryName));
    }

    @Override
    public ResourceLocation getID() {
        return ID;
    }

    @Override
    public boolean test(@NotNull final IContext context) {
        return context instanceof ContextExtension extension
                && isRegistered(extension.dragonSurvival$getRegistryAccess(), registryKey);
    }

    private static <T> boolean isRegistered(final RegistryAccess access, final ResourceKey<T> valueKey) {
        ResourceKey<? extends Registry<T>> registryKey = ResourceKey.createRegistryKey(valueKey.registry());
        return access.registry(registryKey)
                .flatMap(registry -> registry.getHolder(valueKey))
                .map(Holder::isBound)
                .orElse(false);
    }

    public static final class Serializer implements IConditionSerializer<RegisteredCondition<?>> {
        public static final Serializer INSTANCE = new Serializer();

        private Serializer() {}

        @Override
        public void write(final JsonObject json, final RegisteredCondition<?> value) {
            json.addProperty("registry", value.registryKey().registry().toString());
            json.addProperty("value", value.registryKey().location().toString());
        }

        @Override
        public RegisteredCondition<?> read(final JsonObject json) {
            return new RegisteredCondition<>(
                    new ResourceLocation(GsonHelper.getAsString(json, "registry")),
                    new ResourceLocation(GsonHelper.getAsString(json, "value"))
            );
        }

        @Override
        public ResourceLocation getID() {
            return ID;
        }
    }
}
