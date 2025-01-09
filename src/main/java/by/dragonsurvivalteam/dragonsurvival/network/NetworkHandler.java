package by.dragonsurvivalteam.dragonsurvival.network;

import by.dragonsurvivalteam.dragonsurvival.network.animation.StopAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.network.animation.SyncAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncBrokenTool;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenuToggle;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenDragonEditor;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenDragonInventory;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenInventory;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.StopAllEmotes;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.SyncEmote;
import by.dragonsurvivalteam.dragonsurvival.network.flight.*;
import by.dragonsurvivalteam.dragonsurvival.network.magic.*;
import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncBreathParticles;
import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncParticleTrail;
import by.dragonsurvivalteam.dragonsurvival.network.player.*;
import by.dragonsurvivalteam.dragonsurvival.network.sound.StartTickingSound;
import by.dragonsurvivalteam.dragonsurvival.network.sound.StopTickingSound;
import by.dragonsurvivalteam.dragonsurvival.network.status.*;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "3";
    private static final Logger log = LoggerFactory.getLogger(NetworkHandler.class);

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // Generic packets
        registrar.playToClient(SyncPlayerJump.Data.TYPE, SyncPlayerJump.Data.STREAM_CODEC, SyncPlayerJump::handleClient);
        registrar.playToClient(RefreshDragon.Data.TYPE, RefreshDragon.Data.STREAM_CODEC, RefreshDragon::handleClient);
        registrar.playToClient(OpenDragonAltar.TYPE, OpenDragonAltar.STREAM_CODEC, OpenDragonAltar::handleClient);
        registrar.playToClient(SyncBrokenTool.Data.TYPE, SyncBrokenTool.Data.STREAM_CODEC, SyncBrokenTool::handleClient);
        registrar.playToClient(SyncFlyingPlayerAbility.TYPE, SyncFlyingPlayerAbility.STREAM_CODEC, SyncFlyingPlayerAbility::handleClient);
        registrar.playToClient(SyncEffectModification.TYPE, SyncEffectModification.STREAM_CODEC, SyncEffectModification::handleClient);

        registrar.playToServer(SyncAltarCooldown.TYPE, SyncAltarCooldown.STREAM_CODEC, SyncAltarCooldown::handleServer);
        registrar.playToServer(RequestOpenDragonInventory.TYPE, RequestOpenDragonInventory.STREAM_CODEC, RequestOpenDragonInventory::handleServer);
        registrar.playToServer(RequestOpenInventory.Data.TYPE, RequestOpenInventory.Data.STREAM_CODEC, RequestOpenInventory::handleServer);
        registrar.playToServer(SyncLargeDragonDestruction.TYPE, SyncLargeDragonDestruction.STREAM_CODEC, SyncLargeDragonDestruction::handleServer);

        registrar.playBidirectional(FlightStatus.TYPE, FlightStatus.STREAM_CODEC, new DirectionalPayloadHandler<>(FlightStatus::handleClient, FlightStatus::handleServer));
        registrar.playBidirectional(SyncDragonMovement.Data.TYPE, SyncDragonMovement.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonMovement::handleClient, SyncDragonMovement::handleServer));
        registrar.playBidirectional(SyncComplete.Data.TYPE, SyncComplete.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncComplete::handleClient, SyncComplete::handleServer));
        registrar.playBidirectional(SyncDragonPassengerID.Data.TYPE, SyncDragonPassengerID.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonPassengerID::handleClient, SyncDragonPassengerID::handleServer));

        // Status
        registrar.playToClient(SyncGrowthState.Data.TYPE, SyncGrowthState.Data.STREAM_CODEC, SyncGrowthState::handleClient);
        registrar.playToClient(SyncSize.TYPE, SyncSize.STREAM_CODEC, SyncSize::handleClient);
        registrar.playToClient(SyncDesiredSize.TYPE, SyncDesiredSize.STREAM_CODEC, SyncDesiredSize::handleClient);
        registrar.playToClient(RequestOpenDragonEditor.Data.TYPE, RequestOpenDragonEditor.Data.STREAM_CODEC, RequestOpenDragonEditor::handleClient);
        registrar.playToClient(SyncEnderDragonMark.TYPE, SyncEnderDragonMark.STREAM_CODEC, SyncEnderDragonMark::handleClient);

        registrar.playToServer(SyncMultiMining.TYPE, SyncMultiMining.STREAM_CODEC, SyncMultiMining::handleServer);

        registrar.playBidirectional(SyncResting.TYPE, SyncResting.STREAM_CODEC, SyncResting::handleClient);

        // Flight
        registrar.playBidirectional(SyncWingsSpread.Data.TYPE, SyncWingsSpread.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncWingsSpread::handleClient, SyncWingsSpread::handleServer));
        registrar.playBidirectional(SyncDeltaMovement.Data.TYPE, SyncDeltaMovement.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDeltaMovement::handleClient, SyncDeltaMovement::handleServer));
        registrar.playBidirectional(SpinStatus.TYPE, SpinStatus.STREAM_CODEC, new DirectionalPayloadHandler<>(SpinStatus::handleClient, SpinStatus::handleServer));
        registrar.playBidirectional(SpinDurationAndCooldown.TYPE, SpinDurationAndCooldown.STREAM_CODEC, new DirectionalPayloadHandler<>(SpinDurationAndCooldown::handleClient, SpinDurationAndCooldown::handleServer));

        // Render settings
        registrar.playToClient(SyncDragonClawsMenu.Data.TYPE, SyncDragonClawsMenu.Data.STREAM_CODEC, SyncDragonClawsMenu::handleClient);

        registrar.playBidirectional(SyncDragonClawsMenuToggle.Data.TYPE, SyncDragonClawsMenuToggle.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonClawsMenuToggle::handleClient, SyncDragonClawsMenuToggle::handleServer));

        // Ability packets
        registrar.playToClient(SyncMana.TYPE, SyncMana.STREAM_CODEC, SyncMana::handleClient);
        registrar.playToClient(SyncMagicData.TYPE, SyncMagicData.STREAM_CODEC, SyncMagicData::handleClient);
        registrar.playToClient(SyncHunterStacksRemoval.TYPE, SyncHunterStacksRemoval.STREAM_CODEC, SyncHunterStacksRemoval::handleClient);
        registrar.playToClient(SyncCooldownState.TYPE, SyncCooldownState.STREAM_CODEC, SyncCooldownState::handleClient);
        registrar.playToClient(StartTickingSound.TYPE, StartTickingSound.STREAM_CODEC, StartTickingSound::handleClient);
        registrar.playToClient(StopTickingSound.TYPE, StopTickingSound.STREAM_CODEC, StopTickingSound::handleClient);
        registrar.playToClient(StopAbilityAnimation.TYPE, StopAbilityAnimation.STREAM_CODEC, StopAbilityAnimation::handleClient);
        registrar.playToClient(SyncAbilityAnimation.TYPE, SyncAbilityAnimation.STREAM_CODEC, SyncAbilityAnimation::handleClient);
        registrar.playToClient(SyncModifierWithDuration.TYPE, SyncModifierWithDuration.STREAM_CODEC, SyncModifierWithDuration::handleClient);
        registrar.playToClient(SyncAbilityLevel.TYPE, SyncAbilityLevel.STREAM_CODEC, SyncAbilityLevel::handleClient);
        registrar.playToClient(SyncHarvestBonus.TYPE, SyncHarvestBonus.STREAM_CODEC, SyncHarvestBonus::handleClient);
        registrar.playToClient(SyncAddPenaltySupply.TYPE, SyncAddPenaltySupply.STREAM_CODEC, SyncAddPenaltySupply::handleClient);
        registrar.playToClient(SyncRemovePenaltySupply.TYPE, SyncRemovePenaltySupply.STREAM_CODEC, SyncRemovePenaltySupply::handleClient);
        registrar.playToClient(SyncPenaltySupplyAmount.TYPE, SyncPenaltySupplyAmount.STREAM_CODEC, SyncPenaltySupplyAmount::handleClient);
        registrar.playToClient(SyncPenaltySupply.TYPE, SyncPenaltySupply.STREAM_CODEC, SyncPenaltySupply::handleClient);
        registrar.playToClient(SyncDamageModification.TYPE, SyncDamageModification.STREAM_CODEC, SyncDamageModification::handleClient);
        registrar.playToClient(SyncSwimDataEntry.TYPE, SyncSwimDataEntry.STREAM_CODEC, SyncSwimDataEntry::handleClient);
        registrar.playToClient(SyncData.TYPE, SyncData.STREAM_CODEC, SyncData::handleClient);
        registrar.playToClient(SyncSummonedEntity.TYPE, SyncSummonedEntity.STREAM_CODEC, SyncSummonedEntity::handleClient);

        registrar.playToServer(SyncSlotAssignment.TYPE, SyncSlotAssignment.STREAM_CODEC, SyncSlotAssignment::handleServer);
        registrar.playToServer(AttemptManualUpgrade.TYPE, AttemptManualUpgrade.STREAM_CODEC, AttemptManualUpgrade::handleServer);
        registrar.playToServer(SyncBeginCast.TYPE, SyncBeginCast.STREAM_CODEC, SyncBeginCast::handleServer);
        registrar.playToServer(SyncSummonedEntitiesBehaviour.TYPE, SyncSummonedEntitiesBehaviour.STREAM_CODEC, SyncSummonedEntitiesBehaviour::handleServer);

        registrar.playBidirectional(SyncStopCast.TYPE, SyncStopCast.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncStopCast::handleClient, SyncStopCast::handleServer));
        registrar.playBidirectional(SyncDisableAbility.TYPE, SyncDisableAbility.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDisableAbility::handleClient, SyncDisableAbility::handleServer));

        // Potion sync
        registrar.playToClient(SyncVisualEffectRemoved.Data.TYPE, SyncVisualEffectRemoved.Data.STREAM_CODEC, SyncVisualEffectRemoved::handleClient);
        registrar.playToClient(SyncVisualEffectAdded.Data.TYPE, SyncVisualEffectAdded.Data.STREAM_CODEC, SyncVisualEffectAdded::handleClient);

        // Emote packets
        registrar.playBidirectional(SyncEmote.TYPE, SyncEmote.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncEmote::handleClient, SyncEmote::handleServer));
        registrar.playBidirectional(StopAllEmotes.TYPE, StopAllEmotes.STREAM_CODEC, new DirectionalPayloadHandler<>(StopAllEmotes::handleClient, StopAllEmotes::handleServer));

        // Client data
        registrar.playToClient(RequestClientData.TYPE, RequestClientData.STREAM_CODEC, RequestClientData::handleClient);
        registrar.playToClient(SyncParticleTrail.TYPE, SyncParticleTrail.STREAM_CODEC, SyncParticleTrail::handleClient);
        registrar.playToClient(SyncBreathParticles.TYPE, SyncBreathParticles.STREAM_CODEC, SyncBreathParticles::handleClient);

        registrar.playBidirectional(SyncPlayerSkinPreset.TYPE, SyncPlayerSkinPreset.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncPlayerSkinPreset::handleClient, SyncPlayerSkinPreset::handleServer));
        registrar.playBidirectional(SyncDragonClawRender.Data.TYPE, SyncDragonClawRender.Data.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonClawRender::handleClient, SyncDragonClawRender::handleServer));
        registrar.playBidirectional(SyncDragonSkinSettings.TYPE, SyncDragonSkinSettings.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonSkinSettings::handleClient, SyncDragonSkinSettings::handleServer));
    }
}