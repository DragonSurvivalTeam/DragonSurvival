package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncHarvestBonus;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HarvestBonuses;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.DSLanguageProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType") // ignore
public class HarvestBonus extends DurationInstanceBase<HarvestBonuses, HarvestBonus.Instance> {
    @Translation(comments = {
            "§6■ Harvest Bonus:§r",
            " - Base speed: %s",
            " - Harvest level: %s",
            " - Break speed: %s",
            " - Applies to: %s"
    })
    private static final String HARVEST_BONUS = Translation.Type.GUI.wrap("harvest_bonus");

    @Translation(comments = "All blocks")
    private static final String ALL_BLOCKS = Translation.Type.GUI.wrap("harvest_bonus.all_blocks");

    @Translation(comments = "Various Blocks (%s)")
    private static final String VARIOUS_BLOCKS = Translation.Type.GUI.wrap("harvest_bonus.various_blocks");

    @Translation(comments = "None")
    private static final String NONE = Translation.Type.GUI.wrap("harvest_bonus.none");

    @Translation(comments = "Default")
    private static final String DEFAULT = Translation.Type.GUI.wrap("harvest_bonus.default");

    public static float BASE_SPEED = 1;
    public static int NO_BONUS_VALUE = 0;
    public static final LevelBasedValue NO_BONUS = LevelBasedValue.constant(NO_BONUS_VALUE);

    public static final Codec<HarvestBonus> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            RegistryCodecs.homogeneousList(Registries.BLOCK).optionalFieldOf("blocks").forGetter(HarvestBonus::blocks),
            LevelBasedTier.CODEC.optionalFieldOf("base_speed").forGetter(HarvestBonus::tiers),
            LevelBasedValue.CODEC.optionalFieldOf("harvest_bonus", NO_BONUS).forGetter(HarvestBonus::harvestBonus),
            LevelBasedValue.CODEC.optionalFieldOf("break_speed_multiplier", NO_BONUS).forGetter(HarvestBonus::breakSpeedMultiplier)
    ).apply(instance, HarvestBonus::new));

    private final Optional<HolderSet<Block>> blocks;
    private final Optional<LevelBasedTier> tiers;
    private final LevelBasedValue harvestBonus;
    private final LevelBasedValue breakSpeedMultiplier;

    public HarvestBonus(final DurationInstanceBase<?, ?> base, final Optional<HolderSet<Block>> blocks, final Optional<LevelBasedTier> tiers, final LevelBasedValue harvestBonus, final LevelBasedValue breakSpeedMultiplier) {
        super(base);
        this.blocks = blocks;
        this.tiers = tiers;
        this.harvestBonus = harvestBonus;
        this.breakSpeedMultiplier = breakSpeedMultiplier;
    }

    public MutableComponent getDescription(final int abilityLevel) {
        int harvestBonus = (int) this.harvestBonus.calculate(abilityLevel);
        String breakSpeedMultiplier = NumberFormat.getPercentInstance().format(1 + this.breakSpeedMultiplier.calculate(abilityLevel));
        Component appliesTo;

        if (blocks.isEmpty()) {
            appliesTo = Component.translatable(ALL_BLOCKS);
        } else if (blocks.get() instanceof HolderSet.Named<Block> named) {
            appliesTo = Component.translatable(Tags.getTagTranslationKey(named.key()));
        } else if (blocks.get().size() > 0) {
            appliesTo = Component.translatable(VARIOUS_BLOCKS, blocks.get().size());
        } else {
            appliesTo = Component.translatable(NONE);
        }

        Component baseSpeed = null;

        if (tiers.isPresent()) {
            Tiers tier = tiers.get().get(abilityLevel);

            if (tier != null) {
                baseSpeed = DSLanguageProvider.enumValue(tier);
            }
        }

        if (baseSpeed == null) {
            baseSpeed = Component.translatable(DEFAULT);
        }

        return Component.translatable(HARVEST_BONUS, DSColors.dynamicValue(baseSpeed), DSColors.dynamicValue(harvestBonus), DSColors.dynamicValue(breakSpeedMultiplier), DSColors.dynamicValue(appliesTo));
    }

    @Override
    public Instance createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, final int currentDuration) {
        return new Instance(this, CommonData.from(id(), dragon, ability, customIcon(), shouldRemoveAutomatically()), currentDuration);
    }

    @Override
    public AttachmentType<HarvestBonuses> type() {
        return DSDataAttachments.HARVEST_BONUSES.value();
    }

    public Optional<HolderSet<Block>> blocks() {
        return blocks;
    }

    public Optional<LevelBasedTier> tiers() {
        return tiers;
    }

    public LevelBasedValue harvestBonus() {
        return harvestBonus;
    }

    public LevelBasedValue breakSpeedMultiplier() {
        return breakSpeedMultiplier;
    }

    public static class Instance extends DurationInstance<HarvestBonus> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(
                instance, () -> HarvestBonus.CODEC).apply(instance, Instance::new)
        );

        public Instance(final HarvestBonus baseData, final CommonData commonData, int currentDuration) {
            super(baseData, commonData, currentDuration);
        }

        public float getBaseSpeed(final BlockState state) {
            if (baseData().tiers().isEmpty()) {
                return BASE_SPEED;
            }

            if (baseData().blocks().isPresent() && !baseData().blocks().get().contains(state.getBlockHolder())) {
                return BASE_SPEED;
            }

            Tiers tier = baseData().tiers().get().get(appliedAbilityLevel());

            if (tier != null) {
                return tier.getSpeed();
            }

            return BASE_SPEED;
        }

        public int getHarvestBonus(final BlockState state) {
            if (baseData().blocks().isPresent() && !baseData().blocks().get().contains(state.getBlockHolder())) {
                return NO_BONUS_VALUE;
            }

            return (int) baseData().harvestBonus().calculate(appliedAbilityLevel());
        }

        public float getSpeedMultiplier(final BlockState state) {
            if (baseData().blocks().isPresent() && !baseData().blocks().get().contains(state.getBlockHolder())) {
                return NO_BONUS_VALUE;
            }

            return baseData().breakSpeedMultiplier().calculate(appliedAbilityLevel());
        }

        @Override
        public Component getDescription() {
            return baseData().getDescription(appliedAbilityLevel());
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncHarvestBonus(player.getId(), this, false));
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncHarvestBonus(player.getId(), this, true));
            }
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable HarvestBonus.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }
    }
}
