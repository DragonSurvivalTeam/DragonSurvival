package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSwimDataEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public record SwimEffect(LevelBasedValue maxOxygen, Holder<FluidType> fluidType) implements AbilityEntityEffect {
    public static final MapCodec<SwimEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            // TODO :: also handle the movement speed bonus here?
            //  so that a different speed can be applied to different fluids
            LevelBasedValue.CODEC.fieldOf("max_oxygen").forGetter(SwimEffect::maxOxygen),
            // TODO :: holderset?
            NeoForgeRegistries.FLUID_TYPES.holderByNameCodec().fieldOf("fluid_type").forGetter(SwimEffect::fluidType)
    ).apply(instance, SwimEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof ServerPlayer player)) {
            return;
        }

        SwimData data = SwimData.getData(player);
        int maxOxygen = (int) this.maxOxygen.calculate(ability.level());
        Integer previous = data.add(maxOxygen, fluidType);

        if (previous == null || previous != maxOxygen) {
            PacketDistributor.sendToPlayer(player, new SyncSwimDataEntry(maxOxygen, fluidType, false));
        }
    }

    @Override
    public void remove(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target, final boolean isAutoRemoval) {
        if (isAutoRemoval) {
            return;
        }

        if (!(target instanceof ServerPlayer player)) {
            return;
        }

        SwimData data = SwimData.getData(player);
        data.remove(fluidType);
        PacketDistributor.sendToPlayer(player, new SyncSwimDataEntry(0, fluidType, true));
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }
}
