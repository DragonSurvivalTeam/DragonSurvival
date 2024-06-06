package by.dragonsurvivalteam.dragonsurvival.network.client;

import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.DragonAltarHandler;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.magic.ClientCastingHandler;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.WingObtainmentController;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.network.RequestClientData;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlightSpeed;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingStatus;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.network.magic.*;
import by.dragonsurvivalteam.dragonsurvival.network.player.*;
import by.dragonsurvivalteam.dragonsurvival.network.status.*;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;


/** To avoid loading client classes on the server side */
public class ClientProxy {
    public static @Nullable Player getLocalPlayer() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return Minecraft.getInstance().player;
        }

        return null;
    }

    public static void handleSyncDragonClawRender(final SyncDragonClawRender.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player) {
                DragonStateProvider.getCap(entity).ifPresent(handler -> handler.getClawToolData().shouldRenderClaws = message.state());
            }
        }
    }

    public static void handleSyncDragonClawsMenu(final SyncDragonClawsMenu.Data message, HolderLookup.Provider provider) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player) {
                DragonStateProvider.getCap(entity).ifPresent(handler -> {
                    handler.getClawToolData().setMenuOpen(message.state());
                    handler.getClawToolData().deserializeNBT(provider, message.clawInventory());
                });
            }
        }
    }

    public static void handleSyncDragonSkinSettings(final SyncDragonSkinSettings.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Level world = localPlayer.level();
            Entity entity = world.getEntity(message.playerId());

            if (entity instanceof Player) {
                DragonStateProvider.getCap(entity).ifPresent(dragonStateHandler -> {
                    dragonStateHandler.getSkinData().renderNewborn = message.newborn();
                    dragonStateHandler.getSkinData().renderYoung = message.young();
                    dragonStateHandler.getSkinData().renderAdult = message.adult();
                });
            }
        }
    }

    public static void requestClientData(final DragonStateHandler handler) {
        if (handler == DragonStateProvider.getOrGenerateHandler(Minecraft.getInstance().player)) {
            ClientEvents.sendClientData(new RequestClientData.Data(handler.getType(), handler.getBody(), handler.getLevel()));
        }
    }

    public static void handleRequestClientData(final RequestClientData.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            ClientEvents.sendClientData(message);
        }
    }

    public static void handleSyncPlayerSkinPreset(final SyncPlayerSkinPreset.Data message, HolderLookup.Provider provider) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    SkinPreset preset = new SkinPreset();
                    preset.deserializeNBT(provider, message.preset());
                    handler.getSkinData().skinPreset = preset;
                    handler.getSkinData().compileSkin();
                });
            }
        }
    }

    public static void handleSyncFlightSpeed(final SyncFlightSpeed.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            // Local player already has the correct values of themselves
            if (entity instanceof Player player && player != localPlayer) {
                player.setDeltaMovement(message.flightSpeedX(), message.flightSpeedY(), message.flightSpeedZ());
            }
        }
    }

    public static void handleOpenDragonAltar() {
        DragonAltarHandler.openAltar();
    }

    public static void handleOpenDragonEditorPacket() {
        Minecraft.getInstance().setScreen(new DragonEditorScreen(Minecraft.getInstance().screen));
    }

    public static void handleSyncFlyingStatus(final SyncFlyingStatus.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> handler.setWingsSpread(message.state()));
            }
        }
    }

    public static void handleSyncSpinStatus(final SyncSpinStatus.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
                    dragonStateHandler.getMovementData().spinAttack = message.spinAttack();
                    dragonStateHandler.getMovementData().spinCooldown = message.spinCooldown();
                    dragonStateHandler.getMovementData().spinLearned = message.spinLearned();
                });

                ClientFlightHandler.lastSync = player.tickCount;
            }
        }
    }

    public static void handleSyncAbilityCasting(final SyncAbilityCasting.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    ActiveDragonAbility ability = handler.getMagicData().getAbilityFromSlot(message.abilitySlot());
                    ability.loadNBT(message.nbt());
                    handler.getMagicData().isCasting = message.isCasting();

                    if (message.isCasting()) {
                        ability.onKeyPressed(player, () -> {
                            if (player.getId() == localPlayer.getId()) {
                                ClientCastingHandler.hasCast = true;
                                ClientCastingHandler.status = ClientCastingHandler.StatusStop;
                            }
                        }, message.castStartTime(), message.clientTime());
                    } else {
                        ability.onKeyReleased(player);
                    }
                });
            }
        }
    }

    public static void handleSyncMagicCap(final SyncMagicCap.Data message, HolderLookup.Provider provider) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> handler.getMagicData().deserializeNBT(provider, message.nbt()));
            }
        }
    }

    public static void handleSyncMagicstats(final SyncMagicStats.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerid());

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    handler.getMagicData().setCurrentMana(message.currentMana());
                    handler.getMagicData().setSelectedAbilitySlot(message.selectedSlot());
                    handler.getMagicData().setRenderAbilities(message.renderHotbar());
                });
            }
        }
    }

    public static void handleSyncPotionAddedEffect(final SyncPotionAddedEffect.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.entityId());
            Optional<Holder.Reference<MobEffect>> mobEffect = BuiltInRegistries.MOB_EFFECT.getHolder(message.effectId());

            if (mobEffect.isPresent()) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addEffect(new MobEffectInstance(mobEffect.get(), message.duration(), message.amplifier()));
                }
            }
        }
    }

    public static void handleSyncPotionRemovedEffect(final SyncPotionRemovedEffect.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());
            Optional<Holder.Reference<MobEffect>> mobEffect = BuiltInRegistries.MOB_EFFECT.getHolder(message.effectId());

            if (mobEffect != null) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.removeEffect(mobEffect.get());
                }
            }
        }
    }

    public static void handlePacketSyncCapabilityMovement(final SyncDragonMovement.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> handler.setMovementData(message.bodyYaw(), message.headYaw(), message.headPitch(), message.bite()));
            }
        }
    }

    public static void handleSyncChatEvent(final SyncChatEvent.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            WingObtainmentController.clientMessageRecieved(message);
        }
    }

    public static void handleSyncDragonTypeData(final SyncDragonType.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    if (handler.getType() != null) {
                        handler.getType().readNBT(message.nbt());
                    }
                });
            }
        }
    }

    public static void handleSyncGrowthState(final SyncGrowthState.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            DragonStateProvider.getCap(localPlayer).ifPresent(handler -> handler.growing = message.growing());
        }
    }

    public static void handleSynchronizeDragonCap(final SyncDragonHandler.Data message) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;

        // TODO :: use string uuid?
        if (localPlayer != null) {
            if (ClientDragonRender.dragonArmor != null) {
                ClientDragonRender.dragonArmor.playerId = localPlayer.getId();
            }

            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    handler.setType(message.dragonType(), player);
                    handler.setBody(message.dragonBody(), player);
                    handler.setIsHiding(message.hiding());
                    handler.setHasFlight(message.hasWings());
                    handler.setSize(message.size(), player);
                    handler.setPassengerId(message.passengerId());
                });

                // Refresh instances
                if (player != localPlayer) {
                    DragonEntity dragon = DSEntities.DRAGON.get().create(localPlayer.level());
                    dragon.playerId = player.getId();
                    ClientDragonRender.playerDragonHashMap.computeIfAbsent(player.getId(), integer -> new AtomicReference<>(dragon)).getAndSet(dragon);
                }
            }
        }
    }

    public static void handleSyncSize(final SyncSize.Data message) {
        Entity entity = Minecraft.getInstance().level.getEntity(message.playerId());

        if (entity instanceof Player player) {
            DragonStateProvider.getCap(player).ifPresent(handler -> handler.setSize(message.size(), player));
            player.refreshDimensions();
        }
    }

    public static void handleDiggingStatus(final SyncDiggingStatus.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> handler.getMovementData().dig = message.status());
            }
        }
    }

    public static void handlePlayerJumpSync(final SyncPlayerJump.Data message) {
        Entity entity = Minecraft.getInstance().level.getEntity(message.playerId());

        if (entity instanceof Player player) {
            ClientEvents.dragonsJumpingTicks.put(player.getId(), message.ticks());
        }
    }

    public static void handleRefreshDragons(final RefreshDragon.Data message) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;

        ClientDragonRender.dragonArmor = DSEntities.DRAGON_ARMOR.get().create(localPlayer.level());

        if (ClientDragonRender.dragonArmor != null) {
            ClientDragonRender.dragonArmor.playerId = localPlayer.getId();
        }

        Entity entity = localPlayer.level().getEntity(message.playerId());

        if (entity instanceof Player player) {
            DragonEntity dragon = DSEntities.DRAGON.get().create(localPlayer.level());
            dragon.playerId = player.getId();
            ClientDragonRender.playerDragonHashMap.computeIfAbsent(player.getId(), integer -> new AtomicReference<>(dragon)).getAndSet(dragon);
        }
    }

    public static void handleSyncAltarCooldown(final SyncAltarCooldown.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateHandler dragonStateHandler = DragonStateProvider.getOrGenerateHandler(player);
                dragonStateHandler.altarCooldown = message.cooldown();
            }
        }
    }

    public static void handleSyncMagicSourceStatus(final SyncMagicSourceStatus.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    handler.getMagicData().onMagicSource = message.state();
                    handler.getMagicData().magicSourceTimer = message.timer();
                });
            }
        }
    }

    public static void handleSyncTreasureRestStatus(final SyncTreasureRestStatus.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    if (message.state() != handler.treasureResting) {
                        handler.treasureRestTimer = 0;
                        handler.treasureSleepTimer = 0;
                    }

                    handler.treasureResting = message.state();
                });
            }
        }
    }
}
