package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public record DragonAbilityHolder(List<AbilityPair> abilities, Optional<LootItemCondition> conditions, Optional<HolderSet<DragonSpecies>> applicableSpecies) {
    @Translation(comments = "You do not meet the requirements to use this item")
    private static final String REQUIREMENTS_NOT_MET = Translation.Type.GUI.wrap("ability_holder.requirements_not_met");

    public static final Codec<DragonAbilityHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            AbilityPair.CODEC.listOf().fieldOf("abilities").forGetter(DragonAbilityHolder::abilities),
            MiscCodecs.conditional(LootItemCondition.DIRECT_CODEC).optionalFieldOf("conditions").forGetter(DragonAbilityHolder::conditions),
            // This can be handled by the condition, but we are keeping it to make the tooltip more helpful
            RegistryCodecs.homogeneousList(DragonSpecies.REGISTRY).optionalFieldOf("applicable_species").forGetter(DragonAbilityHolder::applicableSpecies)
    ).apply(instance, DragonAbilityHolder::new));

    public record AbilityPair(List<String> add, List<String> remove, boolean strict) {
        public static final Codec<AbilityPair> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocationWrapper.validatedCodec().listOf().fieldOf("add").forGetter(AbilityPair::add),
                ResourceLocationWrapper.validatedCodec().listOf().fieldOf("remove").forGetter(AbilityPair::remove),
                // In strict mode all removals and additions of a pair have to be successful
                Codec.BOOL.optionalFieldOf("strict", true).forGetter(AbilityPair::strict)
        ).apply(instance, AbilityPair::new));
    }

    public boolean use(final ServerPlayer player, final DragonStateHandler handler, final MagicData magic) {
        if (applicableSpecies.map(set -> !set.contains(handler.species())).orElse(false)) {
            player.sendSystemMessage(Component.translatable(REQUIREMENTS_NOT_MET).withStyle(ChatFormatting.RED));
            return false;
        }

        if (!conditions.map(conditions -> conditions.test(Condition.entityContext(player.serverLevel(), player))).orElse(true)) {
            player.sendSystemMessage(Component.translatable(REQUIREMENTS_NOT_MET).withStyle(ChatFormatting.RED));
            return false;
        }

        Registry<DragonAbility> registry = player.registryAccess().registryOrThrow(DragonAbility.REGISTRY);
        List<ResourceKey<DragonAbility>> totalToAdd = new ArrayList<>();
        List<ResourceKey<DragonAbility>> totalToRemove = new ArrayList<>();

        for (AbilityPair pair : abilities) {
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
