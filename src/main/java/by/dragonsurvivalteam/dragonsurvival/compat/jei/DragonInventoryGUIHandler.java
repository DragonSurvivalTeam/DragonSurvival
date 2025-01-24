package by.dragonsurvivalteam.dragonsurvival.compat.jei;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonInventoryScreen;
import by.dragonsurvivalteam.dragonsurvival.server.containers.DragonContainer;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class DragonInventoryGUIHandler implements IRecipeTransferInfo<DragonContainer, RecipeHolder<CraftingRecipe>>, IGuiContainerHandler<DragonInventoryScreen> {
    @Override
    public @NotNull Class<? extends DragonContainer> getContainerClass() {
        return DragonContainer.class;
    }

    @Override
    public @NotNull Optional<MenuType<DragonContainer>> getMenuType() {
        return Optional.empty();
    }

    @Override
    public @NotNull RecipeType<RecipeHolder<CraftingRecipe>> getRecipeType() {
        return RecipeTypes.CRAFTING;
    }


    @Override
    public boolean canHandle(@NotNull DragonContainer container, @NotNull RecipeHolder<CraftingRecipe> recipe) {
        return true;
    }

    @Override
    public @NotNull List<Slot> getRecipeSlots(@NotNull DragonContainer container, @NotNull RecipeHolder<CraftingRecipe> recipe) {
        return container.craftingSlots;
    }

    @Override
    public @NotNull List<Slot> getInventorySlots(@NotNull DragonContainer container, @NotNull RecipeHolder<CraftingRecipe> recipe) {
        return container.inventorySlots;
    }
}