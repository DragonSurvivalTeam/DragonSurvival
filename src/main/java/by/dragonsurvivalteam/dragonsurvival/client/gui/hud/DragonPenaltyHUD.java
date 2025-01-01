package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.DragonPenalty;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.penalty.SupplyTrigger;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.GlStateBackup;

import java.util.Optional;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonPenaltyHUD {
    public static void renderDragonPenaltyHUD(final Gui gui, final GuiGraphics guiGraphics) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;

        if (localPlayer == null || !Minecraft.getInstance().gameMode.canHurtPlayer()) {
            return;
        }

        int rightHeight;

        PenaltySupply penaltySupply = PenaltySupply.getData(localPlayer);
        GlStateBackup backup = new GlStateBackup();
        RenderSystem.backupGlState(backup);
        for(String supplyType : penaltySupply.getSupplyTypes()) {
            float supplyPercentage = penaltySupply.getPercentage(supplyType);
            Optional<Holder<DragonPenalty>> penalty = penaltySupply.getMatchingPenalty(supplyType, DragonStateProvider.getData(localPlayer));
            SupplyTrigger supplyTrigger = null;
            if(penalty.isPresent() && penalty.get().value().trigger() instanceof SupplyTrigger) {
                supplyTrigger = (SupplyTrigger) penalty.get().value().trigger();
            }

            boolean shouldRender = penaltySupply.hasSupply(supplyType) && supplyPercentage < 1;
            if(supplyTrigger != null) {
                shouldRender = shouldRender || supplyTrigger.displayLikeHungerBar();
            }

            if(shouldRender) {
                RenderSystem.enableBlend();

                rightHeight = gui.rightHeight;
                gui.rightHeight += 10;

                boolean displayLikeHungerBar = false;
                if(supplyTrigger != null) {
                    displayLikeHungerBar = supplyTrigger.displayLikeHungerBar();
                }

                // See renderAirLevel in vanilla to understand this value
                final float vanillaSupplyPercentageOffset = (float) 2 / 360;
                final int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
                final int top =  Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
                ResourceLocation supplyIcon = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/custom/supply_icons/" + supplyType + ".png");

                if(displayLikeHungerBar) {
                    int foodLevel = Mth.ceil(supplyPercentage * 20);
                    RenderSystem.enableBlend();

                    for (int i = 0; i < 10; ++i) {
                        // See renderFood for more info
                        int offset = 0;
                        if (localPlayer.tickCount % (foodLevel * 3 + 1) == 0) {
                            offset = localPlayer.level().random.nextInt(3) - 1;
                        }

                        int uOffset;
                        if(i * 2 + 1 < foodLevel) {
                            uOffset = 0;
                        } else if(i * 2 + 1 == foodLevel) {
                            uOffset = 9;
                        } else {
                            uOffset = 18;
                        }

                        guiGraphics.blit(supplyIcon, left - i * 8 - 9, top + offset, 9, 9,  uOffset, 0, 9, 9, 27, 9);
                    }

                    RenderSystem.disableBlend();
                } else {
                    RenderSystem.enableBlend();
                    int full = Mth.ceil((supplyPercentage - vanillaSupplyPercentageOffset) * 10.0);
                    int partial = Mth.ceil(supplyPercentage * 10.0D) - full;

                    for (int i = 0; i < full + partial; ++i) {
                        guiGraphics.blit(supplyIcon, left - i * 8 - 9, top, 9, 9,  i < full ? 0 : 9, 0, 9, 9, 18, 9);
                    }

                    RenderSystem.disableBlend();
                }
            }
        }
        RenderSystem.restoreGlState(backup);
    }
}
