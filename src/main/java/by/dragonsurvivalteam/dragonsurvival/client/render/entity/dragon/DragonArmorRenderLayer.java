package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.compat.car.CosmeticArmorReworkedHelper;
import by.dragonsurvivalteam.dragonsurvival.compat.curios.CurioAPIHelper;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.tags.DSItemTags;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.DestFactor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.SourceFactor;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class DragonArmorRenderLayer<R extends LivingEntityRenderState & GeoRenderState> extends GeoRenderLayer<DragonEntity, Void, R> {
    private static final Identifier ARMOR_GENERATION_SHADER = DragonSurvival.res("core/armor_generation");
    private static final RenderPipeline ARMOR_GENERATION_PIPELINE = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET)
        .withLocation(DragonSurvival.res("pipeline/armor_generation"))
        .withVertexShader(ARMOR_GENERATION_SHADER)
        .withFragmentShader(ARMOR_GENERATION_SHADER)
        .withSampler("ArmorTexture")
        .withSampler("MaskTexture")
        .withSampler("TrimTexture")
        .withUniform("ArmorGenerationInfo", com.mojang.blaze3d.shaders.UniformType.UNIFORM_BUFFER)
        .withColorTargetState(new ColorTargetState(new BlendFunction(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ONE)))
        .build();
    private static final int ARMOR_GENERATION_UBO_SIZE = new Std140SizeCalculator()
        .putFloat()
        .putFloat()
        .putFloat()
        .putFloat()
        .putFloat()
        .putFloat()
        .putFloat()
        .get();

    private static MappableRingBuffer armorGenerationUniformBuffer;
    private static @Nullable DynamicTexture transparentTexture;

    public DragonArmorRenderLayer(final DragonRenderer<R> renderer) {
        super(renderer);
    }

    public static void registerRenderPipelines(final RegisterRenderPipelinesEvent event) {
        event.registerPipeline(ARMOR_GENERATION_PIPELINE);
    }

    @Override
    public void submitRenderTask(final RenderPassInfo<R> renderPassInfo, final SubmitNodeCollector renderTasks) {
        if (!renderPassInfo.willRender() || !(renderer instanceof DragonRenderer<R> dragonRenderer)) {
            return;
        }

        DragonRenderer.DragonRenderData renderData = renderPassInfo.renderState().getGeckolibData(DragonRenderer.DRAGON_RENDER_DATA);

        if (renderData == null || renderData.player() == null || renderData.handler() == null) {
            return;
        }

        Player player = renderData.player();

        if (!hasAnyArmorEquipped(player) && !ClawInventoryData.getData(player).shouldRenderClaws && CurioAPIHelper.getVisibleCurioItems(player).isEmpty()) {
            return;
        }

        Optional<Identifier> armorTexture = constructTrimmedDragonArmorTexture(player);

        if (armorTexture.isEmpty() || !RenderingUtils.hasTexture(armorTexture.get())) {
            return;
        }

        dragonRenderer.submitRenderTasks(renderPassInfo, renderTasks.order(1), RenderTypes.armorTranslucent(armorTexture.get()));
    }

    private static Optional<Identifier> constructTrimmedDragonArmorTexture(final Player player) {
        RenderSystem.assertOnRenderThread();

        Identifier imageResource = DragonSurvival.res("armor_" + buildUniqueArmorUUID(player));

        if (!RenderingUtils.hasTexture(imageResource)) {
            generateArmorTexture(player, imageResource);
        }

        return RenderingUtils.hasTexture(imageResource) ? Optional.of(imageResource) : Optional.empty();
    }

    private static void generateArmorTexture(final Player player, final Identifier imageResource) {
        DragonStateHandler handler = DragonStateProvider.getData(player);
        DragonBody.TextureSize textureSize = handler.body().value().textureSize();
        TextureTarget target = new TextureTarget("Dragon Armor", textureSize.width(), textureSize.height(), false);

        try {
            clearTarget(target);

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (!slot.isArmor()) {
                    continue;
                }

                renderArmorSlot(target, player, handler, slot, textureSize.width(), textureSize.height());
            }

            if (ClawInventoryData.getData(player).shouldRenderClaws) {
                renderOptionalTexture(target, ClawsAndTeeth.constructClawTexture(player), textureSize.width(), textureSize.height());
                renderOptionalTexture(target, ClawsAndTeeth.constructTeethTexture(player), textureSize.width(), textureSize.height());
            }

            ArrayList<ItemStack> visibleCurios = CurioAPIHelper.getVisibleCurioItems(player);

            for (ItemStack itemStack : visibleCurios) {
                renderOptionalTexture(target, toArmorResource(handler.getModel(), itemStack.getItem()), textureSize.width(), textureSize.height());
            }

            RenderingUtils.copyTextureFromRenderTarget(target, imageResource);
        } finally {
            target.destroyBuffers();
        }
    }

    private static void renderArmorSlot(
        final TextureTarget target,
        final Player player,
        final DragonStateHandler handler,
        final EquipmentSlot slot,
        final int width,
        final int height
    ) {
        ItemStack stack = CosmeticArmorReworkedHelper.getItemVisibleInSlot(player, slot);

        if (stack.is(Items.AIR)) {
            return;
        }

        Identifier armorLocation = generateArmorTextureIdentifier(player, slot);

        if (!hasResource(armorLocation)) {
            return;
        }

        AbstractTexture armorTexture = Minecraft.getInstance().getTextureManager().getTexture(armorLocation);

        if (!isTextureSizedForTarget(armorTexture, width, height, armorLocation)) {
            return;
        }

        Identifier maskLocation = getArmorMaskIdentifier(handler.getModel(), slot);
        boolean hasMask = hasResource(maskLocation);

        if (!hasMask) {
            DragonSurvival.LOGGER.error("Armor mask {} missing for model {}", maskLocation.getPath(), handler.getModel().getPath());
            return;
        }

        AbstractTexture maskTexture = Minecraft.getInstance().getTextureManager().getTexture(maskLocation);

        if (!isTextureSizedForTarget(maskTexture, width, height, maskLocation)) {
            return;
        }

        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        ResourceKey<EquipmentAsset> equipmentAssetId = equippable == null ? null : equippable.assetId().orElse(null);
        ArmorGenerationSettings settings = ArmorGenerationSettings.plain();

        if (equipmentAssetId != null) {
            settings = createArmorGenerationSettings(stack, handler, equipmentAssetId, width, height);
        }

        renderTextureToTarget(target, armorTexture, maskTexture, settings.trimTexture(), true, settings);
    }

    private static ArmorGenerationSettings createArmorGenerationSettings(
        final ItemStack stack,
        final DragonStateHandler handler,
        final ResourceKey<EquipmentAsset> equipmentAssetId,
        final int width,
        final int height
    ) {
        float[] dyeHSB = new float[3];
        float[] trimBaseHSB = new float[3];
        boolean applyDye = false;
        boolean hasTrim = false;
        AbstractTexture trimTexture = getTransparentTexture();
        DyedItemColor dyedColor = stack.get(DataComponents.DYED_COLOR);

        if (dyedColor != null) {
            Color.RGBtoHSB(ARGB.red(dyedColor.rgb()), ARGB.green(dyedColor.rgb()), ARGB.blue(dyedColor.rgb()), dyeHSB);
            applyDye = dyeHSB[0] != 0.0F || dyeHSB[1] != 0.0F;
        }

        ArmorTrim trim = stack.get(DataComponents.TRIM);

        if (trim != null) {
            String patternPath = trim.pattern().value().assetId().getPath();
            Identifier trimLocation = Identifier.fromNamespaceAndPath(handler.getModel().getNamespace(), "textures/armor/" + handler.getModel().getPath() + "/armor_trims/" + patternPath + ".png");

            if (hasResource(trimLocation)) {
                AbstractTexture loadedTrimTexture = Minecraft.getInstance().getTextureManager().getTexture(trimLocation);

                if (isTextureSizedForTarget(loadedTrimTexture, width, height, trimLocation)) {
                    extractTrimBaseHSB(trim, equipmentAssetId, trimBaseHSB);
                    trimTexture = loadedTrimTexture;
                    hasTrim = true;
                }
            }
        }

        return new ArmorGenerationSettings(applyDye, hasTrim, dyeHSB[0], dyeHSB[1], trimBaseHSB[0], trimBaseHSB[1], trimTexture);
    }

    private static void renderOptionalTexture(final TextureTarget target, final @Nullable Identifier textureLocation, final int width, final int height) {
        if (!hasResource(textureLocation)) {
            return;
        }

        AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(textureLocation);

        if (!isTextureSizedForTarget(texture, width, height, textureLocation)) {
            return;
        }

        renderTextureToTarget(target, texture, getTransparentTexture(), getTransparentTexture(), false, ArmorGenerationSettings.plain());
    }

    private static void renderTextureToTarget(
        final TextureTarget target,
        final AbstractTexture armorTexture,
        final AbstractTexture maskTexture,
        final AbstractTexture trimTexture,
        final boolean hasMask,
        final ArmorGenerationSettings settings
    ) {
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();

        try (GpuBuffer.MappedView view = commandEncoder.mapBuffer(getArmorGenerationUniformBuffer().currentBuffer(), false, true)) {
            Std140Builder.intoBuffer(view.data())
                .putFloat(hasMask ? 1.0F : 0.0F)
                .putFloat(settings.applyDye() ? 1.0F : 0.0F)
                .putFloat(settings.hasTrim() ? 1.0F : 0.0F)
                .putFloat(settings.dyeHue())
                .putFloat(settings.dyeSaturation())
                .putFloat(settings.trimHue())
                .putFloat(settings.trimSaturation());
        }

        try (RenderPass renderPass = commandEncoder.createRenderPass(() -> "Dragon armor generation", target.getColorTextureView(), java.util.OptionalInt.empty())) {
            renderPass.setPipeline(ARMOR_GENERATION_PIPELINE);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("ArmorGenerationInfo", getArmorGenerationUniformBuffer().currentBuffer());
            renderPass.bindTexture("ArmorTexture", armorTexture.getTextureView(), armorTexture.getSampler());
            renderPass.bindTexture("MaskTexture", maskTexture.getTextureView(), maskTexture.getSampler());
            renderPass.bindTexture("TrimTexture", trimTexture.getTextureView(), trimTexture.getSampler());
            renderPass.draw(0, 3);
        }

        getArmorGenerationUniformBuffer().rotate();
    }

    private static void extractTrimBaseHSB(final ArmorTrim trim, final ResourceKey<EquipmentAsset> equipmentAssetId, final float[] trimBaseHSB) {
        String paletteName = trim.material().value().assets().assetId(equipmentAssetId).suffix();
        Identifier paletteResource = Identifier.withDefaultNamespace("textures/trims/color_palettes/" + paletteName + ".png");
        NativeImage colorPalette = RenderingUtils.getImageFromResource(paletteResource);

        if (colorPalette != null) {
            int red = 0;
            int green = 0;
            int blue = 0;
            int count = 0;

            try {
                for (int x = 0; x < colorPalette.getWidth(); x++) {
                    for (int y = 0; y < colorPalette.getHeight(); y++) {
                        int pixel = colorPalette.getPixel(x, y);

                        if (ARGB.alpha(pixel) == 0) {
                            continue;
                        }

                        red += ARGB.red(pixel);
                        green += ARGB.green(pixel);
                        blue += ARGB.blue(pixel);
                        count++;
                    }
                }
            } finally {
                colorPalette.close();
            }

            if (count > 0) {
                Color.RGBtoHSB(red / count, green / count, blue / count, trimBaseHSB);
                return;
            }
        }

        TextColor textColor = trim.material().value().description().getStyle().getColor();

        if (textColor != null) {
            Color.RGBtoHSB(ARGB.red(textColor.getValue()), ARGB.green(textColor.getValue()), ARGB.blue(textColor.getValue()), trimBaseHSB);
        }
    }

    private static void clearTarget(final TextureTarget target) {
        if (target.getColorTexture() != null) {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(target.getColorTexture(), 0);
        }
    }

    private static MappableRingBuffer getArmorGenerationUniformBuffer() {
        if (armorGenerationUniformBuffer == null) {
            armorGenerationUniformBuffer = new MappableRingBuffer(
                () -> "Dragon Armor Generation UBO",
                GpuBuffer.USAGE_MAP_WRITE | GpuBuffer.USAGE_UNIFORM,
                ARMOR_GENERATION_UBO_SIZE
            );
        }

        return armorGenerationUniformBuffer;
    }

    private static DynamicTexture getTransparentTexture() {
        if (transparentTexture == null) {
            transparentTexture = new DynamicTexture(() -> "Dragon Armor Transparent", 1, 1, true);
            transparentTexture.getPixels().setPixel(0, 0, 0);
            transparentTexture.upload();
        }

        return transparentTexture;
    }

    private static boolean isTextureSizedForTarget(final AbstractTexture texture, final int width, final int height, final Identifier resource) {
        if (texture.getTexture().getWidth(0) != width || texture.getTexture().getHeight(0) != height) {
            DragonSurvival.LOGGER.error("Armor texture {} does not match the expected size {}x{}", resource, width, height);
            return false;
        }

        return true;
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

    private static String buildUniqueArmorUUID(final Player player) {
        StringBuilder armorTotal = new StringBuilder();
        String separator = "/";

        armorTotal.append(DragonStateProvider.getData(player).getModel().toLanguageKey());

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }

            ItemStack stack = CosmeticArmorReworkedHelper.getItemVisibleInSlot(player, slot);
            armorTotal.append(separator).append(stack.typeHolder().unwrapKey().orElseThrow().identifier().toLanguageKey());

            ArmorTrim trim = stack.get(DataComponents.TRIM);

            if (trim != null) {
                armorTotal.append(separator).append(trim.material().value().assets().base().suffix()).append(separator).append(trim.pattern().value().assetId());
            }

            DyedItemColor dyeColor = stack.get(DataComponents.DYED_COLOR);

            if (dyeColor != null) {
                armorTotal.append(separator).append(dyeColor.rgb());
            }
        }

        if (ClawInventoryData.getData(player).shouldRenderClaws) {
            armorTotal.append(separator).append(ClawsAndTeeth.constructClawTexture(player)).append(separator).append(ClawsAndTeeth.constructTeethTexture(player));
        }

        for (ItemStack curio : CurioAPIHelper.getVisibleCurioItems(player)) {
            armorTotal.append(separator).append(curio.getDisplayName());
        }

        return UUID.nameUUIDFromBytes(armorTotal.toString().getBytes()).toString();
    }

    private static Identifier generateArmorTextureIdentifier(final Player player, final EquipmentSlot slot) {
        DragonStateHandler handler = DragonStateProvider.getData(player);
        ItemStack stack = CosmeticArmorReworkedHelper.getItemVisibleInSlot(player, slot);
        Item item = stack.getItem();
        Identifier armorResource = toArmorResource(handler.getModel(), item);

        if (armorResource != null && Minecraft.getInstance().getResourceManager().getResource(armorResource).isPresent()) {
            return armorResource;
        }

        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);

        if (equippable != null && equippable.assetId().isPresent()) {
            Identifier materialResource = equippable.assetId().orElseThrow().identifier();
            String path = "textures/armor/" + handler.getModel().getPath() + "/" + materialResource.getNamespace() + "/materials/" + materialResource.getPath() + "/" + slot.getName();

            if (materialResource.equals(Identifier.withDefaultNamespace("leather")) && stack.get(DataComponents.DYED_COLOR) == null) {
                path += "_undyed";
            }

            Identifier materialTexture = Identifier.fromNamespaceAndPath(handler.getModel().getNamespace(), path + ".png");

            if (Minecraft.getInstance().getResourceManager().getResource(materialTexture).isPresent()) {
                return materialTexture;
            }

            String prefix;
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

            return Identifier.fromNamespaceAndPath(handler.getModel().getNamespace(), "textures/armor/" + handler.getModel().getPath() + "/" + prefix + "_" + slot.getName() + ".png");
        }

        return DragonSurvival.res("textures/armor/empty_armor.png");
    }

    private static @Nullable Identifier toArmorResource(final Identifier model, final Item item) {
        if (item == Items.AIR) {
            return null;
        }

        Identifier itemResource = item.builtInRegistryHolder().unwrapKey().orElseThrow().identifier();
        return Identifier.fromNamespaceAndPath(model.getNamespace(), "textures/armor/" + model.getPath() + "/" + itemResource.getNamespace() + "/" + itemResource.getPath() + ".png");
    }

    private static Identifier getArmorMaskIdentifier(final Identifier model, final EquipmentSlot slot) {
        return Identifier.fromNamespaceAndPath(model.getNamespace(), "textures/armor/" + model.getPath() + "/armor_trims/masks/" + slot.getName() + "_mask.png");
    }

    private static boolean hasResource(final @Nullable Identifier resource) {
        return resource != null && Minecraft.getInstance().getResourceManager().getResource(resource).isPresent();
    }

    private record ArmorGenerationSettings(
        boolean applyDye,
        boolean hasTrim,
        float dyeHue,
        float dyeSaturation,
        float trimHue,
        float trimSaturation,
        AbstractTexture trimTexture
    ) {
        private static ArmorGenerationSettings plain() {
            return new ArmorGenerationSettings(false, false, 0.0F, 0.0F, 0.0F, 0.0F, getTransparentTexture());
        }
    }
}
