package by.dragonsurvivalteam.dragonsurvival.client.gui.hud;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscResources;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.magic.ManaHandler;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigRange;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.DSAttributes;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.MagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.activation.ChanneledActivation;
import by.dragonsurvivalteam.dragonsurvival.util.AnimationUtils;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
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

    @ConfigRange
    @Translation(key = "show_mana_text", type = Translation.Type.CONFIGURATION, comments = "Shows the available (non-reserved) mana in text form above the mana icons")
    @ConfigOption(side = ConfigSide.CLIENT, category = {"ui", "magic"}, key = "show_mana_text")
    public static boolean showManaText = false;

    public static final Identifier ID = DragonSurvival.res("magic_hud");

    // 1.20.6 moved a bunch of widgets around, so to keep compatibility with older versions, we need to use the old widgets texture
    public static final Identifier WIDGET_TEXTURES = Identifier.fromNamespaceAndPath(MODID, "textures/gui/widgets.png");

    private static final Identifier VANILLA_WIDGETS = Identifier.fromNamespaceAndPath(MODID, "textures/gui/pre-1.20.1-widgets.png");
    private static final Identifier CAST_BAR_FILL = Identifier.fromNamespaceAndPath(MODID, "textures/gui/cast_bar_fill.png");

    private static final NumberFormat FORMATTER = NumberFormat.getInstance();

    static {
        FORMATTER.setMinimumFractionDigits(2);
        FORMATTER.setMaximumFractionDigits(2);
    }

    public static class OutlineColorData {
        private int color;
        private double delay;
        private boolean pastDelay;

        public OutlineColorData(int color, double delay, boolean pastDelay) {
            this.color = color;
            this.delay = delay;
            this.pastDelay = pastDelay;
        }
    }

    private static boolean initializedDisabledAbilitiesColor = false;
    private static final OutlineColorData[] colors = new OutlineColorData[MagicData.HOTBAR_SLOTS];

    public static boolean renderExperienceBar(GuiGraphicsExtractor GuiGraphicsExtractor, int screenWidth) {
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

        Profiler.get().push("expLevel");

        if (localPlayer.getXpNeededForNextLevel() > 0) {
            int width = screenWidth / 2 - 91;

            int experienceProgress = (int) (localPlayer.experienceProgress * 183.0F);
            int height = guiScaledHeight - 32 + 3;
            GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, WIDGET_TEXTURES, width, height, 0, 0, 164, 182, 5, 256, 256);

            if (experienceProgress > 0) {
                GuiGraphicsExtractor.blit(RenderPipelines.GUI_TEXTURED, WIDGET_TEXTURES, width, height, 0, 0, 169, experienceProgress, 5, 256, 256);
            }
        }

        Profiler.get().pop();

        if (localPlayer.experienceLevel > 0) {
            Profiler.get().push("expLevel");

            String s = "" + localPlayer.experienceLevel;
            int width = (guiScaledWidth - Minecraft.getInstance().font.width(s)) / 2;
            int height = guiScaledHeight - 31 - 4;

            GuiGraphicsExtractor.text(Minecraft.getInstance().font, s, (width + 1), height, 0, false);
            GuiGraphicsExtractor.text(Minecraft.getInstance().font, s, (width - 1), height, 0, false);
            GuiGraphicsExtractor.text(Minecraft.getInstance().font, s, width, (height + 1), 0, false);
            GuiGraphicsExtractor.text(Minecraft.getInstance().font, s, width, (height - 1), 0, false);
            GuiGraphicsExtractor.text(Minecraft.getInstance().font, s, width, height, DSColors.RED, false);

            Profiler.get().pop();
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

    private static void lerpToColor(int slot, int color) {
        colors[slot].delay -= AnimationUtils.getDeltaSeconds();

        if (colors[slot].delay <= 0) {
            colors[slot].pastDelay = true;
        }

        if (!colors[slot].pastDelay) {
            return;
        }

        int currentColor = colors[slot].color;
        float lerpSpeed = (float) markDisabledAbilitiesRedLerpSpeed;
        float red = Mth.lerp(lerpSpeed, ARGB.redFloat(currentColor), ARGB.redFloat(color));
        float green = Mth.lerp(lerpSpeed, ARGB.greenFloat(currentColor), ARGB.greenFloat(color));
        float blue = Mth.lerp(lerpSpeed, ARGB.blueFloat(currentColor), ARGB.blueFloat(color));
        float alpha = Mth.lerp(lerpSpeed,ARGB.alphaFloat(currentColor), ARGB.alphaFloat(color));
        colors[slot].color = ARGB.colorFromFloat(alpha, red, green, blue);
    }

    public static void render(@NotNull final GuiGraphicsExtractor graphics, @NotNull final DeltaTracker tracker) {
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
                colors[i] = new OutlineColorData(ARGB.color(0xFF, DSColors.WHITE), disabledColorDelay, false);
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

        int hotbarX = graphics.guiWidth() - sizeX * MagicData.HOTBAR_SLOTS - 20;
        int posX = hotbarX;
        int posY = graphics.guiHeight() - sizeY;

        posX += skillbarXOffset;
        posY += skillbarYOffset;

        if (magic.shouldRenderAbilities()) {
            if (!magic.getActiveAbilities().isEmpty()) {
                for (int x = 0; x < MagicData.HOTBAR_SLOTS; x++) {
                    DragonAbilityInstance ability = magic.fromSlot(x);

                    if (ability != null) {
                        if (!ability.isEnabled()) {
                            // TODO :: what color is this and what is this check for?
                            if (colors[x].pastDelay && colors[x].color == 0) {
                                colors[x].delay = disabledColorDelay;
                                colors[x].pastDelay = false;
                            }

                            lerpToColor(x, ARGB.color(0xFF, DSColors.RED));
                        } else if (!ability.hasEnoughMana(player)) {
                            lerpToColor(x, ARGB.color(0xFF, DSColors.YELLOW));
                        } else {
                            colors[x].pastDelay = true;
                            colors[x].delay = 0;
                            lerpToColor(x, ARGB.color(0xFF, DSColors.WHITE));
                        }

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

                        graphics.blit(RenderPipelines.GUI_TEXTURED, VANILLA_WIDGETS, xPos, posY - 2, uOffset, 0, uWidth, 22, 256, 256, colors[x].color);

                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ability.getIcon(), posX + x * sizeX + 3, posY + 1, 16, 16);

                        float skillCooldown = ability.value().activation().getCooldown(ability.level());
                        float currentCooldown = ability.cooldown() - Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);

                        if (skillCooldown > 0 && currentCooldown > 0 && skillCooldown != currentCooldown) {
                            float cooldown = Mth.clamp(currentCooldown / skillCooldown, 0, 1);
                            int boxX = posX + x * sizeX + 3;
                            int boxY = posY + 1;
                            int offset = 16 - (int) (16 - (cooldown * 16));
                            int color = errorTicks > 0 ? DSColors.withAlpha(DSColors.RED, 0.75f) : DSColors.withAlpha(DSColors.DARK_GRAY, 0.75f);
                            graphics.fill(boxX, boxY, boxX + 16, boxY + offset, color);
                        }
                    } else {
                        graphics.blit(RenderPipelines.GUI_TEXTURED, VANILLA_WIDGETS, posX + x * 20, posY - 2, -50, x * 20, 0, 21, 22, 256, 256);
                    }
                }

                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        VANILLA_WIDGETS,
                        posX + sizeX * magic.getSelectedAbilitySlot() - 1,
                        posY - 3,
                        0,
                        22,
                        24,
                        24,
                        256,
                        256,
                        colors[magic.getSelectedAbilitySlot()].color
                );
            }

            float maxMana = ManaHandler.getMaxMana(player);
            float currentMana = magic.getAvailableMana();

            int manaX = hotbarX;
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

            int maxIcons = 9;
            int iconSize = 9;
            int y = 0;

            // Render up to 3 rows with 9 mana icons max.
            for (int row = 0; row < 3; row++) {
                if (row * maxIcons > maxMana + magic.getReservedMana()) {
                    break;
                }

                for (int point = 0; point < maxIcons; point++) {
                    int slot = row * iconSize + point;

                    if (slot > maxMana + magic.getReservedMana()) {
                        break;
                    }

                    int x = manaX + point * iconSize;
                    y = manaY - 13 - row * (iconSize + 1);

                    if (maxMana > 0 && maxMana > slot) {
                        if (magic.isCasting()) {
                            // No mana regeneration
                            blit(graphics, manaSprites.empty(), x, y, iconSize, 1, 1, 1, 1);
                        } else if (player.getAttributeValue(DSAttributes.MANA_REGENERATION) > player.getAttributeBaseValue(DSAttributes.MANA_REGENERATION)) {
                            // Fast mana regeneration
                            blit(graphics, manaSprites.recovery(), x, y, iconSize, 1, red, green, blue);
                        } else {
                            // Slow mana regeneration
                            blit(graphics, manaSprites.empty(), x, y, iconSize, 1, 1, 1, 1);
                            blit(graphics, manaSprites.recovery(), x, y, iconSize, deltaCounter, red, green, blue);
                        }
                    } else if (magic.getReservedMana() > 0 && magic.getReservedMana() > slot - maxMana) {
                        // Reserved mana (as long as at least half of the slot is reserved)
                        blit(graphics, manaSprites.reserved(), x, y, iconSize, 1, red, green, blue);
                    }

                    if (currentMana > slot) {
                        float percentage = Math.min(currentMana - slot, 1);

                        if (percentage < 1) {
                            // We can't do it precise in decimals because the relevant methods work in integers
                            graphics.enableScissor(x, y, x + (int) (iconSize * percentage + /* Round up */ 0.5), y + iconSize);
                            blit(graphics, manaSprites.full(), x, y, iconSize, 1, red, green, blue);
                            graphics.disableScissor();
                        } else {
                            blit(graphics, manaSprites.full(), x, y, iconSize, 1, red, green, blue);
                        }
                    }
                }
            }

            if (showManaText) {
                String manaText = FORMATTER.format(magic.getAvailableMana()) + " / " + FORMATTER.format(ManaHandler.getMaxMana(player));
                int drawX = hotbarX + (iconSize * maxIcons) / 2 - Minecraft.getInstance().font.width(manaText) / 2;
                RenderingUtils.renderTextWithOutline(graphics, manaText, drawX, y - 10);
            }
        }

        if (magic.isCasting()) {
            DragonAbilityInstance ability = Objects.requireNonNull(magic.fromSlot(magic.getSelectedAbilitySlot()));
            float currentTime = magic.getClientCastTimer() - Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
            int targetTime = ability.value().activation().getCastTime(ability.level());

            if (currentTime <= 0 && ability.value().activation() instanceof ChanneledActivation channeled && channeled.maxDuration().isPresent()) {
                // We passed the cast / charging time and now display what is left of the duration
                currentTime = magic.getClientTickTimer() - Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
                targetTime = (int) channeled.maxDuration().get().calculate(ability.level());
            }

            if (targetTime > 0) {
                graphics.pose().pushMatrix();

                graphics.pose().scale(0.5F, 0.5F);

                int startX = graphics.guiWidth() / 2 - 49 + castbarXOffset;
                int startY = graphics.guiHeight() - 96 + castbarYOffset;
                float percentage = Math.clamp(1 - currentTime / (float) targetTime, 0, 1);

                graphics.pose().translate(startX, startY);

                DragonStateHandler handler = DragonStateProvider.getData(player);
                graphics.blit(RenderPipelines.GUI_TEXTURED, handler.species().value().miscResources().castBar(), startX, startY, 0, 0, 196, 47, 196, 47);

                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        CAST_BAR_FILL,
                        startX + 2,
                        startY + 41,
                        0,
                        0,
                        (int) (191 * percentage),
                        4,
                        191,
                        4,
                        DSColors.toARGB(handler.species().value().miscResources().primaryColor())
                );

                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ability.getIcon(), startX + 78, startY + 3, 36, 36);

                graphics.pose().popMatrix();
            }
        }

        if (errorTicks > 0) {
            graphics.text(Minecraft.getInstance().font, errorMessage.getVisualOrderText(), (int) (graphics.guiWidth() / 2f - Minecraft.getInstance().font.width(errorMessage) / 2f), graphics.guiHeight() - 70, DSColors.NONE);
        }
    }

    @SuppressWarnings("SameParameterValue") // ignore
    private static void blit(final GuiGraphicsExtractor graphics, final Identifier resource, int x, int y, int size, float alpha, float red, float green, float blue) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                resource,
                x,
                y,
                0,
                0,
                size,
                size,
                size,
                size,
                ARGB.colorFromFloat(alpha, red, green, blue)
        );
    }
}
