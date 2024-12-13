package by.dragonsurvivalteam.dragonsurvival.client.gui.screens;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.AltarTypeButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.AltarData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStages;
import by.dragonsurvivalteam.dragonsurvival.util.ResourceHelper;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.Objects;

public class DragonAltarScreen extends Screen {
    @Translation(type = Translation.Type.MISC, comments = "Choose a Dragon Species")
    private static final String CHOOSE_SPECIES = Translation.Type.GUI.wrap("altar.choose_species");

    @Translation(type = Translation.Type.MISC, comments = {
            "You didn't make your initial choice in the Dragon Altar!",
            "You may still open the altar in your inventory."
    })
    private static final String NO_CHOICE = Translation.Type.GUI.wrap("altar.no_choice");

    @Translation(type = Translation.Type.MISC, comments = {
            "§6■ Welcome to Dragon Survival!§r",
            "■§7 You can choose which type of dragon you want to become, each with pros and cons.§r",
            "■§7 This decision is not permanent, but you may lose progress if you change your mind.§r",
            "■§7 Don't forget to delete old configs if you update our mod to avoid bugs! Enjoy the game! :3"
    })
    private static final String HELP = Translation.Type.GUI.wrap("altar.help");

    private static final ResourceLocation BACKGROUND_TEXTURE = ResourceLocation.withDefaultNamespace("textures/block/black_concrete.png");

    private final DragonStateHandler handler1 = new DragonStateHandler();
    private final DragonStateHandler handler2 = new DragonStateHandler();
    private final String[] animations = {"sit_animation", "idle_animation", "fly_animation", "swim_animation", "run_animation", "dig_animation", "resting_left_animation", "vibing_sitting", "shy_sitting", "vibing_sitting", "rocking_on_back"};

    private boolean hasInit = false;
    private int animation1 = 1;
    private int animation2 = 0;
    private int tick;

    public DragonAltarScreen() {
        super(Component.translatable(CHOOSE_SPECIES));
    }

    @Override
    public void onClose() {
        super.onClose();
        LocalPlayer player = Minecraft.getInstance().player;
        //noinspection DataFlowIssue -> player should not be null
        AltarData data = AltarData.getData(player);
        data.isInAltar = false;

        if (data.hasUsedAltar) {
            player.displayClientMessage(Component.translatable(NO_CHOICE), false);
        }
    }

    @Override
    public void render(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (minecraft == null) {
            return;
        }

        renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        tick++;

        if (tick % 200 * 20 == 0) {
            animation1++;
            animation2++;

            if (handler1.getBody() == null) {
                handler1.setBody(DragonBody.random(null));
            }

            handler2.setBody(handler1.getBody());
            handler1.setBody(DragonBody.random(null));

            if (animation1 >= animations.length) {
                animation1 = 0;
            }

            if (animation2 >= animations.length) {
                animation2 = 0;
            }
        }

        for (Renderable btn : renderables) {
            if (btn instanceof AltarTypeButton button) {
                if (button.isHoveredOrFocused()) {
                    handler1.setType(button.type);
                    handler2.setType(button.type);

                    if (button.type != null) {
                        initializeHandler(handler1);
                        initializeHandler(handler2);
                    }

                    FakeClientPlayerUtils.getFakePlayer(0, handler1).animationSupplier = () -> animations[animation1];
                    FakeClientPlayerUtils.getFakePlayer(1, handler2).animationSupplier = () -> animations[animation2];

                    LivingEntity entity1;
                    int entity1Scale = 40;
                    if (handler1.isDragon()) {
                        entity1 = FakeClientPlayerUtils.getFakeDragon(0, handler1);
                        entity1Scale = 20;
                    } else {
                        entity1 = FakeClientPlayerUtils.getFakePlayer(0, handler1);
                    }

                    LivingEntity entity2;
                    if (handler2.isDragon()) {
                        entity2 = FakeClientPlayerUtils.getFakeDragon(1, handler2);
                    } else {
                        entity2 = FakeClientPlayerUtils.getFakePlayer(1, handler2);
                    }

                    Quaternionf quaternion = Axis.ZP.rotationDegrees(180.0F);
                    quaternion.rotateY((float) Math.toRadians(150));
                    InventoryScreen.renderEntityInInventory(guiGraphics, (float) width / 2 + 170, button.getY() + button.getHeight(), entity1Scale, new Vector3f(), quaternion, null, entity1);

                    Quaternionf quaternion2 = Axis.ZP.rotationDegrees(180.0F);
                    quaternion2.rotateY((float) Math.toRadians(210));
                    InventoryScreen.renderEntityInInventory(guiGraphics, (float) width / 2 - 170, button.getY() + button.getHeight(), 40, new Vector3f(), quaternion2, null, entity2);
                }
            }
        }

        TextRenderUtil.drawCenteredScaledText(guiGraphics, width / 2, 10, 2f, title.getString(), DyeColor.WHITE.getTextColor());
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void initializeHandler(final DragonStateHandler handler) {
        if (handler.getBody() == null) {
            handler.setBody(DragonBody.random(null));
        }

        handler.setHasFlight(true);
        //noinspection DataFlowIssue -> registry is expected to be present
        Holder.Reference<DragonStage> dragonStage = CommonHooks.resolveLookup(DragonStage.REGISTRY).getOrThrow(DragonStages.adult);
        handler.setStage(null, dragonStage);
        handler.getSkinData().get(handler.getStage().getKey()).get().defaultSkin = true;
    }

    @Override
    public void renderBackground(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -300, -1072689136, -804253680);
        renderBorders(guiGraphics, BACKGROUND_TEXTURE, 0, width, 32, height - 32, width, height);
    }

