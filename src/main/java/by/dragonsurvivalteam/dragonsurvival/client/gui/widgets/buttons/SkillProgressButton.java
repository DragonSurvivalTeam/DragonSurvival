package by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAbilityScreen;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class SkillProgressButton extends Button {
    @Translation(type = Translation.Type.MISC, comments = "§6■ Requires level:§r %s")
    private static final String REQUIRED_LEVEL = Translation.Type.GUI.wrap("required_level");

    private static final ResourceLocation BLANK_TEXTURE = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/blank.png");

    private final int slot;
    private final DragonAbilityScreen screen;
    private DragonAbilityInstance buttonAbility;

    public SkillProgressButton(int x, int y, int slot, DragonAbilityScreen screen) {
        super(x, y, 16, 16, Component.empty(), button -> {}, DEFAULT_NARRATION);
        this.slot = slot;
        this.screen = screen;
    }

    @Override
    public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // FIXME
        /*ResourceLocation texture = buttonAbility != null ? buttonAbility.getIcon() : BLANK_TEXTURE;

        if (screen.unlockableAbilities.size() > slot) {
            ActiveDragonAbility ability1 = screen.unlockableAbilities.get(slot);

            if (ability1 != null && ability1.getIcon() != null) {
                texture = ability1.getIcon();
                buttonAbility = ability1;
            }
        } else {
            texture = BLANK_TEXTURE;
        }

        if (texture == BLANK_TEXTURE) {
            buttonAbility = null;
        }

        guiGraphics.blit(AbilityButton.BLANK_2_TEXTURE, getX() - 1, getY() - 1, 0, 0, 18, 18, 18, 18);
        guiGraphics.blit(texture, getX(), getY(), 0, 0, 16, 16, 16, 16);

        if (buttonAbility != null) {
            //noinspection DataFlowIssue -> player should not be null
            DragonStateHandler handler = DragonStateProvider.getData(Minecraft.getInstance().player);

            if (handler.isDragon()) {
                int level = DragonAbilities.getAbility(Minecraft.getInstance().player, buttonAbility.getClass(), handler.getType()).map(ActiveDragonAbility::getLevel).orElse(0);

                if (buttonAbility.getLevel() > level + 1) {
                    guiGraphics.fill(getX(), getY(), getX() + 16, getY() + 16, new Color(0.25F, 0.25F, 0.25F, 0.75F).getRGB());
                }

                if (isHoveredOrFocused()) {
                    ChatFormatting format = Objects.equals(handler.getType(), DragonTypes.CAVE) ? ChatFormatting.DARK_RED : Objects.equals(handler.getType(), DragonTypes.SEA) ? ChatFormatting.AQUA : Objects.equals(handler.getType(), DragonTypes.FOREST) ? ChatFormatting.GREEN : ChatFormatting.WHITE;
                    Component component = buttonAbility.getTitle();
                    ArrayList<Component> description = new ArrayList<>(List.of(Component.empty().append(component).append(" (" + buttonAbility.getLevel() + " / " + buttonAbility.getMaxLevel() + ")").withStyle(format)));

                    if (!buttonAbility.getLevelUpInfo().isEmpty()) {
                        description.addAll(buttonAbility.getLevelUpInfo());
                    }

                    int requiredLevel = buttonAbility.getCurrentRequiredLevel();

                    if (requiredLevel != -1) {
                        description.add(Component.translatable(REQUIRED_LEVEL, requiredLevel).withStyle(ChatFormatting.WHITE));
                    }

                    guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, description, mouseX, mouseY);
                }
            }
        }*/
    }
}