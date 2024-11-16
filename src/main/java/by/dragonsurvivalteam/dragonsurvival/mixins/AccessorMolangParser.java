package by.dragonsurvivalteam.dragonsurvival.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import software.bernie.geckolib3.core.molang.LazyVariable;
import software.bernie.geckolib3.core.molang.MolangParser;

@Mixin(MolangParser.class)
public interface AccessorMolangParser {
    @Invoker( "getVariable" )
    LazyVariable dragonSurvival$getVariable(String name);
}
