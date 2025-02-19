package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.DarkDragonArmorItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.LightDragonArmorItem;
import by.dragonsurvivalteam.dragonsurvival.compat.car.CosmeticArmorReworkedHelper;
import by.dragonsurvivalteam.dragonsurvival.compat.iris.InnerWrappedRenderType;
import by.dragonsurvivalteam.dragonsurvival.compat.iris.LayeringStates;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ClawInventoryData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DragonArmorRenderLayer extends GeoRenderLayer<DragonEntity> {
    private final GeoEntityRenderer<DragonEntity> renderer;
    private static final HashMap<ResourceLocation, CompletableFuture<Void>> armorTextures = new HashMap<>();
    private static final HashMap<ResourceLocation, HashMap<EquipmentSlot, NativeImage>> armorMasksPerModel = new HashMap<>();

    public DragonArmorRenderLayer(final GeoEntityRenderer<DragonEntity> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    private void initArmorMasks(final ResourceLocation model) {
        armorMasksPerModel.computeIfAbsent(model, key -> {
            HashMap<EquipmentSlot, NativeImage> masks = new HashMap<>();

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                    String texture = "textures/armor/" + key.getPath() + "/armor_trims/masks/" + slot.getName() + "_mask.png";
                    ResourceLocation resource = ResourceLocation.fromNamespaceAndPath(key.getNamespace(), texture);
                    Optional<Resource> armorFile = Minecraft.getInstance().getResourceManager().getResource(resource);

                    if (armorFile.isEmpty()) {
                        DragonSurvival.LOGGER.error("Armor mask {} missing for model {}", texture, model.getPath());
                        continue;
                    }

                    try {
                        InputStream textureStream = armorFile.get().open();
                        masks.put(slot, NativeImage.read(textureStream));
                        textureStream.close();
                    } catch (IOException exception) {
                        DragonSurvival.LOGGER.error("Failed to read file {}", texture, exception);
                    }
                }
            }

            return masks;
        });
    }

    @Override
    public void render(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final RenderType renderType, final MultiBufferSource bufferSource, final VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        Player player = animatable.getPlayer();

        if (player == null) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!armorMasksPerModel.containsKey(handler.getModel())) {
            initArmorMasks(handler.getModel());
        }

        if (!(player instanceof FakeClientPlayer)) {
            if (hasAnyArmorEquipped(player) || ClawInventoryData.getData(player).shouldRenderClaws) {
                Optional<ResourceLocation> armorTexture = constructTrimmedDragonArmorTexture(player);
                if (armorTexture.isPresent()) {
                    ((DragonRenderer) renderer).isRenderLayers = true;
                    renderArmor(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, armorTexture.get());
                    ((DragonRenderer) renderer).isRenderLayers = false;
                }
            }
        }
    }

    private void renderArmor(final PoseStack poseStack, final DragonEntity animatable, final BakedGeoModel bakedModel, final MultiBufferSource bufferSource, float partialTick, int packedLight, final ResourceLocation texture) {
        if (animatable == null) {
            return;
        }

        ClientDragonRenderer.dragonModel.setOverrideTexture(texture);
        RenderType type = renderer.getRenderType(animatable, texture, bufferSource, partialTick);

        if (type != null) {
            // Ensure that the armor is rendering on top of all the other layers
            InnerWrappedRenderType wrappedType = new InnerWrappedRenderType("dragon_armor", type, LayeringStates.VIEW_OFFSET_Z_LAYERING_FORWARD);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(wrappedType);
            renderer.actuallyRender(poseStack, animatable, bakedModel, wrappedType, bufferSource, vertexConsumer, true, partialTick, packedLight, OverlayTexture.NO_OVERLAY, renderer.getRenderColor(animatable, partialTick, packedLight).getColor());
        }
    }

    private static Optional<ResourceLocation> constructTrimmedDragonArmorTexture(final Player player) {
        String armorUUID = buildUniqueArmorUUID(player);
        // This is an internal (dynamically created) resource, so we can keep it in the DS namespace
        ResourceLocation imageResource = DragonSurvival.res("armor_" + armorUUID);

        if (armorTextures.containsKey(imageResource)) {
            CompletableFuture<Void> future = armorTextures.get(imageResource);

            if (future.isDone()) {
                return Optional.of(imageResource);
            }
        } else {
            CompletableFuture<Void> uploadStep = CompletableFuture.supplyAsync(() -> compileArmorTexture(player))
                    .thenAcceptAsync(image -> RenderingUtils.uploadTexture(image, imageResource), Minecraft.getInstance());

            armorTextures.put(imageResource, uploadStep);
        }

        return Optional.empty();
    }

    private static NativeImage compileArmorTexture(final Player player) {
        DragonStateHandler handler = DragonStateProvider.getData(player);
        ResourceLocation currentDragonModel = handler.getModel();

        if (!armorMasksPerModel.containsKey(currentDragonModel)) {
            return new NativeImage(512, 512, true);
        }

        HashMap<EquipmentSlot, NativeImage> armorMasks = armorMasksPerModel.get(currentDragonModel);
        if(armorMasks.isEmpty()) {
            return new NativeImage(512, 512, true);
        }

        NativeImage image = new NativeImage(armorMasks.values().stream().findFirst().get().getWidth(), armorMasks.values().stream().findFirst().get().getHeight(), true);
        for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
            if (!armorMasks.containsKey(equipmentSlot)) {
                continue;
            }

            ItemStack stack = CosmeticArmorReworkedHelper.getItemVisibleInSlot(player, equipmentSlot);
            ResourceLocation existingArmorLocation = generateArmorTextureResourceLocation(player, equipmentSlot);
            NativeImage armorImage = RenderingUtils.getImageFromResource(existingArmorLocation);

            // TODO: This will need to be significantly more flexible for 1.21.2 onwards (since anything can be considered armor)
            if (stack.getItem() instanceof ArmorItem) {
                if (armorImage == null) {
                    continue;
                }

                ArmorTrim trim = stack.get(DataComponents.TRIM);
                boolean hasTrim = false;
                float[] trimBaseHSB = new float[3];

                if (trim != null) {
                    Color trimBaseColor;
                    hasTrim = true;

                    ResourceLocation paletteResource = ResourceLocation.withDefaultNamespace("textures/trims/color_palettes/" + trim.material().value().assetName() + ".png");
                    NativeImage colorPalette = RenderingUtils.getImageFromResource(paletteResource);

                    if (colorPalette != null) {
                        int[] baseRed = new int[colorPalette.getWidth() * colorPalette.getHeight()], baseGreen = new int[colorPalette.getWidth() * colorPalette.getHeight()], baseBlue = new int[colorPalette.getWidth() * colorPalette.getHeight()];
                        int red = 0, green = 0, blue = 0;
                        int z = 0;

                        for (int x = 0; x < colorPalette.getWidth(); x++) {
                            for (int y = 0; y < colorPalette.getHeight(); y++) {
                                int rgba = colorPalette.getPixelRGBA(x, y);

                                if (rgba != 0) {
                                    Color c = new Color(rgba);
                                    baseRed[z] = c.getRed();
                                    baseGreen[z] = c.getGreen();
                                    baseBlue[z] = c.getBlue();
                                    z++;
                                }
                            }
                        }

                        for (int i = 0; i < z; i++) {
                            red += baseRed[i];
                            green += baseGreen[i];
                            blue += baseBlue[i];
                        }

                        trimBaseColor = new Color(blue / z, green / z, red / z, 255);
                        Color.RGBtoHSB(trimBaseColor.getBlue(), trimBaseColor.getGreen(), trimBaseColor.getRed(), trimBaseHSB);

                        colorPalette.close();
                    } else {
                        TextColor textColor = trim.material().value().description().getStyle().getColor();

                        if (textColor != null) {
                            // Not the most elegant solution,
                            // but the best way I could find to get a single color reliably...
                            trimBaseColor = new Color(textColor.getValue());
                            Color.RGBtoHSB(trimBaseColor.getBlue(), trimBaseColor.getGreen(), trimBaseColor.getRed(), trimBaseHSB);
                        }
                    }
                }

                float[] armorHSB = new float[3];
                float[] trimHSB = new float[3];
                float[] dyeHSB = new float[3];
                DyedItemColor dyeColor = stack.get(DataComponents.DYED_COLOR);

                if (dyeColor != null) {
                    Color armorDye = new Color(dyeColor.rgb());
                    Color.RGBtoHSB(armorDye.getBlue(), armorDye.getGreen(), armorDye.getRed(), dyeHSB);
                }

                NativeImage trimImage = null;

                if (hasTrim) {
                    String patternPath = trim.pattern().value().assetId().getPath();
                    String texture = "textures/armor/" + handler.getModel().getPath() + "/armor_trims/" + patternPath + ".png";
                    ResourceLocation resource = ResourceLocation.fromNamespaceAndPath(handler.getModel().getNamespace(), texture);
                    trimImage = RenderingUtils.getImageFromResource(resource);
                }

                for (int x = 0; x < armorImage.getWidth(); x++) {
                    for (int y = 0; y < armorImage.getHeight(); y++) {
                        if (armorMasks.get(equipmentSlot).getPixelRGBA(x, y) == 0) {
                            continue;
                        }

                        Color armorColor = new Color(armorImage.getPixelRGBA(x, y), true);

                        if (hasTrim && trimImage != null) {
                            Color trimColor = new Color(trimImage.getPixelRGBA(x, y), true);
                            Color.RGBtoHSB(trimColor.getRed(), trimColor.getGreen(), trimColor.getBlue(), trimHSB);

                            if (trimColor.getAlpha() != 0) {
                                // Changes the hue and saturation to be the same as the trim's base color while keeping the design's brightness
                                if (trimHSB[1] == 0) {
                                    // Replace any grayscale parts with the appropriate trim color
                                    image.setPixelRGBA(x, y, Color.HSBtoRGB(trimBaseHSB[0], trimBaseHSB[1], trimHSB[2]));
                                }/* else {
                                        // Otherwise, keep the same color (for parts that should not change color)
                                        image.setPixelRGBA(x, y, trimColor.getRGB());
                                    }*/
                            } else if (armorColor.getAlpha() != 0) {
                                // There is no trim on this pixel and we can ignore it safely
                                if (dyeHSB[0] != 0 && dyeHSB[1] != 0) {
                                    // Get the armor's brightness, and the dye's hue and saturation
                                    image.setPixelRGBA(x, y, Color.HSBtoRGB(dyeHSB[0], dyeHSB[1], Color.RGBtoHSB(armorColor.getRed(), armorColor.getGreen(), armorColor.getBlue(), null)[1]));
                                } else {
                                    image.setPixelRGBA(x, y, armorColor.getRGB());
                                }
                            }
                        } else if (armorColor.getAlpha() != 0) {
                            // No armor trim, just the armor
                            Color.RGBtoHSB(armorColor.getRed(), armorColor.getGreen(), armorColor.getBlue(), armorHSB);

                            if ((dyeHSB[0] != 0 || dyeHSB[1] != 0) && armorHSB[1] == 0) {
                                // Get the armor's brightness, and the dye's hue and saturation
                                image.setPixelRGBA(x, y, Color.HSBtoRGB(dyeHSB[0], dyeHSB[1], armorHSB[2]));
                            } else {
                                image.setPixelRGBA(x, y, armorColor.getRGB());
                            }
                        }
                    }
                }

                armorImage.close();

                if (trimImage != null) {
                    trimImage.close();
                }
            } else {
                copyPixels(image, armorImage);
            }
        }

        if (ClawInventoryData.getData(player).shouldRenderClaws) {
            // claws and teeth go over the armor, so they are added last
            ResourceLocation clawResource = ClawsAndTeeth.constructClawTexture(player);

            if (clawResource != null) {
                copyPixels(image, RenderingUtils.getImageFromResource(clawResource));
            }

            ResourceLocation teethResource = ClawsAndTeeth.constructTeethTexture(player);

            if (teethResource != null) {
                copyPixels(image, RenderingUtils.getImageFromResource(teethResource));
            }
        }

        return image;
    }

    private static void copyPixels(final NativeImage destination, final NativeImage source) {
        if (source != null) {
            for (int x = 0; x < source.getWidth(); x++) {
                for (int y = 0; y < source.getHeight(); y++) {
                    int pixel = source.getPixelRGBA(x, y);

                    if (pixel != 0) {
                        destination.setPixelRGBA(x, y, pixel);
                    }
                }
            }

            source.close();
        }
    }

    private static boolean hasAnyArmorEquipped(Player pPlayer) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }

            ItemStack itemstack = pPlayer.getItemBySlot(slot);
            if (!itemstack.is(Items.AIR)) {
                return true;
            }
        }

        return false;
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

        return UUID.nameUUIDFromBytes(armorTotal.toString().getBytes()).toString();
    }

    private static ResourceLocation generateArmorTextureResourceLocation(Player player, EquipmentSlot equipmentSlot) {
        ItemStack stack = CosmeticArmorReworkedHelper.getItemVisibleInSlot(player, equipmentSlot);
        Item item = stack.getItem();
        DragonStateHandler handler = DragonStateProvider.getData(player);
        ResourceLocation armorResource = toArmorResource(handler.getModel(), item);

        if (armorResource != null) {
            if (Minecraft.getInstance().getResourceManager().getResource(armorResource).isPresent()) {
                return armorResource;
            }
        }

        String texture = "textures/armor/" + handler.getModel().getPath() + "/";

        if (item instanceof ArmorItem armorItem) {
            Holder<ArmorMaterial> armorMaterial = armorItem.getMaterial();
            boolean isVanillaArmor = false;
            boolean isDyeable = false;
            if (armorMaterial == ArmorMaterials.NETHERITE) {
                isVanillaArmor = true;
                texture += "netherite_";
            } else if (armorMaterial == ArmorMaterials.DIAMOND) {
                isVanillaArmor = true;
                texture += "diamond_";
            } else if (armorMaterial == ArmorMaterials.IRON) {
                isVanillaArmor = true;
                texture += "iron_";
            } else if (armorMaterial == ArmorMaterials.LEATHER) {
                isVanillaArmor = true;
                isDyeable = true;
                texture += "leather_";
            } else if (armorMaterial == ArmorMaterials.GOLD) {
                isVanillaArmor = true;
                texture += "gold_";
            } else if (armorMaterial == ArmorMaterials.CHAIN) {
                isVanillaArmor = true;
                texture += "chainmail_";
            } else if (armorMaterial == ArmorMaterials.TURTLE) {
                isVanillaArmor = true;
                texture += "turtle_";
            }

            if (isVanillaArmor || item instanceof DarkDragonArmorItem || item instanceof LightDragonArmorItem) {
                if (isVanillaArmor) {
                    texture += "dragon_";
                } else if (item instanceof DarkDragonArmorItem) {
                    texture += "dark_dragon_";
                } else if (item instanceof LightDragonArmorItem) {
                    texture += "light_dragon_";
                }

                switch (equipmentSlot) {
                    case HEAD -> texture += "helmet";
                    case CHEST -> texture += "chestplate";
                    case LEGS -> texture += "leggings";
                    case FEET -> texture += "boots";
                }

                // TODO :: Do we really have to make a special case for this? Do items not have a way to signify "can be dyed?"
                if (isDyeable && player.getItemBySlot(equipmentSlot).get(DataComponents.DYED_COLOR) == null) {
                    texture += "_undyed";
                }

                texture += ".png";
                ResourceLocation resource = ResourceLocation.fromNamespaceAndPath(handler.getModel().getNamespace(), texture);
                if(Minecraft.getInstance().getResourceManager().getResource(resource).isPresent()) {
                    return resource;
                } else {
                    DragonSurvival.LOGGER.warn("Missing vanilla armor texture for {} in model {}, falling back to generic armor.", texture, handler.getModel().getPath());
                }
            }

            int defense = armorItem.getDefense();
            texture = "textures/armor/" + handler.getModel().getPath() + "/";
            switch (equipmentSlot) {
                case FEET -> texture += Mth.clamp(defense, 1, 4) + "_dragon_boots";
                case CHEST -> texture += Mth.clamp(defense / 2, 1, 4) + "_dragon_chestplate";
                case HEAD -> texture += Mth.clamp(defense, 1, 4) + "_dragon_helmet";
                case LEGS -> texture += Mth.clamp((int) (defense / 1.5), 1, 4) + "_dragon_leggings";
            }

            texture += ".png";
            return ResourceLocation.fromNamespaceAndPath(handler.getModel().getNamespace(), texture);
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
}