package by.dragonsurvivalteam.dragonsurvival.registry.data_components;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonAbilityHolder;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonBeaconData;
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

    public static final Supplier<DataComponentType<Vector3f>> TARGET_POSITION = REGISTRY.registerComponentType("target_position", builder -> builder.persistent(ExtraCodecs.VECTOR3F).networkSynchronized(ByteBufCodecs.VECTOR3F));
    public static final Supplier<DataComponentType<SourceOfMagicData>> SOURCE_OF_MAGIC = REGISTRY.registerComponentType("source_of_magic", builder -> builder.persistent(SourceOfMagicData.CODEC).networkSynchronized(SourceOfMagicData.STREAM_CODEC));
    public static final Supplier<DataComponentType<DragonBeaconData>> DRAGON_BEACON = REGISTRY.registerComponentType("dragon_beacon_data", builder -> builder.persistent(DragonBeaconData.CODEC));
    public static final Supplier<DataComponentType<DragonAbilityHolder>> DRAGON_ABILITIES = REGISTRY.registerComponentType("dragon_abilities", builder -> builder.persistent(DragonAbilityHolder.CODEC));
    public static final Supplier<DataComponentType<DragonSoulData>> DRAGON_SOUL = REGISTRY.registerComponentType("dragon_soul", builder -> builder.persistent(DragonSoulData.CODEC));
}
