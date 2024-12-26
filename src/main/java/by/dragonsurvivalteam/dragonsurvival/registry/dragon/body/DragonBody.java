package by.dragonsurvivalteam.dragonsurvival.registry.dragon.body;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.Modifier;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.ModifierType;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.AttributeModifierSupplier;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.emotes.DragonEmoteSet;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public record DragonBody(List<Modifier> modifiers, double heightMultiplier, boolean hasExtendedCrouch, boolean canHideWings, ResourceLocation customModel, List<String> bonesToHideForToggle, Holder<DragonEmoteSet> emotes) implements AttributeModifierSupplier {
    public static final ResourceKey<Registry<DragonBody>> REGISTRY = ResourceKey.createRegistryKey(DragonSurvival.res("dragon_bodies"));

    public static final ResourceLocation DEFAULT_MODEL = DragonSurvival.res("geo/dragon_model.geo.json");

    public static final Codec<DragonBody> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Modifier.CODEC.listOf().fieldOf("modifiers").forGetter(DragonBody::modifiers),
            Codec.DOUBLE.optionalFieldOf("height_multiplier", 1.0).forGetter(DragonBody::heightMultiplier),
            Codec.BOOL.optionalFieldOf("has_extended_crouch", false).forGetter(DragonBody::hasExtendedCrouch),
            Codec.BOOL.optionalFieldOf("can_hide_wings", true).forGetter(DragonBody::canHideWings),
            ResourceLocation.CODEC.optionalFieldOf("custom_model", DEFAULT_MODEL).forGetter(DragonBody::customModel),
            Codec.STRING.listOf().optionalFieldOf("bones_to_hide_for_toggle", List.of("WingLeft", "WingRight", "SmallWingLeft", "SmallWingRight")).forGetter(DragonBody::bonesToHideForToggle),
            DragonEmoteSet.CODEC.fieldOf("emotes").forGetter(DragonBody::emotes)
    ).apply(instance, instance.stable(DragonBody::new)));

    public static final Codec<Holder<DragonBody>> CODEC = RegistryFixedCodec.create(REGISTRY);
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<DragonBody>> STREAM_CODEC = ByteBufCodecs.holderRegistry(REGISTRY);

    private static final RandomSource RANDOM = RandomSource.create();

    @SubscribeEvent
    public static void register(final DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(REGISTRY, DIRECT_CODEC, DIRECT_CODEC);
    }

    public static Holder<DragonBody> random(@Nullable final HolderLookup.Provider provider) {
        HolderLookup.RegistryLookup<DragonBody> registry;

        if (provider == null) {
            registry = CommonHooks.resolveLookup(REGISTRY);
        } else {
            registry = provider.lookupOrThrow(REGISTRY);
        }

        //noinspection DataFlowIssue -> registry expected to be present
        Object[] bodies = registry.listElements().toArray();

        if (bodies.length == 0) {
            throw new IllegalStateException("There are no registered dragon bodies");
        }

        //noinspection unchecked -> cast is okay
        return (Holder<DragonBody>) bodies[RANDOM.nextInt(bodies.length)];
    }

    @Override
    public ModifierType getModifierType() {
        return ModifierType.DRAGON_BODY;
    }

    public static String getWingButtonName(Holder<DragonBody> holder) {
        return Translation.Type.BODY_WINGS.wrap(holder.getKey().location().getNamespace(), holder.getKey().location().getPath());
    }

    public static String getWingButtonDescription(Holder<DragonBody> holder) {
        return Translation.Type.BODY_WINGS_DESCRIPTION.wrap(holder.getKey().location().getNamespace(), holder.getKey().location().getPath());
    }
}
