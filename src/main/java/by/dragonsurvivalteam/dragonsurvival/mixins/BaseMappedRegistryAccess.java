package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.BaseMappedRegistry;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

// TODO :: Remove when https://github.com/neoforged/NeoForge/issues/1867 is fixed
@Mixin(BaseMappedRegistry.class)
public interface BaseMappedRegistryAccess<T> {
    @Accessor("dataMaps")
    Map<DataMapType<T, ?>, Map<ResourceKey<T>, ?>> dragonSurvival$getDataMaps();
}
