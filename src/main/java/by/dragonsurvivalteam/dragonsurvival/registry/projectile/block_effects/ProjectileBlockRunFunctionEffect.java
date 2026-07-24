package by.dragonsurvivalteam.dragonsurvival.registry.projectile.block_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;

public record ProjectileBlockRunFunctionEffect(ResourceLocation function) implements ProjectileBlockEffect {
    public static final MapCodec<ProjectileBlockRunFunctionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("function").forGetter(ProjectileBlockRunFunctionEffect::function)
    ).apply(instance, ProjectileBlockRunFunctionEffect::new));

    @Override
    public void apply(Projectile projectile, BlockPos target, int level) {
        ServerLevel serverLevel = (ServerLevel) projectile.level();
        MinecraftServer server = serverLevel.getServer();
        ServerFunctionManager manager = server.getFunctions();

        manager.get(function).ifPresent(source -> {
            CommandSourceStack stack = server.createCommandSourceStack()
                    .withPermission(Commands.LEVEL_GAMEMASTERS)
                    .withSuppressedOutput()
                    .withLevel(serverLevel)
                    .withPosition(target.getCenter());

            manager.execute(source, stack);
        });
    }

    @Override
    public MapCodec<? extends ProjectileBlockEffect> codec() {
        return CODEC;
    }
}
