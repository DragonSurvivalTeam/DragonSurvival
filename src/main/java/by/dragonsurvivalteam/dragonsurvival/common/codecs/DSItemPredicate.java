package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ItemLike;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Keeps the 1.21 item-predicate JSON contract while evaluating against 1.20 item stacks.
 */
public final class DSItemPredicate implements Predicate<ItemStack> {
    public static final Codec<DSItemPredicate> CODEC = Codec.PASSTHROUGH.comapFlatMap(dynamic -> {
        JsonElement element = dynamic.convert(JsonOps.INSTANCE).getValue();
        return element.isJsonObject()
                ? DataResult.success(new DSItemPredicate(element.getAsJsonObject()))
                : DataResult.error(() -> "Item predicate must be a JSON object");
    }, predicate -> new Dynamic<>(JsonOps.INSTANCE, predicate.json.deepCopy()));

    private final JsonObject json;

    public DSItemPredicate(final JsonObject json) {
        this.json = json.deepCopy();
    }

    public static DSItemPredicate of(final ItemLike... items) {
        JsonObject json = new JsonObject();

        if (items.length == 1) {
            json.addProperty("items", BuiltInRegistries.ITEM.getKey(items[0].asItem()).toString());
        } else {
            JsonArray values = new JsonArray();

            for (ItemLike item : items) {
                values.add(BuiltInRegistries.ITEM.getKey(item.asItem()).toString());
            }

            json.add("items", values);
        }

        return new DSItemPredicate(json);
    }

    public static DSItemPredicate of(final TagKey<Item> tag) {
        JsonObject json = new JsonObject();
        json.addProperty("items", "#" + tag.location());
        return new DSItemPredicate(json);
    }

    public static DSItemPredicate potions(final Potion... potions) {
        JsonObject json = new JsonObject();
        JsonObject predicates = new JsonObject();

        if (potions.length == 1) {
            predicates.addProperty("minecraft:potion_contents", BuiltInRegistries.POTION.getKey(potions[0]).toString());
        } else {
            JsonArray values = new JsonArray();

            for (Potion potion : potions) {
                values.add(BuiltInRegistries.POTION.getKey(potion).toString());
            }

            predicates.add("minecraft:potion_contents", values);
        }

        json.add("predicates", predicates);
        return new DSItemPredicate(json);
    }

    @Override
    public boolean test(final ItemStack stack) {
        if (json.has("items") && !matchesItemSelector(stack, json.get("items"))) {
            return false;
        }

        if (json.has("count") && !matchesIntRange(stack.getCount(), json.get("count"))) {
            return false;
        }

        if (json.has("components") && !matchesComponents(stack, json.getAsJsonObject("components"))) {
            return false;
        }

        return !json.has("predicates") || matchesSubPredicates(stack, json.getAsJsonObject("predicates"));
    }

    private static boolean matchesItemSelector(final ItemStack stack, final JsonElement selector) {
        if (selector.isJsonArray()) {
            for (JsonElement entry : selector.getAsJsonArray()) {
                if (matchesItemSelector(stack, entry)) {
                    return true;
                }
            }

            return false;
        }

        if (!selector.isJsonPrimitive() || !selector.getAsJsonPrimitive().isString()) {
            return false;
        }

        String value = selector.getAsString();

        if (value.startsWith("#")) {
            ResourceLocation location = ResourceLocation.tryParse(value.substring(1));
            return location != null && stack.is(TagKey.create(Registries.ITEM, location));
        }

        ResourceLocation location = ResourceLocation.tryParse(value);
        return location != null && BuiltInRegistries.ITEM.getOptional(location).map(stack::is).orElse(false);
    }

