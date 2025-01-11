package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.registry.datagen.DSRecipes;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Only used during data generation for {@link DSRecipes.ProxyItem}*/
@Mixin(targets = "net.minecraft.core.Holder$Reference")
public interface Holder$ReferenceAccess {
    @Invoker("bindKey")
    <T> void dragonSurvival$bindKey(final ResourceKey<T> key);

    @Invoker("bindValue")
    <T> void dragonSurvival$bindValue(final T value);
}
