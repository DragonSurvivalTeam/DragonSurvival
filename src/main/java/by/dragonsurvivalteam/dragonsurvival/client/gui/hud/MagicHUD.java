package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ManaHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.awt.*;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@EventBusSubscriber(value = Dist.CLIENT)
public class MagicHUD {
    // 1.20.6 moved a whole bunch of widgets around, so to keep compatibiltiy with older versions, we need to use the old widgets texture
    public static final ResourceLocation WIDGET_TEXTURES = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/widgets.png");

    private static final ResourceLocation VANILLA_WIDGETS = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/pre-1.20.1-widgets.png");
    private static final ResourceLocation CAST_BAR_FILL = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/casting_bars/cast_bar_fill.png");

    public static final Color COLOR = new Color(243, 48, 59);

    @Translation(type = Translation.Type.MISC, comments = "§fNot enough§r §cmana or experience§r!")
    public static final String NO_MANA = Translation.Type.GUI.wrap("ability.no_mana");

    @Translation(type = Translation.Type.MISC, comments = "§fThis ability is §r§cnot ready§r§f yet!§r (%s)")
    public static final String COOLDOWN = Translation.Type.GUI.wrap("ability.cooldown");

    @Translation(type = Translation.Type.MISC, comments = "§fThis skill cannot be used §r§cwhile flying§r§f!§f")
    public static final String FLYING = Translation.Type.GUI.wrap("ability.flying");

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "cast_bar_x_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the x position of the cast bar")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "cast_bar_x_offset")
    public static Integer castbarXOffset = 0;

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "cast_bar_y_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the y position of the cast bar")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "casterBarYPos")
    public static Integer castbarYOffset = 0;

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "skill_bar_x_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the x position of the skill bar")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "skill_bar_x_offset")
    public static Integer skillbarXOffset = 0;

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "skill_bar_y_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the x position of the skill bar")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "skill_bar_y_offset")
    public static Integer skillbarYOffset = 0;

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "mana_bar_x_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the x position of the mana bar")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "mana_bar_x_offset")
    public static Integer manabarXOffset = 0;

    @ConfigRange(min = -1000, max = 1000)
    @Translation(key = "mana_bar_y_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the y position of the mana bar")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "mana_bar_y_offset")
    public static Integer manabarYOffset = 0;

    public static boolean renderExperienceBar(GuiGraphics guiGraphics, int screenWidth) {
        Player localPlayer = DragonSurvival.PROXY.getLocalPlayer();

        if (localPlayer == null || !Minecraft.getInstance().gameMode.canHurtPlayer() || !Minecraft.getInstance().gameMode.hasExperience()) {
            return false;
        }

        DragonStateHandler handler = DragonStateProvider.getData(localPlayer);

        if (!handler.isDragon()) {
            return false;
        }

        /*ActiveDragonAbility ability = handler.getMagicData().getAbilityFromSlot(handler.getMagicData().getSelectedAbilitySlot());

        if (ability == null || ability.canConsumeMana(localPlayer)) {
            return false;
        }*/

        Window window = Minecraft.getInstance().getWindow();
        int guiScaledWidth = window.getGuiScaledWidth();
        int guiScaledHeight = window.getGuiScaledHeight();

        Minecraft.getInstance().getProfiler().push("expLevel");

        if (localPlayer.getXpNeededForNextLevel() > 0) {
            int width = screenWidth / 2 - 91;

            int experienceProgress = (int) (localPlayer.experienceProgress * 183.0F);
            int height = guiScaledHeight - 32 + 3;
            guiGraphics.blit(WIDGET_TEXTURES, width, height, 0, 0, 164, 182, 5, 256, 256);

            if (experienceProgress > 0) {
                guiGraphics.blit(WIDGET_TEXTURES, width, height, 0, 0, 169, experienceProgress, 5, 256, 256);
            }
        }

        Minecraft.getInstance().getProfiler().pop();

        if (localPlayer.experienceLevel > 0) {
            Minecraft.getInstance().getProfiler().push("expLevel");

            String s = "" + localPlayer.experienceLevel;
            int width = (guiScaledWidth - Minecraft.getInstance().font.width(s)) / 2;
            int height = guiScaledHeight - 31 - 4;

            guiGraphics.drawString(Minecraft.getInstance().font, s, (width + 1), height, 0, false);
            guiGraphics.drawString(Minecraft.getInstance().font, s, (width - 1), height, 0, false);
            guiGraphics.drawString(Minecraft.getInstance().font, s, width, (height + 1), 0, false);
            guiGraphics.drawString(Minecraft.getInstance().font, s, width, (height - 1), 0, false);
            guiGraphics.drawString(Minecraft.getInstance().font, s, width, height, COLOR.getRGB(), false);

            Minecraft.getInstance().getProfiler().pop();
        }

        return true;
    }

    private static int errorTicks;
    private static MutableComponent errorMessage;

    public static void castingError(MutableComponent component) {
        errorTicks = Functions.secondsToTicks(5);
        errorMessage = component;
    }

    @SubscribeEvent
    public static void tickDownError(ClientTickEvent.Pre event) {
        errorTicks--;

        if (errorTicks <= 0) {
            errorMessage = Component.empty();
        }
    }

    public static void renderAbilityHUD(final Player player, final GuiGraphics guiGraphics, int width, int height) {
        if (player == null || player.isSpectator()) {
            return;
        }

        int sizeX = 20;
        int sizeY = 20;

        int i1 = width - sizeX * MagicData.MAX_ACTIVE - 20;
        int posX = i1;
        int posY = height - sizeY;

        posX += skillbarXOffset;
        posY += skillbarYOffset;

        MagicData magicData = MagicData.getData(player);
        if (magicData.shouldRenderAbilities()) {
            guiGraphics.blit(VANILLA_WIDGETS, posX, posY - 2, 0, 0, 0, 41, 22, 256, 256);
            guiGraphics.blit(VANILLA_WIDGETS, posX + 41, posY - 2, 0, 141, 0, 41, 22, 256, 256);

            for (int x = 0; x < MagicData.MAX_ACTIVE; x++) {
                DragonAbilityInstance ability = magicData.fromSlot(x);

                if (ability != null) {
                    guiGraphics.blit(ability.getIcon(), posX + x * sizeX + 3, posY + 1, 0, 0, 16, 16, 16, 16);

                    float skillCooldown = ability.value().getCooldown(ability.level());
                    float currentCooldown = ability.getCooldown() - Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);

                    if (skillCooldown > 0 && currentCooldown > 0 && skillCooldown != currentCooldown) {
                        float f = Mth.clamp(currentCooldown / skillCooldown, 0, 1);
                        int boxX = posX + x * sizeX + 3;
                        int boxY = posY + 1;
                        int offset = 16 - (int)(16 - (f * 16));
                        int color = new Color(0.15F, 0.15F, 0.15F, 0.75F).getRGB();
                        int fColor = errorTicks > 0 ? new Color(1F, 0F, 0F, 0.75F).getRGB() : color;
                        guiGraphics.fill(boxX, boxY, boxX + 16, boxY + offset, fColor);
                    }
                }
            }

            guiGraphics.blit(VANILLA_WIDGETS, posX + sizeX * magicData.getSelectedAbilitySlot() - 1, posY - 3, 2, 0, 22, 24, 24, 256, 256);

            // Don't render more than two rows (1 icon = 1 mana point)
            // This makes the mana bars also stop just before the emote button when the chat window is open
            float maxMana = Math.min(20, ManaHandler.getMaxMana(player));
            float currentMana = Math.min(maxMana, ManaHandler.getCurrentMana(player));

            int manaX = i1;
            int manaY = height - sizeY;

            manaX += manabarXOffset;
            manaY += manabarYOffset;

            ResourceLocation manaIcons = DragonStateProvider.getData(player).getType().value().miscResources().manaSprites();

            for (int row = 0; row < 1 + Math.ceil(maxMana / 10); row++) {
                for (int point = 0; point < 10; point++) {
                    int manaSlot = row * 10 + point;

                    if (manaSlot < maxMana) {
                        int xPos;

                        if (currentMana <= manaSlot) {
                            // TODO :: have a partially-filled icon for 0.01..0.99 values?
                            //  would only apply to the last "filled" mana icon
                            xPos = ManaHandler.isRegeneratingMana(player) ? 19 : 37;
                        } else {
                            xPos = 0;
                        }

                        float rescale = 2.15F;
                        guiGraphics.blit(manaIcons, manaX + point * (int) (18 / rescale), manaY - 12 - row * ((int) (18 / rescale) + 1), xPos / rescale, 0, (int) (18 / rescale), (int) (18 / rescale), (int) (256 / rescale), (int) (256 / rescale));
                    }
                }
            }
        }

        if (magicData.isCasting()) {
            DragonAbilityInstance ability = magicData.fromSlot(magicData.getSelectedAbilitySlot());
            float currentCastTime = magicData.getClientCastTimer() - Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
            int skillCastTime = ability.getCastTime();
            if (skillCastTime > 0) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().scale(0.5F, 0.5F, 0);


                float perc = Math.clamp(1 - currentCastTime / (float) skillCastTime, 0, 1);

                int startX = width / 2 - 49 + castbarXOffset;
                int startY = height - 96 + castbarYOffset;

                guiGraphics.pose().translate(startX, startY, 0);

                DragonStateHandler handler = DragonStateProvider.getData(player);
                guiGraphics.blit(handler.getDragonType().value().miscResources().castBar(), startX, startY, 0, 0, 196, 47, 196, 47);

                software.bernie.geckolib.util.Color color = new software.bernie.geckolib.util.Color(handler.getType().value().miscResources().primaryColor().rgba());
                guiGraphics.setColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), 1F);
                guiGraphics.blit(CAST_BAR_FILL, startX + 2, startY + 41, 0, 0, (int) (191 * perc), 4, 191, 4);
                guiGraphics.setColor(1F, 1F, 1F, 1F);

                guiGraphics.blit(ability.getIcon(), startX + 78, startY + 3, 0, 0, 36, 36, 36, 36);

                guiGraphics.pose().popPose();
            }
        }

        if (errorTicks > 0) {
            guiGraphics.drawString(Minecraft.getInstance().font, errorMessage.getVisualOrderText(), (int) (width / 2f - Minecraft.getInstance().font.width(errorMessage) / 2f), height - 70, 0);
        }
    }
}