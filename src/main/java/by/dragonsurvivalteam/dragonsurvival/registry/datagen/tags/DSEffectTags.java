package by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DSEffectTags extends TagsProvider<MobEffect> {
    public static final TagKey<MobEffect> OVERWHELMING_MIGHT_BLACKLIST = key("overwhelming_might_blacklist");
    public static final TagKey<MobEffect> UNBREAKABLE_SPIRIT_BLACKLIST = key("unbreakable_spirit_blacklist");

    public DSEffectTags(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider, @Nullable final ExistingFileHelper helper) {
        super(output, Registries.MOB_EFFECT, provider, DragonSurvival.MODID, helper);
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
