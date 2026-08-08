package by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedBoolean;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.LevelBasedValue;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.network.PacketDistributor;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSwimDataEntry;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.SwimData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

public record SwimEffect(LevelBasedValue maxOxygen, LevelBasedBoolean hasStableSwim, Holder<FluidType> fluidType) implements AbilityEntityEffect {
    @Translation(comments = "§6■ Allows you to breathe in %s for %s")
    private static final String BONUS = Translation.Type.GUI.wrap("swim_effect.bonus");

    @Translation(comments = "an unlimited time")
    private static final String FOREVER = Translation.Type.GUI.wrap("swim_effect.unlimited_time");

    @Translation(comments = "§6■ Prevents you from automatically sinking in %s")
    private static final String STABLE_SWIM = Translation.Type.GUI.wrap("swim_effect.stable_swim");

    public static final MapCodec<SwimEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            // TODO :: Consider fluid-specific speed bonus
            // FIXME :: 26.1 -> should be called 'base_oxygen'
            LevelBasedValue.CODEC.optionalFieldOf("max_oxygen", LevelBasedValue.constant(0)).forGetter(SwimEffect::maxOxygen),
            LevelBasedBoolean.CODEC.optionalFieldOf("has_stable_swim", LevelBasedBoolean.constant(false)).forGetter(SwimEffect::hasStableSwim),
            MiscCodecs.forgeRegistryHolderCodec(ForgeRegistries.FLUID_TYPES).fieldOf("fluid_type").forGetter(SwimEffect::fluidType)
    ).apply(instance, SwimEffect::new));

    @Override
    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Entity target) {
        if (!(target instanceof ServerPlayer player)) {
            return;
        }

        SwimData data = SwimData.getData(player);
        Entry entry = Entry.calculate(this, ability.level());
        SwimData.Entry previous = data.add(entry);

        if (previous == null || previous.maxOxygen() != entry.maxOxygen() || previous.hasStableSwim() != entry.hasStableSwim()) {
            PacketDistributor.sendToPlayer(player, new SyncSwimDataEntry(entry, false));
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
        ResourceKey<FluidType> fluidKey = fluidType.unwrapKey().orElseThrow();
        SwimData.Entry removed = data.remove(fluidKey);

        if (removed != null) {
            PacketDistributor.sendToPlayer(player, new SyncSwimDataEntry(removed.convert(fluidKey), true));
        }
    }

    @Override
    public List<MutableComponent> getDescription(final Player dragon, final DragonAbilityInstance ability) {
        List<MutableComponent> description = new ArrayList<>();

        int bonus = (int) maxOxygen.calculate(ability.level());
        Component value;

        if (bonus == SwimData.UNLIMITED_OXYGEN) {
            value = DSColors.dynamicValue(FOREVER);
        } else {
            value = DSColors.dynamicValue(Functions.Time.fromTicks(bonus).format(Functions.Time.TimeType.MINUTES));
        }

        description.add(Component.translatable(BONUS, DSColors.dynamicValue(fluidType.value().getDescriptionId()), value));

        if (hasStableSwim.calculate(ability.level())) {
            description.add(Component.translatable(STABLE_SWIM, DSColors.dynamicValue(fluidType.value().getDescriptionId())));
        }

        return description;
    }

    @Override
    public MapCodec<? extends AbilityEntityEffect> entityCodec() {
        return CODEC;
    }

    public record Entry(int maxOxygen, boolean hasStableSwim, ResourceKey<FluidType> fluidType) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("max_oxygen").forGetter(Entry::maxOxygen),
                Codec.BOOL.fieldOf("has_stable_swim").forGetter(Entry::hasStableSwim),
                ResourceKey.codec(ForgeRegistries.Keys.FLUID_TYPES).fieldOf("fluid_type").forGetter(Entry::fluidType)
        ).apply(instance, Entry::new));

        public static Entry calculate(final SwimEffect effect, int level) {
            return new Entry((int) effect.maxOxygen().calculate(level), effect.hasStableSwim().calculate(level), effect.fluidType().unwrapKey().orElseThrow());
        }
    }
}
