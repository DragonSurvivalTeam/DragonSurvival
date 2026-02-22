package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DSEffectTags extends KeyTagProvider<MobEffect> {
    public static final TagKey<MobEffect> OVERWHELMING_MIGHT_BLACKLIST = key("overwhelming_might_blacklist");
    public static final TagKey<MobEffect> UNBREAKABLE_SPIRIT_BLACKLIST = key("unbreakable_spirit_blacklist");

    public DSEffectTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider) {
        super(output, Registries.MOB_EFFECT, provider, DragonSurvival.MODID);
    }

    @Override
    protected void addTags(@NotNull final HolderLookup.Provider provider) {
        tag(OVERWHELMING_MIGHT_BLACKLIST);
        tag(UNBREAKABLE_SPIRIT_BLACKLIST);
    }

    private static TagKey<MobEffect> key(@NotNull final String path) {
        return TagKey.create(Registries.MOB_EFFECT, DragonSurvival.res(path));
    }
}
