package by.dragonsurvivalteam.dragonsurvival.mixins.bettercombat;


// FIXME :: Better Combat
@SuppressWarnings("UnstableApiUsage")
//Mixin(value = AnimationApplier.class, remap = false)
public abstract class AnimationApplierMixin {
//    @Inject(method = "updatePart", at = @At("TAIL"))
//    public void dragonSurvival$offsetAttackAnimation(final String partName, final ModelPart part, final CallbackInfo callback) {
//        if (BetterCombat.isAttacking(BetterCombat.CURRENT_PLAYER)) {
//            if (partName.equals("rightArm") || partName.equals("leftArm")) {
//                DragonStateHandler handler = DragonStateProvider.getData(BetterCombat.CURRENT_PLAYER);
//
//                if (handler.isDragon()) {
//                    // 'y' has a default value of 2 - can't find anything to offset it without an extra field
//                    // Seems to be dependent on the eye height, but there is no proper reference value to work with it
//                    // (Since 'y' will always be 2, we don't have access to the position that scales with the eye height or sth. like that)
//                    part.setPos(part.x, (float) (part.y + handler.body().value().betterCombatWeaponOffset()), part.z);
//                }
//            }
//        }
//    }
}