package by.dragonsurvivalteam.dragonsurvival.util.json;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Type;

public class IdentifierAdapter implements JsonSerializer<Identifier>, JsonDeserializer<Identifier> {
    @Override
    public Identifier deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        Identifier location = Identifier.parse(json.getAsString());

        if (location.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
            // Can occur when migrating the save file from the non-registry entries
            return DragonSurvival.res(location.getPath());
        }

        return location;
    }

    @Override
    public JsonElement serialize(Identifier source, Type sourceType, JsonSerializationContext context) {
        return context.serialize(source.toString(), String.class);
    }
}
