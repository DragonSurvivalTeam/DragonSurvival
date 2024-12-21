package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.mixins.client.ScreenAccessor;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(EffectRenderingInventoryScreen.class)
public class EffectRenderingInventoryScreenMixin {
    @Unique private List<ClientEffectProvider> dragonSurvival$providers = List.of();
    @Unique private List<Rect2i> dragonSurvival$areasBlockedByModifierUIForJEI = new ArrayList<>();

    @Inject(method = "renderEffects", at = @At("HEAD"))
    private void dragonSurvival$storeProviders(final GuiGraphics graphics, int mouseX, int mouseY, final CallbackInfo callback) {
        dragonSurvival$providers = ClientEffectProvider.getProviders();
    }

    @ModifyExpressionValue(method = "renderEffects", at = @At(value = "INVOKE", target = "Ljava/util/Collection;size()I"))
    private int dragonSurvival$adjustRenderedEffectsSize(int original) {
        return original + dragonSurvival$providers.size();
    }

    @Inject(method = "renderEffects", at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/client/ClientHooks;onScreenPotionSize(Lnet/minecraft/client/gui/screens/Screen;IZI)Lnet/neoforged/neoforge/client/event/ScreenEvent$RenderInventoryMobEffects;", shift = At.Shift.BY, by = 2))
    private void dragonSurvival$storeEvent(final CallbackInfo callback, @Local final ScreenEvent.RenderInventoryMobEffects event, @Share("stored_event") final LocalRef<ScreenEvent.RenderInventoryMobEffects> storedEvent) {
        storedEvent.set(event);
    }

    @Unique private void dragonSurvival$renderAbilityBackgrounds(final GuiGraphics graphics, int renderX, int yOffset, int initialYOffset, int providerAmount, boolean isCompact) {
        EffectRenderingInventoryScreen<?> self = (EffectRenderingInventoryScreen<?>) (Object) this;
        int topPos = ((AbstractContainerScreenAccessor) self).dragonSurvival$getTopPos() + initialYOffset;
        int width = isCompact ? 32 : 120;

        for (int i = 0; i < providerAmount; i++) {
            graphics.blitSprite(isCompact ? ((EffectRenderingInventoryScreenAccessor) self).dragonSurvival$getEffectBackgroundSmallSprite() : ((EffectRenderingInventoryScreenAccessor) self).dragonSurvival$getEffectBackgroundLargeSprite(), renderX, topPos, width, 32);
            dragonSurvival$areasBlockedByModifierUIForJEI.add(new Rect2i(renderX, topPos, width, 32));
            topPos += yOffset;
        }
    }

    @Unique private void dragonSurvival$renderAbilityIcons(final GuiGraphics graphics, int renderX, int yOffset, int initialYOffset, final List<ClientEffectProvider> providers, boolean isCompact) {
        EffectRenderingInventoryScreen<?> self = (EffectRenderingInventoryScreen<?>) (Object) this;
        int topPos = ((AbstractContainerScreenAccessor) self).dragonSurvival$getTopPos() + initialYOffset;

        for (ClientEffectProvider provider : providers) {
            graphics.blit(provider.clientData().texture(), renderX + (isCompact ? 6 : 7), topPos + 7, 0, 0, 0, 18, 18, 18, 18);
            topPos += yOffset;
        }
    }

    @Unique private void dragonSurvival$renderAbilityLabels(final GuiGraphics graphics, int renderX, int yOffset, int initialYOffset, final List<ClientEffectProvider> providers) {
        EffectRenderingInventoryScreen<?> self = (EffectRenderingInventoryScreen<?>) (Object) this;
        int topPos = ((AbstractContainerScreenAccessor) self).dragonSurvival$getTopPos() + initialYOffset;

        for (ClientEffectProvider provider : providers) {
            Component name = Component.translatable(Translation.Type.MODIFIER.wrap(provider.id()));
            graphics.drawString(((ScreenAccessor) self).dragonSurvival$getFont(), name, renderX + 10 + 18, topPos + 6, DSColors.WHITE);
            //noinspection DataFlowIssue -> level is present
            Component duration = dragonSurvival$formatDuration(provider, Minecraft.getInstance().level.tickRateManager().tickrate());
            graphics.drawString(((ScreenAccessor) self).dragonSurvival$getFont(), duration, renderX + 10 + 18, topPos + 6 + 10, 8355711);
            topPos += yOffset;
        }
    }

    @Unique private static Component dragonSurvival$formatDuration(final ClientEffectProvider effect, float ticksPerSecond) {
        if (effect.isInfiniteDuration()) {
            return Component.translatable("effect.duration.infinite");
        } else {
            int duration = Mth.floor((float) effect.currentDuration());
            return Component.literal(StringUtil.formatTickDuration(duration, ticksPerSecond));
        }
    }

    @Inject(method = "renderEffects", at = @At(value = "TAIL"))
    private void dragonSurvival$renderAbilityEffects(final GuiGraphics graphics, int mouseX, int mouseY, final CallbackInfo callback, @Share("stored_event") final LocalRef<ScreenEvent.RenderInventoryMobEffects> storedEvent) {
        EffectRenderingInventoryScreen<?> self = (EffectRenderingInventoryScreen<?>) (Object) this;
        int offset = ((AbstractContainerScreenAccessor) self).dragonSurvival$getLeftPos() + ((AbstractContainerScreenAccessor) self).dragonSurvival$imageWidth() + 2;
        int width = self.width;

        LocalPlayer player = Objects.requireNonNull(Minecraft.getInstance().player);
        dragonSurvival$areasBlockedByModifierUIForJEI.clear();

        if (!dragonSurvival$providers.isEmpty() && width >= 32) {
            boolean isCompact = width < 120;
            ScreenEvent.RenderInventoryMobEffects event = storedEvent.get();

            if (event == null) {
                event = net.neoforged.neoforge.client.ClientHooks.onScreenPotionSize(self, width, isCompact, offset);
            }

            if (event.isCanceled()) {
                return;
            }

            isCompact = event.isCompact();
            offset = event.getHorizontalOffset();
            Collection<MobEffectInstance> mobEffects = player.getActiveEffects();
            int totalElementsToRender = mobEffects.size() + dragonSurvival$providers.size();

            int yOffset;

            if (totalElementsToRender > 5) {
                yOffset = 132 / (totalElementsToRender - 1);
            } else {
                yOffset = 33;
            }

            int renderedElements = mobEffects.stream().filter(net.neoforged.neoforge.client.ClientHooks::shouldRenderEffect).sorted().toList().size();
            int initialYOffset = yOffset * renderedElements;

            dragonSurvival$renderAbilityBackgrounds(graphics, offset, yOffset, initialYOffset, dragonSurvival$providers.size(), isCompact);
            dragonSurvival$renderAbilityIcons(graphics, offset, yOffset, initialYOffset, dragonSurvival$providers, isCompact);

            if (!isCompact) {
                // TODO: Potentially render extra tooltip data anyways?
                this.dragonSurvival$renderAbilityLabels(graphics, offset, yOffset, initialYOffset, dragonSurvival$providers);
            }

            if (mouseX >= offset && mouseX <= offset + (isCompact ? 32 : 120)) {
                int topPos = ((AbstractContainerScreenAccessor) self).dragonSurvival$getTopPos() + initialYOffset;
                ClientEffectProvider hovered = null;

                for (ClientEffectProvider provider : dragonSurvival$providers) {
                    if (mouseY >= topPos && mouseY <= topPos + yOffset) {
                        hovered = provider;
                    }

                    topPos += yOffset;
                }

                if (hovered != null) {
                    List<Component> list = new ArrayList<>();

                    if (isCompact) {
                        list.add(Component.translatable(Translation.Type.MODIFIER.wrap(hovered.id())));
                        //noinspection DataFlowIssue -> level is present
                        list.add(dragonSurvival$formatDuration(hovered, Minecraft.getInstance().level.tickRateManager().tickrate()));
                    }

                    if (!Objects.equals(hovered.clientData().tooltip(), Component.empty())) {
                        list.add(hovered.clientData().tooltip());
                    }

                    graphics.renderTooltip(((ScreenAccessor) self).dragonSurvival$getFont(), list, Optional.empty(), mouseX, mouseY);
                }
            }
        }
    }
}
