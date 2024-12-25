package by.dragonsurvivalteam.dragonsurvival.common.codecs;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.network.modifiers.SyncModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.DSDataAttachments;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.ModifiersWithDuration;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.AttributeModifierSupplier;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
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

import java.util.*;
import javax.annotation.Nullable;

public record ModifierWithDuration(ResourceLocation id, ResourceLocation icon, List<Modifier> modifiers, LevelBasedValue duration, boolean isHidden) {
    public static final ResourceLocation DEFAULT_MODIFIER_ICON = ResourceLocation.fromNamespaceAndPath(DragonSurvival.MODID, "textures/modifiers/default_modifier.png");

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

        if (instance != null) {
            data.remove(target, instance);
        }

        ClientEffectProvider.ClientData clientData = new ClientEffectProvider.ClientData(icon, /* TODO */ Component.empty(), Optional.of(dragon.getUUID()));
        instance = new ModifierWithDuration.Instance(this, clientData, ability.level(), newDuration, new HashMap<>());
        data.add(target, instance);

        if (target instanceof ServerPlayer serverPlayer) {
            // TODO :: just sync client data in one generic packet so it can be re-used?
            PacketDistributor.sendToPlayer(serverPlayer, new SyncModifierWithDuration(serverPlayer.getId(), instance, false));
        }
    }

    public void remove(final LivingEntity target) {
        ModifiersWithDuration data = target.getData(DSDataAttachments.MODIFIERS_WITH_DURATION);
        Instance instance = data.get(id);

        if (instance != null) {
            data.remove(target, instance);

            if (target instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncModifierWithDuration(serverPlayer.getId(), instance, true));
            }
        }
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
        public void onAddedToStorage(final Entity entity) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                return;
            }

            Holder<DragonType> type = null;

            if (entity instanceof Player player) {
                type = DragonUtils.getType(player);
            }

            applyModifiers(livingEntity, type, appliedAbilityLevel());
        }

        @Override
        public void onRemovalFromStorage(final Entity entity) {
            if (!(entity instanceof LivingEntity livingEntity)) {
                return;
            }

            removeModifiers(livingEntity);
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
