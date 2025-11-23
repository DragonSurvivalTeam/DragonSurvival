package by.dragonsurvivalteam.dragonsurvival.mixins.curios;

// @Mixin(GuiEventHandler.class)
public abstract class GuiEventHandlerMixin {
    /* TODO :: mixinextras 0.5.0 / neoforge has some issue with classloaders
    @Definition(id = "screen", local = @Local(type = Screen.class, name = "screen"))
    @Definition(id = "InventoryScreen", type = InventoryScreen.class)
    @Expression("screen instanceof InventoryScreen")
    @ModifyExpressionValue(method = "onInventoryGuiInit", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean dragonSurvival$addCuriosButtonToDragonInventory(final boolean original, @Local(name = "screen") final Screen screen) {
        if (screen instanceof DragonInventoryScreen) {
            return true;

        }

        return original;
    }
    */
}
