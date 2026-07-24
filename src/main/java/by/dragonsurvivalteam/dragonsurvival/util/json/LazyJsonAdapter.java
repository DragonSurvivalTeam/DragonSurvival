package by.dragonsurvivalteam.dragonsurvival.util.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.neoforged.neoforge.common.util.Lazy;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/** Get the actual type from the {@link Lazy} supplier to properly read and write the value through JSON */
public class LazyJsonAdapter implements JsonSerializer<Lazy<Object>>, JsonDeserializer<Lazy<Object>> {
    @Override
    public Lazy<Object> deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        Type actualTypeArgument = ((ParameterizedType) type).getActualTypeArguments()[0];
        return Lazy.of(() -> GsonFactory.getDefault().fromJson(json, actualTypeArgument));
    }

    @Override
    public JsonElement serialize(Lazy<Object> source, Type sourceType, JsonSerializationContext context) {
        Type actualTypeArgument = ((ParameterizedType) sourceType).getActualTypeArguments()[0];
        return GsonFactory.getDefault().toJsonTree(source.get(), actualTypeArgument);
    }
}