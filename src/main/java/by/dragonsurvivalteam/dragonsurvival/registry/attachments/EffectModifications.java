package by.dragonsurvivalteam.dragonsurvival.registry.attachments;

import by.dragonsurvivalteam.dragonsurvival.common.codecs.EffectModification;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber
public class EffectModifications extends Storage<EffectModification.Instance> {
    public MobEffectInstance modifyEffect(final MobEffectInstance instance) {
        int newDuration = calculateDuration(instance);
        int newAmplifier = calculateAmplifier(instance);

        if (newDuration == instance.getDuration() && newAmplifier == instance.getAmplifier()) {
            return instance;
        }

        return new MobEffectInstance(instance.getEffect(), newDuration, newAmplifier, instance.isAmbient(), instance.isVisible(), instance.showIcon());
    }

    public int calculateDuration(final MobEffectInstance effect) {
        int duration = effect.getDuration();

        for (EffectModification.Instance instance : all()) {
            if (instance.baseData().effects().contains( effect.getEffect())) {
                duration = instance.calculateDuration(duration);
            }
        }

        return duration;
    }

    public int calculateAmplifier(final MobEffectInstance effect) {
        int amplifier = effect.getAmplifier();

        for (EffectModification.Instance instance : all()) {
            if (instance.baseData().effects().contains( effect.getEffect())) {
                amplifier = instance.calculateAmplifier(amplifier);
            }
        }

        return amplifier;
    }

    @SubscribeEvent
    public static void shouldApplyEffect(final MobEffectEvent.Applicable event) {
        event.getEntity().getExistingData(DSDataAttachments.EFFECT_MODIFICATIONS).ifPresent(data -> {
            int duration = data.calculateDuration(event.getEffectInstance());

            if (duration <= 0 && duration != MobEffectInstance.INFINITE_DURATION) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
                return;
            }

            if (data.calculateAmplifier(event.getEffectInstance()) < 0) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
        });
    }

    @SubscribeEvent
    public static void tickData(final EntityTickEvent.Post event) {
        // Effects are only applied to living entities
        if (event.getEntity() instanceof LivingEntity) {
            event.getEntity().getExistingData(DSDataAttachments.EFFECT_MODIFICATIONS).ifPresent(data -> {
                data.tick(event.getEntity());

                if (data.isEmpty()) {
                    event.getEntity().removeData(DSDataAttachments.EFFECT_MODIFICATIONS);
                }
            });
        }
    }

    @Override
    protected Tag save(@NotNull final HolderLookup.Provider provider, final EffectModification.Instance entry) {
        return entry.save(provider);
    }

    @Override
    protected EffectModification.Instance load(@NotNull final HolderLookup.Provider provider, final CompoundTag tag) {
        return EffectModification.Instance.load(provider, tag);
    }

    @Override
    public AttachmentType<?> type() {
        return DSDataAttachments.EFFECT_MODIFICATIONS.get();
    }
}
