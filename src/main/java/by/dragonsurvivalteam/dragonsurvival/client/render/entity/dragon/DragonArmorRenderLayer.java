package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.DragonSurvivalClient;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.compat.car.CosmeticArmorReworkedHelper;
import by.dragonsurvivalteam.dragonsurvival.compat.curios.CurioAPIHelper;
import by.dragonsurvivalteam.dragonsurvival.compat.iris.InnerWrappedRenderType;
import by.dragonsurvivalteam.dragonsurvival.compat.iris.LayeringStates;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.GlStateBackup;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(value = Dist.CLIENT)
public class DragonArmorRenderLayer extends GeoRenderLayer<DragonEntity> {
    private final GeoEntityRenderer<DragonEntity> renderer;
    private static ShaderInstance armorGenerationShader;
    private static final Set<ResourceLocation> generatedArmorTextures = new HashSet<>();
    private static final Set<ResourceLocation> usedArmorTextures = new HashSet<>();

    public DragonArmorRenderLayer(final GeoEntityRenderer<DragonEntity> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    @Override
    public void render(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        Player player = animatable.getPlayer();

        if (player == null) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (hasAnyArmorEquipped(player) || ClawInventoryData.getData(player).shouldRenderClaws || hasVisibleCurios(player)) {
            constructTrimmedDragonArmorTexture(player).ifPresent(resourceLocation -> renderArmor(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, resourceLocation));
        }
    }

    private void renderArmor(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final MultiBufferSource bufferSource, float partialTick, int packedLight, final ResourceLocation texture) {
        if (animatable == null) {
            return;
        }

        DragonSurvivalClient.DRAGON_MODEL.setOverrideTexture(texture);
        RenderType type = renderer.getRenderType(animatable, texture, bufferSource, partialTick);

        if (type != null) {
            // Ensure that the armor is rendering on top of all the other layers
            InnerWrappedRenderType wrappedType = new InnerWrappedRenderType("dragon_armor", type, LayeringStates.VIEW_OFFSET_Z_LAYERING_FORWARD);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(wrappedType);
            renderer.actuallyRender(poseStack, animatable, bakedModel, wrappedType, bufferSource, vertexConsumer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, renderer.getRenderColor(animatable, partialTick, packedLight).getColor());
        }

        DragonSurvivalClient.DRAGON_MODEL.setOverrideTexture(null);
    }

    private static Optional<ResourceLocation> constructTrimmedDragonArmorTexture(final Player player) {
        RenderSystem.assertOnRenderThread();

        String armorUUID = buildUniqueArmorUUID(player);
        ResourceLocation imageResource = DragonSurvival.res("armor_" + armorUUID);

        if (!generatedArmorTextures.contains(imageResource)) {
            generateArmorTexture(player, imageResource);
            generatedArmorTextures.add(imageResource);
        }

        usedArmorTextures.add(imageResource);
        return Optional.of(imageResource);
    }

    @SubscribeEvent
    public static void purgeUnusedArmorTextures(final RenderFrameEvent.Pre event) {
        generatedArmorTextures.removeIf(texture -> {
            if (usedArmorTextures.contains(texture)) {
                return false;
            }

            Minecraft.getInstance().getTextureManager().release(texture);
            return true;
        });
        usedArmorTextures.clear();
    }

    private static void generateArmorTexture(final Player player, final ResourceLocation imageResource) {
        DragonStateHandler handler = DragonStateProvider.getData(player);
        DragonBody.TextureSize textureSize = handler.body().value().textureSize();
        GlStateBackup state = new GlStateBackup();
        RenderSystem.backupGlState(state);
        RenderSystem.backupProjectionMatrix();

        int framebuffer = GlStateManager.getBoundFramebuffer();
        int viewportX = GlStateManager.Viewport.x();
        int viewportY = GlStateManager.Viewport.y();
        int viewportWidth = GlStateManager.Viewport.width();
        int viewportHeight = GlStateManager.Viewport.height();
        int activeTexture = GlStateManager._getActiveTexture();
        int activeTextureBinding = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        int shaderProgram = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        int[] boundTextures = new int[3];

        for (int i = 0; i < boundTextures.length; i++) {
            RenderSystem.activeTexture(GlConst.GL_TEXTURE0 + i);
            boundTextures[i] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        }

        RenderSystem.activeTexture(activeTexture);
        RenderSystem.bindTexture(activeTextureBinding);
        RenderTarget target = null;

        try {
            target = new TextureTarget(textureSize.width(), textureSize.height(), false, Minecraft.ON_OSX);
            target.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            target.clear(true);

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.isArmor()) {
                    renderArmorSlot(target, player, handler, slot);
                }
            }

            if (ClawInventoryData.getData(player).shouldRenderClaws) {
                renderOptionalTexture(target, ClawsAndTeeth.constructClawTexture(player));
                renderOptionalTexture(target, ClawsAndTeeth.constructTeethTexture(player));
            }

            ArrayList<ItemStack> visibleCurios = CurioAPIHelper.getVisibleCurioItems(player);
            if (visibleCurios != null) {
                for (ItemStack itemStack : visibleCurios) {
                    renderOptionalTexture(target, toArmorResource(handler.getModel(), itemStack.getItem()));
                }
            }

            RenderingUtils.copyTextureFromRenderTarget(target, imageResource);
        } finally {
            if (target != null) {
                target.destroyBuffers();
            }

            RenderSystem.restoreGlState(state);
            RenderSystem.restoreProjectionMatrix();
            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, framebuffer);
            GlStateManager._viewport(viewportX, viewportY, viewportWidth, viewportHeight);

