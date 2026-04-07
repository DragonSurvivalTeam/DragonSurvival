package by.dragonsurvivalteam.dragonsurvival.mixins.client;

import by.dragonsurvivalteam.dragonsurvival.compat.emi.EmiCompat;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Mixin(EffectsInInventory.class)
public class EffectsInInventoryMixin {
    @Shadow @Final private AbstractContainerScreen<?> screen;

    @Unique private List<ClientEffectProvider> dragonSurvival$providers = List.of();

    /** Interacted with through {@link EffectsInInventoryAccessor}. */
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    @Unique private final List<Rect2i> dragonSurvival$areasBlockedByModifierUIForJEI = new ArrayList<>();

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void dragonSurvival$storeProviders(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final CallbackInfo callback) {
        dragonSurvival$providers = ClientEffectProvider.getProviders(true);
        dragonSurvival$areasBlockedByModifierUIForJEI.clear();
    }

    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Ljava/util/Collection;isEmpty()Z"))
    private boolean dragonSurvival$considerClientEffectsForIsEmpty(final boolean original) {
        if (EmiCompat.hideEffects()) {
            return original;
        }

        return original && dragonSurvival$providers.isEmpty();
    }

    @ModifyExpressionValue(method = "extractRenderState", at = @At(value = "INVOKE", target = "Ljava/util/Collection;size()I"))
    private int dragonSurvival$adjustRenderedEffectsSize(final int original) {
        if (EmiCompat.hideEffects()) {
            return original;
        }

        return original + dragonSurvival$providers.size();
    }

    @Inject(
            method = "extractRenderState",
            at = @At(
                    value = "INVOKE_ASSIGN",
                    target = "Lnet/neoforged/neoforge/client/ClientHooks;onScreenPotionSize(Lnet/minecraft/client/gui/screens/Screen;IZI)Lnet/neoforged/neoforge/client/event/ScreenEvent$RenderInventoryMobEffects;"
            )
    )
    private void dragonSurvival$storeEvent(final CallbackInfo callback, @Local final ScreenEvent.RenderInventoryMobEffects event, @Share("stored_event") final LocalRef<ScreenEvent.RenderInventoryMobEffects> storedEvent) {
        storedEvent.set(event);
    }

