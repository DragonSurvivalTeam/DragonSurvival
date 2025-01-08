package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.iris_compat.InnerWrappedRenderType;
import by.dragonsurvivalteam.dragonsurvival.client.render.util.iris_compat.LayeringStates;
import by.dragonsurvivalteam.dragonsurvival.client.util.RenderingUtils;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.DarkDragonArmorItem;
import by.dragonsurvivalteam.dragonsurvival.common.items.armor.LightDragonArmorItem;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
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
import net.minecraft.world.item.*;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.item.component.DyedItemColor;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.LOGGER;
import static by.dragonsurvivalteam.dragonsurvival.DragonSurvival.res;

public class DragonArmorRenderLayer extends GeoRenderLayer<DragonEntity> {
    private final GeoEntityRenderer<DragonEntity> renderer;
    private static final HashMap<ResourceLocation, CompletableFuture<Void>> armorTextures = new HashMap<>();
    private static final HashMap<ResourceLocation, HashMap<EquipmentSlot, NativeImage>> armorMasksPerModel = new HashMap<>();

    public DragonArmorRenderLayer(final GeoEntityRenderer<DragonEntity> renderer) {
        super(renderer);
        this.renderer = renderer;
    }

    private void initArmorMasks(final String modelName, final ResourceLocation modelResource) {
        armorMasksPerModel.computeIfAbsent(modelResource, resourceLocation -> {
            HashMap<EquipmentSlot, NativeImage> masks = new HashMap<>();

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.isArmor()) {
                    String texture = "textures/armor/" + modelName + "/armor_trims/masks/" + slot.getName() + "_mask.png";
                    Optional<Resource> armorFile = Minecraft.getInstance().getResourceManager().getResource(res(texture));

                    if (armorFile.isEmpty()) {
                        continue;
                    }

                    try {
                        InputStream textureStream = armorFile.get().open();
                        masks.put(slot, NativeImage.read(textureStream));
                        textureStream.close();
                    } catch (IOException e) {
                        LOGGER.error("Failed to read file {}", texture);
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

        if (player.isSpectator()) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);
        if(!armorMasksPerModel.containsKey(handler.getCurrentCustomModel())) {
            initArmorMasks(handler.getCustomModelName(), handler.getCurrentCustomModel());
        }

        if (hasAnyArmorEquipped(player)) {
            Optional<ResourceLocation> armorTexture = constructTrimmedDragonArmorTexture(player);
            if (armorTexture.isPresent()) {
                ((DragonRenderer) renderer).isRenderLayers = true;
                renderArmor(poseStack, animatable, bakedModel, bufferSource, partialTick, packedLight, armorTexture.get());
                ((DragonRenderer) renderer).isRenderLayers = false;
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

    private static Optional<ResourceLocation> constructTrimmedDragonArmorTexture(final Player pPlayer) {
        String armorUUID = buildUniqueArmorUUID(pPlayer);
        ResourceLocation imageLoc = res("armor_" + armorUUID);
        if (armorTextures.containsKey(imageLoc)) {
            CompletableFuture<Void> future = armorTextures.get(imageLoc);
            if (future.isDone()) {
                try {
                    return Optional.of(imageLoc);
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        } else {
            CompletableFuture<NativeImage> imageCompilationStep = CompletableFuture.supplyAsync(() -> {
                try {
                    return compileArmorTexture(pPlayer);
                } catch (IOException e) {
                    DragonSurvival.LOGGER.error("An error occurred while compiling the dragon armor texture", e);
                }

                return new NativeImage(0, 0, true);
            });
            CompletableFuture<Void> uploadStep = imageCompilationStep.thenRunAsync(() -> {
                try {
                    RenderingUtils.uploadTexture(imageCompilationStep.get(), imageLoc);
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }, Minecraft.getInstance());
            armorTextures.put(imageLoc, uploadStep);
        }

        return Optional.empty();
    }

    private static NativeImage compileArmorTexture(final Player player) throws IOException {
        NativeImage image = new NativeImage(512, 512, true);
        DragonStateHandler handler = DragonStateProvider.getData(player);
        ResourceLocation currentDragonModel = handler.getCurrentCustomModel();
        if(!armorMasksPerModel.containsKey(currentDragonModel)) {
            return image;
        }

        HashMap<EquipmentSlot, NativeImage> armorMasks = armorMasksPerModel.get(currentDragonModel);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!armorMasks.containsKey(slot)) continue;
            ItemStack itemstack = player.getItemBySlot(slot);
            ResourceLocation existingArmorLocation = generateArmorTextureResourceLocation(player, slot);
            NativeImage armorImage = RenderingUtils.getImageFromResource(existingArmorLocation);
            // TODO: This will need to be significantly more flexible for 1.21.2 onwards (since anything can be considered armor)
            if (itemstack.getItem() instanceof ArmorItem) {
                if (armorImage == null) {
                    continue;
                }

                ArmorTrim trim = itemstack.get(DataComponents.TRIM);
                boolean hasTrim = false;
                float[] trimBaseHSB = new float[3];
                if (trim != null) {
                    Color trimBaseColor;
                    hasTrim = true;
                    String materialAssetName = trim.material().value().assetName();
                    NativeImage colorPalette = RenderingUtils.getImageFromResource(ResourceLocation.withDefaultNamespace("textures/trims/color_palettes/" + materialAssetName + ".png"));
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
                    } else {
                        TextColor tc = trim.material().value().description().getStyle().getColor();
                        if (tc != null) {
                            // Not the most elegant solution,
                            // but the best way I could find to get a single color reliably...
                            trimBaseColor = new Color(tc.getValue());
                            Color.RGBtoHSB(trimBaseColor.getBlue(), trimBaseColor.getGreen(), trimBaseColor.getRed(), trimBaseHSB);
                        }
                    }
                }
                float[] armorHSB = new float[3];
                float[] trimHSB = new float[3];
                float[] dyeHSB = new float[3];
                DyedItemColor dyeColor = itemstack.get(DataComponents.DYED_COLOR);
                if (dyeColor != null) {
                    Color armorDye = new Color(dyeColor.rgb());
                    Color.RGBtoHSB(armorDye.getBlue(), armorDye.getGreen(), armorDye.getRed(), dyeHSB);
                }

                NativeImage trimImage = null;
                if (hasTrim) {
                    String patternPath = trim.pattern().value().assetId().getPath();
                    trimImage = RenderingUtils.getImageFromResource(res("textures/armor/" + handler.getCustomModelName() + "/armor_trims/" + patternPath + ".png"));
                }

                for (int x = 0; x < armorImage.getWidth(); x++) {
                    for (int y = 0; y < armorImage.getHeight(); y++) {
                        if (armorMasks.get(slot).getPixelRGBA(x, y) == 0) continue;
                        Color armorColor = new Color(armorImage.getPixelRGBA(x, y), true);

                        if (hasTrim) {
                            if (trimImage == null) {
                                continue;
                            }

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
            } else {
                // If it isn't an armor item, just copy the texture over
                if(armorImage != null) {
                    for (int x = 0; x < armorImage.getWidth(); x++) {
                        for (int y = 0; y < armorImage.getHeight(); y++) {
                            if(armorImage.getPixelRGBA(x, y) != 0) {
                                image.setPixelRGBA(x, y, armorImage.getPixelRGBA(x, y));
                            }
                        }
                    }
                }
            }
        }
        return image;
    }

    private static boolean hasAnyArmorEquipped(Player pPlayer) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor())
                continue;

            ItemStack itemstack = pPlayer.getItemBySlot(slot);
            if (!itemstack.is(Items.AIR)) {
                return true;
            }
        }

        return false;
    }

    private static String buildUniqueArmorUUID(Player player) {
        StringBuilder armorTotal = new StringBuilder();
        DragonStateHandler handler = DragonStateProvider.getData(player);
        ResourceLocation currentDragonModel = handler.getCurrentCustomModel();
        armorTotal.append(currentDragonModel.toString());
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor())
                continue;
            ItemStack itemstack = player.getItemBySlot(slot);
            armorTotal.append(itemstack);

            ArmorTrim armorTrim = itemstack.getComponents().get(DataComponents.TRIM);
            if (armorTrim != null) {
                armorTotal.append("_").append(armorTrim.material().value().assetName()).append("_").append(armorTrim.pattern().value().assetId());
            }

            DyedItemColor dyeColor = itemstack.get(DataComponents.DYED_COLOR);
            if (dyeColor != null) {
                armorTotal.append(dyeColor);
            }
        }
        return UUID.nameUUIDFromBytes(armorTotal.toString().getBytes()).toString();
    }

    private static ResourceLocation generateArmorTextureResourceLocation(Player player, EquipmentSlot equipmentSlot) {
        Item item = player.getItemBySlot(equipmentSlot).getItem();
        DragonStateHandler handler = DragonStateProvider.getData(player);
        ResourceLocation resourceLocation = itemToArmorResLoc(handler.getCustomModelName(), item);
        if (resourceLocation != null) {
            if (Minecraft.getInstance().getResourceManager().getResource(resourceLocation).isPresent()) {
                return resourceLocation;
            }
        }

        String texture = "textures/armor/" + handler.getCustomModelName() + "/";
        if (item instanceof ArmorItem armorItem) {
            Holder<ArmorMaterial> armorMaterial = armorItem.getMaterial();
            boolean isVanillaArmor = false;
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
                texture += ".png";
                return res(stripInvalidPathChars(texture));
            }

            int defense = armorItem.getDefense();
            switch (equipmentSlot) {
                case FEET -> texture += Mth.clamp(defense, 1, 4) + "_dragon_boots";
                case CHEST -> texture += Mth.clamp(defense / 2, 1, 4) + "_dragon_chestplate";
                case HEAD -> texture += Mth.clamp(defense, 1, 4) + "_dragon_helmet";
                case LEGS -> texture += Mth.clamp((int) (defense / 1.5), 1, 4) + "_dragon_leggings";
            }
            texture += ".png";
            return res(stripInvalidPathChars(texture));
        }
        return res("textures/armor/empty_armor.png");
    }

    private static ResourceLocation itemToArmorResLoc(final String customModelName, final Item item) {
        if (item == Items.AIR) {
            return null;
        }

        //noinspection deprecation,DataFlowIssue -> ignore deprecated / key is present
        ResourceLocation location = item.builtInRegistryHolder().getKey().location();
        return ResourceLocation.parse(DragonSurvival.MODID + ":" + "textures/armor/" + customModelName + "/" + location.getNamespace() + "/" + location.getPath() + ".png");
    }

    private static String stripInvalidPathChars(String loc) {
        // filters certain characters (non [a-z0-9/._-]) to prevent crashes
        // this probably should never be relevant, but you can never be too safe
        loc = loc.chars()
                .filter(ch -> ResourceLocation.validPathChar((char) ch))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
        return loc;
    }
}