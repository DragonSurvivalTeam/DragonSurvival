package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public record EndPlatform(ResourceLocation structure, BlockPos spawnPosition) {
    public static final Codec<EndPlatform> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("structure").forGetter(EndPlatform::structure),
            BlockPos.CODEC.fieldOf("spawn_position").forGetter(EndPlatform::spawnPosition)
    ).apply(instance, EndPlatform::new));

    public static EndPlatform from(final String path, final int x, final int y, final int z) {
        return from(DragonSurvival.res(path), x, y, z);
    }

    public static EndPlatform from(final ResourceLocation structure, final int x, final int y, final int z) {
        return new EndPlatform(structure, new BlockPos(x, y, z));
    }
}
