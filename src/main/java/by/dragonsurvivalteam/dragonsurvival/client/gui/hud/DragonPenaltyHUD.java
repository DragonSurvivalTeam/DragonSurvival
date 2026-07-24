package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.SupplyTrigger;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

/** Render dragon specific hud elements (e.g. time in rain for cave dragons or time without water for sea dragons) */
public class DragonPenaltyHUD {
    public static final ResourceLocation ID = DragonSurvival.res("penalty_hud");

    public static void render(@NotNull final GuiGraphics graphics, @NotNull final DeltaTracker tracker) {
        if (Minecraft.getInstance().options.hideGui || !DragonPenalty.ENABLE_PENALTIES) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;

        //noinspection DataFlowIssue -> game mode is expected to be present
        if (player == null || !Minecraft.getInstance().gameMode.canHurtPlayer()) {
            return;
        }

        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        PenaltySupply supply = player.getExistingData(DSDataAttachments.PENALTY_SUPPLY).orElse(null);

        if (supply == null) {
            return;
        }

        for (ResourceLocation supplyType : supply.getSupplyTypes()) {
            boolean displayLikeHungerBar = supply.getMatchingPenalty(supplyType, DragonStateProvider.getData(player)).map(penalty -> {
                if (penalty.value().trigger() instanceof SupplyTrigger supplyTrigger) {
                    return supplyTrigger.displayLikeHungerBar();
                }

                return false;
            }).orElse(false);

            float supplyPercentage = supply.getPercentage(supplyType);
            boolean shouldRender = supply.hasSupply(supplyType) && supplyPercentage < 1 || displayLikeHungerBar;

            if (shouldRender) {
                int rightHeight = Minecraft.getInstance().gui.rightHeight;
                Minecraft.getInstance().gui.rightHeight += 10;

                // See renderAirLevel in vanilla to understand this value
                float vanillaSupplyPercentageOffset = (float) 2 / 360;
                int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
                int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
                ResourceLocation supplyIcon = supplyType.withPrefix("textures/gui/custom/supply_icons/").withSuffix(".png");

                if (displayLikeHungerBar) {
                    int foodLevel = Mth.ceil(supplyPercentage * 20);

                    for (int i = 0; i < 10; ++i) {
                        // See renderFood for more info
                        int offset = 0;
                        if (player.tickCount % (foodLevel * 3 + 1) == 0 && supplyPercentage < 1) {
                            offset = player.level().random.nextInt(3) - 1;
                        }

                        int uOffset;
                        if (i * 2 + 1 < foodLevel) {
                            uOffset = 0;
                        } else if (i * 2 + 1 == foodLevel) {
                            uOffset = 9;
                        } else {
                            uOffset = 18;
                        }

                        graphics.blit(supplyIcon, left - i * 8 - 9, top + offset, 9, 9, uOffset, 0, 9, 9, 27, 9);
                    }
                } else {
                    int full = Mth.ceil((supplyPercentage - vanillaSupplyPercentageOffset) * 10);
                    int partial = Mth.ceil(supplyPercentage * 10) - full;

                    for (int i = 0; i < full + partial; ++i) {
                        graphics.blit(supplyIcon, left - i * 8 - 9, top, 9, 9, i < full ? 0 : 9, 0, 9, 9, 18, 9);
                    }
                }
            }
        }
    }
}
