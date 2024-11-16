package by.dragonsurvivalteam.dragonsurvival.client.util;

import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;

import java.awt.Color;

public class RenderingUtils{
	static final double PI_TWO = Math.PI * 2.0;

	public static void clipRendering(int xPos, int yPos, int width, int height){
		double scale = Minecraft.getInstance().getWindow().getGuiScale();
		RenderSystem.enableScissor((int)(xPos * scale), (int)(height * scale - yPos * scale), (int)(width * scale), (int)(height * scale));
	}

	public static void clipRendering(int xPos, int yPos, int width, int height, Runnable runnable){
		double scale = Minecraft.getInstance().getWindow().getGuiScale();
		RenderSystem.enableScissor((int)(xPos * scale), (int)(yPos * scale), (int)(width * scale), (int)(height * scale));
		runnable.run();
		RenderSystem.disableScissor();
	}

	public static void disableClip(){
		RenderSystem.disableScissor();
	}


	public static void drawRect(PoseStack mStack, int x, int y, int width, int height, int color){
		Minecraft.getInstance().screen.hLine(mStack, x, x + width, y, color);
		Minecraft.getInstance().screen.hLine(mStack, x, x + width, y + height, color);
		Minecraft.getInstance().screen.vLine(mStack, x, y, y + height, color);
		Minecraft.getInstance().screen.vLine(mStack, x + width, y, y + height, color);
	}

	public static void drawRect(PoseStack mStack, int zLevel, int xPos, int yPos, int width, int heigth, int color){
		Color cc = new Color(color);
		Matrix4f mat = mStack.last().pose();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder buffer = tesselator.getBuilder();

		buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(mat, xPos + width, yPos, zLevel).color(cc.getRed(), cc.getGreen(), cc.getBlue(), cc.getAlpha()).endVertex();
		buffer.vertex(mat, xPos, yPos, zLevel).color(cc.getRed(), cc.getGreen(), cc.getBlue(), cc.getAlpha()).endVertex();
		buffer.vertex(mat, xPos, yPos + heigth, zLevel).color(cc.getRed(), cc.getGreen(), cc.getBlue(), cc.getAlpha()).endVertex();
		buffer.vertex(mat, xPos + width, yPos + heigth, zLevel).color(cc.getRed(), cc.getGreen(), cc.getBlue(), cc.getAlpha()).endVertex();
		// Insecure modifications
		tesselator.end();
	}

	public static void drawGradientRect(PoseStack mat, int zLevel, int left, int top, int right, int bottom, int[] color){
		drawGradientRect(mat.last().pose(), zLevel, left, top, right, bottom, color);
	}

