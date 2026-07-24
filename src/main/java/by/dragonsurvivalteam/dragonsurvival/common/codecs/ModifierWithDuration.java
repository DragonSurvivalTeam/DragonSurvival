package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.CommonData;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstance;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.duration_instance.DurationInstanceBase;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ModifiersWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.datagen.lang.LangKey;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.AttributeModifierSupplier;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.entity_effects.ModifierEffect;
import by.dragonsurvivalteam.dragonsurvival.util.DSColors;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ModifierWithDuration extends DurationInstanceBase<ModifiersWithDuration, ModifierWithDuration.Instance> {
    public static final Codec<ModifierWithDuration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DurationInstanceBase.CODEC.fieldOf("base").forGetter(identity -> identity),
            Modifier.CODEC.listOf().fieldOf("modifiers").forGetter(ModifierWithDuration::modifiers)
    ).apply(instance, ModifierWithDuration::new));

    private final List<Modifier> modifiers;

    public ModifierWithDuration(final DurationInstanceBase<?, ?> base, final List<Modifier> modifiers) {
        super(base);
        this.modifiers = modifiers;
    }

    public MutableComponent getDescription(final int abilityLevel) {
        double duration = Functions.ticksToSeconds((int) duration().calculate(abilityLevel));
        MutableComponent description = null;

        for (Modifier modifier : modifiers) {
            MutableComponent name = modifier.getFormattedDescription(abilityLevel, false);

            if (duration > 0) {
                name.append(Component.translatable(LangKey.ABILITY_EFFECT_DURATION, DSColors.dynamicValue(duration)));
            }

            if (description == null) {
                description = Component.literal("\n").append(name);
            } else {
                description.append(Component.literal("\n")).append(name);
            }
        }

        return Objects.requireNonNullElse(description, Component.empty());
    }

    @Override
    public Instance createInstance(final ServerPlayer dragon, final DragonAbilityInstance ability, final int currentDuration) {
        return new Instance(this, CommonData.from(id(), dragon, ability, customIcon(), shouldRemoveAutomatically()), currentDuration);
    }

    @Override
    public AttachmentType<ModifiersWithDuration> type() {
        return DSDataAttachments.MODIFIERS_WITH_DURATION.value();
    }

    public List<Modifier> modifiers() {
        return modifiers;
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

        public Instance(final ModifierWithDuration baseData, final CommonData commonData, final int currentDuration) {
            this(baseData, commonData, currentDuration, new HashMap<>());
        }

        public Instance(final ModifierWithDuration baseData, final CommonData commonData, final int currentDuration, final Map<Holder<Attribute>, List<ResourceLocation>> ids) {
            super(baseData, commonData, currentDuration);
            this.ids = ids;
        }

        @Override
        public Component getDescription() {
            MutableComponent description = baseData().getDescription(appliedAbilityLevel());
            return Component.translatable(ModifierEffect.ATTRIBUTE_MODIFIERS).append(description);
        }

        @Override
        public void onAddedToStorage(final Entity storageHolder) {
            if (!(storageHolder instanceof LivingEntity livingEntity)) {
                return;
            }

            applyModifiers(livingEntity, appliedAbilityLevel());

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

        public Tag save(@NotNull final HolderLookup.Provider provider) {
            return CODEC.encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), this).getOrThrow();
        }

        public static @Nullable Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag nbt) {
            return CODEC.parse(provider.createSerializationContext(NbtOps.INSTANCE), nbt).resultOrPartial(DragonSurvival.LOGGER::error).orElse(null);
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
    }
}
