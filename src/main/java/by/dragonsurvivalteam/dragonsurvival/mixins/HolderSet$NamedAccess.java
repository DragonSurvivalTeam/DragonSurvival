package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(HolderSet.Named.class)
public interface HolderSet$NamedAccess<T> {
    @Invoker("contents")
    List<Holder<T>> dragonSurvival$contents();
}
