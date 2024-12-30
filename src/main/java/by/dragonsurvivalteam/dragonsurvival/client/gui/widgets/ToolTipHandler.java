package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import org.joml.Vector2ic;

@EventBusSubscriber(Dist.CLIENT)
public class ToolTipHandler {
    @Translation(key = "tooltip_changes", type = Translation.Type.CONFIGURATION, comments = "If enabled certain modifications to some tooltips will be made (e.g. dragon food items)")
    @ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "tooltip_changes")
    public static Boolean TOOLTIP_CHANGES = true;

    @Translation(key = "enchantment_descriptions", type = Translation.Type.CONFIGURATION, comments = "Adds enchantment descriptions to enchanted books which contain 1 enchantment (and said enchantment is from this mod)")
    @ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "enchantment_descriptions")
    public static Boolean ENCHANTMENT_DESCRIPTIONS = true;

    private static final ResourceLocation TOOLTIP_BLINKING = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/magic_tips_1.png");
    private static final ResourceLocation TOOLTIP = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/magic_tips_0.png");

    private static boolean isBlinking;
    private static int tick;

    @SubscribeEvent
    public static void addDragonFoodTooltip(final ItemTooltipEvent event) {
        if (event.getEntity() != null) {
            DragonStateHandler data = DragonStateProvider.getData(event.getEntity());

            if (!data.isDragon()) {
                return;
            }

            MutableComponent foodData = getFoodTooltipData(event.getItemStack().getItem(), data.getType());

            if (foodData.getContents() != PlainTextContents.EMPTY) {
                event.getToolTip().add(foodData);
            }
        }
    }

    /** Returns a tooltip component in the format of '1.0 nutrition_icon / 0.5 saturation_icon' (color and icon depend on the dragon type) */
    public static MutableComponent getFoodTooltipData(final Item item, final Holder<DragonType> type) {
        if (type == null) {
            return Component.empty();
        }

        FoodProperties properties = type.value().getDiet(item);

        if (properties == null) {
            return Component.empty();
        }

        ResourceLocation font = type.value().miscResources().foodTooltip().font();
        String nutritionIcon = type.value().miscResources().foodTooltip().nutritionIcon();
        String saturationIcon = type.value().miscResources().foodTooltip().saturationIcon();

        // 1 Icon = 2 points (e.g. 10 nutrition icons for a maximum food level of 20)
        String nutrition = String.format("%.1f", properties.nutrition() / 2f);
        String saturation = String.format("%.1f", properties.saturation() / 2f);
        int color = type.value().miscResources().foodTooltip().color().map(TextColor::getValue).orElse(type.value().miscResources().primaryColor().getValue());

        MutableComponent nutritionComponent = Component.literal(nutrition + " ").withStyle(Style.EMPTY.withColor(color));
        MutableComponent saturationComponent = Component.literal(" / " + saturation + " ").withStyle(Style.EMPTY.withColor(color));

        return nutritionComponent.append(parseIcon(nutritionIcon, font)).append(saturationComponent).append(parseIcon(saturationIcon, font));
    }

    private static Component parseIcon(final String icon, final ResourceLocation font) {
        String actualIcon;

        if (icon.length() == 6 && icon.startsWith("\\u")) {
            // Since the icon needs to be escaped when defining it in the .json as Unicode
            // we need to transform it into the character it's supposed to represent
            actualIcon = new String(Character.toChars(Integer.parseInt(icon.substring(2), 16)));
        } else {
            actualIcon = icon;
        }

        // Use white color to reset the color (i.e. don't color the icons)
        return Component.literal(actualIcon).withStyle(Style.EMPTY.withFont(font).withColor(ChatFormatting.WHITE));
    }

    @SubscribeEvent // Add certain descriptions to our items which use generic classes
    @SuppressWarnings("DataFlowIssue") // resource key should be present
    public static void addCustomItemDescriptions(final ItemTooltipEvent event) {
        if (event.getEntity() != null && event.getEntity().level().isClientSide() && event.getItemStack() != ItemStack.EMPTY) {
            ResourceLocation location = event.getItemStack().getItemHolder().getKey().location();
            MutableComponent description = null;

            if (ENCHANTMENT_DESCRIPTIONS && event.getItemStack().getItem() instanceof EnchantedBookItem) {
                ItemEnchantments enchantments = event.getItemStack().get(DataComponents.STORED_ENCHANTMENTS);

                // Only add it to single-entry enchanted books since the text is longer than usual enchantment descriptions
                if (enchantments != null && enchantments.size() == 1) {
                    Holder<Enchantment> holder = enchantments.entrySet().iterator().next().getKey();
                    ResourceKey<Enchantment> resourceKey = holder.getKey();

                    if (resourceKey.location().getNamespace().equals(DragonSurvival.MODID)) {
                        description = Component.translatable(Translation.Type.ENCHANTMENT_DESCRIPTION.wrap(resourceKey.location().getPath())).withStyle(ChatFormatting.DARK_GRAY);
                    }
                }
            } else if (location.getNamespace().equals(DragonSurvival.MODID)) {
                /* TODO
                    do this via mixin in 'ItemStack#getTooltipLines' at the point below?
                    so that the tooltip behaves the same as a regular tooltip
                    (above custom things like enchantments, attributes or the advanced tooltip)

                if (!this.has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
                    this.getItem().appendHoverText(this, tooltipContext, list, tooltipFlag);
                }
                */

                String translationKey = Translation.Type.DESCRIPTION_ADDITION.wrap(location.getPath());

                if (I18n.exists(translationKey)) {
                    description = Component.translatable(translationKey);
                }
            }

            if (description != null) {
                event.getToolTip().add(description);
            }
        }
    }

    @SubscribeEvent
    public static void renderHelpTextCornerElements(RenderTooltipEvent.Pre event) {
        boolean render = isHelpText();

        if (!render) {
            return;
        }

        if (!isBlinking) {
            if (tick >= Functions.secondsToTicks(30)) {
                isBlinking = true;
                tick = 0;
            }
        } else {
            if (tick >= Functions.secondsToTicks(5)) {
                isBlinking = false;
                tick = 0;
            }
        }

        tick++;

        // Logic to determine width / height is from 'GuiGraphics#renderTooltipInternal'
        int width = 0;
        int height = event.getComponents().size() == 1 ? -2 : 0;

        for (ClientTooltipComponent component : event.getComponents()) {
            int componentWidth = component.getWidth(event.getFont());

            if (componentWidth > width) {
                width = componentWidth;
            }

            height += component.getHeight();
        }

        Vector2ic tooltipPosition = event.getTooltipPositioner().positionTooltip(event.getScreenWidth(), event.getScreenHeight(), event.getX(), event.getY(), width, height);

        int x = tooltipPosition.x();
        int y = tooltipPosition.y();

        int textureWidth = 128;
        int textureHeight = 128;

        event.getGraphics().blit(isBlinking ? TOOLTIP_BLINKING : TOOLTIP, x - 8 - 6, y - 8 - 6, 400, 1, 1 % textureHeight, 16, 16, textureWidth, textureHeight);
        event.getGraphics().blit(isBlinking ? TOOLTIP_BLINKING : TOOLTIP, x + width - 8 + 6, y - 8 - 6, 400, textureWidth - 16 - 1, 1 % textureHeight, 16, 16, textureWidth, textureHeight);

        event.getGraphics().blit(isBlinking ? TOOLTIP_BLINKING : TOOLTIP, x - 8 - 6, y + height - 8 + 6, 400, 1, 1 % textureHeight + 16, 16, 16, textureWidth, textureHeight);
        event.getGraphics().blit(isBlinking ? TOOLTIP_BLINKING : TOOLTIP, x + width - 8 + 6, y + height - 8 + 6, 400, textureWidth - 16 - 1, 1 % textureHeight + 16, 16, 16, textureWidth, textureHeight);

        event.getGraphics().blit(isBlinking ? TOOLTIP_BLINKING : TOOLTIP, x + width / 2 - 47, y - 16, 400, 16 + 2 * textureWidth + 1, 1 % textureHeight, 94, 16, textureWidth, textureHeight);
        event.getGraphics().blit(isBlinking ? TOOLTIP_BLINKING : TOOLTIP, x + width / 2 - 47, y + height, 400, 16 + 2 * textureWidth + 1, 1 % textureHeight + 16, 94, 16, textureWidth, textureHeight);
    }

    private static boolean isHelpText() {
        if (!TOOLTIP_CHANGES) {
            return false;
        }

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null || minecraft.screen == null) {
            return false;
        }

        for (GuiEventListener element : minecraft.screen.children()) {
            if (element instanceof HelpButton helpButton && helpButton.isHovered()) {
                return true;
            }
        }

        return false;
    }

    @SubscribeEvent
    public static void renderTooltipBorderInDragonColor(final RenderTooltipEvent.Color event) {
        if (!TOOLTIP_CHANGES) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(player);

        if (!data.isDragon()) {
            return;
        }

        if (isHelpText()) {
            event.setBorderStart(DSColors.withAlpha(DSColors.LIGHT_PURPLE, 1));
            event.setBorderEnd(DSColors.withAlpha(DSColors.DARK_PURPLE, 1));
        } else if (data.getType().value().getDiet(event.getItemStack().getItem()) != null) {
            event.setBorderStart(DSColors.toARGB(data.getType().value().miscResources().primaryColor()));
            event.setBorderEnd(DSColors.toARGB(data.getType().value().miscResources().secondaryColor()));
        }
    }
}