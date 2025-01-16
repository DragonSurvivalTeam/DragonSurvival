package by.dragonsurvivalteam.dragonsurvival.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.neoforged.neoforge.registries.DeferredRegister;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@SuppressWarnings("unused")
public class DSMapDecorationTypes {
    public static final DeferredRegister<MapDecorationType> REGISTRY = DeferredRegister.create(BuiltInRegistries.MAP_DECORATION_TYPE, MODID);

    public static final Holder<MapDecorationType> DRAGON_HUNTER = REGISTRY.register("dragon_hunter", () -> new MapDecorationType(
            ResourceLocation.fromNamespaceAndPath(MODID, "dragon_hunter"), true, MapColor.COLOR_RED.col, true, true
    ));
}
