package by.dragonsurvivalteam.dragonsurvival.gametests;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonType;
import by.dragonsurvivalteam.dragonsurvival.config.ConfigHandler;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.body.DragonBody;
import by.dragonsurvivalteam.dragonsurvival.registry.dragon.stage.DragonStage;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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

    @SuppressWarnings("DataFlowIssue") // ignore
    public static void setToDragon(final GameTestHelper helper, final Player player, final AbstractDragonType dragonType, final ResourceKey<DragonBody> dragonBody, final ResourceKey<DragonStage> dragonStage) {
        DragonStateHandler data = DragonStateProvider.getData(player);

        data.setType(dragonType, player);
        helper.assertTrue(DragonUtils.isType(data, dragonType), String.format("Dragon type was [%s] - expected [%s]", data.getType(), dragonType));

        Holder<DragonBody> body = player.registryAccess().holderOrThrow(dragonBody);
        data.setBody(body, player);
        helper.assertTrue(DragonUtils.isBody(data, body), String.format("Dragon type was [%s] - expected [%s]", data.getBody(), dragonBody));

        Holder<DragonStage> stage = player.registryAccess().holderOrThrow(dragonStage);
        data.setSize(player, stage, stage.value().sizeRange().min());
        helper.assertTrue(data.getStage().is(stage), String.format("Dragon stage was [%s] - expected [%s]", data.getStage().getKey().location(), stage.getKey().location()));

        helper.assertTrue(data.isDragon(), "Player is not a dragon - expected player to be a dragon");
    }

    public static Player createPlayer(final GameTestHelper helper, final GameType type) {
        Player player = helper.makeMockPlayer(type);
        resetPlayer(helper, player);
        return player;
    }

    public static void resetPlayer(final GameTestHelper helper, final Player player) {
        DragonStateHandler data = DragonStateProvider.getData(player);
        data.revertToHumanForm(player, false);

        AbstractDragonType dragonType = data.getType();
        helper.assertTrue(dragonType == null, String.format("Dragon type was [%s] - expected [null]", dragonType));

        Holder<DragonBody> dragonBody = data.getBody();
        helper.assertTrue(dragonBody == null, String.format("Dragon body was [%s] - expected [null]", dragonBody));

        Holder<DragonStage> dragonStage = data.getStage();
        helper.assertTrue(dragonStage == null, String.format("Dragon level was [%s] - expected [null]", dragonStage));

        double size = data.getSize();
        helper.assertTrue(size == DragonStateHandler.NO_SIZE, String.format("Size was [%f] - expected [0]", size));
    }
}
