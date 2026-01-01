package by.dragonsurvivalteam.dragonsurvival.util.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.util.Lazy;

final public class GsonFactory {
    private static final Gson GSON = newBuilder().create();

    public static Gson getDefault() {
        return GSON;
    }

    public static GsonBuilder newBuilder() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Lazy.class, new LazyJsonAdapter());
        builder.registerTypeAdapter(Identifier.class, new IdentifierAdapter());
        return builder;
    }
}
