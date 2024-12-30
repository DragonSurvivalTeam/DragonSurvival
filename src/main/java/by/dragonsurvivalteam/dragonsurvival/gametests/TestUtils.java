package by.dragonsurvivalteam.dragonsurvival.gametests;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.DragonType;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.lang.reflect.Field;

public class TestUtils {
    public static final String AIR_CUBE_1X = "test_templates/1x1x1_air";
    public static final String AIR_CUBE_3X = "test_templates/3x3x3_air";

    /** Sets the block at {@link BlockPos#ZERO} and returns the resulting {@link BlockState} */
    public static BlockState setBlock(final GameTestHelper helper, final Block block) {
        return setBlock(helper, block, BlockPos.ZERO);
    }

    /** The position needs to be the non-absolute position (meaning without using {@link GameTestHelper#absolutePos(BlockPos)}) */
    public static BlockState setBlock(final GameTestHelper helper, final Block block, final BlockPos position) {
        helper.setBlock(position, block);
        BlockState state = helper.getBlockState(position);
        helper.assertTrue(state.is(block), "Block at position [" + position + "] was [" + state.getBlock() + "] - expected [" + block + "]");
        return helper.getBlockState(position);
    }

    public static boolean compare(final Object value, final Object fieldValue) {
        if (value instanceof Number number && fieldValue instanceof Number fieldNumber) {
            // 1 and 1.0 are otherwise not seen as equal
            return Float.compare(number.floatValue(), fieldNumber.floatValue()) == 0;
        } else {
            return value.equals(fieldValue);
        }
    }

    public static void setAndCheckConfig(final GameTestHelper helper, final String configKey, final Object value) {
        ConfigHandler.updateConfigValue(configKey, value);
        Field field = ConfigHandler.getField(configKey);

        try {
            Object fieldValue = field.get(null);
            helper.assertTrue(compare(value, fieldValue), "The field value [" + fieldValue + "] did not match the new value [" + value + "] after updating the config");
        } catch (IllegalAccessException exception) {
            helper.fail("Failed trying to access a field for the config [" + configKey + "] to validate the config: [" + exception.getMessage() + "]");
        }
    }

    public static void setToDragon(final GameTestHelper helper, final Player player, final ResourceKey<DragonType> dragonType, final ResourceKey<DragonBody> dragonBody, final ResourceKey<DragonStage> dragonStage) {
        DragonStateHandler data = DragonStateProvider.getData(player);

        Holder<DragonType> species = player.level().registryAccess().holderOrThrow(dragonType);
        data.setType(player, species);
        helper.assertTrue(data.species().is(dragonType), String.format("Dragon species was [%s] - expected [%s]", data.species(), species));

        Holder<DragonBody> body = player.registryAccess().holderOrThrow(dragonBody);
        data.setBody(player, body);
        helper.assertTrue(DragonUtils.isBody(data, body), String.format("Dragon type was [%s] - expected [%s]", data.body(), dragonBody));

        Holder<DragonStage> stage = player.registryAccess().holderOrThrow(dragonStage);
        data.setStage(player, stage);
        helper.assertTrue(data.stage().is(stage), String.format("Dragon stage was [%s] - expected [%s]", data.stage(), stage));

        helper.assertTrue(data.isDragon(), "Player is not a dragon - expected player to be a dragon");

        tick(player);
    }

    public static Player createPlayer(final GameTestHelper helper) {
        // FIXME :: crashes instantly due to 'NetworkRegistry#checkPacket'
        //  helper.makeMockPlayer() doesn't create a 'ServerPlayer' but rather a mix of client and server elements
        //noinspection removal -> ignore
        Player player = helper.makeMockServerPlayerInLevel();
        resetPlayer(helper, player);
        return player;
    }

    public static void resetPlayer(final GameTestHelper helper, final Player player) {
        DragonStateHandler data = DragonStateProvider.getData(player);
        data.revertToHumanForm(player, false);

        Holder<DragonType> dragonType = data.species();
        helper.assertTrue(dragonType == null, String.format("Dragon type was [%s] - expected [null]", dragonType));

        Holder<DragonBody> dragonBody = data.body();
        helper.assertTrue(dragonBody == null, String.format("Dragon body was [%s] - expected [null]", dragonBody));

        Holder<DragonStage> dragonStage = data.stage();
        helper.assertTrue(dragonStage == null, String.format("Dragon level was [%s] - expected [null]", dragonStage));

        double size = data.getSize();
        helper.assertTrue(size == DragonStateHandler.NO_SIZE, String.format("Size was [%f] - expected [0]", size));

        tick(player);
    }

    /** The mock player is not ticked, even if 'tick()' is explicitly called */
    public static void tick(final Player player) {
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Pre(player));
        NeoForge.EVENT_BUS.post(new PlayerTickEvent.Post(player));
    }
}
