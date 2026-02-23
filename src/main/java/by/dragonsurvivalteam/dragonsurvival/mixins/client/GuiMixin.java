package by.dragonsurvivalteam.dragonsurvival.mixins.client;



// FIXME
//@Mixin(Gui.class)
public abstract class GuiMixin {
//    @Unique private List<ClientEffectProvider> dragonSurvival$providers = List.of();
//
//    @Inject(method = "renderEffects", at = @At("HEAD"))
//    private void dragonSurvival$storeProviders(final GuiGraphics graphics, final DeltaTracker deltaTracker, final CallbackInfo callback) {
//        dragonSurvival$providers = ClientEffectProvider.getProviders(false);
//    }
//
//    @ModifyExpressionValue(method = "renderEffects", at = @At(value = "INVOKE", target = "Ljava/util/Collection;isEmpty()Z"))
//    private boolean dragonSurvival$considerClientEffectsForIsEmpty(boolean isEmpty) {
//        return isEmpty && dragonSurvival$providers.isEmpty();
//    }
//
//    // TODO :: Do we care to determine if effects are beneficial or not? In this UI vanilla puts harmful effects below beneficial ones instead of beside them
//    @Inject(method = "renderEffects", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
//    private void dragonSurvival$renderAbilityEffects(final GuiGraphics graphics, final DeltaTracker deltaTracker, final CallbackInfo callback, @Local(ordinal = 0) int renderedCount) {
//        Gui self = (Gui) (Object) this;
//        int effectCount = renderedCount;
//
//        for (ClientEffectProvider provider : dragonSurvival$providers) {
//            int xPos = graphics.guiWidth();
//            int yPos = 1;
//
//            if (Minecraft.getInstance().isDemo()) {
//                yPos += 15;
//            }
//
//            effectCount++;
//            xPos -= 25 * effectCount;
//            graphics.blitSprite(((GuiAccessor) self).dragonSurvival$getEffectBackgroundSprite(), xPos, yPos, 24, 24);
//
//            float alpha = 1;
//
//            if (!provider.isInfiniteDuration() && provider.currentDuration() < 200) {
//                int duration = (int) (10 - Functions.ticksToSeconds(provider.currentDuration()));
//
//                alpha = Mth.clamp((float) provider.currentDuration() / 10 / 5 * 0.5f, 0, 0.5f)
//                        + Mth.cos((float) provider.currentDuration() * (float) Math.PI / 5) * Mth.clamp((float) duration / 10 * 0.25f, 0, 0.25f);
//            }
//
//            graphics.setColor(1, 1, 1, alpha);
//            graphics.blit(provider.clientData().texture(), xPos + 3, yPos + 3, 0, 0, 0, 18, 18, 18, 18);
//            graphics.setColor(1, 1, 1, 1);
//        }
//    }
//
//    @Unique private static @Nullable Identifier dragonSurvival$getSpriteForAirBubble(boolean burst) {
//        Player player = Minecraft.getInstance().player;
//
//        //noinspection DataFlowIssue -> player is present
//        FluidType previousFluidType = SwimData.getData(player).previousFluid;
//        FluidType currentFluidType = player.getEyeInFluidType();
//
//        Identifier replacementSprite;
//        FluidType relevantFluid;
//
//        if (NeoForgeMod.EMPTY_TYPE.getKey() == SwimData.key(currentFluidType)) {
//            relevantFluid = previousFluidType;
//        } else {
//            relevantFluid = currentFluidType;
//        }
//
//        if (burst) {
//            replacementSprite = SwimData.getAirBurstSprite(relevantFluid);
//        } else {
//            replacementSprite = SwimData.getAirSprite(relevantFluid);
//        }
//
//        if (replacementSprite == null) {
//            return null;
//        }
//
//        TextureAtlasSprite sprite = Minecraft.getInstance().getGuiSprites().getSprite(replacementSprite);
//
//        if (sprite.contents().name() != MissingTextureAtlasSprite.getLocation()) {
//            return replacementSprite;
//        }
//
//        return null;
//    }
//
//    @ModifyArg(method = "renderAirLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 0))
//    private Identifier dragonSurvival$modifyAirSprite(Identifier sprite) {
//        Identifier replacementSprite = dragonSurvival$getSpriteForAirBubble(false);
//
//        if (replacementSprite != null) {
//            return replacementSprite;
//        } else {
//            return sprite;
//        }
//    }
//
//    @ModifyArg(method = "renderAirLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/Identifier;IIII)V", ordinal = 1))
//    private Identifier dragonSurvival$modifyAirBurstSprite(Identifier sprite) {
//        Identifier replacementSprite = dragonSurvival$getSpriteForAirBubble(true);
//
//        if (replacementSprite != null) {
//            return replacementSprite;
//        } else {
//            return sprite;
//        }
//    }
}
