package by.dragonsurvivalteam.dragonsurvival.util.json;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.resources.ResourceLocation;

import java.lang.reflect.Type;

public class ResourceLocationAdapter implements JsonSerializer<ResourceLocation>, JsonDeserializer<ResourceLocation> {
    @Override
    public ResourceLocation deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        ResourceLocation location = ResourceLocation.parse(json.getAsString());

        if (location.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
            // Can occur when migrating the save file from the non-registry entries
            return DragonSurvival.res(location.getPath());
        }

        return location;
    }

    @Override
    public JsonElement serialize(ResourceLocation source, Type sourceType, JsonSerializationContext context) {
        return context.serialize(source.toString(), String.class);
    }
}
