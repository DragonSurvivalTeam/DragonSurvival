package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ModifiersWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.AttributeModifierSupplier;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.ClientEffectProvider;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public record ModifierWithDuration(ResourceLocation id, ResourceLocation icon, List<Modifier> modifiers, LevelBasedValue duration, boolean isHidden) {
    public static final ResourceLocation DEFAULT_MODIFIER_ICON = DragonSurvival.res("textures/modifiers/default_modifier.png");

    public static final Codec<ModifierWithDuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(ModifierWithDuration::id),
            ResourceLocation.CODEC.optionalFieldOf("icon", DEFAULT_MODIFIER_ICON).forGetter(ModifierWithDuration::icon),
            Modifier.CODEC.listOf().fieldOf("modifiers").forGetter(ModifierWithDuration::modifiers),
            LevelBasedValue.CODEC.optionalFieldOf("duration", LevelBasedValue.constant(DurationInstance.INFINITE_DURATION)).forGetter(ModifierWithDuration::duration),
            Codec.BOOL.optionalFieldOf("is_hidden", false).forGetter(ModifierWithDuration::isHidden)
    ).apply(instance, ModifierWithDuration::new));

    public void apply(final ServerPlayer dragon, final DragonAbilityInstance ability, final LivingEntity target) {
        int newDuration = (int) duration.calculate(ability.level());

        ModifiersWithDuration data = target.getData(DSDataAttachments.MODIFIERS_WITH_DURATION);
        Instance instance = data.get(id);

        if (instance != null && instance.appliedAbilityLevel() == ability.level() && instance.currentDuration() == newDuration) {
            return;
        }

        data.remove(target, instance);
        data.add(target, new ModifierWithDuration.Instance(this, ClientEffectProvider.ClientData.from(dragon, ability, id, Component.empty(), icon), ability.level(), newDuration, new HashMap<>()));
    }

    public void remove(final LivingEntity target) {
        ModifiersWithDuration data = target.getData(DSDataAttachments.MODIFIERS_WITH_DURATION);
        data.remove(target, data.get(id));
    }

    public static class Instance extends DurationInstance<ModifierWithDuration> implements AttributeModifierSupplier {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> DurationInstance.codecStart(instance, () -> ModifierWithDuration.CODEC)
                .and(Codec.compoundList(BuiltInRegistries.ATTRIBUTE.holderByNameCodec(), ResourceLocation.CODEC.listOf()).xmap(pairs -> {
                            Map<Holder<Attribute>, List<ResourceLocation>> ids = new HashMap<>();
                            pairs.forEach(pair -> pair.getSecond().forEach(id -> ids.computeIfAbsent(pair.getFirst(), key -> new ArrayList<>()).add(id)));
                            return ids;
                        }, ids -> {
                            List<Pair<Holder<Attribute>, List<ResourceLocation>>> pairs = new ArrayList<>();
                            ids.forEach((attribute, value) -> pairs.add(new Pair<>(attribute, value)));
                            return pairs;
                        }).fieldOf("ids").forGetter(Instance::getStoredIds)
                ).apply(instance, Instance::new));

        private final Map<Holder<Attribute>, List<ResourceLocation>> ids;

        public Instance(final ModifierWithDuration baseData, final ClientData clientData, int appliedAbilityLevel, int currentDuration, final Map<Holder<Attribute>, List<ResourceLocation>> ids) {
            super(baseData, clientData, appliedAbilityLevel, currentDuration);
            this.ids = ids;
        }

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (!(storageHolder instanceof LivingEntity livingEntity)) {
                return;
            }

            Holder<DragonSpecies> type = null;

            if (storageHolder instanceof Player player) {
                type = DragonUtils.getType(player);
            }

            applyModifiers(livingEntity, type, appliedAbilityLevel());

            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncModifierWithDuration(player.getId(), this, false));
            }
        }

        @Override
        public void onRemovalFromStorage(final Entity storageHolder) {
            if (!(storageHolder instanceof LivingEntity livingEntity)) {
                return;
            }

            removeModifiers(livingEntity);

            if (storageHolder instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, new SyncModifierWithDuration(player.getId(), this, true));
            }
        }

        @Override
        public int getDuration() {
            return (int) baseData().duration().calculate(appliedAbilityLevel());
        }

        @Override
        public List<Modifier> modifiers() {
            return baseData().modifiers();
        }

        @Override
        public void storeId(final Holder<Attribute> attribute, final ResourceLocation id) {
            ids.computeIfAbsent(attribute, key -> new ArrayList<>()).add(id);
        }

        @Override
        public Map<Holder<Attribute>, List<ResourceLocation>> getStoredIds() {
            return ids;
        }

        @Override
        public ModifierType getModifierType() {
            return ModifierType.CUSTOM;
        }

        @Override
        public ResourceLocation id() {
            return baseData().id();
        }

        @Override
        public boolean isInvisible() {
            return baseData().isHidden();
        }
    }
}
