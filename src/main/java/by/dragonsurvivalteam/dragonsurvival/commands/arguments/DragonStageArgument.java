package by.dragonsurvivalteam.dragonsurvival.commands.arguments;

import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class DragonStageArgument implements ArgumentType<Holder<DragonStage>> {
    public static final String ID = "dragon_stage";

    private final HolderLookup.RegistryLookup<DragonStage> lookup;

    public DragonStageArgument(final CommandBuildContext context) {
        lookup = context.lookupOrThrow(DragonStage.REGISTRY);
    }

    @Override
    public @Nullable Holder<DragonStage> parse(final StringReader reader) throws CommandSyntaxException {
        Optional<Holder.Reference<DragonStage>> optional = lookup.get(ResourceKey.create(DragonStage.REGISTRY, ResourceLocation.read(reader)));
        return optional.orElse(null);
    }

    public static Holder<DragonStage> get(final CommandContext<?> context) {
        //noinspection unchecked -> type is valid
        return (Holder<DragonStage>) context.getArgument(ID, Holder.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        List<String> suggestions = new ArrayList<>();
        lookup.listElementIds().forEach(element -> suggestions.add(element.location().toString()));
        return SharedSuggestionProvider.suggest(suggestions, builder);
    }
}
