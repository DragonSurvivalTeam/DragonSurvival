package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAltarScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.DietComponent;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
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

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.MODID;

public class AltarTypeButton extends Button {
    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/dragon_altar_icons.png");
    private static final ResourceLocation HUMAN_ALTAR_ICON = ResourceLocation.fromNamespaceAndPath(MODID, "textures/gui/custom/altar/human/altar_icon.png");

    @Translation(type = Translation.Type.MISC, comments = "You have awakened from your sleep, and become a human.")
    private static final String CHOICE_HUMAN = Translation.Type.GUI.wrap("altar.choice.human");

    @Translation(type = Translation.Type.MISC, comments = {
            "■ §nHuman.§r",
            "■ Homo sapiens.§r",
            "Travelers, builders, and creators."
    })
    private static final String HUMAN = Translation.Type.GUI.wrap("altar.info.human");

    public Holder<DragonType> type;
    private final DragonAltarScreen parent;

    private static final int MAX_SHOWN = 5;
    private int scroll;
    private boolean resetScroll;

    public AltarTypeButton(DragonAltarScreen parent, Holder<DragonType> type, int x, int y) {
        super(x, y, 49, 147, Component.empty(), Button::onPress, DEFAULT_NARRATION);
        this.parent = parent;
        this.type = type;

        scroll = 0;
    }

    @Override
    public void onPress() {
        initiateDragonForm(type);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isHovered() && isBottomOrTop(mouseY)) {
            scroll += (int) -scrollY; // invert the value so that scrolling down shows further entries
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    protected void renderWidget(@NotNull final GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (isHovered() && isBottomOrTop(mouseY)) {
            if (resetScroll) {
                resetScroll = false;
                scroll = 0;
            }

            List<Either<FormattedText, TooltipComponent>> components = new ArrayList<>();

            if (type != null) {
                List<Item> diet = type.value().getDietItems();

                if (diet.size() <= MAX_SHOWN) {
                    scroll = 0;
                } else {
                    scroll = Math.clamp(scroll, 0, diet.size() - MAX_SHOWN);
                }

                int max = Math.min(diet.size(), scroll + MAX_SHOWN);

                // Using the color codes in the translation doesn't seem to apply the color to the entire text - therefor we create the [shown / max_items] tooltip part here
                MutableComponent shownFoods = Component.literal(" [" + Math.min(diet.size(), scroll + MAX_SHOWN) + " / " + diet.size() + "]").withStyle(ChatFormatting.DARK_GRAY);
                //noinspection DataFlowIssue -> key is present
                components.addFirst(Either.left(Component.translatable(Translation.Type.DRAGON_TYPE_DESCRIPTION.wrap(type.getKey().location())).append(shownFoods)));

                for (int i = scroll; i < max; i++) {
                    components.add(Either.right(new DietComponent(type, diet.get(i))));
                }

                graphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, components, mouseX, mouseY, ItemStack.EMPTY);
            } else {
                components.addFirst(Either.left(Component.translatable(HUMAN)));
                graphics.renderComponentTooltipFromElements(Minecraft.getInstance().font, components, mouseX, mouseY, ItemStack.EMPTY);
            }
        } else {
            resetScroll = true;
        }

        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);

        graphics.fill(getX() - 1, getY() - 1, getX() + width, getY() + height, new Color(0.5f, 0.5f, 0.5f).getRGB());
        if(type != null) {
            graphics.blit(type.value().miscResources().altarBanner(), getX(), getY(), 0, isHovered ? 0 : 147, 49, 147, 49, 294);
        } else {
            graphics.blit(HUMAN_ALTAR_ICON, getX(), getY(), 0, isHovered ? 0 : 147, 49, 147, 49, 294);
        }
    }

    private boolean isBottomOrTop(double mouseY) {
        return mouseY > getY() + 6 && mouseY < getY() + 26 || mouseY > getY() + 133 && mouseY < getY() + 153;
    }

    private void initiateDragonForm(Holder<DragonType> type) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player == null) {
            return;
        }

        if (type == null) {
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable(CHOICE_HUMAN));

            DragonStateProvider.getOptional(player).ifPresent(cap -> {
                player.level().playSound(player, player.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 1, 0.7f);
                cap.revertToHumanForm(player, false);
                PacketDistributor.sendToServer(new SyncComplete.Data(player.getId(), cap.serializeNBT(player.registryAccess())));
            });

            player.closeContainer();
        } else {
            Minecraft.getInstance().setScreen(new DragonEditorScreen(parent, type));
        }
    }
}