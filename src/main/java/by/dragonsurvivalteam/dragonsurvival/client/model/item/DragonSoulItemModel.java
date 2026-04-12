package by.dragonsurvivalteam.dragonsurvival.client.model.item;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.loaders.CustomSoulIconLoader;
import by.dragonsurvivalteam.dragonsurvival.common.items.DragonSoulItem;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;

public class DragonSoulItemModel implements ItemModel {
    public static final Identifier ID = DragonSurvival.res("dragon_soul");

    private final ItemModel fallback;
    private final Map<Identifier, ItemModel> models;

    public DragonSoulItemModel(final ItemModel fallback, final Map<Identifier, ItemModel> models) {
        this.fallback = fallback;
        this.models = models;
    }

    @Override
    public void update(
        final ItemStackRenderState output,
        final ItemStack item,
        final ItemModelResolver resolver,
        final ItemDisplayContext displayContext,
        final @Nullable ClientLevel level,
        final @Nullable ItemOwner owner,
        final int seed
    ) {
        if (item.getItem() instanceof DragonSoulItem soul) {
            HolderLookup.Provider access = null;

            if (level != null) {
                access = level.registryAccess();
            } else {
                LivingEntity entity = owner != null ? owner.asLivingEntity() : null;

                if (entity != null) {
                    access = entity.registryAccess();
                }
            }

            ResourceKey<DragonSpecies> species = soul.getSpecies(item, access);

            if (species != null) {
                Identifier modelId = CustomSoulIconLoader.getIcon(species, soul.getStage(item, access));

                if (modelId != null) {
                    ItemModel model = models.get(modelId);

                    if (model != null) {
                        model.update(output, item, resolver, displayContext, level, owner, seed);
                        return;
                    }
                }
            }
        }

        fallback.update(output, item, resolver, displayContext, level, owner, seed);
    }

    public record Unbaked(Optional<ItemModel.Unbaked> fallback) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemModels.CODEC.optionalFieldOf("fallback").forGetter(Unbaked::fallback)
        ).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(final ItemModel.BakingContext context, final Matrix4fc transformation) {
            CustomSoulIconLoader.reloadFromGameResources();

            ItemModel bakedFallback = fallback
                    .<ItemModel>map(model -> model.bake(context, transformation))
                    .orElseGet(() -> context.missingItemModel(transformation));

            Map<Identifier, ItemModel> models = new HashMap<>();

            for (Identifier modelId : CustomSoulIconLoader.getModels()) {
                ItemModel model = new CuboidItemModelWrapper.Unbaked(modelId, Optional.empty(), List.of()).bake(context, transformation);
                models.put(modelId, model);
            }

            return new DragonSoulItemModel(bakedFallback, Map.copyOf(models));
        }

        @Override
        public void resolveDependencies(final ResolvableModel.Resolver resolver) {
            CustomSoulIconLoader.reloadFromGameResources();
            fallback.ifPresent(model -> model.resolveDependencies(resolver));
            CustomSoulIconLoader.getModels().forEach(resolver::markDependency);
        }
    }
}