            for (int i = 0; i < boundTextures.length; i++) {
                RenderSystem.activeTexture(GlConst.GL_TEXTURE0 + i);
                RenderSystem.bindTexture(boundTextures[i]);
            }

            RenderSystem.activeTexture(activeTexture);
            RenderSystem.bindTexture(activeTextureBinding);
            GL20.glUseProgram(shaderProgram);
        }
    }

    private static void renderArmorSlot(final RenderTarget target, final Player player, final DragonStateHandler handler, final EquipmentSlot slot) {
        ItemStack stack = CosmeticArmorReworkedHelper.getItemVisibleInSlot(player, slot);

        if (stack.is(Items.AIR)) {
            return;
        }

        ResourceLocation armorLocation = generateArmorTextureResourceLocation(player, slot);

        if (!hasResource(armorLocation)) {
            return;
        }

        AbstractTexture armorTexture = Minecraft.getInstance().getTextureManager().getTexture(armorLocation);

        if (!(stack.getItem() instanceof ArmorItem)) {
            renderTextureToTarget(target, armorTexture, armorTexture, armorTexture, false, false, 0.0F, 0.0F, 0.0F, 0.0F);
            return;
        }

        ResourceLocation maskLocation = getArmorMaskResourceLocation(handler.getModel(), slot);

        if (!hasResource(maskLocation)) {
            DragonSurvival.LOGGER.error("Armor mask {} missing for model {}", maskLocation.getPath(), handler.getModel().getPath());
            return;
        }

        AbstractTexture maskTexture = Minecraft.getInstance().getTextureManager().getTexture(maskLocation);
        AbstractTexture trimTexture = armorTexture;
        boolean hasTrim = false;
        float trimHue = 0.0F;
        float trimSaturation = 0.0F;
        ArmorTrim trim = stack.get(DataComponents.TRIM);

        if (trim != null) {
            ResourceLocation trimLocation = ResourceLocation.fromNamespaceAndPath(
                handler.getModel().getNamespace(),
                "textures/armor/" + handler.getModel().getPath() + "/armor_trims/" + trim.pattern().value().assetId().getPath() + ".png"
            );

            if (hasResource(trimLocation)) {
                float[] trimBaseHSB = extractTrimBaseHSB(trim);
                trimTexture = Minecraft.getInstance().getTextureManager().getTexture(trimLocation);
                trimHue = trimBaseHSB[0];
                trimSaturation = trimBaseHSB[1];
                hasTrim = true;
            }
        }

        float dyeHue = 0.0F;
        float dyeSaturation = 0.0F;
        DyedItemColor dyeColor = stack.get(DataComponents.DYED_COLOR);

        if (dyeColor != null) {
            float[] dyeHSB = Color.RGBtoHSB(
                (dyeColor.rgb() >> 16) & 0xFF,
                (dyeColor.rgb() >> 8) & 0xFF,
                dyeColor.rgb() & 0xFF,
                null
            );
            dyeHue = dyeHSB[0];
            dyeSaturation = dyeHSB[1];
        }

        renderTextureToTarget(target, armorTexture, maskTexture, trimTexture, true, hasTrim, dyeHue, dyeSaturation, trimHue, trimSaturation);
    }

    private static void renderOptionalTexture(final RenderTarget target, final ResourceLocation textureLocation) {
        if (!hasResource(textureLocation)) {
            return;
        }

        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(textureLocation);
        renderTextureToTarget(target, texture, texture, texture, false, false, 0.0F, 0.0F, 0.0F, 0.0F);
    }

    private static void renderTextureToTarget(
        final RenderTarget target,
        final AbstractTexture armorTexture,
        final AbstractTexture maskTexture,
        final AbstractTexture trimTexture,
        final boolean hasMask,
        final boolean hasTrim,
        final float dyeHue,
        final float dyeSaturation,
        final float trimHue,
        final float trimSaturation
    ) {
        target.bindWrite(true);
        RenderSystem.enableBlend();
        RenderSystem.colorMask(true, true, true, true);
        RenderSystem.blendEquation(GlConst.GL_FUNC_ADD);
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.SRC_ALPHA,
            GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
            GlStateManager.SourceFactor.ONE,
            GlStateManager.DestFactor.ONE
        );
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        armorGenerationShader.setSampler("ArmorTexture", armorTexture);
        armorGenerationShader.setSampler("MaskTexture", maskTexture);
        armorGenerationShader.setSampler("TrimTexture", trimTexture);
        armorGenerationShader.getUniform("HasMask").set(hasMask ? 1.0F : 0.0F);
        armorGenerationShader.getUniform("ApplyDye").set(dyeHue != 0.0F || dyeSaturation != 0.0F ? 1.0F : 0.0F);
        armorGenerationShader.getUniform("HasTrim").set(hasTrim ? 1.0F : 0.0F);
        armorGenerationShader.getUniform("DyeHue").set(dyeHue);
        armorGenerationShader.getUniform("DyeSaturation").set(dyeSaturation);
        armorGenerationShader.getUniform("TrimHue").set(trimHue);
        armorGenerationShader.getUniform("TrimSaturation").set(trimSaturation);
        armorGenerationShader.apply();

        BufferBuilder buffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
        buffer.addVertex(0.0F, 0.0F, 0.0F);
        buffer.addVertex(1.0F, 0.0F, 0.0F);
        buffer.addVertex(1.0F, 1.0F, 0.0F);
        buffer.addVertex(0.0F, 1.0F, 0.0F);
        BufferUploader.draw(buffer.buildOrThrow());

        armorGenerationShader.clear();
        target.unbindWrite();
    }

    private static float[] extractTrimBaseHSB(final ArmorTrim trim) {
        ResourceLocation paletteResource = ResourceLocation.withDefaultNamespace("textures/trims/color_palettes/" + trim.material().value().assetName() + ".png");
        NativeImage colorPalette = RenderingUtils.getImageFromResource(paletteResource);

        if (colorPalette != null) {
            int red = 0;
            int green = 0;
            int blue = 0;
            int count = 0;

            try {
                for (int x = 0; x < colorPalette.getWidth(); x++) {
                    for (int y = 0; y < colorPalette.getHeight(); y++) {
                        Color color = new Color(colorPalette.getPixelRGBA(x, y), true);

                        if (color.getAlpha() != 0) {
                            red += color.getRed();
                            green += color.getGreen();
                            blue += color.getBlue();
                            count++;
                        }
                    }
                }
            } finally {
                colorPalette.close();
            }

            if (count > 0) {
                return Color.RGBtoHSB(red / count, green / count, blue / count, null);
            }
        }

        TextColor textColor = trim.material().value().description().getStyle().getColor();

        if (textColor != null) {
            int color = textColor.getValue();
            return Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
        }

        return new float[3];
    }

    private static ResourceLocation getArmorMaskResourceLocation(final ResourceLocation model, final EquipmentSlot slot) {
        return ResourceLocation.fromNamespaceAndPath(model.getNamespace(), "textures/armor/" + model.getPath() + "/armor_trims/masks/" + slot.getName() + "_mask.png");
    }

    private static boolean hasResource(final ResourceLocation resource) {
        return resource != null && Minecraft.getInstance().getResourceManager().getResource(resource).isPresent();
    }

    private static boolean hasAnyArmorEquipped(final Player player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }

            if (!CosmeticArmorReworkedHelper.getItemVisibleInSlot(player, slot).is(Items.AIR)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasVisibleCurios(final Player player) {
        ArrayList<ItemStack> visibleCurios = CurioAPIHelper.getVisibleCurioItems(player);
        return visibleCurios != null && !visibleCurios.isEmpty();
    }

    /**
     * Appends the following elements together to create an id <br>
     * - {@link DragonBody#model} (e.g. dragonsurvival.dragon_model <br>
     * - {@link ResourceLocation#toLanguageKey()} of each equipped armor item <br>
     * - {@link DataComponents#TRIM} of each equipped armor slot <br>
     * - {@link DataComponents#DYED_COLOR} of each equipped armor slot <br> <br>
     * - What is currently being used in the claw slot and teeth slot (i.e. tools/weapons)
     */
    private static String buildUniqueArmorUUID(final Player player) {
        StringBuilder armorTotal = new StringBuilder();
        String separator = "/";

        armorTotal.append(DragonStateProvider.getData(player).getModel().toLanguageKey());

        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (!equipmentSlot.isArmor()) {
                continue;
            }

            ItemStack stack = CosmeticArmorReworkedHelper.getItemVisibleInSlot(player, equipmentSlot);

            //noinspection DataFlowIssue -> key is present
            armorTotal.append(separator).append(separator).append(stack.getItemHolder().getKey().location().toLanguageKey());

            ArmorTrim trim = stack.getComponents().get(DataComponents.TRIM);

            if (trim != null) {
                armorTotal.append(separator).append(trim.material().value().assetName()).append(separator).append(trim.pattern().value().assetId());
            }

            DyedItemColor dyeColor = stack.get(DataComponents.DYED_COLOR);

            if (dyeColor != null) {
                armorTotal.append(separator).append(dyeColor.rgb());
            }
        }

        if (ClawInventoryData.getData(player).shouldRenderClaws) {
            armorTotal.append(separator).append(ClawsAndTeeth.constructClawTexture(player)).append(separator).append(ClawsAndTeeth.constructTeethTexture(player));
        }

        ArrayList<ItemStack> visibleCurios = CurioAPIHelper.getVisibleCurioItems(player);
        if (visibleCurios != null) {
            for (ItemStack curio : visibleCurios) {
                armorTotal.append(separator).append(curio.getDisplayName());
            }
        }

        return UUID.nameUUIDFromBytes(armorTotal.toString().getBytes()).toString();
    }

    private static ResourceLocation generateArmorTextureResourceLocation(Player player, EquipmentSlot equipmentSlot) {
        DragonStateHandler handler = DragonStateProvider.getData(player);
        Item item = CosmeticArmorReworkedHelper.getItemVisibleInSlot(player, equipmentSlot).getItem();
        ResourceLocation armorResource = toArmorResource(handler.getModel(), item);

        if (armorResource != null && Minecraft.getInstance().getResourceManager().getResource(armorResource).isPresent()) {
            return armorResource;
        }

        String texture = "textures/armor/" + handler.getModel().getPath() + "/";

        if (item instanceof ArmorItem armorItem) {
            //noinspection DataFlowIssue -> key is present
            ResourceLocation materialResource = armorItem.getMaterial().getKey().location();
            texture += materialResource.getNamespace() + "/materials/" + materialResource.getPath() + "/" + equipmentSlot.getName();

            if (armorItem.getMaterial() == ArmorMaterials.LEATHER && player.getItemBySlot(equipmentSlot).get(DataComponents.DYED_COLOR) == null) {
                // TODO :: Do we really have to make a special case for this? Do items not have a way to signify "can be dyed?"
                texture += "_undyed";
            }

            ResourceLocation resource = ResourceLocation.fromNamespaceAndPath(handler.getModel().getNamespace(), texture + ".png");

            if (Minecraft.getInstance().getResourceManager().getResource(resource).isPresent()) {
                return resource;
            } else if (materialResource.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
                DragonSurvival.LOGGER.warn("Missing vanilla armor texture for {} in model {}, falling back to generic armor.", resource.getPath(), handler.getModel().getPath());
            }

            String prefix;
            //noinspection deprecation -> ignore
            Holder.Reference<Item> holder = item.builtInRegistryHolder();

            if (holder.is(DSItemTags.EPIC_ARMOR)) {
                prefix = "epic";
            } else if (holder.is(DSItemTags.RARE_ARMOR)) {
                prefix = "rare";
            } else if (holder.is(DSItemTags.UNCOMMON_ARMOR)) {
                prefix = "uncommon";
            } else {
                prefix = "default";
            }

            return ResourceLocation.fromNamespaceAndPath(handler.getModel().getNamespace(), "textures/armor/" + handler.getModel().getPath() + "/" + prefix + "_" + equipmentSlot.getName() + ".png");
        }

        // Since this is just an empty image it should be applicable to all models
        return DragonSurvival.res("textures/armor/empty_armor.png");
    }

    private static ResourceLocation toArmorResource(final ResourceLocation model, final Item item) {
        if (item == Items.AIR) {
            return null;
        }

        //noinspection deprecation,DataFlowIssue -> ignore deprecated / key is present
        ResourceLocation itemResource = item.builtInRegistryHolder().getKey().location();
        String texture = "textures/armor/" + model.getPath() + "/" + itemResource.getNamespace() + "/" + itemResource.getPath() + ".png";
        return ResourceLocation.fromNamespaceAndPath(model.getNamespace(), texture);
    }

    @SubscribeEvent
    public static void registerShaders(final RegisterShadersEvent event) throws IOException {
        event.registerShader(
            new ShaderInstance(event.getResourceProvider(), DragonSurvival.res("armor_generation"), DefaultVertexFormat.BLIT_SCREEN),
            instance -> armorGenerationShader = instance
        );
    }
}
