package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncBlockVision;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.BlockVisionData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
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
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// TODO :: should have range : holderset (in the vision handle the highest range decides the search range, the data entries store the visible range per block (and color)
public record BlockVision(DurationInstanceBase base, HolderSet<Block> blocks, TextColor outlineColor) {
    public static int NO_COLOR = -1;

    public static final Codec<BlockVision> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(BlockVision::base),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(BlockVision::blocks),
            TextColor.CODEC.fieldOf("outline_color").forGetter(BlockVision::outlineColor)
    ).apply(instance, BlockVision::new));

    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final Player target) {
        int newDuration = (int) base.duration().calculate(ability.level());

        BlockVisionData data = target.getData(DSDataAttachments.BLOCK_VISION);
        BlockVision.Instance instance = data.get(base.id());

        if (instance != null && instance.appliedAbilityLevel() == ability.level() && instance.currentDuration() == newDuration) {
            return;
        }

        data.remove(target, instance);
        data.add(target, new BlockVision.Instance(this, ClientEffectProvider.ClientData.from(dragon, ability, base.customIcon()), ability.level(), newDuration));
    }

    public void remove(final Player target) {
        BlockVisionData blockVision = target.getData(DSDataAttachments.BLOCK_VISION);
        blockVision.remove(target, blockVision.get(base.id()));
    }

    public MutableComponent getDescription(final int abilityLevel) {
        // FIXME :: ADD DESCRIPTION
        return Component.empty();
    }

    public static class Instance extends DurationInstance<BlockVision> {
        public static final Codec<BlockVision.Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(instance, () -> BlockVision.CODEC).apply(instance, BlockVision.Instance::new));

        public Instance(final BlockVision baseData, final ClientData clientData, int appliedAbilityLevel, int currentDuration) {
            super(baseData, clientData, appliedAbilityLevel, currentDuration);
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable BlockVision.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }

        public int getColor(final BlockState state) {
            if (baseData().blocks().contains(state.getBlockHolder())) {
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

        @Override
        public ResourceLocation id() {
            return baseData().base().id();
        }

        @Override
        public int getDuration() {
            return (int) baseData().base().duration().calculate(appliedAbilityLevel());
        }

        @Override
        public Optional<LootItemCondition> earlyRemovalCondition() {
            return baseData().base().earlyRemovalCondition();
        }

        @Override
        public boolean isHidden() {
            return baseData().base().isHidden();
        }
    }
}
