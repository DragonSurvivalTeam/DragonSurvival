package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonAltarBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonBeacon;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonDoor;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonPressurePlates;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonRiderWorkbenchBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.DragonSoulBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.HelmetBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.PrimordialAnchorBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SkeletonPieceBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SmallDragonDoor;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.SourceOfMagicBlock;
import by.dragonsurvivalteam.dragonsurvival.common.blocks.TreasureBlock;
import by.dragonsurvivalteam.dragonsurvival.registry.DSBlocks;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.entity.vault.VaultState;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Optional;

public class DataBlockStateProvider extends ModelProvider {
    private static final String BLOCK_FOLDER = "block";
    private static final TextureSlot PRIMORDIAL_ANCHOR_SLOT = TextureSlot.create("2");
    private static final TextureSlot SKELETON_TEXTURE_SLOT = TextureSlot.create("skeleton_texture");

    private static final ModelTemplate SKULL_TEMPLATE = new ModelTemplate(
            Optional.of(Identifier.withDefaultNamespace("block/skull")),
            Optional.empty(),
            TextureSlot.ALL
    );
    private static final TextureSlot SMALL_DOOR_BOTTOM = TextureSlot.create("bottom");
    private static final ModelTemplate SMALL_DOOR_TEMPLATE = new ModelTemplate(
            Optional.of(DragonSurvival.res(BLOCK_FOLDER + "/small_dragon_door")),
            Optional.empty(),
            SMALL_DOOR_BOTTOM,
            TextureSlot.PARTICLE
    );
    private static final ModelTemplate SMALL_DOOR_HINGE_TEMPLATE = new ModelTemplate(
            Optional.of(DragonSurvival.res(BLOCK_FOLDER + "/small_dragon_door_rh")),
            Optional.empty(),
            SMALL_DOOR_BOTTOM,
            TextureSlot.PARTICLE
    );
    private static final ModelTemplate DOOR_BOTTOM_LEFT_TEMPLATE = ModelTemplates.DOOR_BOTTOM_LEFT;
    private static final ModelTemplate DOOR_BOTTOM_LEFT_OPEN_TEMPLATE = ModelTemplates.DOOR_BOTTOM_LEFT_OPEN;
    private static final ModelTemplate DOOR_BOTTOM_RIGHT_TEMPLATE = ModelTemplates.DOOR_BOTTOM_RIGHT;
    private static final ModelTemplate DOOR_BOTTOM_RIGHT_OPEN_TEMPLATE = ModelTemplates.DOOR_BOTTOM_RIGHT_OPEN;
    private static final ModelTemplate DOOR_TOP_LEFT_TEMPLATE = ModelTemplates.DOOR_TOP_LEFT;
    private static final ModelTemplate DOOR_TOP_LEFT_OPEN_TEMPLATE = ModelTemplates.DOOR_TOP_LEFT_OPEN;
    private static final ModelTemplate DOOR_TOP_RIGHT_TEMPLATE = ModelTemplates.DOOR_TOP_RIGHT;
    private static final ModelTemplate DOOR_TOP_RIGHT_OPEN_TEMPLATE = ModelTemplates.DOOR_TOP_RIGHT_OPEN;
    private static final ModelTemplate VAULT_TEMPLATE = ModelTemplates.VAULT;

    public DataBlockStateProvider(final PackOutput output, final String modId) {
        super(output, modId);
    }

