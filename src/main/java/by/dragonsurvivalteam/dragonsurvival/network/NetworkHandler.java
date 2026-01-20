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
import by.dragonsurvivalteam.dragonsurvival.network.dragon_soul_block.RequestDragonSoulData;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_soul_block.SyncDragonSoulAnimation;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_soul_block.SyncDragonSoulData;
import by.dragonsurvivalteam.dragonsurvival.network.dragon_soul_block.SyncDragonSoulLock;
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
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncCooldown;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncDragonSoulPlacement;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncKey;
import by.dragonsurvivalteam.dragonsurvival.network.syncing.SyncMana;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "3";

    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);

        registrar.playToServer(SortInventory.TYPE, SortInventory.STREAM_CODEC, SortInventory::handleServer);
        registrar.playToServer(SyncAltarCooldown.TYPE, SyncAltarCooldown.STREAM_CODEC, SyncAltarCooldown::handleServer);
        registrar.playToServer(RequestOpenDragonInventory.TYPE, RequestOpenDragonInventory.STREAM_CODEC, RequestOpenDragonInventory::handleServer);
        registrar.playToServer(RequestOpenVanillaInventory.TYPE, RequestOpenVanillaInventory.STREAM_CODEC, RequestOpenVanillaInventory::handleServer);
        registrar.playToServer(SyncLargeDragonDestruction.TYPE, SyncLargeDragonDestruction.STREAM_CODEC, SyncLargeDragonDestruction::handleServer);
        registrar.playToServer(RequestDragonSoulData.TYPE, RequestDragonSoulData.STREAM_CODEC, RequestDragonSoulData::handleServer);

        registrar.playBidirectional(OpenDragonEditor.TYPE, OpenDragonEditor.STREAM_CODEC, OpenDragonEditor::handleServer);
        registrar.playBidirectional(OpenDragonAltar.TYPE, OpenDragonAltar.STREAM_CODEC, OpenDragonAltar::handleServer);
        registrar.playBidirectional(FlightStatus.TYPE, FlightStatus.STREAM_CODEC, FlightStatus::handleServer);
        registrar.playBidirectional(SyncDragonMovement.TYPE, SyncDragonMovement.STREAM_CODEC, SyncDragonMovement::handleServer);
        registrar.playBidirectional(SyncComplete.TYPE, SyncComplete.STREAM_CODEC, SyncComplete::handleServer);
        registrar.playBidirectional(ToggleFlight.TYPE, ToggleFlight.STREAM_CODEC, ToggleFlight::handleServer);
        registrar.playBidirectional(SyncPitchAndYaw.TYPE, SyncPitchAndYaw.STREAM_CODEC, SyncPitchAndYaw::handleServer);

        registrar.playToServer(SyncMultiMining.TYPE, SyncMultiMining.STREAM_CODEC, SyncMultiMining::handleServer);

        registrar.playBidirectional(SyncResting.TYPE, SyncResting.STREAM_CODEC, SyncResting::handleClient);

        registrar.playBidirectional(SyncWingsSpread.TYPE, SyncWingsSpread.STREAM_CODEC, SyncWingsSpread::handleServer);
        registrar.playBidirectional(SyncDeltaMovement.TYPE, SyncDeltaMovement.STREAM_CODEC, SyncDeltaMovement::handleServer);
        registrar.playBidirectional(SyncSpinStatus.TYPE, SyncSpinStatus.STREAM_CODEC, SyncSpinStatus::handleServer);
        registrar.playBidirectional(SpinDurationAndCooldown.TYPE, SpinDurationAndCooldown.STREAM_CODEC, SpinDurationAndCooldown::handleServer);

        registrar.playToServer(SyncDragonClawMenuToggle.TYPE, SyncDragonClawMenuToggle.STREAM_CODEC, SyncDragonClawMenuToggle::handleServer);


        registrar.playToServer(SyncSlotAssignment.TYPE, SyncSlotAssignment.STREAM_CODEC, SyncSlotAssignment::handleServer);
        registrar.playToServer(AttemptManualUpgrade.TYPE, AttemptManualUpgrade.STREAM_CODEC, AttemptManualUpgrade::handleServer);
        registrar.playToServer(SyncBeginCast.TYPE, SyncBeginCast.STREAM_CODEC, SyncBeginCast::handleServer);
        registrar.playToServer(SyncSummonedEntitiesBehaviour.TYPE, SyncSummonedEntitiesBehaviour.STREAM_CODEC, SyncSummonedEntitiesBehaviour::handleServer);

        registrar.playBidirectional(SyncData.TYPE, SyncData.STREAM_CODEC, SyncData::handleCommon);
        registrar.playBidirectional(SyncStopCast.TYPE, SyncStopCast.STREAM_CODEC, SyncStopCast::handleServer);
        registrar.playBidirectional(SyncDisableAbility.TYPE, SyncDisableAbility.STREAM_CODEC, SyncDisableAbility::handleServer);

        // Emote packets
        registrar.playBidirectional(SyncEmote.TYPE, SyncEmote.STREAM_CODEC, SyncEmote::handleServer);
        registrar.playBidirectional(StopAllEmotes.TYPE, StopAllEmotes.STREAM_CODEC, StopAllEmotes::handleServer);

        registrar.playToServer(SyncDragonSoulPlacement.TYPE, SyncDragonSoulPlacement.STREAM_CODEC, SyncDragonSoulPlacement::handleServer);
        registrar.playToServer(SyncKey.TYPE, SyncKey.STREAM_CODEC, SyncKey::handleServer);

        registrar.playBidirectional(SyncDragonSoulAnimation.TYPE, SyncDragonSoulAnimation.STREAM_CODEC, SyncDragonSoulAnimation::handleServer);
        registrar.playBidirectional(SyncPlayerSkinPreset.TYPE, SyncPlayerSkinPreset.STREAM_CODEC, SyncPlayerSkinPreset::handleServer);
        registrar.playBidirectional(SyncDragonClawRender.TYPE, SyncDragonClawRender.STREAM_CODEC, SyncDragonClawRender::handleServer);
        registrar.playBidirectional(SyncDragonSkinSettings.TYPE, SyncDragonSkinSettings.STREAM_CODEC, SyncDragonSkinSettings::handleServer);
    }

    @SubscribeEvent
    public static void register(final RegisterClientPayloadHandlersEvent event) {
        // Generic packets
        event.register(SyncPlayerJump.TYPE, SyncPlayerJump::handleClient);
        event.register(SyncBrokenTool.TYPE, SyncBrokenTool::handleClient);
        event.register(SyncFlyingPlayerAbility.TYPE, SyncFlyingPlayerAbility::handleClient);
        event.register(SyncEffectModification.TYPE, SyncEffectModification::handleClient);
        event.register(SyncOxygenBonus.TYPE, SyncOxygenBonus::handleClient);
        event.register(SyncDragonPassengerID.TYPE, SyncDragonPassengerID::handleClient);

        // Status
        event.register(SyncGrowthState.TYPE, SyncGrowthState::handleClient);
        event.register(SyncGrowth.TYPE, SyncGrowth::handleClient);
        event.register(SyncDesiredGrowth.TYPE, SyncDesiredGrowth::handleClient);
        event.register(SyncEnderDragonMark.TYPE, SyncEnderDragonMark::handleClient);
        event.register(SyncAltarState.TYPE, SyncAltarState::handleClient);

        // Flight
        event.register(SyncWingIcon.TYPE, SyncWingIcon::handleClient);

        // Render settings
        event.register(SyncDragonClawsMenu.TYPE, SyncDragonClawsMenu::handleClient);

        // Ability packets
        event.register(SyncMagicData.TYPE, SyncMagicData::handleClient);
        event.register(SyncHunterStacksRemoval.TYPE, SyncHunterStacksRemoval::handleClient);
        event.register(StartTickingSound.TYPE, StartTickingSound::handleClient);
        event.register(StopTickingSound.TYPE, StopTickingSound::handleClient);
        event.register(StopAbilityAnimation.TYPE, StopAbilityAnimation::handleClient);
        event.register(SyncAbilityAnimation.TYPE, SyncAbilityAnimation::handleClient);
        event.register(SyncModifierWithDuration.TYPE, SyncModifierWithDuration::handleClient);
        event.register(SyncAbilityLevel.TYPE, SyncAbilityLevel::handleClient);
        event.register(SyncHarvestBonus.TYPE, SyncHarvestBonus::handleClient);
        event.register(SyncAddPenaltySupply.TYPE, SyncAddPenaltySupply::handleClient);
        event.register(SyncRemovePenaltySupply.TYPE, SyncRemovePenaltySupply::handleClient);
        event.register(SyncPenaltySupplyAmount.TYPE, SyncPenaltySupplyAmount::handleClient);
        event.register(SyncPenaltySupply.TYPE, SyncPenaltySupply::handleClient);
        event.register(SyncDamageModification.TYPE, SyncDamageModification::handleClient);
        event.register(SyncSwimDataEntry.TYPE, SyncSwimDataEntry::handleClient);
        event.register(SyncSummonedEntity.TYPE,  SyncSummonedEntity::handleClient);
        event.register(SyncGlowInstance.TYPE, SyncGlowInstance::handleClient);
        event.register(SyncBlockVision.TYPE, SyncBlockVision::handleClient);

        // Potion sync
        event.register(SyncVisualEffectRemoval.TYPE, SyncVisualEffectRemoval::handleClient);
        event.register(SyncVisualEffectAdded.TYPE, SyncVisualEffectAdded::handleClient);

        event.register(SyncDragonSoulLock.TYPE, SyncDragonSoulLock::handleClient);
        event.register(RequestClientData.TYPE, RequestClientData::handleClient);
        event.register(SyncDragonSoulData.TYPE, SyncDragonSoulData::handleClient);
        event.register(SyncParticleTrail.TYPE, SyncParticleTrail::handleClient);
        event.register(SyncBreathParticles.TYPE,  SyncBreathParticles::handleClient);
        event.register(SyncMana.TYPE, SyncMana::handleClient);
        event.register(SyncCooldown.TYPE, SyncCooldown::handleClient);

        event.register(OpenDragonEditor.TYPE, OpenDragonEditor::handleClient);
        event.register(OpenDragonAltar.TYPE, OpenDragonAltar::handleClient);
        event.register(FlightStatus.TYPE, FlightStatus::handleClient);
        event.register(SyncDragonMovement.TYPE, SyncDragonMovement::handleClient);
        event.register(SyncComplete.TYPE, SyncComplete::handleClient);
        event.register(ToggleFlight.TYPE, ToggleFlight::handleClient);
        event.register(SyncPitchAndYaw.TYPE, SyncPitchAndYaw::handleClient);
        event.register(SyncResting.TYPE, SyncResting::handleClient);

        event.register(SyncWingsSpread.TYPE, SyncWingsSpread::handleClient);
        event.register(SyncDeltaMovement.TYPE, SyncDeltaMovement::handleClient);
        event.register(SyncSpinStatus.TYPE, SyncSpinStatus::handleClient);
        event.register(SpinDurationAndCooldown.TYPE, SpinDurationAndCooldown::handleClient);

        event.register(SyncData.TYPE, SyncData::handleCommon);
        event.register(SyncStopCast.TYPE, SyncStopCast::handleClient);
        event.register(SyncDisableAbility.TYPE, SyncDisableAbility::handleClient);

        event.register(SyncEmote.TYPE, SyncEmote::handleClient);
        event.register(StopAllEmotes.TYPE, StopAllEmotes::handleClient);

        event.register(SyncDragonSoulAnimation.TYPE, SyncDragonSoulAnimation::handleClient);
        event.register(SyncPlayerSkinPreset.TYPE, SyncPlayerSkinPreset::handleClient);
        event.register(SyncDragonClawRender.TYPE, SyncDragonClawRender::handleClient);
        event.register(SyncDragonSkinSettings.TYPE, SyncDragonSkinSettings::handleClient);
    }
}