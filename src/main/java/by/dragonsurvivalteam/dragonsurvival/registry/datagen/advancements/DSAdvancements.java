package by.dragonsurvivalteam.dragonsurvival.registry.datagen.advancements;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Copies the canonical 1.21.1 advancement JSON through the data generator.
 *
 * <p>Using the 1.20 advancement builder here would rewrite holder sets, item
 * components, and icon fields into the old schema. The checked-in JSON remains
 * the source of truth so generated datapacks keep the exact 1.21.1 format.</p>
 */
public class DSAdvancements implements DataProvider {
    private static final String SOURCE_PROPERTY = "dragonsurvival.sourceResources";

    private final Path source;
    private final PackOutput.PathProvider output;

    public DSAdvancements(final PackOutput output) {
        this(
                output,
                Path.of(System.getProperty(SOURCE_PROPERTY, "src/main/resources"))
        );
    }

    DSAdvancements(final PackOutput output, final Path resourceRoot) {
        this.source = resourceRoot.resolve(Path.of(
                "data",
                DragonSurvival.MODID,
                "advancement"
        )).toAbsolutePath().normalize();
        this.output = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement");
    }

    @Override
    public CompletableFuture<?> run(final CachedOutput cache) {
        final List<Path> files;
        try (Stream<Path> paths = Files.walk(source)) {
            files = paths.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::toString))
                    .toList();
        } catch (IOException exception) {
            return CompletableFuture.failedFuture(exception);
        }

        if (files.isEmpty()) {
            return CompletableFuture.failedFuture(
                    new IOException("No canonical advancements found in " + source)
            );
        }

        CompletableFuture<?>[] writes = files.stream()
                .map(path -> write(cache, path))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(writes);
    }

    private CompletableFuture<?> write(final CachedOutput cache, final Path source) {
        try (Reader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
            JsonElement json = JsonParser.parseReader(reader);
            String relative = this.source.relativize(source).toString().replace('\\', '/');
            ResourceLocation id = DragonSurvival.res(
                    relative.substring(0, relative.length() - ".json".length())
            );
            return DataProvider.saveStable(cache, json, output.json(id));
        } catch (IOException | RuntimeException exception) {
            return CompletableFuture.failedFuture(
                    new IOException("Failed to generate advancement from " + source, exception)
            );
        }
    }

    @Override
    public String getName() {
        return "Dragon Survival 1.21.1 Advancements";
    }
}
