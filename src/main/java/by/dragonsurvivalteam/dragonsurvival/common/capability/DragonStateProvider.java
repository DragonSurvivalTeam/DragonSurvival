package by.dragonsurvivalteam.dragonsurvival.common.capability;

import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayer;
import by.dragonsurvivalteam.dragonsurvival.network.client.ClientProxy;
import com.ibm.icu.impl.Pair;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DragonStateProvider implements ICapabilitySerializable<CompoundTag> {
	public static final Map<String, LazyOptional<DragonStateHandler>> SERVER_CACHE = new HashMap<>();
	public static final Map<String, LazyOptional<DragonStateHandler>> CLIENT_CACHE = new HashMap<>();

	private final DragonStateHandler handlerObject = new DragonStateHandler();
	private final LazyOptional<DragonStateHandler> instance = LazyOptional.of(() -> handlerObject);

	private static Pair<Boolean, LazyOptional<DragonStateHandler>> getFakePlayer(Entity entity) {
		if (entity instanceof FakeClientPlayer fakeClientPlayer) {
			if (fakeClientPlayer.handler != null) {
				return Pair.of(true, LazyOptional.of(() -> fakeClientPlayer.handler));
			}
		}

		return Pair.of(false, LazyOptional.empty());
	}

	public static @Nullable DragonStateHandler getHandler(final Entity entity) {
		if (!(entity instanceof Player)) {
			return null;
		}

		LazyOptional<DragonStateHandler> capability = getCap(entity);

		if (capability.isPresent()) {
			Optional<DragonStateHandler> optional = capability.resolve();
			return optional.orElse(null);
		}

		return null;
	}

	public static @NotNull DragonStateHandler getUnsafeHandler(final Player player) throws IllegalStateException {
		return getCap(player).orElseThrow(() -> new IllegalStateException("Dragon State was not present"));
	}

	public static LazyOptional<DragonStateHandler> getCap(final Entity entity) {
		if (entity == null) {
			return LazyOptional.empty();
		} else {
			if (entity.level().isClientSide()) {
				Pair<Boolean, LazyOptional<DragonStateHandler>> fakeState = getFakePlayer(entity);

				if (fakeState.first) {
					return fakeState.second;
				}
			}

			if (!(entity instanceof Player) || /* e.g. Create Deployer */ entity instanceof FakePlayer) {
				return LazyOptional.empty();
			}

			Map<String, LazyOptional<DragonStateHandler>> sidedCache = entity.level().isClientSide() ? CLIENT_CACHE : SERVER_CACHE;
			LazyOptional<DragonStateHandler> cachedCapability = sidedCache.get(entity.getStringUUID());

			if (cachedCapability != null) {
				return cachedCapability;
			}

			LazyOptional<DragonStateHandler> capability = entity.getCapability(Capabilities.DRAGON_CAPABILITY);

			if (capability.isPresent()) {
				sidedCache.put(entity.getStringUUID(), capability);
				capability.addListener(ignored -> sidedCache.remove(entity.getStringUUID()));
			}

			return capability;
		}
	}

	public static LazyOptional<? extends EntityStateHandler> getEntityCap(Entity entity){
		if (entity instanceof Player) {
			return getCap(entity);
		}

		return entity.getCapability(Capabilities.ENTITY_CAPABILITY);
	}

	public static void clearCache(final Player player) {
		if (player.level().isClientSide()) {
			if (player == ClientProxy.getLocalPlayer()) {
				CLIENT_CACHE.clear();
			} else {
				CLIENT_CACHE.remove(player.getStringUUID());
			}
		} else {
			SERVER_CACHE.remove(player.getStringUUID());
		}
	}

	@Override
	public <T> @NotNull LazyOptional<T> getCapability(@NotNull final Capability<T> capability, final Direction side) {
		return capability == Capabilities.DRAGON_CAPABILITY ? instance.cast() : LazyOptional.empty();
	}

	@Override
	public CompoundTag serializeNBT(){
		return instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")).writeNBT();
	}

	@Override
	public void deserializeNBT(CompoundTag nbt){
		instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional must not be empty!")).readNBT(nbt);
	}
}