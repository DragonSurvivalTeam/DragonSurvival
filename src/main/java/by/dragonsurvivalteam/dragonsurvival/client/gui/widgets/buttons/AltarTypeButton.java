package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAltarScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.DietComponent;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HoverDisableable;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.AltarBehaviour;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.StageResources;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncAltarCooldown;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.registry.data_maps.DietEntryCache;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class AltarTypeButton extends Button implements HoverDisableable {
    @Translation(comments = "You have awakened from your sleep, and become a human.")
    private static final String CHOICE_HUMAN = Translation.Type.GUI.wrap("altar.choice.human");

    @Translation(comments = {
            "§7■ §6Humans§r§f are the builders, travelers, and dreamers of this world.",
            "§2■ Features:§f§r§7 standard gameplay, varied diet, all items available for use.",
            "§4■ Weakness:§r§7 no flight, no progressive increase in HP, no magic.",
    })
    private static final String HUMAN = Translation.Type.GUI.wrap("altar.info.human");

    private static final ResourceLocation HUMAN_BANNER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/custom/altar/human/altar_icon.png");
    private static final ResourceLocation LOCKED_BANNER = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/altar/blocked_species.png");

    public final @Nullable AltarBehaviour.Entry entry;
    private final DragonAltarScreen parent;

    private boolean disableHover;
    private static final int MAX_SHOWN = 5;
    private int scroll;
    private boolean resetScroll;

    public AltarTypeButton(final DragonAltarScreen parent, @Nullable final AltarBehaviour.Entry entry, int x, int y) {
        super(x, y, 49, 147, Component.empty(), Button::onPress, DEFAULT_NARRATION);
        this.parent = parent;
        this.entry = entry;

        scroll = 0;
    }

    @Override
    public void onPress() {
        if (entry == null) {
            // Human
            initiateDragonForm(null);
        } else if (entry.isUnlocked()) {
            initiateDragonForm(entry.species());
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isHovered() && isTop(mouseY)) {
            scroll += (int) -scrollY; // invert the value so that scrolling down shows further entries
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHovered() && isTop(mouseY)) {
            if (resetScroll) {
                resetScroll = false;
                scroll = 0;
            }

            List<Either<FormattedText, TooltipComponent>> components = new ArrayList<>();

            if (entry != null) {
                List<Item> diet = DietEntryCache.getDietItems(entry.species());

                if (diet.size() <= MAX_SHOWN) {
                    scroll = 0;
                } else {
                    scroll = Math.clamp(scroll, 0, diet.size() - MAX_SHOWN);
                }

                int max = Math.min(diet.size(), scroll + MAX_SHOWN);

                // Using the color codes in the translation doesn't seem to apply the color to the entire text - therefor we create the [shown / max_items] tooltip part here
                MutableComponent shownFoods = Component.literal("[" + Math.min(diet.size(), scroll + MAX_SHOWN) + " / " + diet.size() + "]").withStyle(ChatFormatting.DARK_GRAY);
                //noinspection DataFlowIssue -> key is present
                components.addFirst(Either.left(Component.translatable(Translation.Type.DRAGON_SPECIES_ALTAR_DESCRIPTION.wrap(entry.species().getKey().location()), shownFoods)));

                for (int i = scroll; i < max; i++) {
                    components.add(Either.right(new DietComponent(entry.species(), diet.get(i))));
                }

                graphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, components, mouseX, mouseY, ItemStack.EMPTY);
            } else {
                components.addFirst(Either.left(Component.translatable(HUMAN)));
                graphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, components, mouseX, mouseY, ItemStack.EMPTY);
            }
        } else {
            resetScroll = true;
        }

        graphics.renderOutline(getX() - 1, getY() - 1, width + 2, height + 2, Color.black.getRGB());
        RenderSystem.enableBlend(); // Needs to happen after the outline for the transparent locked banner to render correctly

        if (entry != null) {
            graphics.blit(entry.species().value().miscResources().altarBanner(), getX(), getY(), 0, isHovered() ? 0 : 147, 49, 147, 49, 294);

            if (!entry.isUnlocked()) {
                graphics.blit(LOCKED_BANNER, getX(), getY(), 0, 0, 49, 147, 49, 147);
            }

            StageResources.GrowthIcon growthIcon = StageResources.getGrowthIcon(entry.species(), entry.species().value().getStartingStage(null).getKey());
            graphics.blit(isHovered() && isTop(mouseY) ? growthIcon.hoverIcon() : growthIcon.icon(), getX() + 1, getY() + 1, 0, 0, 18, 18, 18, 18);
        } else {
            graphics.blit(HUMAN_BANNER, getX(), getY(), 0, isHovered() ? 0 : 147, 49, 147, 49, 294);
        }

        RenderSystem.disableBlend();
    }

    private boolean isTop(double mouseY) {
        return mouseY > getY() + 6 && mouseY < getY() + 26;
    }

    public void initiateDragonForm(@Nullable final Holder<DragonSpecies> species) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        if (species == null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable(CHOICE_HUMAN));
            player.level().playSound(player, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);

            DragonStateHandler data = DragonStateProvider.getData(player);
            data.revertToHumanForm(player, false);
            PacketDistributor.sendToServer(new SyncAltarCooldown(Functions.secondsToTicks(ServerConfig.altarUsageCooldown)));
            PacketDistributor.sendToServer(new SyncComplete(player.getId(), data.serializeNBT(player.registryAccess())));

            player.closeContainer();
        } else {
            Minecraft.getInstance().setScreen(new DragonEditorScreen(parent, species));
        }
    }

    @Override
    public boolean isHovered() {
        return !disableHover && visible && super.isHovered();
    }

    @Override
    public boolean isFocused() {
        return !disableHover && visible && super.isFocused();
    }

    public void disableHover() {
        this.disableHover = true;
    }

    public void enableHover() {
        this.disableHover = false;
    }
}