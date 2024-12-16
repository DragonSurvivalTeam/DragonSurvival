package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PenaltySupply;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.GlStateBackup;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class DragonPenaltyHUD {
    private static final ResourceLocation DRAGON_HUD = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dragon_hud.png");

    public static void renderDragonPenaltyHUD(final DragonStateHandler handler, final Gui gui, final GuiGraphics guiGraphics) {
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
            boolean hasSupply = penaltySupply.hasSupply(supplyType);
            if(hasSupply && supplyPercentage < 1) {
                RenderSystem.enableBlend();

                rightHeight = gui.rightHeight;
                gui.rightHeight += 10;

                final int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
                final int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
                final int full = Mth.floor(supplyPercentage * 10.0D);
                final int partial = Mth.ceil(supplyPercentage * 10.0D) - full;
                ResourceLocation supplyIcon = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/supply_icons/" + supplyType + ".png");
                for (int i = 0; i < full + partial; ++i) {
                    guiGraphics.blit(supplyIcon, left - i * 8 - 9, top, 9, 9,  i < full ? 0 : 9, 0, 9, 9, 18, 9);
                }

                RenderSystem.disableBlend();
            }
        }
        RenderSystem.restoreGlState(backup);


        // TODO: SupplyTrigger handling
        /*if (handler.getType() instanceof SeaDragonType seaDragonType) {
            if (seaDragonType.timeWithoutWater > 0 && ServerConfig.penaltiesEnabled && SeaDragonConfig.seaTicksWithoutWater != 0) {
                RenderSystem.enableBlend();

                rightHeight = gui.rightHeight;
                gui.rightHeight += 10;

                int maxTimeWithoutWater = SeaDragonConfig.seaTicksWithoutWater;
                Optional<WaterAbility> waterAbility = DragonAbilities.getAbility(localPlayer, WaterAbility.class);

                if (waterAbility.isPresent()) {
                    maxTimeWithoutWater += Functions.secondsToTicks(waterAbility.get().getDuration());
                }

                double timeWithoutWater = maxTimeWithoutWater - seaDragonType.timeWithoutWater;
                boolean flag = false;

                if (timeWithoutWater < 0) {
                    flag = true;
                    timeWithoutWater = Math.abs(timeWithoutWater);
                }

                final int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
                final int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
                final int full = flag ? Mth.floor(timeWithoutWater * 10.0D / maxTimeWithoutWater) : Mth.ceil((timeWithoutWater - 2) * 10.0D / maxTimeWithoutWater);
                final int partial = Mth.ceil(timeWithoutWater * 10.0D / maxTimeWithoutWater) - full;

                for (int i = 0; i < full + partial; ++i) {
                    guiGraphics.blit(DRAGON_HUD, left - i * 8 - 9, top, flag ? 18 : i < full ? 0 : 9, 36, 9, 9);
                }

                RenderSystem.disableBlend();
            }
        } else if (handler.getType() instanceof CaveDragonType caveDragonType) {
            int maxRainResistanceSupply = CaveDragonType.getMaxRainResistanceSupply(localPlayer);

            if (ServerConfig.penaltiesEnabled && CaveDragonConfig.caveRainDamage > 0 && caveDragonType.rainResistanceSupply < maxRainResistanceSupply) {
                RenderSystem.enableBlend();

                rightHeight = gui.rightHeight;
                gui.rightHeight += 10;

                int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
                int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
                int full = Mth.ceil((double) (caveDragonType.rainResistanceSupply - 2) * 10 / maxRainResistanceSupply);
                int partial = Mth.ceil((double) caveDragonType.rainResistanceSupply * 10 / maxRainResistanceSupply) - full;

                for (int i = 0; i < full + partial; ++i) {
                    guiGraphics.blit(DRAGON_HUD, left - i * 8 - 9, top, i < full ? 0 : 9, 54, 9, 9);
                }

                RenderSystem.disableBlend();
            }

            if (caveDragonType.lavaAirSupply < CaveDragonConfig.caveLavaSwimmingTicks && DragonBonusConfig.bonusesEnabled && CaveDragonConfig.caveLavaSwimmingTicks != 0 && CaveDragonConfig.caveLavaSwimming) {
                RenderSystem.enableBlend();

                rightHeight = gui.rightHeight;
                gui.rightHeight += 10;

                int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
                int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
                int full = Mth.ceil((double) (caveDragonType.lavaAirSupply - 2) * 10.0D / CaveDragonConfig.caveLavaSwimmingTicks);
                int partial = Mth.ceil((double) caveDragonType.lavaAirSupply * 10.0D / CaveDragonConfig.caveLavaSwimmingTicks) - full;

                for (int i = 0; i < full + partial; ++i) {
                    guiGraphics.blit(DRAGON_HUD, left - i * 8 - 9, top, i < full ? 0 : 9, 27, 9, 9);
                }

                RenderSystem.disableBlend();
            }
        } else if (handler.getType() instanceof ForestDragonType forestDragonType) {
            if (forestDragonType.timeInDarkness > 0 && ServerConfig.penaltiesEnabled && ForestDragonConfig.stressTicks != 0 && !localPlayer.hasEffect(DSEffects.STRESS)) {
                RenderSystem.enableBlend();

                rightHeight = gui.rightHeight;
                gui.rightHeight += 10;

                int maxTimeInDarkness = ForestDragonConfig.stressTicks;
                Optional<LightInDarknessAbility> lightInDarkness = DragonAbilities.getAbility(localPlayer, LightInDarknessAbility.class, forestDragonType);

                if (lightInDarkness.isPresent()) {
                    maxTimeInDarkness += Functions.secondsToTicks(lightInDarkness.get().getDuration());
                }

                final int timeInDarkness = maxTimeInDarkness - Math.min(forestDragonType.timeInDarkness, maxTimeInDarkness);

                final int left = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + 91;
                final int top = Minecraft.getInstance().getWindow().getGuiScaledHeight() - rightHeight;
                final int full = Mth.ceil((double) (timeInDarkness - 2) * 10.0D / maxTimeInDarkness);
                final int partial = Mth.ceil((double) timeInDarkness * 10.0D / maxTimeInDarkness) - full;

                for (int i = 0; i < full + partial; ++i) {
                    guiGraphics.blit(DRAGON_HUD, left - i * 8 - 9, top, i < full ? 0 : 9, 45, 9, 9);
                }

                RenderSystem.disableBlend();
            }
        }*/
    }
}
