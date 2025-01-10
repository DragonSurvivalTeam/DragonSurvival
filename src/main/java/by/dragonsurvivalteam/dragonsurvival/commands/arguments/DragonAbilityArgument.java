package by.dragonsurvivalteam.dragonsurvival.commands.arguments;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;

public class DragonAbilityArgument implements ArgumentType<Holder<DragonAbility>> {
    public static final String ID = "dragon_ability";

    private final HolderLookup.RegistryLookup<DragonAbility> lookup;

    public DragonAbilityArgument(final CommandBuildContext context) {
        lookup = context.lookupOrThrow(DragonAbility.REGISTRY);
    }

    @Override
    public @Nullable Holder<DragonAbility> parse(final StringReader reader) throws CommandSyntaxException {
        Optional<Holder.Reference<DragonAbility>> optional = lookup.get(ResourceKey.create(DragonAbility.REGISTRY, ResourceLocation.read(reader)));
        return optional.orElse(null);
    }

    public static Holder<DragonAbility> get(final CommandContext<?> context) {
        //noinspection unchecked -> type is valid
        return (Holder<DragonAbility>) context.getArgument(ID, Holder.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        List<String> suggestions = new ArrayList<>();
        lookup.listElementIds().forEach(element -> suggestions.add(element.location().toString()));
        return SharedSuggestionProvider.suggest(suggestions, builder);
    }
}
