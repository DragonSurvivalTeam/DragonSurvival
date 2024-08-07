package by.dragonsurvivalteam.dragonsurvival.registry;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod.MODID;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DSAttributes {
    public static final DeferredRegister<Attribute> DS_ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, MODID);
    public static final Holder<Attribute> FLIGHT_STAMINA_COST = DS_ATTRIBUTES.register("flight_stamina_cost", () -> new RangedAttribute("ds.attribute.flight_stamina", 1.0D, 0.0D, 5.0D).setSyncable(true));
}