    @Override
    protected void registerModels(@NotNull final BlockModelGenerators blockModels, @NotNull final net.minecraft.client.data.models.ItemModelGenerators itemModels) {
        DSBlocks.REGISTRY.getEntries().forEach(holder -> {
            Block block = holder.get();
            String name = holder.getId().getPath();

            if (block instanceof DragonDoor dragonDoor) {
                createDragonDoor(blockModels, dragonDoor, name);
                blockModels.registerSimpleItemModel(dragonDoor, blockModel(name + "_middle"));
            } else if (block instanceof SmallDragonDoor smallDragonDoor) {
                createSmallDragonDoor(blockModels, smallDragonDoor, name);
                blockModels.registerSimpleItemModel(smallDragonDoor, blockModel(name));
            } else if (block instanceof HelmetBlock helmetBlock) {
                createHelmet(blockModels, helmetBlock, name);
                blockModels.registerSimpleFlatItemModel(helmetBlock);
            } else if (block instanceof DragonPressurePlates pressurePlate) {
                createPressurePlate(blockModels, pressurePlate, name);
            } else if (block instanceof DragonAltarBlock altarBlock) {
                createDragonAltar(blockModels, altarBlock, name);
            } else if (block instanceof TreasureBlock treasureBlock) {
                createTreasure(blockModels, treasureBlock, name);
            } else if (block instanceof VaultBlock vaultBlock) {
                createVault(blockModels, vaultBlock, name);
            } else if (block instanceof DragonRiderWorkbenchBlock workbenchBlock) {
                createWorkbench(blockModels, workbenchBlock, name);
            } else if (block instanceof SourceOfMagicBlock sourceOfMagicBlock) {
                createSourceOfMagic(blockModels, sourceOfMagicBlock, name);
                blockModels.registerSimpleItemModel(sourceOfMagicBlock, blockModel(name));
            } else if (block instanceof RotatedPillarBlock rotatedPillarBlock) {
                createDragonMemory(blockModels, rotatedPillarBlock, name);
            } else if (block instanceof DragonBeacon dragonBeacon) {
                createDragonBeacon(blockModels, dragonBeacon);
                blockModels.registerSimpleItemModel(dragonBeacon, blockModel("empty"));
            } else if (block instanceof SkeletonPieceBlock skeletonPieceBlock) {
                createSkeletonPiece(blockModels, skeletonPieceBlock, name);
            } else if (block instanceof PrimordialAnchorBlock primordialAnchorBlock) {
                createPrimordialAnchor(blockModels, primordialAnchorBlock, name);
            } else if (block instanceof DragonSoulBlock dragonSoulBlock) {
                createDragonSoul(blockModels, dragonSoulBlock);
            }
        });

        DataItemModelProvider.registerItemModels(itemModels);
    }

