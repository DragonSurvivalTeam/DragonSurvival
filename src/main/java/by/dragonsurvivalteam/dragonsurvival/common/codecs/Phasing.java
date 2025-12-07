package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncPhasingInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.PhasingData;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.Translation;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Phasing extends DurationInstanceBase<PhasingData, Phasing.Instance> {
    @Translation(comments = {
            "§6■ Block Phasing:§r",
            " - Vision Range: %s",
            " - Applies to: %s"
    })
    private static final String PHASE_DATA = Translation.Type.GUI.wrap("block_phasing");

    public static Codec<Phasing> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(Phasing::blocks),
            LevelBasedValue.CODEC.fieldOf("range").forGetter(Phasing::range),
            Codec.BOOL.optionalFieldOf("invert").forGetter(Phasing::invert)
    ).apply(instance, Phasing::new));

    public static final int NO_RANGE = 0;
    public static final int NO_ALPHA = 255;

    private final HolderSet<Block> blocks;
    private final LevelBasedValue range;
    private final Optional<Boolean> invert;
    
    public Phasing(final DurationInstanceBase<?, ?> base, final HolderSet<Block> blocks, final LevelBasedValue range, final Optional<Boolean> invert) {
        super(base);
        this.blocks = blocks;
        this.range = range;
        this.invert = invert;
    }

    public MutableComponent getDescription(final int abilityLevel) {
        int range = (int) this.range.calculate(abilityLevel);

        MutableComponent appliesTo = Functions.translateHolderSet(blocks, Holder::getRegisteredName);
        return Component.translatable(PHASE_DATA, DSColors.dynamicValue(range), DSColors.dynamicValue(appliesTo));
    }

    public static Phasing create(final ResourceLocation id, final HolderSet<Block> validBlocks, final LevelBasedValue range, final Optional<Boolean> invert) {
        return new Phasing(DurationInstanceBase.create(id).infinite().removeAutomatically().hidden().build(), validBlocks, range, invert);
    }

    public static Phasing create(final ResourceLocation id, final LevelBasedValue duration, final HolderSet<Block> validBlocks, final LevelBasedValue range, final Optional<Boolean> invert) {
        return new Phasing(DurationInstanceBase.create(id).duration(duration).hidden().build(), validBlocks, range, invert);
    }

    @Override
    public Instance createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, final int currentDuration) {
        return new Instance(this, CommonData.from(id(), dragon, ability, customIcon(), shouldRemoveAutomatically()), currentDuration);
    }

    @Override
    public AttachmentType<PhasingData> type() {
        return DSDataAttachments.PHASING.value();
    }

    public HolderSet<Block> blocks() {
        return blocks;
    }

    public LevelBasedValue range() {
        return range;
    }

    public Optional<Boolean> invert() { return invert; }

    public static class Instance extends DurationInstance<Phasing> {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(
                instance, () -> Phasing.CODEC).apply(instance, Instance::new)
        );

        public Instance(final Phasing baseData, final CommonData commonData, int currentDuration) {
            super(baseData, commonData, currentDuration);
        }

        /** If the passed state is 'null' it will return the range as well */
        public int getRange(@Nullable final Block block) {
            //noinspection deprecation -> ignore
            if (block == null || baseData().blocks().contains(block.builtInRegistryHolder())) {
                return (int) Math.max(NO_RANGE, baseData().range().calculate(appliedAbilityLevel()));
            }

            return NO_RANGE;
        }

        /** If the passed state is 'null' it will return the alpha as well */
        public int getAlpha(@Nullable final Block block) {
            //noinspection deprecation -> ignore
            if (block == null || baseData().blocks().contains(block.builtInRegistryHolder())) {
                // Maybe let them set the alpha instead of it being 255/range?
                return (int) Math.max(NO_RANGE, Math.min(NO_ALPHA, Math.ceil(255 / baseData().range().calculate(appliedAbilityLevel()))));
            }

            return NO_ALPHA;
        }

        public boolean getAngleCheck(Block block, Vec3 blockVec, Vec3 blockStraightVec, boolean above, Vec3 entityLookVec, float playerXRot) {
            // Up is -90, down is 90
            // Looking 45 degrees up or down should result in 'stairs' of collision when phasing
            // Looking within 10 degrees of 'down' should result in no collision at all
            // We need angle above/below, not flat - mult by -1 if y is greater than player pos
            // We also need in front of/behind - if in front 180 degrees of view, compare against playerXRot, if behind compare against -playerXRot
            if (block == null || ( baseData().blocks().contains(block.builtInRegistryHolder()) ^ baseData().invert.orElse(false))) {
                int aboveMult = 1;
                if (above) {
                    aboveMult = -1;
                }
                // TODO: Add check for if player is inside of block and exclude those from the check
                double dotXProd = blockVec.dot(blockStraightVec);
                double magXSqBlock = blockVec.dot(blockVec);
                double magSqStraight = blockStraightVec.dot(blockStraightVec);
                double dXSqrt = dotXProd / (Math.sqrt(magXSqBlock) * Math.sqrt(magSqStraight));  // If this is too slow, compare the square
                double dXDegrees = Math.acos(dXSqrt) * 180/Math.PI * aboveMult;
                double dotYProd = entityLookVec.dot(blockStraightVec); // If > 0, in front, else behind
                float compareRot = playerXRot;
                if (dotYProd < 0) {
                    compareRot = -playerXRot;
                }
                return (dXDegrees < compareRot || playerXRot > 80);
            }
            return false;
        }

        @Override
        public Component getDescription() {
            return baseData().getDescription(appliedAbilityLevel());
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (!storageHolder.level().isClientSide()) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(storageHolder, new SyncPhasingInstance(storageHolder.getId(), this, false));
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (!storageHolder.level().isClientSide()) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(storageHolder, new SyncPhasingInstance(storageHolder.getId(), this, true));
            }
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable Phasing.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }
    }
}
