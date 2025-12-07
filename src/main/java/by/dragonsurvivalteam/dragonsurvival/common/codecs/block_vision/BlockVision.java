package by.dragonsurvivalteam.dragonsurvival.common.codecs.block_vision;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.MiscCodecs;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncBlockVision;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.BlockVisionData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlockVision extends DurationInstanceBase<BlockVisionData, BlockVision.Instance> {
    @Translation(comments = {
            "§6■ Block Vision:§r",
            " - Range: %s",
            " - Color: %s",
            " - Applies to: %s"
    })
    private static final String HARVEST_BONUS = Translation.Type.GUI.wrap("block_vision");

    @Translation(comments = "Multiple")
    private static final String MULTIPLE_COLORS = Translation.Type.GUI.wrap("block_vision.multiple_colors");

    public static final int NO_RANGE = 0;

    public static final int DEFAULT_PARTICLE_RATE = 10;
    public static final double DEFAULT_COLOR_SHIFT_RATE = 1;
    public static final int NO_PARTICLE_RATE = -1;

    public static final Codec<BlockVision> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(BlockVision::blocks),
            LevelBasedValue.CODEC.fieldOf("range").forGetter(BlockVision::range),
            DisplayType.CODEC.fieldOf("display_type").forGetter(BlockVision::displayType),
            TextColor.CODEC.listOf().fieldOf("colors").forGetter(BlockVision::colors),
            ExtraCodecs.intRange(1, Integer.MAX_VALUE).optionalFieldOf("particle_rate", DEFAULT_PARTICLE_RATE).forGetter(BlockVision::particleRate),
            MiscCodecs.doubleRange(0.1, 10).optionalFieldOf("particle_rate", DEFAULT_COLOR_SHIFT_RATE).forGetter(BlockVision::colorShiftRate)
    ).apply(instance, BlockVision::new));

    private final HolderSet<Block> blocks;
    private final LevelBasedValue range;
    private final DisplayType displayType;
    private final List<TextColor> colors;
    private final int particleRate;
    private final double colorShiftRate;

    public BlockVision(
            final DurationInstanceBase<?, ?> base,
            final HolderSet<Block> blocks,
            final LevelBasedValue range,
            final DisplayType displayType,
            final List<TextColor> colors,
            final int particleRate,
            final double colorShiftRate
    ) {
        super(base);
        this.blocks = blocks;
        this.range = range;
        this.displayType = displayType;
        this.colors = colors;
        this.particleRate = particleRate;
        this.colorShiftRate = colorShiftRate;
    }

    public MutableComponent getDescription(final int abilityLevel) {
        int range = (int) this.range.calculate(abilityLevel);

        Component color;

        if (colors.size() == 1) {
            color = DSColors.withColor(colors.getFirst().serialize(), colors.getFirst().getValue());
        } else if (colors.size() > 1) {
            color = DSColors.withColor(Component.translatable(MULTIPLE_COLORS), Functions.lerpColor(colors.stream().map(TextColor::getValue).toList()));
        } else {
            color = DSColors.dynamicValue(Component.translatable(LangKey.NONE));
        }

        MutableComponent appliesTo = Functions.translateHolderSet(blocks, Holder::getRegisteredName);
        return Component.translatable(HARVEST_BONUS, DSColors.dynamicValue(range), color, DSColors.dynamicValue(appliesTo));
    }

    public static BlockVision.Builder create(final DurationInstanceBase<?, ?> base) {
        return new BlockVision.Builder(base);
    }

    public HolderSet<Block> blocks() {
        return blocks;
    }

    public LevelBasedValue range() {
        return range;
    }

    public DisplayType displayType() {
        return displayType;
    }

    public List<TextColor> colors() {
        return colors;
    }

    public int particleRate() {
        return particleRate;
    }

    public double colorShiftRate() {
        return colorShiftRate;
    }

    @Override
    public Instance createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, final int currentDuration) {
        return new Instance(this, CommonData.from(id(), dragon, ability, customIcon(), shouldRemoveAutomatically()), currentDuration);
    }

    @Override
    public AttachmentType<BlockVisionData> type() {
        return DSDataAttachments.BLOCK_VISION.value();
    }

    public static class Instance extends DurationInstance<BlockVision> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(
                instance -> DurationInstance.codecStart(instance, () -> BlockVision.CODEC).apply(instance, Instance::new)
        );

        public Instance(final BlockVision baseData, final CommonData commonData, final int currentDuration) {
            super(baseData, commonData, currentDuration);
        }

        /** If the passed state is 'null' it will return the range as well */
        public int getRange(@Nullable final Block block) {
            //noinspection deprecation -> ignore
            if (block == null || baseData().blocks().contains(block.builtInRegistryHolder())) {
                return (int) Math.max(NO_RANGE, baseData().range().calculate(appliedAbilityLevel()));
            }

            return 0;
        }

        public List<Integer> getColors(final Block block) {
            //noinspection deprecation -> ignore
            if (baseData().blocks().contains(block.builtInRegistryHolder())) {
                return baseData().colors().stream().map(color -> DSColors.withAlpha(color.getValue(), 1)).toList();
            }

            return List.of();
        }

        public DisplayType getDisplayType(final Block block) {
            //noinspection deprecation -> ignore
            if (baseData().blocks().contains(block.builtInRegistryHolder())) {
                return baseData().displayType();
            }

            return DisplayType.NONE;
        }

        public int getParticleRate(final Block block) {
            //noinspection deprecation -> ignore
            if (baseData().blocks().contains(block.builtInRegistryHolder())) {
                return baseData().particleRate();
            }

            return NO_PARTICLE_RATE;
        }

        @Override
        public Component getDescription() {
            return baseData().getDescription(appliedAbilityLevel());
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncBlockVision(player.getId(), this, false));
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncBlockVision(player.getId(), this, true));
            }
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable BlockVision.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }
    }

    public enum DisplayType implements StringRepresentable {
        OUTLINE("outline"),
        PARTICLES("particles"),
        TREASURE("treasure"),
        TREASURE_SHADER("treasure_shader"),
        NONE("none");

        public static final Codec<DisplayType> CODEC = StringRepresentable.fromEnum(DisplayType::values);
        private final String name;

        DisplayType(final String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }
    
    public static class Builder {
        private final DurationInstanceBase<?, ?> base;
        private HolderSet<Block> blocks = HolderSet.empty();
        private LevelBasedValue range = LevelBasedValue.constant(1);
        private DisplayType displayType = DisplayType.NONE;
        private List<TextColor> colors = List.of();
        private int particleRate = DEFAULT_PARTICLE_RATE;
        private double colorShiftRate = DEFAULT_COLOR_SHIFT_RATE;

        public Builder(final DurationInstanceBase<?, ?> base) {
            this.base = base;
        }

        public Builder blocks(final HolderSet<Block> blocks) {
            this.blocks = blocks;
            return this;
        }

        public Builder range(final LevelBasedValue range) {
            this.range = range;
            return this;
        }

        public Builder displayType(final DisplayType displayType) {
            this.displayType = displayType;
            return this;
        }

        public Builder colors(final List<TextColor> colors) {
            this.colors = colors;
            return this;
        }

        public Builder particleRate(final int particleRate) {
            this.particleRate = particleRate;
            return this;
        }

        public Builder colorShiftRate(final double colorShiftRate) {
            this.colorShiftRate = colorShiftRate;
            return this;
        }

        public BlockVision build() {
            return new BlockVision(base, blocks, range, displayType, colors, particleRate, colorShiftRate);
        }
    }
}
