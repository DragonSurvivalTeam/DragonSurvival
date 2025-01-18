package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockVision extends DurationInstanceBase<BlockVisionData, BlockVision.Instance> {
    @Translation(comments = {
            "§6■ Block Vision:§r",
            " - Range: %s",
            " - Color: %s",
            " - Applies to: %s"
    })
    private static final String HARVEST_BONUS = Translation.Type.GUI.wrap("block_vision");

    public static int NO_COLOR = -1;
    public static int NO_RANGE = 0;

    public static final Codec<BlockVision> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(BlockVision::blocks),
            LevelBasedValue.CODEC.fieldOf("range").forGetter(BlockVision::range),
            TextColor.CODEC.fieldOf("outline_color").forGetter(BlockVision::outlineColor)
    ).apply(instance, BlockVision::new));

    private final HolderSet<Block> blocks;
    private final LevelBasedValue range;
    private final TextColor outlineColor;

    public BlockVision(final DurationInstanceBase<?, ?> base, final HolderSet<Block> blocks, final LevelBasedValue range, final TextColor outlineColor) {
        super(base);
        this.blocks = blocks;
        this.range = range;
        this.outlineColor = outlineColor;
    }

    public MutableComponent getDescription(final int abilityLevel) {
        int range = (int) this.range.calculate(abilityLevel);
        String color = outlineColor.serialize();

        MutableComponent appliesTo = null;

        if (blocks instanceof HolderSet.Named<Block> named) {
            appliesTo = Component.translatable(Tags.getTagTranslationKey(named.key()));
        } else if (blocks.size() > 0) {
            for (Holder<Block> block : blocks) {
                Component name = block.value().getName();

                if (appliesTo == null) {
                    appliesTo = DSColors.dynamicValue(name);
                } else {
                    appliesTo.append(Component.literal(", ").withStyle(ChatFormatting.GRAY)).append(name);
                }
            }
        } else {
            appliesTo = Component.translatable(LangKey.NONE);
        }

        return Component.translatable(HARVEST_BONUS, DSColors.dynamicValue(range), DSColors.withColor(color, outlineColor.getValue()), DSColors.dynamicValue(appliesTo));
    }

    public HolderSet<Block> blocks() {
        return blocks;
    }

    public LevelBasedValue range() {
        return range;
    }

    public TextColor outlineColor() {
        return outlineColor;
    }

    @Override
    public Instance createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, final int currentDuration) {
        return new Instance(this, CommonData.from(id(), dragon, ability, customIcon() , shouldRemoveAutomatically()), currentDuration);
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
                return (int) baseData().range().calculate(appliedAbilityLevel());
            }

            return 0;
        }

        public int getColor(final Block block) {
            //noinspection deprecation -> ignore
            if (baseData().blocks().contains(block.builtInRegistryHolder())) {
                return baseData().outlineColor().getValue();
            }

            return NO_COLOR;
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
}
