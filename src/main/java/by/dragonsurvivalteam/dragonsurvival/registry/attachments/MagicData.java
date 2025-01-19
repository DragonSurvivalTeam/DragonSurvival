package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvival;
import by.dragonsurvivalteam.dragonsurvival.client.gui.hud.MagicHUD;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.codecs.DragonAbilityHolder;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncCooldownState;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicData;
import by.dragonsurvivalteam.dragonsurvival.registry.DSSounds;
import by.dragonsurvivalteam.dragonsurvival.registry.data_components.DSDataComponents;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.BuiltInDragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonSpecies;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbility;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.DragonAbilityInstance;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.ExperiencePointsUpgrade;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.InputData;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.ability.upgrade.UpgradeType;
import by.dragonsurvivalteam.dragonsurvival.util.Functions;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@EventBusSubscriber
public class MagicData implements INBTSerializable<CompoundTag> {
    public static final int HOTBAR_SLOTS = 4;
    public static final int NO_SLOT = -1;

    private final Map<ResourceKey<DragonSpecies>, Map<ResourceKey<DragonAbility>, DragonAbilityInstance>> abilities = new HashMap<>();
    private final Map<ResourceKey<DragonSpecies>, Map<Integer, ResourceKey<DragonAbility>>> hotbar = new HashMap<>();
    private @Nullable ResourceKey<DragonSpecies> currentSpecies; // TODO :: are we storing this in two data attachments now?
    private boolean renderAbilities = true;
    private int selectedAbilitySlot;
    private float currentMana;

    private boolean errorMessageSent;
    private boolean isCasting;
    private int castTimer;

    public static MagicData getData(final Player player) {
        return player.getData(DSDataAttachments.MAGIC);
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public void setCurrentMana(float currentMana) {
        this.currentMana = Math.max(0, currentMana);
    }

    public void setSelectedAbilitySlot(int newSlot) {
        selectedAbilitySlot = newSlot;
    }

    public int getSelectedAbilitySlot() {
        return selectedAbilitySlot;
    }

    public DragonAbilityInstance getSelectedAbility() {
        return fromSlot(selectedAbilitySlot);
    }

    public int getClientCastTimer() {
        return castTimer;
    }

    public void setCurrentSpecies(final Player player, @Nullable final ResourceKey<DragonSpecies> currentSpecies) {
        // Disable any active effects (important for passive abilities)
        if (this.currentSpecies != null && abilities.containsKey(this.currentSpecies)) {
            abilities.get(this.currentSpecies).values().forEach(ability -> ability.setActive(player, false));
        }

        this.currentSpecies = currentSpecies;

        // Since passive abilities need to be activated manually
        if (currentSpecies != null && abilities.containsKey(currentSpecies)) {
            abilities.get(currentSpecies).values().forEach(ability -> {
                if (ability.isPassive()) {
                    ability.setActive(player, true);
                }
            });
        }
    }

    public boolean dataForSpeciesIsEmpty(final ResourceKey<DragonSpecies> species) {
        return abilities.get(species) == null || abilities.get(species).isEmpty();
    }

    public @Nullable DragonAbilityInstance getAbility(final ResourceKey<DragonAbility> key) {
        if (currentSpecies == null) {
            return null;
        }

        return getAbilities().get(key);
    }

    public Map<ResourceKey<DragonAbility>, DragonAbilityInstance> getAbilities() {
        if (currentSpecies == null) {
            return Map.of();
        }

        return abilities.computeIfAbsent(currentSpecies, species -> new HashMap<>());
    }

    private Map<Integer, ResourceKey<DragonAbility>> getHotbar() {
        if (currentSpecies == null) {
            return Map.of();
        }

        return hotbar.computeIfAbsent(currentSpecies, species -> new HashMap<>());
    }

    @SubscribeEvent
    public static void cancelCastingOnDeath(final LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player && DragonStateProvider.isDragon(player)) {
            MagicData.getData(player).stopCasting(player);
        }
    }

