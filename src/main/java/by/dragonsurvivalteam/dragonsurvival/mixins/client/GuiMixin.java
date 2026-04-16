package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.util.FluidTypeUtil;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.google.common.collect.Ordering;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.List;

@Mixin(Gui.class)
public abstract class GuiMixin {
    @Shadow @Final private static Identifier EFFECT_BACKGROUND_SPRITE;

    @Unique private List<ClientEffectProvider> dragonSurvival$providers = List.of();

    @Inject(method = "extractEffects", at = @At("HEAD"))
    private void dragonSurvival$storeProviders(final GuiGraphicsExtractor graphics, final DeltaTracker deltaTracker, final CallbackInfo callback) {
        dragonSurvival$providers = ClientEffectProvider.getProviders(false);
    }

    @ModifyExpressionValue(method = "extractEffects", at = @At(value = "INVOKE", target = "Ljava/util/Collection;isEmpty()Z"))
    private boolean dragonSurvival$considerClientEffectsForIsEmpty(final boolean original) {
        return original && dragonSurvival$providers.isEmpty();
    }

    @Inject(method = "extractEffects", at = @At("TAIL"))
    private void dragonSurvival$renderAbilityEffects(final GuiGraphicsExtractor graphics, final DeltaTracker deltaTracker, final CallbackInfo callback) {
        if (dragonSurvival$providers.isEmpty()) {
            return;
        }

        final Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player == null || minecraft.screen != null && minecraft.screen.showsActiveEffects()) {
            return;
        }

        int renderedCount = 0;
        final Collection<MobEffectInstance> activeEffects = minecraft.player.getActiveEffects();

        for (final MobEffectInstance instance : Ordering.natural().reverse().sortedCopy(activeEffects)) {
            final IClientMobEffectExtensions renderer = IClientMobEffectExtensions.of(instance);

            if (!renderer.isVisibleInGui(instance) || !instance.showIcon()) {
                continue;
            }

            final Holder<MobEffect> effect = instance.getEffect();

            if (effect.value().isBeneficial()) {
                renderedCount++;
            }
        }

        for (final ClientEffectProvider provider : dragonSurvival$providers) {
            int xPos = graphics.guiWidth();
            int yPos = 1;

            if (minecraft.isDemo()) {
                yPos += 15;
            }

            renderedCount++;
            xPos -= 25 * renderedCount;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_SPRITE, xPos, yPos, 24, 24);

            float alpha = 1.0F;

            if (!provider.isInfiniteDuration() && provider.currentDuration() < 200) {
                final int duration = (int)(10 - Functions.ticksToSeconds(provider.currentDuration()));
                alpha = Mth.clamp((float)provider.currentDuration() / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                    + Mth.cos((float)provider.currentDuration() * (float)Math.PI / 5.0F) * Mth.clamp((float)duration / 10.0F * 0.25F, 0.0F, 0.25F);
                alpha = Mth.clamp(alpha, 0.0F, 1.0F);
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, provider.clientData().texture(), xPos + 3, yPos + 3, 0, 0, 18, 18, 18, 18, ARGB.white(alpha));
        }
    }

    @Unique
    private static @Nullable Identifier dragonSurvival$getSpriteForAirBubble(final boolean burst) {
        final Player player = Minecraft.getInstance().player;

        if (player == null) {
            return null;
        }

        final FluidType previousFluidType = SwimData.getData(player).previousFluid;
        final FluidType currentFluidType = FluidTypeUtil.getEyeFluidType(player);
        final FluidType relevantFluid;

        if (NeoForgeMod.EMPTY_TYPE.getKey() == SwimData.key(currentFluidType)) {
            relevantFluid = previousFluidType;
        } else {
            relevantFluid = currentFluidType;
        }

        if (relevantFluid == null) {
            return null;
        }

        final Identifier replacementSprite = burst ? SwimData.getAirBurstSprite(relevantFluid) : SwimData.getAirSprite(relevantFluid);

        if (replacementSprite == null) {
            return null;
        }

        return replacementSprite;
    }

    @ModifyArg(
        method = "extractAirBubbles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
            ordinal = 0
        ),
        index = 1
    )
    private Identifier dragonSurvival$modifyAirSprite(final Identifier sprite) {
        final Identifier replacementSprite = dragonSurvival$getSpriteForAirBubble(false);
        return replacementSprite != null ? replacementSprite : sprite;
    }

    @ModifyArg(
        method = "extractAirBubbles",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIII)V",
            ordinal = 1
        ),
        index = 1
    )
    private Identifier dragonSurvival$modifyAirBurstSprite(final Identifier sprite) {
        final Identifier replacementSprite = dragonSurvival$getSpriteForAirBubble(true);
        return replacementSprite != null ? replacementSprite : sprite;
    }
}
