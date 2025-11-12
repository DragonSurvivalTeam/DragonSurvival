package by.dragonsurvivalteam.dragonsurvival.network;

import by.dragonsurvivalteam.dragonsurvival.network.animation.StopAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.network.animation.SyncAbilityAnimation;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncBrokenTool;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawMenuToggle;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawRender;
import by.dragonsurvivalteam.dragonsurvival.network.claw.SyncDragonClawsMenu;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonAltar;
import by.dragonsurvivalteam.dragonsurvival.network.container.OpenDragonEditor;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenDragonInventory;
import by.dragonsurvivalteam.dragonsurvival.network.container.RequestOpenVanillaInventory;
import by.dragonsurvivalteam.dragonsurvival.network.container.SortInventory;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncDragonSkinSettings;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_editor.SyncPlayerSkinPreset;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.StopAllEmotes;
import by.dragonsurvivalteam.dragonsurvival.network.emotes.SyncEmote;
import by.dragonsurvivalteam.dragonsurvival.network.flight.FlightStatus;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SpinDurationAndCooldown;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncDeltaMovement;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncFlyingPlayerAbility;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncSpinStatus;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncWingIcon;
import by.dragonsurvivalteam.dragonsurvival.network.flight.SyncWingsSpread;
import by.dragonsurvivalteam.dragonsurvival.network.flight.ToggleFlight;
import by.dragonsurvivalteam.dragonsurvival.network.magic.AttemptManualUpgrade;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncAbilityLevel;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncAddPenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncBeginCast;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncBlockVision;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncDamageModification;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncData;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncDisableAbility;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncEffectModification;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncGlowInstance;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncHarvestBonus;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncHunterStacksRemoval;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncMagicData;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncModifierWithDuration;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncOxygenBonus;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncPenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncPenaltySupplyAmount;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncRemovePenaltySupply;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSlotAssignment;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncStopCast;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSummonedEntitiesBehaviour;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSummonedEntity;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncSwimDataEntry;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncVisualEffectAdded;
import by.dragonsurvivalteam.dragonsurvival.network.magic.SyncVisualEffectRemoval;
import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncBreathParticles;
import by.dragonsurvivalteam.dragonsurvival.network.particle.SyncParticleTrail;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDesiredGrowth;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDragonMovement;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncDragonPassengerID;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncGrowth;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncGrowthState;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncLargeDragonDestruction;
import by.dragonsurvivalteam.dragonsurvival.network.player.SyncPitchAndYaw;
import by.dragonsurvivalteam.dragonsurvival.network.sound.StartTickingSound;
import by.dragonsurvivalteam.dragonsurvival.network.sound.StopTickingSound;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncAltarCooldown;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncAltarState;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncEnderDragonMark;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncMultiMining;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncPlayerJump;
import by.dragonsurvivalteam.dragonsurvival.network.status.SyncResting;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncComplete;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "3";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        // Generic packets
        registrar.playToClient(SyncPlayerJump.TYPE, SyncPlayerJump.STREAM_CODEC, SyncPlayerJump::handleClient);
        registrar.playToClient(SyncBrokenTool.TYPE, SyncBrokenTool.STREAM_CODEC, SyncBrokenTool::handleClient);
        registrar.playToClient(SyncFlyingPlayerAbility.TYPE, SyncFlyingPlayerAbility.STREAM_CODEC, SyncFlyingPlayerAbility::handleClient);
        registrar.playToClient(SyncEffectModification.TYPE, SyncEffectModification.STREAM_CODEC, SyncEffectModification::handleClient);
        registrar.playToClient(SyncOxygenBonus.TYPE, SyncOxygenBonus.STREAM_CODEC, SyncOxygenBonus::handleClient);
        registrar.playToClient(SyncDragonPassengerID.TYPE, SyncDragonPassengerID.STREAM_CODEC, SyncDragonPassengerID::handleClient);

        registrar.playToServer(SortInventory.TYPE, SortInventory.STREAM_CODEC, SortInventory::handleServer);
        registrar.playToServer(SyncAltarCooldown.TYPE, SyncAltarCooldown.STREAM_CODEC, SyncAltarCooldown::handleServer);
        registrar.playToServer(RequestOpenDragonInventory.TYPE, RequestOpenDragonInventory.STREAM_CODEC, RequestOpenDragonInventory::handleServer);
        registrar.playToServer(RequestOpenVanillaInventory.TYPE, RequestOpenVanillaInventory.STREAM_CODEC, RequestOpenVanillaInventory::handleServer);
        registrar.playToServer(SyncLargeDragonDestruction.TYPE, SyncLargeDragonDestruction.STREAM_CODEC, SyncLargeDragonDestruction::handleServer);

        registrar.playBidirectional(OpenDragonEditor.TYPE, OpenDragonEditor.STREAM_CODEC, new DirectionalPayloadHandler<>(OpenDragonEditor::handleClient, OpenDragonEditor::handleServer));
        registrar.playBidirectional(OpenDragonAltar.TYPE, OpenDragonAltar.STREAM_CODEC, new DirectionalPayloadHandler<>(OpenDragonAltar::handleClient, OpenDragonAltar::handleServer));
        registrar.playBidirectional(FlightStatus.TYPE, FlightStatus.STREAM_CODEC, new DirectionalPayloadHandler<>(FlightStatus::handleClient, FlightStatus::handleServer));
        registrar.playBidirectional(SyncDragonMovement.TYPE, SyncDragonMovement.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonMovement::handleClient, SyncDragonMovement::handleServer));
        registrar.playBidirectional(SyncComplete.TYPE, SyncComplete.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncComplete::handleClient, SyncComplete::handleServer));
        registrar.playBidirectional(ToggleFlight.TYPE, ToggleFlight.STREAM_CODEC, new DirectionalPayloadHandler<>(ToggleFlight::handleClient, ToggleFlight::handleServer));
        registrar.playBidirectional(SyncPitchAndYaw.TYPE, SyncPitchAndYaw.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncPitchAndYaw::handleClient, SyncPitchAndYaw::handleServer));

        // Status
        registrar.playToClient(SyncGrowthState.TYPE, SyncGrowthState.STREAM_CODEC, SyncGrowthState::handleClient);
        registrar.playToClient(SyncGrowth.TYPE, SyncGrowth.STREAM_CODEC, SyncGrowth::handleClient);
        registrar.playToClient(SyncDesiredGrowth.TYPE, SyncDesiredGrowth.STREAM_CODEC, SyncDesiredGrowth::handleClient);
        registrar.playToClient(SyncEnderDragonMark.TYPE, SyncEnderDragonMark.STREAM_CODEC, SyncEnderDragonMark::handleClient);
        registrar.playToClient(SyncAltarState.TYPE, SyncAltarState.STREAM_CODEC, SyncAltarState::handleClient);

        registrar.playToServer(SyncMultiMining.TYPE, SyncMultiMining.STREAM_CODEC, SyncMultiMining::handleServer);

        registrar.playBidirectional(SyncResting.TYPE, SyncResting.STREAM_CODEC, SyncResting::handleClient);

        // Flight
        registrar.playToClient(SyncWingIcon.TYPE, SyncWingIcon.STREAM_CODEC, SyncWingIcon::handleClient);

        registrar.playBidirectional(SyncWingsSpread.TYPE, SyncWingsSpread.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncWingsSpread::handleClient, SyncWingsSpread::handleServer));
        registrar.playBidirectional(SyncDeltaMovement.TYPE, SyncDeltaMovement.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDeltaMovement::handleClient, SyncDeltaMovement::handleServer));
        registrar.playBidirectional(SyncSpinStatus.TYPE, SyncSpinStatus.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncSpinStatus::handleClient, SyncSpinStatus::handleServer));
        registrar.playBidirectional(SpinDurationAndCooldown.TYPE, SpinDurationAndCooldown.STREAM_CODEC, new DirectionalPayloadHandler<>(SpinDurationAndCooldown::handleClient, SpinDurationAndCooldown::handleServer));

        // Render settings
        registrar.playToClient(SyncDragonClawsMenu.TYPE, SyncDragonClawsMenu.STREAM_CODEC, SyncDragonClawsMenu::handleClient);

        registrar.playToServer(SyncDragonClawMenuToggle.TYPE, SyncDragonClawMenuToggle.STREAM_CODEC, SyncDragonClawMenuToggle::handleServer);

        // Ability packets
        registrar.playToClient(SyncMagicData.TYPE, SyncMagicData.STREAM_CODEC, SyncMagicData::handleClient);
        registrar.playToClient(SyncHunterStacksRemoval.TYPE, SyncHunterStacksRemoval.STREAM_CODEC, SyncHunterStacksRemoval::handleClient);
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
        registrar.playToClient(SyncGlowInstance.TYPE, SyncGlowInstance.STREAM_CODEC, SyncGlowInstance::handleClient);
        registrar.playToClient(SyncBlockVision.TYPE, SyncBlockVision.STREAM_CODEC, SyncBlockVision::handleClient);

        registrar.playToServer(SyncSlotAssignment.TYPE, SyncSlotAssignment.STREAM_CODEC, SyncSlotAssignment::handleServer);
        registrar.playToServer(AttemptManualUpgrade.TYPE, AttemptManualUpgrade.STREAM_CODEC, AttemptManualUpgrade::handleServer);
        registrar.playToServer(SyncBeginCast.TYPE, SyncBeginCast.STREAM_CODEC, SyncBeginCast::handleServer);
        registrar.playToServer(SyncSummonedEntitiesBehaviour.TYPE, SyncSummonedEntitiesBehaviour.STREAM_CODEC, SyncSummonedEntitiesBehaviour::handleServer);

        registrar.playBidirectional(SyncStopCast.TYPE, SyncStopCast.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncStopCast::handleClient, SyncStopCast::handleServer));
        registrar.playBidirectional(SyncDisableAbility.TYPE, SyncDisableAbility.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDisableAbility::handleClient, SyncDisableAbility::handleServer));

        // Potion sync
        registrar.playToClient(SyncVisualEffectRemoval.TYPE, SyncVisualEffectRemoval.STREAM_CODEC, SyncVisualEffectRemoval::handleClient);
        registrar.playToClient(SyncVisualEffectAdded.TYPE, SyncVisualEffectAdded.STREAM_CODEC, SyncVisualEffectAdded::handleClient);

        // Emote packets
        registrar.playBidirectional(SyncEmote.TYPE, SyncEmote.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncEmote::handleClient, SyncEmote::handleServer));
        registrar.playBidirectional(StopAllEmotes.TYPE, StopAllEmotes.STREAM_CODEC, new DirectionalPayloadHandler<>(StopAllEmotes::handleClient, StopAllEmotes::handleServer));

        // Client data
        registrar.playToClient(RequestClientData.TYPE, RequestClientData.STREAM_CODEC, RequestClientData::handleClient);
        registrar.playToClient(SyncParticleTrail.TYPE, SyncParticleTrail.STREAM_CODEC, SyncParticleTrail::handleClient);
        registrar.playToClient(SyncBreathParticles.TYPE, SyncBreathParticles.STREAM_CODEC, SyncBreathParticles::handleClient);

        registrar.playBidirectional(SyncPlayerSkinPreset.TYPE, SyncPlayerSkinPreset.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncPlayerSkinPreset::handleClient, SyncPlayerSkinPreset::handleServer));
        registrar.playBidirectional(SyncDragonClawRender.TYPE, SyncDragonClawRender.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonClawRender::handleClient, SyncDragonClawRender::handleServer));
        registrar.playBidirectional(SyncDragonSkinSettings.TYPE, SyncDragonSkinSettings.STREAM_CODEC, new DirectionalPayloadHandler<>(SyncDragonSkinSettings::handleClient, SyncDragonSkinSettings::handleServer));
    }
}