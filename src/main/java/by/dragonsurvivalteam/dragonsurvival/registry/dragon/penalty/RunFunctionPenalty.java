package by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerPlayer;

public record RunFunctionPenalty(ResourceLocation function) implements PenaltyEffect {
    public static final MapCodec<RunFunctionPenalty> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("function").forGetter(RunFunctionPenalty::function)
    ).apply(instance, RunFunctionPenalty::new));

    @Override
    public void apply(final ServerPlayer player, final Holder<DragonPenalty> penalty) {
        MinecraftServer server = player.serverLevel().getServer();
        ServerFunctionManager manager = server.getFunctions();

        manager.get(function).ifPresent(source -> {
            CommandSourceStack stack = server.createCommandSourceStack()
                    .withPermission(Commands.LEVEL_GAMEMASTERS)
                    .withSuppressedOutput()
                    .withEntity(player)
                    .withLevel(player.serverLevel())
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
