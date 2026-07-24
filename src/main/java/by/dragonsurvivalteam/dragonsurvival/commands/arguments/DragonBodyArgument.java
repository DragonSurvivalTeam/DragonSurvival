package by.dragonsurvivalteam.dragonsurvival.commands.arguments;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DragonBodyArgument implements ArgumentType<Holder<DragonBody>> {
    public static final String ID = "dragon_body";

    private final HolderLookup.RegistryLookup<DragonBody> lookup;
    private final CommandBuildContext context;

    public DragonBodyArgument(final CommandBuildContext context) {
        lookup = context.lookupOrThrow(DragonBody.REGISTRY);
        this.context = context;
    }

    @Override
    public @Nullable Holder<DragonBody> parse(final StringReader reader) throws CommandSyntaxException {
        Optional<Holder.Reference<DragonBody>> optional = lookup.get(ResourceKey.create(DragonBody.REGISTRY, ResourceLocation.read(reader)));
        return optional.orElse(null);
    }

    public static Holder<DragonBody> get(final CommandContext<?> context) {
        //noinspection unchecked -> type is valid
        return (Holder<DragonBody>) context.getArgument(ID, Holder.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        List<String> suggestions = new ArrayList<>();
        Holder<DragonSpecies> species = null;

        try {
            species = DragonSpeciesArgument.get(context);
        } catch (IllegalArgumentException ignored) {
            // Mixins do not apply to this library, so we cannot check properly before accessing the argument
        }

        // TODO :: is there a way to parse for a player to retrieve the species and check against that?

        if (species != null) {
            Holder<DragonSpecies> finalSpecies = species;

            this.context.lookupOrThrow(DragonBody.REGISTRY).listElements().forEach(body -> {
                if (DragonBody.bodyIsValidForSpecies(body, finalSpecies)) {
                    suggestions.add(body.getRegisteredName());
                }
            });
        } else {
            lookup.listElementIds().forEach(element -> suggestions.add(element.location().toString()));
        }

        return SharedSuggestionProvider.suggest(suggestions, builder);
    }
}
