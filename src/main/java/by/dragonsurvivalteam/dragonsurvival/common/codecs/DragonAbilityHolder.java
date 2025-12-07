package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

public record DragonAbilityHolder(List<String> abilities, Optional<LootItemCondition> conditions, Optional<HolderSet<DragonSpecies>> applicableSpecies, boolean isRemoval) {
    @Translation(comments = "You do not match the requirements to use this item")
    private static final String REQUIREMENTS_NOT_MET = Translation.Type.GUI.wrap("ability_holder.requirements_not_met");

    @Translation(comments = "You already know all the abilities this item can grant you")
    private static final String ALREADY_KNOWN = Translation.Type.GUI.wrap("ability_holder.already_known");

    @Translation(comments = "You do not know any abilities this item would remove")
    private static final String NOT_KNOWN = Translation.Type.GUI.wrap("ability_holder.not_known");

    @Translation(comments = "%s of the stored %s abilities were granted to you")
    private static final String ADDED = Translation.Type.GUI.wrap("ability_holder.added");

    @Translation(comments = "%s of the stored %s abilities were removed from you")
    private static final String REMOVED = Translation.Type.GUI.wrap("ability_holder.removed");

    public static final Codec<DragonAbilityHolder> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocationWrapper.validatedCodec().listOf().fieldOf("abilities").forGetter(DragonAbilityHolder::abilities),
            MiscCodecs.conditional(LootItemCondition.DIRECT_CODEC).optionalFieldOf("conditions").forGetter(DragonAbilityHolder::conditions),
            // This can be handled by the condition, but we are keeping it to make the tooltip more helpful
            RegistryCodecs.homogeneousList(DragonSpecies.REGISTRY).optionalFieldOf("applicable_species").forGetter(DragonAbilityHolder::applicableSpecies),
            Codec.BOOL.optionalFieldOf("is_removal", false).forGetter(DragonAbilityHolder::isRemoval)
    ).apply(instance, DragonAbilityHolder::new));

    public boolean use(final ServerPlayer player, final DragonStateHandler handler, final MagicData magic) {
        if (applicableSpecies.map(set -> !set.contains(handler.species())).orElse(false)) {
            player.sendSystemMessage(Component.translatable(REQUIREMENTS_NOT_MET).withStyle(ChatFormatting.RED));
            return false;
        }

        if (!conditions.map(conditions -> conditions.test(Condition.entityContext(player.serverLevel(), player))).orElse(true)) {
            player.sendSystemMessage(Component.translatable(REQUIREMENTS_NOT_MET).withStyle(ChatFormatting.RED));
            return false;
        }

        int total = 0;
        int count = 0;

        Registry<DragonAbility> registry = player.registryAccess().registryOrThrow(DragonAbility.REGISTRY);

        for (String resource : abilities) {
            Set<ResourceKey<DragonAbility>> entries = ResourceLocationWrapper.map(resource, registry);

            for (ResourceKey<DragonAbility> key : entries) {
                total++;

                DragonAbilityInstance current = magic.getAbility(key);

                if (isRemoval && current == null || !isRemoval && current != null) {
                    continue;
                }

                if (isRemoval) {
                    if (magic.removeAbility(player, key)) {
                        count++;
                    }
                } else {
                    if (magic.addAbility(player, registry.getHolderOrThrow(key))) {
                        count++;
                    }
                }
            }
        }


        if (count == 0) {
            player.sendSystemMessage(Component.translatable(isRemoval ? NOT_KNOWN : ALREADY_KNOWN).withStyle(ChatFormatting.RED));
        } else {
            player.sendSystemMessage(Component.translatable(isRemoval ? REMOVED : ADDED, DSColors.dynamicValue(count), DSColors.dynamicValue(total)));
        }

        return count > 0;
    }
}
