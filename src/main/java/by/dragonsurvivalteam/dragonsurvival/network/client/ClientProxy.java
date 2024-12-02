package by.dragonsurvivalteam.dragonsurvival.network.client;

import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.DragonAltarScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.screens.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.handlers.ClientFlightHandler;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRenderer;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.DragonEditorRegistry;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SavedSkinPresets;
import by.dragonsurvivalteam.dragonsurvival.client.skin_editor_system.objects.SkinPreset;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SpinStatus;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncDeltaMovement;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingStatus;
import by.dragonsurvivalteam.dragonsurvival.network.magic.*;
import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncBreathParticles;
import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncParticleTrail;
import by.dragonsurvivalteam.dragonsurvival.network.player.*;
import by.dragonsurvivalteam.dragonsurvival.network.status.*;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import by.dragonsurvivalteam.dragonsurvival.registry.attachments.*;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/** To avoid loading client classes on the server side */
public class ClientProxy {
    public static void handleSyncDragonClawRender(final SyncDragonClawRender.Data message) {
        Player player = Minecraft.getInstance().player;

        if (player != null) {
            Entity entity = player.level().getEntity(message.playerId());

            if (entity instanceof Player) {
                ClawInventoryData.getData(player).shouldRenderClaws = message.state();
            }
        }
    }

    public static void handleSyncDragonClawsMenu(final SyncDragonClawsMenu.Data message, HolderLookup.Provider provider) {
        Player player = Minecraft.getInstance().player;

        if (player != null) {
            Entity entity = player.level().getEntity(message.playerId());

            if (entity instanceof Player) {
                ClawInventoryData data = ClawInventoryData.getData(player);
                data.setMenuOpen(message.state());
                data.deserializeNBT(provider, message.clawInventory());
            }
        }
    }

    public static void sendClientData() {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer == null) {
            // This check is needed since in single player the server thread will go in here (from 'deserializeNBT')
            return;
        }

        DragonStateHandler data = DragonStateProvider.getData(localPlayer);

        if (!data.isDragon()) {
            // TODO :: this method is also called when the fake players update their size
            //  The setSize method just probably always have an entity (and the nbt deserialization just sets the field and then uses join level event or sth. to actually update)
            return;
        }

        PacketDistributor.sendToServer(new SyncDragonClawRender.Data(localPlayer.getId(), ClientDragonRenderer.renderDragonClaws));
        PacketDistributor.sendToServer(new SyncDragonSkinSettings(localPlayer.getId(), ClientDragonRenderer.renderCustomSkin));
        SavedSkinPresets savedCustomizations = DragonEditorRegistry.getSavedCustomizations(localPlayer.registryAccess());

