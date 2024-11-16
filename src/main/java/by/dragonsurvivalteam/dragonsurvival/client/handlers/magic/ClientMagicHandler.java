package by.dragonsurvivalteam.dragonsurvival.client.handlers.magic;

import by.dragonsurvivalteam.dragonsurvival.client.particles.CaveDragon.SmallFireParticleData;
import by.dragonsurvivalteam.dragonsurvival.client.particles.ForestDragon.SmallPoisonParticleData;
import by.dragonsurvivalteam.dragonsurvival.client.particles.SeaDragon.LargeLightningParticleData;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigOption;
import by.dragonsurvivalteam.dragonsurvival.config.obj.ConfigSide;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ActiveDragonAbility;
import by.dragonsurvivalteam.dragonsurvival.magic.common.active.ChargeCastAbility;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import by.dragonsurvivalteam.dragonsurvival.registry.DragonEffects;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.Objects;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientMagicHandler{
	@ConfigOption(side = ConfigSide.CLIENT, category = "rendering", key = "particles_on_dragons", comment = "Particles (from the dragon type effects) will be rendered on dragons if this is enabled")
	public static Boolean particlesOnDragons = false;

	@SubscribeEvent
	public static void onFovEvent(ComputeFovModifierEvent event){
		Player player = event.getPlayer();

		DragonStateProvider.getCap(player).ifPresent(cap -> {
			if(Arrays.stream(cap.getEmoteData().currentEmotes).anyMatch(Objects::nonNull) && DragonUtils.isDragon(player)){
				event.setNewFovModifier(1f);
				return;
			}

			ActiveDragonAbility ability = cap.getMagicData().getCurrentlyCasting();

			if(ability instanceof ChargeCastAbility chargeCastAbility){
				if(chargeCastAbility.getCastTime() > 0){
					double perc = Math.min(chargeCastAbility.getCastTime() / (float)chargeCastAbility.getSkillCastingTime(), 1) / 4;
					double c4 = 2 * Math.PI / 3;

					if(perc != 0 && perc != 1){
						perc = Math.pow(2, -10 * perc) * Math.sin((perc * 10 - 0.75) * c4) + 1;
					}

					float newFov = (float)Mth.clamp(perc, 1.0F, 1.0F);
					event.setNewFovModifier(newFov);
				}
			}
		});
	}

	@SubscribeEvent
	public static void livingTick(final LivingEvent.LivingTickEvent event){
		LivingEntity entity = event.getEntity();

		if (!entity.level.isClientSide) {
			return;
		}

		if (!particlesOnDragons && DragonUtils.isDragon(entity)) {
			return;
		}

		if (entity.hasEffect(DragonEffects.BURN)) {
			ParticleOptions data = new SmallFireParticleData(37F, false);
			for (int i = 0; i < 4; i++) {
				renderEffectParticle(entity, data);
			}
		}

		if (entity.hasEffect(DragonEffects.DRAIN)) {
			ParticleOptions data = new SmallPoisonParticleData(37F, false);
			for (int i = 0; i < 4; i++) {
				renderEffectParticle(entity, data);
			}
		}

		if (entity.hasEffect(DragonEffects.CHARGED)) {
			ParticleOptions data = new LargeLightningParticleData(37F, false);
			for (int i = 0; i < 4; i++) {
				renderEffectParticle(entity, data);
			}
		}
	}

	public static void renderEffectParticle(final LivingEntity entity, final ParticleOptions particle) {
		Player localPlayer = ClientProxy.getLocalPlayer();

		if (localPlayer != null) {
			double d0 = (double) entity.getRandom().nextFloat() * entity.getBbWidth();
			double d1 = (double) entity.getRandom().nextFloat() * entity.getBbHeight();
			double d2 = (double) entity.getRandom().nextFloat() * entity.getBbWidth();
			double x = entity.getX() + d0 - entity.getBbWidth() / 2;
			double y = entity.getY() + d1;
			double z = entity.getZ() + d2 - entity.getBbWidth() / 2;

			localPlayer.getLevel().addParticle(particle, x, y, z, 0, 0, 0);
		}
	}

	@SubscribeEvent(receiveCanceled = true)
	public static void removeLavaAndWaterFog(ViewportEvent.RenderFog event){
		LocalPlayer player = Minecraft.getInstance().player;
		DragonStateProvider.getCap(player).ifPresent(cap -> {
			if(!cap.isDragon()){
				return;
			}

			if(Objects.equals(cap.getType(), DragonTypes.CAVE) && event.getCamera().getFluidInCamera() == FogType.LAVA){
				if(player.hasEffect(DragonEffects.LAVA_VISION)){
					event.setNearPlaneDistance(-32.0F);
					event.setFarPlaneDistance(128.0F);
					event.setCanceled(true);
				}
			}else if(Objects.equals(cap.getType(), DragonTypes.SEA) && event.getCamera().getFluidInCamera() == FogType.WATER){
				if(player.hasEffect(DragonEffects.WATER_VISION)){
					event.setNearPlaneDistance(-32.0F);
					event.setFarPlaneDistance(128.0F);
					event.setCanceled(true);
				}
			}
		});
	}
}