package by.dragonsurvivalteam.dragonsurvival.mixins;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(LocationPredicate.class)
public abstract class LocationPredicateMixin {
    @Unique
    private List<String> dragonSurvival$biomes = List.of();
    @Unique
    private List<String> dragonSurvival$structures = List.of();
    @Unique
    private Boolean dragonSurvival$canSeeSky;

    @Inject(method = "fromJson", at = @At("RETURN"))
    private static void dragonSurvival$readModernLocation(
            final JsonElement json,
            final CallbackInfoReturnable<LocationPredicate> callback
    ) {
        if (json == null || !json.isJsonObject() || callback.getReturnValue() == LocationPredicate.ANY) {
            return;
        }

        JsonObject predicate = json.getAsJsonObject();
        LocationPredicateMixin result = (LocationPredicateMixin) (Object) callback.getReturnValue();
        result.dragonSurvival$biomes = dragonSurvival$values(predicate.get("biomes"));
        result.dragonSurvival$structures = dragonSurvival$values(predicate.get("structures"));
        if (predicate.has("can_see_sky")) {
            result.dragonSurvival$canSeeSky = predicate.get("can_see_sky").getAsBoolean();
        }
    }

    @Inject(method = "matches", at = @At("RETURN"), cancellable = true)
    private void dragonSurvival$matchModernLocation(
            final ServerLevel level,
            final double x,
            final double y,
            final double z,
            final CallbackInfoReturnable<Boolean> callback
    ) {
        if (!callback.getReturnValue()) {
            return;
        }

        List<String> biomes = dragonSurvival$biomes == null ? List.of() : dragonSurvival$biomes;
        List<String> structures = dragonSurvival$structures == null ? List.of() : dragonSurvival$structures;
        BlockPos pos = BlockPos.containing(x, y, z);
        if ((!biomes.isEmpty()
                || !structures.isEmpty()
                || dragonSurvival$canSeeSky != null)
                && !level.isLoaded(pos)) {
            callback.setReturnValue(false);
            return;
        }

        if (!biomes.isEmpty() && !dragonSurvival$matchesBiome(level.getBiome(pos), biomes)) {
            callback.setReturnValue(false);
            return;
        }
        if (!structures.isEmpty() && !dragonSurvival$matchesStructure(level, pos, structures)) {
            callback.setReturnValue(false);
            return;
        }
        if (dragonSurvival$canSeeSky != null && level.canSeeSky(pos) != dragonSurvival$canSeeSky) {
            callback.setReturnValue(false);
        }
    }

    @Unique
    private boolean dragonSurvival$matchesBiome(final Holder<Biome> biome, final List<String> biomes) {
        for (String value : biomes) {
            ResourceLocation id = new ResourceLocation(dragonSurvival$withoutTag(value));
            if (value.startsWith("#") && biome.is(TagKey.create(Registries.BIOME, id))) {
                return true;
            }
            if (!value.startsWith("#") && biome.is(ResourceKey.create(Registries.BIOME, id))) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private boolean dragonSurvival$matchesStructure(final ServerLevel level, final BlockPos pos, final List<String> structures) {
        for (String value : structures) {
            ResourceLocation id = new ResourceLocation(dragonSurvival$withoutTag(value));
            boolean matches = value.startsWith("#")
                    ? level.structureManager()
                            .getStructureWithPieceAt(pos, TagKey.create(Registries.STRUCTURE, id))
                            .isValid()
                    : level.structureManager()
                            .getStructureWithPieceAt(pos, ResourceKey.create(Registries.STRUCTURE, id))
                            .isValid();
            if (matches) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private static List<String> dragonSurvival$values(final JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return List.of();
        }
        if (element.isJsonPrimitive()) {
            return List.of(element.getAsString());
        }
        if (!element.isJsonArray()) {
            return List.of();
        }

        List<String> values = new ArrayList<>();
        JsonArray array = element.getAsJsonArray();
        array.forEach(value -> {
            if (value.isJsonPrimitive()) {
                values.add(value.getAsString());
            }
        });
        return List.copyOf(values);
    }

    @Unique
    private static String dragonSurvival$withoutTag(final String value) {
        return value.startsWith("#") ? value.substring(1) : value;
    }
}
