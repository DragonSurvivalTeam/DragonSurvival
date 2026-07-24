package by.dragonsurvivalteam.dragonsurvival.registry;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps.RegisteredCondition;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.AndCondition;
import net.minecraftforge.common.crafting.conditions.FalseCondition;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import net.minecraftforge.common.crafting.conditions.ItemExistsCondition;
import net.minecraftforge.common.crafting.conditions.ModLoadedCondition;
import net.minecraftforge.common.crafting.conditions.NotCondition;
import net.minecraftforge.common.crafting.conditions.OrCondition;
import net.minecraftforge.common.crafting.conditions.TagEmptyCondition;
import net.minecraftforge.common.crafting.conditions.TrueCondition;

public final class DSConditions {
    public static final RegisteredCondition<DragonSpecies> CAVE_DRAGON_LOADED = new RegisteredCondition<>(BuiltInDragonSpecies.CAVE_DRAGON);
    public static final RegisteredCondition<DragonSpecies> FOREST_DRAGON_LOADED = new RegisteredCondition<>(BuiltInDragonSpecies.FOREST_DRAGON);
    public static final RegisteredCondition<DragonSpecies> SEA_DRAGON_LOADED = new RegisteredCondition<>(BuiltInDragonSpecies.SEA_DRAGON);

    private static boolean registered;

    private DSConditions() {}

    public static synchronized void register() {
        if (registered) {
            return;
        }

        registerNeoForgeAlias("and", AndCondition.Serializer.INSTANCE);
        registerNeoForgeAlias("or", OrCondition.Serializer.INSTANCE);
        registerNeoForgeAlias("not", NotCondition.Serializer.INSTANCE);
        registerNeoForgeAlias("mod_loaded", ModLoadedCondition.Serializer.INSTANCE);
        registerNeoForgeAlias("item_exists", ItemExistsCondition.Serializer.INSTANCE);
        registerNeoForgeAlias("tag_empty", TagEmptyCondition.Serializer.INSTANCE);
        registerNeoForgeAlias("true", TrueCondition.Serializer.INSTANCE);
        registerNeoForgeAlias("false", FalseCondition.Serializer.INSTANCE);
        CraftingHelper.register(RegisteredCondition.Serializer.INSTANCE);
        registered = true;
    }

    private static <T extends ICondition> void registerNeoForgeAlias(
            final String path,
            final IConditionSerializer<T> serializer
    ) {
        CraftingHelper.register(new AliasSerializer<>(new ResourceLocation("neoforge", path), serializer));
    }

    private record AliasSerializer<T extends ICondition>(
            ResourceLocation id,
            IConditionSerializer<T> delegate
    ) implements IConditionSerializer<T> {
        @Override
        public void write(final JsonObject json, final T value) {
            delegate.write(json, value);
        }

        @Override
        public T read(final JsonObject json) {
            return delegate.read(json);
        }

        @Override
        public ResourceLocation getID() {
            return id;
        }
    }
}