    @SubscribeEvent // TODO :: Completely skip in spectator mode (shows stress e.g.)? And penalties in spectator + creative?
    public static void tickAbilities(final PlayerTickEvent.Post event) {
        if (!DragonStateProvider.isDragon(event.getEntity())) {
            return;
        }

        Optional<MagicData> optional = event.getEntity().getExistingData(DSDataAttachments.MAGIC);

        if (optional.isEmpty()) {
            return;
        }

        MagicData magic = optional.get();
        InputData experienceLevels = InputData.experienceLevels(event.getEntity().experienceLevel);

        for (DragonAbilityInstance ability : magic.getAbilities().values()) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                ability.value().upgrade().ifPresent(upgrade -> {
                    // Handle input type 'Void' (e.g. condition based)
                    upgrade.attempt(serverPlayer, ability, null);
                    // Handle auto upgrades for the item based upgrade
                    upgrade.attempt(serverPlayer, ability, Items.AIR);
                    // There are too many ways the experience level field could be modified
                    upgrade.attempt(serverPlayer, ability, experienceLevels);
                });
            }

            ability.tick(event.getEntity());
        }

        if (event.getEntity().level().isClientSide() && magic.isCasting()) {
            magic.castTimer = Math.max(0, magic.castTimer - 1);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void handleItemBasedLeveling(final PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        DragonStateHandler handler = DragonStateProvider.getData(player);

        if (!handler.isDragon()) {
            return;
        }

        ItemStack stack = event.getItemStack();

        if (stack.isEmpty()) {
            return;
        }

        MagicData magic = MagicData.getData(player);
        DragonAbilityHolder abilityHolder = stack.get(DSDataComponents.DRAGON_ABILITIES);

        if (abilityHolder != null && abilityHolder.use(player, handler, magic)) {
            stack.consume(1, player);
            player.playNotifySound(DSSounds.UPGRADE_BEACON.get(), SoundSource.PLAYERS, 1, 0);
            PacketDistributor.sendToPlayer(player, new SyncMagicData(magic.serializeNBT(player.registryAccess())));
        }

        magic.getAbilities().values().forEach(ability -> ability.value().upgrade().ifPresent(upgrade -> {
            if (stack.isEmpty()) {
                return;
            }

            if (upgrade.attempt(player, ability, stack.getItem())) {
                stack.consume(1, player);
                player.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1, 0);
            }
        }));
    }

    public void handleAutoUpgrades(final ServerPlayer player, final Object... inputs) {
        if (!DragonStateProvider.isDragon(player)) {
            return;
        }

        for (DragonAbilityInstance ability : getAbilities().values()) {
            UpgradeType<?> upgrade = ability.value().upgrade().orElse(null);

            if (upgrade == null) {
                continue;
            }

            for (Object input : inputs) {
                upgrade.attempt(player, ability, input);
            }
        }
    }

    public @Nullable DragonAbilityInstance fromSlot(int slot) {
        ResourceKey<DragonAbility> key = getHotbar().get(slot);
        return key != null ? getAbility(key) : null;
    }

    public int slotFromAbility(final ResourceKey<DragonAbility> key) {
        for (int slot : getHotbar().keySet()) {
            if (getHotbar().get(slot) == key) {
                return slot;
            }
        }

        return NO_SLOT;
    }

    public @Nullable DragonAbilityInstance getCurrentlyCasting() {
        return isCasting ? fromSlot(getSelectedAbilitySlot()) : null;
    }

    public boolean attemptCast(final Player player, int slot) {
        if (slot < 0 || slot >= getAbilities().size()) {
            return false;
        }

        DragonAbilityInstance instance = fromSlot(slot);

        if (instance == null) {
            return false;
        }

        if (!canCastAbility(player, instance)) {
            int cooldown = instance.getCooldown();

            if (!errorMessageSent && player.level().isClientSide() && cooldown != DragonAbilityInstance.NO_COOLDOWN) {
                errorMessageSent = true;
                MagicHUD.castingError(Component.translatable(MagicHUD.COOLDOWN, NumberFormat.getInstance().format(Functions.ticksToSeconds(cooldown)) + "s").withStyle(ChatFormatting.RED));
            }

            return false;
        }

        DragonAbilityInstance currentlyCasting = getCurrentlyCasting();

        if (currentlyCasting != null) {
            currentlyCasting.release(player);
        }

        setSelectedAbilitySlot(slot);
        beginCasting(player);

        return true;
    }

    public void stopCasting(final Player player, boolean withCooldown) {
        DragonAbilityInstance currentlyCasting = getCurrentlyCasting();

        if (currentlyCasting != null) {
            currentlyCasting.stopSound(player);

            if (withCooldown) {
                currentlyCasting.release(player);
                currentlyCasting.value().activation().playEndSound(player);

                if (currentlyCasting.hasEndAnimation()) {
                    currentlyCasting.value().activation().playEndAnimation(player);
                } else {
                    DragonSurvival.PROXY.setCurrentAbilityAnimation(player.getId(), null);
                }
            } else {
                DragonSurvival.PROXY.setCurrentAbilityAnimation(player.getId(), null);
                currentlyCasting.releaseWithoutCooldown();
            }

            currentlyCasting.setActive(player, false);

            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new SyncCooldownState(player.getId(), getSelectedAbilitySlot(), currentlyCasting.getCooldown()));
            }
        }

        isCasting = false;
    }

    public void stopCasting(final Player player) {
        DragonAbilityInstance currentlyCasting = getCurrentlyCasting();
        stopCasting(player, currentlyCasting != null && currentlyCasting.isApplyingEffects());
    }

    public void setClientCooldown(final Player player, int slot, int cooldown) {
        DragonAbilityInstance ability = fromSlot(slot);

        if (ability != null) {
            ability.setCooldown(cooldown);
            ability.setActive(player, false);
        }
    }

    public void setErrorMessageSent(boolean errorMessageSent) {
        this.errorMessageSent = errorMessageSent;
    }

    private void beginCasting(final Player player) {
        DragonAbilityInstance instance = fromSlot(getSelectedAbilitySlot());

        if (instance == null) {
            return;
        }

        isCasting = true;
        castTimer = instance.value().getChargeTime(instance.level());
        instance.setActive(player, true);
    }

    public boolean isCasting() {
        return isCasting && getCurrentlyCasting() != null;
    }

    private boolean canCastAbility(final Player dragon, final DragonAbilityInstance instance) {
        return instance.canBeCast() && instance.hasEnoughMana(dragon);
    }

    public boolean shouldRenderAbilities() {
        return renderAbilities;
    }

    public void setRenderAbilities(boolean renderAbilities) {
        this.renderAbilities = renderAbilities;
    }

    /** This does not automatically synchronize the change to the client */
    public int clear(final ServerPlayer player) {
        int count = abilities.values().stream().mapToInt(Map::size).sum();
        abilities.values().forEach(perSpecies -> perSpecies.values().forEach(ability -> ability.setActive(player, false)));
        abilities.clear();
        hotbar.clear();
        return count;
    }

    // TODO :: don't we need to de-activate the ability?
    //  better have a centralized place where we call remove and only allow it with player context
    /** This does not automatically synchronize the change to the client */
    public void removeAbility(final ResourceKey<DragonAbility> key) {
        if (currentSpecies == null) {
            return;
        }

        getAbilities().remove(key);
        int slot = slotFromAbility(key);

        if (slot != NO_SLOT) {
            getHotbar().remove(slot);
        }
    }

    /** This does not automatically synchronize the change to the client */
    public void addAbility(final ServerPlayer player, final Holder<DragonAbility> ability) {
        UpgradeType<?> upgrade = ability.value().upgrade().orElse(null);
        DragonAbilityInstance instance;

        InputData sizeInput = InputData.size((int) DragonStateProvider.getData(player).getSize());

        if (upgrade == null) {
            instance = new DragonAbilityInstance(ability, ability.value().getMaxLevel());
        } else {
            instance = new DragonAbilityInstance(ability, DragonAbilityInstance.MIN_LEVEL);
            upgrade.attempt(player, instance, sizeInput);
        }


        for (int i = 0; i < HOTBAR_SLOTS; i++) {
            if (!getHotbar().containsKey(i)) {
                getHotbar().put(i, ability.getKey());
                break;
            }
        }

        getAbilities().put(ability.getKey(), instance);
    }

    /** This does not automatically synchronize the change to the client */
    public void refresh(final ServerPlayer player, final Holder<DragonSpecies> currentSpecies) {
        // Make sure we remove any passive effects for abilities that are no longer available
        abilities.values().forEach(perSpecies -> perSpecies.values().forEach(ability -> ability.setActive(player, false)));

        if (currentSpecies == null) {
            return;
        }

        this.currentSpecies = currentSpecies.getKey();
        InputData sizeInput = InputData.size((int) DragonStateProvider.getData(player).getSize());

        int slot = 0;

        for (Holder<DragonAbility> ability : currentSpecies.value().abilities()) {
            UpgradeType<?> upgrade = ability.value().upgrade().orElse(null);
            DragonAbilityInstance instance;

            if (upgrade == null) {
                instance = new DragonAbilityInstance(ability, ability.value().getMaxLevel());
            } else {
                instance = new DragonAbilityInstance(ability, DragonAbilityInstance.MIN_LEVEL);
                upgrade.attempt(player, instance, sizeInput);
            }

            if (slot < HOTBAR_SLOTS && !instance.isPassive()) {
                getHotbar().put(slot, ability.getKey());
                slot++;
            }

            getAbilities().put(ability.getKey(), instance);
        }
    }

    public List<DragonAbilityInstance> getActiveAbilities() {
        return getAbilities().values().stream().filter(instance -> !instance.isPassive()).collect(Collectors.toList());
    }

    public List<DragonAbilityInstance> getPassiveAbilities(final Predicate<Optional<UpgradeType<?>>> predicate) {
        return getAbilities().values().stream().filter(instance -> instance.isPassive() && predicate.test(instance.value().upgrade())).collect(Collectors.toList());
    }

    /** Returns the amount of experience gained / lost when down- or upgrading the ability */
    public int getCost(final Player dragon, final ResourceKey<DragonAbility> key, ExperiencePointsUpgrade.Type type) {
        DragonAbilityInstance ability = getAbility(key);

        if (ability == null) {
            return 0;
        }

        return ability.value().upgrade().map(upgrade -> {
            if (upgrade instanceof ExperiencePointsUpgrade experiencePointsUpgrade) {
                return experiencePointsUpgrade.getExperience(dragon, ability, type);
            }

            return 0;
        }).orElse(0);
    }

    public void moveAbilityToSlot(final ResourceKey<DragonAbility> key, int newSlot) {
        int currentSlot = slotFromAbility(key);
        ResourceKey<DragonAbility> previous = getHotbar().put(newSlot, key);

        if (previous != null && currentSlot != NO_SLOT && newSlot != NO_SLOT) {
            getHotbar().put(currentSlot, previous);
        } else {
            getHotbar().remove(currentSlot);
        }
    }

    @Override
    public CompoundTag serializeNBT(@NotNull final HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        CompoundTag allAbilities = new CompoundTag();
        for (Map.Entry<ResourceKey<DragonSpecies>, Map<ResourceKey<DragonAbility>, DragonAbilityInstance>> entry : abilities.entrySet()) {
            CompoundTag abilities = new CompoundTag();
            entry.getValue().values().forEach(instance -> abilities.put(instance.key().location().toString(), instance.save(provider)));
            allAbilities.put(entry.getKey().location().toString(), abilities);
        }

        CompoundTag allHotbars = new CompoundTag();
        for (Map.Entry<ResourceKey<DragonSpecies>, Map<Integer, ResourceKey<DragonAbility>>> entry : hotbar.entrySet()) {
            CompoundTag hotbar = new CompoundTag();
            entry.getValue().forEach((slot, key) -> hotbar.putInt(key.location().toString(), slot));
            allHotbars.put(entry.getKey().location().toString(), hotbar);
        }

        tag.put(ABILITIES, allAbilities);
        tag.put(HOTBARS, allHotbars);
        tag.putFloat(CURRENT_MANA, currentMana);
        tag.putInt(SELECTED_SLOT, selectedAbilitySlot);
        tag.putBoolean(RENDER_ABILITIES, renderAbilities);

        if (currentSpecies != null) {
            tag.putString(CURRENT_SPECIES, currentSpecies.location().toString());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(@NotNull final HolderLookup.Provider provider, @NotNull final CompoundTag tag) {
        this.abilities.clear();
        this.hotbar.clear();

        if (tag.contains(ABILITIES)) {
            for (String speciesLocation : tag.getCompound(ABILITIES).getAllKeys()) {
                ResourceKey<DragonSpecies> speciesKey = ResourceKey.create(DragonSpecies.REGISTRY, ResourceLocation.parse(speciesLocation));

                if (provider.holder(speciesKey).isEmpty()) {
                    continue;
                }

                Map<ResourceKey<DragonAbility>, DragonAbilityInstance> abilities = new HashMap<>();
                CompoundTag storedAbilities = tag.getCompound(ABILITIES).getCompound(speciesLocation);

                for (String abilityLocation : storedAbilities.getAllKeys()) {
                    CompoundTag abilityTag = storedAbilities.getCompound(abilityLocation);
                    DragonAbilityInstance instance = DragonAbilityInstance.load(provider, abilityTag);

                    if (instance != null) {
                        abilities.put(instance.key(), instance);
                    }
                }

                this.abilities.put(speciesKey, abilities);
            }
        }

        if (tag.contains(HOTBARS)) {
            for (String speciesLocation : tag.getCompound(HOTBARS).getAllKeys()) {
                ResourceKey<DragonSpecies> speciesKey = ResourceKey.create(DragonSpecies.REGISTRY, ResourceLocation.parse(speciesLocation));

                if (provider.holder(speciesKey).isEmpty()) {
                    continue;
                }

                Map<Integer, ResourceKey<DragonAbility>> hotbar = new HashMap<>();
                CompoundTag storedHotbar = tag.getCompound(HOTBARS).getCompound(speciesLocation);

                for (String abilityLocation : storedHotbar.getAllKeys()) {
                    int slot = storedHotbar.getInt(abilityLocation);
                    ResourceKey<DragonAbility> key = ResourceKey.create(DragonAbility.REGISTRY, ResourceLocation.parse(abilityLocation));

                    if (provider.holder(key).isEmpty()) {
                        continue;
                    }

                    hotbar.put(slot, key);
                }

                this.hotbar.put(speciesKey, hotbar);
            }
        }

        currentMana = tag.getFloat(CURRENT_MANA);
        selectedAbilitySlot = tag.getInt(SELECTED_SLOT);
        renderAbilities = tag.getBoolean(RENDER_ABILITIES);

        if (tag.contains(CURRENT_SPECIES)) {
            currentSpecies = ResourceKey.create(DragonSpecies.REGISTRY, ResourceLocation.parse(tag.getString(CURRENT_SPECIES)));

            if (provider.holder(currentSpecies).isEmpty()) {
                DragonSurvival.LOGGER.warn("Failed to load current species for magic data! Did you remove a species from this save? Defaulting to cave dragon");
                currentSpecies = BuiltInDragonSpecies.CAVE;
            }
        }
    }

    private final String HOTBARS = "hotbars";
    private final String ABILITIES = "abilities";
    private final String CURRENT_SPECIES = "current_species";
    private final String CURRENT_MANA = "current_mana";
    private final String SELECTED_SLOT = "selected_slot";
    private final String RENDER_ABILITIES = "render_abilities";
}
