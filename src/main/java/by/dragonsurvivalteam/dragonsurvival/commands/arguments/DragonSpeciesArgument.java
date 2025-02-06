package by.dragonsurvivalteam.dragonsurvival.commands.arguments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ResourceLocationException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DragonSpeciesArgument implements ArgumentType<Holder<DragonSpecies>> {
    public static final String ID = "dragon_species";
    public static final DragonSpecies EMPTY = new DragonSpecies(Optional.empty(), Optional.empty(), HolderSet.empty(), HolderSet.empty(), HolderSet.empty(), null);

    private static final ResourceLocation HUMAN = DragonSurvival.res("human");
    private final HolderLookup.RegistryLookup<DragonSpecies> lookup;

    public DragonSpeciesArgument(final CommandBuildContext context) {
        lookup = context.lookupOrThrow(DragonSpecies.REGISTRY);
    }

    @Override
    public @Nullable Holder<DragonSpecies> parse(final StringReader reader) throws CommandSyntaxException {
        try {
            int start = reader.getCursor();
            ResourceLocation species = ResourceLocation.read(reader);

            if (species.equals(HUMAN)) {
                return Holder.direct(EMPTY);
            }

            Optional<Holder.Reference<DragonSpecies>> optional = lookup.get(ResourceKey.create(DragonSpecies.REGISTRY, species));

            if (optional.isEmpty()) { // TODO :: do the same (error handling) for stage and body
                reader.setCursor(start);
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }

            return optional.get();
        } catch (ResourceLocationException exception) {
            // ResourceLocation#read already resets the cursor
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
        }
    }

    public static Holder<DragonSpecies> get(final CommandContext<?> context) {
        //noinspection unchecked -> type is valid
        return (Holder<DragonSpecies>) context.getArgument(ID, Holder.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        List<String> suggestions = new ArrayList<>();
        lookup.listElementIds().forEach(element -> suggestions.add(element.location().toString()));
        suggestions.add(HUMAN.toString());
        return SharedSuggestionProvider.suggest(suggestions, builder);
    }
}
