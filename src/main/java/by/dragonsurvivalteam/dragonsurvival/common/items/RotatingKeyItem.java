package by.dragonsurvivalteam.dragonsurvival.common.items;

import by.dragonsurvivalteam.dragonsurvival.client.render.item.RotatingKeyRenderer;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import com.mojang.datafixers.util.Pair;
import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.PlayState;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class RotatingKeyItem extends TooltipItem implements GeoItem {
    private static final float NO_TARGET_THRESHOLD = 0.01f;

    public final Identifier texture, model;
    private final TagKey<Structure> target;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    public final RawAnimation NO_TARGET = RawAnimation.begin().thenPlay("no_target");

    public RotatingKeyItem(Properties properties, Identifier model, Identifier texture, Identifier target) {
        super(properties, null);
        this.target = TagKey.create(Registries.STRUCTURE, target);
        this.model = model;
        this.texture = texture;
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private RotatingKeyRenderer renderer;

            @Override
            public @Nullable GeoItemRenderer<?> getGeoItemRenderer() {
                if (renderer == null) {
                    renderer = new RotatingKeyRenderer();
                }

                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("rotating_key_controller", 10, state -> {
            boolean hasTarget = state.getDataOrDefault(RotatingKeyRenderer.HAS_TARGET, false);

            return state.setAndContinue(hasTarget ? IDLE : NO_TARGET);
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel serverLevel, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, serverLevel, entity, slot);

        if (!(entity instanceof Player player) || (player.getMainHandItem() != stack && player.getOffhandItem() != stack)) {
            return;
        }

        // TODO :: as long as the player is within a certain distance of the current target don't re-check
        if (serverLevel.getGameTime() % 20 == 0) {
            Optional<Vector3f> targetPosition = findTargetPosition(serverLevel, player.blockPosition());

            if (targetPosition.isPresent()) {
                stack.set(DSDataComponents.TARGET_POSITION, targetPosition.get());
            } else {
                stack.set(DSDataComponents.TARGET_POSITION, new Vector3f());
            }
        }
    }

    private Optional<Vector3f> findTargetPosition(final ServerLevel serverLevel, final BlockPos searchOrigin) {
        Optional<HolderSet.Named<Structure>> structures = serverLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(this.target);

        if (structures.isEmpty()) {
            return Optional.empty();
        }

        HolderSet.Named<Structure> targetStructures = structures.get();
        StructureStart currentStructure = serverLevel.structureManager().getStructureWithPieceAt(searchOrigin, targetStructures);

        if (isValidTarget(currentStructure)) {
            return Optional.of(currentStructure.getBoundingBox().getCenter().getCenter().toVector3f());
        }

        Pair<BlockPos, Holder<Structure>> nearest = serverLevel.getChunkSource().getGenerator().findNearestMapStructure(serverLevel, targetStructures, searchOrigin, 25, false);

        if (nearest == null) {
            return Optional.empty();
        }

        StructureStart nearestStructure = serverLevel.structureManager().getStructureWithPieceAt(nearest.getFirst(), nearest.getSecond().value());

        if (!isValidTarget(nearestStructure)) {
            return Optional.empty();
        }

        return Optional.of(nearestStructure.getBoundingBox().getCenter().getCenter().toVector3f());
    }

    private static boolean isValidTarget(@Nullable final StructureStart structureStart) {
        return structureStart != null && structureStart.isValid();
    }

    public static boolean hasTarget(@Nullable final Vector3fc target) {
        if (target == null) {
            return false;
        }

        return target.x() * target.x() + target.y() * target.y() + target.z() * target.z() >= NO_TARGET_THRESHOLD;
    }
}
