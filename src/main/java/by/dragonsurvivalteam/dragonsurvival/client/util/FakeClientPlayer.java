package by.dragonsurvivalteam.dragonsurvival.client.util;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import com.geckolib.animation.AnimationController;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Supplier;

public class FakeClientPlayer extends AbstractClientPlayer {
    public final int number;
    public boolean useVisualScale;

    public double scale = -1;

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
    public float getScale() {
        if (scale == -1) {
            return super.getScale();
        }

        return (float) scale;
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
    public void tick() {}

    @Override
    public void die(@NotNull DamageSource source) {}

    @Override
    public void readAdditionalSaveData(@NotNull ValueInput valueInput) {}

    @Override
    public void addAdditionalSaveData(@NotNull ValueOutput valueOutput) {}

    @Override
    public boolean canHarmPlayer(@NotNull Player player) {
        return false;
    }

    @Override
    public void sendSystemMessage(@NotNull Component chatComponent) {
    }

    @Override
    public void awardStat(@NotNull Stat par1StatBase, int par2) {}

    @Override
    public boolean shouldShowName() {
        return false;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.empty();
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
