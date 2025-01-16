package by.dragonsurvivalteam.dragonsurvival.registry.projectile.entity_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;

public record ProjectileEntityRunFunctionEffect(ResourceLocation function) implements ProjectileEntityEffect {
    public static final MapCodec<ProjectileEntityRunFunctionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("function").forGetter(ProjectileEntityRunFunctionEffect::function)
    ).apply(instance, ProjectileEntityRunFunctionEffect::new));

    @Override
    public void apply(Projectile projectile, Entity target, int level) {
        ServerLevel serverLevel = (ServerLevel)projectile.level();
        MinecraftServer server = serverLevel.getServer();
        ServerFunctionManager manager = server.getFunctions();

        manager.get(function).ifPresent(source -> {
            CommandSourceStack stack = server.createCommandSourceStack()
                    .withPermission(Commands.LEVEL_GAMEMASTERS)
                    .withSuppressedOutput()
                    .withEntity(target)
                    .withLevel(serverLevel)
                    .withPosition(target.position())
                    .withRotation(target.getRotationVector());

            manager.execute(source, stack);
        });
    }

    @Override
    public MapCodec<? extends ProjectileEntityEffect> codec() {
        return CODEC;
    }
}
