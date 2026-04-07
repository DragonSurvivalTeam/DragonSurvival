package by.dragonsurvivalteam.dragonsurvival.registry.datagen;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.registry.DSArmorMaterials;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.function.BiConsumer;

public class DataEquipmentAssetProvider extends EquipmentAssetProvider {
    public DataEquipmentAssetProvider(final PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels(final BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
        output.accept(DSArmorMaterials.LIGHT_DRAGON_ARMOR_ASSET, EquipmentClientInfo.builder().addHumanoidLayers(DragonSurvival.res("light_dragon")).build());
        output.accept(DSArmorMaterials.DARK_DRAGON_ARMOR_ASSET, EquipmentClientInfo.builder().addHumanoidLayers(DragonSurvival.res("dark_dragon")).build());
    }
}
