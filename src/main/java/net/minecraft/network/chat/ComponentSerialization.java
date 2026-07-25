package net.minecraft.network.chat;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

public final class ComponentSerialization {
    public static final Codec<Component> CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
        JsonElement json = dynamic.convert(JsonOps.INSTANCE).getValue();

        try {
            return DataResult.success(Component.Serializer.fromJson(json));
        } catch (JsonParseException exception) {
            return DataResult.error(exception::getMessage);
        }
    }, component -> new Dynamic<>(JsonOps.INSTANCE, Component.Serializer.toJsonTree(component)));

    private ComponentSerialization() {
    }
}
