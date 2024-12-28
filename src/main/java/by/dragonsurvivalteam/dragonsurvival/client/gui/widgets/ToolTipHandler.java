package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAbilityScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.SkillProgressButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
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

import java.awt.*;
import java.util.Objects;

@EventBusSubscriber(Dist.CLIENT)
public class ToolTipHandler {
    private static final ResourceLocation TOOLTIP_BLINKING = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/magic_tips_1.png");
    private static final ResourceLocation TOOLTIP = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/magic_tips_0.png");
    private static final ResourceLocation ICONS = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "food_tooltip_icon_font");

    @Translation(key = "tooltip_changes", type = Translation.Type.CONFIGURATION, comments = "If enabled certain modifications to some tooltips will be made (e.g. dragon food items)")
    @ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "tooltip_changes")
    public static Boolean tooltipChanges = true;

    @Translation(key = "enchantment_descriptions", type = Translation.Type.CONFIGURATION, comments = "Adds enchantment descriptions to enchanted books which contain 1 enchantment (and said enchantment is from this mod)")
    @ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "enchantment_descriptions")
    public static Boolean enchantmentDescriptions = true;

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

        String nutritionIcon;
        String saturationIcon;
        ChatFormatting color;

        // FIXME
        // TODO :: need new entry in misc resources for the icon codes?
        //  not sure if there is a way to retrieve it from the resource / sprite
        /*switch (type) {
            case ForestDragonType ignored -> {
                nutritionIcon = "\uEA01";
                saturationIcon = "\uEA04";
                color = ChatFormatting.GREEN;
            }
            case CaveDragonType ignored -> {
                nutritionIcon = "\uEA02";
                saturationIcon = "\uEA05";
                color = ChatFormatting.RED;
            }
            case SeaDragonType ignored -> {
                nutritionIcon = "\uEA03";
                saturationIcon = "\uEA06";
                color = ChatFormatting.DARK_AQUA;
            }
            default -> throw new IllegalArgumentException("Invalid dragon type [" + type + "]");
        }*/

        nutritionIcon = "\uEA02";
        saturationIcon = "\uEA05";
        color = ChatFormatting.RED;

        FoodProperties properties = type.value().getDiet(item);

        if (properties == null) {
            // TODO :: only show custom diet in the tooltip?
            return Component.empty();
        }

        String nutrition;
        String saturation;

        float nutritionValue = properties.nutrition();
        float saturationValue = properties.saturation();

        // 1 Icon = 2 points (e.g. 10 nutrition icons for a maximum food level of 20)
        nutrition = String.format("%.1f", nutritionValue / 2);
        saturation = String.format("%.1f", saturationValue / 2);

        // Use white color to reset the color (i.e. don't color the icons)
        MutableComponent nutritionIconComponent = Component.literal(nutritionIcon).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withFont(ICONS));
        MutableComponent nutritionComponent = Component.literal(nutrition + " ").withStyle(color);

        MutableComponent saturationIconComponent = Component.literal(saturationIcon).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE).withFont(ICONS));
        MutableComponent saturationComponent = Component.literal(" / " + saturation + " ").withStyle(color);

        return nutritionComponent.append(nutritionIconComponent).append(saturationComponent).append(saturationIconComponent);
    }

    @SubscribeEvent // Add certain descriptions to our items which use generic classes
    @SuppressWarnings("DataFlowIssue") // resource key should be present
    public static void addCustomItemDescriptions(ItemTooltipEvent event) {
        if (event.getEntity() != null && event.getEntity().level().isClientSide() && event.getItemStack() != ItemStack.EMPTY) {
            ResourceLocation location = event.getItemStack().getItemHolder().getKey().location();
            MutableComponent description = null;

            if (enchantmentDescriptions && event.getItemStack().getItem() instanceof EnchantedBookItem) {
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
        if (!tooltipChanges) {
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
    public static void renderTooltipBorderInDragonColor(RenderTooltipEvent.Color event) {
        if (!tooltipChanges) {
            return;
        }

        boolean isAbilityScreen = Minecraft.getInstance().screen instanceof DragonAbilityScreen;
        ItemStack stack = event.getItemStack();

        // FIXME
        boolean isSeaFood = false;//dragonFoodTooltips && !stack.isEmpty() && DragonFoodHandler.getEdibleFoods(DragonTypes.SEA).contains(stack.getItem());
        boolean isForestFood = false;//dragonFoodTooltips && !stack.isEmpty() && DragonFoodHandler.getEdibleFoods(DragonTypes.FOREST).contains(stack.getItem());
        boolean isCaveFood = false;//dragonFoodTooltips && !stack.isEmpty() && DragonFoodHandler.getEdibleFoods(DragonTypes.CAVE).contains(stack.getItem());

        boolean isDragonFood = isSeaFood || isForestFood || isCaveFood;
        boolean isSkillProgressButtonHovered = false;

        if (isAbilityScreen) {
            for (GuiEventListener widget : ((DragonAbilityScreen) Minecraft.getInstance().screen).widgetList()) {
                if (widget instanceof SkillProgressButton && ((SkillProgressButton) widget).isHoveredOrFocused()) {
                    isSkillProgressButtonHovered = true;
                    break;
                }
            }
        }

        if (isHelpText()) {
            int top = new Color(154, 132, 154).getRGB();
            int bottom = new Color(89, 68, 89).getRGB();

            event.setBorderStart(top);
            event.setBorderEnd(bottom);
        } else if (isAbilityScreen || isDragonFood) {
            Holder<DragonType> type = DragonUtils.getType(DragonSurvival.PROXY.getLocalPlayer());
            Color topColor = null;
            Color bottomColor = null;

            if (type != null) {
                if (Objects.equals(type, DragonTypes.SEA) && isSkillProgressButtonHovered || isSeaFood) {
                    topColor = new Color(93, 201, 255);
                    bottomColor = new Color(49, 109, 144);
                } else if (Objects.equals(type, DragonTypes.FOREST) && isSkillProgressButtonHovered || isForestFood) {
                    topColor = new Color(0, 255, 148);
                    bottomColor = new Color(4, 130, 82);
                } else if (Objects.equals(type, DragonTypes.CAVE) && isSkillProgressButtonHovered || isCaveFood) {
                    topColor = new Color(255, 118, 133);
                    bottomColor = new Color(139, 66, 74);
                }
            }

            if (topColor != null) {
                event.setBorderStart(topColor.getRGB());
            }

            if (bottomColor != null) {
                event.setBorderEnd(bottomColor.getRGB());
            }
        }
    }
}