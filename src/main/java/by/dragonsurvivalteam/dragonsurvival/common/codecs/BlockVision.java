package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncBlockVision;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.BlockVisionData;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.HarvestBonuses;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;

public record BlockVision(ResourceLocation id, HolderSet<Block> blocks, LevelBasedValue duration, int outlineColor, Optional<ResourceLocation> customIcon, boolean isHidden) {

    public static final Codec<BlockVision> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(BlockVision::id),
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("blocks").forGetter(BlockVision::blocks),
            LevelBasedValue.CODEC.optionalFieldOf("duration", LevelBasedValue.constant(DurationInstance.INFINITE_DURATION)).forGetter(BlockVision::duration),
            ExtraCodecs.ARGB_COLOR_CODEC.fieldOf("outline_color").forGetter(BlockVision::outlineColor),
            ResourceLocation.CODEC.optionalFieldOf("custom_icon").forGetter(BlockVision::customIcon),
            Codec.BOOL.optionalFieldOf("is_hidden", false).forGetter(BlockVision::isHidden)
    ).apply(instance, BlockVision::new));

    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final LivingEntity target) {
        int newDuration = (int) duration.calculate(ability.level());

        BlockVisionData data = target.getData(DSDataAttachments.BLOCK_VISION);
        BlockVision.Instance instance = data.get(id);

        if (instance != null && instance.appliedAbilityLevel() == ability.level() && instance.currentDuration() == newDuration) {
            return;
        }

        data.remove(target, instance);
        data.add(target, new BlockVision.Instance(this, ClientEffectProvider.ClientData.from(dragon, ability, customIcon), ability.level(), newDuration));
    }

    public void remove(final LivingEntity target) {
        HarvestBonuses data = target.getData(DSDataAttachments.HARVEST_BONUSES);
        data.remove(target, data.get(id));
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

        @Override
        public Component getDescription() {
            return baseData().getDescription(appliedAbilityLevel());
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncBlockVision(player.getId(), this, false));
            } else {
                // Refresh the world renderer since this visual effect interacts with it
                DragonSurvival.PROXY.levelRendererAllChanged();
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncBlockVision(player.getId(), this, true));
            } else {
                // Refresh the world renderer since this visual effect interacts with it
                DragonSurvival.PROXY.levelRendererAllChanged();
            }
        }

        public boolean isVisible(final BlockState state) {
            return baseData().blocks().contains(state.getBlockHolder());
        }

        @Override
        public ResourceLocation id() {
            return baseData().id();
        }

        @Override
        public int getDuration() {
            return (int) baseData().duration().calculate(appliedAbilityLevel());
        }

        @Override
        public boolean isHidden() {
            return baseData().isHidden();
        }
    }
}
