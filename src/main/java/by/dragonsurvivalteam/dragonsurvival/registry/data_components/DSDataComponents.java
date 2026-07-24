package by.dragonsurvivalteam.dragonsurvival.registry.data_components;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonAbilityHolder;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonBeaconData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.SourceOfMagicData;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class DSDataComponents {
    public static final Component<Vector3f> TARGET_POSITION = component("target_position", ExtraCodecs.VECTOR3F);
    public static final Component<SourceOfMagicData> SOURCE_OF_MAGIC = component("source_of_magic", SourceOfMagicData.CODEC);
    public static final Component<DragonBeaconData> DRAGON_BEACON = component("dragon_beacon_data", DragonBeaconData.CODEC);
    public static final Component<DragonAbilityHolder> DRAGON_ABILITIES = component("dragon_abilities", DragonAbilityHolder.CODEC);
    public static final Component<DragonSoulData> DRAGON_SOUL = component("dragon_soul", DragonSoulData.CODEC);

    private DSDataComponents() {}

    private static <T> Component<T> component(final String path, final Codec<T> codec) {
        return new Component<>(DragonSurvival.res(path), codec);
    }

    public record Component<T>(ResourceLocation id, Codec<T> codec) {
        private static final String COMPONENTS_KEY = "components";

        public boolean has(final ItemStack stack) {
            CompoundTag tag = stack.getTag();
            return tag != null && tag.getCompound(COMPONENTS_KEY).contains(id.toString());
        }

        public @Nullable T get(final ItemStack stack) {
            return get(stack, NbtOps.INSTANCE);
        }

        public @Nullable T get(final ItemStack stack, final HolderLookup.Provider provider) {
            return get(stack, RegistryOps.create(NbtOps.INSTANCE, provider));
        }

        public T getOrDefault(final ItemStack stack, final T fallback) {
            T value = get(stack);
            return value != null ? value : fallback;
        }

        public void set(final ItemStack stack, final T value) {
            set(stack, value, NbtOps.INSTANCE);
        }

        public void set(final ItemStack stack, final T value, final HolderLookup.Provider provider) {
            set(stack, value, RegistryOps.create(NbtOps.INSTANCE, provider));
        }

        public void remove(final ItemStack stack) {
            CompoundTag root = stack.getTag();
            if (root == null || !root.contains(COMPONENTS_KEY, Tag.TAG_COMPOUND)) {
                return;
            }

            CompoundTag components = root.getCompound(COMPONENTS_KEY);
            components.remove(id.toString());
            if (components.isEmpty()) {
                root.remove(COMPONENTS_KEY);
            }
        }

        public @Nullable T decode(final Tag tag) {
            return decode(NbtOps.INSTANCE, tag);
        }

        public Tag encode(final T value) {
            return encode(NbtOps.INSTANCE, value);
        }

        private @Nullable T get(final ItemStack stack, final DynamicOps<Tag> ops) {
            CompoundTag root = stack.getTag();
            if (root == null) {
                return null;
            }

            CompoundTag components = root.getCompound(COMPONENTS_KEY);
            Tag encoded = components.get(id.toString());
            return encoded != null ? decode(ops, encoded) : null;
        }

        private void set(final ItemStack stack, final T value, final DynamicOps<Tag> ops) {
            CompoundTag root = stack.getOrCreateTag();
            CompoundTag components = root.getCompound(COMPONENTS_KEY);
            components.put(id.toString(), encode(ops, value));
            root.put(COMPONENTS_KEY, components);
        }

        private @Nullable T decode(final DynamicOps<Tag> ops, final Tag tag) {
            return codec.parse(ops, tag)
                    .resultOrPartial(message -> DragonSurvival.LOGGER.error(
                            "Unable to decode item component {}: {}", id, message
                    ))
                    .orElse(null);
        }

        private Tag encode(final DynamicOps<Tag> ops, final T value) {
            return codec.encodeStart(ops, value).getOrThrow(
                    false,
                    message -> DragonSurvival.LOGGER.error("Unable to encode item component {}: {}", id, message)
            );
        }
    }
}
