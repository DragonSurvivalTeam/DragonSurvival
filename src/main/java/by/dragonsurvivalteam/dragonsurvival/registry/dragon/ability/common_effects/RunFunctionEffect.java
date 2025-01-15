package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.common_effects;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.block_effects.AbilityBlockEffect;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.AbilityEntityEffect;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerFunctionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.effects.RunFunction;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.List;

/** See {@link RunFunction} */
public record RunFunctionEffect(ResourceLocation function) implements AbilityEntityEffect, AbilityBlockEffect {
    public static final MapCodec<RunFunctionEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("function").forGetter(RunFunctionEffect::function)
    ).apply(instance, RunFunctionEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        MinecraftServer server = dragon.serverLevel().getServer();
        ServerFunctionManager manager = server.getFunctions();

        manager.get(function).ifPresent(source -> {
            CommandSourceStack stack = server.createCommandSourceStack()
                    .withPermission(Commands.LEVEL_GAMEMASTERS)
                    .withSuppressedOutput()
                    .withEntity(target)
                    .withLevel(dragon.serverLevel())
                    .withPosition(target.position())
                    .withRotation(target.getRotationVector());

            manager.execute(source, stack);
        });
    }

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final BlockPos position, @Nullable final Direction direction) {
        MinecraftServer server = dragon.serverLevel().getServer();
        ServerFunctionManager manager = server.getFunctions();

        manager.get(function).ifPresent(source -> {
            CommandSourceStack stack = server.createCommandSourceStack()
                    .withPermission(Commands.LEVEL_GAMEMASTERS)
                    .withSuppressedOutput()
                    .withLevel(dragon.serverLevel())
                    .withPosition(position.getCenter());

            if (direction != null) {
                Vector3f rotation = new Vector3f();
                new Matrix4f().set(direction.getRotation()).getEulerAnglesZYX(rotation);
                stack.withRotation(new Vec2(rotation.x(), rotation.y()));
            }

            manager.execute(source, stack);
        });
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        return List.of();
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }

    @Override
    public MapCodec<? extends AbilityBlockEffect> blockCodec() {
        return CODEC;
    }
}
