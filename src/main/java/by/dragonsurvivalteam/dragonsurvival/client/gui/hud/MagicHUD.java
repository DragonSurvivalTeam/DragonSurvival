package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscResources;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ManaHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.mixins.client.GuiGraphicsAccess;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.util.Color;

import java.util.Objects;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

@EventBusSubscriber(value = Dist.CLIENT)
public class MagicHUD {
    @Translation(comments = "§fThis ability is §r§cnot ready§r§f yet!§r (%s)")
    public static final String COOLDOWN = Translation.Type.GUI.wrap("ability.cooldown");

    @ConfigRange(min = 0, max = 1)
    @Translation(key = "mark_disabled_abilities_red_lerp_speed", type = Translation.Type.CONFIGURATION, comments = "How fast the lerp speed is for marking disabled abilities red.")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "mark_disabled_abilities_red_lerp_speed")
    public static double markDisabledAbilitiesRedLerpSpeed = 0.05;

    @ConfigRange(min = 0)
    @Translation(key = "mark_disabled_abilities_red_delay", type = Translation.Type.CONFIGURATION, comments = "How long until the red overlay activates if an ability is disabled.")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "mark_disabled_abilities_red_delay")
    public static double disabledColorDelay = 0.8;

    @ConfigRange
    @Translation(key = "cast_bar_x_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the x position of the cast bar")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "cast_bar_x_offset")
    public static Integer castbarXOffset = 0;

    @ConfigRange
    @Translation(key = "cast_bar_y_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the y position of the cast bar")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "casterBarYPos")
    public static Integer castbarYOffset = 0;

    @ConfigRange
    @Translation(key = "skill_bar_x_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the x position of the skill bar")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "skill_bar_x_offset")
    public static Integer skillbarXOffset = 0;

    @ConfigRange
    @Translation(key = "skill_bar_y_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the x position of the skill bar")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "skill_bar_y_offset")
    public static Integer skillbarYOffset = 0;

    @ConfigRange
    @Translation(key = "mana_bar_x_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the x position of the mana bar")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "mana_bar_x_offset")
    public static Integer manabarXOffset = 0;

    @ConfigRange
    @Translation(key = "mana_bar_y_offset", type = Translation.Type.CONFIGURATION, comments = "Offset for the y position of the mana bar")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "mana_bar_y_offset")
    public static Integer manabarYOffset = 0;

    public static final ResourceLocation ID = DragonSurvival.res("magic_hud");

    // 1.20.6 moved a bunch of widgets around, so to keep compatibility with older versions, we need to use the old widgets texture
    public static final ResourceLocation WIDGET_TEXTURES = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/widgets.png");

    private static final ResourceLocation VANILLA_WIDGETS = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/pre-1.20.1-widgets.png");
    private static final ResourceLocation CAST_BAR_FILL = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/cast_bar_fill.png");

    public static class OutlineColorData {
        private Color color;
        private double delay;
        private boolean pastDelay;

        public OutlineColorData(Color color, double delay, boolean pastDelay) {
            this.color = color;
            this.delay = delay;
            this.pastDelay = pastDelay;
        }
    }

    private static boolean initializedDisabledAbilitiesColor = false;
    private static final OutlineColorData[] colors = new OutlineColorData[MagicData.HOTBAR_SLOTS];

    public static boolean renderExperienceBar(GuiGraphics guiGraphics, int screenWidth) {
        Player localPlayer = DragonSurvival.PROXY.getLocalPlayer();

        //noinspection DataFlowIssue -> instance is present
        if (localPlayer == null || !Minecraft.getInstance().gameMode.canHurtPlayer() || !Minecraft.getInstance().gameMode.hasExperience()) {
            return false;
        }

        DragonStateHandler handler = DragonStateProvider.getData(localPlayer);

        if (!handler.isDragon() || handler.species().value().manaHandling().manaXpConversion() == 0) {
            return false;
        }

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
            guiGraphics.drawString(Minecraft.getInstance().font, s, width, height, DSColors.RED, false);

            Minecraft.getInstance().getProfiler().pop();
        }

        return true;
    }

    private static int errorTicks;
    private static Component errorMessage;

    public static void castingError(final Component component) {
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

    private static float deltaCounter;
    private static boolean reverseCounter;

    private static void lerpToColor(int slot, Color color) {
        colors[slot].delay -= AnimationUtils.getDeltaSeconds();

        if (colors[slot].delay <= 0) {
            colors[slot].pastDelay = true;
        }

        if (!colors[slot].pastDelay) {
            return;
        }

        Color currentColor = colors[slot].color;
        float lerpSpeed = (float) markDisabledAbilitiesRedLerpSpeed;
        float red = Mth.lerp(lerpSpeed, currentColor.getRedFloat(), color.getRedFloat());
        float green = Mth.lerp(lerpSpeed, currentColor.getGreenFloat(), color.getGreenFloat());
        float blue = Mth.lerp(lerpSpeed, currentColor.getBlueFloat(), color.getBlueFloat());
        float alpha = Mth.lerp(lerpSpeed, currentColor.getAlphaFloat(), color.getAlphaFloat());
        colors[slot].color = Color.ofRGBA(red, green, blue, alpha);
    }

    public static void render(@NotNull final GuiGraphics graphics, @NotNull final DeltaTracker tracker) {
        if (Minecraft.getInstance().options.hideGui) {
            return;
        }

        Player player = Minecraft.getInstance().player;

        if (player == null || player.isSpectator()) {
            return;
        }

        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        MagicData magic = MagicData.getData(player);

        if (magic.getAbilities().isEmpty()) {
            return;
        }

        if (!initializedDisabledAbilitiesColor) {
            for (int i = 0; i < MagicData.HOTBAR_SLOTS; i++) {
                colors[i] = new OutlineColorData(Color.ofRGBA(1f, 1f, 1f, 1f), disabledColorDelay, false);
            }

            initializedDisabledAbilitiesColor = true;
        }

        // Blinking speed
        float delta = tracker.getGameTimeDeltaPartialTick(true) / 30;
        deltaCounter += reverseCounter ? -delta : delta;

        if (deltaCounter > 1) {
            reverseCounter = true;
            deltaCounter = 1;
        } else if (deltaCounter < 0) {
            reverseCounter = false;
            deltaCounter = 0;
        }

        int sizeX = 20;
        int sizeY = 20;

        int i1 = graphics.guiWidth() - sizeX * MagicData.HOTBAR_SLOTS - 20;
        int posX = i1;
        int posY = graphics.guiHeight() - sizeY;

        posX += skillbarXOffset;
        posY += skillbarYOffset;

        if (magic.shouldRenderAbilities()) {
            if (!magic.getActiveAbilities().isEmpty()) {
                graphics.setColor(1, 1, 1, 1);

                for (int x = 0; x < MagicData.HOTBAR_SLOTS; x++) {
                    DragonAbilityInstance ability = magic.fromSlot(x);

                    if (ability != null) {
                        if (!ability.isEnabled()) {
                            // TODO :: what color is this and what is this check for?
                            if (colors[x].pastDelay && colors[x].color.equals(Color.ofOpaque(-2314))) {
                                colors[x].delay = disabledColorDelay;
                                colors[x].pastDelay = false;
                            }

                            lerpToColor(x, Color.RED);
                        } else if (!ability.hasEnoughMana(player)) {
                            lerpToColor(x, Color.YELLOW);
                        } else {
                            colors[x].pastDelay = true;
                            colors[x].delay = 0;
                            lerpToColor(x, Color.WHITE);
                        }

                        Color outlineColor = colors[x].color;
                        graphics.setColor(outlineColor.getRedFloat(), outlineColor.getGreenFloat(), outlineColor.getBlueFloat(), outlineColor.getAlphaFloat());

                        int uOffset;
                        int uWidth;
                        int xPos;

                        if (x == 0) {
                            uOffset = 0;
                            uWidth = 21;
                            xPos = posX;
                        } else if (x != MagicData.HOTBAR_SLOTS - 1) {
                            uOffset = 21;
                            uWidth = 20;
                            xPos = posX + x * 20 + 1;
                        } else {
                            uOffset = 161;
                            uWidth = 21;
                            xPos = posX + x * 20 + 1;
                        }

                        graphics.blit(VANILLA_WIDGETS, xPos, posY - 2, -50, uOffset, 0, uWidth, 22, 256, 256);
                        graphics.setColor(1f, 1f, 1f, 1f);

                        graphics.blitSprite(ability.getIcon(), posX + x * sizeX + 3, posY + 1, 0, 16, 16);

                        float skillCooldown = ability.value().activation().getCooldown(ability.level());
                        float currentCooldown = ability.getCooldown() - Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);

                        if (skillCooldown > 0 && currentCooldown > 0 && skillCooldown != currentCooldown) {
                            float cooldown = Mth.clamp(currentCooldown / skillCooldown, 0, 1);
                            int boxX = posX + x * sizeX + 3;
                            int boxY = posY + 1;
                            int offset = 16 - (int) (16 - (cooldown * 16));
                            int color = errorTicks > 0 ? DSColors.withAlpha(DSColors.RED, 0.75f) : DSColors.withAlpha(DSColors.DARK_GRAY, 0.75f);
                            graphics.fill(boxX, boxY, boxX + 16, boxY + offset, color);
                        }
                    } else {
                        graphics.blit(VANILLA_WIDGETS, posX + x * 20, posY - 2, -50, x * 20, 0, 21, 22, 256, 256);
                    }
                }

                if (magic.getSelectedAbility() != null) {
                    Color outlineColor = colors[magic.getSelectedAbilitySlot()].color;
                    graphics.setColor(outlineColor.getRedFloat(), outlineColor.getGreenFloat(), outlineColor.getBlueFloat(), outlineColor.getAlphaFloat());
                }

                graphics.blit(VANILLA_WIDGETS, posX + sizeX * magic.getSelectedAbilitySlot() - 1, posY - 3, 2, 0, 22, 24, 24, 256, 256);
                graphics.setColor(1f, 1f, 1f, 1f);
            }

            float reservedMana = ManaHandler.getReservedMana(player);
            float maxMana = ManaHandler.getMaxMana(player);
            float currentMana = Math.min(maxMana, ManaHandler.getCurrentMana(player));

            int manaX = i1;
            int manaY = graphics.guiHeight() - sizeY;

            manaX += manabarXOffset;
            manaY += manabarYOffset;

            if (magic.getActiveAbilities().isEmpty()) {
                // Move the bar down a bit if there are no abilities to show
                manaY += 20;
            }

            float red = 1;
            float green = 1;
            float blue = 1;

            DragonStateHandler handler = DragonStateProvider.getData(player);
            MiscResources.ManaSprites manaSprites = handler.species().value().miscResources().manaSprites().orElse(null);

            if (manaSprites == null) {
                manaSprites = MiscResources.ManaSprites.DEFAULT;

                DSColors.RGB color = DSColors.RGB.of(handler.species().value().miscResources().primaryColor().getValue());
                red = color.red();
                green = color.green();
                blue = color.blue();
            }

            // Render up to 3 rows with 9 mana icons max.
            for (int row = 0; row < 3; row++) {
                for (int point = 0; point < 9; point++) {
                    int slot = row * 9 + point;

                    if (slot + 0.5 > currentMana) {
                        int x = manaX + point * 9;
                        int y = manaY - 13 - row * 10;

                        if (maxMana > 0 && maxMana >= slot + 0.5) {
                            if (magic.isCasting()) {
                                // No mana regeneration
                                blit(graphics, manaSprites.empty(), x, y, 9, 1, 1, 1, 1);
                            } else if (player.getAttributeValue(DSAttributes.MANA_REGENERATION) > player.getAttributeBaseValue(DSAttributes.MANA_REGENERATION)) {
                                // Fast mana regeneration
                                blit(graphics, manaSprites.recovery(), x, y, 9, 1, red, green, blue);
                            } else {
                                // Slow mana regeneration
                                blit(graphics, manaSprites.empty(), x, y, 9, 1, 1, 1, 1);
                                blit(graphics, manaSprites.recovery(), x, y, 9, deltaCounter, red, green, blue);
                            }
                        } else if (reservedMana > 0 && reservedMana >= slot + 0.5 - maxMana) {
                            // Reserved mana
                            blit(graphics, manaSprites.reserved(), x, y, 9, 1, red, green, blue);
                        }
                    } else {
                        blit(graphics, manaSprites.full(), manaX + point * 9, manaY - 13 - row * 10, 9, 1, red, green, blue);
                    }
                }
            }
        }

        if (magic.isCasting()) {
            DragonAbilityInstance ability = Objects.requireNonNull(magic.fromSlot(magic.getSelectedAbilitySlot()));
            float currentCastTime = magic.getClientCastTimer() - Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
            int skillCastTime = ability.value().activation().getCastTime(ability.level());

            if (skillCastTime > 0) {
                graphics.pose().pushPose();
                graphics.pose().scale(0.5F, 0.5F, 0);

                int startX = graphics.guiWidth() / 2 - 49 + castbarXOffset;
                int startY = graphics.guiHeight() - 96 + castbarYOffset;
                float percentage = Math.clamp(1 - currentCastTime / (float) skillCastTime, 0, 1);

                graphics.pose().translate(startX, startY, 0);

                DragonStateHandler handler = DragonStateProvider.getData(player);
                graphics.blit(handler.species().value().miscResources().castBar(), startX, startY, 0, 0, 196, 47, 196, 47);

                Color color = new Color(DSColors.toARGB(handler.species().value().miscResources().primaryColor()));
                graphics.setColor(color.getRedFloat(), color.getGreenFloat(), color.getBlueFloat(), color.getAlpha());
                graphics.blit(CAST_BAR_FILL, startX + 2, startY + 41, 0, 0, (int) (191 * percentage), 4, 191, 4);
                graphics.setColor(1, 1, 1, 1);

                graphics.blitSprite(ability.getIcon(), startX + 78, startY + 3, 0, 36, 36);

                graphics.pose().popPose();
            }
        }

        if (errorTicks > 0) {
            graphics.drawString(Minecraft.getInstance().font, errorMessage.getVisualOrderText(), (int) (graphics.guiWidth() / 2f - Minecraft.getInstance().font.width(errorMessage) / 2f), graphics.guiHeight() - 70, DSColors.NONE);
        }
    }

    @SuppressWarnings("SameParameterValue") // ignore
    private static void blit(final GuiGraphics graphics, final ResourceLocation resource, int x, int y, int size, float alpha, float red, float green, float blue) {
        ((GuiGraphicsAccess) graphics).dragonSurvival$innerBlit(resource, x, x + size, y, y + size, 0, 0, 1, 0, 1, red, green, blue, alpha);
    }
}