package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AttributeMap.class)
public interface AttributeMapAccessor {
    @Accessor("attributes")
    Map<Attribute, AttributeInstance> dragonSurvival$getAttributes();
}
