package by.dragonsurvivalteam.dragonsurvival.registry.datagen.data_maps;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonBeaconData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSDataMaps;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEffects;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class DragonBeaconDataProvider extends DataMapProvider {
    public DragonBeaconDataProvider(final PackOutput output, final CompletableFuture<HolderLookup.Provider> provider) {
        super(output, provider);
    }

    @Override
    protected void gather() {
        builder(DSDataMaps.DRAGON_BEACON_DATA)
                .add(BuiltInDragonSpecies.CAVE, new DragonBeaconData(
                        DragonBeaconData.createEffects(Functions.secondsToTicks(20), 0, DSEffects.FIRE, DSEffects.STURDY_SKIN),
                        new DragonBeaconData.PaymentData(60, 30, 0)
                ), false)
                .add(BuiltInDragonSpecies.FOREST, new DragonBeaconData(
                        DragonBeaconData.createEffects(Functions.secondsToTicks(20), 0, DSEffects.PEACE, DSEffects.ANIMAL_PEACE),
                        new DragonBeaconData.PaymentData(60, 30, 0)
                ), false)
                .add(BuiltInDragonSpecies.SEA, new DragonBeaconData(
                        DragonBeaconData.createEffects(Functions.secondsToTicks(20), 0, DSEffects.MAGIC, MobEffects.DIG_SPEED),
                        new DragonBeaconData.PaymentData(60, 30, 0)
                ), false);
    }

    @Override
    public @NotNull String getName() {
        return "Dragon Beacon Data";
    }
}
