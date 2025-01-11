package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Unique private List<ClientEffectProvider> dragonSurvival$providers = List.of();

    @Inject(method = "renderEffects", at = @At("HEAD"))
    private void dragonSurvival$storeProviders(final GuiGraphics graphics, final DeltaTracker deltaTracker, final CallbackInfo callback) {
        dragonSurvival$providers = ClientEffectProvider.getProviders();
    }

    @ModifyExpressionValue(method = "renderEffects", at = @At(value = "INVOKE", target = "Ljava/util/Collection;isEmpty()Z"))
    private boolean dragonSurvival$considerClientEffectsForIsEmpty(boolean isEmpty) {
        return isEmpty && dragonSurvival$providers.isEmpty();
    }

    // TODO :: Do we care to determine if effects are beneficial or not? In this UI vanilla puts harmful effects below beneficial ones instead of beside them
    @Inject(method = "renderEffects", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"))
    private void dragonSurvival$renderAbilityEffects(final GuiGraphics graphics, final DeltaTracker deltaTracker, final CallbackInfo callback, @Local(ordinal = 0) int renderedCount) {
        Gui self = (Gui) (Object) this;
        int effectCount = renderedCount;

        for (ClientEffectProvider provider : dragonSurvival$providers) {
            int xPos = graphics.guiWidth();
            int yPos = 1;

            if (Minecraft.getInstance().isDemo()) {
                yPos += 15;
            }

            effectCount++;
            xPos -= 25 * effectCount;
            graphics.blitSprite(((GuiAccessor) self).dragonSurvival$getEffectBackgroundSprite(), xPos, yPos, 24, 24);

            float alpha = 1;

            if (!provider.isInfiniteDuration() && provider.currentDuration() < 200) {
                int duration = (int) (10 - Functions.ticksToSeconds(provider.currentDuration()));

                alpha = Mth.clamp((float) provider.currentDuration() / 10 / 5 * 0.5f, 0, 0.5f)
                        + Mth.cos((float) provider.currentDuration() * (float) Math.PI / 5) * Mth.clamp((float) duration / 10 * 0.25f, 0, 0.25f);
            }

            graphics.setColor(1, 1, 1, alpha);
            graphics.blit(provider.clientData().texture(), xPos + 3, yPos + 3, 0, 0, 0, 18, 18, 18, 18);
            graphics.setColor(1, 1, 1, 1);
        }
    }

    @Unique private static @Nullable ResourceLocation dragonSurvival$getSpriteForAirBubble(boolean burst) {
        Player player = Minecraft.getInstance().player;

        //noinspection DataFlowIssue -> player is present
        FluidType previousFluidType = SwimData.getData(player).previousFluid;
        FluidType currentFluidType = player.getEyeInFluidType();

        ResourceLocation replacementSprite;
        FluidType relevantFluid;

        if (NeoForgeMod.EMPTY_TYPE.getKey() == SwimData.key(currentFluidType)) {
            relevantFluid = previousFluidType;
        } else {
            relevantFluid = currentFluidType;
        }

        if (burst) {
            replacementSprite = SwimData.getAirBurstSprite(relevantFluid);
        } else {
            replacementSprite = SwimData.getAirSprite(relevantFluid);
        }

        if (replacementSprite == null) {
            return null;
        }

        TextureAtlasSprite sprite = Minecraft.getInstance().getGuiSprites().getSprite(replacementSprite);

        if (sprite.contents().name() != MissingTextureAtlasSprite.getLocation()) {
            return replacementSprite;
        }

        return null;
    }

    @ModifyArg(method = "renderAirLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 0))
    private ResourceLocation dragonSurvival$modifyAirSprite(ResourceLocation sprite) {
        ResourceLocation replacementSprite = dragonSurvival$getSpriteForAirBubble(false);

        if (replacementSprite != null) {
            return replacementSprite;
        } else {
            return sprite;
        }
    }

    @ModifyArg(method = "renderAirLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V", ordinal = 1))
    private ResourceLocation dragonSurvival$modifyAirBurstSprite(ResourceLocation sprite) {
        ResourceLocation replacementSprite = dragonSurvival$getSpriteForAirBubble(true);

        if (replacementSprite != null) {
            return replacementSprite;
        } else {
            return sprite;
        }
    }
}
