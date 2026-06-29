package by.dragonsurvivalteam.dragonsurvival.client.render.entity.dragon;

import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

/**
 * Capturing the data to render after the model seems to be the intended approach <br/>
 * See <a href="https://discord.com/channels/730912704776110121/1045053167668310086/1508123420041285732">GeckoLib Discord</a> </br>
 * Otherwise the model rendering will break when glow (outline render type) is present
 */
public record RenderInfo(ItemStack stack, ItemDisplayContext displayContext, Matrix4f poseMatrix, Matrix3f normalMatrix) {
}
