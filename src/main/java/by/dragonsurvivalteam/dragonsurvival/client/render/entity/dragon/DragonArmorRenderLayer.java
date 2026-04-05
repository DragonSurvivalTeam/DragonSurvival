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
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
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
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.GeoRenderLayer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DragonArmorRenderLayer<R extends LivingEntityRenderState & GeoRenderState> extends GeoRenderLayer<DragonEntity, Void, R> {
    private static final Map<Identifier, CompletableFuture<Void>> ARMOR_TEXTURES = new HashMap<>();
    private static final Map<Identifier, Map<EquipmentSlot, NativeImage>> ARMOR_MASKS_PER_MODEL = new HashMap<>();

    public DragonArmorRenderLayer(final DragonRenderer<R> renderer) {
        super(renderer);
    }

    @Override
    public void submitRenderTask(final RenderPassInfo<R> renderPassInfo, final SubmitNodeCollector renderTasks) {
        if (!renderPassInfo.willRender() || !(renderer instanceof DragonRenderer<R> dragonRenderer) || !dragonRenderer.shouldRenderLayers) {
            return;
        }

        DragonRenderer.DragonRenderData renderData = renderPassInfo.renderState().getGeckolibData(DragonRenderer.DRAGON_RENDER_DATA);

        if (renderData == null || renderData.player() == null || renderData.handler() == null) {
            return;
        }

        Player player = renderData.player();
        DragonStateHandler handler = renderData.handler();

        ARMOR_MASKS_PER_MODEL.computeIfAbsent(handler.getModel(), DragonArmorRenderLayer::loadArmorMasks);

        if (!hasAnyArmorEquipped(player) && !ClawInventoryData.getData(player).shouldRenderClaws && CurioAPIHelper.getVisibleCurioItems(player).isEmpty()) {
            return;
        }

        Optional<Identifier> armorTexture = constructTrimmedDragonArmorTexture(player);

        if (armorTexture.isEmpty() || !RenderingUtils.hasTexture(armorTexture.get())) {
            return;
        }

        dragonRenderer.isRenderingLayer = true;

        try {
            dragonRenderer.submitRenderTasks(renderPassInfo, renderTasks.order(1), RenderTypes.armorTranslucent(armorTexture.get()));
        } finally {
            dragonRenderer.isRenderingLayer = false;
        }
    }

    private static Map<EquipmentSlot, NativeImage> loadArmorMasks(final Identifier model) {
        Map<EquipmentSlot, NativeImage> masks = new HashMap<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }

            Identifier resource = Identifier.fromNamespaceAndPath(model.getNamespace(), "textures/armor/" + model.getPath() + "/armor_trims/masks/" + slot.getName() + "_mask.png");
            NativeImage mask = RenderingUtils.getImageFromResource(resource);

            if (mask == null) {
                DragonSurvival.LOGGER.error("Armor mask {} missing for model {}", resource.getPath(), model.getPath());
                continue;
            }

            masks.put(slot, mask);
        }

        return masks;
    }

    private static Optional<Identifier> constructTrimmedDragonArmorTexture(final Player player) {
        String armorUUID = buildUniqueArmorUUID(player);
        Identifier imageResource = DragonSurvival.res("armor_" + armorUUID);
        CompletableFuture<Void> future = ARMOR_TEXTURES.get(imageResource);

        if (future != null) {
            return future.isDone() ? Optional.of(imageResource) : Optional.empty();
        }

        CompletableFuture<Void> uploadStep = CompletableFuture.supplyAsync(() -> compileArmorTexture(player))
            .thenAcceptAsync(image -> RenderingUtils.uploadTexture(image, imageResource), Minecraft.getInstance());
        ARMOR_TEXTURES.put(imageResource, uploadStep);
        return Optional.empty();
    }

    private static NativeImage compileArmorTexture(final Player player) {
        DragonStateHandler handler = DragonStateProvider.getData(player);
        Identifier model = handler.getModel();
        DragonBody.TextureSize textureSize = handler.body().value().textureSize();
        Map<EquipmentSlot, NativeImage> armorMasks = ARMOR_MASKS_PER_MODEL.get(model);

        if (armorMasks == null || armorMasks.isEmpty()) {
            return new NativeImage(textureSize.width(), textureSize.height(), true);
        }

        NativeImage image = new NativeImage(textureSize.width(), textureSize.height(), true);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            NativeImage mask = armorMasks.get(slot);

            if (mask == null) {
                continue;
            }

            ItemStack stack = CosmeticArmorReworkedHelper.getItemVisibleInSlot(player, slot);
            Identifier armorLocation = generateArmorTextureIdentifier(player, slot);
            NativeImage armorImage = RenderingUtils.getImageFromResource(armorLocation);

            if (armorImage == null) {
                continue;
            }

            try {
                if (armorImage.getWidth() != image.getWidth() || armorImage.getHeight() != image.getHeight()) {
                    DragonSurvival.LOGGER.error("Armor texture {} does not match the expected size {}x{}", armorLocation, image.getWidth(), image.getHeight());
                    continue;
                }

                Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
                ResourceKey<EquipmentAsset> equipmentAssetId = equippable == null ? null : equippable.assetId().orElse(null);

                if (equipmentAssetId != null) {
                    applyArmorLayer(image, mask, armorImage, stack, handler, equipmentAssetId);
                } else {
                    copyPixels(image, armorImage);
                }
            } finally {
                armorImage.close();
            }
        }

        if (ClawInventoryData.getData(player).shouldRenderClaws) {
            Identifier clawResource = ClawsAndTeeth.constructClawTexture(player);

            if (clawResource != null) {
                copyPixels(image, RenderingUtils.getImageFromResource(clawResource));
            }

            Identifier teethResource = ClawsAndTeeth.constructTeethTexture(player);

            if (teethResource != null) {
                copyPixels(image, RenderingUtils.getImageFromResource(teethResource));
            }
        }

        ArrayList<ItemStack> visibleCurios = CurioAPIHelper.getVisibleCurioItems(player);
        for (ItemStack itemStack : visibleCurios) {
            Identifier curioResource = toArmorResource(model, itemStack.getItem());

            if (curioResource != null && Minecraft.getInstance().getResourceManager().getResource(curioResource).isPresent()) {
                copyPixels(image, RenderingUtils.getImageFromResource(curioResource));
            }
        }

        return image;
    }

    private static void applyArmorLayer(
        final NativeImage destination,
        final NativeImage mask,
        final NativeImage armorImage,
        final ItemStack stack,
        final DragonStateHandler handler,
        final ResourceKey<EquipmentAsset> equipmentAssetId
    ) {
        float[] trimBaseHSB = new float[3];
        float[] dyeHSB = new float[3];
        float[] armorHSB = new float[3];
        float[] trimHSB = new float[3];
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        DyedItemColor dyedColor = stack.get(DataComponents.DYED_COLOR);
        NativeImage trimImage = null;
        boolean hasTrim = trim != null;

        if (dyedColor != null) {
            Color.RGBtoHSB(ARGB.red(dyedColor.rgb()), ARGB.green(dyedColor.rgb()), ARGB.blue(dyedColor.rgb()), dyeHSB);
        }

        if (hasTrim) {
            extractTrimBaseHSB(trim, equipmentAssetId, trimBaseHSB);
            String patternPath = trim.pattern().value().assetId().getPath();
            Identifier resource = Identifier.fromNamespaceAndPath(handler.getModel().getNamespace(), "textures/armor/" + handler.getModel().getPath() + "/armor_trims/" + patternPath + ".png");
            trimImage = RenderingUtils.getImageFromResource(resource);
        }

        try {
            for (int x = 0; x < armorImage.getWidth(); x++) {
                for (int y = 0; y < armorImage.getHeight(); y++) {
                    if (ARGB.alpha(mask.getPixel(x, y)) == 0) {
                        continue;
                    }

                    int armorPixel = armorImage.getPixel(x, y);

                    if (ARGB.alpha(armorPixel) == 0) {
                        continue;
                    }

                    if (hasTrim && trimImage != null) {
                        int trimPixel = trimImage.getPixel(x, y);

                        if (ARGB.alpha(trimPixel) != 0) {
                            Color.RGBtoHSB(ARGB.red(trimPixel), ARGB.green(trimPixel), ARGB.blue(trimPixel), trimHSB);

                            if (trimHSB[1] == 0.0F) {
                                int rgb = Color.HSBtoRGB(trimBaseHSB[0], trimBaseHSB[1], trimHSB[2]);
                                destination.setPixel(x, y, ARGB.color(255, ARGB.red(rgb), ARGB.green(rgb), ARGB.blue(rgb)));
                            }

                            continue;
                        }
                    }

                    Color.RGBtoHSB(ARGB.red(armorPixel), ARGB.green(armorPixel), ARGB.blue(armorPixel), armorHSB);

                    if ((dyeHSB[0] != 0.0F || dyeHSB[1] != 0.0F) && armorHSB[2] != 0.0F) {
                        int rgb = Color.HSBtoRGB(dyeHSB[0], dyeHSB[1], armorHSB[2]);
                        destination.setPixel(x, y, ARGB.color(ARGB.alpha(armorPixel), ARGB.red(rgb), ARGB.green(rgb), ARGB.blue(rgb)));
                    } else {
                        destination.setPixel(x, y, armorPixel);
                    }
                }
            }
        } finally {
            if (trimImage != null) {
                trimImage.close();
            }
        }
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

    private static void copyPixels(final NativeImage destination, final NativeImage source) {
        if (source == null) {
            return;
        }

        try {
            for (int x = 0; x < source.getWidth(); x++) {
                for (int y = 0; y < source.getHeight(); y++) {
                    int pixel = source.getPixel(x, y);

                    if (ARGB.alpha(pixel) != 0) {
                        destination.setPixel(x, y, pixel);
                    }
                }
            }
        } finally {
            source.close();
        }
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

    private static Identifier toArmorResource(final Identifier model, final Item item) {
        if (item == Items.AIR) {
            return null;
        }

        Identifier itemResource = item.builtInRegistryHolder().unwrapKey().orElseThrow().identifier();
        return Identifier.fromNamespaceAndPath(model.getNamespace(), "textures/armor/" + model.getPath() + "/" + itemResource.getNamespace() + "/" + itemResource.getPath() + ".png");
    }
}
