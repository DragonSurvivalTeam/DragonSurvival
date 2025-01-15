package by.dragonsurvivalteam.dragonsurvival.commands.arguments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.HolderLookup;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DragonSizeArgument implements ArgumentType<Double> {
    public static final String ID = "dragon_size";

    private final HolderLookup.RegistryLookup<DragonStage> lookup;

    public DragonSizeArgument(final CommandBuildContext context) {
        lookup = context.lookupOrThrow(DragonStage.REGISTRY);
    }

    @Override
    public @Nullable Double parse(final StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        double size = reader.readDouble();
        MiscCodecs.Bounds bounds = DragonStage.getBounds();

        if (size < bounds.min()) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooLow().createWithContext(reader, size, bounds.min());
        }

        if (size > bounds.max()) {
            reader.setCursor(start);
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.doubleTooHigh().createWithContext(reader, size, bounds.max());
        }

        return size;
    }

    public static Double get(final CommandContext<?> context) {
        return context.getArgument(ID, Double.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        List<String> suggestions = new ArrayList<>();
        lookup.listElements().forEach(element -> suggestions.add(String.valueOf(element.value().sizeRange().min())));
        return SharedSuggestionProvider.suggest(suggestions, builder);
    }
}
