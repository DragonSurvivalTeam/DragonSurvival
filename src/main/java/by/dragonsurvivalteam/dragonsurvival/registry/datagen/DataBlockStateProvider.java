package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;
import org.jetbrains.annotations.NotNull;


public class DataBlockStateProvider extends ModelProvider {

    public DataBlockStateProvider(PackOutput output, String modId) {
        super(output, modId);
    }

    @Override
    protected void registerModels(@NotNull BlockModelGenerators blockModels, @NotNull ItemModelGenerators itemModels) {
//        DSBlocks.REGISTRY.getEntries().forEach((holder) -> {
//            if (holder.get() instanceof DragonDoor) {
//                getVariantBuilder(holder.get())
//                        .forAllStates(state -> {
//                            boolean partTop = state.getValue(DragonDoor.PART) == DragonDoor.Part.TOP;
//                            boolean partMiddle = state.getValue(DragonDoor.PART) == DragonDoor.Part.MIDDLE;
//                            boolean hingeRight = state.getValue(DragonDoor.HINGE) == DoorHingeSide.RIGHT;
//                            boolean open = state.getValue(DragonDoor.OPEN);
//                            String name = holder.getId().getPath();
//                            String suffix = (partTop ? "_top" : partMiddle ? "_middle" : "_bottom") + (hingeRight ? "_hinge" : "") + (open ? "_open" : "");
//                            String modelName = BLOCK_FOLDER + "/" + name + suffix;
//                            Identifier bottom = modLoc(BLOCK_FOLDER + "/" + name + "_bottom");
//                            Identifier center = modLoc(BLOCK_FOLDER + "/" + name + "_center");
//                            Identifier top = modLoc(BLOCK_FOLDER + "/" + name + "_top");
//                            Identifier selected = partTop ? top : partMiddle ? center : bottom;
//                            ModelFile door;
//                            if (hingeRight) {
//                                if (partTop) {
//                                    if (open) {
//                                        door = models().doorTopRightOpen(modelName, selected, selected).renderType("cutout");
//                                    } else {
//                                        door = models().doorTopRight(modelName, selected, selected).renderType("cutout");
//                                    }
//                                } else {
//                                    if (open) {
//                                        door = models().doorBottomRightOpen(modelName, selected, selected).renderType("cutout");
//                                    } else {
//                                        door = models().doorBottomRight(modelName, selected, selected).renderType("cutout");
//                                    }
//                                }
//                            } else {
//                                if (partTop) {
//                                    if (open) {
//                                        door = models().doorTopLeftOpen(modelName, selected, selected).renderType("cutout");
//                                    } else {
//                                        door = models().doorTopLeft(modelName, selected, selected).renderType("cutout");
//                                    }
//                                } else {
//                                    if (open) {
//                                        door = models().doorBottomLeftOpen(modelName, selected, selected).renderType("cutout");
//                                    } else {
//                                        door = models().doorBottomLeft(modelName, selected, selected).renderType("cutout");
//                                    }
//                                }
//                            }
//                            int yRot = (int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 90;
//                            yRot = open && hingeRight ? yRot - 90 : yRot;
//                            yRot = open && !hingeRight ? yRot + 90 : yRot;
//                            yRot = yRot < 0 ? 360 + yRot : yRot;
//                            yRot = yRot % 360;
//                            return ConfiguredModel.builder().modelFile(door).rotationY(yRot).build();
//                        });
//            } else if (holder.get() instanceof SmallDragonDoor) {
//                getVariantBuilder(holder.get())
//                        .forAllStates(state -> {
//                            boolean hingeRight = state.getValue(SmallDragonDoor.HINGE) == DoorHingeSide.RIGHT;
//                            boolean open = state.getValue(SmallDragonDoor.OPEN);
//                            String name = holder.getId().getPath();
//                            String suffix = (hingeRight && !open || !hingeRight && open ? "_hinge" : "");
//                            Identifier texture = modLoc(BLOCK_FOLDER + "/" + name);
//                            ModelFile door = models()
//                                    .withExistingParent(name + suffix, DragonSurvival.res(BLOCK_FOLDER + "/" + "small_dragon_door" + (suffix.equals("_hinge") ? "_rh" : "")))
//                                    .texture("bottom", texture)
//                                    .texture("top", texture);
//                            int yRot = (int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 90;
//                            yRot = open && hingeRight ? yRot - 90 : yRot;
//                            yRot = open && !hingeRight ? yRot + 90 : yRot;
//                            yRot = yRot < 0 ? 360 + yRot : yRot;
//                            yRot = yRot % 360;
//                            return ConfiguredModel.builder().modelFile(door).rotationY(yRot).build();
//                        });
//            } else if (holder.get() instanceof HelmetBlock) {
//                String name = holder.getId().getPath();
//                getVariantBuilder(holder.get())
//                        .forAllStates(state -> {
//                            BlockModelBuilder builder = models().withExistingParent(name, "block/skull")
//                                    .texture("all", modLoc(BLOCK_FOLDER + "/" + name));
//                            return ConfiguredModel.builder().modelFile(builder).build();
//                        });
//            } else if (holder.get() instanceof DragonPressurePlates plate) {
//                String name = holder.getId().getPath();
//                Identifier texture = modLoc("block/" + name);
//                ModelFile pressurePlate = models().pressurePlate(name, texture);
//                ModelFile pressurePlateDown = models().pressurePlateDown(name + "_down", texture);
//                horizontalFacingPressurePlate(plate, pressurePlateDown, pressurePlate);
//            } else if (holder.get() instanceof DragonAltarBlock) {
//                String name = holder.getId().getPath();
//                BlockModelBuilder builder = models().withExistingParent(name, "orientable")
//                        .texture("up", modLoc(BLOCK_FOLDER + "/" + name + "_top"))
//                        .texture("down", modLoc(BLOCK_FOLDER + "/" + name + "_top"))
//                        .texture("east", modLoc(BLOCK_FOLDER + "/" + name + "_east"))
//                        .texture("west", modLoc(BLOCK_FOLDER + "/" + name + "_west"))
//                        .texture("north", modLoc(BLOCK_FOLDER + "/" + name + "_north"))
//                        .texture("south", modLoc(BLOCK_FOLDER + "/" + name + "_south"))
//                        .texture("particle", modLoc(BLOCK_FOLDER + "/" + name + "_top"));
//
//                getVariantBuilder(holder.get())
//                        .forAllStates(state ->
//                                ConfiguredModel.builder()
//                                        .modelFile(builder)
//                                        .rotationY((int) state.getValue(DragonAltarBlock.FACING).toYRot())
//                                        .build()
//                        );
//            } else if (holder.get() instanceof TreasureBlock treasureBlock) {
//                getVariantBuilder(treasureBlock)
//                        .forAllStatesExcept(state -> {
//                            int layers = state.getValue(TreasureBlock.LAYERS);
//                            String name = holder.getId().getPath();
//                            BlockModelBuilder builder = layers != 8 ? models()
//                                    // FIXME :: use our own base since this re-use causes issues with resourcepacks
//                                    .withExistingParent(name + layers * 2, BLOCK_FOLDER + "/" + "snow_height" + layers * 2)
//                                    .texture("particle", modLoc(BLOCK_FOLDER + "/" + name))
//                                    .texture("texture", modLoc(BLOCK_FOLDER + "/" + name)) // TODO :: why isn't this particle added to the full block type?
//                                    : /* 8 layers */ models().cubeAll(name, modLoc(BLOCK_FOLDER + "/" + name));
//                            return ConfiguredModel.builder().modelFile(builder).build();
//                        }, TreasureBlock.WATERLOGGED);
//            } else if (holder.get() instanceof VaultBlock vaultBlock) {
//                String name = holder.getId().getPath();
//
//                getVariantBuilder(vaultBlock)
//                        .forAllStates(state -> {
//                                    // I don't know why there is a 180 offset for just this block, but vanilla does it so we do it
//                                    int yRot = (int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() - 180;
//                                    // Map model file based off of the current state
//                                    VaultState vaultState = state.getValue(VaultBlock.STATE);
//                                    String suffix = vaultState.name().toLowerCase(Locale.ENGLISH);
//                                    // For some reason, vanilla named this state "ejecting_reward" instead of just "ejecting", so I'm maintaining that convention here
//                                    if (vaultState == VaultState.EJECTING) {
//                                        suffix = "ejecting_reward";
//                                    }
//                                    BlockModelBuilder builder = models().withExistingParent(name + "_" + suffix, "template_vault")
//                                            .texture("bottom", modLoc(BLOCK_FOLDER + "/" + name + "_bottom"))
//                                            .texture("front", modLoc(BLOCK_FOLDER + "/" + name + "_front" + (vaultState == VaultState.ACTIVE ? "_on" : vaultState == VaultState.INACTIVE ? "_off" : "_ejecting")))
//                                            .texture("side", modLoc(BLOCK_FOLDER + "/" + name + "_side" + (vaultState == VaultState.ACTIVE || vaultState == VaultState.EJECTING ? "_on" : "_off")))
//                                            .texture("top", modLoc(BLOCK_FOLDER + "/" + name + "_top" + (vaultState == VaultState.EJECTING ? "_ejecting" : "")))
//                                            .renderType("cutout");
//                                    return ConfiguredModel.builder()
//                                            .modelFile(builder)
//                                            .rotationY(yRot)
//                                            .build();
//                                }
//                        );
//            } else if (holder.get() instanceof DragonRiderWorkbenchBlock) {
//                String name = holder.getId().getPath();
//
//                getVariantBuilder(holder.get())
//                        .forAllStates(state -> {
//                            String modelName = BLOCK_FOLDER + "/" + name;
//                            BlockModelBuilder builder = models().cube(modelName,
//                                    modLoc(modelName + "_down"),
//                                    modLoc(modelName + "_up"),
//                                    modLoc(modelName + "_north"),
//                                    modLoc(modelName + "_south"),
//                                    modLoc(modelName + "_east"),
//                                    modLoc(modelName + "_west")
//                            ).texture("particle", modLoc(modelName + "_down"));
//                            return ConfiguredModel.builder().modelFile(builder).rotationY((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()).build();
//                        });
//            } else if (holder.get() instanceof SourceOfMagicBlock) {
//                String name = holder.getId().getPath();
//
//                getVariantBuilder(holder.get())
//                        .forAllStates(state -> {
//                            boolean isEmpty = !state.getValue(SourceOfMagicBlock.FILLED);
//                            ModelFile.ExistingModelFile modelFile = models().getExistingFile(DragonSurvival.res(BLOCK_FOLDER + "/" + name + (isEmpty ? "_empty" : "")));
//                            return ConfiguredModel.builder()
//                                    .modelFile(modelFile)
//                                    .rotationY((int) (state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180) % 360)
//                                    .build();
//                        });
//            } else if (holder.get() instanceof RotatedPillarBlock) {
//                getVariantBuilder(holder.get())
//                        .forAllStates(state -> {
//                            BlockModelBuilder builder = models().withExistingParent(holder.getId().getPath(), BLOCK_FOLDER + "/cube_column")
//                                    .texture("side", DragonSurvival.res(BLOCK_FOLDER + "/" + "dragons_memory_side"))
//                                    .texture("end", DragonSurvival.res(BLOCK_FOLDER + "/" + "dragons_memory_top"));
//                            return ConfiguredModel.builder().modelFile(builder).build();
//                        });
//            } else if (holder.get() instanceof DragonBeacon) {
//                getVariantBuilder(holder.get())
//                        .forAllStates(state -> {
//                            ModelFile.ExistingModelFile modelFile = models().getExistingFile(DragonSurvival.res(BLOCK_FOLDER + "/" + "empty"));
//                            return ConfiguredModel.builder().modelFile(modelFile).build();
//                        });
//            } else if (holder.get() instanceof SkeletonPieceBlock) {
//                // Parse the string up to "_skin"
//                String[] split = holder.getId().getPath().split("_skin");
//
//                // The last character has the number for the skin to select, so parse it
//                String skin = split[1].substring(split[1].length() - 1);
//
//                getVariantBuilder(holder.get())
//                        .forAllStates(state -> {
//                            BlockModelBuilder builder = models().withExistingParent(holder.getId().getPath(), DragonSurvival.res(BLOCK_FOLDER + "/" + split[0]))
//                                    .texture("skeleton_texture", DragonSurvival.res(BLOCK_FOLDER + "/" + "skeleton_dragon_" + skin))
//                                    .texture("particle", DragonSurvival.res(BLOCK_FOLDER + "/" + "placeholder_" + skin));
//                            return ConfiguredModel.builder().modelFile(builder).rotationY((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()).build();
//                        });
//            } else if (holder.get() instanceof PrimordialAnchorBlock) {
//                getVariantBuilder(holder.get())
//                        .forAllStates(state -> {
//                            String suffix = state.getValue(PrimordialAnchorBlock.BLOODY) ? "_bloody" : state.getValue(PrimordialAnchorBlock.CHARGED) ? "_charge" : "_empty";
//                            BlockModelBuilder builder = models().withExistingParent(holder.getId().getPath() + suffix, DragonSurvival.res(BLOCK_FOLDER + "/" + "primordial_anchor"))
//                                    .texture("2", DragonSurvival.res(BLOCK_FOLDER + "/" + "primordial_anchor" + suffix));
//
//                            return ConfiguredModel.builder().modelFile(builder).build();
//                        });
//            } else if (holder.get() instanceof DragonSoulBlock) {
//                getVariantBuilder(holder.get())
//                        .forAllStates(state -> ConfiguredModel.builder().modelFile(models().getExistingFile(DragonSurvival.res(BLOCK_FOLDER + "/dragon_soul"))).rotationY((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot()).build());
//            }
//        });
    }

//    public void horizontalFacingPressurePlate(final DragonPressurePlates plate, final ModelFile poweredModel, final ModelFile unpoweredModel) {
//        getVariantBuilder(plate)
//                .forAllStatesExcept(state -> ConfiguredModel.builder()
//                                .modelFile(state.getValue(DragonPressurePlates.POWERED) ? poweredModel : unpoweredModel)
//                                .rotationY((((int) state.getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot() + 180)) % 360)
//                                .build()
//                        , DragonPressurePlates.WATERLOGGED);
//    }

    @Override
    public @NotNull String getName() {
        return "Dragon Survival Block states";
    }
}