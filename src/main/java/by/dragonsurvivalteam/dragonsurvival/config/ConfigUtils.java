package by.dragonsurvivalteam.dragonsurvival.config;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class ConfigUtils {
    private static final int NAMESPACE = 0;
    private static final int PATH = 1;

    public static Predicate<ItemStack> itemStackPredicate(final String[] splitData) {
        boolean isTag = splitData[NAMESPACE].startsWith("#");

        ResourceLocation location = getLocation(splitData, isTag);
        if (isTag) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, location);
            return stack -> stack.is(tag);
        } else {
            Item item = BuiltInRegistries.ITEM.get(location);
            return stack -> stack.is(item);
        }
    }

    public static Predicate<Item> itemPredicate(final String[] splitData) {
        boolean isTag = splitData[NAMESPACE].startsWith("#");
        ResourceLocation location = getLocation(splitData, isTag);

        if (isTag) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, location);
            return item -> item.builtInRegistryHolder().is(tag);
        } else {
            Item itemToCheck = BuiltInRegistries.ITEM.get(location);
            return item -> item == itemToCheck;
        }
    }

    public static Predicate<BlockState> blockStatePredicate(final String[] splitData) {
        boolean isTag = splitData[NAMESPACE].startsWith("#");
        ResourceLocation location = getLocation(splitData, isTag);

        if (isTag) {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, location);
            return stack -> stack.is(tag);
        } else {
            Block block = BuiltInRegistries.BLOCK.get(location);
            return stack -> stack.is(block);
        }
    }

    public static Supplier<HolderSet<Item>> itemSupplier(final String[] splitData) {
        boolean isTag = splitData[NAMESPACE].startsWith("#");
        ResourceLocation location = getLocation(splitData, isTag);

        if (isTag) {
            TagKey<Item> tag = TagKey.create(Registries.ITEM, location);

            return () -> {
                Optional<HolderSet.Named<Item>> optional = BuiltInRegistries.ITEM.getTag(tag);
                return optional.isPresent() ? optional.get() : HolderSet.empty();
            };
        } else {
            return () -> {
                Optional<Holder.Reference<Item>> optional = BuiltInRegistries.ITEM.getHolder(location);
                return optional.isPresent() ? HolderSet.direct(optional.get()) : HolderSet.empty();
            };
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted") // ignore
    public static boolean validateResourceLocation(final String[] splitData) {
        if (splitData.length < 2) {
            return false;
        }

        String namespace = splitData[NAMESPACE].startsWith("#") ? splitData[NAMESPACE].substring(1) : splitData[NAMESPACE];
        String path = splitData[PATH];

        return ResourceLocation.tryParse(namespace + ":" + path) != null;
    }

    public static boolean validateInteger(final String string) {
        try {
            Integer.parseInt(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean validateFloat(final String string) {
        try {
            Float.parseFloat(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean validateDouble(final String string) {
        try {
            Double.parseDouble(string);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @SuppressWarnings("deprecation") // ignore
    public static String location(final Object object) {
        if (object instanceof TagKey<?> tag) {
            return "#" + tag.location();
        }

        if (object instanceof Block block) {
            return location(block.builtInRegistryHolder().getRegisteredName());
        }

        if (object instanceof Item item) {
            return location(item.builtInRegistryHolder().getRegisteredName());
        }

        if (object instanceof ResourceKey<?> key) {
            return location(key);
        }

        if (object instanceof Holder<?> holder) {
            return holder.getRegisteredName();
        }

        if (object instanceof ResourceLocation location) {
            return location.toString();
        }

        throw new IllegalArgumentException("Cannot handle [" + object.getClass().getName() + "]");
    }

    private static String location(final ResourceKey<?> key) {
        return key.location().toString();
    }

    private static ResourceLocation getLocation(final String[] splitData, boolean isTag) {
        if (isTag) {
            return DragonSurvival.location(splitData[NAMESPACE].substring(1), splitData[PATH]);
        }

        return DragonSurvival.location(splitData[NAMESPACE], splitData[PATH]);
    }
}
