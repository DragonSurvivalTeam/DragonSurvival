package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Condition;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.UnlockableBehavior;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record DragonBody(
        boolean isDefault,
        Optional<UnlockableBehavior> unlockableBehavior,
        List<Modifier> modifiers,
        boolean canHideWings,
        ResourceLocation model,
        TextureSize textureSize,
        ResourceLocation animation,
        Optional<ResourceLocation> defaultIcon,
        List<String> bonesToHideForToggle,
        Holder<DragonEmoteSet> emotes,
        ScalingProportions scalingProportions,
        double crouchHeightRatio,
        Optional<MountingOffsets> mountingOffsets,
        Optional<BackpackOffsets> backpackOffsets,
        double betterCombatWeaponOffset
) implements AttributeModifierSupplier {
    public static final ResourceKey<Registry<DragonBody>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_body"));
    public static final ResourceLocation DEFAULT_MODEL = DragonSurvival.res("dragon_model");

    public static final Codec<DragonBody> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("is_default", false).forGetter(DragonBody::isDefault),
            UnlockableBehavior.CODEC.optionalFieldOf("unlockable_behavior").forGetter(DragonBody::unlockableBehavior),
            Modifier.CODEC.listOf().fieldOf("modifiers").forGetter(DragonBody::modifiers),
            Codec.BOOL.optionalFieldOf("can_hide_wings", true).forGetter(DragonBody::canHideWings),
            ResourceLocation.CODEC.optionalFieldOf("model", DEFAULT_MODEL).forGetter(DragonBody::model),
            TextureSize.CODEC.optionalFieldOf("texture_size", new TextureSize(512, 512)).forGetter(DragonBody::textureSize),
            ResourceLocation.CODEC.fieldOf("animation").forGetter(DragonBody::animation),
            ResourceLocation.CODEC.optionalFieldOf("default_icon").forGetter(DragonBody::defaultIcon),
            Codec.STRING.listOf().optionalFieldOf("bones_to_hide_for_toggle", List.of("WingLeft", "WingRight", "SmallWingLeft", "SmallWingRight")).forGetter(DragonBody::bonesToHideForToggle),
            DragonEmoteSet.CODEC.fieldOf("emotes").forGetter(DragonBody::emotes),
            ScalingProportions.CODEC.fieldOf("scaling_proportions").forGetter(DragonBody::scalingProportions),
            MiscCodecs.doubleRange(0, 1).fieldOf("crouch_height_ratio").forGetter(DragonBody::crouchHeightRatio),
            MountingOffsets.CODEC.optionalFieldOf("mounting_offset").forGetter(DragonBody::mountingOffsets),
            BackpackOffsets.CODEC.optionalFieldOf("backpack_offset").forGetter(DragonBody::backpackOffsets),
            Codec.DOUBLE.optionalFieldOf("bettercombat_weapon_offset", 0d).forGetter(DragonBody::betterCombatWeaponOffset)
    ).apply(instance, instance.stable(DragonBody::new)));

    public static final Codec<Holder<DragonBody>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonBody>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    private static final RandomSource RANDOM = RandomSource.create();

    public record TextureSize(int width, int height) {
        public static final Codec<TextureSize> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("width").forGetter(TextureSize::width),
                Codec.INT.fieldOf("height").forGetter(TextureSize::height)
        ).apply(instance, TextureSize::new));
    }

    public record ScalingProportions(double width, double height, double eyeHeight, double scaleMultiplier, double shadowMultiplier) { // TODO :: scaling_offset
        public static final Codec<ScalingProportions> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                MiscCodecs.doubleRange(0, Double.MAX_VALUE).fieldOf("width").forGetter(ScalingProportions::width),
                MiscCodecs.doubleRange(0, Double.MAX_VALUE).fieldOf("height").forGetter(ScalingProportions::height),
                MiscCodecs.doubleRange(0, Double.MAX_VALUE).fieldOf("eye_height").forGetter(ScalingProportions::eyeHeight),
                MiscCodecs.doubleRange(0, Double.MAX_VALUE).optionalFieldOf("scale_multiplier", 1.0).forGetter(ScalingProportions::scaleMultiplier),
                MiscCodecs.doubleRange(0, Double.MAX_VALUE).optionalFieldOf("shadow_multiplier", 1.0).forGetter(ScalingProportions::shadowMultiplier)
        ).apply(instance, ScalingProportions::new));

        public static ScalingProportions of(final double width, final double height, final double eyeHeight, final double offset, final double shadowOffset) {
            return new ScalingProportions(width, height, eyeHeight, offset, shadowOffset);
        }
    }

    public record MountingOffsets(Vec3 humanOffset, Vec3 dragonOffset, Vec3 scale) {
        public static final Codec<MountingOffsets> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3.CODEC.optionalFieldOf("human_offset", Vec3.ZERO).forGetter(MountingOffsets::humanOffset),
                Vec3.CODEC.optionalFieldOf("dragon_offset", Vec3.ZERO).forGetter(MountingOffsets::dragonOffset),
                Vec3.CODEC.optionalFieldOf("offset_per_scale_above_one", Vec3.ZERO).forGetter(MountingOffsets::scale)
        ).apply(instance, MountingOffsets::new));

        public static MountingOffsets of(final Vec3 humanOffset, final Vec3 dragonOffset, final Vec3 scale) {
            return new MountingOffsets(humanOffset, dragonOffset, scale);
        }
    }

    public record BackpackOffsets(Vec3 posOffset, Vec3 rotOffset, Vec3 scale) {
        public static final Codec<BackpackOffsets> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Vec3.CODEC.optionalFieldOf("position_offset", Vec3.ZERO).forGetter(BackpackOffsets::posOffset),
                Vec3.CODEC.optionalFieldOf("rotation_offset", Vec3.ZERO).forGetter(BackpackOffsets::rotOffset),
                Vec3.CODEC.optionalFieldOf("scale", new Vec3(1, 1, 1)).forGetter(BackpackOffsets::scale)
        ).apply(instance, BackpackOffsets::new));

        public static BackpackOffsets of(final Vec3 pos_offset, final Vec3 rot_offset, final Vec3 scale) {
            return new BackpackOffsets(pos_offset, rot_offset, scale);
        }
    }

    /**
     * Returns the list of relevant bodies for the player <br>
     * It may contain locked bodies, depending on how the bodies' altar visibility is configured
     */
    public static List<UnlockableBehavior.BodyEntry> getBodies(final ServerPlayer player, boolean isEditor) {
        List<UnlockableBehavior.BodyEntry> entries = new ArrayList<>();

        ResourceHelper.all(player.registryAccess(), REGISTRY).forEach(body -> {
            UnlockableBehavior behaviour = body.value().unlockableBehavior().orElse(null);

            if (behaviour == null) {
                entries.add(new UnlockableBehavior.BodyEntry(body, true));
                return;
            }

            boolean isUnlocked = behaviour.unlockCondition().map(condition -> condition.test(Condition.entityContext(player.serverLevel(), player))).orElse(true);
            UnlockableBehavior.Visibility visibility = behaviour.visibility().orElse(null);

            if (isEditor) {
                if (visibility == UnlockableBehavior.Visibility.ALWAYS_VISIBLE) {
                    entries.add(new UnlockableBehavior.BodyEntry(body, isUnlocked));
                    return;
                }

                if (visibility == UnlockableBehavior.Visibility.ALWAYS_HIDDEN) {
                    return;
                }
            }

            if (isUnlocked) {
                entries.add(new UnlockableBehavior.BodyEntry(body, true));
                return;
            }

            if (isEditor && visibility == UnlockableBehavior.Visibility.VISIBLE_IF_LOCKED) {
                entries.add(new UnlockableBehavior.BodyEntry(body, false));
            }
        });

        return entries;
    }

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }

    public static boolean bodyIsValidForSpecies(final Holder<DragonBody> body, @Nullable Holder<DragonSpecies> species) {
        if (species == null || species.value().bodies().size() == 0) {
            return body.value().isDefault();
        } else {
            return species.value().bodies().contains(body);
        }
    }

    public static Holder<DragonBody> getRandomUnlocked(final ServerPlayer player) {
        return getRandomUnlocked(DragonStateProvider.getData(player).species(), getBodies(player, false));
    }

    public static Holder<DragonBody> getRandomUnlocked(@Nullable final Holder<DragonSpecies> species, List<UnlockableBehavior.BodyEntry> unlockedBodies) {
        List<Holder<DragonBody>> validBodiesForSpecies = unlockedBodies.stream().filter(bodyEntry -> bodyIsValidForSpecies(bodyEntry.body(), species) && bodyEntry.isUnlocked()).map(UnlockableBehavior.BodyEntry::body).toList();
        return validBodiesForSpecies.get(RANDOM.nextInt(validBodiesForSpecies.size()));
    }

    public static Holder<DragonBody> getRandom(@Nullable final HolderLookup.Provider provider, @Nullable final Holder<DragonSpecies> species) {
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
