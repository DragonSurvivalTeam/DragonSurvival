package by.dragonsurvivalteam.dragonsurvival.network.client;

import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientEvents;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.DragonAltarHandler;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.magic.ClientCastingHandler;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.common.handlers.WingObtainmentController;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.network.ISidedMessage;
import by.dragonsurvivalteam.dragonsurvival.network.RequestClientData;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncDeltaMovement;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlightSpeed;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingStatus;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.network.magic.*;
import by.dragonsurvivalteam.dragonsurvival.network.player.*;
import by.dragonsurvivalteam.dragonsurvival.network.status.*;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicReference;

/** To avoid loading client classes on the server side */
public class ClientProxy {
    public static @Nullable Player getLocalPlayer() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            return Minecraft.getInstance().player;
        }

        return null;
    }

    public static void handleSyncDragonClawRender(final SyncDragonClawRender message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player) {
                DragonStateProvider.getCap(entity).ifPresent(handler -> handler.getClawToolData().shouldRenderClaws = message.state);
            }
        }
    }

    public static void handleSyncDragonClawsMenu(final SyncDragonClawsMenu message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player) {
                DragonStateProvider.getCap(entity).ifPresent(handler -> {
                    handler.getClawToolData().setMenuOpen(message.state);
                    handler.getClawToolData().setClawsInventory(message.clawInventory);
                });
            }
        }
    }

    public static void handleSyncDragonSkinSettings(final SyncDragonSkinSettings message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Level world = localPlayer.level;
            Entity entity = world.getEntity(message.playerId);

            if (entity instanceof Player) {
                DragonStateProvider.getCap(entity).ifPresent(dragonStateHandler -> {
                    dragonStateHandler.getSkinData().renderNewborn = message.newborn;
                    dragonStateHandler.getSkinData().renderYoung = message.young;
                    dragonStateHandler.getSkinData().renderAdult = message.adult;
                });
            }
        }
    }

    public static void requestClientData(final DragonStateHandler handler) {
        if (handler == DragonUtils.getHandler(Minecraft.getInstance().player)) {
            ClientEvents.sendClientData(new RequestClientData(handler.getType(), handler.getBody(), handler.getLevel()));
        }
    }

    public static void handleRequestClientData(final RequestClientData message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            ClientEvents.sendClientData(message);
        }
    }

    public static <T extends ISidedMessage<T>> void handleRunClient(final T message, final NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            Player localPlayer = Minecraft.getInstance().player;

            if (localPlayer != null) {
                Entity entity = localPlayer.level.getEntity(message.playerId);

                if (entity instanceof Player player) {
                    message.runClient(message, context, player);
                }
            }
        });
    }

    public static void handleSyncPlayerSkinPreset(final SyncPlayerSkinPreset message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    handler.getSkinData().skinPreset = message.preset;
                    handler.getSkinData().compileSkin();
                });
            }
        }
    }

    public static void handleSyncDeltaMovement(final SyncDeltaMovement message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            // Local player already has the correct values of themselves
            if (entity instanceof Player player && player != localPlayer) {
                player.setDeltaMovement(message.speedX, message.speedY, message.speedZ);
            }
        }
    }

    public static void handleSyncFlightSpeed(final SyncFlightSpeed message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            // Local player already has the correct values of themselves
            if (entity instanceof Player player && player != localPlayer) {
                player.setDeltaMovement(message.flightSpeed);
            }
        }
    }

    public static void handleOpenDragonAltar() {
        DragonAltarHandler.openAltar();
    }

    public static void handleOpenDragonEditorPacket() {
        Minecraft.getInstance().setScreen(new DragonEditorScreen(Minecraft.getInstance().screen));
    }

    public static void handleSyncFlyingStatus(final SyncFlyingStatus message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> handler.setWingsSpread(message.state));
            }
        }
    }

    public static void handleSyncSpinStatus(final SyncSpinStatus message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
                    dragonStateHandler.getMovementData().spinAttack = message.spinAttack;
                    dragonStateHandler.getMovementData().spinCooldown = message.spinCooldown;
                    dragonStateHandler.getMovementData().spinLearned = message.spinLearned;
                });

                ClientFlightHandler.lastSync = player.tickCount;
            }
        }
    }

    public static void handleSyncAbilityCasting(final SyncAbilityCasting message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    ActiveDragonAbility ability = handler.getMagicData().getAbilityFromSlot(message.abilitySlot);
                    ability.loadNBT(message.nbt);
                    handler.getMagicData().isCasting = message.isCasting;

                    if (message.isCasting) {
                        ability.onKeyPressed(player, () -> {
                            if (player.getId() == localPlayer.getId()) {
                                ClientCastingHandler.hasCast = true;
                                ClientCastingHandler.status = ClientCastingHandler.StatusStop;
                            }
                        }, message.castStartTime, message.clientTime);
                    } else {
                        ability.onKeyReleased(player);
                    }
                });
            }
        }
    }

    public static void handleSyncMagicCap(final SyncMagicCap message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> handler.getMagicData().readNBT(message.nbt));
            }
        }
    }

    public static void handleSyncMagicstats(final SyncMagicStats message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerid);

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    handler.getMagicData().setCurrentMana(message.currentMana);
                    handler.getMagicData().setSelectedAbilitySlot(message.selectedSlot);
                    handler.getMagicData().setRenderAbilities(message.renderHotbar);
                });
            }
        }
    }

    public static void handleSyncPotionAddedEffect(final SyncPotionAddedEffect message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.entityId);
            MobEffect mobEffect = MobEffect.byId(message.effectId);

            if (mobEffect != null) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addEffect(new MobEffectInstance(mobEffect, message.duration, message.amplifier));
                }
            }
        }
    }

    public static void handleSyncPotionRemovedEffect(final SyncPotionRemovedEffect message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);
            MobEffect mobEffect = MobEffect.byId(message.effectId);

            if (mobEffect != null) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.removeEffect(mobEffect);
                }
            }
        }
    }

    public static void handlePacketSyncCapabilityMovement(final SyncDragonMovement message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    handler.setBite(message.bite);
                    handler.setFirstPerson(message.isFirstPerson);
                    handler.setFreeLook(message.isFreeLook);
                    handler.setDesiredMoveVec(new Vec2(message.desiredMoveVecX, message.desiredMoveVecY));
                });
            }
        }
    }

    public static void handleSyncChatEvent(final SyncChatEvent message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            WingObtainmentController.clientMessageRecieved(message);
        }
    }

    public static void handleSyncDragonTypeData(final SyncDragonTypeData message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    if (handler.getType() != null) {
                        handler.getType().readNBT(message.nbt);
                    }
                });
            }
        }
    }

    public static void handleSyncGrowthState(final SyncGrowthState message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            DragonStateProvider.getCap(localPlayer).ifPresent(handler -> handler.growing = message.growing);
        }
    }

    public static void handleSynchronizeDragonCap(final SynchronizeDragonCap message) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;

        // TODO :: use string uuid?
        if (localPlayer != null) {
            if (ClientDragonRender.dragonArmor != null) {
                ClientDragonRender.dragonArmor.playerId = localPlayer.getId();
            }

            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    handler.setType(message.dragonType, player);
                    handler.setBody(message.dragonBody, player);
                    handler.setIsHiding(message.hiding);
                    handler.setHasFlight(message.hasWings);
                    handler.setSize(message.size, player);
                    handler.setPassengerId(message.passengerId);
                });

                // Refresh instances
                if (player != localPlayer) {
                    DragonEntity dragon = DSEntities.DRAGON.create(localPlayer.level);
                    dragon.playerId = player.getId();
                    ClientDragonRender.playerDragonHashMap.computeIfAbsent(player.getId(), integer -> new AtomicReference<>(dragon)).getAndSet(dragon);
                }
            }
        }
    }

    public static void handleSyncSize(final SyncSize message) {
        Entity entity = Minecraft.getInstance().level.getEntity(message.playerId);

        if (entity instanceof Player player) {
            DragonStateProvider.getCap(player).ifPresent(handler -> handler.setSize(message.size, player));
            player.refreshDimensions();
        }
    }

    public static void handleDiggingStatus(final DiggingStatus message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> handler.getMovementData().dig = message.status);
            }
        }
    }

    public static void handlePlayerJumpSync(final PlayerJumpSync message) {
        Entity entity = Minecraft.getInstance().level.getEntity(message.playerId);

        if (entity instanceof Player player) {
            ClientEvents.dragonsJumpingTicks.put(player.getId(), message.ticks);
        }
    }

    public static void handleRefreshDragons(final RefreshDragons message) {
        LocalPlayer localPlayer = Minecraft.getInstance().player;

        ClientDragonRender.dragonArmor = DSEntities.DRAGON_ARMOR.create(localPlayer.level);

        if (ClientDragonRender.dragonArmor != null) {
            ClientDragonRender.dragonArmor.playerId = localPlayer.getId();
        }

        Entity entity = localPlayer.level.getEntity(message.playerId);

        if (entity instanceof Player player) {
            DragonEntity dragon = DSEntities.DRAGON.create(localPlayer.level);
            dragon.playerId = player.getId();
            ClientDragonRender.playerDragonHashMap.computeIfAbsent(player.getId(), integer -> new AtomicReference<>(dragon)).getAndSet(dragon);
        }
    }

    public static void handleSyncAltarCooldown(final SyncAltarCooldown message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player player) {
                DragonStateHandler dragonStateHandler = DragonUtils.getHandler(player);
                dragonStateHandler.altarCooldown = message.cooldown;
            }
        }
    }

    public static void handleSyncMagicSourceStatus(final SyncMagicSourceStatus message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    handler.getMagicData().onMagicSource = message.state;
                    handler.getMagicData().magicSourceTimer = message.timer;
                });
            }
        }
    }

    public static void handleSyncTreasureRestStatus(final SyncTreasureRestStatus message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level.getEntity(message.playerId);

            if (entity instanceof Player player) {
                DragonStateProvider.getCap(player).ifPresent(handler -> {
                    if (message.state != handler.treasureResting) {
                        handler.treasureRestTimer = 0;
                        handler.treasureSleepTimer = 0;
                    }

                    handler.treasureResting = message.state;
                });
            }
        }
    }
}
