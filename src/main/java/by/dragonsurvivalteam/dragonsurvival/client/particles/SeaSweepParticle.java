package by.dragonsurvivalteam.dragonsurvival.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class SeaSweepParticle extends TextureSheetParticle{
	private final SpriteSet sprites;

	public SeaSweepParticle(ClientLevel p_i232341_1_, double p_i232341_2_, double p_i232341_4_, double p_i232341_6_, double p_i232341_8_, SpriteSet p_i232341_10_){
		super(p_i232341_1_, p_i232341_2_, p_i232341_4_, p_i232341_6_, 0.0D, 0.0D, 0.0D);
		sprites = p_i232341_10_;
		lifetime = 4;
		float f = random.nextFloat() * 0.6F + 0.4F;
		rCol = f;
		gCol = f;
		bCol = f;
		quadSize = 1.0F - (float)p_i232341_8_ * 0.5F;
		setSpriteFromAge(p_i232341_10_);
	}

	@Override
	public void tick(){
		xo = x;
		yo = y;
		zo = z;
		if(age++ >= lifetime){
			remove();
		}else{
			setSpriteFromAge(sprites);
		}
	}

	@Override
	public ParticleRenderType getRenderType(){
		return ParticleRenderType.PARTICLE_SHEET_LIT;
	}

	@Override
	public int getLightColor(float p_189214_1_){
		return 15728880;
	}

	@OnlyIn( Dist.CLIENT )
	public static class Factory implements DragonParticleProvider<SimpleParticleType> {
		private final SpriteSet sprites;

		public Factory(SpriteSet p_i50563_1_){
			sprites = p_i50563_1_;
		}

		@Override
		public Particle createParticle(SimpleParticleType p_199234_1_, ClientLevel p_199234_2_, double p_199234_3_, double p_199234_5_, double p_199234_7_, double p_199234_9_, double p_199234_11_, double p_199234_13_){
			return new SeaSweepParticle(p_199234_2_, p_199234_3_, p_199234_5_, p_199234_7_, p_199234_9_, sprites);
		}
	}
}