package by.dragonsurvivalteam.dragonsurvival.registry.projectile.world_effects;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.world.entity.projectile.Projectile;

public record ProjectileWorldRunFunctionEffect(Identifier function) implements ProjectileWorldEffect {
    public static final MapCodec<ProjectileWorldRunFunctionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("function").forGetter(ProjectileWorldRunFunctionEffect::function)
    ).apply(instance, ProjectileWorldRunFunctionEffect::new));

    @Override
    public void apply(final Projectile projectile, final Void target, int level) {
        ServerLevel serverLevel = (ServerLevel) projectile.level();
        MinecraftServer server = serverLevel.getServer();
        ServerFunctionManager manager = server.getFunctions();

        manager.get(function).ifPresent(source -> {
            CommandSourceStack stack = server.createCommandSourceStack()
                    .withPermission(LevelBasedPermissionSet.GAMEMASTER)
                    .withSuppressedOutput()
                    .withEntity(projectile)
                    .withLevel(serverLevel)
                    .withPosition(projectile.position())
                    .withRotation(projectile.getRotationVector());

            manager.execute(source, stack);
        });
    }

    @Override
    public MapCodec<? extends ProjectileWorldEffect> codec() {
        return CODEC;
    }
}
