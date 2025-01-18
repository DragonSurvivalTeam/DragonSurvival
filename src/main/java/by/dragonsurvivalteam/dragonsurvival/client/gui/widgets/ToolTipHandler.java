package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonAbilityHolder;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryCache;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class ToolTipHandler {
    @Translation(key = "tooltip_changes", type = Translation.Type.CONFIGURATION, comments = "If enabled certain modifications to some tooltips will be made (e.g. dragon food items)")
    @ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "tooltip_changes")
    public static Boolean TOOLTIP_CHANGES = true;

    @Translation(key = "enchantment_descriptions", type = Translation.Type.CONFIGURATION, comments = "Adds enchantment descriptions to enchanted books which contain 1 enchantment (and said enchantment is from this mod)")
    @ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "enchantment_descriptions")
    public static Boolean ENCHANTMENT_DESCRIPTIONS = true;

    @Translation(key = "food_tooltip_style", type = Translation.Type.CONFIGURATION, comments = {
            "Determines how dragon food tooltip is handled",
            "none: no food tooltip / default: always show current and others when shift is pressed / only_current: never show others / all_shift: show current and others only when shift is pressed"
    })
    @ConfigOption(side = ConfigSide.CLIENT, category = "tooltips", key = "food_tooltip_style")
    public static TooltipStyle TOOLTIP_STYLE = TooltipStyle.DEFAULT;

    @Translation(comments = "■ %s food: %s")
    private static final String DRAGON_FOOD = Translation.Type.GUI.wrap("tooltip.dragon_food");

    @Translation(comments = "§7■ Adds the following abilities: %s")
    private static final String ADDS_ABILITIES = Translation.Type.GUI.wrap("tooltip.adds_abilities");

    @Translation(comments = "§7■ Removes the following abilities: %s")
    private static final String REMOVES_ABILITIES = Translation.Type.GUI.wrap("tooltip.removes_abilities");

    @Translation(comments = "Press 'SHIFT' for more info")
    private static final String PRESS_SHIFT = Translation.Type.GUI.wrap("tooltip.shift");

    private static final ResourceLocation TOOLTIP_BLINKING = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/magic_tips_1.png");
    private static final ResourceLocation TOOLTIP = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/gui/magic_tips_0.png");

    private static boolean isBlinking;
    private static int tick;

    public enum TooltipStyle {NONE, DEFAULT, ONLY_CURRENT, ALL_SHIFT}

    @SubscribeEvent
    public static void addAbilityHolderInfo(final ItemTooltipEvent event) {
        DragonAbilityHolder holder = event.getItemStack().get(DSDataComponents.DRAGON_ABILITIES);

        if (holder == null) {
            return;
        }

        if (Screen.hasShiftDown()) {
            MutableComponent abilities = null;

            // TODO :: color abilities in red / green depending on whether the player already has it?
            //  might get too colorful? use gray-tones instead?
            for (Holder<DragonAbility> ability : holder.abilities()) {
                //noinspection DataFlowIssue -> key is present
                MutableComponent name = DSColors.dynamicValue(Component.translatable(Translation.Type.ABILITY.wrap(ability.getKey().location())));

                if (abilities == null) {
                    abilities = name;
                } else {
                    abilities.append(Component.literal(", ").withStyle(ChatFormatting.GRAY)).append(name);
                }
            }

            event.getToolTip().add(Component.translatable(holder.isRemoval() ? REMOVES_ABILITIES : ADDS_ABILITIES, abilities));
        } else {
            event.getToolTip().add(Component.translatable(PRESS_SHIFT).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    @SubscribeEvent
    public static void addDragonFoodTooltip(final ItemTooltipEvent event) {
        HolderLookup.Provider provider = event.getContext().registries();

        if (provider == null || TOOLTIP_STYLE == TooltipStyle.NONE) {
            return;
        }

        Item item = event.getItemStack().getItem();
        Holder<DragonSpecies> playerSpecies;

        if (TOOLTIP_STYLE != TooltipStyle.ALL_SHIFT && event.getEntity() != null) {
            playerSpecies = DragonStateProvider.getData(event.getEntity()).species();
        } else {
            playerSpecies = null;
        }

        if (playerSpecies != null) {
            MutableComponent tooltip = getTooltip(playerSpecies, item);

            if (tooltip != null) {
                event.getToolTip().add(tooltip);
            }
        }

        if (TOOLTIP_STYLE == TooltipStyle.ONLY_CURRENT) {
            return;
        }

        List<Component> dragonFoodTooltips = new ArrayList<>();

        provider.lookupOrThrow(DragonSpecies.REGISTRY).listElements().forEach(species -> {
            if (playerSpecies != null && species.is(playerSpecies)) {
                return;
            }

            MutableComponent tooltip = getTooltip(species, item);

            if (tooltip != null) {
                dragonFoodTooltips.add(tooltip);
            }
        });

        if (dragonFoodTooltips.isEmpty()) {
            return;
        }

        if (Screen.hasShiftDown()) {
            dragonFoodTooltips.forEach(tooltip -> event.getToolTip().add(tooltip));
        } else {
            event.getToolTip().add(Component.translatable(PRESS_SHIFT).withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    private static @Nullable MutableComponent getTooltip(final Holder<DragonSpecies> species, final Item item) {
        MutableComponent foodData = getFoodTooltipData(species, item);

        if (foodData.getContents() != PlainTextContents.EMPTY) {
            //noinspection DataFlowIssue -> key is present
            MutableComponent speciesTranslation = Component.translatable(Translation.Type.DRAGON_SPECIES.wrap(species.getKey().location()));
            return Component.translatable(DRAGON_FOOD, speciesTranslation.withStyle(foodData.getStyle()), foodData).withStyle(foodData.getStyle());
        }

        return null;
    }

    /** Returns a tooltip component in the format of '1.0 nutrition_icon / 0.5 saturation_icon' (color and icon depend on the dragon species) */
    public static MutableComponent getFoodTooltipData(final Holder<DragonSpecies> species, final Item item) {
        FoodProperties properties = DietEntryCache.getDiet(species, item);

        if (properties == null) {
            return Component.empty();
        }

        ResourceLocation font = species.value().miscResources().foodTooltip().font();
        String nutritionIcon = species.value().miscResources().foodTooltip().nutritionIcon();
        String saturationIcon = species.value().miscResources().foodTooltip().saturationIcon();

        // 1 Icon = 2 points (e.g. 10 nutrition icons for a maximum food level of 20)
        String nutrition = String.format("%.1f", properties.nutrition() / 2f);
        String saturation = String.format("%.1f", properties.saturation() / 2f);
        int color = species.value().miscResources().foodTooltip().color().map(TextColor::getValue).orElse(species.value().miscResources().primaryColor().getValue());

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
    public static void renderHelpTextCornerElements(final RenderTooltipEvent.Pre event) {
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
        } else if (DietEntryCache.getDiet(data.species(), event.getItemStack().getItem()) != null) {
            event.setBorderStart(DSColors.toARGB(data.species().value().miscResources().primaryColor()));
            event.setBorderEnd(DSColors.toARGB(data.species().value().miscResources().secondaryColor()));
        }
    }
}