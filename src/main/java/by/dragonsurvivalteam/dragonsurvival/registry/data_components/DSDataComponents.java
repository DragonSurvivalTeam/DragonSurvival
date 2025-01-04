package by.dragonsurvivalteam.dragonsurvival.registry.data_components;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.SourceOfMagicData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class DSDataComponents {
    public static final DeferredRegister.DataComponents REGISTRY = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, DragonSurvival.MODID);

    public static Supplier<DataComponentType<Vector3f>> TARGET_POSITION = REGISTRY.registerComponentType("target_position", builder -> builder.persistent(ExtraCodecs.VECTOR3F).networkSynchronized(ByteBufCodecs.VECTOR3F));
    public static Supplier<DataComponentType<SourceOfMagicData>> SOURCE_OF_MAGIC = REGISTRY.registerComponentType("source_of_magic", builder -> builder.persistent(SourceOfMagicData.CODEC).networkSynchronized(SourceOfMagicData.STREAM_CODEC));
}