        if (savedCustomizations != null) {
            Holder<DragonType> type = data.getType();

            //noinspection DataFlowIssue -> level and key are present
            int selectedSaveSlot = savedCustomizations.current.getOrDefault(type.getKey(), new HashMap<>()).getOrDefault(data.getStage().getKey().location().toString(), 0);
            SkinPreset preset = savedCustomizations.skinPresets.getOrDefault(type.getKey(), new HashMap<>()).getOrDefault(selectedSaveSlot, new SkinPreset());
            PacketDistributor.sendToServer(new SyncPlayerSkinPreset.Data(localPlayer.getId(), preset.serializeNBT(localPlayer.registryAccess())));
        } else {
            PacketDistributor.sendToServer(new SyncPlayerSkinPreset.Data(localPlayer.getId(), new SkinPreset().serializeNBT(localPlayer.registryAccess())));
        }
    }

    // For replying during the configuration stage
    public static void sendClientData(final IPayloadContext context) {
        Player sender = context.player();

        context.reply(new SyncDragonClawRender.Data(sender.getId(), ClientDragonRenderer.renderDragonClaws));
        context.reply(new SyncDragonSkinSettings(sender.getId(), ClientDragonRenderer.renderCustomSkin));

        DragonStateHandler data = DragonStateProvider.getData(sender);
        SavedSkinPresets savedCustomizations = DragonEditorRegistry.getSavedCustomizations(sender.registryAccess());

        if (savedCustomizations != null) {
            Holder<DragonType> type = data.getType();

            if (type != null) {
                //noinspection DataFlowIssue -> level and key are present
                int selectedSaveSlot = savedCustomizations.current.getOrDefault(type.getKey(), new HashMap<>()).getOrDefault(data.getStage().getKey().location().toString(), 0);
                SkinPreset preset = savedCustomizations.skinPresets.getOrDefault(type.getKey(), new HashMap<>()).getOrDefault(selectedSaveSlot, new SkinPreset());
                context.reply(new SyncPlayerSkinPreset.Data(sender.getId(), preset.serializeNBT(sender.registryAccess())));
            }
        } else {
            context.reply(new SyncPlayerSkinPreset.Data(sender.getId(), new SkinPreset().serializeNBT(sender.registryAccess())));
        }
    }

    public static void handleSyncPlayerSkinPreset(final SyncPlayerSkinPreset.Data message, HolderLookup.Provider provider) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getOptional(player).ifPresent(handler -> {
                    SkinPreset preset = new SkinPreset();
                    preset.deserializeNBT(provider, message.preset());
                    handler.getSkinData().skinPreset = preset;

                    if (handler.isDragon()) {
                        handler.getSkinData().compileSkin(handler.getStage());
                    }
                });
            }
        }
    }

    public static void handleSyncDeltaMovement(final SyncDeltaMovement.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            // Local player already has the correct values of themselves
            if (entity instanceof Player player && player != localPlayer) {
                player.setDeltaMovement(message.speedX(), message.speedY(), message.speedZ());
            }
        }
    }

    public static void handleOpenDragonAltar() {
        Minecraft.getInstance().setScreen(new DragonAltarScreen());
    }

    public static void handleOpenDragonEditorPacket() {
        Minecraft.getInstance().setScreen(new DragonEditorScreen(Minecraft.getInstance().screen));
    }

    public static void handleSyncFlyingStatus(final SyncFlyingStatus.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getOptional(player).ifPresent(handler -> handler.setWingsSpread(message.state()));
            }
        }
    }

    public static void handleSyncSpinStatus(final SpinStatus packet) {
        Player localPlayer = Objects.requireNonNull(Minecraft.getInstance().player);

        if (localPlayer.level().getEntity(packet.playerId()) instanceof Player player) {
            SpinData spin = SpinData.getData(player);
            spin.hasSpin = packet.hasSpin();
            spin.cooldown = packet.cooldown();
            spin.duration = packet.duration();
            ClientFlightHandler.lastSync = player.tickCount;
        }
    }

    // FIXME: I'm pretty sure this is done entirely differently
    /*public static void handleSyncAbilityCasting(final SyncAbilityCasting.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getOptional(player).ifPresent(handler -> {
                    ActiveDragonAbility ability = handler.getMagicData().getAbilityFromSlot(message.abilitySlot());
                    ability.loadNBT(message.nbt());
                    handler.getMagicData().isCasting = message.isCasting();

                    if (message.isCasting()) {
                        ability.onKeyPressed(player, () -> {
                            if (player.getId() == localPlayer.getId()) {
                                ClientCastingHandler.hasCast = true;
                                ClientCastingHandler.status = ClientCastingHandler.CastingStatus.Stop;
                            }
                        }, message.castStartTime(), message.clientTime());
                    } else {
                        ability.onKeyReleased(player);
                    }
                });
            }
        }
    }*/

    public static void handleSyncMagicCap(final SyncMagicCap.Data message, HolderLookup.Provider provider) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                MagicData magicData = MagicData.getData(player);
                magicData.deserializeNBT(provider, message.nbt());
            }
        }
    }

    public static void handleSyncMagicstats(final SyncMagicStats.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerid());

            if (entity instanceof Player player) {
                MagicData magicData = MagicData.getData(player);
                magicData.setCurrentMana(message.currentMana());
                magicData.setSelectedAbilitySlot(message.selectedSlot());
                magicData.setRenderAbilities(message.renderHotbar());
            }
        }
    }

    public static void handleSyncPotionAddedEffect(final SyncVisualEffectAdded.Data message) {
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

    public static void handleSyncPotionRemovedEffect(final SyncVisualEffectRemoved.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());
            Optional<Holder.Reference<MobEffect>> mobEffect = BuiltInRegistries.MOB_EFFECT.getHolder(message.effectId());

            if (mobEffect.isPresent()) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.removeEffect(mobEffect.get());
                }
            }
        }
    }

    public static void handleSyncDragonMovement(final SyncDragonMovement.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if(DragonStateProvider.isDragon(entity)) {
                MovementData data = MovementData.getData(entity);
                data.setFirstPerson(message.isFirstPerson());
                data.setBite(message.bite());
                data.setFreeLook(message.isFreeLook());
                data.setDesiredMoveVec(new Vec2(message.desiredMoveVecX(), message.desiredMoveVecY()));
            }
        }
    }

    public static void handleSyncPassengerID(final SyncDragonPassengerID.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getOptional(player).ifPresent(handler -> handler.setPassengerId(message.passengerId()));
            }
        }
    }

    // TODO: Don't think this is needed anymore (we need something new for penalty data sync maybe?
    /*public static void handleSyncDragonTypeData(final SyncDragonType.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getOptional(player).ifPresent(handler -> {
                    if (handler.getType() != null) {
                        handler.getType().readNBT(message.nbt());
                    }
                });
            }
        }
    }*/

    public static void handleSyncGrowthState(final SyncGrowthState.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            DragonStateProvider.getOptional(localPlayer).ifPresent(handler -> handler.isGrowing = message.growing());
        }
    }

    public static void handleSyncDestructionEnabled(final SyncDestructionEnabled.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getOptional(player).ifPresent(handler -> {
                    handler.setDestructionEnabled(message.destructionEnabled());
                });
            }
        }
    }

    public static void handleDiggingStatus(final SyncDiggingStatus.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());
            MovementData.getData(entity).dig = message.status();
        }
    }

    public static void handlePlayerJumpSync(final SyncPlayerJump.Data message) {
        Entity entity = Minecraft.getInstance().level.getEntity(message.playerId());

        if (entity instanceof Player player) {
            DragonEntity.dragonsJumpingTicks.put(player.getId(), message.ticks());
        }
    }

    public static void handleRefreshDragons(final RefreshDragon.Data message) {
        Player localPlayer = Minecraft.getInstance().player;
        Entity entity = localPlayer.level().getEntity(message.playerId());

        if (entity instanceof Player player) {
            DragonEntity dragon = DSEntities.DRAGON.get().create(localPlayer.level());
            dragon.playerId = player.getId();
            ClientDragonRenderer.playerDragonHashMap.computeIfAbsent(player.getId(), integer -> new AtomicReference<>(dragon)).getAndSet(dragon);
        }
    }

    public static void handleSyncAltarCooldown(final SyncAltarCooldown.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                AltarData data = AltarData.getData(player);
                data.altarCooldown = message.cooldown();
                data.hasUsedAltar = true;
                data.isInAltar = false;
            }
        }
    }

    // TODO: Don't think this is needed anymore
    /*public static void handleSyncMagicSourceStatus(final SyncMagicSourceStatus.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                DragonStateProvider.getOptional(player).ifPresent(handler -> {
                    handler.getMagicData().onMagicSource = message.state();
                    handler.getMagicData().magicSourceTimer = message.timer();
                });
            }
        }
    }*/

    public static void handleSyncTreasureRestStatus(final SyncTreasureRestStatus.Data message) {
        Player localPlayer = Minecraft.getInstance().player;

        if (localPlayer != null) {
            Entity entity = localPlayer.level().getEntity(message.playerId());

            if (entity instanceof Player player) {
                TreasureRestData data = TreasureRestData.getData(player);
                if (message.state() != data.isResting) {
                    data.restingTicks = 0;
                    data.sleepingTicks = 0;
                }

                data.isResting = message.state();
            }
        }
    }

    public static void handleSyncParticleTrail(SyncParticleTrail message) {
            // Creates a trail of particles between the entity and target(s)
            Vec3 source = new Vec3(message.source().x(), message.source().y(), message.source().z());
            Vec3 target = new Vec3(message.target().x(), message.target().y(), message.target().z());
            // Scale steps based off of the distance between the source and target
            int steps = Math.max(20, (int) Math.ceil(source.distanceTo(target) * 2.5));
            float stepSize = 1.f / steps;
            Vec3 distV = new Vec3(source.x - target.x, source.y - target.y, source.z - target.z);
            for (int i = 0; i < steps; i++) {
                // the current entity coordinate + ((the distance between it and the target) * (the fraction of the total))
                Vec3 step = target.add(distV.scale(stepSize * i));
                Minecraft.getInstance().level.addParticle(message.trailParticle(), step.x(), step.y(), step.z(), 0.0, 0.0, 0.0);
            }
    }

    public static void handleSyncBreathParticles(SyncBreathParticles message) {
        Vec3 normalizedVelocity = new Vec3(message.velocity().x, message.velocity().y, message.velocity().z).normalize();
        for (int i = 0; i < message.numParticles(); i++) {
            RandomSource rand = Minecraft.getInstance().level.getRandom();
            double xSpeed = message.velocity().x + message.spread() / 2 * (rand.nextFloat() * 2 - 1) * Math.sqrt(normalizedVelocity.x * normalizedVelocity.x);
            double ySpeed = message.velocity().y + message.spread() / 2 * (rand.nextFloat() * 2 - 1) * Math.sqrt(normalizedVelocity.y * normalizedVelocity.y);
            double zSpeed = message.velocity().z + message.spread() / 2 * (rand.nextFloat() * 2 - 1) * Math.sqrt(normalizedVelocity.z * normalizedVelocity.z);
            Minecraft.getInstance().level.addParticle(message.secondaryParticle(), message.position().x, message.position().y, message.position().z, xSpeed, ySpeed, zSpeed);
        }

        for (int i = 0; i <  message.numParticles() / 2; i++) {
            RandomSource rand = Minecraft.getInstance().level.getRandom();
            double xSpeed = message.velocity().x + message.spread() * (rand.nextFloat() * 2 - 1) * Math.sqrt(normalizedVelocity.x * normalizedVelocity.x);
            double ySpeed = message.velocity().y + message.spread() * (rand.nextFloat() * 2 - 1) * Math.sqrt(normalizedVelocity.y * normalizedVelocity.y);
            double zSpeed = message.velocity().z + message.spread() * (rand.nextFloat() * 2 - 1) * Math.sqrt(normalizedVelocity.z * normalizedVelocity.z);
            Minecraft.getInstance().level.addParticle(message.mainParticle(), message.position().x, message.position().y, message.position().z, xSpeed, ySpeed, zSpeed);
        }
    }
}
