package by.dragonsurvivalteam.dragonsurvival.compat.do_a_barrel_roll;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;

public final class DoABarrelRollMixinCanceller implements MixinCanceller {
    private static final String FABRIC_CAMERA_MIXIN = "nl.enjarai.doabarrelroll.mixin.client.roll.CameraMixin";

    @Override
    public boolean shouldCancel(final List<String> targetClassNames, final String mixinClassName) {
        return FABRIC_CAMERA_MIXIN.equals(mixinClassName);
    }
}