    @Unique
    private int dragonSurvival$renderAbilityBackground(final GuiGraphicsExtractor graphics, final Font font, final Component effectName, final Component duration, final int x0, final int y0, final int maxTextureWidth) {
        final int nameWidth = 32 + font.width(effectName) + 7;
        final int durationWidth = 32 + font.width(duration) + 7;
        final int textureWidth = Math.min(maxTextureWidth, Math.max(nameWidth, durationWidth));
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, EffectsInInventoryAccessor.dragonSurvival$getEffectBackgroundSprite(), x0, y0, textureWidth, 32);
        return textureWidth;
    }

    @Unique
    private void dragonSurvival$renderAbilityLabel(final GuiGraphicsExtractor graphics, final Font font, final Component effectText, final Component duration, final int x0, final int y0, final int textureWidth) {
        final int textX = x0 + 32;
        final int textY = y0 + 7;
        final int maxTextWidth = textureWidth - 39;

        if (maxTextWidth <= 0) {
            return;
        }

        final FormattedCharSequence clippedText = font.width(effectText) > maxTextWidth ? ComponentRenderUtils.clipText(effectText, font, maxTextWidth) : effectText.getVisualOrderText();
        graphics.text(font, clippedText, textX, textY, DSColors.withAlpha(DSColors.WHITE, 1));
        graphics.text(font, duration, textX, textY + 9, -8355712);
    }

    @Unique
    private static Component dragonSurvival$formatDuration(final ClientEffectProvider effect, final float ticksPerSecond) {
        if (effect.isInfiniteDuration()) {
            return Component.translatable(LangKey.DURATION, DSColors.dynamicValue(Component.translatable("effect.duration.infinite")));
        }

        final int duration = Mth.floor((float) effect.currentDuration());
        return Component.translatable(LangKey.DURATION, DSColors.dynamicValue(StringUtil.formatTickDuration(duration, ticksPerSecond)));
    }

    @Unique
    private void dragonSurvival$appendTooltipLine(final List<FormattedCharSequence> tooltip, final Font font, final Component line) {
        tooltip.addAll(font.split(line, 170));
    }

    @Unique
    private List<FormattedCharSequence> dragonSurvival$createTooltip(final ClientEffectProvider hovered, final Font font) {
        final List<FormattedCharSequence> tooltip = new ArrayList<>();
        dragonSurvival$appendTooltipLine(tooltip, font, hovered.clientData().name());

        if (Minecraft.getInstance().options.advancedItemTooltips) {
            dragonSurvival$appendTooltipLine(tooltip, font, Component.literal(hovered.clientData().id().toString()).withStyle(ChatFormatting.DARK_GRAY));
        }

        if (!hovered.clientData().effectSource().getString().isEmpty()) {
            dragonSurvival$appendTooltipLine(tooltip, font, Component.translatable(LangKey.APPLIED_BY, DSColors.dynamicValue(hovered.clientData().effectSource())));
        }

        //noinspection DataFlowIssue
        dragonSurvival$appendTooltipLine(tooltip, font, dragonSurvival$formatDuration(hovered, Minecraft.getInstance().level.tickRateManager().tickrate()));

        if (!hovered.getDescription().getString().isEmpty()) {
            tooltip.add(FormattedCharSequence.EMPTY);
            dragonSurvival$appendTooltipLine(tooltip, font, hovered.getDescription());
        }

        return tooltip;
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void dragonSurvival$renderAbilityEffects(final GuiGraphicsExtractor graphics, final int mouseX, final int mouseY, final CallbackInfo callback, @Share("stored_event") final LocalRef<ScreenEvent.RenderInventoryMobEffects> storedEvent) {
        if (dragonSurvival$providers.isEmpty() || EmiCompat.hideEffects()) {
            return;
        }

        int renderX = screen.getGuiLeft() + screen.getXSize() + 2;
        final int availableSpace = ((ScreenAccessor) screen).dragonSurvival$getWidth() - renderX;

        if (availableSpace < 32) {
            return;
        }

        final LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        ScreenEvent.RenderInventoryMobEffects event = storedEvent.get();

        if (event == null) {
            event = ClientHooks.onScreenPotionSize(screen, availableSpace, availableSpace < 120, renderX);
        }

        if (event.isCanceled()) {
            return;
        }

        final List<MobEffectInstance> renderedMobEffects = player.getActiveEffects().stream().filter(ClientHooks::shouldRenderEffect).sorted().toList();
        final int totalElementsToRender = renderedMobEffects.size() + dragonSurvival$providers.size();
        final int yOffset = totalElementsToRender > 5 ? 132 / (totalElementsToRender - 1) : 33;
        int topPos = screen.getGuiTop() + yOffset * renderedMobEffects.size();
        final boolean renderLabels = !event.isCompact();
        final int maxWidth = renderLabels ? availableSpace - 7 : 32;
        final Font font = screen.getFont();

        renderX = event.getHorizontalOffset();

        for (final ClientEffectProvider provider : dragonSurvival$providers) {
            final Component effectName = provider.clientData().name();
            //noinspection DataFlowIssue
            final Component duration = dragonSurvival$formatDuration(provider, Minecraft.getInstance().level.tickRateManager().tickrate());
            final int textureWidth = dragonSurvival$renderAbilityBackground(graphics, font, effectName, duration, renderX, topPos, maxWidth);
            dragonSurvival$areasBlockedByModifierUIForJEI.add(new Rect2i(renderX, topPos, textureWidth, 32));

            if (renderLabels) {
                dragonSurvival$renderAbilityLabel(graphics, font, effectName, duration, renderX, topPos, textureWidth);
            }

            graphics.blit(RenderPipelines.GUI_TEXTURED, provider.clientData().texture(), renderX + 7, topPos + 7, 0, 0, 18, 18, 18, 18);

            if (mouseX >= renderX && mouseX <= renderX + textureWidth && mouseY >= topPos && mouseY <= topPos + yOffset) {
                graphics.setTooltipForNextFrame(font, dragonSurvival$createTooltip(provider, font), mouseX, mouseY);
            }

            topPos += yOffset;
        }
    }
}
