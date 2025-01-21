package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.AttributeModifierSupplier;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmoteSet;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record DragonBody(boolean isDefault, List<Modifier> modifiers, double heightMultiplier, boolean hasExtendedCrouch, boolean canHideWings, ResourceLocation model, ResourceLocation animation, List<String> bonesToHideForToggle, Holder<DragonEmoteSet> emotes, ScalingProportions scalingProportions, MountingOffsets mountingOffsets) implements AttributeModifierSupplier {
    public static final ResourceKey<Registry<DragonBody>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_bodies"));

    public static final ResourceLocation DEFAULT_MODEL = DragonSurvival.res("dragon_model");

    public static final Codec<DragonBody> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("is_default", false).forGetter(DragonBody::isDefault),
            Modifier.CODEC.listOf().fieldOf("modifiers").forGetter(DragonBody::modifiers),
            Codec.DOUBLE.optionalFieldOf("height_multiplier", 1.0).forGetter(DragonBody::heightMultiplier),
            Codec.BOOL.optionalFieldOf("has_extended_crouch", false).forGetter(DragonBody::hasExtendedCrouch),
            Codec.BOOL.optionalFieldOf("can_hide_wings", true).forGetter(DragonBody::canHideWings),
            ResourceLocation.CODEC.optionalFieldOf("model", DEFAULT_MODEL).forGetter(DragonBody::model),
            ResourceLocation.CODEC.fieldOf("animation").forGetter(DragonBody::animation),
            Codec.STRING.listOf().optionalFieldOf("bones_to_hide_for_toggle", List.of("WingLeft", "WingRight", "SmallWingLeft", "SmallWingRight")).forGetter(DragonBody::bonesToHideForToggle),
            DragonEmoteSet.CODEC.fieldOf("emotes").forGetter(DragonBody::emotes),
            ScalingProportions.CODEC.fieldOf("scaling_proportions").forGetter(DragonBody::scalingProportions),
            MountingOffsets.CODEC.fieldOf("mounting_offset").forGetter(DragonBody::mountingOffsets)

    ).apply(instance, instance.stable(DragonBody::new)));

    public static final Codec<Holder<DragonBody>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonBody>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    public static final double EXTENDED_CROUCH_HEIGHT_RATIO = 3d / 6d;
    public static final double CROUCH_HEIGHT_RATIO = 5d / 6d;

    private static final RandomSource RANDOM = RandomSource.create();

    public record ScalingProportions(double width, double height, double eyeHeight, double offset) {
        public static final Codec<ScalingProportions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("width", 1.0).forGetter(ScalingProportions::width),
                Codec.DOUBLE.optionalFieldOf("height", 1.0).forGetter(ScalingProportions::height),
                Codec.DOUBLE.optionalFieldOf("eye_height", 1.0).forGetter(ScalingProportions::eyeHeight),
                Codec.DOUBLE.optionalFieldOf("offset", 0.0).forGetter(ScalingProportions::offset)
        ).apply(instance, ScalingProportions::new));

        public static ScalingProportions of(final double width, final double height, final double eyeHeight, final double offset) {
            return new ScalingProportions(width, height, eyeHeight, offset);
        }
    }

    public record MountingOffsets(Vec3 offset, Vec3 scale) {
        public static final Codec<MountingOffsets> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3.CODEC.optionalFieldOf("offset", Vec3.ZERO).forGetter(MountingOffsets::offset),
                Vec3.CODEC.optionalFieldOf("scale", Vec3.ZERO).forGetter(MountingOffsets::scale)
        ).apply(instance, MountingOffsets::new));

        public static MountingOffsets of(final Vec3 offset, final Vec3 scale) {
            return new MountingOffsets(offset, scale);
        }
    }

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }

    public static Holder<DragonBody> random(@Nullable final HolderLookup.Provider provider, @Nullable final Holder<DragonSpecies> species) {
        List<Holder.Reference<DragonBody>> all = ResourceHelper.all(provider, REGISTRY);

        if (species == null || species.value().bodies().size() == 0) {
            all = all.stream().filter(body -> body.value().isDefault()).toList();
        } else {
            all = all.stream().filter(body -> species.value().bodies().contains(body)).toList();
        }

        return all.get(RANDOM.nextInt(all.size()));
    }

    @Override
    public ModifierType getModifierType() {
        return ModifierType.DRAGON_BODY;
    }

    public static String getWingButtonName(final Holder<DragonBody> holder) {
        //noinspection DataFlowIssue -> key is present
        return Translation.Type.BODY_WINGS.wrap(holder.getKey().location());
    }

    public static String getWingButtonDescription(final Holder<DragonBody> holder) {
        //noinspection DataFlowIssue -> key is present
        return Translation.Type.BODY_WINGS_DESCRIPTION.wrap(holder.getKey().location());
    }
}