    private static boolean matchesSubPredicates(final ItemStack stack, final JsonObject predicates) {
        for (Map.Entry<String, JsonElement> entry : predicates.entrySet()) {
            boolean matches = switch (entry.getKey()) {
                case "minecraft:potion_contents" -> matchesPotion(stack, entry.getValue());
                case "minecraft:damage" -> matchesDamage(stack, entry.getValue());
                case "minecraft:enchantments", "minecraft:stored_enchantments" -> matchesEnchantments(stack, entry.getValue());
                case "minecraft:custom_data" -> matchesCustomData(stack, entry.getValue());
                default -> false;
            };

            if (!matches) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchesPotion(final ItemStack stack, final JsonElement selector) {
        ResourceLocation potion = BuiltInRegistries.POTION.getKey(PotionUtils.getPotion(stack));
        return matchesIdSelector(potion, selector);
    }

    private static boolean matchesDamage(final ItemStack stack, final JsonElement value) {
        if (!value.isJsonObject()) {
            return false;
        }

        JsonObject damage = value.getAsJsonObject();
        return (!damage.has("damage") || matchesIntRange(stack.getDamageValue(), damage.get("damage")))
                && (!damage.has("durability") || matchesIntRange(stack.getMaxDamage() - stack.getDamageValue(), damage.get("durability")));
    }

    private static boolean matchesEnchantments(final ItemStack stack, final JsonElement value) {
        if (!value.isJsonArray()) {
            return false;
        }

        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

        for (JsonElement element : value.getAsJsonArray()) {
            if (!element.isJsonObject()) {
                return false;
            }

            JsonObject predicate = element.getAsJsonObject();
            boolean matched = false;

            for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
                ResourceLocation id = BuiltInRegistries.ENCHANTMENT.getKey(enchantment.getKey());
                boolean selected = !predicate.has("enchantments") || matchesIdSelector(id, predicate.get("enchantments"));
                boolean levelMatches = !predicate.has("levels") || matchesIntRange(enchantment.getValue(), predicate.get("levels"));

                if (selected && levelMatches) {
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchesCustomData(final ItemStack stack, final JsonElement value) {
        try {
            CompoundTag expected = TagParser.parseTag(value.isJsonPrimitive() ? value.getAsString() : value.toString());
            return stack.getTag() != null && NbtUtils.compareNbt(expected, stack.getTag(), true);
        } catch (CommandSyntaxException exception) {
            return false;
        }
    }

    private static boolean matchesComponents(final ItemStack stack, final JsonObject components) {
        for (Map.Entry<String, JsonElement> entry : components.entrySet()) {
            JsonElement expected = entry.getValue();
            boolean matches = switch (entry.getKey()) {
                case "minecraft:damage" -> expected.isJsonPrimitive() && expected.getAsInt() == stack.getDamageValue();
                case "minecraft:max_damage" -> expected.isJsonPrimitive() && expected.getAsInt() == stack.getMaxDamage();
                case "minecraft:repair_cost" -> expected.isJsonPrimitive() && expected.getAsInt() == stack.getBaseRepairCost();
                case "minecraft:custom_model_data" -> expected.isJsonPrimitive()
                        && stack.hasTag()
                        && stack.getTag().contains("CustomModelData", 3)
                        && stack.getTag().getInt("CustomModelData") == expected.getAsInt();
                case "minecraft:unbreakable" -> stack.hasTag()
                        && stack.getTag().contains("Unbreakable", 1)
                        && stack.getTag().getBoolean("Unbreakable");
                case "minecraft:potion_contents" -> {
                    JsonElement potion = expected.isJsonObject() && expected.getAsJsonObject().has("potion")
                            ? expected.getAsJsonObject().get("potion")
                            : expected;
                    yield matchesPotion(stack, potion);
                }
                case "minecraft:custom_data" -> matchesCustomData(stack, expected);
                default -> false;
            };

            if (!matches) {
                return false;
            }
        }

        return true;
    }

    private static boolean matchesIdSelector(final ResourceLocation actual, final JsonElement selector) {
        if (selector.isJsonArray()) {
            for (JsonElement entry : selector.getAsJsonArray()) {
                if (matchesIdSelector(actual, entry)) {
                    return true;
                }
            }

            return false;
        }

        return selector.isJsonPrimitive() && selector.getAsJsonPrimitive().isString()
                && actual.toString().equals(selector.getAsString());
    }

    private static boolean matchesIntRange(final int value, final JsonElement range) {
        if (range.isJsonPrimitive() && range.getAsJsonPrimitive().isNumber()) {
            return value == range.getAsInt();
        }

        if (!range.isJsonObject()) {
            return false;
        }

        JsonObject bounds = range.getAsJsonObject();
        return (!bounds.has("min") || value >= bounds.get("min").getAsInt())
                && (!bounds.has("max") || value <= bounds.get("max").getAsInt());
    }
}
