package by.dragonsurvivalteam.dragonsurvival.mixins;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ApplyMixinPlugin implements IMixinConfigPlugin {
    private final static String PREFIX = ApplyMixinPlugin.class.getPackageName() + ".";

    // If there are other mods for which the mixins also need to apply to (e.g. forks)
    private final static Map<String, String> ALIAS = Map.of(
            "sodium", "embeddium",
            "iris", "oculus"
    );

    @Override
    public void onLoad(final String mixinPackage) { /* Nothing to do */ }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(final String targetClassName, final String mixinClassName) {
        String directory = mixinClassName.replace(PREFIX, "");
        // Remove directories which are not related to mods
        directory = directory.replace("client.", "");
        directory = directory.replace("tool_swap.", "");
        // If a directory is still present it will run through the check below
        String[] elements = directory.split("\\.");

        if (elements.length == 2) {
            String modid = elements[0];
            boolean isPresent = LoadingModList.get().getModFileById(modid) != null;

            if (!isPresent && ALIAS.containsKey(modid)) {
                isPresent = LoadingModList.get().getModFileById(ALIAS.get(modid)) != null;
            }

            return isPresent;
        }

        if (mixinClassName.equals(PREFIX + "client.MinecraftMixin")) {
            // Iris doesn't properly work with Fabulous! mode (the translucency sorting feature) (it causes particles to be invisible)
            return LoadingModList.get().getModFileById("iris") == null || LoadingModList.get().getModFileById(ALIAS.get("iris")) == null;
        }

        if (mixinClassName.equals(PREFIX + "HolderSetCodecMixin") || mixinClassName.equals(PREFIX + "Holder$ReferenceAccess")) {
            // In production, this system property doesn't exist. We don't ever use this mixin in production anyways, so we can just return false in this case.
            if(FMLLoader.isProduction()) {
                return false;
            } else {
                return System.getProperty("dragonsurvival.data_generation").equals("true");
            }
        }

        return true;
    }

    @Override
    public void acceptTargets(final Set<String> myTargets, final Set<String> otherTargets) { /* Nothing to do */ }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) { /* Nothing to do */ }

    @Override
    public void postApply(final String targetClassName, final ClassNode targetClass, final String mixinClassName, final IMixinInfo mixinInfo) { /* Nothing to do */ }
}
