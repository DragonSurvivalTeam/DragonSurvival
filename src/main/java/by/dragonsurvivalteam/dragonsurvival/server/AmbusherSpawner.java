package by.dragonsurvivalteam.dragonsurvival.server;

import by.dragonsurvivalteam.dragonsurvival.common.entity.creatures.AmbusherEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.DSEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.ModifyCustomSpawnersEvent;
import org.jetbrains.annotations.NotNull;

@EventBusSubscriber // Initially coped from 'PatrolSpawner'
public class AmbusherSpawner implements CustomSpawner {
    private int nextTick;

    @Override
    public int tick(@NotNull ServerLevel level, boolean spawnEnemies, boolean spawnFriendlies) {
        if (!spawnEnemies || !level.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
            return 0;
        }

        nextTick--;

        if (nextTick > 0) {
            return 0;
        }

        // The random is used to vary the spawn rate a bit
        nextTick = nextTick + AmbusherEntity.SPAWN_FREQUENCY + level.getRandom().nextInt(AmbusherEntity.SPAWN_FREQUENCY / 10);

        if (!level.isDay() || level.getRandom().nextDouble() > AmbusherEntity.SPAWN_CHANCE) {
            return 0;
        }

        if (level.players().isEmpty()) {
            return 0;
        }

        Player player = level.players().get(level.getRandom().nextInt(level.players().size()));

        if (player.isCreative() || player.isSpectator()) {
            return 0;
        }

        int x = (24 + level.getRandom().nextInt(24)) * (level.getRandom().nextBoolean() ? -1 : 1);
        int z = (24 + level.getRandom().nextInt(24)) * (level.getRandom().nextBoolean() ? -1 : 1);
        BlockPos.MutableBlockPos spawnPosition = player.blockPosition().mutable().move(x, 0, z);

        if (!level.hasChunksAt(spawnPosition.getX() - 10, spawnPosition.getZ() - 10, spawnPosition.getX() + 10, spawnPosition.getZ() + 10)) {
            return 0;
        }

        if (level.getBiome(spawnPosition).is(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
            return 0;
        }

        spawnPosition.setY(level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, spawnPosition).getY());
        spawnAmbusher(level, spawnPosition, level.getRandom());

        return 1;
    }

    private void spawnAmbusher(ServerLevel level, BlockPos spawnPosition, RandomSource random) {
        BlockState blockstate = level.getBlockState(spawnPosition);

        if (!NaturalSpawner.isValidEmptySpawnBlock(level, spawnPosition, blockstate, blockstate.getFluidState(), DSEntities.HUNTER_AMBUSHER.get())) {
            return;
        }

        if (!PatrollingMonster.checkPatrollingMonsterSpawnRules(EntityType.PILLAGER, level, MobSpawnType.PATROL, spawnPosition, random)) {
            return;
        }

        AmbusherEntity ambusher = DSEntities.HUNTER_AMBUSHER.get().create(level);

        if (ambusher == null) {
            return;
        }

        ambusher.setPos(spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ());
        EventHooks.finalizeMobSpawn(ambusher, level, level.getCurrentDifficultyAt(spawnPosition), MobSpawnType.PATROL, null);
        level.addFreshEntityWithPassengers(ambusher);
    }

    @SubscribeEvent
    public static void addCustomSpawners(final ModifyCustomSpawnersEvent event) {
        event.addCustomSpawner(new AmbusherSpawner());
    }
}
