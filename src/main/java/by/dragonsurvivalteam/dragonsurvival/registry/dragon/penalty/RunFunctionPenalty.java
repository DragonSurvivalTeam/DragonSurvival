package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;

public record RunFunctionPenalty(Identifier function) implements PenaltyEffect {
    public static final MapCodec<RunFunctionPenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("function").forGetter(RunFunctionPenalty::function)
    ).apply(instance, RunFunctionPenalty::new));

    @Override
    public void apply(final ServerPlayer player, final Holder<DragonPenalty> penalty) {
        MinecraftServer server = player.level().getServer();
        ServerFunctionManager manager = server.getFunctions();

        manager.get(function).ifPresent(source -> {
            CommandSourceStack stack = server.createCommandSourceStack()
                    .withPermission(LevelBasedPermissionSet.GAMEMASTER)
                    .withSuppressedOutput()
                    .withEntity(player)
                    .withLevel(player.level())
                    .withPosition(player.position())
                    .withRotation(player.getRotationVector());

            manager.execute(source, stack);
        });
    }

    @Override
    public MapCodec<? extends PenaltyEffect> codec() {
        return CODEC;
    }
}