	public static void drawGradientRect(Matrix4f mat, int zLevel, int left, int top, int right, int bottom, int[] color){
		float[] alpha = new float[4];
		float[] red = new float[4];
		float[] green = new float[4];
		float[] blue = new float[4];

		for(int i = 0; i < 4; i++){
			alpha[i] = (float)(color[i] >> 24 & 255) / 255.0F;
			red[i] = (float)(color[i] >> 16 & 255) / 255.0F;
			green[i] = (float)(color[i] >> 8 & 255) / 255.0F;
			blue[i] = (float)(color[i] & 255) / 255.0F;
		}

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferbuilder.vertex(mat, right, top, zLevel).color(red[0], green[0], blue[0], alpha[0]).endVertex();
		bufferbuilder.vertex(mat, left, top, zLevel).color(red[1], green[1], blue[1], alpha[1]).endVertex();
		bufferbuilder.vertex(mat, left, bottom, zLevel).color(red[2], green[2], blue[2], alpha[2]).endVertex();
		bufferbuilder.vertex(mat, right, bottom, zLevel).color(red[3], green[3], blue[3], alpha[3]).endVertex();
		// Insecure modifications
		//BufferUploader.draw(bufferbuilder.end());
		tesselator.end();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	public static void renderPureColorSquare(PoseStack mStack, int x, int y, int width, int height){
		Matrix4f mat = mStack.last().pose();
		int zLevel = 100;
		//BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferbuilder.begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

		for(int i = 0; i <= width; i++){
			float val = (float)i / width * 360f / 360f;
			Color top = new Color(Color.HSBtoRGB(val, 1f, 1f));
			bufferbuilder.vertex(mat, x + i, y, zLevel).color(top.getRed() / 255f, top.getGreen() / 255f, top.getBlue() / 255f, top.getAlpha() / 255f).endVertex();
			bufferbuilder.vertex(mat, x + i, y + height, zLevel).color(top.getRed() / 255f, top.getGreen() / 255f, top.getBlue() / 255f, top.getAlpha() / 255f).endVertex();
		}

		// Insecure modifications
		tesselator.end();
		//BufferUploader.draw(bufferbuilder.end());
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	public static void renderColorSquare(PoseStack mStack, int x, int y, int width, int height){
		Matrix4f mat = mStack.last().pose();
		int zLevel = 200;
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		bufferbuilder.begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

		for(int i = 0; i < width; i++){
			float val = (float)i / width * 360f / 360f;
			Color top = new Color(Color.HSBtoRGB(val, 1f, 0f));
			Color bot = new Color(Color.HSBtoRGB(val, 1f, 1f));

			bufferbuilder.vertex(mat, x + i, y, zLevel).color(top.getRed() / 255f, top.getGreen() / 255f, top.getBlue() / 255f, top.getAlpha() / 255f).endVertex();
			bufferbuilder.vertex(mat, x + i, y + height / 2f, zLevel).color(bot.getRed() / 255f, bot.getGreen() / 255f, bot.getBlue() / 255f, bot.getAlpha() / 255f).endVertex();
		}

		for(int i = 0; i < width; i++){
			float val = (float)i / width * 360f / 360f;
			Color top = new Color(Color.HSBtoRGB(val, 1f, 1f));
			Color bot = new Color(Color.HSBtoRGB(val, 0f, 1f));

			bufferbuilder.vertex(mat, x + i, y + height / 2f, zLevel).color(top.getRed() / 255f, top.getGreen() / 255f, top.getBlue() / 255f, top.getAlpha() / 255f).endVertex();
			bufferbuilder.vertex(mat, x + i, y + height, zLevel).color(bot.getRed() / 255f, bot.getGreen() / 255f, bot.getBlue() / 255f, bot.getAlpha() / 255f).endVertex();
		}

		// Insecure modifications
		//BufferUploader.draw(bufferbuilder.end());
		tesselator.end();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	public static void fill(PoseStack mStack, double pMinX, double pMinY, double pMaxX, double pMaxY, int pColor){
		Matrix4f pMatrix = mStack.last().pose();

		if(pMinX < pMaxX){
			double i = pMinX;
			pMinX = pMaxX;
			pMaxX = i;
		}

		if(pMinY < pMaxY){
			double j = pMinY;
			pMinY = pMaxY;
			pMaxY = j;
		}

		float f3 = (float)(pColor >> 24 & 255) / 255.0F;
		float f = (float)(pColor >> 16 & 255) / 255.0F;
		float f1 = (float)(pColor >> 8 & 255) / 255.0F;
		float f2 = (float)(pColor & 255) / 255.0F;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferbuilder.vertex(pMatrix, (float)pMinX, (float)pMaxY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.vertex(pMatrix, (float)pMaxX, (float)pMaxY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.vertex(pMatrix, (float)pMaxX, (float)pMinY, 0.0F).color(f, f1, f2, f3).endVertex();
		bufferbuilder.vertex(pMatrix, (float)pMinX, (float)pMinY, 0.0F).color(f, f1, f2, f3).endVertex();
		// Insecure modifications
		//BufferUploader.draw(bufferbuilder.end());
		tesselator.end();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	public static void drawTexturedCircle(PoseStack stack, double x, double y, double radius, double u, double v, double texRadius, int sides, double percent, double startAngle){
		Matrix4f matrix4f = stack.last().pose();

		double rad;
		double sin;
		double cos;

		double z = 100;

		RenderSystem.enableBlend();
		Tesselator tesselator = RenderSystem.renderThreadTesselator();
		final BufferBuilder buffer = tesselator.getBuilder();

		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

		buffer.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_TEX);
		buffer.vertex(matrix4f, (float)x, (float)y, (float)z).uv((float)u, (float)v).endVertex();

		for(int i = 0; i <= percent * sides; i++){
			rad = PI_TWO * ((double)i / (double)sides + startAngle);
			sin = Math.sin(rad);
			cos = Math.cos(rad);

			float xPos = (float)(x + sin * radius);
			float yPos = (float)(y + cos * radius);
			buffer.vertex(matrix4f, xPos, yPos, (float)z).uv((float)(u + sin * texRadius), (float)(v + cos * texRadius)).endVertex();
		}

		if(percent == 1.0){
			rad = PI_TWO * (percent + startAngle);
			sin = Math.sin(rad);
			cos = Math.cos(rad);
			buffer.vertex(matrix4f, (float)(x + sin * radius), (float)(y + cos * radius), (float)z).uv((float)(u + sin * texRadius), (float)(v + cos * texRadius)).endVertex();
		}

		tesselator.end();
		RenderSystem.disableBlend();
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		// Insecure modifications
	}

	public static void drawSmoothCircle(PoseStack stack, double x, double y, double radius, int sides, double percent, double startAngle){
		Matrix4f matrix4f = stack.last().pose();
		double rad;
		double sin;
		double cos;

		double z = 100;

		float[] colors = RenderSystem.getShaderColor();
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		Tesselator tesselator = Tesselator.getInstance();
		final BufferBuilder buffer = tesselator.getBuilder();

		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

		buffer.begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
		buffer.vertex(matrix4f, (float)x, (float)y, (float)z).color(colors[0], colors[1], colors[2], 1f).endVertex();

		for(int i = 0; i <= percent * sides; i++){
			rad = PI_TWO * ((double)i / (double)sides + startAngle);
			sin = Math.sin(rad);
			cos = Math.cos(rad);

			float xPos = (float)(x + sin * radius);
			float yPos = (float)(y + cos * radius);
			buffer.vertex(matrix4f, xPos, yPos, (float)z).color(colors[0], colors[1], colors[2], 1f).endVertex();
		}

		rad = PI_TWO * (percent + startAngle);
		sin = Math.sin(rad);
		cos = Math.cos(rad);
		buffer.vertex(matrix4f, (float)(x + sin * radius), (float)(y + cos * radius), (float)z).color(colors[0], colors[1], colors[2], 1f).endVertex();

		RenderSystem.disableBlend();
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		// Insecure modifications
		//BufferUploader.draw(buffer.end());
		tesselator.end();
	}

	public static void drawTexturedRing(PoseStack stack, double x, double y, double innerRadius, double outerRadius, double u, double v, double texInnerRadius, double texOuterRadius, int sides, double percent, double startAngle){
		Matrix4f matrix4f = stack.last().pose();

		float rad;
		float sin;
		float cos;

		float z = 100;

		RenderSystem.enableBlend();
		RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);

		GL11.glEnable(GL11.GL_LINE_SMOOTH);
		GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);

		Tesselator tesselator = Tesselator.getInstance();
		final BufferBuilder buffer =  tesselator.getBuilder();
		buffer.begin(Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_TEX);

		for(int i = 0; i <= percent * sides; i++){
			rad = (float)(PI_TWO * ((double)i / (double)sides + startAngle));
			sin = (float)Math.sin(rad);
			cos = (float)-Math.cos(rad);

			buffer.vertex(matrix4f, (float)(x + sin * outerRadius), (float)(y + cos * outerRadius), z).uv((float)(u + sin * texOuterRadius), (float)(v + cos * texOuterRadius)).endVertex();
			buffer.vertex(matrix4f, (float)(x + sin * innerRadius), (float)(y + cos * innerRadius), z).uv((float)(u + sin * texInnerRadius), (float)(v + cos * texInnerRadius)).endVertex();
		}

		rad = (float)(PI_TWO * (percent + startAngle));
		sin = (float)Math.sin(rad);
		cos = (float)-Math.cos(rad);

		buffer.vertex(matrix4f, (float)(x + sin * outerRadius), (float)(y + cos * outerRadius), z).uv((float)(u + sin * texOuterRadius), (float)(v + cos * texOuterRadius)).endVertex();
		buffer.vertex(matrix4f, (float)(x + sin * innerRadius), (float)(y + cos * innerRadius), z).uv((float)(u + sin * texInnerRadius), (float)(v + cos * texInnerRadius)).endVertex();

		tesselator.end();
		GL11.glDisable(GL11.GL_LINE_SMOOTH);
		RenderSystem.disableBlend();
	}
}