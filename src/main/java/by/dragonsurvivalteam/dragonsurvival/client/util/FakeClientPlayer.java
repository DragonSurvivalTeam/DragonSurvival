package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animation.AnimationController;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.Supplier;

public class FakeClientPlayer extends AbstractClientPlayer {
    public final int number;

    public DragonStateHandler handler = new DragonStateHandler();
    public Supplier<String> animationSupplier = null;
    public AnimationController<DragonEntity> animationController = null;
    public Long lastAccessed;

    public FakeClientPlayer(int number) {
        //noinspection DataFlowIssue -> level is expected to be present
        super(Minecraft.getInstance().level, new GameProfile(UUID.randomUUID(), "FAKE_PLAYER_" + number));
        this.number = number;
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return true;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return true;
    }

    @Override
    public void tick() {
    }

    @Override
    public void die(@NotNull DamageSource source) {
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
    }

    @Override
    public boolean isInvulnerableTo(@NotNull DamageSource source) {
        return true;
    }

    @Override
    public boolean canHarmPlayer(@NotNull Player player) {
        return false;
    }

    @Override
    public void displayClientMessage(@NotNull Component chatComponent, boolean actionBar) {
    }

    @Override
    public void awardStat(@NotNull Stat par1StatBase, int par2) {
    }

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.empty();
    }

    @Override
    public boolean saveAsPassenger(@NotNull CompoundTag pCompound) {
        return false;
    }

    @Override
    public boolean save(@NotNull CompoundTag pCompound) {
        return false;
    }

    @Override
    @Nullable public MinecraftServer getServer() {
        return Minecraft.getInstance().getSingleplayerServer();
    }

    @Override
    public @NotNull Vec3 position() {
        return new Vec3(0, 0, 0);
    }

    @Override
    public @NotNull BlockPos blockPosition() {
        return BlockPos.ZERO;
    }

    @Override
    public void onAddedToLevel() {
    }
}