    private void createDragonDoor(final BlockModelGenerators blockModels, final DragonDoor block, final String name) {
        Material bottomTexture = blockMaterial(name + "_bottom");
        Material middleTexture = blockMaterial(name + "_center");
        Material topTexture = blockMaterial(name + "_top");

        Identifier bottomLeft = createDoorModel(blockModels, name + "_bottom", bottomTexture, DOOR_BOTTOM_LEFT_TEMPLATE);
        Identifier bottomLeftOpen = createDoorModel(blockModels, name + "_bottom_open", bottomTexture, DOOR_BOTTOM_LEFT_OPEN_TEMPLATE);
        Identifier bottomRight = createDoorModel(blockModels, name + "_bottom_hinge", bottomTexture, DOOR_BOTTOM_RIGHT_TEMPLATE);
        Identifier bottomRightOpen = createDoorModel(blockModels, name + "_bottom_hinge_open", bottomTexture, DOOR_BOTTOM_RIGHT_OPEN_TEMPLATE);
        Identifier middleLeft = createDoorModel(blockModels, name + "_middle", middleTexture, DOOR_BOTTOM_LEFT_TEMPLATE);
        Identifier middleLeftOpen = createDoorModel(blockModels, name + "_middle_open", middleTexture, DOOR_BOTTOM_LEFT_OPEN_TEMPLATE);
        Identifier middleRight = createDoorModel(blockModels, name + "_middle_hinge", middleTexture, DOOR_BOTTOM_RIGHT_TEMPLATE);
        Identifier middleRightOpen = createDoorModel(blockModels, name + "_middle_hinge_open", middleTexture, DOOR_BOTTOM_RIGHT_OPEN_TEMPLATE);
        Identifier topLeft = createDoorModel(blockModels, name + "_top", topTexture, DOOR_TOP_LEFT_TEMPLATE);
        Identifier topLeftOpen = createDoorModel(blockModels, name + "_top_open", topTexture, DOOR_TOP_LEFT_OPEN_TEMPLATE);
        Identifier topRight = createDoorModel(blockModels, name + "_top_hinge", topTexture, DOOR_TOP_RIGHT_TEMPLATE);
        Identifier topRightOpen = createDoorModel(blockModels, name + "_top_hinge_open", topTexture, DOOR_TOP_RIGHT_OPEN_TEMPLATE);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(
                        PropertyDispatch.initial(DragonDoor.FACING, DragonDoor.OPEN, DragonDoor.HINGE, DragonDoor.PART)
                                .generate((facing, open, hinge, part) -> {
                                    Identifier model = switch (part) {
                                        case TOP -> hinge == DoorHingeSide.RIGHT
                                                ? open ? topRightOpen : topRight
                                                : open ? topLeftOpen : topLeft;
                                        case MIDDLE -> hinge == DoorHingeSide.RIGHT
                                                ? open ? middleRightOpen : middleRight
                                                : open ? middleLeftOpen : middleLeft;
                                        case BOTTOM -> hinge == DoorHingeSide.RIGHT
                                                ? open ? bottomRightOpen : bottomRight
                                                : open ? bottomLeftOpen : bottomLeft;
                                    };
                                    int rotation = dragonDoorRotation((int) facing.toYRot(), open, hinge == DoorHingeSide.RIGHT);
                                    return rotatedVariant(model, rotation);
                                })
                )
        );
    }

    private void createSmallDragonDoor(final BlockModelGenerators blockModels, final SmallDragonDoor block, final String name) {
        Material bottomTexture = blockMaterial(name);
        TextureMapping mapping = new TextureMapping()
                .put(SMALL_DOOR_BOTTOM, bottomTexture)
                .put(TextureSlot.PARTICLE, bottomTexture);
        Identifier leftModel = SMALL_DOOR_TEMPLATE.create(blockModel(name), mapping, blockModels.modelOutput);
        Identifier rightModel = SMALL_DOOR_HINGE_TEMPLATE.create(blockModel(name + "_hinge"), mapping, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(
                        PropertyDispatch.initial(SmallDragonDoor.FACING, SmallDragonDoor.OPEN, SmallDragonDoor.HINGE)
                                .generate((facing, open, hinge) -> {
                                    boolean hingeRight = hinge == DoorHingeSide.RIGHT;
                                    Identifier model = hingeRight == open ? leftModel : rightModel;
                                    int rotation = dragonDoorRotation((int) facing.toYRot(), open, hingeRight);
                                    return rotatedVariant(model, rotation);
                                })
                )
        );
    }

    private void createHelmet(final BlockModelGenerators blockModels, final HelmetBlock block, final String name) {
        Identifier model = SKULL_TEMPLATE.create(blockModel(name), TextureMapping.singleSlot(TextureSlot.ALL, blockMaterial(name)), blockModels.modelOutput);
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
    }

    private void createPressurePlate(final BlockModelGenerators blockModels, final DragonPressurePlates block, final String name) {
        TextureMapping mapping = TextureMapping.singleSlot(TextureSlot.TEXTURE, blockMaterial(name));
        Identifier upModel = ModelTemplates.PRESSURE_PLATE_UP.create(blockModel(name), mapping, blockModels.modelOutput);
        Identifier downModel = ModelTemplates.PRESSURE_PLATE_DOWN.create(blockModel(name + "_down"), mapping, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(
                        PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, DragonPressurePlates.POWERED)
                                .generate((facing, powered) -> rotatedVariant(powered ? downModel : upModel, normalizeRotation((int) facing.toYRot() + 180)))
                )
        );
    }

    private void createDragonAltar(final BlockModelGenerators blockModels, final DragonAltarBlock block, final String name) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.DOWN, blockMaterial(name + "_top"))
                .put(TextureSlot.UP, blockMaterial(name + "_top"))
                .put(TextureSlot.NORTH, blockMaterial(name + "_north"))
                .put(TextureSlot.SOUTH, blockMaterial(name + "_south"))
                .put(TextureSlot.EAST, blockMaterial(name + "_east"))
                .put(TextureSlot.WEST, blockMaterial(name + "_west"))
                .put(TextureSlot.PARTICLE, blockMaterial(name + "_top"));
        Identifier model = ModelTemplates.CUBE.create(blockModel(name), mapping, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(
                        PropertyDispatch.initial(DragonAltarBlock.FACING)
                                .generate((facing) -> rotatedVariant(model, (int) facing.toYRot()))
                )
        );
    }

    private void createTreasure(final BlockModelGenerators blockModels, final TreasureBlock block, final String name) {
        Identifier fullModel = ModelTemplates.CUBE_ALL.create(blockModel(name), TextureMapping.cube(blockMaterial(name)), blockModels.modelOutput);

        for (int height = 1; height < 8; height++) {
            int pixels = height * 2;
            new ModelTemplate(Optional.of(Identifier.withDefaultNamespace("block/snow_height" + pixels)), Optional.empty(), TextureSlot.PARTICLE, TextureSlot.TEXTURE)
                    .create(blockModel(name + pixels), new TextureMapping()
                            .put(TextureSlot.PARTICLE, blockMaterial(name))
                            .put(TextureSlot.TEXTURE, blockMaterial(name)), blockModels.modelOutput);
        }

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(
                        PropertyDispatch.initial(TreasureBlock.LAYERS)
                                .generate((layers) -> BlockModelGenerators.plainVariant(layers == 8 ? fullModel : blockModel(name + layers * 2)))
                )
        );
        blockModels.registerSimpleItemModel(block, blockModel(name + "2"));
    }

    private void createVault(final BlockModelGenerators blockModels, final VaultBlock block, final String name) {
        Identifier active = createVaultModel(blockModels, name, VaultState.ACTIVE);
        Identifier inactive = createVaultModel(blockModels, name, VaultState.INACTIVE);
        Identifier unlocking = createVaultModel(blockModels, name, VaultState.UNLOCKING);
        Identifier ejecting = createVaultModel(blockModels, name, VaultState.EJECTING);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(
                        PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, VaultBlock.STATE)
                                .generate((facing, state) -> {
                                    Identifier model = switch (state) {
                                        case ACTIVE -> active;
                                        case INACTIVE -> inactive;
                                        case UNLOCKING -> unlocking;
                                        case EJECTING -> ejecting;
                                    };
                                    return rotatedVariant(model, (int) facing.toYRot() - 180);
                                })
                )
        );
        blockModels.registerSimpleItemModel(block, inactive);
    }

    private void createWorkbench(final BlockModelGenerators blockModels, final DragonRiderWorkbenchBlock block, final String name) {
        TextureMapping mapping = new TextureMapping()
                .put(TextureSlot.DOWN, blockMaterial(name + "_down"))
                .put(TextureSlot.UP, blockMaterial(name + "_up"))
                .put(TextureSlot.NORTH, blockMaterial(name + "_north"))
                .put(TextureSlot.SOUTH, blockMaterial(name + "_south"))
                .put(TextureSlot.EAST, blockMaterial(name + "_east"))
                .put(TextureSlot.WEST, blockMaterial(name + "_west"))
                .put(TextureSlot.PARTICLE, blockMaterial(name + "_down"));
        Identifier model = ModelTemplates.CUBE.create(blockModel(name), mapping, blockModels.modelOutput);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(
                        PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING)
                                .generate((facing) -> rotatedVariant(model, (int) facing.toYRot()))
                )
        );
    }

    private void createSourceOfMagic(final BlockModelGenerators blockModels, final SourceOfMagicBlock block, final String name) {
        Identifier fullModel = blockModel(name);
        Identifier emptyModel = blockModel(name + "_empty");

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(
                        PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING, SourceOfMagicBlock.FILLED)
                                .generate((facing, filled) -> rotatedVariant(filled ? fullModel : emptyModel, normalizeRotation((int) facing.toYRot() + 180)))
                )
        );
    }

    private void createDragonMemory(final BlockModelGenerators blockModels, final RotatedPillarBlock block, final String name) {
        Identifier model = ModelTemplates.CUBE_COLUMN.create(
                blockModel(name),
                new TextureMapping()
                        .put(TextureSlot.SIDE, blockMaterial("dragons_memory_side"))
                        .put(TextureSlot.END, blockMaterial("dragons_memory_top")),
                blockModels.modelOutput
        );
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(model)));
    }

    private void createDragonBeacon(final BlockModelGenerators blockModels, final DragonBeacon block) {
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(blockModel("empty"))));
    }

    private void createSkeletonPiece(final BlockModelGenerators blockModels, final SkeletonPieceBlock block, final String name) {
        String[] split = name.split("_skin");
        String skin = split[1].substring(split[1].length() - 1);

        Identifier model = new ModelTemplate(Optional.of(blockModel(split[0])), Optional.empty(), SKELETON_TEXTURE_SLOT, TextureSlot.PARTICLE)
                .create(blockModel(name), new TextureMapping()
                        .put(SKELETON_TEXTURE_SLOT, blockMaterial("skeleton_dragon_" + skin))
                        .put(TextureSlot.PARTICLE, blockMaterial("placeholder_" + skin)), blockModels.modelOutput);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(
                        PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING)
                                .generate((facing) -> rotatedVariant(model, (int) facing.toYRot()))
                )
        );
    }

    private void createPrimordialAnchor(final BlockModelGenerators blockModels, final PrimordialAnchorBlock block, final String name) {
        Identifier empty = createPrimordialAnchorModel(blockModels, name, "_empty");
        Identifier charged = createPrimordialAnchorModel(blockModels, name, "_charge");
        Identifier bloody = createPrimordialAnchorModel(blockModels, name, "_bloody");

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(
                        PropertyDispatch.initial(PrimordialAnchorBlock.BLOODY, PrimordialAnchorBlock.CHARGED)
                                .generate((isBloody, isCharged) -> BlockModelGenerators.plainVariant(isBloody ? bloody : isCharged ? charged : empty))
                )
        );
    }

    private void createDragonSoul(final BlockModelGenerators blockModels, final DragonSoulBlock block) {
        Identifier model = blockModel("dragon_soul");
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(block).with(
                        PropertyDispatch.initial(BlockStateProperties.HORIZONTAL_FACING)
                                .generate((facing) -> rotatedVariant(model, (int) facing.toYRot()))
                )
        );
    }

    private Identifier createDoorModel(final BlockModelGenerators blockModels, final String name, final Material texture, final ModelTemplate template) {
        return template.create(blockModel(name), TextureMapping.door(texture, texture), blockModels.modelOutput);
    }

    private Identifier createVaultModel(final BlockModelGenerators blockModels, final String name, final VaultState state) {
        String suffix = state == VaultState.EJECTING ? "ejecting_reward" : state.name().toLowerCase(Locale.ENGLISH);
        String frontState = switch (state) {
            case ACTIVE -> "_on";
            case INACTIVE -> "_off";
            case EJECTING, UNLOCKING -> "_ejecting";
        };
        String sideState = state == VaultState.ACTIVE || state == VaultState.EJECTING ? "_on" : "_off";
        String topState = state == VaultState.EJECTING ? "_ejecting" : "";

        return VAULT_TEMPLATE.create(
                blockModel(name + "_" + suffix),
                new TextureMapping()
                        .put(TextureSlot.BOTTOM, blockMaterial(name + "_bottom"))
                        .put(TextureSlot.FRONT, blockMaterial(name + "_front" + frontState))
                        .put(TextureSlot.SIDE, blockMaterial(name + "_side" + sideState))
                        .put(TextureSlot.TOP, blockMaterial(name + "_top" + topState)),
                blockModels.modelOutput
        );
    }

    private Identifier createPrimordialAnchorModel(final BlockModelGenerators blockModels, final String name, final String suffix) {
        return new ModelTemplate(Optional.of(blockModel("primordial_anchor")), Optional.empty(), PRIMORDIAL_ANCHOR_SLOT)
                .create(blockModel(name + suffix), TextureMapping.singleSlot(PRIMORDIAL_ANCHOR_SLOT, blockMaterial("primordial_anchor" + suffix)), blockModels.modelOutput);
    }

    private MultiVariant rotatedVariant(final Identifier model, final int rotation) {
        return BlockModelGenerators.variant(BlockModelGenerators.plainModel(model).withYRot(Quadrant.parseJson(normalizeRotation(rotation))));
    }

    private int dragonDoorRotation(final int baseRotation, final boolean open, final boolean hingeRight) {
        int rotation = baseRotation + 90;
        if (open) {
            rotation += hingeRight ? -90 : 90;
        }
        return normalizeRotation(rotation);
    }

    private int normalizeRotation(final int rotation) {
        int normalized = rotation % 360;
        return normalized < 0 ? normalized + 360 : normalized;
    }

    private Identifier blockTexture(final String path) {
        return DragonSurvival.res(BLOCK_FOLDER + "/" + path);
    }

    private Material blockMaterial(final String path) {
        return new Material(blockTexture(path));
    }

    private Identifier blockModel(final String path) {
        return DragonSurvival.res(BLOCK_FOLDER + "/" + path);
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Survival Block states";
    }
}
