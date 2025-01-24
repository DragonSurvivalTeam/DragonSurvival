package by.dragonsurvivalteam.dragonsurvival.compat.jei;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static IJeiRuntime runtime;

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MODID, "fix");
    }

    @Override
    public void registerRecipeTransferHandlers(final IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new DragonInventoryGUIHandler());
    }

    @Override
    public void registerGuiHandlers(final IGuiHandlerRegistration registration) {
        registration.addGlobalGuiHandler(new ModifierRenderingGUIHandler());
        registration.addGuiContainerHandler(DragonInventoryScreen.class, new DragonInventoryGUIHandler());
    }

    @Override
    public void onRuntimeAvailable(@NotNull final IJeiRuntime runtime) {
        JEIPlugin.runtime = runtime;
    }

    public static boolean handleKeyPress(final InputConstants.Key key, final ItemStack stack) {
        RecipeIngredientRole role = null;

        if (JEIPlugin.runtime.getKeyMappings().getShowUses().isActiveAndMatches(key)) {
            role = RecipeIngredientRole.INPUT;
        } else if (JEIPlugin.runtime.getKeyMappings().getShowRecipe().isActiveAndMatches(key)) {
            role = RecipeIngredientRole.OUTPUT;
        }

        if (role == null) {
            return false;
        }

        ITypedIngredient<ItemStack> ingredient = JEIPlugin.runtime.getIngredientManager().createTypedIngredient(VanillaTypes.ITEM_STACK, stack).orElse(null);

        if (ingredient == null) {
            return false;
        }

        IFocusFactory factory = JEIPlugin.runtime.getJeiHelpers().getFocusFactory();
        IFocus<ItemStack> focus = factory.createFocus(role, ingredient);
        JEIPlugin.runtime.getRecipesGui().show(focus);

        return true;
    }
}