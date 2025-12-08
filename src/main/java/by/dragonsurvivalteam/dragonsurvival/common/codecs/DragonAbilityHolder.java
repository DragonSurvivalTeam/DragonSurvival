package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public record DragonAbilityHolder(List<AbilityPair> pairs, Optional<LootItemCondition> conditions, List<String> applicableSpecies) {
    @Translation(comments = "You do not meet the requirements to use this item")
    private static final String REQUIREMENTS_NOT_MET = Translation.Type.GUI.wrap("ability_holder.requirements_not_met");

    @Translation(comments = "§7■ Adds the following abilities: %s")
    private static final String ADD = Translation.Type.GUI.wrap("ability_holder.add");

    @Translation(comments = "§7■ Removes the following abilities: %s")
    private static final String REMOVE = Translation.Type.GUI.wrap("ability_holder.remove");

    @Translation(comments = "§7■ Mode: §rStrict")
    private static final String STRICT = Translation.Type.GUI.wrap("ability_holder.strict");

    @Translation(comments = "§7■ Applicable to: %s")
    private static final String APPLICABLE_TO = Translation.Type.GUI.wrap("tooltip.applicable_to");

    @Translation(comments = "")
    private static final String DIVIDER = Translation.Type.GUI.wrap("tooltip.divider");

    public static final Codec<DragonAbilityHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AbilityPair.CODEC.listOf().fieldOf("pairs").forGetter(DragonAbilityHolder::pairs),
            MiscCodecs.conditional(LootItemCondition.DIRECT_CODEC).optionalFieldOf("conditions").forGetter(DragonAbilityHolder::conditions),
            // This can be handled by the condition, but we are keeping it to make the tooltip more helpful
            ResourceLocationWrapper.validatedCodec().listOf().optionalFieldOf("applicable_species", List.of()).forGetter(DragonAbilityHolder::applicableSpecies)
    ).apply(instance, DragonAbilityHolder::new));

    public record AbilityPair(List<String> add, List<String> remove, boolean strict) {
        public static final Codec<AbilityPair> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocationWrapper.validatedCodec().listOf().fieldOf("add").forGetter(AbilityPair::add),
                ResourceLocationWrapper.validatedCodec().listOf().fieldOf("remove").forGetter(AbilityPair::remove),
                // In strict mode all removals and additions of a pair have to be successful
                Codec.BOOL.optionalFieldOf("strict", true).forGetter(AbilityPair::strict)
        ).apply(instance, AbilityPair::new));

        public Component translate(final Registry<?> registry) {
            List<MutableComponent> add = ResourceLocationWrapper.getTranslations(this.add, registry, Translation.Type.ABILITY);
            List<MutableComponent> remove = ResourceLocationWrapper.getTranslations(this.remove, registry, Translation.Type.ABILITY);

            MutableComponent translation = null;
            MutableComponent addTranslation = DSLanguageProvider.formatList(add, Function.identity());
            MutableComponent removeTranslation = DSLanguageProvider.formatList(remove, Function.identity());

            if (addTranslation.getContents() != PlainTextContents.EMPTY) {
                translation = Component.translatable(ADD, DSColors.dynamicValue(addTranslation));
            }

            if (removeTranslation.getContents() != PlainTextContents.EMPTY) {
                MutableComponent component = Component.translatable(REMOVE, DSColors.dynamicValue(removeTranslation));

                if (translation != null) {
                    translation.append(Component.literal("\n"));
                    translation.append(component);
                } else {
                    translation = component;
                }
            }

            if (strict) {
                MutableComponent component = Component.translatable(STRICT);

                if (translation != null) {
                    translation.append(Component.literal("\n"));
                    translation.append(component);
                } else {
                    translation = component;
                }
            }

            return translation;
        }
    }

    public void translate(final RegistryAccess access, final Consumer<Component> consumer) {
        Registry<DragonAbility> registry = access.registryOrThrow(DragonAbility.REGISTRY);

        for (int index = 0; index < pairs.size(); index++) {
            AbilityPair pair = pairs.get(index);
            consumer.accept(pair.translate(registry));

            if (index < pairs.size() - 1) {
                consumer.accept(Component.translatable(DIVIDER));
            }
        }

        if (!pairs.isEmpty() && !applicableSpecies.isEmpty()) {
            MutableComponent translation = DSLanguageProvider.formatList(ResourceLocationWrapper.getTranslations(applicableSpecies, registry, Translation.Type.DRAGON_SPECIES), Function.identity());
            consumer.accept(Component.empty());
            consumer.accept(Component.translatable(APPLICABLE_TO, DSColors.dynamicValue(translation)));
        }
    }

    public boolean use(final ServerPlayer player, final DragonStateHandler handler, final MagicData magic) {
        for (String resource : applicableSpecies) {
            if (!ResourceLocationWrapper.getEntries(resource, player.registryAccess().registryOrThrow(DragonSpecies.REGISTRY)).contains(handler.speciesId())) {
                player.sendSystemMessage(Component.translatable(REQUIREMENTS_NOT_MET).withStyle(ChatFormatting.RED));
                return false;
            }
        }

        if (!conditions.map(conditions -> conditions.test(Condition.entityContext(player.serverLevel(), player))).orElse(true)) {
            player.sendSystemMessage(Component.translatable(REQUIREMENTS_NOT_MET).withStyle(ChatFormatting.RED));
            return false;
        }

        Registry<DragonAbility> registry = player.registryAccess().registryOrThrow(DragonAbility.REGISTRY);
        List<ResourceKey<DragonAbility>> totalToAdd = new ArrayList<>();
        List<ResourceKey<DragonAbility>> totalToRemove = new ArrayList<>();

        for (AbilityPair pair : pairs) {
            List<ResourceKey<DragonAbility>> toAdd = new ArrayList<>();
            List<ResourceKey<DragonAbility>> toRemove = new ArrayList<>();

            if (!handle(pair.remove(), registry, toRemove::add, key -> magic.getAbility(key) != null, pair.strict())) {
                continue;
            }

            if (handle(pair.add(), registry, toAdd::add, key -> magic.getAbility(key) == null, pair.strict())) {
                totalToAdd.addAll(toAdd);
                totalToRemove.addAll(toRemove);
            }
        }

        if (totalToAdd.isEmpty() && totalToRemove.isEmpty()) {
            player.sendSystemMessage(Component.translatable(REQUIREMENTS_NOT_MET).withStyle(ChatFormatting.RED));
            return false;
        }

        // Handle removal first, allowing the addition of an ability that is also removed (for whatever reason)
        totalToRemove.forEach(key -> magic.removeAbility(player, key));
        totalToAdd.forEach(key -> registry.getHolder(key).ifPresent(ability -> magic.addAbility(player, ability)));

        return true;
    }

    private static boolean handle(
            final List<String> resources,
            final Registry<DragonAbility> registry,
            final Consumer<ResourceKey<DragonAbility>> handler,
            final Function<ResourceKey<DragonAbility>, Boolean> test,
            final boolean strict
    ) {
        for (String resource : resources) {
            Set<ResourceKey<DragonAbility>> entries = ResourceLocationWrapper.map(resource, registry);

            for (ResourceKey<DragonAbility> entry : entries) {
                if (test.apply(entry)) {
                    handler.accept(entry);
                } else if (strict) {
                    return false;
                }
            }
        }

        return true;
    }
}