    public static void renderBorders(@NotNull final GuiGraphics guiGraphics, ResourceLocation texture, int x0, int x1, int y0, int y1, int width, int height) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, texture);
        float zLevel = 0;

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);
        bufferbuilder.addVertex(x0, y0, zLevel).setUv(0.0F, (float) y0 / 32.0F).setColor(64, 64, 64, 55);
        bufferbuilder.addVertex(x0 + width, y0, zLevel).setUv((float) width / 32.0F, (float) y0 / 32.0F).setColor(64, 64, 64, 255);
        bufferbuilder.addVertex(x0 + width, 0.0F, zLevel).setUv((float) width / 32.0F, 0.0F).setColor(64, 64, 64, 255);
        bufferbuilder.addVertex(x0, 0.0F, zLevel).setUv(0.0F, 0.0F).setColor(64, 64, 64, 255);
        bufferbuilder.addVertex(x0, height, zLevel).setUv(0.0F, (float) height / 32.0F).setColor(64, 64, 64, 255);
        bufferbuilder.addVertex(x0 + width, height, zLevel).setUv((float) width / 32.0F, (float) height / 32.0F).setColor(64, 64, 64, 255);
        bufferbuilder.addVertex(x0 + width, y1, zLevel).setUv((float) width / 32.0F, (float) y1 / 32.0F).setColor(64, 64, 64, 255);
        bufferbuilder.addVertex(x0, y1, zLevel).setUv(0.0F, (float) y1 / 32.0F).setColor(64, 64, 64, 255);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        bufferbuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.addVertex(x0, y0 + 4, zLevel).setUv(0.0F, 1.0F).setColor(0, 0, 0, 0);
        bufferbuilder.addVertex(x1, y0 + 4, zLevel).setUv(1.0F, 1.0F).setColor(0, 0, 0, 0);
        bufferbuilder.addVertex(x1, y0, zLevel).setUv(1.0F, 0.0F).setColor(0, 0, 0, 255);
        bufferbuilder.addVertex(x0, y0, zLevel).setUv(0.0F, 0.0F).setColor(0, 0, 0, 255);
        bufferbuilder.addVertex(x0, y1, zLevel).setUv(0.0F, 1.0F).setColor(0, 0, 0, 255);
        bufferbuilder.addVertex(x1, y1, zLevel).setUv(1.0F, 1.0F).setColor(0, 0, 0, 255);
        bufferbuilder.addVertex(x1, y1 - 4, zLevel).setUv(1.0F, 0.0F).setColor(0, 0, 0, 0);
        bufferbuilder.addVertex(x0, y1 - 4, zLevel).setUv(0.0F, 0.0F).setColor(0, 0, 0, 0);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    @Override
    protected void init() {
        super.init();

        if (!hasInit) {
            hasInit = true;
        }

        int guiTop = (height - 190) / 2;

        addRenderableWidget(new HelpButton(width / 2, 32 + 5, 16, 16, HELP));
        int xPos = width / 2 - 104;
        for (ResourceKey<DragonType> key : ResourceHelper.keys(Minecraft.getInstance().level.registryAccess(), DragonType.REGISTRY)) {
            addRenderableWidget(new AltarTypeButton(this, Minecraft.getInstance().level.registryAccess().holderOrThrow(key), xPos, guiTop + 30));
            xPos += 53;
        }
        addRenderableWidget(new AltarTypeButton(this, null, xPos, guiTop + 30));
        addRenderableWidget(new ExtendedButton(width / 2 - 75, height - 25, 150, 20, Component.translatable(LangKey.GUI_DRAGON_EDITOR), action -> Minecraft.getInstance().setScreen(new DragonEditorScreen(Minecraft.getInstance().screen))) {
            @Override
            public void renderWidget(@NotNull final GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                visible = DragonStateProvider.isDragon(Objects.requireNonNull(minecraft).player);
                super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            }
        });
    }
}